package com.gregtechceu.gtceu.api.item.spoilage;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.core.config.GTEarlyConfig;
import com.gregtechceu.gtceu.core.mixins.GTMixinPlugin;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

import org.jetbrains.annotations.Nullable;

public class SpoilUtils {

    public static boolean spoilageEnabled() {
        return GTMixinPlugin.isOptionEnabled(GTEarlyConfig.SPOILAGE);
    }

    public static void mergeSpoilage(ItemStack stack, ItemStack other) {
        if (stack.isEmpty() || other.isEmpty() || !stack.hasTag() || !other.hasTag() ||
                stack.getOrCreateTag().equals(other.getOrCreateTag()))
            return;
        var cap1 = GTCapabilityHelper.getSpoilable(stack);
        var cap2 = GTCapabilityHelper.getSpoilable(other);
        if (cap1 != null && cap2 != null) {
            cap1.prepareForComparisonWith(cap2);
        }
    }

    /**
     * Initializes this ItemStack's spoilage timer if it wasn't initialized before.
     * Should be called when it finishes crafting, for example.
     */
    public static void update(ItemStack stack, SpoilContext spoilContext) {
        ISpoilableItem spoilable = GTCapabilityHelper.getSpoilable(stack);
        if (spoilable != null) spoilable.updateFreshness(spoilContext, true);
    }

    public static void updateBlock(Level level, BlockPos pos) {
        var be = level.getBlockEntity(pos);
        if (be == null) return;
        updateBlock(be);
    }

    public static void updateBlock(BlockEntity blockEntity) {
        for (Direction side : GTUtil.DIRECTIONS) updateBlock(blockEntity, side);
        updateBlock(blockEntity, null);
    }

    public static void updateBlock(BlockEntity blockEntity, @Nullable Direction side) {
        blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, side)
                .resolve()
                .ifPresent(
                        handler -> updateBlockItemHandler(handler, blockEntity.getLevel(), blockEntity.getBlockPos()));
    }

    public static void updateBlockItemHandler(IItemHandler handler, @Nullable Level level, @Nullable BlockPos pos) {
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (stack.isEmpty()) continue;

            ISpoilableItem spoilable = GTCapabilityHelper.getSpoilable(stack);
            if (spoilable != null) {
                spoilable.updateFreshness(new SpoilContext(level, pos, handler, slot), true);
            }
        }
    }

    public static void updateEntity(Entity entity) {
        if (entity instanceof ItemEntity itemEntity) {
            SpoilUtils.update(itemEntity.getItem(),
                    new SpoilContext(entity.level(), entity.blockPosition(), entity, itemEntity::setItem));
        }

        // technically this updates player's armor/offhand slots 3 times, but I don't think that matters too much.
        for (Direction side : GTUtil.DIRECTIONS) updateEntity(entity, side);
        updateEntity(entity, null);
    }

    public static void updateEntity(Entity entity, @Nullable Direction context) {
        entity.getCapability(ForgeCapabilities.ITEM_HANDLER, context)
                .ifPresent(handler -> updateEntityItemHandler(handler, entity));
    }

    public static void updateEntityItemHandler(IItemHandler handler, Entity entity) {
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (stack.isEmpty()) continue;

            ISpoilableItem spoilable = GTCapabilityHelper.getSpoilable(stack);
            if (spoilable != null) {
                spoilable.updateFreshness(new SpoilContext(entity, handler, slot), true);
            }
        }
    }

    public static void spawnEntity(SpoilContext spoilContext, EntityType<? extends Mob> type, int count) {
        if (!(spoilContext.level() instanceof ServerLevel level)) return;

        BlockPos pos = spoilContext.pos();
        if (pos == null) return;

        if (level.getBlockState(pos).isSuffocating(level, pos)) {
            for (Direction side : GTUtil.DIRECTIONS) {
                BlockPos relative = pos.relative(side);
                if (!level.getBlockState(relative).isSuffocating(level, relative)) {
                    pos = relative;
                    break;
                }
            }
        }
        for (int i = 0; i < count; i++) {
            type.spawn(level, pos, MobSpawnType.CONVERSION);
        }
    }
}
