package com.gregtechceu.gtceu.api.multiblock.predicates;

import com.gregtechceu.gtceu.api.multiblock.PredicateContext;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class MultiPredicate extends BasePredicate {

    private final List<BasePredicate> predicateList = new ObjectArrayList<>();
    private final @Nullable String debugName;
    private @Nullable Logic type;

    public MultiPredicate(@Nullable String debugName) {
        this.debugName = debugName;
    }

    protected MultiPredicate(@Nullable String debugName, Iterable<BasePredicate> predicates, Logic type) {
        this(debugName);
        this.type = type;
        for (BasePredicate predicate : predicates) {
            if (!(predicate instanceof MultiPredicate multi)) {
                addPredicates(predicate);
                continue;
            }

            if (!multi.isValid() || multi.getType() == this.getType()) {
                addPredicates(multi.predicateList);
            } else {
                addPredicates(multi);
            }
        }
        sorted();
    }

    @Override
    public boolean test(PredicateContext ctx) {
        return getType().run(ctx, this.predicateList);
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
    public void visit(Consumer<BasePredicate> visitor) {
        predicateList.forEach(p -> p.visit(visitor));
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

    protected MultiPredicate sorted() {
        this.predicateList.sort(PREDICATE_COMPARATOR);
        return this;
    }

    @Override
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

        return predicate.sorted();
    }

    @Override
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

        return predicate.sorted();
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public StringBuilder appendType(StringBuilder builder) {
        builder.append("Multi")
                .append('(')
                .append(this.type == null ? "INVAlID" : this.type)
                .append(')');
        if (debugName != null) {
            builder.append('#').append(debugName);
        }
        return builder;
    }

    @Override
    protected StringBuilder appendContents(StringBuilder builder) {
        StringJoiner joiner = new StringJoiner(", ");
        this.predicateList.forEach(p -> joiner.add(p.toString()));
        return builder.append(joiner);
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
