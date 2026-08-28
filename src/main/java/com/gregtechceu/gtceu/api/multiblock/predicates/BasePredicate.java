package com.gregtechceu.gtceu.api.multiblock.predicates;

import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.api.multiblock.PredicateContext;
import com.gregtechceu.gtceu.api.multiblock.error.SinglePredicateError;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.UnaryOperator;

public abstract class BasePredicate implements Comparable<BasePredicate>, SettingsHolder {

    public static final BasePredicate AIR = new PredicateBuilder("Air")
            .predicate(ctx -> ctx.state().isAir())
            .build();

    public static final BasePredicate ANY = new PredicateBuilder("Any")
            .predicate(ctx -> true)
            .build();

    @Getter(lazy = true)
    private final List<BlockInfo> candidates = computeCandidates();

    @Getter
    protected PredicateSettings settings = PredicateSettings.create();

    @Setter
    private @Nullable MultiPredicate parent;

    @Getter
    private final List<Component> additionalTooltips = new ArrayList<>();

    public MultiPredicate getParent() {
        return Objects.requireNonNull(this.parent);
    }

    /// the main testing method
    public abstract boolean test(PredicateContext ctx);

    // this is called after calling getPredicateAtPos()
    public abstract List<Component> getRecipeViewerTooltips(MultiPredicate root);

    public abstract void onError(PredicateContext ctx);

    /// @param root the top-most multi predicate for this multi predicate
    /// @return a list of components to be displayed while hovering over a block in the Multiblock Preview
    public abstract List<Component> getRecipeViewerTooltips(MultiPredicate root);

    /// delegates to {@link MultiPredicate#testMaxCount(BasePredicate, PredicateContext)},
    /// with this predicate as the passing predicate
    public boolean checkMaxCount(PredicateContext context) {
        return getParent().testMaxCount(this, context);
    }

    /// test against global max count
    public boolean testGlobalMax(PredicateContext ctx) {
        int count = ctx.incrementGlobalCount(this);
        if (testGlobalMax(count)) return true;
        ctx.appendError(SinglePredicateError.maxCount(this, count));
        return false;
    }

    /// test against slice max count
    public boolean testSliceMax(PredicateContext ctx) {
        if (!ctx.isCheckLayer()) return true;
        int count = ctx.incrementSliceCount(this);
        if (testSliceMax(count)) return true;
        ctx.appendError(SinglePredicateError.maxLayerCount(this, count));
        return false;
    }

    /// test against global min count
    public boolean testGlobalMin(PredicateContext ctx) {
        if (getMinCount() == -1) return true;
        int count = ctx.getGlobalCount(this);
        if (testGlobalMin(count)) return true;
        ctx.appendError(SinglePredicateError.minCount(this, count));
        return false;
    }

    /// test against slice min count
    public boolean testSliceMin(PredicateContext ctx) {
        if (!ctx.isCheckLayer()) return true;
        int count = ctx.getSliceCount(this);
        if (testSliceMin(count)) return true;
        ctx.appendError(SinglePredicateError.minLayerCount(this, count));
        return false;
    }

    /// computes the candidates for this predicate
    public abstract List<BlockInfo> computeCandidates();

    public List<ItemStack> getCandidateStacks() {
        return getCandidates().stream()
                .filter(BlockInfo::nonAir)
                .map(BlockInfo::getItemStackForm)
                .toList();
    }

    public Optional<BlockInfo> getFirstCandidate() {
        return Optional.of(getCandidates())
                .filter(c -> !c.isEmpty())
                .map(c -> c.get(0));
    }

    /// the type of this predicate
    public abstract String getTypeName();

    /// the contents of this predicate
    protected void appendContents(StringBuilder builder) {}

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getTypeName());
        builder.append('{');
        appendContents(builder);
        builder.append('}');
        return builder.toString();
    }

    @Override
    public int compareTo(BasePredicate o) {
        return this.settings.comparePriority(o.settings);
    }

    // COPY AND MUTATE

    public abstract BasePredicate copy();

    protected void copyTo(BasePredicate other) {
        other.setSettings(this.settings.copy());
        other.additionalTooltips.addAll(this.additionalTooltips);
    }

    @Override
    public BasePredicate withSettings(UnaryOperator<PredicateSettings> configurator) {
        BasePredicate copy = copy();
        copy.updateSettings(configurator);
        return copy;
    }

    @Override
    public BasePredicate withDisableRenderFormed(boolean disableRenderFormed) {
        return withSettings(s -> s.withDisableRenderFormed(disableRenderFormed));
    }

    @Override
    public BasePredicate withPreviewCount(int previewCount) {
        return withSettings(s -> s.withPreviewCount(previewCount));
    }

    @Override
    public BasePredicate withMaxSliceCount(int maxSliceCount) {
        return withSettings(s -> s.withMaxSliceCount(maxSliceCount));
    }

    @Override
    public BasePredicate withMinSliceCount(int minSliceCount) {
        return withSettings(s -> s.withMinSliceCount(minSliceCount));
    }

    @Override
    public BasePredicate withMaxCount(int maxCount) {
        return withSettings(s -> s.withMaxCount(maxCount));
    }

    @Override
    public BasePredicate withMinCount(int minCount) {
        return withSettings(s -> s.withMinCount(minCount));
    }

    @Override
    public BasePredicate withPriority(int priority) {
        return withSettings(s -> s.withPriority(priority));
    }

    @Override
    public void setSettings(PredicateSettings settings) {
        this.settings = settings;
    }

    // MUTATE ONLY

    public void addTooltips(Component tooltip) {
        this.additionalTooltips.add(tooltip);
    }

    public void updateSettings(UnaryOperator<PredicateSettings> configurator) {
        setSettings(configurator.apply(getSettings()));
    }

    @Override
    public void setDisableRenderFormed(boolean disableRenderFormed) {
        updateSettings(s -> s.withDisableRenderFormed(disableRenderFormed));
    }

    @Override
    public void setPreviewCount(int previewCount) {
        updateSettings(s -> s.withPreviewCount(previewCount));
    }

    @Override
    public void setMaxSliceCount(int maxSliceCount) {
        updateSettings(s -> s.withMaxSliceCount(maxSliceCount));
    }

    @Override
    public void setMinSliceCount(int minSliceCount) {
        updateSettings(s -> s.withMinSliceCount(minSliceCount));
    }

    @Override
    public void setMaxCount(int maxCount) {
        updateSettings(s -> s.withMaxCount(maxCount));
    }

    @Override
    public void setMinCount(int minCount) {
        updateSettings(s -> s.withMinCount(minCount));
    }

    @Override
    public void setPriority(int priority) {
        updateSettings(s -> s.withPriority(priority));
    }
}
