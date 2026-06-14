package com.gregtechceu.gtceu.api.multiblock.predicates;

import com.gregtechceu.gtceu.api.multiblock.PredicateContext;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Stream;

public class MultiPredicate extends BasePredicate {

    private final List<BasePredicate> predicateList = new ArrayList<>();
    private @Nullable String debugName;
    private @Nullable Logic type;

    public MultiPredicate() {}

    public MultiPredicate(String debugName) {
        this.debugName = debugName;
    }

    @Override
    public boolean test(PredicateContext ctx) {
        for (BasePredicate predicate : predicateList) {
            if (isOr() && predicate.test(ctx)) {
                return true;
            } else if (isAnd() && !predicate.test(ctx)) {
                return false;
            }
        }
        return false;
    }

    @Override
    public List<BlockInfo> computeCandidates() {
        return predicateList.stream()
                .flatMap(p -> p.getCandidates().stream())
                .toList();
    }

    @Override
    public List<BasePredicate> expand() {
        return predicateList.stream()
                .flatMap(p -> p instanceof MultiPredicate mp ? mp.predicateList.stream() : Stream.of(p))
                .toList();
    }

    @Override
    public MultiPredicate setMinCount(int minCount) {
        this.predicateList.forEach(p -> p.setMinCount(minCount));
        super.setMinCount(minCount);
        return this;
    }

    @Override
    public MultiPredicate setMaxCount(int maxCount) {
        this.predicateList.forEach(p -> p.setMaxCount(maxCount));
        super.setMaxCount(maxCount);
        return this;
    }

    @Override
    public MultiPredicate setMinSliceCount(int minSliceCount) {
        this.predicateList.forEach(p -> p.setMinSliceCount(minSliceCount));
        super.setMinSliceCount(minSliceCount);
        return this;
    }

    @Override
    public MultiPredicate setMaxSliceCount(int maxSliceCount) {
        this.predicateList.forEach(p -> p.setMaxSliceCount(maxSliceCount));
        super.setMaxSliceCount(maxSliceCount);
        return this;
    }

    @Override
    public MultiPredicate setPreviewCount(int previewCount) {
        this.predicateList.forEach(p -> p.setPreviewCount(previewCount));
        super.setPreviewCount(previewCount);
        return this;
    }

    @Override
    public MultiPredicate setDisableRenderFormed(boolean disableRenderFormed) {
        this.predicateList.forEach(p -> p.setDisableRenderFormed(disableRenderFormed));
        super.setDisableRenderFormed(disableRenderFormed);
        return this;
    }

    public boolean isOr() {
        return this.type == Logic.OR;
    }

    public boolean isAnd() {
        return this.type == Logic.AND;
    }

    public boolean isValid() {
        return this.type != null;
    }

    @Override
    public MultiPredicate or(BasePredicate other) {
        if (!isValid()) {
            this.type = Logic.OR;
        }
        if (!(other instanceof MultiPredicate multi)) {
            this.predicateList.add(other);
            return this;
        }

        if (!isOr()) return multi.or(this);

        if (multi.isOr()) {
            this.predicateList.addAll(multi.predicateList);
        } else if (multi.isAnd()) {
            this.predicateList.add(multi);
        }

        return this;
    }

    @Override
    public MultiPredicate and(BasePredicate other) {
        if (!isValid()) {
            this.type = Logic.AND;
        }
        if (!(other instanceof MultiPredicate multi)) {
            this.predicateList.add(other);
            return this;
        }

        if (!isAnd()) return multi.and(this);

        if (multi.isAnd()) {
            this.predicateList.addAll(multi.predicateList);
        } else if (multi.isOr()) {
            this.predicateList.add(multi);
        }

        return this;
    }

    @Override
    public String getDebugName() {
        StringBuilder builder = new StringBuilder("Multi");
        if (debugName != null) {
            builder.append('#').append(debugName);
        }
        StringJoiner joiner = new StringJoiner(", ");
        this.predicateList.forEach(p -> joiner.add(p.getDebugName()));
        return builder.append('{')
                .append(joiner)
                .append('}')
                .toString();
    }

    protected enum Logic {
        OR, AND,
        XOR // unused
    }
}
