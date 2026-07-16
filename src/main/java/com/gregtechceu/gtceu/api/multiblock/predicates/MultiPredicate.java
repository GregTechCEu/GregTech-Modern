package com.gregtechceu.gtceu.api.multiblock.predicates;

import com.gregtechceu.gtceu.api.multiblock.PredicateContext;
import com.gregtechceu.gtceu.api.multiblock.predicates.logic.AndLogic;
import com.gregtechceu.gtceu.api.multiblock.predicates.logic.BaseLogic;
import com.gregtechceu.gtceu.api.multiblock.predicates.logic.OrLogic;
import com.gregtechceu.gtceu.api.multiblock.predicates.logic.XorLogic;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class MultiPredicate implements Iterable<BasePredicate> {

    private static final Comparator<BasePredicate> PREDICATE_COMPARATOR = Comparator
            .comparingInt(BasePredicate::getPriority);

    private static final MultiPredicate EMPTY = new MultiPredicate(Logic.OR, false);

    public static final MultiPredicate AIR = new MultiPredicate(BasePredicate.AIR);

    public static final MultiPredicate ANY = new MultiPredicate(BasePredicate.ANY);

    private final List<BasePredicate> predicateList = new ObjectArrayList<>();
    @Getter
    private final BaseLogic logic;
    private final boolean hasAir;
    @Getter
    private boolean controller;

    MultiPredicate(@Nullable String debugName, Predicate<PredicateContext> predicate,
                   Stream<BlockInfo> candidateStream, @Nullable Consumer<StringBuilder> contents) {
        this(BasePredicate.of(debugName, predicate, candidateStream, contents));
    }

    MultiPredicate(BasePredicate predicate) {
        this(Logic.OR, predicate == BasePredicate.AIR);
        addPredicate(predicate);
    }

    private MultiPredicate(Logic type, boolean hasAir) {
        this.hasAir = hasAir;
        this.logic = type.createLogic(this);
    }

    public void reset() {
        this.logic.reset();
    }

    // this is called for each block
    /// delegates to {@link #logic} to run {@link BasePredicate#testLimited(PredicateContext)}
    public boolean test(PredicateContext ctx) {
        return this.logic.test(ctx);
    }

    /// delegates to {@link #logic} to test against global min count
    public boolean testGlobalMin(PredicateContext ctx) {
        return this.logic.testGlobalMin(ctx);
    }

    /// delegates to {@link #logic} to test against slice min count
    public boolean testSliceMin(PredicateContext ctx) {
        return this.logic.testSliceMin(ctx);
    }

    protected MultiPredicate addPredicates(Iterable<BasePredicate> predicates) {
        for (BasePredicate predicate : predicates) {
            addPredicate(predicate);
        }
        return this;
    }

    protected MultiPredicate addPredicate(BasePredicate predicate) {
        this.predicateList.add(predicate);
        predicate.setParent(this);
        this.logic.onPredicateAdd(predicate);
        return this;
    }

    public List<List<BlockInfo>> getCandidates() {
        return this.predicateList.stream()
                .map(BasePredicate::getCandidates)
                .toList();
    }

    public boolean isOr() {
        return this.logic instanceof OrLogic;
    }

    public boolean isAnd() {
        return this.logic instanceof AndLogic;
    }

    public boolean isXor() {
        return this.logic instanceof XorLogic;
    }

    @ApiStatus.Internal
    public boolean isEmpty() {
        return this == EMPTY;
    }

    public boolean isAny() {
        return this == ANY;
    }

    public boolean isAir() {
        return this == AIR;
    }

    public boolean hasAir() {
        return this.hasAir;
    }

    protected BasePredicate compact() {
        return new CompactedPredicate(this);
    }

    public MultiPredicate setMinGlobalLimited(int min) {
        return this.setMinCount(min);
    }

    public MultiPredicate setMinGlobalLimited(int min, int previewCount) {
        return this.setMinCount(min).setPreviewCount(previewCount);
    }

    public MultiPredicate setMinCount(int min) {
        this.forEach(p -> p.setMinCount(min));
        return this;
    }

    public MultiPredicate setMaxGlobalLimited(int max) {
        return this.setMaxCount(max);
    }

    public MultiPredicate setMaxGlobalLimited(int max, int previewCount) {
        return this.setMaxCount(max).setPreviewCount(previewCount);
    }

    public MultiPredicate setMaxCount(int max) {
        this.forEach(p -> p.setMaxCount(max));
        return this;
    }

    public MultiPredicate setGlobalMinMax(int min, int max) {
        return this.setMinCount(min).setMaxCount(max);
    }

    public MultiPredicate setMinLayerLimited(int min) {
        return this.setMinSliceCount(min);
    }

    public MultiPredicate setMinLayerLimited(int min, int previewCount) {
        return this.setMinSliceCount(min).setPreviewCount(previewCount);
    }

    public MultiPredicate setMinSliceCount(int min) {
        this.forEach(p -> p.setMinSliceCount(min));
        return this;
    }

    public MultiPredicate setMaxLayerLimited(int max) {
        return this.setMaxSliceCount(max);
    }

    public MultiPredicate setMaxLayerLimited(int max, int previewCount) {
        return this.setMaxSliceCount(max).setPreviewCount(previewCount);
    }

    public MultiPredicate setMaxSliceCount(int max) {
        this.forEach(p -> p.setMaxSliceCount(max));
        return this;
    }

    public MultiPredicate setPreviewCount(int previewCount) {
        this.forEach(p -> p.setPreviewCount(previewCount));
        return this;
    }

    public MultiPredicate setLayerMinMax(int min, int max) {
        return this.setMinSliceCount(min).setMaxSliceCount(max);
    }

    /**
     * Sets the Minimum and Maximum limit to the passed value
     *
     * @param limit The Maximum and Minimum limit
     */
    public MultiPredicate setExactLimit(int limit) {
        return this.setGlobalMinMax(limit, limit);
    }

    public MultiPredicate disabledRenderFormed() {
        return setDisableRenderFormed(true);
    }

    private MultiPredicate setDisableRenderFormed(boolean disable) {
        this.forEach(p -> p.setDisableRenderFormed(disable));
        return this;
    }

    protected MultiPredicate sorted() {
        this.predicateList.sort(PREDICATE_COMPARATOR);
        return this;
    }

    public MultiPredicate or(MultiPredicate other) {
        return combine(this, Logic.OR, other);
    }

    public MultiPredicate and(MultiPredicate other) {
        return combine(this, Logic.AND, other);
    }

    public MultiPredicate xor(MultiPredicate other) {
        return combine(this, Logic.XOR, other);
    }

    public boolean isSingle() {
        return predicateList.size() == 1;
    }

    protected void appendContents(StringBuilder builder) {
        StringJoiner joiner = new StringJoiner(", ");
        this.forEach(p -> joiner.add(p.toString()));
        builder.append(joiner);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("MulitPredicate");
        if (isController()) builder.append("[Controller]");
        builder.append('{');
        appendContents(builder);
        builder.append('}');
        return builder.toString();
    }

    @Override
    public Iterator<BasePredicate> iterator() {
        return this.predicateList.iterator();
    }

    public MultiPredicate setController(boolean controller) {
        this.controller = controller;
        return this;
    }

    public MultiPredicate setPriority(int priority) {
        this.forEach(p -> p.setPriority(priority));
        return this;
    }

    public static MultiPredicate empty() {
        return EMPTY;
    }

    /// @param a will have type set
    /// @param b may or may not be a multi predicate
    /// @return copy of {@code a} combined with {@code b}
    private static MultiPredicate combine(MultiPredicate a, Logic type, @Nullable MultiPredicate b) {
        if (b == null) return a; // no op
        if (a.isEmpty()) return b;
        var ret = new MultiPredicate(type, a.hasAir() || b.hasAir());
        appendPredicate(a, ret, type);
        appendPredicate(b, ret, type);
        return ret.sorted();
    }

    private static void appendPredicate(MultiPredicate source, MultiPredicate dest, Logic type) {
        if (source.isSingle()) {
            dest.addPredicate(source.predicateList.get(0));
        } else if (source.logic.getType() != type) {
            dest.addPredicate(source.compact());
        } else {
            dest.addPredicates(source);
        }
    }

    /*
     * OR logic
     * at least one of n predicates must be valid
     * AND logic
     * all predicates must be valid in the multiblock/slice
     * this deals with global/slice counts
     * AND_SLICE?
     * XOR logic
     * must only have one of n predicates be valid and present in multi
     * global/slice counts of other predicates must be zero
     *
     * when it comes to the internal predicates
     * any can pass, regardless of logic type
     * since a block can only have one state
     * and internal predicate is checked per block
     *
     * this may also introduce the ability
     * to have an invalid predicate form a multiblock
     * if predicate a has a min of two,
     * and predicate b has min of one,
     * and you use XOR logic
     * the multi predicate would pass since a fails and b succeeds min checks
     * really it should be "only one predicate can be present in multi/slice"
     *
     * how do i handle global/slice max?
     * i dont think i actually need to
     * what i really need to do is track passed vs failed predicates
     */
    public enum Logic {

        OR,
        AND,
        XOR;

        public BaseLogic createLogic(MultiPredicate source) {
            return switch (this) {
                case AND -> new AndLogic(source);
                case XOR -> new XorLogic(source);
                default -> new OrLogic(source);
            };
        }
    }

    public static class CompactedPredicate extends BasePredicate {

        private final MultiPredicate root;

        private CompactedPredicate(MultiPredicate root) {
            this.root = root;
        }

        public MultiPredicate expand() {
            return this.root;
        }

        @Override
        public boolean test(PredicateContext ctx) {
            // test parent predicates
            return expand().test(ctx);
        }

        @Override
        public boolean testLimited(PredicateContext ctx) {
            return this.test(ctx);
        }

        @Override
        public boolean testGlobalMin(PredicateContext ctx) {
            return expand().testGlobalMin(ctx);
        }

        @Override
        public boolean testSliceMin(PredicateContext ctx) {
            return expand().testSliceMin(ctx);
        }

        @Override
        public List<BlockInfo> computeCandidates() {
            return expand().getCandidates()
                    .stream()
                    .flatMap(Collection::stream)
                    .toList();
        }

        @Override
        public String getTypeName() {
            return "CompactedPredicate";
        }

        @Override
        protected void appendContents(StringBuilder builder) {
            expand().appendContents(builder);
        }

        @Override
        protected void appendStats(StringBuilder builder) {}
    }
}
