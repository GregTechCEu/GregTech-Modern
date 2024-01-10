package com.gregtechceu.gtceu.api.item.tool.forge;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ToolHelperImpl {

    public static boolean onBlockBreakEvent(Level level, GameType gameType, ServerPlayer player, BlockPos pos) {
        return ForgeHooks.onBlockBreakEvent(level, gameType, player, pos) != -1;
    }

    public static void onPlayerDestroyItem(Player player, ItemStack stack, InteractionHand hand) {
        ForgeEventFactory.onPlayerDestroyItem(player, stack, hand);
    }

    public static double getPlayerBlockReach(@NotNull Player player) {
        return player.getBlockReach();
    }

    public static boolean isCorrectTierForDrops(BlockState state, int tier) {
        return TierSortingRegistry.isCorrectTierForDrops(getTier(tier), state);
    }

    private static Tier getTier(int harvestLevel) {
        return TierSortingRegistry.getSortedTiers().stream().dropWhile(tier -> tier.getLevel() < harvestLevel || tier.getLevel() > harvestLevel).findAny().orElse(Tiers.WOOD);
    }

    public static boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, Player player) {
        return itemstack.onBlockStartBreak(pos, player);
    }
}
