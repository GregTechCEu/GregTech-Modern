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
    protected boolean global = true;

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

    /// @return innermost base predicate that passes state check at given pos
    public @Nullable BasePredicate getPredicateAtPos(PredicateContext context) {
        context.setStage(PredicateContext.PredicateStage.INTERNAL);
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

    /// Called after all blocks are iterated <br/>
    /// Usually used for testing the global min of predicates
    public final boolean postGlobalTest(PredicateContext ctx) {
        ctx.setStage(PredicateContext.PredicateStage.GLOBAL_MIN);
        return testGlobalMin(ctx);
    }

    protected abstract boolean testGlobalMin(PredicateContext ctx);

    /// Called after iterating all blocks in a given slice <br/>
    /// Usually used for testing the slice min of predicates
    public final boolean postSliceTest(PredicateContext ctx) {
        ctx.setStage(PredicateContext.PredicateStage.SLICE_MIN);
        return testSliceMin(ctx);
    }

    protected abstract boolean testSliceMin(PredicateContext ctx);

    /// test against global/slice max counts
    public boolean testMaxCount(BasePredicate passedPredicate, PredicateContext context) {
        context.setStage(PredicateContext.PredicateStage.GLOBAL_MAX);
        if (!passedPredicate.testGlobalMax(context))
            return false;
        context.setStage(PredicateContext.PredicateStage.SLICE_MAX);
        return passedPredicate.testSliceMax(context);
    }

    public List<List<BlockInfo>> getCandidates() {
        List<List<BlockInfo>> result = new ArrayList<>();
        for (BasePredicate predicate : predicates()) {
            result.add(predicate.getCandidates());
        }
        for (MultiPredicate child : children()) {
            result.addAll(child.getCandidates());
        }
        return Collections.unmodifiableList(result);
    }

    public void resetLogic() {
        this.children.forEach(MultiPredicate::resetLogic);
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

    public MultiPredicate setDisableRenderFormed(boolean disable) {
        this.forEach(p -> p.setDisableRenderFormed(disable));
        return this;
    }

    /// Setting this to {@code true} means that this multi predicate will
    /// only consider the final global state of the multiblock.
    /// <br/>
    /// If {@code false}, the multi predicate logic will only consider
    /// each slice separately. Only noticable for XOR logic type multi predicates
    /// <br/><br/>
    /// Defaults to {@code true}
    public MultiPredicate setGlobal(boolean global) {
        this.global = global;
        return this;
    }

    /// @return a new multi predicate where any predicate may pass or be present in the multiblock
    public MultiPredicate or(@Nullable MultiPredicate other) {
        return combine(this, Logic.OR, other);
    }

    /// @return a new multi predicate where every predicate must pass or be present in the multiblock
    public MultiPredicate and(@Nullable MultiPredicate other) {
        return combine(this, Logic.AND, other);
    }

    /// @return a new multi predicate with every predicate exclusively <br/>
    /// OR-ed (only one predicate may be present in the multi)
    public MultiPredicate xor(@Nullable MultiPredicate other) {
        return combine(this, Logic.XOR, other);
    }

    /// @return a new multi predicate where any predicate may pass or be present in the multiblock
    public static MultiPredicate or(List<BasePredicate> predicates) {
        return of(Logic.OR, predicates);
    }

    /// @return a new multi predicate where every predicate must pass or be present in the multiblock
    public static MultiPredicate and(List<BasePredicate> predicates) {
        return of(Logic.AND, predicates);
    }

    /// @return a new multi predicate with every predicate exclusively <br/>
    /// OR-ed (only one predicate may be present in the multi)
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

    /// @return a flattened list of all base predicates
    public List<BasePredicate> expand() {
        if (!hasChildren()) return this.predicates;
        List<BasePredicate> expanded = new ArrayList<>(this.predicates);
        forEachChild(mp -> expanded.addAll(mp.expand()));
        return expanded;
    }

    public static MultiPredicate empty() {
        return EMPTY;
    }

    /// @param a left operand
    /// @param type logic of the new predicate
    /// @param b right operand, may be null
    /// @return If {@code b == null}, returns {@code a}. <br />
    /// If {@code a == EMPTY}, returns {@code b}. <br />
    /// Otherwise, returns a new MultiPredicate that combines {@code a} and {@code b}
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
