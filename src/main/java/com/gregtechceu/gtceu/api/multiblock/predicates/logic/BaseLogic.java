package com.gregtechceu.gtceu.api.multiblock.predicates.logic;

import com.gregtechceu.gtceu.api.multiblock.PredicateContext;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.predicates.MultiPredicate;

import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

public abstract class BaseLogic {

    protected final MultiPredicate rootPredicate;
    private Set<MultiPredicate.CompactedPredicate> compactedPredicates = new HashSet<>();
    @Setter
    protected boolean global = true; // used for XOR, AND

    public BaseLogic(MultiPredicate rootPredicate) {
        this.rootPredicate = rootPredicate;
    }

    public void onPredicateAdded() {
        for (BasePredicate predicate : this.rootPredicate) {
            if (predicate instanceof MultiPredicate.CompactedPredicate cp) {
                this.compactedPredicates.add(cp);
            }
        }
    }

    public void reset() {
        for (MultiPredicate.CompactedPredicate predicate : this.compactedPredicates) {
            predicate.expand().getLogic().reset();
        }
    }

    public abstract boolean test(PredicateContext ctx);

    public abstract boolean testGlobalMin(PredicateContext ctx);

    public abstract boolean testSliceMin(PredicateContext ctx);

    public abstract MultiPredicate.Logic getType();

    @Override
    public String toString() {
        return getType().name();
    }
}
