package com.gregtechceu.gtceu.api.multiblock.util;

import com.gregtechceu.gtceu.api.multiblock.PatternPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.ExpandablePattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

import java.util.Map;

public class ExpandablePatternHelper extends AbstractStructureHelper {

    private final IntList userRepeats;

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
    protected void populateWithUserBlockPreferences(Map<BlockPos, BlockInfo> resultStructure, IBlockPattern pattern,
                                                    Long2ObjectMap<BlockInfo> userBlockPreferences,
                                                    Direction frontFacing, Direction upFacing, boolean isFlipped) {
        ExpandablePattern expandablePattern = (ExpandablePattern) pattern;

        var cornerData = getCorners(userRepeats, expandablePattern, frontFacing, upFacing, isFlipped);
        BoundingBox corners = cornerData.bounds();
        Direction[] absolutes = cornerData.absolutes();
        // contains is min<=x<max, inflate to make sure all positions are inside
        // kinda gross, but it's the least invasive way I guess, maybe look for something better
        BoundingBox bounds = corners.inflatedBy(1);

        for (var entry : userBlockPreferences.long2ObjectEntrySet()) {
            BlockPos pos = BlockPos.of(entry.getLongKey()); // absolute-space
            // Reverse-transform to relative/pattern space (transpose of orthogonal rotation) to check against bounds
            int relX = getOffsetFromDirection(absolutes[0], pos);
            int relY = getOffsetFromDirection(absolutes[1], pos);
            int relZ = getOffsetFromDirection(absolutes[2], pos);

            if (bounds.isInside(relX, relY, relZ)) {
                resultStructure.put(pos, entry.getValue());
            }
        }
    }

    @Override
    public void populateFromPattern(Map<BlockPos, BlockInfo> resultStructure, IBlockPattern pattern,
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
            PatternPredicate predicate = predicateProvider.apply(mutablePos, userRepeats);

            // this basically reshuffles the coordinates into absolute form from relative form
            setFromDirection(mutablePos, absolutes[0], pos.getX());
            setFromDirection(mutablePos, absolutes[1], pos.getY());
            setFromDirection(mutablePos, absolutes[2], pos.getZ());
            // translate from the origin to the center
            // mutablePos = mutablePos.move(translation);
            if (resultStructure.containsKey(mutablePos)) continue;

            // Attempts to first place the predicate if the min (layer) count isn't satisfied, then the
            // max (layer) count
            if (tryMinCount(resultStructure, predicate, mutablePos)) continue;
            if (tryMaxCount(resultStructure, predicate, mutablePos)) continue;
            // If we arrive here, there's nothing we can place that doesn't overflow a max count!
            throw new IllegalStateException("Could not place a block without breaking maxCount requirements");
        }
    }

    private boolean tryMinCount(Map<BlockPos, BlockInfo> resultStructure, PatternPredicate predicate,
                                BlockPos pos) {
        for (BasePredicate basePredicate : predicate.subPredicates) {
            int minCount = getMinCount(predicate, basePredicate);
            if (minCount == 0) continue;

            int totalAlreadyPopulated = countPopulatedGlobal(resultStructure, basePredicate);
            if (minCount <= 0 || totalAlreadyPopulated >= minCount) continue;

            BlockInfo toInsert = null;
            if (blockPreferences.contains(predicate, basePredicate)) {
                toInsert = blockPreferences.get(predicate, basePredicate);
            } else if (!basePredicate.getCandidates().isEmpty()) {
                toInsert = basePredicate.getCandidates().get(0);
            }
            if (toInsert != null) resultStructure.put(pos, toInsert);
            return true;
        }
        return false;
    }

    private boolean tryMaxCount(Map<BlockPos, BlockInfo> resultStructure, PatternPredicate predicate,
                                BlockPos pos) {
        for (BasePredicate basePredicate : predicate.subPredicates) {
            int maxCount = getMaxCount(predicate, basePredicate);
            if (maxCount == 0) continue;

            int totalAlreadyPopulated = countPopulatedGlobal(resultStructure, basePredicate);
            if (maxCount != -1 && totalAlreadyPopulated >= maxCount) continue;

            BlockInfo toInsert = null;
            if (blockPreferences.contains(predicate, basePredicate)) {
                toInsert = blockPreferences.get(predicate, basePredicate);
            } else if (!basePredicate.getCandidates().isEmpty()) {
                toInsert = basePredicate.getCandidates().get(0);
            }
            if (toInsert != null) resultStructure.put(pos, toInsert);
            return true;
        }
        return false;
    }

    @Override
    public PatternPredicate getPredicateFromPos(IBlockPattern pattern, BlockPos pos,
                                                Direction frontFacing, Direction upFacing, boolean isFlipped) {
        ExpandablePattern expandablePattern = (ExpandablePattern) pattern;
        Direction[] absolutes = getCorners(userRepeats, expandablePattern, frontFacing, upFacing, isFlipped)
                .absolutes();
        // Reverse the absolute->relative transform (transpose of orthogonal rotation matrix)
        int relX = getOffsetFromDirection(absolutes[0], pos);
        int relY = getOffsetFromDirection(absolutes[1], pos);
        int relZ = getOffsetFromDirection(absolutes[2], pos);
        return expandablePattern.getPredicateProvider().apply(new BlockPos(relX, relY, relZ).mutable(), userRepeats);
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
