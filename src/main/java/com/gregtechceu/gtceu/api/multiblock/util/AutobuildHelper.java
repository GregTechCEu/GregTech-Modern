package com.gregtechceu.gtceu.api.multiblock.util;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.multiblock.PredicateContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.Map;

import static com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine.DEFAULT_STRUCTURE;

public class AutobuildHelper {

    // todo remove
    public static Long2ObjectMap<BlockInfo> readBlockPreferences(CompoundTag tag) {
        Long2ObjectMap<BlockInfo> blockPreferences = new Long2ObjectOpenHashMap<>();
        if (!tag.contains("blockPreferences", CompoundTag.TAG_LIST)) {
            return blockPreferences;
        }

        var preferences = tag.getList("blockPreferences", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < preferences.size(); i++) {
            CompoundTag preference = preferences.getCompound(i);
            BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(),
                    preference.getCompound("state"));
            if (state.isAir()) continue;
            blockPreferences.put(preference.getLong("pos"), BlockInfo.fromBlockState(state));
        }
        return blockPreferences;
    }

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

    public static void autobuild(Player player, ItemStack item, MultiblockMachineDefinition definition,
                                 MultiblockControllerMachine controller, Map<BlockPos, BlockInfo> blocksToPlace, AbstractStructureHelper structureHelper) {
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

            var predicate = structureHelper.getPredicateFromPos(definition.getStructurePatterns().get(DEFAULT_STRUCTURE).get(),
                    entry.getKey(), controller.getFrontFacing(), controller.getUpwardsFacing(), controller.isFlipped());

            cxt.updatePos(pos);
            var innerPredicate = predicate.getPredicateAtPos(cxt);
            if (innerPredicate != null) {
                alreadyValidPlaced.put(pos.asLong(), blockState);
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

        for (var entry : replaceableBlocks.entrySet()) {
            // inventory check

        }

    }
}
