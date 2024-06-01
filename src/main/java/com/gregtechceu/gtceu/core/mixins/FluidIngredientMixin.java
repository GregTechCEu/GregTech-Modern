package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.recipe.ingredient.SizedSingleFluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedTagFluidIngredient;
import com.gregtechceu.gtceu.core.ISizedFluidIngredient;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = FluidIngredient.class, remap = false)
public abstract class FluidIngredientMixin implements ISizedFluidIngredient {

    @SuppressWarnings("UnreachableCode")
    @Override
    public int getAmount() {
        FluidIngredient self = (FluidIngredient) (Object) this;

        if (self instanceof SizedTagFluidIngredient tag) {
            return tag.getAmount();
        } else if (self instanceof SizedSingleFluidIngredient single) {
            return single.getAmount();
        }
        return FluidType.BUCKET_VOLUME;
    }

    @SuppressWarnings("UnreachableCode")
    @Override
    public void setAmount(int amount) {
        FluidIngredient self = (FluidIngredient) (Object) this;

        if (self instanceof SizedTagFluidIngredient tag) {
            tag.setAmount(amount);
        } else if (self instanceof SizedSingleFluidIngredient single) {
            single.setAmount(amount);
        }
    }
}
