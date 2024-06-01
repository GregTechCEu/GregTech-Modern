package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.Holder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SingleFluidIngredient;

import java.util.stream.Stream;

public class SizedSingleFluidIngredient extends SingleFluidIngredient {
    public static final MapCodec<SizedSingleFluidIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            FluidStack.FLUID_NON_EMPTY_CODEC.fieldOf("fluid").forGetter(SizedSingleFluidIngredient::fluid),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("amount").forGetter(SizedSingleFluidIngredient::getAmount)
    ).apply(instance, SizedSingleFluidIngredient::new));

    @Getter @Setter
    private int amount;

    public SizedSingleFluidIngredient(Holder<Fluid> fluid, int amount) {
        super(fluid);
        this.amount = amount;
    }


    public SizedSingleFluidIngredient(FluidStack fluidStack) {
        super(fluidStack.getFluidHolder());
        this.amount = fluidStack.getAmount();
    }

    @Override
    protected Stream<FluidStack> generateStacks() {
        return Stream.of(new FluidStack(fluid(), amount));
    }

    public SizedSingleFluidIngredient copy() {
        return new SizedSingleFluidIngredient(fluid(), getAmount());
    }
}
