package com.gregtechceu.gtceu.api.item.tool.forge;

import com.gregtechceu.gtceu.data.recipe.CustomTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.event.ForgeEventFactory;

public class ToolHelperImpl {

    public static boolean onBlockBreakEvent(Level level, GameType gameType, ServerPlayer player, BlockPos pos) {
        return ForgeHooks.onBlockBreakEvent(level, gameType, player, pos) != -1;
    }

    public static void onPlayerDestroyItem(Player player, ItemStack stack, InteractionHand hand) {
        ForgeEventFactory.onPlayerDestroyItem(player, stack, hand);
    }
}
