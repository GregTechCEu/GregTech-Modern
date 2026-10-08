package com.gregtechceu.gtceu.api.multiblock.util;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.MultiblockSchemaInfo;
import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.ExpandablePattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;

import java.util.Map;
import java.util.Objects;

public class ExpandablePatternHelper extends AbstractStructureHelper {

    private final IntList userRepeats;

    private final Object2IntOpenHashMap<MultiPredicate> predicateCount = new Object2IntOpenHashMap<>();
    private final Object2IntOpenHashMap<BasePredicate> basePredicateCount = new Object2IntOpenHashMap<>();

    protected ExpandablePatternHelper(IntList userRepeats) {
        this.userRepeats = userRepeats;
    }

    private static CornerData getCorners(IntList bounds,
                                         ExpandablePattern pattern,
                                         Direction frontFacing, Direction upFacing,
                                         boolean isFlipped) {
        BlockPos.MutableBlockPos negCorner = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos posCorner = new BlockPos.MutableBlockPos();

        Direction[] absolutes = new Direction[3];

        for (int i = 0; i < 3; i++) {
            RelativeDirection selected = pattern.getDirections()[i];

            absolutes[i] = selected.getRelativeFacing(frontFacing, upFacing, isFlipped);

            if (i == 0) {
                negCorner.setX(-bounds.getInt(selected.oppositeOrdinal()));
                posCorner.setX(bounds.getInt(selected.ordinal()));
            } else if (i == 1) {
                negCorner.setY(-bounds.getInt(selected.oppositeOrdinal()));
                posCorner.setY(bounds.getInt(selected.ordinal()));
            } else {
                negCorner.setZ(-bounds.getInt(selected.oppositeOrdinal()));
                posCorner.setZ(bounds.getInt(selected.ordinal()));
            }
        }
        return CornerData.of(posCorner, negCorner, absolutes);
    }

    @Override
    protected void populateWithUserBlockPreferences(MultiblockSchemaInfo info, Map<BlockPos, BlockInfo> resultStructure,
                                                    IBlockPattern pattern,
                                                    Object2ObjectMap<BlockPos, BlockInfo> userBlockPreferences,
                                                    Direction frontFacing, Direction upFacing, boolean isFlipped) {
        ExpandablePattern expandablePattern = (ExpandablePattern) pattern;

        var cornerData = getCorners(userRepeats, expandablePattern, frontFacing, upFacing, isFlipped);
        BoundingBox corners = cornerData.bounds();
        Direction[] absolutes = cornerData.absolutes();
        // contains is min<=x<max, inflate to make sure all positions are inside
        // kinda gross, but it's the least invasive way I guess, maybe look for something better
        BoundingBox bounds = corners.inflatedBy(1);

        for (var entry : userBlockPreferences.object2ObjectEntrySet()) {
            BlockPos pos = entry.getKey(); // absolute-space
            // Reverse-transform to relative/pattern space (transpose of orthogonal rotation) to check against bounds
            int relX = getOffsetFromDirection(absolutes[0], pos);
            int relY = getOffsetFromDirection(absolutes[1], pos);
            int relZ = getOffsetFromDirection(absolutes[2], pos);

            if (bounds.isInside(relX, relY, relZ)) {
                resultStructure.put(pos, entry.getValue());
            }
        }
    }

    protected Char2ObjectMap<MultiPredicate> getPredicatesFromPattern(IBlockPattern pattern) {
        return ((ExpandablePattern) pattern).getSymbolMap();
    }

    @Override
    public void populateFromPattern(MultiblockSchemaInfo info, Map<BlockPos, BlockInfo> resultStructure,
                                    IBlockPattern pattern, Char2ObjectMap<MultiPredicate> sortedPredicates,
                                    Direction frontFacing,
                                    Direction upFacing, boolean isFlipped) {
        ExpandablePattern expandablePattern = (ExpandablePattern) pattern;
        var corners = getCorners(userRepeats, expandablePattern, frontFacing, upFacing, isFlipped);
        Direction[] absolutes = corners.absolutes();

        var predicateProvider = expandablePattern.getPredicateProvider();
        // SOUTH, UP, EAST means point is +z, line is +y, plane is +x.
        // this basically means the x val of the iter is aisle count, y is str count, and z is char count.
        for (BlockPos pos : betweenClosed(corners.bounds())) {
            BlockPos.MutableBlockPos mutablePos = pos.mutable();
            char key = predicateProvider.getPredicateKey(mutablePos, userRepeats);
            MultiPredicate predicate = sortedPredicates.get(key);

            if (predicate == null)
                throw new IllegalStateException(
                        "Predicate provider returned character that is not mapped to a predicate: '%s'"
                                .formatted(key));

            // this basically reshuffles the coordinates into absolute form from relative form
            setFromDirection(mutablePos, absolutes[0], pos.getX());
            setFromDirection(mutablePos, absolutes[1], pos.getY());
            setFromDirection(mutablePos, absolutes[2], pos.getZ());
            // translate from the origin to the center
            // mutablePos = mutablePos.move(translation);
            if (resultStructure.containsKey(mutablePos)) continue;

            if (predicate.isAny()) {
                continue;
            }

            // Attempts to first place the predicate if the min (layer) count isn't satisfied, then the
            // max (layer) count
            if (tryMinCount(info, resultStructure, predicate, key, mutablePos)) continue;
            if (tryMaxCount(info, resultStructure, predicate, key, mutablePos)) continue;
            // If we arrive here, there's nothing we can place that doesn't overflow a max count!
            throw new IllegalStateException("Could not place a block without breaking maxCount requirements");
        }
    }

    @Override
    protected void setup(IBlockPattern pattern, Direction frontFacing, Direction upFacing, boolean isFlipped) {
        this.predicateCount.clear();
        this.basePredicateCount.clear();
    }

    private boolean tryMinCount(MultiblockSchemaInfo info, Map<BlockPos, BlockInfo> resultStructure,
                                MultiPredicate predicate, char predicateChar,
                                BlockPos pos) {
        BasePredicate baseNotSatisfied = null;
        if (predicate.isAnd() || predicate.isOr()) {
            for (BasePredicate basePredicate : predicate.predicates()) {
                int baseMinCount = basePredicate.getPreviewOrMinCount();
                if (baseMinCount == 0) continue;

                int baseTotalAlreadyPopulated = basePredicateCount.getInt(basePredicate);
                boolean baseGlobalMinMet = baseMinCount == -1 || baseTotalAlreadyPopulated >= baseMinCount;

                if (!baseGlobalMinMet) {
                    baseNotSatisfied = basePredicate;
                    break;
                }
            }
        } else if (predicate.isXor()) {
            // For XOR, only one can be true. If we find any condition already satisfied, return false
            int predTotalAlreadyPopulated = predicateCount.getInt(predicate);

            int predMinCount = predicate.getPreviewOrMinCount();
            boolean predGlobalMinMet = predTotalAlreadyPopulated >= predMinCount;
            if (predMinCount != -1 && predGlobalMinMet) return false;

            for (BasePredicate basePredicate : predicate.predicates()) {
                // Same goes for the basePredicates, any satisfied basePredicate with mins returns false
                int baseMinCount = basePredicate.getPreviewOrMinCount();
                if (baseMinCount == 0) return false;

                int baseTotalAlreadyPopulated = basePredicateCount.getInt(basePredicate);
                boolean baseGlobalMinMet = baseTotalAlreadyPopulated >= baseMinCount;

                // If there is a limit, and it's been met, return
                if (baseMinCount != -1 && baseGlobalMinMet) return false;

                // If there is a limit, and it hasn't been met, we have a predicate that needs blocks
                // noinspection ConstantConditions
                if ((baseMinCount != -1 && !baseGlobalMinMet)) {
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
            return true;
        }

        // check if each child predicate min is satisfied
        for (MultiPredicate child : predicate.children()) {
            if (tryMinCount(info, resultStructure, child, predicateChar, pos)) return true;
        }

        // check if main predicate min is satisfied
        int minCount = predicate.getPreviewOrMinCount();
        if (minCount == 0) return false;

        int totalAlreadyPopulated = predicateCount.getInt(predicate);

        boolean globalMinMet = minCount == -1 || totalAlreadyPopulated >= minCount;
        if (globalMinMet) {
            return false;
        }

        BlockInfo toInsert = info.getBlockPreferences().get(predicateChar);
        if (toInsert == null) {
            // TODO filtering?
            toInsert = predicate.getCandidates().get(0).get(0);
        }
        resultStructure.put(pos, toInsert);
        if (this.controllerBlock == null && predicate.isController()) {
            this.controllerBlock = toInsert.getBlockState().getBlock();
        }
        predicateCount.merge(predicate, 1, Integer::sum);
        for (var child : predicate.children()) {
            predicateCount.merge(child, 1, Integer::sum);
        }
        return true;
    }

    private boolean tryMaxCount(MultiblockSchemaInfo info, Map<BlockPos, BlockInfo> resultStructure,
                                MultiPredicate predicate, char predicateChar,
                                BlockPos pos) {
        // check if main predicate max is satisfied
        int maxCount = predicate.getMaxCount();
        if (maxCount == 0) return false;

        int totalAlreadyPopulated = predicateCount.getInt(predicate);

        boolean globalMaxMet = maxCount != -1 && totalAlreadyPopulated >= maxCount;

        if (globalMaxMet) {
            return false;
        }

        // check if each base predicate max is satisfied
        BasePredicate baseNotSatisfied = null;
        if (predicate.isAnd() || predicate.isOr()) {
            for (BasePredicate basePredicate : predicate.predicates()) {
                int baseMaxCount = basePredicate.getMaxCount();
                if (baseMaxCount == 0) continue;

                int baseTotalAlreadyPopulated = basePredicateCount.getInt(basePredicate);
                boolean baseGlobalMaxMet = baseMaxCount != -1 && baseTotalAlreadyPopulated >= baseMaxCount;

                if (!baseGlobalMaxMet) {
                    baseNotSatisfied = basePredicate;
                    break;
                }
            }
        } else if (predicate.isXor()) {
            for (BasePredicate basePredicate : predicate.predicates()) {
                // Any satisfied basePredicate with mins and maxs satisfied returns false
                int baseMaxCount = basePredicate.getMaxCount();
                if (baseMaxCount == 0) return false;

                int baseTotalAlreadyPopulated = basePredicateCount.getInt(basePredicate);
                boolean baseGlobalMaxMet = baseTotalAlreadyPopulated >= baseMaxCount;

                // If there is a limit, and it's been met, return
                if (baseMaxCount != -1 && baseGlobalMaxMet) return false;

                // If there is no limit, or there is one that hasn't been met, we have a predicate that allows blocks
                // noinspection ConstantConditions
                if ((baseMaxCount == -1 || !baseGlobalMaxMet)) {
                    baseNotSatisfied = basePredicate;
                    break;
                }
            }
        }

        if (baseNotSatisfied != null) {
            basePredicateCount.merge(baseNotSatisfied, 1, Integer::sum);
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
            if (tryMaxCount(info, resultStructure, child, predicateChar, pos)) return true;
        }

        BlockInfo toInsert = info.getBlockPreferences().get(predicateChar);
        if (toInsert == null) {
            // TODO filtering?
            toInsert = predicate.getCandidates().get(0).get(0);
        }
        resultStructure.put(pos, toInsert);
        if (this.controllerBlock == null && predicate.isController()) {
            this.controllerBlock = toInsert.getBlockState().getBlock();
        }
        predicateCount.merge(predicate, 1, Integer::sum);
        for (var child : predicate.children()) {
            predicateCount.merge(child, 1, Integer::sum);
        }
        return true;
    }

    @Override
    public MultiPredicate getPredicateFromPos(IBlockPattern pattern, BlockPos pos,
                                              Direction frontFacing, Direction upFacing, boolean isFlipped) {
        ExpandablePattern expandablePattern = (ExpandablePattern) pattern;
        Direction[] absolutes = getCorners(userRepeats, expandablePattern, frontFacing, upFacing, isFlipped)
                .absolutes();
        // Reverse the absolute->relative transform (transpose of orthogonal rotation matrix)
        int relX = getOffsetFromDirection(absolutes[0], pos);
        int relY = getOffsetFromDirection(absolutes[1], pos);
        int relZ = getOffsetFromDirection(absolutes[2], pos);
        char key = expandablePattern.getPredicateProvider().getPredicateKey(new BlockPos(relX, relY, relZ).mutable(),
                userRepeats);
        return Objects.requireNonNull(expandablePattern.getSymbolMap().get(key));
    }

    private static int getOffsetFromDirection(Direction dir, BlockPos pos) {
        return dir.getAxis().choose(pos.getX(), pos.getY(), pos.getZ()) * dir.getAxisDirection().getStep();
    }

    public static Iterable<BlockPos> betweenClosed(BoundingBox box) {
        return BlockPos.betweenClosed(Math.min(box.minX(), box.maxX()),
                Math.min(box.minY(), box.maxY()),
                Math.min(box.minZ(), box.maxZ()),
                Math.max(box.minX(), box.maxX()),
                Math.max(box.minY(), box.maxY()),
                Math.max(box.minZ(), box.maxZ()));
    }

    private static BlockPos.MutableBlockPos setFromDirection(BlockPos.MutableBlockPos pos,
                                                             Direction direction, int amount) {
        return switch (direction) {
            case DOWN -> pos.setY(-amount);
            case UP -> pos.setY(amount);
            case NORTH -> pos.setZ(-amount);
            case SOUTH -> pos.setZ(amount);
            case WEST -> pos.setX(-amount);
            case EAST -> pos.setX(amount);
        };
    }

    private record CornerData(BoundingBox bounds, Direction[] absolutes) {

        private static CornerData of(BlockPos posCorner, BlockPos negCorner, Direction[] directions) {
            return new CornerData(BoundingBox.fromCorners(posCorner, negCorner), directions);
        }
    }
}
