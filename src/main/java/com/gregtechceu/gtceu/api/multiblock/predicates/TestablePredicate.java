package com.gregtechceu.gtceu.api.multiblock.predicates;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.mui.MultiblockSchemaInfo;
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

import brachy.modularui.api.drawable.Text;
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

    /// @return a list of components to be displayed while hovering over a block in the Multiblock Preview
    public List<Component> getRecipeViewerTooltips() {
        List<Component> tooltips = new ArrayList<>(this.getAdditionalTooltips());
        if (minCount == maxCount && maxCount != -1) {
            tooltips.add(Component.translatable("gtceu.multiblock.pattern.error.limited.exact", minCount));
        } else if (minCount != maxCount && minCount != -1 && maxCount != -1) {
            tooltips.add(Component.translatable("gtceu.multiblock.pattern.error.limited.range", minCount, maxCount));
        } else {
            // todo actual lang
            if (minCount > 0) {
                tooltips.add(Component.literal(Text.RED + "At least: " + Text.RESET + minCount));
                // tooltips.add(Component.translatable("gtceu.multiblock.pattern.error.limited.min_count", minCount,
                // ctx.getGlobalCount(this)));
            }
            if (maxCount != -1) {
                tooltips.add(Component.literal(Text.RED + "At most: " + Text.RESET + maxCount));
                // tooltips.add(Component.translatable("gtceu.multiblock.pattern.error.limited.max_count", maxCount,
                // ctx.getGlobalCount(this)));
            }
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
