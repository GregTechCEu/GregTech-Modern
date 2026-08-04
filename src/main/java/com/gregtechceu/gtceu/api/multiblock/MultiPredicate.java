package com.gregtechceu.gtceu.api.multiblock;

import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import net.minecraft.network.chat.Component;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public abstract class MultiPredicate {

    private static final MultiPredicate EMPTY = of(Logic.OR, List.of());

    public static final MultiPredicate AIR = of(BasePredicate.AIR);

    public static final MultiPredicate ANY = of(BasePredicate.ANY);

    private final List<BasePredicate> predicates;
    private final List<MultiPredicate> children;
    private final boolean hasAir;
    @Getter
    private final Logic type;
    @Getter
    @Setter
    @Accessors(chain = true)
    private boolean controller;

    @Nullable
    @Getter
    @Setter
    private MultiPredicate parent;

    public static MultiPredicate of(BasePredicate predicate) {
        return Logic.OR.makePredicate(predicate, predicate == BasePredicate.AIR);
    }

    /// @param predicates list must be modifiable
    private static MultiPredicate of(Logic type, List<BasePredicate> predicates) {
        return type.makePredicate(List.of(), predicates, predicates.stream().anyMatch(p -> p == BasePredicate.AIR));
    }

    /// @param children list of multi predicate children
    /// @param predicates list of testable predicates, should be sorted already
    protected MultiPredicate(Logic type, List<MultiPredicate> children, List<BasePredicate> predicates,
                             boolean hasAir) {
        predicates.forEach(p -> p.setParent(this));
        children.forEach(mp -> mp.setParent(this));
        this.predicates = Collections.unmodifiableList(predicates);
        this.children = Collections.unmodifiableList(children);
        this.type = type;
        this.hasAir = hasAir;
    }

    /// test against global min count
    public abstract boolean testGlobalMin(PredicateContext ctx);

    /// test against slice min count
    public abstract boolean testSliceMin(PredicateContext ctx);

    /// test against global/slice max counts
    public abstract boolean testMaxCount(BasePredicate passedPredicate, PredicateContext context);

    public List<List<BlockInfo>> getCandidates() {
        return this.predicates.stream()
                .map(BasePredicate::getCandidates)
                .toList();
    }

    public void resetLogic() {
        children.forEach(MultiPredicate::resetLogic);
    }

    public boolean isOr() {
        return this.getType() == Logic.OR;
    }

    public boolean isAnd() {
        return this.getType() == Logic.AND;
    }

    public boolean isXor() {
        return this.getType() == Logic.XOR;
    }

    private boolean isType(Logic type) {
        return this.type == type;
    }

    @ApiStatus.Internal
    public boolean isEmpty() {
        return this == EMPTY;
    }

    public boolean isAny() {
        return this == ANY;
    }

    public boolean isAir() {
        return this == AIR;
    }

    public boolean hasAir() {
        return this.hasAir;
    }

    public MultiPredicate addTooltips(Component tooltip) {
        this.forEach(p -> p.addTooltips(tooltip));
        return this;
    }

    public MultiPredicate setPriority(int priority) {
        this.forEach(p -> p.setPriority(priority));
        return this;
    }

    public MultiPredicate setMinGlobalLimited(int min) {
        return this.setMinCount(min);
    }

    public MultiPredicate setMinGlobalLimited(int min, int previewCount) {
        return this.setMinCount(min).setPreviewCount(previewCount);
    }

    public MultiPredicate setMinCount(int min) {
        this.forEach(p -> p.setMinCount(min));
        return this;
    }

    public MultiPredicate setMaxGlobalLimited(int max) {
        return this.setMaxCount(max);
    }

    public MultiPredicate setMaxGlobalLimited(int max, int previewCount) {
        return this.setMaxCount(max).setPreviewCount(previewCount);
    }

    public MultiPredicate setMaxCount(int max) {
        this.forEach(p -> p.setMaxCount(max));
        return this;
    }

    public MultiPredicate setGlobalMinMax(int min, int max) {
        return this.setMinCount(min).setMaxCount(max);
    }

    public MultiPredicate setMinLayerLimited(int min) {
        return this.setMinSliceCount(min);
    }

    public MultiPredicate setMinLayerLimited(int min, int previewCount) {
        return this.setMinSliceCount(min).setPreviewCount(previewCount);
    }

    public MultiPredicate setMinSliceCount(int min) {
        this.forEach(p -> p.setMinSliceCount(min));
        return this;
    }

    public MultiPredicate setMaxLayerLimited(int max) {
        return this.setMaxSliceCount(max);
    }

    public MultiPredicate setMaxLayerLimited(int max, int previewCount) {
        return this.setMaxSliceCount(max).setPreviewCount(previewCount);
    }

    public MultiPredicate setMaxSliceCount(int max) {
        this.forEach(p -> p.setMaxSliceCount(max));
        return this;
    }

    public MultiPredicate setPreviewCount(int previewCount) {
        this.forEach(p -> p.setPreviewCount(previewCount));
        return this;
    }

    public MultiPredicate setLayerMinMax(int min, int max) {
        return this.setMinSliceCount(min).setMaxSliceCount(max);
    }

    /**
     * Sets the Minimum and Maximum limit to the passed value
     *
     * @param limit The Maximum and Minimum limit
     */
    public MultiPredicate setExactLimit(int limit) {
        return this.setGlobalMinMax(limit, limit);
    }

    public MultiPredicate disabledRenderFormed() {
        return setDisableRenderFormed(true);
    }

    private MultiPredicate setDisableRenderFormed(boolean disable) {
        this.forEach(p -> p.setDisableRenderFormed(disable));
        return this;
    }

    public MultiPredicate or(MultiPredicate other) {
        return combine(this, Logic.OR, other);
    }

    public MultiPredicate and(MultiPredicate other) {
        return combine(this, Logic.AND, other);
    }

    public MultiPredicate xor(MultiPredicate other) {
        return combine(this, Logic.XOR, other);
    }

    public static MultiPredicate or(List<BasePredicate> predicates) {
        return of(Logic.OR, predicates);
    }

    public static MultiPredicate and(List<BasePredicate> predicates) {
        return of(Logic.AND, predicates);
    }

    public static MultiPredicate xor(List<BasePredicate> predicates) {
        return of(Logic.XOR, predicates);
    }

    public boolean isSingle() {
        return predicates.size() == 1 && !hasChildren();
    }

    protected boolean hasChildren() {
        return !this.children.isEmpty();
    }

    public void appendContents(StringBuilder builder) {
        StringJoiner joiner = new StringJoiner(", ");
        this.forEach(p -> joiner.add(p.toString()));
        builder.append(joiner);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("MultiPredicate");
        if (isController()) builder.append("[Controller]");
        builder.append('{');
        appendContents(builder);
        builder.append('}');
        return builder.toString();
    }

    protected void forEach(Consumer<BasePredicate> action) {
        this.predicates.forEach(action);
    }

    public List<BasePredicate> predicates() {
        return this.predicates;
    }

    public void forEachChild(Consumer<MultiPredicate> action) {
        this.children.forEach(action);
    }

    public List<MultiPredicate> children() {
        return this.children;
    }

    public List<BasePredicate> expand() {
        List<BasePredicate> expanded = new ArrayList<>(this.predicates);
        if (hasChildren()) {
            forEachChild(mp -> expanded.addAll(mp.expand()));
        }
        return expanded;
    }

    public static MultiPredicate empty() {
        return EMPTY;
    }

    /// @param a will have type set
    /// @param b may or may not be a multi predicate
    /// @return copy of {@code a} combined with {@code b}
    private static MultiPredicate combine(MultiPredicate a, Logic type, @Nullable MultiPredicate b) {
        if (b == null) return a; // no op
        if (a.isEmpty()) return b;
        List<BasePredicate> predicates = new ArrayList<>();
        List<MultiPredicate> children = new ArrayList<>();
        appendPredicates(type, a, predicates, children);
        appendPredicates(type, b, predicates, children);
        predicates.sort(BasePredicate::compareTo);
        return type.makePredicate(children, predicates, a.hasAir || b.hasAir);
    }

    private static void appendPredicates(Logic type, MultiPredicate multiPredicate,
                                         List<BasePredicate> predicates, List<MultiPredicate> children) {
        if (multiPredicate.isSingle() || multiPredicate.isType(type)) {
            predicates.addAll(multiPredicate.predicates());
        } else {
            children.add(multiPredicate);
        }
    }

    /// @return innermost base predicate that passes state check at given pos
    public @Nullable BasePredicate getPredicateAtPos(PredicateContext context) {
        for (BasePredicate predicate : predicates()) {
            if (predicate.test(context)) {
                return predicate;
            }
        }
        for (MultiPredicate predicates : children()) {
            BasePredicate p = predicates.getPredicateAtPos(context);
            if (p != null) return p;
        }
        if (!hasChildren()) {
            onError(context);
        }
        return null;
    }

    /// called when all predicates failed
    protected void onError(PredicateContext ctx) {
        this.forEach(p -> p.onError(ctx));
        this.forEachChild(mp -> mp.onError(ctx));
    }

    protected enum Logic {

        OR,
        AND,
        XOR;

        public MultiPredicate makePredicate(List<MultiPredicate> children, List<BasePredicate> predicates,
                                            boolean hasAir) {
            return switch (this) {
                case OR -> new OrPredicate(children, predicates, hasAir);
                case AND -> new AndPredicate(children, predicates, hasAir);
                case XOR -> new XorPredicate(children, predicates, hasAir);
            };
        }

        public MultiPredicate makePredicate(BasePredicate predicate, boolean hasAir) {
            return makePredicate(List.of(), List.of(predicate), hasAir);
        }
    }
}
