package com.gregtechceu.gtceu.api.multiblock.predicates;

import com.gregtechceu.gtceu.api.multiblock.PredicateContext;
import com.gregtechceu.gtceu.api.multiblock.error.SinglePredicateError;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MultiPredicate implements Iterable<BasePredicate> {

    private static final String UNINITIALIZED = "__UNINITIALIZED__";
    private final List<BasePredicate> predicateList = new ObjectArrayList<>();
    private @Nullable final String debugName;
    private @Nullable final Logic type;
    private final boolean hasAir;
    @Getter
    private boolean controller;

    public MultiPredicate() {
        this.debugName = UNINITIALIZED;
        this.type = null;
        this.hasAir = false;
    }

    private MultiPredicate(@Nullable String debugName, Logic type, boolean hasAir) {
        this.debugName = debugName;
        this.type = type;
        this.hasAir = hasAir;
    }

    protected MultiPredicate(BasePredicate singleton) {
        this.debugName = null;
        this.type = null;
        this.hasAir = false;
        addPredicate(singleton);
    }

    @ApiStatus.Internal
    public boolean isUninitialized() {
        return UNINITIALIZED.equals(debugName);
    }

    public boolean test(PredicateContext ctx) {
        return getType().run(ctx, this);
    }

    /// test against global min count
    public boolean testGlobalMin(PredicateContext ctx) {
        return getType().testGlobalMin(ctx, this);
    }

    /// test against slice min count
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

    public boolean hasType() {
        return this.type != null;
    }

    public boolean isValid() {
        return isSingle() || hasType();
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

            @Override
            public boolean test(PredicateContext ctx) {
                return getType().run(ctx, MultiPredicate.this);
            }

            @Override
            public boolean testGlobalMin(PredicateContext ctx) {
                return getType().testGlobalMin(ctx, MultiPredicate.this);
            }

            @Override
            public boolean testSliceMin(PredicateContext ctx) {
                return getType().testSliceMin(ctx, MultiPredicate.this);
            }

            @Override
            public List<BlockInfo> computeCandidates() {
                return MultiPredicate.this.getCandidates()
                        .stream()
                        .flatMap(Collection::stream)
                        .toList();
            }

            @Override
            public String getTypeName() {
                return MultiPredicate.this.getTypeName();
            }

            @Override
            protected void appendContents(StringBuilder builder) {
                MultiPredicate.this.appendContents(builder);
            }
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
        return this.setMinCount(limit).setMaxCount(limit);
    }

    public MultiPredicate disabledRenderFormed() {
        return setDisableRenderFormed(true);
    }

    private MultiPredicate setDisableRenderFormed(boolean disable) {
        this.forEach(p -> p.setDisableRenderFormed(disable));
        return this;
    }

    protected MultiPredicate sorted() {
        this.predicateList.sort(BasePredicate.PREDICATE_COMPARATOR);
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
        return this.predicateList.size() == 1;
    }

    public String getTypeName() {
        if (isSingle()) return "SINGLETON";
        StringBuilder builder = new StringBuilder().append(this.type);
        if (this.debugName != null) builder.append("#").append(this.debugName);
        return builder.toString();
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

    protected Logic getType() {
        if (isSingle()) return Logic.OR;
        return Objects.requireNonNull(this.type, "type == null");
    }

    public MultiPredicate setController(boolean controller) {
        this.controller = controller;
        return this;
    }

    public MultiPredicate setPriority(int priority) {
        this.forEach(p -> p.setPriority(priority));
        return this;
    }

    protected enum Logic {

        OR,
        AND {

            @Override
            protected boolean run(PredicateContext ctx, MultiPredicate predicates) {
                return !super.run(ctx, predicates);
            }

            @Override
            public boolean testGlobalMin(PredicateContext ctx, MultiPredicate predicates) {
                return !super.testGlobalMin(ctx, predicates);
            }

            @Override
            public boolean testSliceMin(PredicateContext ctx, MultiPredicate predicates) {
                return !super.testSliceMin(ctx, predicates);
            }

            // override test slice/global min?
        },
        XOR {

            @Override
            protected boolean run(PredicateContext ctx, MultiPredicate predicates) {
                int passed = 0;
                for (BasePredicate basePredicate : predicates) {
                    if (basePredicate.testLimited(ctx)) {
                        if (++passed > 1) return false;
                    }
                }
                return passed == 1;
            }

            @Override
            public boolean testGlobalMin(PredicateContext ctx, MultiPredicate predicates) {
                int skipped = 0;
                int passed = 0;
                for (BasePredicate predicate : predicates) {
                    if (predicate.getMinCount() == -1) {
                        skipped++;
                        continue;
                    }
                    int count = ctx.globalCache().getInt(predicate);
                    if (predicate.testGlobalMin(count)) {
                        if (++passed > 1) {
                            // TODO special xor error
                            return ctx.error(SinglePredicateError.minCount(predicate, count));
                        }
                    } else {
                        return ctx.error(SinglePredicateError.minCount(predicate, count));
                    }
                }
                if (skipped == predicates.predicateList.size()) return true;
                return passed == 1;
            }

            @Override
            public boolean testSliceMin(PredicateContext ctx, MultiPredicate predicates) {
                if (ctx.layerCache() == null) return true;
                int skipped = 0;
                int passed = 0;
                for (BasePredicate predicate : predicates) {
                    if (predicate.getMinCount() == -1) {
                        skipped++;
                        continue;
                    }
                    int count = ctx.layerCache().getInt(predicate);
                    if (predicate.testGlobalMin(count)) {
                        if (++passed > 1) {
                            // TODO special xor error
                            return ctx.error(SinglePredicateError.minCount(predicate, count));
                        }
                    } else {
                        return ctx.error(SinglePredicateError.minCount(predicate, count));
                    }
                }
                if (skipped == predicates.predicateList.size()) return true;
                return passed == 1;
            }
        };

        /// @param a will have type set
        /// @param b may or may not be a multi predicate
        /// @return copy of {@code a} with {@code b} added
        protected MultiPredicate combine(MultiPredicate a, @Nullable MultiPredicate b) {
            if (b == null) return a; // b really should not be null in the first place
            if (a.isUninitialized()) return b;
            var ret = new MultiPredicate("Multi", this, a.hasAir() || b.hasAir());
            appendPredicate(a, ret);
            appendPredicate(b, ret);
            return ret.sorted();
        }

        private void appendPredicate(MultiPredicate source, MultiPredicate dest) {
            if (source.type == null && source.isSingle()) {
                dest.addPredicate(source.predicateList.get(0));
            } else if (source.type != this) {
                dest.addPredicate(source.compact());
            } else {
                dest.addPredicates(source);
            }
        }

        protected boolean run(PredicateContext ctx, MultiPredicate predicates) {
            for (BasePredicate basePredicate : predicates) {
                if (basePredicate.testLimited(ctx)) return true;
            }
            return false;
        }

        public boolean testGlobalMin(PredicateContext ctx, MultiPredicate predicates) {
            for (BasePredicate predicate : predicates) {
                if (predicate.testGlobalMin(ctx)) return true;
            }
            return false;
        }

        public boolean testSliceMin(PredicateContext ctx, MultiPredicate predicates) {
            if (ctx.layerCache() == null) return true;
            for (BasePredicate predicate : predicates) {
                if (predicate.testSliceMin(ctx)) return true;
            }
            return false;
        }
    }
}
