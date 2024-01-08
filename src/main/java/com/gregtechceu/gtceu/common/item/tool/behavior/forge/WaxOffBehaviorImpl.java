package com.gregtechceu.gtceu.common.item.tool.behavior.forge;

import com.gregtechceu.gtceu.common.item.tool.behavior.WaxOffBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.Nullable;

public class WaxOffBehaviorImpl extends WaxOffBehavior {
    public static WaxOffBehavior create() {
        return new WaxOffBehaviorImpl();
    }

    @Override
    protected boolean isBlockUnWaxable(ItemStack stack, Level level, Player player, BlockPos pos, UseOnContext context) {
        BlockState state = level.getBlockState(pos);
        BlockState newState = state.getToolModifiedState(context, ToolActions.AXE_WAX_OFF, false);
        return newState != null && newState != state;
    }

    protected BlockState getUnWaxed(BlockState unscrapedState, UseOnContext context) {
        return unscrapedState.getToolModifiedState(context, ToolActions.AXE_WAX_OFF, false);
    }
}
