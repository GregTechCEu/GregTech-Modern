package com.gregtechceu.gtceu.api.multiblock;

import com.gregtechceu.gtceu.api.multiblock.error.PatternError;
import com.gregtechceu.gtceu.api.multiblock.pattern.CurrentBlockInfo;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import lombok.Getter;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PatternPredicate {

    public static PatternPredicate ANY = new PatternPredicate("Any", currentBlockInfo -> null, null);
    public static PatternPredicate AIR = new PatternPredicate("Air",
            currentBlockInfo -> currentBlockInfo.getBlockState().isAir() ? null :
                    new PatternError(currentBlockInfo.getBlockPos(), Collections.emptyList()),
            Collections.singletonList(BlockInfo.EMPTY));

    public List<BasePredicate> predicateList = new ArrayList<>();
    @Getter
    protected boolean isController;
    protected boolean hasAir = false;
    @Getter
    protected boolean isSingle = true;

    public PatternPredicate() {}

    public PatternPredicate(PatternPredicate predicate) {
        predicateList.addAll(predicate.predicateList);
        isController = predicate.isController;
        hasAir = predicate.hasAir;
        isSingle = predicate.isSingle;
    }

    /**
     * @param debugName  the debug name
     * @param predicate  the testing function for if the current block information is valid
     * @param candidates the valid list of BlockInfos that this traceability predicate allows
     */
    public PatternPredicate(String debugName, Function<CurrentBlockInfo, PatternError> predicate,
                            List<BlockInfo> candidates) {
        predicateList.add(new BasePredicate(debugName, predicate, candidates));
    }

    /**
     *
     * @param predicate  the testing function for if the current block information is valid
     * @param candidates the valid list of BlockInfos that this traceability predicate allows
     */
    public PatternPredicate(Function<CurrentBlockInfo, PatternError> predicate,
                            List<BlockInfo> candidates) {
        predicateList.add(new BasePredicate(predicate, candidates));
    }

    public PatternPredicate(Function<CurrentBlockInfo, PatternError> predicate) {
        this(predicate, null);
    }

    public PatternPredicate(BasePredicate basePredicate) {
        predicateList.add(basePredicate);
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
            predicateList.forEach(predicate -> {
                if (predicate.candidates == null) return;
                if (predicate.toolTips == null) {
                    predicate.toolTips = new ArrayList<>();
                }
                predicate.toolTips.addAll(tooltips);
            });
        }
        return this;
    }

    public List<List<ItemStack>> getCandidates() {
        return predicateList.stream()
                .map(BasePredicate::getCandidateStacks)
                .collect(Collectors.toList());
    }

    /**
     * Set the minimum number of candidate blocks.
     */
    public PatternPredicate setMinGlobalLimited(int min) {
        predicateList.forEach(p -> p.minCount = min);
        return this;
    }

    public PatternPredicate setMinGlobalLimited(int min, int previewCount) {
        return this.setMinGlobalLimited(min).setPreviewCount(previewCount);
    }

    /**
     * Set the maximum number of candidate blocks.
     */
    public PatternPredicate setMaxGlobalLimited(int max) {
        predicateList.forEach(p -> p.maxCount = max);
        return this;
    }

    public PatternPredicate setMaxGlobalLimited(int max, int previewCount) {
        return this.setMaxGlobalLimited(max).setPreviewCount(previewCount);
    }

    /**
     * Set the minimum number of candidate blocks for each aisle layer.
     */
    public PatternPredicate setMinLayerLimited(int min) {
        predicateList.forEach(p -> p.minLayerCount = min);
        return this;
    }

    public PatternPredicate setMinLayerLimited(int min, int previewCount) {
        return this.setMinLayerLimited(min).setPreviewCount(previewCount);
    }

    /**
     * Set the maximum number of candidate blocks for each aisle layer.
     */
    public PatternPredicate setMaxLayerLimited(int max) {
        predicateList.forEach(p -> p.maxLayerCount = max);
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
        predicateList.forEach(p -> p.previewCount = count);
        return this;
    }

    /**
     * Set renderMask.
     */
    public PatternPredicate disableRenderFormed() {
        predicateList.forEach(p -> p.disableRenderFormed = true);
        return this;
    }

    public PatternPredicate setNBTParser(String nbtParser) {
        predicateList.forEach(predicate -> predicate.nbtParser = nbtParser);
        return this;
    }

    public PatternError test(CurrentBlockInfo currBlock, Object2IntMap<BasePredicate> globalCache,
                             Object2IntMap<BasePredicate> layerCache) {
        PatternError lastError = null;
        for (BasePredicate p : predicateList) {
            PatternError error = p.testLimited(currBlock, globalCache, layerCache);
            if (error == null) return null;
            lastError = error;
        }
        return lastError == PatternError.PLACEHOLDER ? new PatternError(currBlock.getBlockPos(), getCandidates()) :
                lastError;
    }

    public PatternPredicate or(PatternPredicate other) {
        if (other != null) {
            PatternPredicate newPredicate = new PatternPredicate(this);
            newPredicate.hasAir = newPredicate.hasAir || this == AIR || other == AIR;
            newPredicate.predicateList.addAll(other.predicateList);
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
                this.predicateList.equals(pred.predicateList);
    }
}
