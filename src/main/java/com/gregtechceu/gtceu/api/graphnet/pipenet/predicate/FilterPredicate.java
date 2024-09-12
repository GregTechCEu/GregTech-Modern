package com.gregtechceu.gtceu.api.graphnet.pipenet.predicate;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.cover.filter.Filter;
import com.gregtechceu.gtceu.api.graphnet.predicate.EdgePredicate;
import com.gregtechceu.gtceu.api.graphnet.predicate.NetPredicateType;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.IPredicateTestObject;

import net.minecraft.nbt.CompoundTag;

import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class FilterPredicate extends EdgePredicate<FilterPredicate, CompoundTag> {

    public static final NetPredicateType<FilterPredicate> TYPE = new NetPredicateType<>(GTCEu.MOD_ID, "Filter",
            FilterPredicate::new, new FilterPredicate());

    @Setter
    private @Nullable Filter<?, ?> sourceFilter;
    @Setter
    private @Nullable Filter<?, ?> targetFilter;

    @Override
    public @NotNull NetPredicateType<FilterPredicate> getType() {
        return TYPE;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        if (sourceFilter != null) tag.put("Source", sourceFilter.saveFilter());
        if (targetFilter != null) tag.put("Target", targetFilter.saveFilter());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains("Source")) {
            sourceFilter = Filter.FilterType.makeNew(nbt.getCompound("Source").getString("type"));
            sourceFilter.loadFilter(nbt.getCompound("Source"));
        } else sourceFilter = null;
        if (nbt.contains("Target")) {
            targetFilter = Filter.FilterType.makeNew(nbt.getCompound("Target").getString("type"));
            targetFilter.loadFilter(nbt.getCompound("Target"));
        } else targetFilter = null;
    }

    @Override
    public boolean andy() {
        return true;
    }

    @Override
    public @NotNull FilterPredicate getNew() {
        return new FilterPredicate();
    }

    @Override
    public boolean test(IPredicateTestObject object) {
        Object test = object.recombine();
        if (sourceFilter != null && !sourceFilter.testGeneric(test)) return false;
        return targetFilter == null || targetFilter.testGeneric(test);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilterPredicate predicate = (FilterPredicate) o;
        return Objects.equals(sourceFilter, predicate.sourceFilter) &&
                Objects.equals(targetFilter, predicate.targetFilter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceFilter, targetFilter);
    }
}
