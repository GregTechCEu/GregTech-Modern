package com.gregtechceu.gtceu.api.multiblock.predicates.logic;

import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.api.multiblock.PredicateContext;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.predicates.CompactedPredicate;

import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

public abstract class BaseLogic {

    protected final MultiPredicate rootPredicate;
    private final Set<CompactedPredicate> compactedPredicates = new HashSet<>();
    @Setter
    protected boolean global = true; // used for XOR, AND

    public BaseLogic(MultiPredicate rootPredicate) {
        this.rootPredicate = rootPredicate;
        for (BasePredicate predicate : rootPredicate) {
            if (predicate instanceof CompactedPredicate compacted) {
                this.compactedPredicates.add(compacted);
            }
        }
    }

    public void reset() {
        this.compactedPredicates.forEach(CompactedPredicate::reset);
    }

    public abstract boolean test(PredicateContext ctx);

    public abstract boolean testGlobalMin(PredicateContext ctx);

    public abstract boolean testSliceMin(PredicateContext ctx);

    public abstract MultiPredicate.Logic getType();

    @Override
    public String toString() {
        return getType().name();
    }

    /// check max counts plus any additional logic
    public abstract boolean testMaxCount(BasePredicate passedPredicate, PredicateContext context);

    public void predicatePassed(BasePredicate predicate) {}
}
