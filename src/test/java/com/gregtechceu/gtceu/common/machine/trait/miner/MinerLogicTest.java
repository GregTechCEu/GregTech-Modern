package com.gregtechceu.gtceu.common.machine.trait.miner;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class MinerLogicTest {

    private static final Field BLOCKS_TO_MINE = getField("blocksToMine");
    private static final Field BLOCKS_TO_MINE_ORIGINAL_COUNT = getField("blocksToMineOriginalCount");

    @GameTest(template = "empty", batch = "MinerLogic")
    public static void pendingQueueSurvivesReload(GameTestHelper helper) {
        var queuedBlocks = List.of(new BlockPos(1, 2, 3), new BlockPos(4, 5, 6));
        MinerLogic restored = roundTrip(logicWithQueue(queuedBlocks, queuedBlocks.size()));

        helper.assertTrue(getBlocksToMine(restored).equals(queuedBlocks),
                "pending ore queue changed during save and reload");
        helper.assertFalse(restored.isDone(), "miner became done while restored ores remain queued");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "MinerLogic")
    public static void emptyQueueKeepsScanCursor(GameTestHelper helper) {
        MinerLogic original = logicWithQueue(List.of(), 0);
        original.x = 11;
        original.y = 22;
        original.z = 33;

        MinerLogic restored = roundTrip(original);

        helper.assertTrue(getBlocksToMine(restored).isEmpty(), "empty pending queue was not restored as empty");
        helper.assertTrue(restored.getX() == 11 && restored.getY() == 22 && restored.getZ() == 33,
                "scan cursor changed during save and reload");
        helper.assertFalse(restored.isDone(), "incomplete miner became done after restoring an empty queue");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "MinerLogic")
    public static void validOreRemainsAfterStaleQueueEntry(GameTestHelper helper) {
        BlockPos staleBlock = new BlockPos(1, 2, 3);
        BlockPos validOre = new BlockPos(4, 5, 6);
        MinerLogic restored = roundTrip(logicWithQueue(List.of(staleBlock, validOre), 2));

        LinkedList<BlockPos> restoredQueue = getBlocksToMine(restored);
        restoredQueue.removeFirst();

        helper.assertTrue(restoredQueue.equals(List.of(validOre)),
                "valid queued ore was lost when the stale leading position was skipped");
        helper.assertFalse(restored.isDone(), "miner became done while valid queued ore remains");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "MinerLogic")
    public static void restoredQueueKeepsConsistentProgress(GameTestHelper helper) {
        MinerLogic restored = roundTrip(logicWithQueue(List.of(
                new BlockPos(1, 2, 3), new BlockPos(4, 5, 6)), 2));

        assertProgress(helper, restored, 0, 2);
        getBlocksToMine(restored).removeFirst();
        assertProgress(helper, restored, 1, 2);
        getBlocksToMine(restored).removeFirst();
        assertProgress(helper, restored, 2, 2);
        helper.succeed();
    }

    private static MinerLogic logicWithQueue(List<BlockPos> blocks, int originalCount) {
        MinerLogic logic = new MinerLogic(0, 1, 1);
        getBlocksToMine(logic).addAll(blocks);
        setOriginalCount(logic, originalCount);
        return logic;
    }

    private static MinerLogic roundTrip(MinerLogic original) {
        CompoundTag saved = original.getSyncDataHolder().serializeNBT(false);
        MinerLogic restored = new MinerLogic(0, 1, 1);
        restored.getSyncDataHolder().deserializeNBT(saved, false);
        return restored;
    }

    @SuppressWarnings("unchecked")
    private static LinkedList<BlockPos> getBlocksToMine(MinerLogic logic) {
        try {
            return (LinkedList<BlockPos>) BLOCKS_TO_MINE.get(logic);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    private static void setOriginalCount(MinerLogic logic, int count) {
        try {
            BLOCKS_TO_MINE_ORIGINAL_COUNT.setInt(logic, count);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    private static Field getField(String name) {
        try {
            Field field = MinerLogic.class.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private static void assertProgress(GameTestHelper helper, MinerLogic logic, int expectedMined, int expectedTotal) {
        var progress = logic.getCustomProgressLine();
        helper.assertTrue(progress != null, "miner progress line was missing");
        helper.assertTrue(progress.getContents() instanceof TranslatableContents,
                "miner progress line was not translatable");
        Object[] args = ((TranslatableContents) progress.getContents()).getArgs();
        helper.assertTrue(args.length == 2 && args[0].equals(expectedMined) && args[1].equals(expectedTotal),
                "miner progress was inconsistent: expected %s/%s, got %s/%s"
                        .formatted(expectedMined, expectedTotal, args[0], args[1]));
    }
}
