package com.gregtechceu.gtceu.api.data.chemical.material.properties;

import com.google.common.base.Preconditions;
import com.gregtechceu.gtceu.api.data.chemical.fluid.FluidType;
import com.gregtechceu.gtceu.api.data.chemical.fluid.FluidTypes;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class FluidProperty implements IMaterialProperty<FluidProperty> {

    public static final int BASE_TEMP = 293; // Room Temperature

    private Supplier<? extends Fluid> fluidSupplier;
    private final FluidType fluidType;
    private boolean hasBlock;
    private boolean isGas;
    private int fluidTemperature = BASE_TEMP;
    private int burnTime = -1;
    @Setter
    @Getter
    private ResourceLocation stillTexture, flowTexture;

    public FluidProperty(@Nonnull FluidType fluidType, boolean hasBlock) {
        this.fluidType = fluidType;
        this.isGas = fluidType == FluidTypes.GAS;
        this.hasBlock = hasBlock;
    }

    /**
     * Default values of: no Block, not Gas.
     */
    public FluidProperty() {
        this(FluidTypes.LIQUID, false);
    }

    public boolean isGas() {
        return isGas;
    }

    public boolean hasFluidSupplier() {
        return fluidSupplier != null;
    }

    @Nullable
    public Fluid getFluid() {
        return fluidSupplier.get();
    }

    public void setFluid(Supplier<? extends Fluid> fluidSupplier) {
        this.fluidSupplier = fluidSupplier;
    }

    public boolean hasBlock() {
        return hasBlock;
    }

    public void setHasBlock(boolean hasBlock) {
        this.hasBlock = hasBlock;
    }

    public void setIsGas(boolean isGas) {
        this.isGas = isGas;
    }

    @Nullable
    public FluidStack getFluid(int amount) {
        var fluid = getFluid();
        return fluid == null ? null : FluidStack.create(fluid, amount);
    }

    public void setFluidTemperature(int fluidTemperature) {
        setFluidTemperature(fluidTemperature, true);
    }

    public void setFluidTemperature(int fluidTemperature, boolean isKelvin) {
        if (isKelvin) Preconditions.checkArgument(fluidTemperature >= 0, "Invalid temperature");
        else fluidTemperature += 273;
        this.fluidTemperature = fluidTemperature;
    }

    public void setBurnTime(int burnTime) {
        this.burnTime = burnTime;
    }

    public int getBurnTime() {
        return burnTime;
    }

    public int getFluidTemperature() {
        return fluidTemperature;
    }

    @Nonnull
    public FluidType getFluidType() {
        return this.fluidType;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        if (properties.hasProperty(PropertyKey.PLASMA)) {
            hasBlock = false;
        }
    }

}
