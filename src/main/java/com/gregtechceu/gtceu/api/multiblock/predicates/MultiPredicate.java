package com.gregtechceu.gtceu.api.multiblock.predicates;

import com.gregtechceu.gtceu.api.multiblock.PredicateContext;
import com.gregtechceu.gtceu.api.multiblock.error.SinglePredicateError;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class MultiPredicate extends BasePredicate {

    private final List<BasePredicate> predicateList = new ObjectArrayList<>();
    private final List<List<BlockInfo>> indexedCandidates;
    private final String debugName;
    private @Nullable Logic type;
    private boolean hasAir = false;

    public MultiPredicate(@Nullable String debugName) {
        this.debugName = debugName == null ? "MultiPredicate" : debugName;
        this.indexedCandidates = Collections.emptyList();
    }

    protected MultiPredicate(@Nullable String debugName, Iterable<BasePredicate> predicates, Logic type) {
        this.debugName = debugName == null ? "MultiPredicate" : debugName;
        this.type = type;
        List<List<BlockInfo>> indexedCandidates = new ArrayList<>();
        for (BasePredicate predicate : predicates) {
            this.hasAir |= predicate.hasAir();
            if (!(predicate instanceof MultiPredicate multi)) {
                addPredicates(predicate);
                indexedCandidates.add(predicate.getCandidates());
            } else if (!multi.sameType(this)) {
                addPredicates(predicate);
                indexedCandidates.add(predicate.getCandidates());
            } else {
                addPredicates(multi.predicateList);
                multi.predicateList.forEach(p -> indexedCandidates.add(p.getCandidates()));
            }
        }
        this.indexedCandidates = Collections.unmodifiableList(indexedCandidates);
        sorted();
    }

    @Override
    public boolean test(PredicateContext ctx) {
        return testInternal(ctx) && testGlobalMax(ctx) && testSliceMax(ctx);
    }

    /// custom testing logic, usually checking if blockstate/entity is correct
    private boolean testInternal(PredicateContext ctx) {
        if (this.predicateList.isEmpty()) return true;
        if (isSingle()) return this.predicateList.get(0).test(ctx);
        return getType().run(ctx, this.predicateList);
    }

    /// test against global max count
    private boolean testGlobalMax(PredicateContext ctx) {
        ctx.globalCache().mergeInt(this, 1, Integer::sum);
        if ((minCount == -1 && maxCount == -1) || ctx.layerCache() == null) return true;
        int count = ctx.globalCache().getInt(this);
        if (maxCount == -1 || count <= maxCount) return true;
        return ctx.error(SinglePredicateError.maxCount(this, count));
    }

    /// test against slice max count
    private boolean testSliceMax(PredicateContext ctx) {
        if (ctx.layerCache() == null) return true;
        ctx.layerCache().mergeInt(this, 1, Integer::sum);
        if ((minSliceCount == -1 && maxSliceCount == -1)) return true;
        int count = ctx.layerCache().getInt(this);
        if (maxSliceCount == -1 || count <= maxSliceCount) return true;
        return ctx.error(SinglePredicateError.maxLayerCount(this, count));
    }

    protected MultiPredicate addPredicates(Collection<BasePredicate> predicates) {
        predicates.forEach(this::addPredicates);
        return this;
    }

    protected MultiPredicate addPredicates(BasePredicate predicate) {
        this.predicateList.add(predicate);
        return this;
    }

    @Override
    public List<BlockInfo> computeCandidates() {
        return predicateList.stream()
                .flatMap(p -> p.getCandidates().stream())
                .toList();
    }

    @Override
    public List<BlockInfo> getCandidates(int index) {
        return this.indexedCandidates.get(index);
    }

    @Override
    public void visit(Consumer<BasePredicate> visitor) {
        predicateList.forEach(p -> p.visit(visitor));
    }

    public boolean isOr() {
        return this.type == Logic.OR;
    }

    public boolean isAnd() {
        return this.type == Logic.AND;
    }

    public boolean isValid() {
        return isSingle() || this.type != null;
    }

    protected Logic getType() {
        return Objects.requireNonNull(type, "null type: " + this);
    }

    protected boolean sameType(MultiPredicate other) {
        return this.type == other.type;
    }

    @Override
    public boolean hasAir() {
        return this.hasAir;
    }

    protected MultiPredicate sorted() {
        this.predicateList.sort(PREDICATE_COMPARATOR);
        return this;
    }

    protected MultiPredicate copy() {
        return new MultiPredicate(this.debugName, this.predicateList, this.getType());
    }

    @Override
    public MultiPredicate or(BasePredicate other) {
        if (!isValid()) this.type = Logic.OR;

        if (!(other instanceof MultiPredicate multi)) {
            return this.copy().addPredicates(other).sorted();
        }

        if (!isOr()) return multi.or(this);

        return combine(this, multi, this.copy());
    }

    @Override
    public MultiPredicate and(BasePredicate other) {
        if (!isValid()) this.type = Logic.AND;

        if (!(other instanceof MultiPredicate multi)) {
            return this.copy().addPredicates(other).sorted();
        }

        if (!isAnd()) return multi.and(this);

        return combine(this, multi, this.copy());
    }

    @Override
    public boolean isSingle() {
        return this.predicateList.size() == 1;
    }

    @Override
    public StringBuilder appendType(StringBuilder builder) {
        builder.append(debugName)
                .append('(')
                .append(isValid() ? isSingle() ? "SINGLE" : this.type : "INVAlID")
                .append(')');
        return builder;
    }

    @Override
    protected StringBuilder appendContents(StringBuilder builder) {
        StringJoiner joiner = new StringJoiner(", ");
        this.predicateList.forEach(p -> joiner.add(p.toString()));
        return builder.append(joiner);
    }

    private static MultiPredicate combine(MultiPredicate a, MultiPredicate b, MultiPredicate dest) {
        if (!b.isValid()) {
            b.type = a.getType();
        }
        if (a.getType() == b.getType()) {
            dest.addPredicates(b.predicateList);
        } else {
            dest.addPredicates(b);
        }

        return dest.sorted();
    }

    protected enum Logic {

        OR {

            @Override
            protected boolean run(PredicateContext ctx, List<BasePredicate> predicates) {
                for (BasePredicate basePredicate : predicates) {
                    if (basePredicate.test(ctx)) {
                        return true;
                    }
                }
                return false;
            }
        },
        AND {

            @Override
            protected boolean run(PredicateContext ctx, List<BasePredicate> predicates) {
                return !OR.run(ctx, predicates);
            }
        },
        // unused
        XOR {

            @Override
            protected boolean run(PredicateContext ctx, List<BasePredicate> predicates) {
                return true;
            }
        };

        protected abstract boolean run(PredicateContext ctx, List<BasePredicate> predicates);
    }
}
