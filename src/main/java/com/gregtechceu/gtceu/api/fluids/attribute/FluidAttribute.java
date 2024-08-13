package com.gregtechceu.gtceu.api.fluids.attribute;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class FluidAttribute {

    private final ResourceLocation resourceLocation;
    private final Consumer<Consumer<Component>> fluidTooltip;
    private final Consumer<Consumer<Component>> containerTooltip;
    private final int hashCode;

    public FluidAttribute(@NotNull ResourceLocation resourceLocation,
                          @NotNull Consumer<Consumer<@NotNull Component>> fluidTooltip,
                          @NotNull Consumer<Consumer<@NotNull Component>> containerTooltip) {
        this.resourceLocation = resourceLocation;
        this.fluidTooltip = fluidTooltip;
        this.containerTooltip = containerTooltip;
        this.hashCode = resourceLocation.hashCode();
    }

    public @NotNull ResourceLocation getResourceLocation() {
        return resourceLocation;
    }

    public void appendFluidTooltips(@NotNull Consumer<@NotNull Component> tooltip) {
        fluidTooltip.accept(tooltip);
    }

    public void appendContainerTooltips(@NotNull Consumer<@NotNull Component> tooltip) {
        containerTooltip.accept(tooltip);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FluidAttribute that = (FluidAttribute) o;

        return resourceLocation.equals(that.getResourceLocation());
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public @NotNull String toString() {
        return "FluidAttribute{" + resourceLocation + '}';
    }
}
