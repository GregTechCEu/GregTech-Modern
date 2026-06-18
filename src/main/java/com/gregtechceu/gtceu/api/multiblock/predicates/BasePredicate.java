package com.gregtechceu.gtceu.api.multiblock.predicates;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.multiblock.PredicateContext;
import com.gregtechceu.gtceu.api.multiblock.error.SinglePredicateError;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.data.lang.LangHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

@Accessors(chain = true)
public abstract class BasePredicate {

    public static final BasePredicate AIR = new BasePredicate() {

        @Override
        public String getTypeName() {
            return "Air";
        }
    };

    public static final BasePredicate ANY = new BasePredicate() {

        @Override
        public String getTypeName() {
            return "Any";
        }
    };

    protected static final Comparator<BasePredicate> PREDICATE_COMPARATOR = Comparator.comparingInt(BasePredicate::getPriority);
    private @Nullable List<BlockInfo> candidates;
    private @Nullable List<Component> tooltips;
    @Getter
    @Setter
    private int priority = 0;
    @Getter
    @Setter
    private int minCount = -1;
    @Getter
    @Setter
    private int maxCount = -1;
    @Getter
    @Setter
    private int minSliceCount = -1;
    @Getter
    @Setter
    private int maxSliceCount = -1;
    @Getter
    @Setter
    private int previewCount = -1;
    @Getter
    @Setter
    private boolean disableRenderFormed = false;
    @Getter
    @Setter
    private @Nullable String nbtParser; // unsure what this does

    /// custom testing logic, usually checking if blockstate/entity is correct
    public boolean testInternal(PredicateContext ctx) {
        return true;
    }

    /// computes the candidates for this predicate, lazily initialized
    public List<BlockInfo> computeCandidates() {
        return Collections.emptyList();
    }

    /// the type of this predicate
    public abstract @Nullable String getTypeName();

    /// the contents of this predicate
    protected String getContents() {
        return "";
    }

    /// the main testing method
    public boolean test(PredicateContext ctx) {
        return testInternal(ctx) && testGlobal(ctx) && testLayer(ctx);
    }

    /// test against global max count
    public boolean testGlobal(PredicateContext ctx) {
        ctx.globalCache().merge(this, 1, Integer::sum);
        if ((minCount == -1 && maxCount == -1) || ctx.layerCache() == null) return true;
        int count = ctx.globalCache().getInt(this);
        if (maxCount == -1 || count <= maxCount) return true;
        return ctx.error(SinglePredicateError.maxCount(this, count));
    }

    /// test against slice max count
    public boolean testLayer(PredicateContext ctx) {
        if (ctx.layerCache() == null) return true;
        ctx.layerCache().mergeInt(this, 1, Integer::sum);
        if ((minSliceCount == -1 && maxSliceCount == -1)) return true;
        int count = ctx.layerCache().getInt(this);
        if (maxSliceCount == -1 || count <= maxSliceCount) return true;
        return ctx.error(SinglePredicateError.maxLayerCount(this, count));
    }

    public boolean isAir() {
        return this == AIR;
    }

    public boolean isAny() {
        return this == ANY;
    }

    /// used for tooltips
    public boolean hasAir() {
        return isAir();
    }

    /// used for tooltips
    public boolean isSingle() {
        return true;
    }

    public boolean isController() {
        return false;
    }

    public BasePredicate setMinGlobalLimited(int min) {
        return this.setMinCount(min);
    }

    public BasePredicate setMinGlobalLimited(int min, int previewCount) {
        return this.setMinCount(min).setPreviewCount(previewCount);
    }

    public BasePredicate setMaxGlobalLimited(int max) {
        return this.setMaxCount(max);
    }

    public BasePredicate setMaxGlobalLimited(int max, int previewCount) {
        return this.setMaxCount(max).setPreviewCount(previewCount);
    }

    public BasePredicate setGlobalMinMax(int min, int max) {
        return this.setMinCount(min).setMaxCount(max);
    }

    public BasePredicate setMinLayerLimited(int min) {
        return this.setMinSliceCount(min);
    }

    public BasePredicate setMinLayerLimited(int min, int previewCount) {
        return this.setMinSliceCount(min).setPreviewCount(previewCount);
    }

    public BasePredicate setMaxLayerLimited(int max) {
        return this.setMaxSliceCount(max);
    }

    public BasePredicate setMaxLayerLimited(int max, int previewCount) {
        return this.setMaxSliceCount(max).setPreviewCount(previewCount);
    }

    public BasePredicate setLayerMinMax(int min, int max) {
        return this.setMinSliceCount(min).setMaxSliceCount(max);
    }

    /**
     * Sets the Minimum and Maximum limit to the passed value
     *
     * @param limit The Maximum and Minimum limit
     */
    public BasePredicate setExactLimit(int limit) {
        return this.setMinCount(limit).setMaxCount(limit);
    }

    public BasePredicate disabledRenderFormed() {
        return setDisableRenderFormed(true);
    }

    public List<Component> additionalTooltips() {
        if (this.tooltips == null) {
            this.tooltips = new ArrayList<>();
        }
        return this.tooltips;
    }

    public BasePredicate addTooltips(Component tooltip) {
        this.additionalTooltips().add(tooltip);
        return this;
    }

    public BasePredicate addTooltips(Component... tooltips) {
        Collections.addAll(this.additionalTooltips(), tooltips);
        return this;
    }

    @OnlyIn(Dist.CLIENT)
    public List<Component> getTooltips(@Nullable BasePredicate predicate) {
        List<Component> result = new ArrayList<>();
        if (tooltips != null) {
            result.addAll(tooltips);
        }
        if (minCount == maxCount && maxCount != -1) {
            result.add(Component.translatable("gtceu.multiblock.pattern.error.limited_exact", minCount));
        } else if (minCount != maxCount && minCount != -1 && maxCount != -1) {
            result.add(Component.translatable("gtceu.multiblock.pattern.error.limited_within", minCount, maxCount));
        } else {
            if (minCount != -1) {
                result.add(LangHandler.getFromMultiLang("gtceu.multiblock.pattern.error.limited", 1, minCount));
            }
            if (maxCount != -1) {
                result.add(LangHandler.getFromMultiLang("gtceu.multiblock.pattern.error.limited", 0, maxCount));
            }
        }
        if (predicate == null) return result;
        if (predicate.isSingle()) {
            result.add(Component.translatable("gtceu.multiblock.pattern.single"));
        }
        if (predicate.hasAir()) {
            result.add(Component.translatable("gtceu.multiblock.pattern.replaceable_air"));
        }
        return result;
    }

    public List<ItemStack> getCandidateStacks() {
        return getCandidates().stream()
                .filter(BlockInfo::nonAir)
                .map(info -> {
                    if (GTCEu.isClientSide()) {
                        Level level = Objects.requireNonNull(Minecraft.getInstance().level);
                        return info.getItemStackForm(level, BlockPos.ZERO);
                    }

                    return info.getItemStackForm();
                })
                .toList();
    }

    public List<BlockInfo> getCandidates() {
        if (candidates == null) {
            candidates = computeCandidates();
        }
        return candidates;
    }

    public Optional<BlockInfo> getFirstCandidate() {
        return Optional.of(getCandidates())
                .filter(c -> !c.isEmpty())
                .map(c -> c.get(0));
    }

    public void visit(Consumer<BasePredicate> visitor) {
        visitor.accept(this);
    }

    public final List<BasePredicate> expand() {
        List<BasePredicate> result = new ArrayList<>();
        visit(result::add);
        result.sort(PREDICATE_COMPARATOR);
        return result;
    }

    @Override
    public String toString() {
        return getTypeName() + "{" + getContents() + "}";
    }

    public BasePredicate or(BasePredicate other) {
        return or(this, other);
    }

    public BasePredicate and(BasePredicate other) {
        return and(this, other);
    }

    private static BasePredicate or(BasePredicate a, BasePredicate b) {
        if (a instanceof MultiPredicate mp) {
            return mp.or(b);
        } else {
            return or(null, List.of(a, b));
        }
    }

    private static BasePredicate and(BasePredicate a, BasePredicate b) {
        if (a instanceof MultiPredicate mp) {
            return mp.and(b);
        } else {
            return and(null, List.of(a, b));
        }
    }

    public static BasePredicate or(@Nullable String debugName, Iterable<BasePredicate> predicates) {
        return new MultiPredicate(debugName, predicates, MultiPredicate.Logic.OR);
    }

    public static BasePredicate and(@Nullable String debugName, Iterable<BasePredicate> predicates) {
        return new MultiPredicate(debugName, predicates, MultiPredicate.Logic.AND);
    }
}
