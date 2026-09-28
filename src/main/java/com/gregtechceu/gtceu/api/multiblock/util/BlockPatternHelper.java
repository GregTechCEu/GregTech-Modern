package com.gregtechceu.gtceu.api.multiblock.util;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.MultiblockSchemaInfo;
import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.multiblock.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternSlice;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Map;

public class BlockPatternHelper extends AbstractStructureHelper {

    private final Int2IntMap sliceRepeats;
    private char[][][] flattenedBlockPattern = new char[0][][];

    private Object2IntOpenHashMap<MultiPredicate> predicateCount = new Object2IntOpenHashMap<>();
    private Object2IntOpenHashMap<BasePredicate> basePredicateCount = new Object2IntOpenHashMap<>();
    private Table<MultiPredicate, Integer, Integer> predicateSliceCount = HashBasedTable.create();
    private Table<BasePredicate, Integer, Integer> basePredicateSliceCount = HashBasedTable.create();

    protected BlockPatternHelper(Int2IntMap sliceRepeats) {
        this.sliceRepeats = sliceRepeats;
    }

    public MultiPredicate getPredicateFromPos(IBlockPattern pattern, BlockPos pos,
                                              Direction frontFacing, Direction upFacing, boolean isFlipped) {
        BlockPattern blockPattern = (BlockPattern) pattern;
        char[][][] flattenedBlockPattern = flattenBlockPattern(blockPattern);
        char[][][] adjustedBlockPattern = rotateAndFlipPattern(flattenedBlockPattern, blockPattern.getDirections(),
                frontFacing, upFacing, isFlipped);
        Vec3i dimensions = getDimensions(adjustedBlockPattern);
        if (pos.getX() < 0 || pos.getX() >= dimensions.getX() ||
                pos.getY() < 0 || pos.getY() >= dimensions.getY() ||
                pos.getZ() < 0 || pos.getZ() >= dimensions.getZ()) {
            return Predicates.air();
        }
        char c = adjustedBlockPattern[pos.getX()][pos.getY()][pos.getZ()];
        return blockPattern.getPredicates().get(c);
    }

    protected void setup(IBlockPattern pattern, Direction frontFacing, Direction upFacing, boolean isFlipped) {
        BlockPattern blockPattern = (BlockPattern) pattern;
        this.flattenedBlockPattern = rotateAndFlipPattern(flattenBlockPattern(blockPattern),
                blockPattern.getDirections(),
                frontFacing, upFacing, isFlipped);

        this.predicateCount.clear();
        this.predicateSliceCount.clear();
        this.basePredicateCount.clear();
        this.basePredicateSliceCount.clear();
    }

    protected void populateWithUserBlockPreferences(MultiblockSchemaInfo info, Map<BlockPos, BlockInfo> resultStructure,
                                                    IBlockPattern pattern,
                                                    Long2ObjectMap<BlockInfo> userBlockPreferences,
                                                    Direction frontFacing, Direction upFacing, boolean isFlipped) {
        BlockPattern blockPattern = (BlockPattern) pattern;

        Vec3i dimensions = getDimensions(this.flattenedBlockPattern);
        Direction sliceDir = blockPattern.getDirections()[0].getRelativeFacing(frontFacing, upFacing, isFlipped);

        for (var blockPreference : userBlockPreferences.long2ObjectEntrySet()) {
            BlockPos pos = BlockPos.of(blockPreference.getLongKey());
            BlockInfo blockInfo = blockPreference.getValue();
            if (pos.getX() >= dimensions.getX() ||
                    pos.getY() >= dimensions.getY() ||
                    pos.getZ() >= dimensions.getZ()) {
                throw new IllegalStateException(
                        "BlockPos preference " + pos + "is outside of bounds for pattern of size " +
                                dimensions.getX() + "," + dimensions.getY() + "," + dimensions.getZ());
            }
            char c = this.flattenedBlockPattern[pos.getX()][pos.getY()][pos.getZ()];
            MultiPredicate predicate = blockPattern.getPredicates().get(c);
            if (!isValidCandidate(info, resultStructure, predicate, pos, blockInfo, sliceDir)) {
                throw new IllegalStateException("Invalid preference " + blockInfo.getBlockState().getBlock().getName() +
                        " for position " + pos);
            }
            resultStructure.put(pos, blockInfo);
        }
    }

    protected void populateFromPattern(MultiblockSchemaInfo info, Map<BlockPos, BlockInfo> resultStructure,
                                       IBlockPattern pattern,
                                       Direction frontFacing, Direction upFacing, boolean isFlipped) {
        // spotless:off
        // 4. Iterate slice by slice (a slice == one "slice"), then over the other two axes within the slice,
        // get the char at that position,
        // 4a. Go through every BasePredicate in order of priority, see if there's a minCount/minSliceCount that's
        //      not satisfied yet, then try those
        // 4b. If all basePredicates with a mincount/minSliceCount are satisfied, place the first predicate that works
        // 4c. If the BasePredicate is at its max (maxCount/maxSliceCount), remove it from the list to be considered
        // 4d. error if none are valid candidates(?)
        // spotless:on

        BlockPattern blockPattern = (BlockPattern) pattern;
        Vec3i dimensions = getDimensions(this.flattenedBlockPattern);
        Direction sliceDir = blockPattern.getDirections()[0].getRelativeFacing(frontFacing, upFacing, isFlipped);
        Direction stringDir = blockPattern.getDirections()[1].getRelativeFacing(frontFacing, upFacing, isFlipped);
        Direction charDir = blockPattern.getDirections()[2].getRelativeFacing(frontFacing, upFacing, isFlipped);
        Direction.Axis sliceAxis = sliceDir.getAxis();
        Direction.Axis stringAxis = stringDir.getAxis();
        Direction.Axis charAxis = charDir.getAxis();

        for (int sliceCoord = 0; sliceCoord < dimensions.get(sliceAxis); sliceCoord++) {
            for (int stringCoord = 0; stringCoord < dimensions.get(stringAxis); stringCoord++) {
                for (int charCoord = 0; charCoord < dimensions.get(charAxis); charCoord++) {
                    // convert from local pattern relative directions to global xyz ordering
                    BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
                    setAxis(pos, sliceAxis, sliceCoord);
                    setAxis(pos, stringAxis, stringCoord);
                    setAxis(pos, charAxis, charCoord);

                    if (resultStructure.containsKey(pos)) continue;

                    char c = this.flattenedBlockPattern[pos.getX()][pos.getY()][pos.getZ()];
                    MultiPredicate predicate = blockPattern.getPredicates().get(c);

                    if (predicate.isAny()) {
                        continue;
                    }

                    // Attempts to first place the predicate if the minimum (slice) count isn't satisfied, then the
                    // maximum (slice) count
                    if (tryMinCount(info, resultStructure, predicate, pos, sliceDir, sliceCoord)) continue;
                    if (tryMaxCount(info, resultStructure, predicate, pos, sliceDir, sliceCoord)) continue;
                    // If we arrive here, there's nothing we can place that doesn't overflow a max count!
                    throw new IllegalStateException(
                            "Could not place a block without breaking maxCount requirements for character " + c);
                }
            }
        }
    }

    private boolean tryMinCount(MultiblockSchemaInfo info, Map<BlockPos, BlockInfo> resultStructure,
                                MultiPredicate predicate,
                                BlockPos pos, Direction dir, int offset) {
        // TODO rehandle user min count
        // Find first unsatisfied min predicate while also checking type specific logic
        BasePredicate baseNotSatisfied = null;
        if (predicate.isAnd() || predicate.isOr()) {
            for (BasePredicate basePredicate : predicate.predicates()) {
                int baseMinCount = basePredicate.getMinCount();
                if (baseMinCount == 0) continue;
                int baseMinSliceCount = basePredicate.getMinSliceCount();
                if (baseMinSliceCount == 0) continue;

                int baseTotalAlreadyPopulated = basePredicateCount.getInt(basePredicate);
                int baseSliceAlreadyPopulated = basePredicateSliceCount.row(basePredicate).getOrDefault(offset, 0);
                boolean baseGlobalMinMet = baseMinCount == -1 || baseTotalAlreadyPopulated >= baseMinCount;
                boolean baseSliceMinMet = baseMinSliceCount == -1 || baseSliceAlreadyPopulated >= baseMinSliceCount;

                if (!baseGlobalMinMet || !baseSliceMinMet) {
                    baseNotSatisfied = basePredicate;
                    break;
                }
            }
        } else if (predicate.isXor()) {
            // For XOR, only one can be true. If we find any condition already satisfied, return false
            int predTotalAlreadyPopulated = predicateCount.getInt(predicate);
            int predSliceAlreadyPopulated = predicateSliceCount.row(predicate).getOrDefault(offset, 0);

            int predMinCount = predicate.getMinCount();
            int predMinSliceCount = predicate.getMinSliceCount();
            boolean predGlobalMinMet = predTotalAlreadyPopulated >= predMinCount;
            boolean predSliceMinMet = predSliceAlreadyPopulated >= predMinSliceCount;
            if (predMinCount != -1 && predGlobalMinMet) return false;
            if (predMinSliceCount != -1 && predSliceMinMet) return false;

            for (BasePredicate basePredicate : predicate.predicates()) {
                // Same goes for the basePredicates, any satisfied basePredicate with mins returns false
                int baseMinCount = basePredicate.getMinCount();
                if (baseMinCount == 0) return false;

                int baseMinSliceCount = basePredicate.getMinSliceCount();
                if (baseMinSliceCount == 0) return false;

                int baseTotalAlreadyPopulated = basePredicateCount.getInt(basePredicate);
                int baseSliceAlreadyPopulated = basePredicateSliceCount.row(basePredicate).getOrDefault(offset, 0);
                boolean baseGlobalMinMet = baseTotalAlreadyPopulated >= baseMinCount;
                boolean baseSliceMinMet = baseSliceAlreadyPopulated >= baseMinSliceCount;

                // If there is a limit, and it's been met, return
                if (baseMinCount != -1 && baseGlobalMinMet) return false;
                if (baseMinSliceCount != -1 && baseSliceMinMet) return false;

                // If there is a limit, and it hasn't been met, we have a predicate that needs blocks
                // noinspection ConstantConditions
                if ((baseMinCount != -1 && !baseGlobalMinMet) || (baseMinSliceCount != -1 && !baseSliceMinMet)) {
                    baseNotSatisfied = basePredicate;
                    break;
                }
            }
        }

        if (baseNotSatisfied != null) {
            BasePredicate finalBaseNotSatisfied = baseNotSatisfied;
            BlockInfo toInsert = baseNotSatisfied.getFirstCandidate().orElseGet(() -> {
                GTCEu.LOGGER.warn("Predicate\n\t{}\nhas no candidates to chose from!", finalBaseNotSatisfied);
                return BlockInfo.EMPTY;
            });
            resultStructure.put(pos, toInsert);
            if (this.controllerBlock == null && predicate.isController()) {
                this.controllerBlock = toInsert.getBlockState().getBlock();
            }
            basePredicateCount.merge(baseNotSatisfied, 1, Integer::sum);
            basePredicateSliceCount.column(offset).merge(baseNotSatisfied, 1, Integer::sum);
            return true;
        }

        // check if each child predicate min is satisfied
        for (MultiPredicate child : predicate.children()) {
            if (tryMinCount(info, resultStructure, child, pos, dir, offset)) return true;
        }

        // check if main predicate min is satisfied
        int minCount = predicate.getMinCount();
        if (minCount == 0) return false;
        int sliceMinCount = predicate.getMinSliceCount();
        if (sliceMinCount == 0) return false;

        int totalAlreadyPopulated = predicateCount.getInt(predicate);
        int sliceAlreadyPopulated = predicateSliceCount.row(predicate).getOrDefault(offset, 0);

        boolean globalMinMet = minCount == -1 || totalAlreadyPopulated >= minCount;
        boolean sliceMinMet = sliceMinCount == -1 || sliceAlreadyPopulated >= sliceMinCount;
        if (globalMinMet && sliceMinMet) {
            return false;
        }

        BlockInfo toInsert = info.getBlockPreferences().get(predicate);
        if (toInsert == null) {
            // TODO filtering?
            toInsert = predicate.getCandidates().get(0).get(0);
        }
        resultStructure.put(pos, toInsert);
        if (this.controllerBlock == null && predicate.isController()) {
            this.controllerBlock = toInsert.getBlockState().getBlock();
        }
        predicateCount.merge(predicate, 1, Integer::sum);
        predicateSliceCount.column(offset).merge(predicate, 1, Integer::sum);
        for (var child : predicate.children()) {
            predicateCount.merge(child, 1, Integer::sum);
            predicateSliceCount.column(offset).merge(child, 1, Integer::sum);
        }
        return true;
    }

    private boolean tryMaxCount(MultiblockSchemaInfo info, Map<BlockPos, BlockInfo> resultStructure,
                                MultiPredicate predicate,
                                BlockPos pos, Direction dir, int offset) {
        // check if main predicate max is satisfied
        int maxCount = predicate.getMaxCount();
        if (maxCount == 0) return false;
        int maxSliceCount = predicate.getMaxSliceCount();
        if (maxSliceCount == 0) return false;

        int totalAlreadyPopulated = predicateCount.getInt(predicate);
        int sliceAlreadyPopulated = predicateSliceCount.row(predicate).getOrDefault(offset, 0);

        boolean globalMaxMet = maxCount != -1 && totalAlreadyPopulated >= maxCount;
        boolean sliceMaxMet = maxSliceCount != -1 && sliceAlreadyPopulated >= maxSliceCount;

        if (globalMaxMet || sliceMaxMet) {
            return false;
        }

        // check if each base predicate max is satisfied
        BasePredicate baseNotSatisfied = null;
        if (predicate.isAnd() || predicate.isOr()) {
            for (BasePredicate basePredicate : predicate.predicates()) {
                int baseMaxCount = basePredicate.getMaxCount();
                if (baseMaxCount == 0) continue;
                int baseMaxSliceCount = basePredicate.getMaxSliceCount();
                if (baseMaxSliceCount == 0) continue;

                int baseTotalAlreadyPopulated = basePredicateCount.getInt(basePredicate);
                int baseSliceAlreadyPopulated = basePredicateSliceCount.row(basePredicate).getOrDefault(offset, 0);
                boolean baseGlobalMaxMet = baseMaxCount != -1 && baseTotalAlreadyPopulated >= baseMaxCount;
                boolean baseSliceMaxMet = baseMaxSliceCount != -1 && baseSliceAlreadyPopulated >= baseMaxSliceCount;

                if (!baseGlobalMaxMet && !baseSliceMaxMet) {
                    baseNotSatisfied = basePredicate;
                    break;
                }
            }
        } else if (predicate.isXor()) {
            for (BasePredicate basePredicate : predicate.predicates()) {
                // Any satisfied basePredicate with mins and maxs satisfied returns false
                int baseMaxCount = basePredicate.getMaxCount();
                if (baseMaxCount == 0) return false;
                int baseMaxSliceCount = basePredicate.getMaxSliceCount();
                if (baseMaxSliceCount == 0) return false;

                int baseTotalAlreadyPopulated = basePredicateCount.getInt(basePredicate);
                int baseSliceAlreadyPopulated = basePredicateSliceCount.row(basePredicate).getOrDefault(offset, 0);
                boolean baseGlobalMaxMet = baseTotalAlreadyPopulated >= baseMaxCount;
                boolean baseSliceMaxMet = baseSliceAlreadyPopulated >= baseMaxSliceCount;

                // If there is a limit, and it's been met, return
                if (baseMaxCount != -1 && baseGlobalMaxMet) return false;
                if (baseMaxSliceCount != -1 && baseSliceMaxMet) return false;

                // If there is no limit, or there is one that hasn't been met, we have a predicate that allows blocks
                // noinspection ConstantConditions
                if ((baseMaxCount == -1 || !baseGlobalMaxMet) && (baseMaxSliceCount == -1 || !baseSliceMaxMet)) {
                    baseNotSatisfied = basePredicate;
                    break;
                }
            }
        }

        if (baseNotSatisfied != null) {
            basePredicateCount.merge(baseNotSatisfied, 1, Integer::sum);
            basePredicateSliceCount.column(offset).merge(baseNotSatisfied, 1, Integer::sum);
            BasePredicate finalBaseNotSatisfied = baseNotSatisfied;
            BlockInfo toInsert = baseNotSatisfied.getFirstCandidate().orElseGet(() -> {
                GTCEu.LOGGER.warn("Predicate\n\t{}\nhas no candidates to chose from!", finalBaseNotSatisfied);
                return BlockInfo.EMPTY;
            });
            resultStructure.put(pos, toInsert);
            if (this.controllerBlock == null && predicate.isController()) {
                this.controllerBlock = toInsert.getBlockState().getBlock();
            }
            return true;
        }

        // check if each child predicate min is satisfied
        for (MultiPredicate child : predicate.children()) {
            if (tryMaxCount(info, resultStructure, child, pos, dir, offset)) return true;
        }

        BlockInfo toInsert = info.getBlockPreferences().get(predicate);
        if (toInsert == null) {
            // TODO filtering?
            toInsert = predicate.getCandidates().get(0).get(0);
        }
        resultStructure.put(pos, toInsert);
        if (this.controllerBlock == null && predicate.isController()) {
            this.controllerBlock = toInsert.getBlockState().getBlock();
        }
        predicateCount.merge(predicate, 1, Integer::sum);
        predicateSliceCount.column(offset).merge(predicate, 1, Integer::sum);
        for (var child : predicate.children()) {
            predicateCount.merge(child, 1, Integer::sum);
            predicateSliceCount.column(offset).merge(child, 1, Integer::sum);
        }
        return true;
    }

    private boolean isValidCandidate(MultiblockSchemaInfo info, Map<BlockPos, BlockInfo> resultStructure,
                                     MultiPredicate predicate,
                                     BlockPos pos, BlockInfo newInfo, Direction sliceDir) {
        // force true because idk what to do with this
        if (newInfo == BlockInfo.EMPTY) return true;
        // The slice this position belongs to.
        int sliceCoord = getCoordFromDir(pos, sliceDir);

        // newInfo is valid if there's a basePredicate it qualifies for whose maxCount (global) and maxSliceCount
        // (this slice) wouldn't be exceeded by placing it here.
        for (BasePredicate basePredicate : predicate.predicates()) {
            /*
             * PROBLEM:
             * certain predicates (like air/any) do not have any candidates
             * so they fail with BlockInfo.EMPTY
             * there's also no way to "test" the block info since you need a PredicateContext
             */
            if (!basePredicate.getCandidates().contains(newInfo)) continue;

            int maxCount = info.getMaxCount(predicate, basePredicate);
            if (maxCount == 0) continue;

            int totalAlreadyPopulated = countPopulatedGlobal(resultStructure, basePredicate);
            int sliceAlreadyPopulated = countPopulatedInSlice(resultStructure, basePredicate, sliceDir, sliceCoord);
            if (maxCount != -1 && totalAlreadyPopulated >= maxCount) continue;

            if (basePredicate.getMaxSliceCount() == -1 || sliceAlreadyPopulated < basePredicate.getMaxSliceCount()) {
                return true;
            }
        }
        for (MultiPredicate child : predicate.children()) {
            if (isValidCandidate(info, resultStructure, child, pos, newInfo, sliceDir)) {
                return true;
            }
        }
        return false;
    }

    private @UnmodifiableView char[][][] flattenBlockPattern(BlockPattern pattern) {
        int totalSlices = sliceRepeats.values().intStream().sum();
        int[] dimensions = pattern.getDimensions();
        char[][][] flattenedPattern = new char[totalSlices][dimensions[1]][dimensions[2]];
        PatternSlice[] slices = pattern.getSlices();
        int totalSlicesIndex = 0;

        for (int sliceIndex = 0; sliceIndex < slices.length; sliceIndex++) {
            PatternSlice slice = slices[sliceIndex];
            int repeats = sliceRepeats.getOrDefault(sliceIndex, 1);
            for (int i = 0; i < repeats; i++) {
                flattenedPattern[totalSlicesIndex] = slice.getPattern();
                totalSlicesIndex++;
            }
        }
        assert totalSlicesIndex == totalSlices;

        return flattenedPattern;
    }

    private static Vec3i getDimensions(char[][][] charPattern) {
        int d0 = charPattern.length;
        int d1 = d0 > 0 ? charPattern[0].length : 0;
        int d2 = d1 > 0 ? charPattern[0][0].length : 0;
        return new Vec3i(d0, d1, d2);
    }

    private static char[][][] rotateAndFlipPattern(char[][][] localFlattenedPattern,
                                                   RelativeDirection[] patternDirections,
                                                   Direction frontFacing, Direction upFacing, boolean isFlipped) {
        Direction absoluteX = patternDirections[0].getRelativeFacing(frontFacing, upFacing, isFlipped);
        Direction absoluteY = patternDirections[1].getRelativeFacing(frontFacing, upFacing, isFlipped);
        Direction absoluteZ = patternDirections[2].getRelativeFacing(frontFacing, upFacing, isFlipped);

        Vec3i dimensions = getDimensions(localFlattenedPattern);
        if (dimensions.getX() == 0 || dimensions.getY() == 0 || dimensions.getZ() == 0) return new char[0][0][0];

        int[][] steps = {
                { absoluteX.getStepX(), absoluteX.getStepY(), absoluteX.getStepZ() },
                { absoluteY.getStepX(), absoluteY.getStepY(), absoluteY.getStepZ() },
                { absoluteZ.getStepX(), absoluteZ.getStepY(), absoluteZ.getStepZ() },
        };

        // World-space bounding box. Each axis contributes monotonically, so the extremes are reached at index 0 or at
        // (dimensions[axis] - 1) depending on the sign of the step.
        BlockPos.MutableBlockPos min = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos max = new BlockPos.MutableBlockPos();
        for (Direction.Axis axis : Direction.Axis.VALUES) {
            for (Direction.Axis world : Direction.Axis.VALUES) {
                int contribution = steps[axis.ordinal()][world.ordinal()] * (dimensions.get(axis) - 1);
                Direction worldDir = Direction.fromAxisAndDirection(world, Direction.AxisDirection.POSITIVE);
                min.move(worldDir, Math.min(0, contribution));
                max.move(worldDir, Math.max(0, contribution));
            }
        }
        // this *should* be the same as dimensions. I think?
        Vec3i size = max.move(-min.getX() + 1, -min.getY() + 1, -min.getZ() + 1);
        char[][][] result = new char[size.getX()][size.getY()][size.getZ()];

        for (int x = 0; x < dimensions.getX(); x++) {
            for (int y = 0; y < dimensions.getY(); y++) {
                for (int z = 0; z < dimensions.getZ(); z++) {
                    int worldX = absoluteX.getStepX() * x + absoluteY.getStepX() * y + absoluteZ.getStepX() * z;
                    int worldY = absoluteX.getStepY() * x + absoluteY.getStepY() * y + absoluteZ.getStepY() * z;
                    int worldZ = absoluteX.getStepZ() * x + absoluteY.getStepZ() * y + absoluteZ.getStepZ() * z;
                    result[worldX - min.getX()][worldY - min.getY()][worldZ -
                            min.getZ()] = localFlattenedPattern[x][y][z];
                }
            }
        }

        return result;
    }

    private static BlockPos.MutableBlockPos setAxis(BlockPos.MutableBlockPos pos, Direction.Axis axis, int amount) {
        return switch (axis) {
            case X -> pos.setX(amount);
            case Y -> pos.setY(amount);
            case Z -> pos.setZ(amount);
        };
    }
}
