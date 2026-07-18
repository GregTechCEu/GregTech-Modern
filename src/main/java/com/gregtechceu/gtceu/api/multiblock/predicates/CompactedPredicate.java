package com.gregtechceu.gtceu.api.multiblock.predicates;

import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.api.multiblock.PredicateContext;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import java.util.Collection;
import java.util.List;

public class CompactedPredicate extends BasePredicate {

    private final MultiPredicate root;

    public CompactedPredicate(MultiPredicate root) {
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

    public void reset() {
        this.root.reset();
    }
}
