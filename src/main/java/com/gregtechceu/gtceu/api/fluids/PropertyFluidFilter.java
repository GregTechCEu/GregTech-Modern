package com.gregtechceu.gtceu.api.fluids;

import com.gregtechceu.gtceu.api.capability.IPropertyFluidFilter;
import com.gregtechceu.gtceu.api.fluids.attribute.FluidAttribute;
import com.gregtechceu.gtceu.api.fluids.attribute.FluidAttributes;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;

public class PropertyFluidFilter implements IPropertyFluidFilter {

    private final Object2BooleanMap<FluidAttribute> containmentPredicate = new Object2BooleanOpenHashMap<>();

    @Getter
    private final int maxFluidTemperature;
    @Getter
    private final int minFluidTemperature;
    @Getter
    private final boolean gasProof;
    @Getter
    private final boolean plasmaProof;

    public PropertyFluidFilter(int maxFluidTemperature,
                               int minFluidTemperature,
                               boolean gasProof,
                               boolean acidProof,
                               boolean plasmaProof) {
        this.maxFluidTemperature = maxFluidTemperature;
        this.minFluidTemperature = minFluidTemperature;
        this.gasProof = gasProof;
        if (acidProof) setCanContain(FluidAttributes.ACID, true);
        this.plasmaProof = plasmaProof;
    }

    @Override
    public boolean canContain(@NotNull FluidState state) {
        return switch (state) {
            case LIQUID -> true;
            case GAS -> gasProof;
            case PLASMA -> plasmaProof;
        };
    }

    @Override
    public boolean canContain(@NotNull FluidAttribute attribute) {
        return containmentPredicate.getBoolean(attribute);
    }

    @Override
    public void setCanContain(@NotNull FluidAttribute attribute, boolean canContain) {
        containmentPredicate.put(attribute, canContain);
    }

    @Override
    public @NotNull @UnmodifiableView Collection<@NotNull FluidAttribute> getContainedAttributes() {
        return containmentPredicate.keySet();
    }

    @Override
    public String toString() {
        return "SimplePropertyFluidFilter{" +
                "maxFluidTemperature=" + maxFluidTemperature +
                "minFluidTemperature=" + minFluidTemperature +
                ", gasProof=" + gasProof +
                ", plasmaProof=" + plasmaProof +
                ", containmentPredicate=" + containmentPredicate +
                '}';
    }
}
