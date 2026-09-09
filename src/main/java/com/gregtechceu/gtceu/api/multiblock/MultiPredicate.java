package com.gregtechceu.gtceu.api.multiblock;

import com.gregtechceu.gtceu.api.multiblock.error.PatternStringError;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.predicates.PredicateSettings;
import com.gregtechceu.gtceu.api.multiblock.predicates.SettingsHolder;
import com.gregtechceu.gtceu.api.multiblock.predicates.TestType;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import net.minecraft.network.chat.Component;

import dev.latvian.mods.rhino.util.RemapForJS;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public abstract class MultiPredicate implements SettingsHolder<MultiPredicate> {

    private static final MultiPredicate EMPTY = of(Logic.OR, List.of()).markImmutable();

    /// use {@link Predicates#air()} instead
    @ApiStatus.Internal
    public static final MultiPredicate AIR = of(BasePredicate.AIR)
            .isAir(true).markImmutable();

    /// use {@link Predicates#any()} instead
    @ApiStatus.Internal
    public static final MultiPredicate ANY = of(BasePredicate.ANY)
            .isAny(true).markImmutable();

    private final List<BasePredicate> predicates;
    private final List<MultiPredicate> children;
    private final boolean hasAir;

    @Accessors(fluent = true)
    @Setter(AccessLevel.PRIVATE)
    @Getter
    private boolean isAir = false;

    @Accessors(fluent = true)
    @Setter(AccessLevel.PRIVATE)
    @Getter
    private boolean isAny = false;

    @Getter
    private final Logic type;

    @Getter
    @Accessors(chain = true)
    private boolean controller;

    /// Nonnull by default, see {@link #recursive()}
    @Nullable
    @Getter
    private PredicateSettings settings;

    private boolean mutable = true;

    /// @param children list of multi predicate children
    /// @param predicates list of testable predicates, should be sorted already
    protected MultiPredicate(Logic type, List<MultiPredicate> children,
                             List<BasePredicate> predicates, boolean hasAir) {
        this.predicates = Collections.unmodifiableList(predicates);
        this.children = Collections.unmodifiableList(children);
        this.type = type;
        this.hasAir = hasAir;
    }

    /// @return innermost base predicate that passes state check at given pos
    public PredicateResult getPredicateAtPos(PredicateContext context) {
        context.setStage(PredicateContext.PredicateStage.INTERNAL);
        for (BasePredicate predicate : predicates()) {
            if (predicate.test(context)) {
                PredicateResult result = onPredicateMatched(PredicateResult.of(predicate, this), context);
                if (result.failed()) return PredicateResult.noMatch();
                if (result.hasMatched()) return result;
            }
        }
        for (MultiPredicate predicates : children()) {
            var result = predicates.getPredicateAtPos(context);
            if (result.hasMatched()) {
                result = onPredicateMatched(result, context);
                return result.failed() ? PredicateResult.noMatch() : result.appendParent(this);
            }
        }

        return PredicateResult.noMatch();
    }

    /// @param result a result with the passed predicate and call chain
    /// @return by default returns {@code result}, but can return a modified result (see {@link XorPredicate})
    protected PredicateResult onPredicateMatched(PredicateResult result, PredicateContext context) {
        return result;
    }

    /// called when all predicates failed
    public void onError(PredicateContext ctx) {
        this.forEach(p -> p.onError(ctx));
        this.forEachChild(mp -> mp.onError(ctx));
    }

    /// Called after all blocks are iterated <br/>
    /// Usually used for testing the global min of predicates
    public final boolean postGlobalTest(PredicateContext ctx) {
        ctx.setStage(PredicateContext.PredicateStage.GLOBAL_MIN);
        if (testGlobalMin(ctx) && TestType.GLOBAL_MIN.testCounts(this, ctx)) {
            return true;
        }
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
        if (testSliceMin(ctx) && TestType.SLICE_MIN.testCounts(this, ctx)) {
            return true;
        }
        for (Component content : getDescriptiveContents()) {
            ctx.appendError(PatternStringError.of(content));
        }
        return false;
    }

    protected abstract boolean testSliceMin(PredicateContext ctx);

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

    public boolean hasAir() {
        return this.hasAir;
    }

    /// @return {@code true} if this multi predicate has only one predicate and has no children
    public boolean isSingle() {
        return predicates.size() == 1 && this.children.isEmpty();
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
        if (this.children.isEmpty()) return this.predicates;
        List<BasePredicate> expanded = new ArrayList<>(this.predicates);
        forEachChild(mp -> expanded.addAll(mp.expand()));
        return expanded;
    }

    @Override
    public boolean hasSettings() {
        return this.settings != null;
    }

    /*
     * MUTATE AND DO NOT COPY
     */

    @RemapForJS("addTooltip")
    public MultiPredicate addTooltips(Component tooltip) {
        var mutated = mutable ? this : deepCopy();
        mutated.forEach(p -> p.addTooltips(tooltip));
        mutated.forEachChild(mp -> mp.addTooltips(tooltip));
        return mutated;
    }

    @CheckReturnValue
    public MultiPredicate addTooltips(Component... tooltip) {
        var mutated = mutable ? this : deepCopy();
        mutated.forEach(p -> Collections.addAll(p.getAdditionalTooltips(), tooltip));
        mutated.forEachChild(mp -> mp.addTooltips(tooltip));
        return mutated;
    }

    @Override
    public void updateSettings(UnaryOperator<PredicateSettings> configurator) {
        if (!mutable) return;
        if (isSingle()) {
            // the idea is that if we only have a single predicate, we mutate that predicate instead of ourselves
            // as we're basically the same as that predicate
            predicates().get(0).updateSettings(configurator);
            onSettingsChanged();
        } else {
            PredicateSettings settings = getSettings();
            if (settings != null) {
                setSettings(Objects.requireNonNull(configurator.apply(settings)));
            } else {
                // update predicate settings
                forEach(p -> p.updateSettings(configurator));
                // update children
                // if they have settings, they should mutate themselves (non-recursive)
                // otherwise they should mutate their predicates and children instead (recursive)
                forEachChild(mp -> mp.updateSettings(configurator));
                onSettingsChanged();
            }
        }
    }

    protected void onSettingsChanged() {}

    private MultiPredicate markImmutable() {
        this.mutable = false;
        return this;
    }

    public void setSettings(@Nullable PredicateSettings settings) {
        if (!mutable) return;
        this.settings = settings == null ? null : settings.copy();
        onSettingsChanged();
    }

    public MultiPredicate setController(boolean controller) {
        var mutated = mutable ? this : deepCopy();
        mutated.controller = controller;
        return mutated;
    }

    /*
     * MUTATE AND COPY
     */

    @CheckReturnValue
    protected MultiPredicate deepCopy() {
        List<BasePredicate> copiedPredicates = predicates().stream()
                .map(BasePredicate::copy)
                // sort high to low (descending)
                .sorted(Collections.reverseOrder(BasePredicate::compareTo))
                .toList();
        List<MultiPredicate> copiedChildren = children().stream()
                .map(MultiPredicate::deepCopy)
                // sort high to low (descending)
                .sorted(Collections.reverseOrder(MultiPredicate::compareTo))
                .toList();
        MultiPredicate copy = this.type.makePredicate(copiedChildren, copiedPredicates, this.hasAir);
        copy.setSettings(this.settings);
        copy.setController(this.controller);
        return copy;
    }

    @CheckReturnValue
    public MultiPredicate copyWith(Consumer<MultiPredicate> configurator) {
        MultiPredicate copy = deepCopy();
        configurator.accept(copy);
        return copy;
    }

    /// Mark this multipredicate as recursive (`this.settings = null`),
    /// meaning that settings are applied to children instead of itself
    /// @return a copy of this multipredicate with `this.settings = null`
    @CheckReturnValue
    public MultiPredicate recursive() {
        return copyWith(mp -> mp.setSettings(null));
    }

    @Override
    @CheckReturnValue
    public MultiPredicate withSettings(UnaryOperator<PredicateSettings> configurator) {
        return copyWith(p -> p.updateSettings(configurator));
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
        if (isSingle()) return predicates().get(0).toString();
        StringBuilder builder = new StringBuilder();
        if (isController()) builder.append("C");
        builder.append('[');
        var delimiter = switch (this.type) {
            case OR -> " OR ";
            case AND -> " AND ";
            case XOR -> " XOR ";
        };
        StringJoiner joiner = new StringJoiner(delimiter);
        this.forEach(p -> joiner.add(p.toString()));
        this.forEachChild(mp -> joiner.add(mp.toString()));
        builder.append(joiner);
        builder.append(']');
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
    /// @return If {@code b == null || b == EMPTY}, returns {@code a}. <br />
    /// If {@code a == EMPTY}, returns {@code b}. <br />
    /// Otherwise, returns a new MultiPredicate that combines {@code a} and {@code b}
    private static MultiPredicate combine(MultiPredicate a, Logic type, @Nullable MultiPredicate b) {
        if (b == null || b.isEmpty()) return a; // no op
        if (a.isEmpty()) return b;

        List<MultiPredicate> children;
        List<BasePredicate> predicates;
        if (b.isSingle()) {
            predicates = Stream.concat(a.predicates().stream(), Stream.of(b.predicates().get(0)))
                    .map(BasePredicate::copy)
                    .toList();
            children = a.children().stream()
                    .map(MultiPredicate::deepCopy)
                    .toList();
        } else {
            predicates = List.of();
            children = Stream.of(a, b).map(MultiPredicate::deepCopy).toList();
        }

        MultiPredicate combined = type.makePredicate(children, predicates, a.hasAir || b.hasAir);
        combined.setSettings(PredicateSettings.create());
        return combined;
    }

    public static MultiPredicate empty() {
        return EMPTY;
    }

    public static MultiPredicate of(BasePredicate predicate) {
        MultiPredicate multiPredicate = Logic.OR.makePredicate(predicate, predicate == BasePredicate.AIR);
        multiPredicate.setSettings(PredicateSettings.create());
        return multiPredicate;
    }

    /// @return A multi predicate with default settings
    private static MultiPredicate of(Logic type, List<BasePredicate> predicates) {
        MultiPredicate predicate = type.makePredicate(List.of(), predicates, predicates.stream()
                .anyMatch(BasePredicate::isAir));
        predicate.setSettings(PredicateSettings.create());
        return predicate;
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
