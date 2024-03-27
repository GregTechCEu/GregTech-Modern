package com.gregtechceu.gtceu.api.data.chemical.material.properties;

import com.gregtechceu.gtceu.data.recipe.misc.alloyblast.AlloyBlastRecipeProducer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;

import net.minecraft.world.level.material.Fluid;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

@NoArgsConstructor
public class AlloyBlastProperty implements IMaterialProperty<AlloyBlastProperty> {
    public static final Codec<AlloyBlastProperty> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        BuiltInRegistries.FLUID.byNameCodec().optionalFieldOf("fluid", null).xmap(fluid1 -> (Supplier<Fluid>) () -> fluid1, Supplier::get).forGetter(val -> (Supplier<Fluid>) val.fluidSupplier),
        ExtraCodecs.POSITIVE_INT.optionalFieldOf("temperature", -1).forGetter(val -> val.temperature)
    ).apply(instance, AlloyBlastProperty::new));

    /**
     * Internal material fluid field
     */
    private Supplier<? extends Fluid> fluidSupplier;
    @Getter
    private int temperature;

    @Getter
    @Setter
    @NotNull
    private AlloyBlastRecipeProducer recipeProducer = AlloyBlastRecipeProducer.DEFAULT_PRODUCER;

    public AlloyBlastProperty(int temperature) {
        this.temperature = temperature;
    }

    public AlloyBlastProperty(Supplier<? extends Fluid> fluidSupplier, int temperature) {
        this.fluidSupplier = fluidSupplier;
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
}
