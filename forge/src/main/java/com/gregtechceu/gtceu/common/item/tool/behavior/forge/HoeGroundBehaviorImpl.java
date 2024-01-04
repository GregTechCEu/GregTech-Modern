package com.gregtechceu.gtceu.common.item.tool.behavior.forge;

import com.gregtechceu.gtceu.common.item.tool.behavior.HoeGroundBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.Nullable;

public class HoeGroundBehaviorImpl extends HoeGroundBehavior {
    public static HoeGroundBehavior create() {
        return new HoeGroundBehaviorImpl();
    }

    protected boolean isBlockTillable(ItemStack stack, Level world, Player player, BlockPos pos, UseOnContext context) {
        if (world.getBlockState(pos.above()).isAir()) {
            BlockState state = world.getBlockState(pos);
            BlockState newState = state.getToolModifiedState(context, ToolActions.HOE_TILL, false);
            return newState != null && newState != state;
        }
        return false;
    }

    protected boolean tillGround(UseOnContext context, BlockState state) {
        BlockState newState = state.getToolModifiedState(context, ToolActions.HOE_TILL, false);
        if (newState != null && newState != state) {
            context.getLevel().gameEvent(GameEvent.BLOCK_CHANGE, context.getClickedPos(), GameEvent.Context.of(context.getPlayer(), state));
            return context.getLevel().setBlock(context.getClickedPos(), newState, Block.UPDATE_ALL_IMMEDIATE);
        }
        return false;
    }
}
