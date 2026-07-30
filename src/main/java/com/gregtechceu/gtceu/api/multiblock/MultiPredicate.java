package com.gregtechceu.gtceu.api.multiblock;

import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.predicates.CompactedPredicate;
import com.gregtechceu.gtceu.api.multiblock.predicates.logic.AndLogic;
import com.gregtechceu.gtceu.api.multiblock.predicates.logic.BaseLogic;
import com.gregtechceu.gtceu.api.multiblock.predicates.logic.OrLogic;
import com.gregtechceu.gtceu.api.multiblock.predicates.logic.XorLogic;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import net.minecraft.network.chat.Component;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MultiPredicate implements Iterable<BasePredicate> {

    private static final Comparator<BasePredicate> PREDICATE_COMPARATOR = Comparator
            .comparingInt(BasePredicate::getPriority);

    private static final MultiPredicate EMPTY = new MultiPredicate();

    public static final MultiPredicate AIR = new MultiPredicate(BasePredicate.AIR);

    public static final MultiPredicate ANY = new MultiPredicate(BasePredicate.ANY);

    @Getter
    private final List<BasePredicate> predicateList;
    @Getter
    private final BaseLogic logic;
    private final boolean hasAir;
    @Getter
    @Setter
    @Accessors(chain = true)
    private boolean controller;

    public MultiPredicate(BasePredicate predicate) {
        this.predicateList = List.of(predicate);
        predicate.setParent(this);
        this.logic = Logic.OR.createLogic(this);
        this.hasAir = predicate == BasePredicate.AIR;
    }

    private MultiPredicate() {
        this.predicateList = List.of();
        this.logic = Logic.OR.createLogic(this);
        this.hasAir = false;
    }

    /// @param predicates list must be modifiable
    public MultiPredicate(Logic type, List<BasePredicate> predicates) {
        this(type, predicates, predicates.stream().anyMatch(p -> p == BasePredicate.AIR));
    }

    /// @param predicates list must be modifiable
    private MultiPredicate(Logic type, List<BasePredicate> predicates, boolean hasAir) {
        predicates.forEach(p -> p.setParent(this));
        predicates.sort(PREDICATE_COMPARATOR);
        this.predicateList = Collections.unmodifiableList(predicates);
        this.logic = type.createLogic(this);
        this.hasAir = hasAir;
    }

    /// delegates to {@link #logic} to test against global min count
    public boolean testGlobalMin(PredicateContext ctx) {
        return this.logic.testGlobalMin(ctx);
    }

    /// delegates to {@link #logic} to test against slice min count
    public boolean testSliceMin(PredicateContext ctx) {
        return this.logic.testSliceMin(ctx);
    }

    public List<List<BlockInfo>> getCandidates() {
        return this.predicateList.stream()
                .map(BasePredicate::getCandidates)
                .toList();
    }

    public void resetLogic() {
        this.logic.reset();
        for (BasePredicate p : this.predicateList) {
            if (p instanceof CompactedPredicate compacted) {
                compacted.expand().resetLogic();
            }
        }
    }

    public boolean isOr() {
        return this.logic.getType() == Logic.OR;
    }

    public boolean isAnd() {
        return this.logic.getType() == Logic.AND;
    }

    public boolean isXor() {
        return this.logic.getType() == Logic.XOR;
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

    protected BasePredicate compact() {
        return new CompactedPredicate(this);
    }

    public MultiPredicate addTooltips(Component tooltip) {
        forEach(p -> p.addTooltips(tooltip));
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
        return new MultiPredicate(Logic.OR, predicates);
    }

    public static MultiPredicate and(List<BasePredicate> predicates) {
        return new MultiPredicate(Logic.AND, predicates);
    }

    public static MultiPredicate xor(List<BasePredicate> predicates) {
        return new MultiPredicate(Logic.XOR, predicates);
    }

    public boolean isSingle() {
        return predicateList.size() == 1;
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

    @Override
    public Iterator<BasePredicate> iterator() {
        return this.predicateList.iterator();
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
        ArrayList<BasePredicate> builder = new ArrayList<>();
        appendPredicates(type, a, builder);
        appendPredicates(type, b, builder);
        return new MultiPredicate(type, builder, a.hasAir || b.hasAir);
    }

    private static void appendPredicates(Logic type, MultiPredicate multiPredicate, ArrayList<BasePredicate> builder) {
        if (multiPredicate.isSingle() || multiPredicate.logic.getType() == type) {
            builder.addAll(multiPredicate.predicateList);
        } else {
            builder.add(multiPredicate.compact());
        }
    }

    /// @return innermost base predicate that passes state check at given pos
    public @Nullable BasePredicate getPredicateAtPos(PredicateContext context) {
        for (BasePredicate predicate : this) {
            if (predicate instanceof CompactedPredicate compacted) {
                var p = compacted.expand().getPredicateAtPos(context);
                if (p != null) return p;
                // else continue...
            } else if (predicate.test(context)) {
                // logic needs to capture this
                getLogic().predicatePassed(predicate);
                return predicate;
            }
        }
        onError(context);
        return null;
    }

    /// called when all predicates failed
    public void onError(PredicateContext ctx) {
        // ctx.appendError(new SimplePatternError(ctx.pos(), getCandidates()));
        forEach(p -> p.onError(ctx));
    }

    public enum Logic {

        OR,
        AND,
        XOR;

        public BaseLogic createLogic(MultiPredicate source) {
            return switch (this) {
                case AND -> new AndLogic(source);
                case XOR -> new XorLogic(source);
                default -> new OrLogic(source);
            };
        }
    }
}
