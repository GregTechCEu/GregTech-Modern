package com.gregtechceu.gtceu.api.multiblock.predicates;

import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.api.multiblock.PredicateContext;
import com.gregtechceu.gtceu.api.multiblock.error.PatternStringError;
import com.gregtechceu.gtceu.api.multiblock.error.PlaceholderError;
import com.gregtechceu.gtceu.api.multiblock.error.SinglePredicateError;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class BasePredicate {

    public static final BasePredicate AIR = new Builder("Air")
            .predicate(ctx -> ctx.state().isAir())
            // todo error?
            .build();

    public static final BasePredicate ANY = new Builder("Any")
            .predicate(ctx -> true)
            .build();

    @Getter(lazy = true)
    private final List<BlockInfo> candidates = computeCandidates();

    @Getter
    @Setter
    protected int priority = 0;
    @Getter
    @Setter
    protected int minCount = -1;
    @Getter
    @Setter
    protected int maxCount = -1;
    @Getter
    @Setter
    protected int minSliceCount = -1;
    @Getter
    @Setter
    protected int maxSliceCount = -1;
    @Getter
    @Setter
    protected int previewCount = -1;
    @Getter
    @Setter
    protected boolean disableRenderFormed = false;
    @Getter
    @Setter
    private @Nullable String nbtParser; // unsure what this does
    @Setter
    private @Nullable MultiPredicate parent;

    @Getter
    private final List<Component> additionalTooltips = new ArrayList<>();

    public MultiPredicate getParent() {
        return Objects.requireNonNull(this.parent);
    }

    /// the main testing method
    public abstract boolean test(PredicateContext ctx);

    public abstract void onError(PredicateContext ctx);

    public void addTooltips(Component tooltip) {
        this.additionalTooltips.add(tooltip);
    }

    public boolean checkMaxCount(PredicateContext context) {
        return getParent().getLogic().testMaxCount(this, context);
    }

    /// test against global max count
    public boolean testGlobalMax(PredicateContext ctx) {
        if (getMaxCount() == -1) return true;
        ctx.setStage(PredicateContext.PredicateStage.GLOBAL_MAX);
        int count = ctx.incrementGlobalCount(this);
        return testGlobalMax(count) || ctx.error(SinglePredicateError.maxCount(this, count));
    }

    /// test against slice max count
    public boolean testSliceMax(PredicateContext ctx) {
        if (!ctx.isCheckLayer()) return true;
        ctx.setStage(PredicateContext.PredicateStage.SLICE_MAX);
        int count = ctx.incrementSliceCount(this);
        return getMaxSliceCount() == -1 || testSliceMax(count) ||
                ctx.error(SinglePredicateError.maxLayerCount(this, count));
    }

    /// test against global min count
    public boolean testGlobalMin(PredicateContext ctx) {
        if (getMinCount() == -1) return true;
        int count = ctx.getGlobalCount(this);
        return testGlobalMin(count) || ctx.error(SinglePredicateError.minCount(this, count));
    }

    /// test against slice min count
    public boolean testSliceMin(PredicateContext ctx) {
        if (getMinSliceCount() == -1 || !ctx.isCheckLayer()) return true;
        int count = ctx.getSliceCount(this);
        return testSliceMin(count) || ctx.error(SinglePredicateError.minLayerCount(this, count));
    }

    /// simple test against global min count
    public boolean testGlobalMin(int count) {
        return minCount == -1 || count >= minCount;
    }

    /// simple test against slice min count
    public boolean testSliceMin(int count) {
        return minSliceCount == -1 || count >= minSliceCount;
    }

    /// simple test against global max count
    public boolean testGlobalMax(int count) {
        return maxCount == -1 || count <= maxCount;
    }

    /// simple test against slice max count
    public boolean testSliceMax(int count) {
        return maxSliceCount == -1 || count <= maxSliceCount;
    }

    /// computes the candidates for this predicate
    public abstract List<BlockInfo> computeCandidates();

    public List<ItemStack> getCandidateStacks() {
        return getCandidates().stream()
                .filter(BlockInfo::nonAir)
                .map(BlockInfo::getItemStackForm)
                .toList();
    }

    public Optional<BlockInfo> getFirstCandidate() {
        return Optional.of(getCandidates())
                .filter(c -> !c.isEmpty())
                .map(c -> c.get(0));
    }

    /// the type of this predicate
    public abstract String getTypeName();

    /// the contents of this predicate
    protected void appendContents(StringBuilder builder) {}

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getTypeName());
        builder.append('{');
        appendContents(builder);
        builder.append('}');
        return builder.toString();
    }

    @Accessors(chain = true, fluent = true)
    public static class Builder {

        private final String name;
        @Setter
        private Predicate<PredicateContext> predicate;
        @Setter
        private Stream<BlockInfo> candidates = Stream.empty();
        @Setter
        private @Nullable Consumer<StringBuilder> contents;
        @Setter
        private @Nullable Consumer<PredicateContext> onError;

        public Builder(@Nullable String debugName) {
            this.name = debugName != null ? debugName : "Predicate";
        }

        /// fills candidates and sets string contents with this block tag
        public Builder blockTag(TagKey<Block> tag) {
            this.candidates = Objects.requireNonNull(ForgeRegistries.BLOCKS.tags())
                    .getTag(tag).stream().map(BlockInfo::fromBlock);
            this.contents = builder -> builder.append(tag.location());
            return this;
        }

        /// fills candidates and sets string contents with this fluid tag
        public Builder fluidTag(TagKey<Fluid> tag) {
            this.candidates = Objects.requireNonNull(ForgeRegistries.FLUIDS.tags())
                    .getTag(tag).stream().map(BlockInfo::fromFluid);
            this.contents = builder -> builder.append(tag.location());
            return this;
        }

        public MultiPredicate toMultiPredicate() {
            return new MultiPredicate(build());
        }

        public BasePredicate build() {
            final Predicate<PredicateContext> finalPredicate = Objects.requireNonNull(predicate);
            return new BasePredicate() {

                @Override
                public void onError(PredicateContext ctx) {
                    if (onError != null) {
                        onError.accept(ctx);
                    } else {
                        placeholderError(ctx);
                    }
                    this.getAdditionalTooltips().forEach(c -> ctx.appendError(PatternStringError.component(c)));
                }

                private void placeholderError(PredicateContext ctx) {
                    ctx.appendError(new PlaceholderError(ctx.pos(), List.of(getCandidates())));
                }

                @Override
                public boolean test(PredicateContext ctx) {
                    return finalPredicate.test(ctx);
                }

                @Override
                public List<BlockInfo> computeCandidates() {
                    return Objects.requireNonNull(candidates).toList();
                }

                @Override
                public String getTypeName() {
                    return name;
                }

                @Override
                protected void appendContents(StringBuilder builder) {
                    if (contents != null) {
                        contents.accept(builder);
                    }
                }
            };
        }
    }
}
