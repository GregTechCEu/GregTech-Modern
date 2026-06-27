package com.gregtechceu.gtceu.api.multiblock.predicates;

import com.gregtechceu.gtceu.api.multiblock.PredicateContext;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class MultiPredicate extends BasePredicate implements Iterable<BasePredicate> {

    private final List<BasePredicate> predicateList = new ObjectArrayList<>();
    private final String debugName;
    @Getter(AccessLevel.PROTECTED)
    private final Logic type;
    private final boolean hasAir;

    protected MultiPredicate(@Nullable String debugName, Iterable<BasePredicate> predicates, Logic type) {
        this.debugName = debugName == null ? "MultiPredicate" : debugName;
        this.type = type;
        boolean hasAir = false;
        for (BasePredicate predicate : predicates) {
            hasAir |= predicate.hasAir();
            if (!(predicate instanceof MultiPredicate multi) || !multi.sameType(this)) {
                addPredicate(predicate.simplify());
            } else {
                addPredicates(multi);
            }
        }
        this.hasAir = hasAir;
        sorted();
    }

    @Override
    public boolean test(PredicateContext ctx) {
        return testInternal(ctx) && testGlobalMax(ctx) && testSliceMax(ctx);
    }

    /// custom testing logic, usually checking if blockstate/entity is correct
    private boolean testInternal(PredicateContext ctx) {
        return getType().run(ctx, this.predicateList);
    }

    @Override
    public Predicate<PredicateContext> getPredicate() {
        // should a nested multi predicate respect its own global/slice max?
        // precedent would say no
        return this::testInternal;
    }

    protected MultiPredicate addPredicates(Iterable<BasePredicate> predicates) {
        predicates.forEach(this::addPredicate);
        return this;
    }

    protected MultiPredicate addPredicate(BasePredicate predicate) {
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
        return this.predicateList.get(index).getCandidates();
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

    public boolean isXor() {
        return this.type == Logic.XOR;
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
    public BasePredicate or(BasePredicate other) {
        return Logic.OR.combine(this, other);
    }

    @Override
    public BasePredicate and(BasePredicate other) {
        return Logic.AND.combine(this, other);
    }

    @Override
    public BasePredicate xor(BasePredicate other) {
        return Logic.XOR.combine(this, other);
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public String getTypeName() {
        return debugName + '(' + this.type + ')';
    }

    @Override
    protected void appendContents(StringBuilder builder) {
        StringJoiner joiner = new StringJoiner(", ");
        this.predicateList.forEach(p -> joiner.add(p.toString()));
        builder.append(joiner);
    }

    @Override
    public Iterator<BasePredicate> iterator() {
        return this.predicateList.iterator();
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
        XOR {

            @Override
            protected boolean run(PredicateContext ctx, List<BasePredicate> predicates) {
                int passed = 0;
                for (BasePredicate basePredicate : predicates) {
                    if (basePredicate.test(ctx)) {
                        if (++passed > 1) return false;
                    }
                }
                return passed == 1;
            }

        };

        protected abstract boolean run(PredicateContext ctx, List<BasePredicate> predicates);

        /// @param a will have type set
        /// @param b may or may not be a multi predicate
        /// @return copy of {@code a} with {@code b} added
        protected BasePredicate combine(MultiPredicate a, BasePredicate b) {
            if (!(b instanceof MultiPredicate multi)) {
                // b is not a multi predicate, simply add it
                return a.copy().addPredicate(b.simplify()).sorted();
            }

            if (a.getType() != this && multi.getType() != this) {
                // if neither predicate is of this type, make new multi predicate of this type
                return new MultiPredicate(null, List.of(a, b), this);
            } else if (a.getType() != this) {
                // b must be of this type, flip operation to add a to b
                return this.combine(multi, a);
            }

            // a must be of this type, b may or may not
            var dest = a.copy();
            if (a.getType() == multi.getType()) {
                // add all inner predicates
                dest.addPredicates(multi);
            } else {
                // add multi predicate as a simplified predicate
                dest.addPredicate(multi.simplify());
            }

            return dest.sorted();
        }
    }
}
