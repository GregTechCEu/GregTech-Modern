package com.gregtechceu.gtceu.common.item.spoil_actions;

import com.gregtechceu.gtceu.api.item.spoilage.SpoilAction;
import com.gregtechceu.gtceu.api.item.spoilage.SpoilContext;
import com.gregtechceu.gtceu.api.item.spoilage.SpoilUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A spoil action which spawns an entity.
 */
@Accessors(fluent = true)
public class SpawnEntitySpoilAction extends SpoilAction {

    // spotless:off
    public static final MapCodec<SpawnEntitySpoilAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryFixedCodec.create(Registries.ENTITY_TYPE).fieldOf("entity_type").forGetter(SpawnEntitySpoilAction::entityType),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("entity_count").forGetter(SpawnEntitySpoilAction::entityCount)
    ).apply(instance, SpawnEntitySpoilAction::new));
    // spotless:on

    @Getter
    private final Holder<EntityType<?>> entityType;
    @Getter
    private final int entityCount;

    public SpawnEntitySpoilAction(Holder<EntityType<?>> entityType, int entityCount) {
        this.entityType = entityType;
        this.entityCount = entityCount;
    }

    public SpawnEntitySpoilAction(EntityType<?> entityType, int entityCount) {
        this.entityType = entityType.builtInRegistryHolder();
        this.entityCount = entityCount;
    }

    public SpawnEntitySpoilAction(EntityType<?> entityType) {
        this(entityType, 1);
    }


    @Override
    public ItemStack getSpoilResult(ItemStack currentSpoilResult, ItemStack stack, @NotNull SpoilContext spoilContext, boolean simulate) {
        if (!simulate) {
            EntityType<?> type = entityType.value();
            SpoilUtils.spawnEntity(spoilContext, type, stack.getCount());
        }
        return currentSpoilResult;
    }

    @Override
    public void appendTooltip(List<Component> tooltips, ItemStack stack) {
        EntityType<?> type = entityType.value();
        MutableComponent component = type.getDescription().copy();
        tooltips.add(Component.literal(entityCount + "x ").withStyle(ChatFormatting.GREEN).append(component));
    }

    @Override
    public MapCodec<? extends SpoilAction> codec() {
        return CODEC;
    }
}
