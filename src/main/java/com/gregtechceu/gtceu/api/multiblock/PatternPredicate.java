package com.gregtechceu.gtceu.api.multiblock;

import com.gregtechceu.gtceu.api.multiblock.error.PatternError;
import com.gregtechceu.gtceu.api.multiblock.error.SimplePatternError;
import com.gregtechceu.gtceu.api.multiblock.pattern.CurrentBlockInfo;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import net.minecraft.network.chat.Component;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PatternPredicate {

    public static PatternPredicate ANY = new PatternPredicate("Any", currentBlockInfo -> null, null);
    public static PatternPredicate AIR = new PatternPredicate("Air",
            currentBlockInfo -> currentBlockInfo.retrieveCurrentBlockState().isAir() ? null :
                    new SimplePatternError(currentBlockInfo.getBlockPos(), Collections.emptyList()),
            Collections.singletonList(BlockInfo.EMPTY));
    private static final Comparator<BasePredicate> predicateComparator = Comparator.comparingInt(p -> p.priority);
    public List<BasePredicate> subPredicates = new ArrayList<>();
    @Getter
    protected boolean isController;
    protected boolean hasAir = false;
    @Getter
    protected boolean isSingle = true;

    public PatternPredicate() {}

    public PatternPredicate(PatternPredicate predicate) {
        subPredicates.addAll(predicate.subPredicates);
        isController = predicate.isController;
        hasAir = predicate.hasAir;
        isSingle = predicate.isSingle;
    }

    /**
     * @param debugName  the debug name
     * @param predicate  the testing function for if the current block information is valid
     * @param candidates the valid list of BlockInfos that this traceability predicate allows
     */
    public PatternPredicate(String debugName, Function<CurrentBlockInfo, @Nullable PatternError> predicate,
                            @Nullable List<BlockInfo> candidates) {
        subPredicates.add(new BasePredicate(debugName, predicate, candidates));
    }

    /**
     *
     * @param predicate  the testing function for if the current block information is valid
     * @param candidates the valid list of BlockInfos that this traceability predicate allows
     */
    public PatternPredicate(Function<CurrentBlockInfo, @Nullable PatternError> predicate,
                            @Nullable List<BlockInfo> candidates) {
        this("Unknown", predicate, candidates);
    }

    public PatternPredicate(Function<CurrentBlockInfo, @Nullable PatternError> predicate) {
        this(predicate, null);
    }

    public PatternPredicate(BasePredicate basePredicate) {
        subPredicates.add(basePredicate);
    }

    /**
     * Mark it as the controller of this multi. Normally you won't call it yourself. Use plz.
     */
    public PatternPredicate setController() {
        isController = true;
        return this;
    }

    public boolean hasAir() {
        return hasAir;
    }

    /**
     * Add tooltips for candidates. They are shown in JEI Pages.
     */
    public PatternPredicate addTooltips(Component... tips) {
        if (tips.length > 0) {
            List<Component> tooltips = Arrays.stream(tips).toList();
            subPredicates.forEach(predicate -> {
                if (predicate.tooltips == null) {
                    predicate.tooltips = new ArrayList<>();
                }
                predicate.tooltips.addAll(tooltips);
            });
        }
        return this;
    }

    public List<List<BlockInfo>> getCandidates() {
        return subPredicates.stream()
                .map(BasePredicate::getCandidates)
                .collect(Collectors.toList());
    }

    /**
     * Set the minimum number of candidate blocks.
     */
    public PatternPredicate setMinGlobalLimited(int min) {
        subPredicates.forEach(p -> p.minCount = min);
        return this;
    }

    public PatternPredicate setMinGlobalLimited(int min, int previewCount) {
        return this.setMinGlobalLimited(min).setPreviewCount(previewCount);
    }

    /**
     * Set the maximum number of candidate blocks.
     */
    public PatternPredicate setMaxGlobalLimited(int max) {
        subPredicates.forEach(p -> p.maxCount = max);
        return this;
    }

    public PatternPredicate setMaxGlobalLimited(int max, int previewCount) {
        return this.setMaxGlobalLimited(max).setPreviewCount(previewCount);
    }

    /**
     * Set the minimum number of candidate blocks for each slice layer.
     */
    public PatternPredicate setMinLayerLimited(int min) {
        subPredicates.forEach(p -> p.minSliceCount = min);
        return this;
    }

    public PatternPredicate setMinLayerLimited(int min, int previewCount) {
        return this.setMinLayerLimited(min).setPreviewCount(previewCount);
    }

    /**
     * Set the maximum number of candidate blocks for each slice layer.
     */
    public PatternPredicate setMaxLayerLimited(int max) {
        subPredicates.forEach(p -> p.maxSliceCount = max);
        return this;
    }

    public PatternPredicate setMaxLayerLimited(int max, int previewCount) {
        return this.setMaxLayerLimited(max).setPreviewCount(previewCount);
    }

    /**
     * Sets the Minimum and Maximum limit to the passed value
     * 
     * @param limit The Maximum and Minimum limit
     */
    public PatternPredicate setExactLimit(int limit) {
        return this.setMinGlobalLimited(limit).setMaxGlobalLimited(limit);
    }

    /**
     * Set the number of it appears in JEI pages. It only affects JEI preview. (The specific number)
     */
    public PatternPredicate setPreviewCount(int count) {
        subPredicates.forEach(p -> p.previewCount = count);
        return this;
    }

    public PatternPredicate setPriority(int priority) {
        subPredicates.forEach(p -> p.priority = priority);
        return this;
    }

    /**
     * Set renderMask.
     */
    public PatternPredicate disableRenderFormed() {
        subPredicates.forEach(p -> p.disableRenderFormed = true);
        return this;
    }

    public PatternPredicate setNBTParser(String nbtParser) {
        subPredicates.forEach(p -> p.nbtParser = nbtParser);
        return this;
    }

    public List<PatternError> test(CurrentBlockInfo currBlock, Object2IntMap<BasePredicate> globalCache,
                                   @Nullable Object2IntMap<BasePredicate> layerCache) {
        List<PatternError> lastErrors = new ArrayList<>();
        for (BasePredicate p : subPredicates) {
            PatternError error = p.testLimited(currBlock, globalCache, layerCache);
            if (error == null) return List.of();
            lastErrors.add(error);
        }
        return lastErrors;
    }

    public PatternPredicate or(@Nullable PatternPredicate other) {
        if (other != null) {
            PatternPredicate newPredicate = new PatternPredicate(this);
            newPredicate.hasAir = newPredicate.hasAir || this == AIR || other == AIR;
            newPredicate.subPredicates.addAll(other.subPredicates);
            newPredicate.subPredicates.sort(predicateComparator);
            return newPredicate;
        }
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof PatternPredicate pred)) return false;

        return this.hasAir == pred.hasAir &&
                this.isController == pred.isController &&
                this.subPredicates.equals(pred.subPredicates);
    }
}
