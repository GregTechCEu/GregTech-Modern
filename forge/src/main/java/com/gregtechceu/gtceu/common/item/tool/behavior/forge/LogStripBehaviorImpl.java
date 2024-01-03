package com.gregtechceu.gtceu.common.item.tool.behavior.forge;

import com.gregtechceu.gtceu.common.item.tool.behavior.LogStripBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.Nullable;

public class LogStripBehaviorImpl extends LogStripBehavior {
    public static LogStripBehavior create() {
        return new LogStripBehaviorImpl();
    }

    @Override
    protected boolean isBlockStrippable(ItemStack stack, Level level, Player player, BlockPos pos, UseOnContext context) {
        BlockState state = level.getBlockState(pos);
        BlockState newState = state.getToolModifiedState(context, ToolActions.AXE_STRIP, false);
        return newState != null && newState != state;
    }

    @Override
    protected BlockState getStripped(BlockState unscrapedState, UseOnContext context) {
        return unscrapedState.getToolModifiedState(context, ToolActions.AXE_STRIP, false);
    }
}
