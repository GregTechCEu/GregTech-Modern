package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.api.item.tool.behavior.ToolBehaviorType;
import com.gregtechceu.gtceu.data.tools.GTToolBehaviors;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Add to tools to have them deal bonus damage to specific mobs.
 * Pass null for the mobType parameter to ignore the tooltip.
 */
public class EntityDamageBehavior implements IToolBehavior<EntityDamageBehavior> {

    public static final Codec<EntityDamageBehavior> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.lenientOptionalFieldOf("mob_type", "")
                    .forGetter(val -> val.mobType == null ? "" : val.mobType),
            Codec.unboundedMap(TagKey.codec(Registries.ENTITY_TYPE), Codec.FLOAT).fieldOf("bonus_list")
                    .forGetter(val -> val.bonusList))
            .apply(instance, EntityDamageBehavior::new));

    public static final StreamCodec<ByteBuf, TagKey<EntityType<?>>> TAG_KEY_STREAM_CODEC = ResourceLocation.STREAM_CODEC
            .map(rl -> TagKey.create(Registries.ENTITY_TYPE, rl), TagKey::location);

    public static final StreamCodec<RegistryFriendlyByteBuf, EntityDamageBehavior> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ByteBufCodecs.stringUtf8(128)), val -> Optional.ofNullable(val.mobType),
            ByteBufCodecs.map(Object2FloatOpenHashMap::new, TAG_KEY_STREAM_CODEC, ByteBufCodecs.FLOAT),
            val -> val.bonusList,
            EntityDamageBehavior::new);

    private final Map<TagKey<EntityType<?>>, Float> bonusList = new Object2FloatOpenHashMap<>();
    private final String mobType;

    @SafeVarargs
    public EntityDamageBehavior(float bonus, TagKey<EntityType<?>>... entities) {
        this(null, bonus, entities);
    }

    public EntityDamageBehavior(Map<TagKey<EntityType<?>>, Float> entities) {
        this((String) null, entities);
    }

    public EntityDamageBehavior(String mobType, float bonus, TagKey<EntityType<?>>... entities) {
        this.mobType = mobType == null || mobType.isEmpty() ? null : mobType;
        for (TagKey<EntityType<?>> entity : entities) {
            bonusList.put(entity, bonus);
        }
    }

    public EntityDamageBehavior(String mobType, Map<TagKey<EntityType<?>>, Float> entities) {
        this.mobType = mobType == null || mobType.isEmpty() ? null : mobType;
        bonusList.putAll(entities);
    }

    public EntityDamageBehavior(Optional<String> mobType, Map<TagKey<EntityType<?>>, Float> entities) {
        this.mobType = mobType.orElse(null);
        bonusList.putAll(entities);
    }

    @Override
    public void hitEntity(@NotNull ItemStack stack, @NotNull LivingEntity target,
                          @NotNull LivingEntity attacker) {
        float damageBonus = bonusList.entrySet().stream().filter(entry -> target.getType().is(entry.getKey()))
                .map(Map.Entry::getValue).filter(f -> f > 0).findFirst().orElse(0f);
        if (damageBonus != 0f) {
            DamageSource source = attacker instanceof Player player ?
                    attacker.damageSources().playerAttack(player) : attacker.damageSources().mobAttack(attacker);
            target.hurt(source, damageBonus);
        }
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, Item.TooltipContext context, @NotNull List<Component> tooltip,
                               @NotNull TooltipFlag flag) {
        if (mobType != null && !mobType.isEmpty()) {
            tooltip.add(Component.translatable("item.gtceu.tool.behavior.damage_boost",
                    Component.translatable("item.gtceu.tool.behavior.damage_boost_" + mobType)));
        }
    }

    @Override
    public ToolBehaviorType<EntityDamageBehavior> getType() {
        return GTToolBehaviors.ENTITY_DAMAGE;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof EntityDamageBehavior that))
            return false;

        return bonusList.equals(that.bonusList) && Objects.equals(mobType, that.mobType);
    }

    @Override
    public int hashCode() {
        int result = bonusList.hashCode();
        result = 31 * result + Objects.hashCode(mobType);
        return result;
    }
}
