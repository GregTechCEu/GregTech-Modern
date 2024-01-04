package com.gregtechceu.gtceu.api.item.tool.fabric;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.mininglevel.v1.MiningLevelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class ToolHelperImpl {

    public static boolean onBlockBreakEvent(Level level, GameType gameType, ServerPlayer player, BlockPos pos) {
        return PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(level, player, pos, level.getBlockState(pos), level.getBlockEntity(pos));
    }

    public static void onPlayerDestroyItem(Player player, ItemStack stack, InteractionHand hand) {

    }

    public static double getPlayerBlockReach(@NotNull Player player) {
        return 5.0F;
    }

    public static boolean isCorrectTierForDrops(BlockState state, int tier) {
        return MiningLevelManager.getRequiredMiningLevel(state) <= tier;
    }
}
