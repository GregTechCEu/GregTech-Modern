package com.gregtechceu.gtceu.api.multiblock.predicates;

import com.gregtechceu.gtceu.api.multiblock.PredicateContext;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Stream;

public class MultiPredicate extends BasePredicate {

    private final List<BasePredicate> predicateList = new ArrayList<>();
    private final @Nullable String debugName;
    private @Nullable Logic type;

    public MultiPredicate() {
        this(null);
    }

    public MultiPredicate(@Nullable String debugName) {
        this.debugName = debugName;
    }

    protected MultiPredicate(@Nullable String debugName, List<BasePredicate> predicates, Logic type) {
        this(debugName);
        this.type = type;
        addPredicates(predicates);
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

    protected MultiPredicate addPredicates(List<BasePredicate> predicates) {
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

    protected Logic getType() {
        return Objects.requireNonNull(type, "null type: " + this);
    }

    @Override
    public boolean hasAir() {
        for (BasePredicate predicate : predicateList) {
            if (predicate.hasAir()) return true;
        }
        return false;
    }

    @Override
    @Contract(pure = true)
    public MultiPredicate or(BasePredicate other) {
        if (!isValid()) this.type = Logic.OR;

        var predicate = new MultiPredicate(this.debugName, this.predicateList, this.getType());
        if (!(other instanceof MultiPredicate multi)) {
            return predicate.addPredicates(other);
        }

        if (!multi.isValid()) multi.type = Logic.OR;
        if (!isOr()) return multi.or(this);

        if (multi.isOr()) {
            predicate.addPredicates(multi.predicateList);
        } else if (multi.isAnd()) {
            predicate.addPredicates(multi);
        }

        return predicate;
    }

    @Override
    @Contract(pure = true)
    public MultiPredicate and(BasePredicate other) {
        if (!isValid()) this.type = Logic.AND;

        var predicate = new MultiPredicate(this.debugName, this.predicateList, this.getType());
        if (!(other instanceof MultiPredicate multi)) {
            return predicate.addPredicates(other);
        }

        if (!multi.isValid()) multi.type = Logic.AND;
        if (!isAnd()) return multi.and(this);

        if (multi.isAnd()) {
            predicate.addPredicates(multi.predicateList);
        } else if (multi.isOr()) {
            predicate.addPredicates(multi);
        }

        return predicate;
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public @Nullable String getTypeName() {
        StringBuilder builder = new StringBuilder("Multi")
                .append('(')
                .append(this.type == null ? "INVAlID" : this.type)
                .append(')');
        if (debugName != null) {
            builder.append('#').append(debugName);
        }
        StringJoiner joiner = new StringJoiner(", ");
        this.predicateList.forEach(p -> joiner.add(p.getTypeName()));
        return builder.append('{')
                .append(joiner)
                .append('}')
                .toString();
    }

    protected enum Logic {
        OR,
        AND,
        XOR // unused
    }
}
