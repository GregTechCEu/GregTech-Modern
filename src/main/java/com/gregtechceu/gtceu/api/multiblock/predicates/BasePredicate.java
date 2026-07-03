package com.gregtechceu.gtceu.api.multiblock.predicates;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.multiblock.PredicateContext;
import com.gregtechceu.gtceu.api.multiblock.error.SinglePredicateError;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class BasePredicate {

    public static final MultiPredicate AIR = create("Air", ctx -> ctx.state().is(Blocks.AIR));

    public static final MultiPredicate ANY = create("Any", ctx -> true);

    protected static final Comparator<BasePredicate> PREDICATE_COMPARATOR = Comparator
            .comparingInt(BasePredicate::getPriority);

    private @Nullable List<BlockInfo> candidates;

    @Getter
    @Setter
    private boolean isController = false;
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

    /// the main testing method
    public abstract boolean test(PredicateContext ctx);

    /// test with internal function and global/slice max
    public boolean testLimited(PredicateContext ctx) {
        return test(ctx) && testGlobalMax(ctx) && testSliceMax(ctx);
    }

    /// test against global max count
    public boolean testGlobalMax(PredicateContext ctx) {
        if (skipGlobalTest()) return true;
        int count = ctx.incrementGlobalCount(this);
        if (maxCount == -1 || count <= maxCount) return true;
        return ctx.error(SinglePredicateError.maxCount(this, count));
    }

    /// test against slice max count
    public boolean testSliceMax(PredicateContext ctx) {
        if (skipSliceTest() || ctx.layerCache() == null) return true;
        int count = ctx.incrementSliceCount(this);
        if (maxSliceCount == -1 || count <= maxSliceCount) return true;
        return ctx.error(SinglePredicateError.maxLayerCount(this, count));
    }

    /// test against global max count
    public boolean testGlobalMin(PredicateContext ctx) {
        if (skipGlobalTest()) return true;
        int count = ctx.globalCache().getInt(this);
        if (maxCount == -1 || count <= maxCount) return true;
        return ctx.error(SinglePredicateError.minCount(this, count));
    }

    /// test against slice max count
    public boolean testSliceMin(PredicateContext ctx) {
        if (skipSliceTest() || ctx.layerCache() == null) return true;
        int count = ctx.layerCache().getInt(this);
        if (maxSliceCount == -1 || count <= maxSliceCount) return true;
        return ctx.error(SinglePredicateError.minLayerCount(this, count));
    }

    /// test against global max count
    public boolean testGlobalMin(int count) {
        return minCount == -1 || count >= minCount;
    }

    /// test against slice max count
    public boolean testSliceMin(int count) {
        return minSliceCount == -1 || count >= minSliceCount;
    }

    public boolean skipGlobalTest() {
        return minCount == -1 && maxCount == -1;
    }

    public boolean skipSliceTest() {
        return minSliceCount == -1 && maxSliceCount == -1;
    }

    /// computes the candidates for this predicate
    public abstract List<BlockInfo> computeCandidates();

    /// @return the candidate blocks for this predicate, may be empty, lazily initialized
    public List<BlockInfo> getCandidates() {
        if (candidates == null) {
            candidates = computeCandidates();
        }
        return candidates;
    }

    public List<ItemStack> getCandidateStacks() {
        return getCandidates().stream()
                .filter(BlockInfo::nonAir)
                .map(info -> {
                    if (GTCEu.isClientSide()) {
                        Level level = Objects.requireNonNull(Minecraft.getInstance().level);
                        return info.getItemStackForm(level, BlockPos.ZERO);
                    }

                    return info.getItemStackForm();
                })
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

    public static MultiPredicate create(@Nullable String debugName, Predicate<PredicateContext> predicate) {
        return create(debugName, predicate, Stream.empty(), null);
    }

    // this uses stream for lazy initialization
    public static MultiPredicate create(@Nullable String debugName, Predicate<PredicateContext> predicate,
                                        Stream<BlockInfo> candidateStream, @Nullable Consumer<StringBuilder> contents) {
        BasePredicate basePredicate = new BasePredicate() {

            @Override
            public boolean test(PredicateContext ctx) {
                return predicate.test(ctx);
            }

            @Override
            public List<BlockInfo> computeCandidates() {
                return candidateStream.toList();
            }

            @Override
            public String getTypeName() {
                return Objects.requireNonNullElse(debugName, "Predicate");
            }

            @Override
            protected void appendContents(StringBuilder builder) {
                if (contents != null) {
                    contents.accept(builder);
                }
            }
        };
        return new MultiPredicate(basePredicate);
    }
}
