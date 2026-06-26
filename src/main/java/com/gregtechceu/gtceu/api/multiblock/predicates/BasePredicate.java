package com.gregtechceu.gtceu.api.multiblock.predicates;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.mui.MultiblockSchemaInfo;
import com.gregtechceu.gtceu.api.multiblock.PatternPredicate;
import com.gregtechceu.gtceu.api.multiblock.error.PatternError;
import com.gregtechceu.gtceu.api.multiblock.error.SinglePredicateError;
import com.gregtechceu.gtceu.api.multiblock.pattern.CurrentBlockInfo;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.client.renderer.PatternPreviewRenderer;
import com.gregtechceu.gtceu.common.item.behavior.TerminalBehavior;
import com.gregtechceu.gtceu.data.lang.LangHandler;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.MultiblockPreviewWidget;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class BasePredicate {

    @Getter
    public List<BlockInfo> candidates;
    public Function<CurrentBlockInfo, @Nullable PatternError> errorPredicate;
    public @Nullable List<Component> tooltips;
    public int priority = 0;
    public int minCount = -1;
    public int maxCount = -1;
    public int minSliceCount = -1;
    public int maxSliceCount = -1;
    public int previewCount = -1;
    public boolean disableRenderFormed = false;
    public @Nullable String nbtParser;

    protected String debugName;

    public BasePredicate() {
        this.debugName = "Unknown";
        this.errorPredicate = $ -> null;
        this.candidates = Collections.emptyList();
    }

    /**
     * @param errorPredicate The predicate function for being a valid block state or tile entity in a pattern
     * @param candidates     The qualifying blocks or item stacks valid in this predicate based on information from
     *                       either the
     *                       {@link TerminalBehavior#use(Item, Level, Player, InteractionHand)
     *                       Terminal Auto-Builder},
     *                       {@link PatternPreviewRenderer#draw(PoseStack, MultiBufferSource.BufferSource, Camera, RenderLevelStageEvent.Stage, float)
     *                       In-world Preview} or
     *                       {@link MultiblockPreviewWidget#MultiblockPreviewWidget(MultiblockMachineDefinition, MultiblockSchemaInfo)
     *                       XEI Preview}
     */
    public BasePredicate(Function<CurrentBlockInfo, PatternError> errorPredicate,
                         @Nullable List<BlockInfo> candidates) {
        this("Unknown", errorPredicate, candidates);
    }

    public BasePredicate(String debugName, Function<CurrentBlockInfo, @Nullable PatternError> errorPredicate,
                         @Nullable List<BlockInfo> candidates) {
        this.debugName = debugName;
        this.errorPredicate = errorPredicate;
        this.candidates = candidates != null ? candidates : Collections.emptyList();
    }

    @OnlyIn(Dist.CLIENT)
    public List<Component> getTooltips(@Nullable PatternPredicate predicates) {
        List<Component> result = new ArrayList<>();
        if (tooltips != null) {
            result.addAll(tooltips);
        }
        if (minCount == maxCount && maxCount != -1) {
            result.add(Component.translatable("gtceu.multiblock.pattern.error.limited_exact", minCount));
        } else if (minCount != maxCount && minCount != -1 && maxCount != -1) {
            result.add(Component.translatable("gtceu.multiblock.pattern.error.limited_within", minCount, maxCount));
        } else {
            if (minCount != -1) {
                result.add(LangHandler.getFromMultiLang("gtceu.multiblock.pattern.error.limited", 1, minCount));
            }
            if (maxCount != -1) {
                result.add(LangHandler.getFromMultiLang("gtceu.multiblock.pattern.error.limited", 0, maxCount));
            }
        }
        if (predicates == null) return result;
        if (predicates.isSingle()) {
            result.add(Component.translatable("gtceu.multiblock.pattern.single"));
        }
        if (predicates.hasAir()) {
            result.add(Component.translatable("gtceu.multiblock.pattern.replaceable_air"));
        }
        return result;
    }

    public @Nullable PatternError testRaw(CurrentBlockInfo currBlock) {
        return errorPredicate.apply(currBlock);
    }

    public @Nullable PatternError testLimited(CurrentBlockInfo currBlock,
                                              Object2IntMap<BasePredicate> globalCache,
                                              @Nullable Object2IntMap<BasePredicate> layerCache) {
        PatternError error = testGlobal(currBlock, globalCache, layerCache);
        if (error != null) return error;
        return testLayer(currBlock, layerCache);
    }

    public @Nullable PatternError testGlobal(CurrentBlockInfo currentBlock,
                                             Object2IntMap<BasePredicate> globalCache,
                                             @Nullable Object2IntMap<BasePredicate> layerCache) {
        PatternError error = errorPredicate.apply(currentBlock);
        globalCache.mergeInt(this, (error == null ? 1 : 0), Integer::sum);
        if ((minCount == -1 && maxCount == -1) || error != null || layerCache == null) return error;

        int count = globalCache.getInt(this);
        if (maxCount == -1 || count <= maxCount) return null;

        return new SinglePredicateError(this, SinglePredicateError.ErrorType.MAX_COUNT, count);
    }

    public @Nullable PatternError testLayer(CurrentBlockInfo currBlock,
                                            @Nullable Object2IntMap<BasePredicate> layerCache) {
        PatternError error = errorPredicate.apply(currBlock);
        if (layerCache == null) return error;

        layerCache.mergeInt(this, (error == null ? 1 : 0), Integer::sum);
        if ((minSliceCount == -1 && maxSliceCount == -1) || error != null) return error;

        if (maxSliceCount != -1 && layerCache.getInt(this) > maxSliceCount) {
            return new SinglePredicateError(this, SinglePredicateError.ErrorType.MAX_LAYER_COUNT,
                    layerCache.getInt(this));
        }

        return null;
    }

    public String getPredicateName() {
        return debugName;
    }
}
