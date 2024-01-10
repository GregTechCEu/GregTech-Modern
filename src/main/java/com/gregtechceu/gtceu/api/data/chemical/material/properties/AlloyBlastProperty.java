package com.gregtechceu.gtceu.api.data.chemical.material.properties;

import com.google.common.base.Preconditions;
import com.gregtechceu.gtceu.data.recipe.misc.alloyblast.AlloyBlastRecipeProducer;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.level.material.Fluid;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class AlloyBlastProperty implements IMaterialProperty<AlloyBlastProperty> {

    /**
     * Internal material fluid field
     */
    private Supplier<? extends Fluid> fluidSupplier;
    private int temperature;

    @Getter
    @Setter
    @Nonnull
    private AlloyBlastRecipeProducer recipeProducer = AlloyBlastRecipeProducer.DEFAULT_PRODUCER;

    public AlloyBlastProperty(int temperature) {
        this.temperature = temperature;
    }

    @Override
    public void verifyProperty(MaterialProperties materialProperties) {
        materialProperties.ensureSet(PropertyKey.BLAST);
        materialProperties.ensureSet(PropertyKey.FLUID);
        this.temperature = materialProperties.getProperty(PropertyKey.BLAST).getBlastTemperature();
    }

    /**
     * internal usage only
     */
    public void setFluid(@Nonnull Supplier<? extends Fluid> materialFluid) {
        Preconditions.checkNotNull(materialFluid);
        this.fluidSupplier = materialFluid;
    }

    public Fluid getFluid() {
        return fluidSupplier.get();
    }

    public void setTemperature(int fluidTemperature) {
        Preconditions.checkArgument(fluidTemperature > 0, "Invalid temperature");
        this.temperature = fluidTemperature;
    }

    public int getTemperature() {
        return temperature;
    }
}
