package com.gregtechceu.gtceu.api.multiblock.util;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.multiblock.PredicateContext;
import com.gregtechceu.gtceu.common.item.behavior.TerminalBehavior;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.common.network.packets.SPacketAutobuildHighlight;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.config.Actionable;
import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;
import appeng.util.Platform;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Map;

import static com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine.DEFAULT_STRUCTURE;

public class AutobuildHelper {

    /*
     * iterate over every position in the structure
     * for each block
     * - if that block is part of the structure and valid in that predicate, add to alreadyPlaced
     * - if that block can be replaced(air, tall grass, etc? block property replaceable) add to replaceableBlocks
     * - if that block CANT be replaced and not valid, add to canNotPlace,
     * maybe add what already exists there to another list for reporting(invalidBlocks)?
     * 
     * 
     * for each replaceableBlock
     * - if that candidate from the predicate exists in the inventory, add to some blocksToRemove list
     * (small size for chunked building)
     * figure out the auto placement action
     * - if the candidate does not exist, add to blocksMissing(for later reporting)
     */

    public static void autobuild(ServerPlayer player, ItemStack item, MultiblockMachineDefinition definition,
                                 MultiblockControllerMachine controller, Map<BlockPos, BlockInfo> blocksToPlace,
                                 AbstractStructureHelper structureHelper, boolean isFlipped) {
        Long2ObjectOpenHashMap<BlockState> alreadyValidPlaced = new Long2ObjectOpenHashMap<>();
        Long2ObjectOpenHashMap<BlockState> replaceableBlocks = new Long2ObjectOpenHashMap<>();
        Long2ObjectOpenHashMap<BlockState> canNotPlaceBlocks = new Long2ObjectOpenHashMap<>();

        Level level = player.level();

        Block controllerBlock = controller.getDefinition().getBlock();
        BlockPos schemaControllerPos = BlockPos.ZERO;
        for (var entry : blocksToPlace.entrySet()) {
            if (entry.getValue().getBlockState().is(controllerBlock)) {
                schemaControllerPos = entry.getKey();
                break;
            }
        }

        BlockPos controllerOffset = controller.getBlockPos().subtract(schemaControllerPos);

        PredicateContext cxt = new PredicateContext(null);
        cxt.updateLevel(level);
        int checkedBlocks = 0;
        for (var entry : blocksToPlace.entrySet()) {
            BlockPos pos = entry.getKey().offset(controllerOffset);
            var blockState = level.getBlockState(pos);

            var predicate = structureHelper.getPredicateFromPos(
                    definition.getStructurePatterns().get(DEFAULT_STRUCTURE).get(),
                    entry.getKey(), controller.getFrontFacing(), controller.getUpwardsFacing(), isFlipped);

            cxt.updatePos(pos);
            var innerPredicate = predicate.getPredicateAtPos(cxt);
            if (innerPredicate.hasMatched()) {
                alreadyValidPlaced.put(pos.asLong(), blockState);
            } else if (entry.getValue().getBlockState().isAir() && blockState.canBeReplaced()) {
                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            } else {
                if (blockState.canBeReplaced()) {
                    replaceableBlocks.put(pos.asLong(), blocksToPlace.get(entry.getKey()).getBlockState());
                    checkedBlocks++;
                } else {
                    canNotPlaceBlocks.put(pos.asLong(), blockState);
                }
            }
            if (checkedBlocks > 32) {
                break;
            }
        }

        // Step 1. Making the "what we want" list
        Map<Item, Integer> whatWeWant = new Object2IntArrayMap<>();
        for (var entry : replaceableBlocks.long2ObjectEntrySet()) {
            whatWeWant.merge(entry.getValue().getBlock().asItem(), 1, Integer::sum);
        }

        // Step 2. Try to fetch from inventory
        Map<Item, Integer> whatWeHave = new Object2IntArrayMap<>();
        for (var entry : whatWeWant.entrySet()) {
            Item desiredItem = entry.getKey();
            int desiredAmount = entry.getValue();
            var slotIndex = player.getInventory().findSlotMatchingItem(new ItemStack(desiredItem));
            if (slotIndex == -1) continue;
            var playerSlotStack = player.getInventory().getItem(slotIndex);
            int toDeduct = Math.min(playerSlotStack.getCount(), desiredAmount);
            playerSlotStack.shrink(toDeduct);
            whatWeHave.put(desiredItem, toDeduct);
        }

        // Step 2a. Calculate remaining items
        Map<Item, Integer> whatWeDontHave = new Object2IntArrayMap<>();
        for (var entry : whatWeWant.entrySet()) {
            Item item1 = entry.getKey();
            int count = whatWeHave.getOrDefault(item1, 0);
            if (count < entry.getValue()) {
                whatWeDontHave.put(item1, entry.getValue() - count);
            }
        }

        // Step 2b. Try to fetch from AE
        Map<Item, Integer> whatWeHaveAE = new Object2IntArrayMap<>();
        if (GTCEu.Mods.isAE2Loaded()) {
            whatWeHaveAE = AEWrapper.tryGrid(whatWeDontHave, level, item, player);
        }

        printBlockList(Component.translatable("gtceu.autobuild.ae_blocks").withStyle(ChatFormatting.AQUA), whatWeHaveAE,
                player);

        Map<Item, Integer> whatWePlaced = new Object2IntArrayMap<>();
        // Step 3. Place what was fetched
        for (var entry : replaceableBlocks.long2ObjectEntrySet()
                .stream()
                .sorted(Comparator.comparingLong(Long2ObjectMap.Entry::getLongKey))
                .toList()) {
            var blockState = entry.getValue();
            var blocksLeft = whatWeHave.merge(blockState.getBlock().asItem(), -1, Integer::sum);
            if (blocksLeft < 0) {
                blocksLeft = whatWeHaveAE.merge(blockState.getBlock().asItem(), -1, Integer::sum);
                if (blocksLeft < 0) continue;
            }
            whatWeWant.merge(blockState.getBlock().asItem(), -1, Integer::sum);
            whatWePlaced.merge(blockState.getBlock().asItem(), 1, Integer::sum);
            level.setBlockAndUpdate(BlockPos.of(entry.getLongKey()), blockState);
        }

        printBlockList(Component.translatable("gtceu.autobuild.placed_blocks").withStyle(ChatFormatting.GREEN),
                whatWePlaced, player);
        printBlockList(Component.translatable("gtceu.autobuild.missing_blocks").withStyle(ChatFormatting.RED),
                whatWeWant, player);

        if (!canNotPlaceBlocks.isEmpty()) player.displayClientMessage(
                Component.translatable("gtceu.autobuild.unplaced_blocks").withStyle(ChatFormatting.RED), false);
        if (!canNotPlaceBlocks.isEmpty())
            GTNetwork.sendToPlayer(player, new SPacketAutobuildHighlight(canNotPlaceBlocks.keySet().toLongArray()));
    }

    public static void printBlockList(Component firstMessage, Map<Item, Integer> blockList, Player player) {
        if (!blockList.isEmpty()) {
            player.displayClientMessage(
                    firstMessage, false);
            MutableComponent toPrint = Component.empty();
            boolean first = true;
            for (var entry : blockList.entrySet()) {
                if (entry.getValue() <= 0) continue;
                if (!first) toPrint = toPrint.append(",\n");
                toPrint = toPrint.append(entry.getValue() + "x ").append(entry.getKey().getDescription());
                first = false;
            }
            player.displayClientMessage(toPrint, false);
        }
    }

    public class AEWrapper {

        @Nullable
        public static IGrid getLinkedGrid(ItemStack stack, Level level, Player player) {
            if (!(level instanceof ServerLevel serverLevel)) {
                return null;
            }

            var linkedPos = TerminalBehavior.getLinkedPos(stack);
            if (linkedPos == null) {
                player.displayClientMessage(Component.translatable("gtceu.terminal.not_linked"), true);
                return null;
            }

            var linkedLevel = serverLevel.getServer().getLevel(linkedPos.dimension());
            if (linkedLevel == null) {
                player.displayClientMessage(Component.translatable("gtceu.terminal.network_missing"), true);
                return null;
            }

            var be = Platform.getTickingBlockEntity(linkedLevel, linkedPos.pos());
            if (!(be instanceof IWirelessAccessPoint accessPoint)) {
                player.displayClientMessage(Component.translatable("gtceu.terminal.network_missing"), true);
                return null;
            }

            var grid = accessPoint.getGrid();
            if (grid == null) {
                player.displayClientMessage(Component.translatable("gtceu.terminal.network_missing"), true);
                return null;
            }
            return grid;
        }

        public static Map<Item, Integer> tryGrid(Map<Item, Integer> itemMap, Level level, ItemStack stack,
                                                 Player player) {
            Map<Item, Integer> ret = new Object2IntOpenHashMap<>();

            var grid = getLinkedGrid(stack, level, player);
            if (grid == null) return ret;

            MEStorage storage = grid.getStorageService().getInventory();
            for (var entry : itemMap.entrySet()) {
                int value = (int) storage.extract(AEItemKey.of(entry.getKey()), entry.getValue(),
                        Actionable.MODULATE, IActionSource.ofPlayer(player));
                if (value > 0) {
                    ret.put(entry.getKey(), value);
                }
            }
            return ret;
        }
    }
}
