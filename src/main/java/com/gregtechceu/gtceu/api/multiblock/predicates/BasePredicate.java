package com.gregtechceu.gtceu.api.multiblock.predicates;

import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.api.multiblock.PredicateContext;
import com.gregtechceu.gtceu.api.multiblock.error.SinglePredicateError;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class BasePredicate implements Comparable<BasePredicate> {

    public static final BasePredicate AIR = new PredicateBuilder("Air")
            .predicate(ctx -> ctx.state().isAir())
            // todo error?
            .build();

    public static final BasePredicate ANY = new PredicateBuilder("Any")
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

    /// delegates to {@link MultiPredicate#testMaxCount(BasePredicate, PredicateContext)},
    /// with this predicate as the passing predicate
    public boolean checkMaxCount(PredicateContext context) {
        return getParent().testMaxCount(this, context);
    }

    /// test against global max count
    public boolean testGlobalMax(PredicateContext ctx) {
        if (getMaxCount() == -1) return true;
        ctx.setStage(PredicateContext.PredicateStage.GLOBAL_MAX);
        int count = ctx.incrementGlobalCount(this);
        if (testGlobalMax(count)) return true;
        ctx.appendError(SinglePredicateError.maxCount(this, count));
        return false;
    }

    /// test against slice max count
    public boolean testSliceMax(PredicateContext ctx) {
        if (!ctx.isCheckLayer()) return true;
        ctx.setStage(PredicateContext.PredicateStage.SLICE_MAX);
        int count = ctx.incrementSliceCount(this);
        if (getMaxSliceCount() == -1 || testSliceMax(count)) return true;
        ctx.appendError(SinglePredicateError.maxLayerCount(this, count));
        return false;
    }

    /// test against global min count
    public boolean testGlobalMin(PredicateContext ctx) {
        if (getMinCount() == -1) return true;
        int count = ctx.getGlobalCount(this);
        if (testGlobalMin(count)) return true;
        ctx.appendError(SinglePredicateError.minCount(this, count));
        return false;
    }

    /// test against slice min count
    public boolean testSliceMin(PredicateContext ctx) {
        if (getMinSliceCount() == -1 || !ctx.isCheckLayer()) return true;
        int count = ctx.getSliceCount(this);
        if (testSliceMin(count)) return true;
        ctx.appendError(SinglePredicateError.minLayerCount(this, count));
        return false;
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

    @Override
    public int compareTo(BasePredicate o) {
        return Integer.compare(this.priority, o.priority);
    }
}
