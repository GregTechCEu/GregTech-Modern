package com.gregtechceu.gtceu.api.multiblock.predicates;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.api.multiblock.PredicateContext;
import com.gregtechceu.gtceu.api.multiblock.error.SinglePredicateError;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class BasePredicate {

    public static final BasePredicate AIR = of("Air", ctx -> ctx.state().isAir());

    public static final BasePredicate ANY = of("Any", ctx -> true);

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

    public MultiPredicate getParent() {
        return Objects.requireNonNull(this.parent);
    }

    /// the main testing method
    public abstract boolean test(PredicateContext ctx);

    /// test with internal function and global/slice max
    public boolean testLimited(PredicateContext ctx) {
        ctx.setStage(PredicateContext.PredicateStage.INTERNAL);
        if (!test(ctx)) return false;
        ctx.setStage(PredicateContext.PredicateStage.GLOBAL_MAX);
        if (!testGlobalMax(ctx)) return false;
        ctx.setStage(PredicateContext.PredicateStage.SLICE_MAX);
        return testSliceMax(ctx);
    }

    /// test against global max count
    public boolean testGlobalMax(PredicateContext ctx) {
        if (getMaxCount() == -1) return true;
        int count = ctx.incrementGlobalCount(this);
        return testGlobalMax(count) || ctx.error(SinglePredicateError.maxCount(this, count));
    }

    /// test against slice max count
    public boolean testSliceMax(PredicateContext ctx) {
        if (getMaxSliceCount() == -1 || ctx.layerCache() == null) return true;
        int count = ctx.incrementSliceCount(this);
        return testSliceMax(count) || ctx.error(SinglePredicateError.maxLayerCount(this, count));
    }

    /// test against global min count
    public boolean testGlobalMin(PredicateContext ctx) {
        if (getMinCount() == -1) return true;
        int count = ctx.getGlobalCount(this);
        return testGlobalMin(count) || ctx.error(SinglePredicateError.minCount(this, count));
    }

    /// test against slice min count
    public boolean testSliceMin(PredicateContext ctx) {
        if (getMinSliceCount() == -1 || ctx.layerCache() == null) return true;
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

    protected void appendStats(StringBuilder builder) {
        if (minCount != -1 && maxCount != -1) {
            builder.append("g[%d,%d] ".formatted(minCount, maxCount));
        }
        if (minSliceCount != -1 && maxSliceCount != -1) {
            builder.append("s[%d,%d] ".formatted(minSliceCount, maxSliceCount));
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getTypeName());
        builder.append(" ");
        appendStats(builder);
        builder.append('{');
        appendContents(builder);
        builder.append('}');
        return builder.toString();
    }

    public static MultiPredicate create(@Nullable String debugName, Predicate<PredicateContext> predicate) {
        return create(debugName, predicate, Stream.empty(), null);
    }

    // this uses stream for lazy initialization
    public static MultiPredicate create(@Nullable String debugName, Predicate<PredicateContext> predicate,
                                        Stream<BlockInfo> candidateStream, @Nullable Consumer<StringBuilder> contents) {
        return new MultiPredicate(of(debugName, predicate, candidateStream, contents));
    }

    public static BasePredicate of(@Nullable String debugName, Predicate<PredicateContext> predicate,
                            Stream<BlockInfo> candidateStream, @Nullable Consumer<StringBuilder> contents) {
        return new SinglePredicate(predicate, candidateStream, debugName, contents);
    }

    private static BasePredicate of(@Nullable String debugName, Predicate<PredicateContext> predicate) {
        return of(debugName, predicate, Stream.empty(), null);
    }
}
