package com.gregtechceu.gtceu.api.multiblock.predicates;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.mui.MultiblockSchemaInfo;
import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.api.multiblock.PredicateContext;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.RenderLevelStageEvent;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

class TestablePredicate extends BasePredicate {

    private final ErrorHandler onError;
    private final Supplier<List<BlockInfo>> candidates;
    private final @Nullable Consumer<StringBuilder> contents;
    private final String name;
    private final Predicate<PredicateContext> predicate;

    /**
     * @param name       The debug name
     * @param predicate  The predicate function for being a valid block state or tile entity in a pattern
     * @param candidates The qualifying blocks or item stacks valid in this predicate based on information from
     *                   either the
     *                   {@link com.gregtechceu.gtceu.common.item.behavior.TerminalBehavior#use(Item, Level, Player, InteractionHand)
     *                   Terminal Auto-Builder},
     *                   {@link com.gregtechceu.gtceu.client.renderer.PatternPreviewRenderer#draw(PoseStack, MultiBufferSource.BufferSource, Camera, RenderLevelStageEvent.Stage, float)
     *                   In-world Preview} or
     *                   {@link com.gregtechceu.gtceu.integration.recipeviewer.widgets.MultiblockPreviewWidget#MultiblockPreviewWidget(MultiblockMachineDefinition definition, MultiblockSchemaInfo schemaInfo, int width,int height)
     *                   XEI Preview}
     */
    TestablePredicate(String name, Predicate<PredicateContext> predicate,
                      Supplier<List<BlockInfo>> candidates,
                      @Nullable Consumer<StringBuilder> contents,
                      ErrorHandler onError) {
        this.name = name;
        this.predicate = predicate;
        this.candidates = candidates;
        this.contents = contents;
        this.onError = onError;
    }

    @Override
    public List<Component> getRecipeViewerTooltips(MultiPredicate root) {
        List<Component> tooltips = new ArrayList<>(this.getAdditionalTooltips());
        if (minCount == maxCount && maxCount != -1) {
            tooltips.add(Component.translatable("gtceu.multiblock.pattern.exact_count", minCount));
        } else if (minCount != maxCount && minCount != -1 && maxCount != -1) {
            tooltips.add(Component.translatable("gtceu.multiblock.pattern.between_count", minCount, maxCount));
        } else {
            if (minCount > 0) {
                tooltips.add(Component.translatable("gtceu.multiblock.pattern.min_count", minCount));
            }
            if (maxCount != -1) {
                tooltips.add(Component.translatable("gtceu.multiblock.pattern.max_count", maxCount));
            }
        }
        if (root.isSingle()) {
            tooltips.add(Component.translatable("gtceu.multiblock.pattern.single"));
        }
        if (root.hasAir()) {
            tooltips.add(Component.translatable("gtceu.multiblock.pattern.replaceable_air"));
        }
        return tooltips;
    }

    @Override
    public void onError(PredicateContext ctx) {
        this.onError.appendError(ctx, this);
    }

    @Override
    public boolean test(PredicateContext ctx) {
        return this.predicate.test(ctx);
    }

    @Override
    public BasePredicate copy() {
        TestablePredicate copy = new TestablePredicate(this.name, this.predicate, this.candidates, this.contents,
                this.onError);
        copyTo(copy);
        return copy;
    }

    @Override
    public List<BlockInfo> computeCandidates() {
        return this.candidates.get();
    }

    @Override
    public String getTypeName() {
        return this.name;
    }

    @Override
    protected void appendContents(StringBuilder builder) {
        if (this.contents != null) {
            this.contents.accept(builder);
        }
    }
}
