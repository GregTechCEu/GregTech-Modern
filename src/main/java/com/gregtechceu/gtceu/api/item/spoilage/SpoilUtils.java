package com.gregtechceu.gtceu.api.item.spoilage;

import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.Nullable;

public class SpoilUtils {

    public static boolean spoilageEnabled() {
        return GTMixinPlugin.isOptionEnabled(GTEarlyConfig.SPOILAGE);
    }

    public static void mergeSpoilables(ItemStack stack, ItemStack other) {
        if (stack.isEmpty() || other.isEmpty()) return;

        var cap1 = GTCapabilityHelper.getSpoilable(stack);
        var cap2 = GTCapabilityHelper.getSpoilable(other);
        if (cap1 != null && cap2 != null) {
            cap1.prepareForComparisonWith(cap2);
        }
    }

    /**
     * Checks if an item and tags are the same, with support for spoilage
     * 
     * @return If the item stacks and their non-spoilage data are the same, and checks that the spoilage items are
     *         compatible.
     */
    public static boolean isSameItemSameTagsSpoilable(ItemStack a, ItemStack b) {
        if (!ItemStack.isSameItem(a, b)) return false; // both have same item
        if (a.isEmpty() != b.isEmpty()) return false; // only one is empty, the other isn't

        var aCap = a.getCapability(GTCapability.CAPABILITY_SPOILABLE_ITEM);
        var bCap = b.getCapability(GTCapability.CAPABILITY_SPOILABLE_ITEM);

        if (aCap == null || bCap == null) return ItemStack.isSameItemSameComponents(a, b); // at least one doesn't
                                                                                           // have the capability,
                                                                                           // fall back to vanilla

        ItemStack aCopy = a.copy();
        aCopy.remove(GTDataComponents.SPOILAGE_CREATION_TICK);
        aCopy.remove(GTDataComponents.SPOILAGE_FREEZE_TICK);

        ItemStack bCopy = b.copy();
        bCopy.remove(GTDataComponents.SPOILAGE_CREATION_TICK);
        bCopy.remove(GTDataComponents.SPOILAGE_FREEZE_TICK);

        return ItemStack.isSameItemSameComponents(aCopy, bCopy);
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
        for (Direction side : GTUtil.DIRECTIONS) updateBlock(level, pos, side);
        updateBlock(level, pos, null);
    }

    public static void updateBlock(BlockEntity blockEntity) {
        updateBlock(blockEntity.getLevel(), blockEntity.getBlockPos());
    }

    public static void updateBlock(Level level, BlockPos pos, @Nullable Direction side) {
        var cap = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, side);
        if (cap != null) updateBlockItemHandler(cap, level, pos);
    }

    public static void updateBlockItemHandler(IItemHandler handler, @Nullable Level level, @Nullable BlockPos pos) {
        if (!(handler instanceof IItemHandlerModifiable modifiable)) return;

        for (int slot = 0; slot < modifiable.getSlots(); slot++) {
            ItemStack stack = modifiable.getStackInSlot(slot);
            if (stack.isEmpty()) continue;

            ISpoilableItem spoilable = GTCapabilityHelper.getSpoilable(stack);
            if (spoilable != null) {
                spoilable.updateFreshness(new SpoilContext(level, pos, modifiable, slot), true);
            }
        }
    }

    public static void updateEntity(Entity entity) {
        if (entity instanceof ItemEntity itemEntity) {
            SpoilUtils.update(itemEntity.getItem(),
                    new SpoilContext(entity.level(), entity.blockPosition(), entity, itemEntity::setItem));
        }

        var cap = entity.getCapability(Capabilities.ItemHandler.ENTITY);

        if (cap != null) updateEntityItemHandler(cap, entity);
    }

    public static void updateEntityItemHandler(IItemHandler handler, Entity entity) {
        if (!(handler instanceof IItemHandlerModifiable modifiable)) return;

        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = modifiable.getStackInSlot(slot);
            if (stack.isEmpty()) continue;

            ISpoilableItem spoilable = GTCapabilityHelper.getSpoilable(stack);
            if (spoilable != null) {
                spoilable.updateFreshness(new SpoilContext(entity, modifiable, slot), true);
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
