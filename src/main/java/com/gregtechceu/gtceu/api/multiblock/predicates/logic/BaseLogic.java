package com.gregtechceu.gtceu.api.multiblock.predicates.logic;

import com.gregtechceu.gtceu.api.multiblock.PredicateContext;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.predicates.MultiPredicate;

import lombok.Setter;

public abstract class BaseLogic {

    protected final MultiPredicate rootPredicate;
    protected boolean noneValid; // used for XOR, AND
    @Setter
    protected boolean global = true; // used for XOR, AND

    public BaseLogic(MultiPredicate rootPredicate) {
        this.rootPredicate = rootPredicate;
    }

    public void onPredicateAdd(BasePredicate predicate) {
        boolean noneValid = true;
        if (predicate instanceof MultiPredicate.CompactedPredicate cp) {
            if (!cp.expand().getLogic().noneValid) {
                noneValid = false;
            }
        } else if (predicate.getMinCount() > 0 || predicate.getMinSliceCount() > 0) {
            noneValid = false;
        }
        this.noneValid |= noneValid;
    }

    public void reset() {}

    public abstract boolean test(PredicateContext ctx);

    public abstract boolean testGlobalMin(PredicateContext ctx);

    public abstract boolean testSliceMin(PredicateContext ctx);

    public abstract MultiPredicate.Logic getType();

    @Override
    public String toString() {
        return getType().name();
    }
}
