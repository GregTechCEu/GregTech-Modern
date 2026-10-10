package com.gregtechceu.gtceu.api.multiblock.util;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.MultiblockSchemaInfo;
import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.ExpandablePattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ExpandablePatternHelper extends AbstractStructureHelper {

    private final IntList userRepeats;

    private final Object2IntOpenHashMap<MultiPredicate> predicateCount = new Object2IntOpenHashMap<>();
    private final Object2IntOpenHashMap<BasePredicate> basePredicateCount = new Object2IntOpenHashMap<>();

    protected ExpandablePatternHelper(IntList userRepeats) {
        this.userRepeats = userRepeats;
    }

    @Override
    protected void setup(IBlockPattern pattern, Direction frontFacing, Direction upFacing, boolean isFlipped) {
        this.predicateCount.clear();
        this.basePredicateCount.clear();
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
                                                    Char2ObjectMap<MultiPredicate> sortedPredicates,
                                                    Object2ObjectMap<BlockPos, BlockInfo> userBlockPreferences,
                                                    Direction frontFacing, Direction upFacing, boolean isFlipped) {
        ExpandablePattern expandablePattern = (ExpandablePattern) pattern;

        var cornerData = getCorners(userRepeats, expandablePattern, frontFacing, upFacing, isFlipped);
        BoundingBox corners = cornerData.bounds();
        Direction[] absolutes = cornerData.absolutes();
        // contains is min<=x<max, inflate to make sure all positions are inside
        // kinda gross, but it's the least invasive way I guess, maybe look for something better
        BoundingBox bounds = corners.inflatedBy(1);

        var predicateProvider = expandablePattern.getPredicateProvider();

        for (var entry : userBlockPreferences.object2ObjectEntrySet()) {
            BlockPos pos = entry.getKey(); // absolute-space
            BlockInfo blockInfo = entry.getValue();
            // Reverse-transform to relative/pattern space (transpose of orthogonal rotation) to check against bounds
            int relX = getOffsetFromDirection(absolutes[0], pos);
            int relY = getOffsetFromDirection(absolutes[1], pos);
            int relZ = getOffsetFromDirection(absolutes[2], pos);

            if (!bounds.isInside(relX, relY, relZ)) continue;

            char key = predicateProvider.getPredicateKey(new BlockPos.MutableBlockPos(relX, relY, relZ), userRepeats);
            MultiPredicate predicate = sortedPredicates.get(key);
            if (predicate == null) continue;
            var basePair = getBasePredicateRoute(new ArrayList<>(List.of(predicate)), blockInfo);
            if (basePair == null) {
                GTCEu.LOGGER.warn("Ignoring invalid preference {} for position {}",
                        blockInfo.getBlockState().getBlock().getName().getString(), pos);
                continue;
            }
            resultStructure.put(pos, blockInfo);
            incrementPredicate(basePair.value(), basePair.key());
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
            List<MultiPredicate> chain = new ArrayList<>();
            chain.add(predicate);
            if (tryMinCount(info, resultStructure, predicate, key, mutablePos, chain)) continue;
            if (tryMaxCount(info, resultStructure, predicate, key, mutablePos, chain)) continue;
            // If we arrive here, there's nothing we can place that doesn't overflow a max count!
            throw new IllegalStateException("Could not place a block without breaking maxCount requirements");
        }
    }

    private void incrementPredicate(List<MultiPredicate> predicateChain, BasePredicate base) {
        basePredicateCount.merge(base, 1, Integer::sum);
        for (var pred : predicateChain) {
            predicateCount.merge(pred, 1, Integer::sum);
        }
    }

    private @Nullable Pair<BasePredicate, List<MultiPredicate>> getBasePredicateRoute(List<MultiPredicate> predicateChain,
                                                                                      BlockInfo info) {
        MultiPredicate last = predicateChain.get(predicateChain.size() - 1);
        for (var base : last.predicates()) {
            if (base.getCandidates().contains(info)) {
                return Pair.of(base, predicateChain);
            }
        }
        for (var child : last.children()) {
            predicateChain.add(child);
            var pair = getBasePredicateRoute(predicateChain, info);
            if (pair != null) {
                return pair;
            }
            predicateChain.remove(predicateChain.size() - 1);
        }
        return null;
    }

    private boolean tryMinCount(MultiblockSchemaInfo info, Map<BlockPos, BlockInfo> resultStructure,
                                MultiPredicate predicate, char predicateKey, BlockPos pos,
                                List<MultiPredicate> predicateChain) {
        // Find first unsatisfied min predicate while also checking type specific logic
        BasePredicate baseNotSatisfied = null;
        if (predicate.isAnd() || predicate.isOr()) {
            for (BasePredicate basePredicate : predicate.predicates()) {
                int baseMinCount = basePredicate.getPreviewOrMinCount();
                if (baseMinCount == 0) continue;

                int baseTotalAlreadyPopulated = basePredicateCount.getInt(basePredicate);
                if (baseMinCount != -1 && baseTotalAlreadyPopulated < baseMinCount) {
                    baseNotSatisfied = basePredicate;
                    break;
                }
            }
        } else if (predicate.isXor()) {
            // For XOR, only one can be true. If we find any condition already satisfied, return false
            int predTotalAlreadyPopulated = predicateCount.getInt(predicate);
            int predMinCount = predicate.getPreviewOrMinCount();
            if (predMinCount != -1 && predTotalAlreadyPopulated >= predMinCount) return false;

            for (BasePredicate basePredicate : predicate.predicates()) {
                // Same goes for the basePredicates, any satisfied basePredicate with mins returns false
                int baseMinCount = basePredicate.getPreviewOrMinCount();
                if (baseMinCount == 0) return false;

                int baseTotalAlreadyPopulated = basePredicateCount.getInt(basePredicate);
                // If there is a limit, and it's been met, return
                if (baseMinCount != -1 && baseTotalAlreadyPopulated >= baseMinCount) return false;
                // If there is a limit, and it hasn't been met, we have a predicate that needs blocks
                if (baseMinCount != -1) {
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
            incrementPredicate(predicateChain, baseNotSatisfied);
            return true;
        }

        // check if each child predicate min is satisfied
        for (MultiPredicate child : predicate.children()) {
            predicateChain.add(child);
            if (tryMinCount(info, resultStructure, child, predicateKey, pos, predicateChain)) return true;
            predicateChain.remove(predicateChain.size() - 1);
        }

        // check if main predicate min is satisfied
        int minCount = predicate.getPreviewOrMinCount();
        if (minCount == 0) return false;

        int totalAlreadyPopulated = predicateCount.getInt(predicate);
        if (minCount == -1 || totalAlreadyPopulated >= minCount) {
            return false;
        }

        BlockInfo toInsert = info.getBlockPreferences().get(predicateKey);
        if (toInsert == null) {
            // TODO filtering?
            toInsert = predicate.getCandidates().get(0).get(0);
        }
        var basePair = getBasePredicateRoute(predicateChain, toInsert);
        if (basePair == null) return false;
        resultStructure.put(pos, toInsert);
        if (this.controllerBlock == null && predicate.isController()) {
            this.controllerBlock = toInsert.getBlockState().getBlock();
        }
        incrementPredicate(basePair.value(), basePair.key());
        return true;
    }

    private boolean tryMaxCount(MultiblockSchemaInfo info, Map<BlockPos, BlockInfo> resultStructure,
                                MultiPredicate predicate, char predicateKey, BlockPos pos,
                                List<MultiPredicate> predicateChain) {
        // check if main predicate max is satisfied
        int maxCount = predicate.getMaxCount();
        if (maxCount == 0) return false;

        int totalAlreadyPopulated = predicateCount.getInt(predicate);
        if (maxCount != -1 && totalAlreadyPopulated >= maxCount) {
            return false;
        }

        // check if each base predicate max is satisfied
        BasePredicate baseNotSatisfied = null;
        if (predicate.isAnd() || predicate.isOr()) {
            for (BasePredicate basePredicate : predicate.predicates()) {
                int baseMaxCount = basePredicate.getMaxCount();
                if (baseMaxCount == 0) continue;

                int baseTotalAlreadyPopulated = basePredicateCount.getInt(basePredicate);
                if (baseMaxCount == -1 || baseTotalAlreadyPopulated < baseMaxCount) {
                    baseNotSatisfied = basePredicate;
                    break;
                }
            }
        } else if (predicate.isXor()) {
            for (BasePredicate basePredicate : predicate.predicates()) {
                // Any satisfied basePredicate with maxs satisfied returns false
                int baseMaxCount = basePredicate.getMaxCount();
                if (baseMaxCount == 0) return false;

                int baseTotalAlreadyPopulated = basePredicateCount.getInt(basePredicate);
                // If there is a limit, and it's been met, return
                if (baseMaxCount != -1 && baseTotalAlreadyPopulated >= baseMaxCount) return false;
                // If there is no limit, or there is one that hasn't been met, we have a predicate that allows blocks
                baseNotSatisfied = basePredicate;
                break;
            }
        }

        if (baseNotSatisfied != null) {
            incrementPredicate(predicateChain, baseNotSatisfied);
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

        // check if each child predicate max is satisfied
        for (MultiPredicate child : predicate.children()) {
            predicateChain.add(child);
            if (tryMaxCount(info, resultStructure, child, predicateKey, pos, predicateChain)) return true;
            predicateChain.remove(predicateChain.size() - 1);
        }

        BlockInfo toInsert = info.getBlockPreferences().get(predicateKey);
        if (toInsert == null) {
            // TODO filtering?
            toInsert = predicate.getCandidates().get(0).get(0);
        }
        var basePair = getBasePredicateRoute(predicateChain, toInsert);
        if (basePair == null) return false;
        resultStructure.put(pos, toInsert);
        if (this.controllerBlock == null && predicate.isController()) {
            this.controllerBlock = toInsert.getBlockState().getBlock();
        }
        incrementPredicate(basePair.value(), basePair.key());
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
