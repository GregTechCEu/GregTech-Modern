package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.api.item.tool.behavior.ToolBehaviorType;
import com.gregtechceu.gtceu.common.data.GTToolBehaviors;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import com.google.common.base.Strings;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Add to tools to have them deal bonus damage to specific mobs.
 * Pass {@code null} or {@link Optional#empty()} for the {@code mobType} parameter to ignore the tooltip.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class EntityDamageBehavior implements IToolBehavior<EntityDamageBehavior> {

    // spotless:off
    private static final Codec<Object2FloatMap<HolderSet<EntityType<?>>>> BONUS_LIST_CODEC = Codec.mapPair(
                    RegistryCodecs.homogeneousList(Registries.ENTITY_TYPE).fieldOf("entities"),
                    Codec.FLOAT.fieldOf("bonus")
            ).codec().listOf()
            .xmap(list -> {
                Object2FloatOpenHashMap<HolderSet<EntityType<?>>> map = new Object2FloatOpenHashMap<>();
                for (var pair : list) {
                    map.put(pair.getFirst(), pair.getSecond().floatValue());
                }
                return map;
            }, map -> {
                List<Pair<HolderSet<EntityType<?>>, Float>> list = new ArrayList<>();
                for (var entry : map.object2FloatEntrySet()) {
                    list.add(Pair.of(entry.getKey(), entry.getFloatValue()));
                }
                return list;
            });

    public static final Codec<EntityDamageBehavior> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.lenientOptionalFieldOf("mob_type").forGetter(val -> val.mobType),
            BONUS_LIST_CODEC.fieldOf("bonus_list").forGetter(EntityDamageBehavior::getBonusList)
    ).apply(instance, EntityDamageBehavior::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, Object2FloatMap<HolderSet<EntityType<?>>>> BONUS_LIST_STREAM_CODEC = ByteBufCodecs.map(
            Object2FloatOpenHashMap::new,
            ByteBufCodecs.holderSet(Registries.ENTITY_TYPE), ByteBufCodecs.FLOAT
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, EntityDamageBehavior> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ByteBufCodecs.stringUtf8(128)), val -> val.mobType,
            BONUS_LIST_STREAM_CODEC, EntityDamageBehavior::getBonusList,
            EntityDamageBehavior::new);
    // spotless:on

    private @Nullable Reference2FloatMap<TagKey<EntityType<?>>> tempTagBonusList = null;
    private boolean tagBonusesLoaded = false;
    private final Object2FloatMap<HolderSet<EntityType<?>>> bonusList = new Object2FloatOpenHashMap<>();
    private final Optional<String> mobType;

    @SafeVarargs
    public EntityDamageBehavior(float bonus, TagKey<EntityType<?>>... entities) {
        this(null, bonus, entities);
    }

    public EntityDamageBehavior(float bonus, EntityType<?>... entities) {
        this(null, bonus, entities);
    }

    @SafeVarargs
    public EntityDamageBehavior(String mobType, float bonus, TagKey<EntityType<?>>... entities) {
        this.mobType = Strings.isNullOrEmpty(mobType) ? Optional.empty() : Optional.of(mobType);
        tempTagBonusList = new Reference2FloatOpenHashMap<>();
        for (TagKey<EntityType<?>> entity : entities) {
            tempTagBonusList.put(entity, bonus);
        }
    }

    public EntityDamageBehavior(String mobType, float bonus, EntityType<?>... entities) {
        this(mobType, bonus, Arrays.stream(entities)
                .<Holder<EntityType<?>>>map(EntityType::builtInRegistryHolder)
                .toList());
    }

    public EntityDamageBehavior(String mobType, float bonus, List<Holder<EntityType<?>>> entities) {
        this.mobType = Strings.isNullOrEmpty(mobType) ? Optional.empty() : Optional.of(mobType);
        bonusList.put(HolderSet.direct(entities), bonus);
    }

    public EntityDamageBehavior(Optional<String> mobType, Map<HolderSet<EntityType<?>>, Float> entities) {
        this.mobType = mobType;
        bonusList.putAll(entities);
    }

    public Object2FloatMap<HolderSet<EntityType<?>>> getBonusList() {
        if (!tagBonusesLoaded) {
            tagBonusesLoaded = true;
            if (tempTagBonusList == null || tempTagBonusList.isEmpty()) return bonusList;
            for (var entry : Reference2FloatMaps.fastIterable(tempTagBonusList)) {
                HolderSet.Named<EntityType<?>> tag = BuiltInRegistries.ENTITY_TYPE.getOrCreateTag(entry.getKey());
                bonusList.put(tag, entry.getFloatValue());
            }
        }
        return bonusList;
    }

    public void reloadBonusList() {
        tagBonusesLoaded = false;
        if (tempTagBonusList == null || tempTagBonusList.isEmpty()) return;
        bonusList.keySet().removeIf(holderSet -> {
            var maybeKey = holderSet.unwrapKey();
            return maybeKey.filter(key -> tempTagBonusList.containsKey(key)).isPresent();
        });
    }

    @Override
    public void hitEntity(@NotNull ItemStack stack, @NotNull LivingEntity target,
                          @NotNull LivingEntity attacker) {
        float damageBonus = getDamageBonus(target);
        if (damageBonus != 0f) {
            DamageSource source = attacker instanceof Player player ?
                    attacker.damageSources().playerAttack(player) : attacker.damageSources().mobAttack(attacker);
            target.hurt(source, damageBonus);
        }
    }

    private float getDamageBonus(@NotNull LivingEntity target) {
        for (var entry : Object2FloatMaps.fastIterable(getBonusList())) {
            if (target.getType().is(entry.getKey())) {
                float f = entry.getFloatValue();
                if (f > 0) return f;
            }
        }
        return 0f;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, Item.TooltipContext context, @NotNull List<Component> tooltip,
                               @NotNull TooltipFlag flag) {
        // noinspection OptionalIsPresent
        if (mobType.isPresent()) {
            tooltip.add(Component.translatable("item.gtceu.tool.behavior.damage_boost",
                    Component.translatable("item.gtceu.tool.behavior.damage_boost_" + mobType.get())));
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

        return getBonusList().equals(that.getBonusList()) && Objects.equals(mobType, that.mobType);
    }

    @Override
    public int hashCode() {
        int result = getBonusList().hashCode();
        result = 31 * result + Objects.hashCode(mobType);
        return result;
    }
}
