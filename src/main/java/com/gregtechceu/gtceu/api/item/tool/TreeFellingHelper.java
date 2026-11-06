package com.gregtechceu.gtceu.api.item.tool;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.data.GTToolBehaviors;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.*;
import java.util.stream.Collectors;

import static com.gregtechceu.gtceu.api.item.tool.ToolHelper.*;

@EventBusSubscriber(modid = GTCEu.MOD_ID)
public class TreeFellingHelper {

    private final ServerPlayer player;
    private final ItemStack tool;
    private final Deque<BlockPos> orderedBlocks;
    private int tick;

    public static final List<TreeFellingHelper> helpers = ObjectArrayList.of();

    private TreeFellingHelper(ServerPlayer player, ItemStack tool, Deque<BlockPos> orderedBlocks) {
        this.player = player;
        this.tool = tool;
        this.orderedBlocks = orderedBlocks;
        tick = 0;
        helpers.add(this);
    }

    public static void fellTree(ItemStack stack, BlockState originState, BlockPos originPos, ServerPlayer miner) {
        Level level = miner.level();

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        Queue<BlockPos> checking = new ArrayDeque<>();
        Set<BlockPos> visited = new ObjectOpenHashSet<>();

        checking.add(originPos);

        while (!checking.isEmpty()) {
            BlockPos check = checking.remove();
            if (!check.equals(originPos)) {
                visited.add(check);
            }
            for (int y = 0; y <= 1; y++) {
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;

                        cursor.set(check.getX() + x, check.getY() + y, check.getZ() + z);
                        if (visited.contains(cursor)) continue;

                        // Check that the found block matches the original block state, which is wood.
                        if (!originState.is(level.getBlockState(cursor).getBlock())) continue;
                        if (checking.contains(cursor)) continue;

                        BlockPos immutablePos = cursor.immutable();
                        checking.add(immutablePos);
                    }
                }
            }
        }

        if (!visited.isEmpty()) {
            Deque<BlockPos> orderedBlocks = visited.stream()
                    .sorted(Comparator.comparingInt(pos -> pos.getY() - originPos.getY()))
                    .collect(Collectors.toCollection(LinkedList::new));
            new TreeFellingHelper(miner, stack, orderedBlocks);
        }
    }

    private boolean isValid() {
        if (this.player.isRemoved()) return false;
        if (this.orderedBlocks.isEmpty()) return false;
        return !this.tool.isEmpty() && getBehaviorsComponent(this.tool).hasBehavior(GTToolBehaviors.TREE_FELLING);
    }

    private void tick() {
        if (this.tick++ % ConfigHolder.INSTANCE.tools.treeFellingDelay == 0) {
            ToolHelper.destroyBlock(this.player, this.tool, this.orderedBlocks.removeLast(), true);
        }
    }

    @SubscribeEvent
    public static void onWorldTick(LevelTickEvent.Pre event) {
        if (helpers.isEmpty()) return;
        Level currentLevel = event.getLevel();
        if (currentLevel.isClientSide) return;

        var iterator = helpers.iterator();
        while (iterator.hasNext()) {
            TreeFellingHelper helper = iterator.next();
            if (currentLevel != helper.player.level()) {
                continue;
            }
            if (!helper.isValid()) {
                iterator.remove();
                continue;
            }
            helper.tick();
        }
    }
}
