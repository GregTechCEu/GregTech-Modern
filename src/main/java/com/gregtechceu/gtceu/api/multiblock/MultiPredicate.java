package com.gregtechceu.gtceu.api.multiblock;

import com.gregtechceu.gtceu.api.multiblock.error.PatternStringError;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.predicates.PredicateSettings;
import com.gregtechceu.gtceu.api.multiblock.predicates.SettingsHolder;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import net.minecraft.network.chat.Component;

import dev.latvian.mods.rhino.util.RemapForJS;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public abstract class MultiPredicate implements SettingsHolder {

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

    /*
       I was curious on your opinion on how recursiveness of setting mins on multipredicates should be done, if at all

        a.setmin(4).and(b.setmin(4) -> at least 4 of a and 4 of b

        a.and(b).setmin(4) -> at least 4 of a and b combined

        should we allow something like
        a.and(b).setmin(4,true) or .setminRecursive(4) -> at least 4 of a and 4 of b

        for the case where the user doesn't have access to the original a and b predicates?
        how do we see this "recursive" idea for the difference between children and predicates? and what about when you have multiple nested children?
        or should we just not bother?
        I'm leaning to not bother, just make the predicates yourself and edit them acordingly, but wanna discuss it at least
     */
    @Nullable
    @Getter
    private PredicateSettings settings = null;

    @Getter
    private final List<Component> additionalTooltips = new ArrayList<>();

    @Nullable
    @Getter
    @Setter
    private MultiPredicate parent;

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
        if (isRoot()) {
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
        boolean passed = testGlobalMin(ctx);
        if (this.settings != null) {
            passed &= SettingsHolder.super.testGlobalMin(ctx.getGlobalCount(this));
        }
        if (passed) return true;
        for (Component content : getDescriptiveContents()) {
            ctx.appendError(PatternStringError.of(content));
        }
        return false;
    }

    protected abstract boolean testGlobalMin(PredicateContext ctx);

    /// Called after iterating all blocks in a given slice <br/>
    /// Usually used for testing the slice min of predicates
    public final boolean postSliceTest(PredicateContext ctx) {
        ctx.setStage(PredicateContext.PredicateStage.SLICE_MIN);
        boolean passed = testSliceMin(ctx);
        if (this.settings != null) {
            passed &= SettingsHolder.super.testSliceMin(ctx.getSliceCount(this));
        }
        if (passed) return true;
        for (Component content : getDescriptiveContents()) {
            ctx.appendError(PatternStringError.of(content));
        }
        return false;
    }

    protected abstract boolean testSliceMin(PredicateContext ctx);

    /// test against global/slice max counts
    public boolean testMaxCount(BasePredicate passedPredicate, PredicateContext context) {
        context.setStage(PredicateContext.PredicateStage.GLOBAL_MAX);
        boolean passed = passedPredicate.testGlobalMax(context);
        if (this.settings != null) {
            passed &= this.testGlobalMax(context.incrementGlobalCount(this));
        }
        if (passed) {
            context.setStage(PredicateContext.PredicateStage.SLICE_MAX);
            passed = passedPredicate.testSliceMax(context);
            if (this.settings != null) {
                passed &= this.testSliceMax(context.incrementSliceCount(this));
            }
        }
        return passed;
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

    /// @return {@code true} if this multi predicate has only one predicate, has no children, and has no parent
    public boolean isSingle() {
        return predicates.size() == 1 && isLeaf() && isRoot();
    }

    /// @return {@code true} if this multi predicate has no parent
    public boolean isRoot() {
        return getParent() == null;
    }

    /// @return {@code true} if this multi predicate has children and is not a root predicate
    public boolean isBranch() {
        return !isLeaf() && !isRoot();
    }

    /// @return {@code true} if this multi predicate has no children multi predicates
    public boolean isLeaf() {
        return this.children.isEmpty();
    }

    public List<Component> getDescriptiveContents() {
        List<Component> list = new ArrayList<>();
        Component logicLine = switch (this.type) {
            case OR -> Component.literal("any of:");
            case AND -> Component.literal("all of:");
            case XOR -> Component.literal("one of:");
        };
        list.add(logicLine);
        for (BasePredicate predicate : predicates()) {
            // todo prettier string?
            list.add(Component.literal(predicate.toString()));
        }
        for (MultiPredicate child : children()) {
            list.addAll(child.getDescriptiveContents());
        }
        return list;
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
        if (isLeaf()) return this.predicates;
        List<BasePredicate> expanded = new ArrayList<>(this.predicates);
        forEachChild(mp -> expanded.addAll(mp.expand()));
        return expanded;
    }

    /*
     * MUTATE AND DO NOT COPY
     */

    protected PredicateSettings getOrCreateSettings() {
        if (this.settings == null) {
            this.settings = PredicateSettings.create();
        }
        return this.settings;
    }

    @RemapForJS("addTooltip")
    @Contract(mutates = "this")
    public MultiPredicate addTooltips(Component tooltip) {
        this.additionalTooltips.add(tooltip);
        return this;
    }

    @CheckReturnValue
    @Contract(mutates = "this")
    public MultiPredicate addTooltips(Component... tooltip) {
        Collections.addAll(this.additionalTooltips, tooltip);
        return this;
    }

    private void updateSettings(UnaryOperator<PredicateSettings> configurator) {
        if (isSingle()) {
            predicates().get(0).updateSettings(configurator);
        }
        setSettings(Objects.requireNonNull(configurator.apply(getOrCreateSettings())));
    }

    public void setSettings(@Nullable PredicateSettings settings) {
        if (settings != null) {
            this.settings = settings.copy();
        }
    }

    @Override
    public void setPriority(int priority) {
        if (this.settings != null) {
            updateSettings(s -> s.withPriority(priority));
        }
    }

    @Override
    public void setMinCount(int minCount) {
        if (this.settings != null) {
            updateSettings(s -> s.withMinCount(minCount));
        }
    }

    @Override
    public void setMaxCount(int maxCount) {
        if (this.settings != null) {
            updateSettings(s -> s.withMaxCount(maxCount));
        }
    }

    @Override
    public void setMinSliceCount(int minSliceCount) {
        if (this.settings != null) {
            updateSettings(s -> s.withMinSliceCount(minSliceCount));
        }
    }

    @Override
    public void setMaxSliceCount(int maxSliceCount) {
        if (this.settings != null) {
            updateSettings(s -> s.withMaxSliceCount(maxSliceCount));
        }
    }

    @Override
    public void setPreviewCount(int previewCount) {
        if (this.settings != null) {
            updateSettings(s -> s.withPreviewCount(previewCount));
        }
    }

    @Override
    public void setDisableRenderFormed(boolean disableRenderFormed) {
        if (this.settings != null) {
            updateSettings(s -> s.withDisableRenderFormed(disableRenderFormed));
        }
    }

    /*
     * MUTATE AND COPY
     */

    @CheckReturnValue
    private MultiPredicate deepCopy() {
        MultiPredicate copy = this.type.makePredicate(
                children().stream().map(MultiPredicate::deepCopy).toList(),
                predicates().stream().map(BasePredicate::copy).toList(),
                this.hasAir);
        copy.setSettings(this.settings);
        copy.additionalTooltips.addAll(this.additionalTooltips);
        copy.setController(this.controller);
        return copy;
    }

    @CheckReturnValue
    public MultiPredicate copyWith(Consumer<MultiPredicate> configurator) {
        MultiPredicate copy = deepCopy();
        configurator.accept(copy);
        return copy;
    }

    @Override
    @CheckReturnValue
    public MultiPredicate withSettings(UnaryOperator<PredicateSettings> configurator) {
        return copyWith(p -> p.setSettings(configurator.apply(getOrCreateSettings())));
    }

    @CheckReturnValue
    public MultiPredicate withPriority(int priority) {
        return withSettings(s -> s.withPriority(priority));
    }

    @CheckReturnValue
    public MultiPredicate withMinCount(int min) {
        return withSettings(s -> s.withMinCount(min));
    }

    @CheckReturnValue
    public MultiPredicate withMaxCount(int max) {
        return withSettings(s -> s.withMaxCount(max));
    }

    @CheckReturnValue
    public MultiPredicate withMinSliceCount(int min) {
        return withSettings(s -> s.withMinSliceCount(min));
    }

    @CheckReturnValue
    public MultiPredicate withMaxSliceCount(int max) {
        return withSettings(s -> s.withMaxSliceCount(max));
    }

    @CheckReturnValue
    public MultiPredicate withPreviewCount(int previewCount) {
        return withSettings(s -> s.withPreviewCount(previewCount));
    }

    @CheckReturnValue
    public MultiPredicate withDisableRenderFormed(boolean disable) {
        return withSettings(s -> s.withDisableRenderFormed(disable));
    }

    @CheckReturnValue
    public MultiPredicate withMinGlobalLimited(int min) {
        return this.withMinCount(min);
    }

    @CheckReturnValue
    public MultiPredicate withMinGlobalLimited(int min, int previewCount) {
        return withSettings(s -> s.withMinCount(min).withPreviewCount(previewCount));
    }

    @CheckReturnValue
    public MultiPredicate withMaxGlobalLimited(int max) {
        return this.withMaxCount(max);
    }

    @CheckReturnValue
    public MultiPredicate withMaxGlobalLimited(int max, int previewCount) {
        return withSettings(s -> s.withMaxCount(max).withPreviewCount(previewCount));
    }

    @CheckReturnValue
    public MultiPredicate withGlobalMinMax(int min, int max) {
        return withSettings(s -> s.withMinCount(min).withMaxCount(max));
    }

    @CheckReturnValue
    public MultiPredicate withMinLayerLimited(int min) {
        return this.withMinSliceCount(min);
    }

    @CheckReturnValue
    public MultiPredicate withMinLayerLimited(int min, int previewCount) {
        return withSettings(s -> s.withMinSliceCount(min).withPreviewCount(previewCount));
    }

    @CheckReturnValue
    public MultiPredicate withMaxLayerLimited(int max) {
        return this.withMaxSliceCount(max);
    }

    @CheckReturnValue
    public MultiPredicate withMaxLayerLimited(int max, int previewCount) {
        return withSettings(s -> s.withMaxSliceCount(max).withPreviewCount(previewCount));
    }

    @CheckReturnValue
    public MultiPredicate withLayerMinMax(int min, int max) {
        return withSettings(s -> s.withMinSliceCount(min).withMaxSliceCount(max));
    }

    /**
     * Sets the Minimum and Maximum limit to the passed value
     *
     * @param limit The Maximum and Minimum limit
     */
    @CheckReturnValue
    public MultiPredicate withExactLimit(int limit) {
        return this.withGlobalMinMax(limit, limit);
    }

    /// @return a copy of this multi predicate with render formed disabled
    @CheckReturnValue
    public MultiPredicate disabledRenderFormed() {
        return withDisableRenderFormed(true);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("MultiPredicate");
        builder.append('[');
        if (isController()) builder.append("Controller=true, ");
        switch (this.type) {
            case OR -> builder.append("Logic=OR");
            case AND -> builder.append("Logic=AND");
            case XOR -> builder.append("Logic=XOR");
        }
        builder.append(']');
        builder.append('{');
        StringJoiner joiner = new StringJoiner(", ");
        this.forEach(p -> joiner.add(p.toString()));
        this.forEachChild(mp -> joiner.add(mp.toString()));
        builder.append(joiner);
        builder.append('}');
        return builder.toString();
    }

    /*
     * LOGIC AND COMBINATION
     */

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

    /// @param a left operand
    /// @param type logic of the new predicate
    /// @param b right operand, may be null
    /// @return If {@code b == null}, returns {@code a}. <br />
    /// If {@code a == EMPTY}, returns {@code b}. <br />
    /// Otherwise, returns a new MultiPredicate that combines {@code a} and {@code b}
    private static MultiPredicate combine(MultiPredicate a, Logic type, @Nullable MultiPredicate b) {
        if (b == null || b.isEmpty()) return a; // no op
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
            children.addAll(multiPredicate.children());
        } else {
            children.add(multiPredicate);
        }
    }

    public static MultiPredicate empty() {
        return EMPTY;
    }

    public static MultiPredicate of(BasePredicate predicate) {
        return Logic.OR.makePredicate(predicate, predicate == BasePredicate.AIR);
    }

    /// @param predicates list must be modifiable
    private static MultiPredicate of(Logic type, List<BasePredicate> predicates) {
        return type.makePredicate(List.of(), predicates, predicates.stream().anyMatch(p -> p == BasePredicate.AIR));
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
