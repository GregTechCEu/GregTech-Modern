package com.gregtechceu.gtceu.api.multiblock.util;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.multiblock.PatternPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternSlice;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import com.google.common.collect.Table;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Map;

public class BlockPatternStructureUtil {

    public static final Direction[] DIRECTIONS_IN_ORDER = { Direction.NORTH, Direction.SOUTH, Direction.WEST,
            Direction.EAST, Direction.UP, Direction.DOWN };

    private Table<PatternPredicate, BasePredicate, BlockInfo> blockPreferences;
    private Table<PatternPredicate, BasePredicate, Pair<Integer, Integer>> minMaxPreferences;
    private Map<Integer, Integer> sliceRepeats;

    public PatternPredicate getPredicateFromPos(BlockPattern pattern, Direction frontFacing, Direction upFacing,
                                                boolean isFlipped, BlockPos pos) {
        char[][][] flattenedBlockPattern = flattenBlockPattern(pattern);
        char[][][] adjustedBlockPattern = rotateAndFlipCharPattern(flattenedBlockPattern, pattern.getDirections(),
                frontFacing, upFacing, isFlipped);
        var dimensions = getDimensions(adjustedBlockPattern);
        if (pos.getX() < 0 || pos.getX() >= dimensions[0] ||
                pos.getY() < 0 || pos.getY() >= dimensions[1] ||
                pos.getZ() < 0 || pos.getZ() >= dimensions[2]) {
            return PatternPredicate.AIR;
        }
        char c = adjustedBlockPattern[pos.getX()][pos.getY()][pos.getZ()];
        return pattern.getPredicates().get(c);
    }

    public void populatePreferenceTables(Table<PatternPredicate, BasePredicate, BlockInfo> blockPreferences,
                                         Table<PatternPredicate, BasePredicate, Pair<Integer, Integer>> minMaxPreferences,
                                         Map<Integer, Integer> sliceRepeats) {
        this.blockPreferences = blockPreferences;
        this.minMaxPreferences = minMaxPreferences;
        this.sliceRepeats = sliceRepeats;
    }

    public void populateWithUserBlockPreferences(Map<BlockPos, BlockInfo> resultStructure, BlockPattern pattern,
                                                 char[][][] flattenedBlockPattern,
                                                 Map<Long, BlockInfo> userBlockPreferences,
                                                 Direction frontFacing, Direction upFacing, boolean isFlipped) {
        var dimensions = getDimensions(flattenedBlockPattern);
        Direction sliceDir = pattern.getDirections()[0].getRelativeFacing(frontFacing, upFacing, isFlipped);
        for (Map.Entry<Long, BlockInfo> blockPreference : userBlockPreferences.entrySet()) {
            BlockPos pos = BlockPos.of(blockPreference.getKey());
            BlockInfo blockInfo = blockPreference.getValue();
            if (pos.getX() >= dimensions[0] ||
                    pos.getY() >= dimensions[1] ||
                    pos.getZ() >= dimensions[2]) {
                throw new IllegalStateException(
                        "BlockPos preference " + pos + "is outside of bounds for pattern of size " +
                                dimensions[0] + "," + dimensions[1] + "," + dimensions[2]);
            }
            char c = flattenedBlockPattern[pos.getX()][pos.getY()][pos.getZ()];
            PatternPredicate predicate = pattern.getPredicates().get(c);
            if (!isValidCandidate(resultStructure, predicate, pos, blockInfo, sliceDir)) {
                throw new IllegalStateException("Invalid preference " + blockInfo.getBlockState().getBlock().getName() +
                        " for position " + pos);
            }
            resultStructure.put(pos, blockInfo);
        }
    }

    public void populateFromPattern(Map<BlockPos, BlockInfo> resultStructure, BlockPattern pattern,
                                    char[][][] flattenedBlockPattern, Direction frontFacing, Direction upFacing,
                                    boolean isFlipped) {
        /// 4. Iterate slice by slice (a slice == one "layer"), then over the other two axes within the slice,
        /// get the char at that position,
        /// 4a. Go through every BasePredicate in order of priority, see if there's a minCount/minLayerCount that's
        /// not satisfied yet, then try those
        /// 4b. If all basePredicates with a mincount/minLayerCount are satisfied, place the first predicate that works
        /// 4c. If the BasePredicate is at its max (maxCount/maxLayerCount), remove it from the list to be considered
        /// 4d. error if none are valid candidates(?)
        ///

        var dimensions = getDimensions(flattenedBlockPattern);
        Direction sliceDir = pattern.getDirections()[0].getRelativeFacing(frontFacing, upFacing, isFlipped);
        Direction stringDir = pattern.getDirections()[1].getRelativeFacing(frontFacing, upFacing, isFlipped);
        Direction charDir = pattern.getDirections()[2].getRelativeFacing(frontFacing, upFacing, isFlipped);
        int sliceAxis = sliceDir.getAxis().ordinal();
        int stringAxis = stringDir.getAxis().ordinal();
        int charAxis = charDir.getAxis().ordinal();

        for (int sliceCoord = 0; sliceCoord < dimensions[sliceAxis]; sliceCoord++) {
            for (int strCoord = 0; strCoord < dimensions[stringAxis]; strCoord++) {
                for (int charCoord = 0; charCoord < dimensions[charAxis]; charCoord++) {
                    int[] coords = new int[3];
                    // convert from local pattern relative directions to global xyz ordering
                    coords[sliceAxis] = sliceCoord;
                    coords[stringAxis] = strCoord;
                    coords[charAxis] = charCoord;
                    var pos = new BlockPos(coords[0], coords[1], coords[2]);

                    if (resultStructure.containsKey(pos)) continue;

                    char c = flattenedBlockPattern[coords[0]][coords[1]][coords[2]];
                    PatternPredicate predicate = pattern.getPredicates().get(c);

                    if (predicate == PatternPredicate.AIR || predicate == PatternPredicate.ANY) {
                        continue;
                    }

                    // Attempts to first place the predicate if the min(layer)count isn't satisfied, then the
                    // max(layer)count
                    if (tryMinCount(resultStructure, predicate, pos, sliceDir, sliceCoord)) continue;
                    if (tryMaxCount(resultStructure, predicate, pos, sliceDir, sliceCoord)) continue;
                    // If we arrive here, there's nothing we can place that doesn't overflow a max count!
                    throw new IllegalStateException(
                            "Could not place a block without breaking maxCount requirements for character " + c);
                }
            }
        }
    }

    private boolean tryMinCount(Map<BlockPos, BlockInfo> resultStructure, PatternPredicate predicate,
                                BlockPos pos, Direction dir, int offset) {
        for (BasePredicate basePredicate : predicate.predicateList) {
            int minCount = minMaxPreferences.contains(predicate, basePredicate) ?
                    minMaxPreferences.get(predicate, basePredicate).left() :
                    basePredicate.minCount;
            if (minCount == 0) continue;
            int totalAlreadyPopulated = countGlobal(resultStructure, basePredicate);
            int layerAlreadyPopulated = countInLayer(resultStructure, basePredicate, dir, offset);
            boolean globalMinUnmet = minCount > 0 && totalAlreadyPopulated < minCount;
            boolean layerMinUnmet = basePredicate.minSliceCount > 0 &&
                    layerAlreadyPopulated < basePredicate.minSliceCount;
            if (!globalMinUnmet && !layerMinUnmet) continue;
            var toInsert = blockPreferences.contains(predicate, basePredicate) ?
                    blockPreferences.get(predicate, basePredicate) :
                    basePredicate.getCandidates().get(0);
            // TODO: is this needed? doesn't this just do what we're already doing?
            if (!isValidCandidate(resultStructure, predicate, pos, toInsert, dir)) continue;
            resultStructure.put(pos, toInsert);
            return true;
        }
        return false;
    }

    private boolean tryMaxCount(Map<BlockPos, BlockInfo> resultStructure, PatternPredicate predicate,
                                BlockPos pos, Direction dir, int offset) {
        for (BasePredicate basePredicate : predicate.predicateList) {
            int maxCount = minMaxPreferences.contains(predicate, basePredicate) ?
                    minMaxPreferences.get(predicate, basePredicate).right() :
                    basePredicate.maxCount;
            if (maxCount == 0) continue;
            int totalAlreadyPopulated = countGlobal(resultStructure, basePredicate);
            int layerAlreadyPopulated = countInLayer(resultStructure, basePredicate, dir, offset);
            if (maxCount != -1 && totalAlreadyPopulated >= maxCount) continue;
            if (basePredicate.maxSliceCount != -1 && layerAlreadyPopulated >= basePredicate.maxSliceCount)
                continue;
            var toInsert = blockPreferences.contains(predicate, basePredicate) ?
                    blockPreferences.get(predicate, basePredicate) :
                    basePredicate.getCandidates().get(0);
            // TODO: is this needed? doesn't this just do what we're already doing?

            if (!isValidCandidate(resultStructure, predicate, pos, toInsert, dir)) continue;
            resultStructure.put(pos, toInsert);
            return true;
        }
        return false;
    }

    private boolean isValidCandidate(Map<BlockPos, BlockInfo> resultStructure, PatternPredicate predicate,
                                     BlockPos pos, BlockInfo newInfo, Direction sliceDir) {
        // The slice (layer) this position belongs to.
        int sliceCoord = getCoordFromDir(pos, sliceDir);

        // newInfo is valid if there's a basePredicate it qualifies for whose maxCount (global) and maxSliceCount
        // (this slice) wouldn't be exceeded by placing it here.
        for (BasePredicate basePredicate : predicate.predicateList) {
            if (!basePredicate.candidates.contains(newInfo)) continue;
            int maxCount = minMaxPreferences.contains(predicate, basePredicate) ?
                    minMaxPreferences.get(predicate, basePredicate).right() :
                    basePredicate.maxCount;
            if (maxCount == 0) continue;
            int totalAlreadyPopulated = countGlobal(resultStructure, basePredicate);
            int layerAlreadyPopulated = countInLayer(resultStructure, basePredicate, sliceDir, sliceCoord);
            if (maxCount != -1 && totalAlreadyPopulated >= maxCount) continue;
            if (basePredicate.maxSliceCount != -1 && layerAlreadyPopulated >= basePredicate.maxSliceCount) continue;
            return true;
        }
        return false;
    }

    public @UnmodifiableView char[][][] flattenBlockPattern(BlockPattern pattern) {
        int totalSlices = sliceRepeats.values().stream().reduce(0, Integer::sum);
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
        assert (totalSlicesIndex == totalSlices);
        return flattenedPattern;
    }

    private static int[] getDimensions(char[][][] charPattern) {
        int d0 = charPattern.length;
        int d1 = d0 > 0 ? charPattern[0].length : 0;
        int d2 = d1 > 0 ? charPattern[0][0].length : 0;
        return new int[] { d0, d1, d2 };
    }

    public static char[][][] rotateAndFlipCharPattern(char[][][] localFlattenedPattern,
                                                      RelativeDirection[] patternDirections,
                                                      Direction frontFacing, Direction upFacing, boolean isFlipped) {
        Direction absoluteDir0 = patternDirections[0].getRelativeFacing(frontFacing, upFacing, isFlipped);
        Direction absoluteDir1 = patternDirections[1].getRelativeFacing(frontFacing, upFacing, isFlipped);
        Direction absoluteDir2 = patternDirections[2].getRelativeFacing(frontFacing, upFacing, isFlipped);

        var dimensions = getDimensions(localFlattenedPattern);
        if (dimensions[0] == 0 || dimensions[1] == 0 || dimensions[2] == 0) return new char[0][0][0];

        // Per-axis step vectors of each absolute direction.
        int[][] steps = {
                { absoluteDir0.getStepX(), absoluteDir0.getStepY(), absoluteDir0.getStepZ() },
                { absoluteDir1.getStepX(), absoluteDir1.getStepY(), absoluteDir1.getStepZ() },
                { absoluteDir2.getStepX(), absoluteDir2.getStepY(), absoluteDir2.getStepZ() },
        };
        // Max local index reached along each local axis.
        int[] extents = { dimensions[0] - 1, dimensions[1] - 1, dimensions[2] - 1 };

        // World-space bounding box. Each axis contributes monotonically, so the extremes are
        // reached at index 0 or at extents[axis] depending on the sign of the step.
        int[] min = new int[3];
        int[] max = new int[3];
        for (int axis = 0; axis < 3; axis++) {
            for (int world = 0; world < 3; world++) {
                int contribution = steps[axis][world] * extents[axis];
                min[world] += Math.min(0, contribution);
                max[world] += Math.max(0, contribution);
            }
        }

        int sizeX = max[0] - min[0] + 1;
        int sizeY = max[1] - min[1] + 1;
        int sizeZ = max[2] - min[2] + 1;
        char[][][] result = new char[sizeX][sizeY][sizeZ];

        for (int s = 0; s < dimensions[0]; s++) {
            for (int t = 0; t < dimensions[1]; t++) {
                for (int c = 0; c < dimensions[2]; c++) {
                    int worldX = steps[0][0] * s + steps[1][0] * t + steps[2][0] * c;
                    int worldY = steps[0][1] * s + steps[1][1] * t + steps[2][1] * c;
                    int worldZ = steps[0][2] * s + steps[1][2] * t + steps[2][2] * c;
                    result[worldX - min[0]][worldY - min[1]][worldZ - min[2]] = localFlattenedPattern[s][t][c];
                }
            }
        }

        return result;
    }

    public static void fixRotationsAndFacing(Map<BlockPos, BlockInfo> resultStructure, Direction frontFacing,
                                             Direction upFacing, Block controllerBlock) {
        Map<BlockPos, BlockState> toUpdate = new Object2ObjectOpenHashMap<>();
        for (var entry : resultStructure.entrySet()) {
            BlockPos pos = entry.getKey();
            BlockState currentState = entry.getValue().getBlockState();
            Direction valid = null;
            if (currentState.getBlock() instanceof MetaMachineBlock machineBlock) {
                if (!currentState.hasProperty(machineBlock.getRotationState().property)) continue;
                if (machineBlock.equals(controllerBlock)) {
                    var newState = currentState.setValue(machineBlock.getRotationState().property, frontFacing);
                    if (newState.hasProperty(GTBlockStateProperties.UPWARDS_FACING))
                        newState = newState.setValue(GTBlockStateProperties.UPWARDS_FACING, upFacing);
                    toUpdate.put(pos, newState);
                    continue;
                }
                for (var dir : DIRECTIONS_IN_ORDER) {
                    if (!machineBlock.getRotationState().test(valid)) continue;
                    if (!resultStructure.containsKey(pos.relative(dir))) {
                        valid = dir;
                        break;
                    }
                }
                if (valid != null) {
                    toUpdate.put(pos, currentState.setValue(machineBlock.getRotationState().property, valid));
                }
            }
        }
        for (var entry : toUpdate.entrySet()) {
            resultStructure.put(entry.getKey(), BlockInfo.fromBlockState(entry.getValue()));
        }
    };

    private static int countGlobal(Map<BlockPos, BlockInfo> resultStructure, BasePredicate basePredicate) {
        return (int) resultStructure.values()
                .stream()
                .filter(blockInfo -> basePredicate.getCandidates().contains(blockInfo))
                .count();
    }

    private static int countInLayer(Map<BlockPos, BlockInfo> resultStructure, BasePredicate basePredicate,
                                    Direction dir, int offset) {
        return (int) resultStructure.entrySet().stream()
                .filter(e -> getCoordFromDir(e.getKey(), dir) == offset)
                .filter(e -> basePredicate.getCandidates().contains(e.getValue()))
                .count();
    }

    private static int getCoordFromDir(BlockPos pos, Direction dir) {
        return dir.getAxis().choose(pos.getX(), pos.getY(), pos.getZ());
    }
}
