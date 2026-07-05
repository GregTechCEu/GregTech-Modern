package com.gregtechceu.gtceu.api.multiblock.predicates;

import com.gregtechceu.gtceu.api.multiblock.PredicateContext;
import com.gregtechceu.gtceu.api.multiblock.PredicateContext.FailureReason;
import com.gregtechceu.gtceu.api.multiblock.error.PatternStringError;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.AccessLevel;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class MultiPredicate implements Iterable<BasePredicate> {

    private static final Comparator<BasePredicate> PREDICATE_COMPARATOR = Comparator
            .comparingInt(BasePredicate::getPriority);
    private static final MultiPredicate EMPTY = new MultiPredicate();

    private final List<BasePredicate> predicateList = new ObjectArrayList<>();
    @Getter(AccessLevel.PROTECTED)
    private final Logic type;
    private final boolean hasAir;
    @Getter
    private boolean controller;

    MultiPredicate(@Nullable String debugName, Predicate<PredicateContext> predicate,
                   Stream<BlockInfo> candidateStream, @Nullable Consumer<StringBuilder> contents) {
        this(new BasePredicate() {

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
        });
    }

    private MultiPredicate(Logic type, boolean hasAir) {
        this.type = type;
        this.hasAir = hasAir;
    }

    private MultiPredicate(BasePredicate singleton) {
        this();
        addPredicate(singleton);
    }

    private MultiPredicate() {
        this.type = Logic.SINGLE;
        this.hasAir = false;
    }

    @ApiStatus.Internal
    public boolean isEmpty() {
        return this == EMPTY;
    }

    /// delegates to {@link #type} to run {@link BasePredicate#testLimited(PredicateContext)}
    public boolean test(PredicateContext ctx) {
        return getType().run(ctx, this);
    }

    /// delegates to {@link #type} to test against global min count
    public boolean testGlobalMin(PredicateContext ctx) {
        return getType().testGlobalMin(ctx, this);
    }

    /// delegates to {@link #type} to test against slice min count
    public boolean testSliceMin(PredicateContext ctx) {
        return getType().testSliceMin(ctx, this);
    }

    protected MultiPredicate addPredicates(Iterable<BasePredicate> predicates) {
        predicates.forEach(this::addPredicate);
        return this;
    }

    protected MultiPredicate addPredicate(BasePredicate predicate) {
        this.predicateList.add(predicate);
        return this;
    }

    public List<List<BlockInfo>> getCandidates() {
        return this.predicateList.stream()
                .map(BasePredicate::getCandidates)
                .toList();
    }

    public boolean isOr() {
        return this.type == Logic.OR;
    }

    public boolean isAnd() {
        return this.type == Logic.AND;
    }

    public boolean isXor() {
        return this.type == Logic.XOR;
    }

    protected boolean sameType(MultiPredicate other) {
        return this.type == other.type;
    }

    public boolean isAny() {
        return this == BasePredicate.ANY;
    }

    public boolean isAir() {
        return this == BasePredicate.AIR;
    }

    public boolean hasAir() {
        return this.isAir() || this.hasAir;
    }

    protected BasePredicate compact() {
        return new BasePredicate() {

            private MultiPredicate getParent() {
                return MultiPredicate.this;
            }

            @Override
            public boolean test(PredicateContext ctx) {
                // this also tests global/slice max, but with no respect to logic type
                return getParent().test(ctx);
            }

            @Override
            public boolean testGlobalMin(PredicateContext ctx) {
                return getParent().testGlobalMin(ctx);
            }

            @Override
            public boolean testSliceMin(PredicateContext ctx) {
                return getParent().testSliceMin(ctx);
            }

            @Override
            public List<BlockInfo> computeCandidates() {
                return getParent().getCandidates()
                        .stream()
                        .flatMap(Collection::stream)
                        .toList();
            }

            @Override
            public String getTypeName() {
                return getParent().getTypeName();
            }

            @Override
            protected void appendContents(StringBuilder builder) {
                getParent().appendContents(builder);
            }

            @Override
            protected void appendStats(StringBuilder builder) {}
        };
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
        return Logic.OR.combine(this, other);
    }

    public MultiPredicate and(MultiPredicate other) {
        return Logic.AND.combine(this, other);
    }

    public MultiPredicate xor(MultiPredicate other) {
        return Logic.XOR.combine(this, other);
    }

    public boolean isSingle() {
        return getType() == Logic.SINGLE;
    }

    public String getTypeName() {
        return String.valueOf(this.type);
    }

    protected void appendContents(StringBuilder builder) {
        StringJoiner joiner = new StringJoiner(", ");
        this.forEach(p -> joiner.add(p.toString()));
        builder.append(joiner);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getTypeName());
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

    protected enum Logic {

        SINGLE, OR, AND, XOR;

        /// @param a will have type set
        /// @param b may or may not be a multi predicate
        /// @return copy of {@code a} combined with {@code b}
        protected MultiPredicate combine(MultiPredicate a, @Nullable MultiPredicate b) {
            if (b == null) return a; // no op
            if (a.isEmpty()) return b;
            var ret = new MultiPredicate(this, a.hasAir() || b.hasAir());
            appendPredicate(a, ret);
            appendPredicate(b, ret);
            return ret.sorted();
        }

        private void appendPredicate(MultiPredicate source, MultiPredicate dest) {
            if (source.isSingle()) {
                dest.addPredicate(source.predicateList.get(0));
            } else if (source.type != this) {
                dest.addPredicate(source.compact());
            } else {
                dest.addPredicates(source);
            }
        }

        /*
         * OR logic
         *   at least one of n predicates must be valid
         * AND logic
         *   must have n valid predicates in multi
         * XOR logic
         *   must only have one of n predicates be valid and present in multi
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
         */
        protected boolean run(PredicateContext ctx, MultiPredicate predicates) {
            for (BasePredicate basePredicate : predicates) {
                if (basePredicate.testLimited(ctx)) return true;
            }
            return false;
        }

        public boolean testGlobalMin(PredicateContext ctx, MultiPredicate predicates) {
            return test(ctx, predicates, Tester.GLOBAL_MIN);
        }

        public boolean testSliceMin(PredicateContext ctx, MultiPredicate predicates) {
            return test(ctx, predicates, Tester.SLICE_MIN);
        }

        // i hate this (but not as much)
        private boolean test(PredicateContext ctx, MultiPredicate predicates,
                             Tester tester) {
            if (tester.shouldSkipTest(ctx)) return true;
            int skipped = 0, passed = 0, size = predicates.predicateList.size();
            for (BasePredicate predicate : predicates) {
                // get min count
                int expectedCount = tester.getCount(predicate);
                if (expectedCount == -1) {
                    if (++skipped == size) {
                        return true;
                    } else {
                        continue;
                    }
                }
                int actualCount = tester.getActualCount(ctx, predicate);

                boolean success = tester.test(expectedCount, actualCount);
                if (success) passed++;

                if (tester.returnEarly(passed, success, this)) {
                    // get true or false
                    return tester.hasErrored(passed, size, ctx, predicates, this);
                }
            }
            if (tester.finalReturn(passed, skipped, this)) {
                tester.onError(ctx, predicates);
                return false;
            }
            return true;
        }
    }

    enum Tester {
        GLOBAL_MIN,
        GLOBAL_MAX,
        SLICE_MIN,
        SLICE_MAX;

        public int getCount(BasePredicate predicate) {
            return switch (this) {
                case GLOBAL_MIN -> predicate.getMinCount();
                case GLOBAL_MAX -> predicate.getMaxCount();
                case SLICE_MIN -> predicate.getMinSliceCount();
                case SLICE_MAX -> predicate.getMaxSliceCount();
            };
        }

        public int getActualCount(PredicateContext ctx, BasePredicate predicate) {
            return switch (this) {
                case GLOBAL_MIN -> ctx.getGlobalCount(predicate);
                case GLOBAL_MAX -> ctx.incrementGlobalCount(predicate);
                case SLICE_MIN -> ctx.getSliceCount(predicate);
                case SLICE_MAX -> ctx.incrementSliceCount(predicate);
            };
        }

        public boolean test(int expected, int actual) {
            return switch (this) {
                case GLOBAL_MIN, SLICE_MIN -> expected <= actual;
                case GLOBAL_MAX, SLICE_MAX -> expected >= actual;
            };
        }

        public void onError(PredicateContext ctx, MultiPredicate predicates) {
            // todo better error
            var joiner = new StringJoiner("\n");
            predicates.forEach(p -> joiner.add("\t" + p));
            ctx.appendError(new PatternStringError(Component.literal("One of: \n" + joiner)));
            switch (this) {
                case GLOBAL_MIN -> ctx.setFailureReason(FailureReason.GLOBAL_MIN);
                case GLOBAL_MAX -> ctx.setFailureReason(FailureReason.GLOBAL_MAX);
                case SLICE_MIN -> ctx.setFailureReason(FailureReason.SLICE_MIN);
                case SLICE_MAX -> ctx.setFailureReason(FailureReason.SLICE_MAX);
            }
        }

        public boolean returnEarly(int passed, boolean success, Logic logicType) {
            // return true to return early
            return switch (logicType) {
                case XOR -> passed > 1;
                case AND -> !success;
                default -> false;
            };
        }

        public boolean finalReturn(int passed, int size, Logic logicType) {
            // return true if failed
            return switch (logicType) {
                case SINGLE, XOR -> passed != 1;
                case OR -> passed == 0;
                case AND -> passed != size;
            };
        }

        public boolean isGlobal() {
            return this == GLOBAL_MIN || this == GLOBAL_MAX;
        }

        public boolean isSlice() {
            return this == SLICE_MIN || this == SLICE_MAX;
        }

        public boolean isMin() {
            return this == GLOBAL_MIN || this == SLICE_MIN;
        }

        public boolean isMax() {
            return this == GLOBAL_MAX || this == SLICE_MAX;
        }

        public boolean shouldSkipTest(PredicateContext ctx) {
            // skip test if we're slice and slice cache is null
            return isSlice() && ctx.layerCache() != null;
        }

        // called after return early
        public boolean hasErrored(int passed, int size, PredicateContext ctx, MultiPredicate predicates, Logic logic) {
            boolean error = finalReturn(passed, size, logic);
            if (error) {
                onError(ctx, predicates);
            }
            return error;
        }
    }
}
