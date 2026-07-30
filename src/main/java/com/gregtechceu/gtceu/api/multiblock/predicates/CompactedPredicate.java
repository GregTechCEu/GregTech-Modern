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

    // this should never be called
    @Override
    public boolean test(PredicateContext ctx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onError(PredicateContext ctx) {
        expand().onError(ctx);
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
}
