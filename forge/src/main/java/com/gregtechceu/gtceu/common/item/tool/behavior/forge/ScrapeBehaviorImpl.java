package com.gregtechceu.gtceu.common.item.tool.behavior.forge;

import com.gregtechceu.gtceu.common.item.tool.behavior.ScrapeBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.Nullable;

public class ScrapeBehaviorImpl extends ScrapeBehavior {
    public static ScrapeBehavior create() {
        return new ScrapeBehaviorImpl();
    }

    protected boolean isBlockScrapable(ItemStack stack, Level level, Player player, BlockPos pos, UseOnContext context) {
        BlockState state = level.getBlockState(pos);
        BlockState newState = state.getToolModifiedState(context, ToolActions.AXE_SCRAPE, false);
        return newState != null && newState != state;
    }

    protected BlockState getScraped(BlockState unscrapedState, UseOnContext context) {
        // just assume it exists.
        BlockState newState = unscrapedState.getToolModifiedState(context, ToolActions.AXE_SCRAPE, false);
        return newState != null ? newState : unscrapedState;
    }
}
