package com.gregtechceu.gtceu.api.item.spoilage;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import org.jetbrains.annotations.Nullable;

public class SpoilUtils {

    /**
     * Initializes this ItemStack's spoilage timer if it wasn't initialized before.
     * Should be called when it finishes crafting, for example.
     */
    public static void update(ItemStack stack, SpoilContext spoilContext) {
        ISpoilableItem spoilable = GTCapabilityHelper.getSpoilable(stack);
        if (spoilable != null) spoilable.updateFreshness(spoilContext, true);
    }

    public static void updateBlock(Level level, BlockPos pos) {
        for (Direction side : Direction.values()) updateBlock(level, pos, side);
        updateBlock(level, pos, null);
    }

    public static void updateBlock(Level level, BlockPos pos, Direction side) {
        if (level == null) return;
        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, side);
        if (handler != null)
            updateHandler(handler, level, pos, side);
    }

    public static void updateHandler(IItemHandler handler, Level level, @Nullable BlockPos pos,
                                     @Nullable Direction side) {
        SpoilContext ctx = new SpoilContext(level, pos)
                .withItemHandlerSide(side);
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            update(handler.getStackInSlot(slot), ctx.withSlot(slot));
        }
    }

    public static void spawnEntity(SpoilContext spoilContext, EntityType<?> type, int count) {
        if (spoilContext.level() instanceof ServerLevel level) {
            BlockPos pos = null;
            if (spoilContext.entity() != null) pos = spoilContext.entity().blockPosition();
            else if (spoilContext.pos() != null) pos = spoilContext.pos();
            if (pos != null) {
                if (level.getBlockState(pos).isSuffocating(level, pos)) {
                    for (Direction direction : Direction.values()) {
                        BlockPos relative = pos.relative(direction);
                        if (!level.getBlockState(relative).isSuffocating(level, relative)) {
                            pos = relative;
                            break;
                        }
                    }
                }
                for (int i = 0; i < count; i++) type.spawn(level, pos, MobSpawnType.SPAWN_EGG);
            }
        }
    }
}
