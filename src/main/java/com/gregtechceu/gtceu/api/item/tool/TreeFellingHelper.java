package com.gregtechceu.gtceu.api.item.tool;

import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.*;
import java.util.stream.Collectors;

import static com.gregtechceu.gtceu.api.item.tool.ToolHelper.TREE_FELLING_KEY;
import static com.gregtechceu.gtceu.api.item.tool.ToolHelper.getBehaviorsTag;

public class TreeFellingHelper {

    private final ServerPlayer player;
    private final ItemStack tool;
    private final Deque<BlockPos> orderedBlocks;
    private int tick;

    private TreeFellingHelper(ServerPlayer player, ItemStack tool, Deque<BlockPos> orderedBlocks) {
        this.player = player;
        this.tool = tool;
        this.orderedBlocks = orderedBlocks;
        tick = 0;
    }

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
            Deque<BlockPos> orderedBlocks = visited.stream()
                    .sorted(Comparator.comparingInt(pos -> pos.getY() - originPos.getY()))
                    .collect(Collectors.toCollection(LinkedList::new));
            MinecraftForge.EVENT_BUS.register(new TreeFellingHelper(serverPlayer, stack, orderedBlocks));
            // breakBlocksPerTick(serverPlayer, stack, orderedBlocks, origin.getBlock());
        }
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.START && event.level == player.level() && event.side == LogicalSide.SERVER) {
            if (orderedBlocks.isEmpty() || tool.isEmpty() ||
                    !getBehaviorsTag(player.getMainHandItem()).getBoolean(TREE_FELLING_KEY)) {
                MinecraftForge.EVENT_BUS.unregister(this);
                return;
            }
            if (tick % ConfigHolder.INSTANCE.tools.treeFellingDelay == 0)
                ToolHelper.breakBlockRoutine(player, tool, orderedBlocks.removeLast(), true);
            tick++;
        }
    }
}
