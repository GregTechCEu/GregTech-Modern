package com.gregtechceu.gtceu.api.multiblock.util;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.multiblock.PatternPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternSlice;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import com.google.common.collect.Table;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Map;

public class BlockPatternStructureHelper {

    public static final Direction[] DIRECTIONS_IN_ORDER = { Direction.NORTH, Direction.SOUTH, Direction.WEST,
            Direction.EAST, Direction.UP, Direction.DOWN };

    private final @Nullable Table<PatternPredicate, BasePredicate, BlockInfo> blockPreferences;
    private final @Nullable Table<PatternPredicate, BasePredicate, IntIntPair> minMaxPreferences;
    private final Int2IntMap sliceRepeats;

    public BlockPatternStructureHelper(@Nullable Table<PatternPredicate, BasePredicate, BlockInfo> blockPreferences,
                                       @Nullable Table<PatternPredicate, BasePredicate, IntIntPair> minMaxPreferences,
                                       Int2IntMap sliceRepeats) {
        this.blockPreferences = blockPreferences;
        this.minMaxPreferences = minMaxPreferences;
        this.sliceRepeats = sliceRepeats;
    }

    public PatternPredicate getPredicateFromPos(BlockPattern pattern, BlockPos pos,
                                                Direction frontFacing, Direction upFacing, boolean isFlipped) {
        char[][][] flattenedBlockPattern = flattenBlockPattern(pattern);
        char[][][] adjustedBlockPattern = rotateAndFlipPattern(flattenedBlockPattern, pattern.getDirections(),
                frontFacing, upFacing, isFlipped);
        Vec3i dimensions = getDimensions(adjustedBlockPattern);
        if (pos.getX() < 0 || pos.getX() >= dimensions.getX() ||
                pos.getY() < 0 || pos.getY() >= dimensions.getY() ||
                pos.getZ() < 0 || pos.getZ() >= dimensions.getZ()) {
            return PatternPredicate.AIR;
        }
        char c = adjustedBlockPattern[pos.getX()][pos.getY()][pos.getZ()];
        return pattern.getPredicates().get(c);
    }

    public void populateWithUserBlockPreferences(Map<BlockPos, BlockInfo> resultStructure, BlockPattern pattern,
                                                 char[][][] flattenedBlockPattern,
                                                 @Nullable Long2ObjectMap<BlockInfo> userBlockPreferences,
                                                 Direction frontFacing, Direction upFacing, boolean isFlipped) {
        if (userBlockPreferences == null || userBlockPreferences.isEmpty()) {
            return;
        }

        Vec3i dimensions = getDimensions(flattenedBlockPattern);
        Direction sliceDir = pattern.getDirections()[0].getRelativeFacing(frontFacing, upFacing, isFlipped);

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
        // 4. Iterate slice by slice (a slice == one "layer"), then over the other two axes within the slice,
        // get the char at that position,
        // 4a. Go through every BasePredicate in order of priority, see if there's a minCount/minLayerCount that's
        //      not satisfied yet, then try those
        // 4b. If all basePredicates with a mincount/minLayerCount are satisfied, place the first predicate that works
        // 4c. If the BasePredicate is at its max (maxCount/maxLayerCount), remove it from the list to be considered
        // 4d. error if none are valid candidates(?)

        Vec3i dimensions = getDimensions(flattenedBlockPattern);
        Direction sliceDir = pattern.getDirections()[0].getRelativeFacing(frontFacing, upFacing, isFlipped);
        Direction stringDir = pattern.getDirections()[1].getRelativeFacing(frontFacing, upFacing, isFlipped);
        Direction charDir = pattern.getDirections()[2].getRelativeFacing(frontFacing, upFacing, isFlipped);
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

                    char c = flattenedBlockPattern[pos.getX()][pos.getY()][pos.getZ()];
                    PatternPredicate predicate = pattern.getPredicates().get(c);

                    if (predicate == PatternPredicate.AIR || predicate == PatternPredicate.ANY) {
                        continue;
                    }

                    // Attempts to first place the predicate if the minimum (layer) count isn't satisfied, then the
                    //  maximum (layer) count
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
        for (BasePredicate basePredicate : predicate.subPredicates) {
            int minCount = minMaxPreferences != null && minMaxPreferences.contains(predicate, basePredicate) ?
                    minMaxPreferences.get(predicate, basePredicate).leftInt() :
                    basePredicate.minCount;
            if (minCount == 0) continue;

            int totalAlreadyPopulated = countPopulatedGlobal(resultStructure, basePredicate);
            int layerAlreadyPopulated = countPopulatedInLayer(resultStructure, basePredicate, dir, offset);
            boolean globalMinUnmet = minCount > 0 && totalAlreadyPopulated < minCount;
            boolean layerMinUnmet = basePredicate.minSliceCount > 0 &&
                    layerAlreadyPopulated < basePredicate.minSliceCount;
            if (!globalMinUnmet && !layerMinUnmet) continue;

            BlockInfo toInsert = null;
            if (blockPreferences != null) {
                toInsert = blockPreferences.get(predicate, basePredicate);
            }
            if (toInsert == null) {
                toInsert = basePredicate.getCandidates().get(0);
            }
            // TODO: is this needed? doesn't this just do what we're already doing?
            if (isValidCandidate(resultStructure, predicate, pos, toInsert, dir)) {
                resultStructure.put(pos, toInsert);
                return true;
            }
        }
        return false;
    }

    private boolean tryMaxCount(Map<BlockPos, BlockInfo> resultStructure, PatternPredicate predicate,
                                BlockPos pos, Direction dir, int offset) {
        for (BasePredicate basePredicate : predicate.subPredicates) {
            int maxCount = minMaxPreferences != null && minMaxPreferences.contains(predicate, basePredicate) ?
                    minMaxPreferences.get(predicate, basePredicate).rightInt() :
                    basePredicate.maxCount;
            if (maxCount == 0) continue;

            int totalAlreadyPopulated = countPopulatedGlobal(resultStructure, basePredicate);
            int layerAlreadyPopulated = countPopulatedInLayer(resultStructure, basePredicate, dir, offset);
            if (maxCount != -1 && totalAlreadyPopulated >= maxCount) continue;
            if (basePredicate.maxSliceCount != -1 && layerAlreadyPopulated >= basePredicate.maxSliceCount) {
                continue;
            }

            BlockInfo toInsert = null;
            if (blockPreferences != null) {
                toInsert = blockPreferences.get(predicate, basePredicate);
            }
            if (toInsert == null) {
                toInsert = basePredicate.getCandidates().get(0);
            }
            // TODO: is this needed? doesn't this just do what we're already doing?
            if (isValidCandidate(resultStructure, predicate, pos, toInsert, dir)) {
                resultStructure.put(pos, toInsert);
                return true;
            }
        }
        return false;
    }

    private boolean isValidCandidate(Map<BlockPos, BlockInfo> resultStructure, PatternPredicate predicate,
                                     BlockPos pos, BlockInfo newInfo, Direction sliceDir) {
        // The slice (layer) this position belongs to.
        int sliceCoord = getCoordFromDir(pos, sliceDir);

        // newInfo is valid if there's a basePredicate it qualifies for whose maxCount (global) and maxSliceCount
        // (this slice) wouldn't be exceeded by placing it here.
        for (BasePredicate basePredicate : predicate.subPredicates) {
            if (!basePredicate.candidates.contains(newInfo)) continue;

            int maxCount = minMaxPreferences != null && minMaxPreferences.contains(predicate, basePredicate) ?
                    minMaxPreferences.get(predicate, basePredicate).rightInt() :
                    basePredicate.maxCount;
            if (maxCount == 0) continue;

            int totalAlreadyPopulated = countPopulatedGlobal(resultStructure, basePredicate);
            int layerAlreadyPopulated = countPopulatedInLayer(resultStructure, basePredicate, sliceDir, sliceCoord);
            if (maxCount != -1 && totalAlreadyPopulated >= maxCount) continue;

            if (basePredicate.maxSliceCount == -1 || layerAlreadyPopulated < basePredicate.maxSliceCount) {
                return true;
            }
        }
        return false;
    }

    public @UnmodifiableView char[][][] flattenBlockPattern(BlockPattern pattern) {
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

    public static char[][][] rotateAndFlipPattern(char[][][] localFlattenedPattern,
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
        //  (dimensions[axis] - 1) depending on the sign of the step.
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
                    result[worldX - min.getX()][worldY - min.getY()][worldZ - min.getZ()] = localFlattenedPattern[x][y][z];
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
            if (!(currentState.getBlock() instanceof MetaMachineBlock machine)) {
                continue;
            }
            if (!currentState.hasProperty(machine.getRotationState().property)) continue;

            if (machine == controllerBlock) {
                BlockState newState = currentState.setValue(machine.getRotationState().property, frontFacing);
                if (newState.hasProperty(GTBlockStateProperties.UPWARDS_FACING)) {
                    newState = newState.setValue(GTBlockStateProperties.UPWARDS_FACING, upFacing);
                }
                toUpdate.put(pos, newState);
                continue;
            }

            Direction validFacing = null;
            for (Direction dir : DIRECTIONS_IN_ORDER) {
                // make sure the machine can face this way
                if (!machine.getRotationState().test(dir)) continue;
                // and that there won't be a block in front of it
                if (!resultStructure.containsKey(pos.relative(dir))) {
                    validFacing = dir;
                    break;
                }
            }
            if (validFacing != null) {
                toUpdate.put(pos, currentState.setValue(machine.getRotationState().property, validFacing));
            }
        }
        for (var entry : toUpdate.entrySet()) {
            resultStructure.put(entry.getKey(), BlockInfo.fromBlockState(entry.getValue()));
        }
    }

    private static int countPopulatedGlobal(Map<BlockPos, BlockInfo> resultStructure, BasePredicate basePredicate) {
        return (int) resultStructure.values().stream()
                .filter(blockInfo -> basePredicate.getCandidates().contains(blockInfo))
                .count();
    }

    private static int countPopulatedInLayer(Map<BlockPos, BlockInfo> resultStructure, BasePredicate basePredicate,
                                             Direction dir, int offset) {
        return (int) resultStructure.entrySet().stream()
                .filter(e -> getCoordFromDir(e.getKey(), dir) == offset)
                .filter(e -> basePredicate.getCandidates().contains(e.getValue()))
                .count();
    }

    private static int getCoordFromDir(BlockPos pos, Direction dir) {
        return dir.getAxis().choose(pos.getX(), pos.getY(), pos.getZ());
    }

    public static BlockPos.MutableBlockPos setAxis(BlockPos.MutableBlockPos pos, Direction.Axis axis, int amount) {
        return switch (axis) {
            case X -> pos.setX(amount);
            case Y -> pos.setY(amount);
            case Z -> pos.setZ(amount);
        };
    }

    public static BlockPos.MutableBlockPos setFromDirection(BlockPos.MutableBlockPos pos, Direction direction, int amount) {
        return switch (direction) {
            case DOWN -> pos.setY(-amount);
            case UP -> pos.setY(amount);
            case NORTH -> pos.setZ(-amount);
            case SOUTH -> pos.setZ(amount);
            case WEST -> pos.setX(-amount);
            case EAST -> pos.setX(amount);
        };
    }
}
