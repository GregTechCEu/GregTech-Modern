package com.gregtechceu.gtceu.api.item.tool;

import com.gregtechceu.gtceu.utils.TaskHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.*;
import java.util.stream.Collectors;

public class TreeFellingHelper {

    public static void fellTree(ItemStack stack, Level level, BlockState origin, BlockPos originPos,
                                LivingEntity miner) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        Queue<BlockPos> checking = new ArrayDeque<>();
        Set<BlockPos> visited = new ObjectOpenHashSet<>();

        checking.add(originPos);

        while (!checking.isEmpty()) {
            BlockPos check = checking.remove();
            if (check != originPos) {
                visited.add(check);
            }
            for (int y = 0; y <= 1; y++) {
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x != 0 || y != 0 || z != 0) {
                            mutablePos.set(check.getX() + x, check.getY() + y, check.getZ() + z);
                            if (!visited.contains(mutablePos)) {
                                // Check that the found block matches the original block state, which is wood.
                                if (origin.getBlock() == level.getBlockState(mutablePos).getBlock()) {
                                    if (!checking.contains(mutablePos)) {
                                        BlockPos immutablePos = mutablePos.immutable();
                                        checking.add(immutablePos);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!visited.isEmpty() && miner instanceof ServerPlayer serverPlayer) {
            List<BlockPos> orderedBlocks = visited.stream()
                    .sorted(Comparator.comparingInt(pos -> pos.getY() - originPos.getY()))
                    .collect(Collectors.toCollection(LinkedList::new));
            breakBlocksPerTick(serverPlayer, stack, orderedBlocks, origin.getBlock());
        }
    }

    public static void breakBlocksPerTick(ServerPlayer player, ItemStack tool, List<BlockPos> posList,
                                          Block originBlock) {
        for (int i = 0; i < posList.size(); i++) {
            int delayTick = i * 2; // 1 block per 2 tick
            BlockPos pos = posList.get(i);
            TaskHandler.enqueueServerTask(player.serverLevel(), () -> {
                if (player.level().getBlockState(pos).is(originBlock)) {
                    ToolHelper.breakBlockRoutine(player, tool, pos, true);
                }
            }, delayTick);
        }
    }
}
