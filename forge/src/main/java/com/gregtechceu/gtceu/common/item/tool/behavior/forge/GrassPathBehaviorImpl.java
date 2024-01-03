package com.gregtechceu.gtceu.common.item.tool.behavior.forge;

import com.gregtechceu.gtceu.common.item.tool.behavior.GrassPathBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolActions;

public class GrassPathBehaviorImpl extends GrassPathBehavior {
    public static GrassPathBehavior create() {
        return new GrassPathBehaviorImpl();
    }

    protected boolean isBlockPathConvertible(ItemStack stack, Level level, Player player, BlockPos pos, UseOnContext context) {
        if (level.getBlockState(pos.above()).isAir()) {
            BlockState state = level.getBlockState(pos);
            BlockState newState = state.getToolModifiedState(context, ToolActions.SHOVEL_FLATTEN, false);
            return newState != null && newState != state;
        }
        return false;
    }

    protected BlockState getFlattened(BlockState unFlattenedState, UseOnContext context) {
        return unFlattenedState.getToolModifiedState(context, ToolActions.SHOVEL_FLATTEN, false);
    }
}
