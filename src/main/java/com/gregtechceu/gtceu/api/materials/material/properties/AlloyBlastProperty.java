package com.gregtechceu.gtceu.api.materials.material.properties;

import com.google.common.base.Preconditions;
import com.gregtechceu.gtceu.data.recipes.misc.alloyblast.AlloyBlastRecipeProducer;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.level.material.Fluid;

import org.jetbrains.annotations.NotNull;
import java.util.function.Supplier;

public class AlloyBlastProperty implements IMaterialProperty<AlloyBlastProperty> {

    /**
     * Internal material fluid field
     */
    private Supplier<? extends Fluid> fluidSupplier;
    private int temperature;

    @Getter
    @Setter
    @NotNull
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
    public void setFluid(@NotNull Supplier<? extends Fluid> materialFluid) {
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
