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
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Accessors(chain = true)
public abstract class BasePredicate {

    public static final BasePredicate AIR = create("Air", ctx -> ctx.state().is(Blocks.AIR));

    public static final BasePredicate ANY = create("Any", ctx -> true);

    protected static final Comparator<BasePredicate> PREDICATE_COMPARATOR = Comparator
            .comparingInt(BasePredicate::getPriority);

    private @Nullable List<BlockInfo> candidates;

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
    public boolean test(PredicateContext ctx) {
        return true;
    }

    /// test against global max count
    protected boolean testGlobalMax(PredicateContext ctx) {
        ctx.globalCache().mergeInt(this, 1, Integer::sum);
        if ((minCount == -1 && maxCount == -1) || ctx.layerCache() == null) return true;
        int count = ctx.globalCache().getInt(this);
        if (maxCount == -1 || count <= maxCount) return true;
        return ctx.error(SinglePredicateError.maxCount(this, count));
    }

    /// test against slice max count
    protected boolean testSliceMax(PredicateContext ctx) {
        if (ctx.layerCache() == null) return true;
        ctx.layerCache().mergeInt(this, 1, Integer::sum);
        if ((minSliceCount == -1 && maxSliceCount == -1)) return true;
        int count = ctx.layerCache().getInt(this);
        if (maxSliceCount == -1 || count <= maxSliceCount) return true;
        return ctx.error(SinglePredicateError.maxLayerCount(this, count));
    }

    /// method called after all blocks have been iterated, used for checking min values
    public boolean postTest(PredicateContext ctx) {
        return true;
    }

    /// test against global max count
    protected boolean testGlobalMin(PredicateContext ctx) {
        ctx.globalCache().mergeInt(this, 1, Integer::sum);
        if ((minCount == -1 && maxCount == -1) || ctx.layerCache() == null) return true;
        int count = ctx.globalCache().getInt(this);
        if (maxCount == -1 || count <= maxCount) return true;
        return ctx.error(SinglePredicateError.maxCount(this, count));
    }

    /// test against slice max count
    protected boolean testSliceMin(PredicateContext ctx) {
        if (ctx.layerCache() == null) return true;
        ctx.layerCache().mergeInt(this, 1, Integer::sum);
        if ((minSliceCount == -1 && maxSliceCount == -1)) return true;
        int count = ctx.layerCache().getInt(this);
        if (maxSliceCount == -1 || count <= maxSliceCount) return true;
        return ctx.error(SinglePredicateError.maxLayerCount(this, count));
    }

    /// returns the internal predicate
    public abstract Predicate<PredicateContext> getPredicate();

    /// computes the candidates for this predicate
    public List<BlockInfo> computeCandidates() {
        return Collections.emptyList();
    }

    /// @return the candidate blocks for this predicate, may be empty, lazily initialized
    public List<BlockInfo> getCandidates() {
        if (candidates == null) {
            candidates = computeCandidates();
        }
        return candidates;
    }

    /// @return a list of candidates from a predicate at {@code index}
    public List<BlockInfo> getCandidates(int index) {
        return getCandidates();
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

    public boolean isAir() {
        return this == AIR;
    }

    public boolean isAny() {
        return this == ANY;
    }

    /// used for tooltips
    ///
    /// @return true if this or any sub predicates is air
    public boolean hasAir() {
        return isAir();
    }

    /// used for tooltips
    public boolean isSingle() {
        return true;
    }

    public boolean isController() {
        return false;
    }

    public BasePredicate setMinGlobalLimited(int min) {
        return this.setMinCount(min);
    }

    public BasePredicate setMinGlobalLimited(int min, int previewCount) {
        return this.setMinCount(min).setPreviewCount(previewCount);
    }

    public BasePredicate setMaxGlobalLimited(int max) {
        return this.setMaxCount(max);
    }

    public BasePredicate setMaxGlobalLimited(int max, int previewCount) {
        return this.setMaxCount(max).setPreviewCount(previewCount);
    }

    public BasePredicate setGlobalMinMax(int min, int max) {
        return this.setMinCount(min).setMaxCount(max);
    }

    public BasePredicate setMinLayerLimited(int min) {
        return this.setMinSliceCount(min);
    }

    public BasePredicate setMinLayerLimited(int min, int previewCount) {
        return this.setMinSliceCount(min).setPreviewCount(previewCount);
    }

    public BasePredicate setMaxLayerLimited(int max) {
        return this.setMaxSliceCount(max);
    }

    public BasePredicate setMaxLayerLimited(int max, int previewCount) {
        return this.setMaxSliceCount(max).setPreviewCount(previewCount);
    }

    public BasePredicate setLayerMinMax(int min, int max) {
        return this.setMinSliceCount(min).setMaxSliceCount(max);
    }

    /**
     * Sets the Minimum and Maximum limit to the passed value
     *
     * @param limit The Maximum and Minimum limit
     */
    public BasePredicate setExactLimit(int limit) {
        return this.setMinCount(limit).setMaxCount(limit);
    }

    public BasePredicate disabledRenderFormed() {
        return setDisableRenderFormed(true);
    }

    /// visits every predicate
    public void visit(Consumer<BasePredicate> visitor) {
        visitor.accept(this);
    }

    /// @return a flat list of all predicates
    public final List<BasePredicate> expand() {
        List<BasePredicate> result = new ArrayList<>();
        visit(result::add);
        if (result.size() > 1) result.sort(PREDICATE_COMPARATOR);
        return Collections.unmodifiableList(result);
    }

    protected boolean isSimplified() {
        return false;
    }

    /// simplify this predicate to just the internal predicate
    public final BasePredicate simplify() {
        if (isSimplified()) return this;
        return simplify(getTypeName(), getPredicate(), getCandidates(), this::appendContents);
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

    // === instance methods ===
    public BasePredicate or(BasePredicate other) {
        return or(this, other);
    }

    public BasePredicate and(BasePredicate other) {
        return and(this, other);
    }

    public BasePredicate xor(BasePredicate other) {
        return xor(this, other);
    }

    // === private static helpers ===
    private static BasePredicate or(BasePredicate a, BasePredicate b) {
        return or(null, List.of(a, b));
    }

    private static BasePredicate and(BasePredicate a, BasePredicate b) {
        return and(null, List.of(a, b));
    }

    private static BasePredicate xor(BasePredicate a, BasePredicate b) {
        return xor(null, List.of(a, b));
    }

    // === public static helpers ===
    public static BasePredicate or(@Nullable String debugName, Iterable<BasePredicate> predicates) {
        return new MultiPredicate(debugName, predicates, MultiPredicate.Logic.OR);
    }

    public static BasePredicate and(@Nullable String debugName, Iterable<BasePredicate> predicates) {
        return new MultiPredicate(debugName, predicates, MultiPredicate.Logic.AND);
    }

    public static BasePredicate xor(@Nullable String debugName, Iterable<BasePredicate> predicates) {
        return new MultiPredicate(debugName, predicates, MultiPredicate.Logic.XOR);
    }

    public static BasePredicate create(@Nullable String debugName, Predicate<PredicateContext> predicate) {
        return create(debugName, predicate, Stream.empty(), null);
    }

    private static BasePredicate simplify(String debugName, Predicate<PredicateContext> predicate,
                                          List<BlockInfo> candidateList, Consumer<StringBuilder> contents) {
        return new BasePredicate() {

            @Override
            public String getTypeName() {
                return debugName + "(Simplified)";
            }

            @Override
            public boolean test(PredicateContext ctx) {
                return predicate.test(ctx);
            }

            @Override
            public List<BlockInfo> computeCandidates() {
                return candidateList;
            }

            @Override
            public Predicate<PredicateContext> getPredicate() {
                return predicate;
            }

            @Override
            protected void appendContents(StringBuilder builder) {
                contents.accept(builder);
            }

            @Override
            protected boolean isSimplified() {
                return true;
            }
        };
    }

    // this uses stream for lazy initialization
    public static BasePredicate create(@Nullable String debugName, Predicate<PredicateContext> predicate,
                                       Stream<BlockInfo> candidateStream, @Nullable Consumer<StringBuilder> contents) {
        return new BasePredicate() {

            @Override
            public boolean test(PredicateContext ctx) {
                return predicate.test(ctx) && testGlobalMax(ctx) && testSliceMax(ctx);
            }

            @Override
            public Predicate<PredicateContext> getPredicate() {
                return predicate;
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
    }
}
