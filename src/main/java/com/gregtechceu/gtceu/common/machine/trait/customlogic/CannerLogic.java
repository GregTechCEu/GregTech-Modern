package com.gregtechceu.gtceu.common.machine.trait.customlogic;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.utils.GTStringUtils;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import org.jetbrains.annotations.Nullable;

import static net.minecraftforge.fluids.capability.IFluidHandler.*;

public class CannerLogic implements GTRecipeType.ICustomRecipeLogic {

    @Override
    public @Nullable GTRecipe createCustomRecipe(IRecipeCapabilityHolder holder) {
        // TODO: Make this respect distinctness while searching
        var itemInputs = holder.getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP);
        var fluidInputs = holder.getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP);
        for (var itemInput : itemInputs) {
            for (var obj : itemInput.getContents()) {
                if (!(obj instanceof ItemStack stack)) continue;
                if (stack.isEmpty()) continue;
                var singleStack = stack.copyWithCount(1);
                var copy = stack.copyWithCount(1);
                var fluidHandler = FluidUtil.getFluidHandler(copy).resolve().orElse(null);
                if (fluidHandler == null) continue;
                // Try to drain first
                var fluid = fluidHandler.drain(Integer.MAX_VALUE, FluidAction.EXECUTE);
                if (!fluid.isEmpty()) {
                    return GTRecipeTypes.CANNER_RECIPES
                            .recipeBuilder("drain_" + GTStringUtils.itemStackToString(singleStack))
                            .inputItems(singleStack)
                            .outputItems(fluidHandler.getContainer())
                            .outputFluids(fluid)
                            .duration(Math.max(16, fluid.getAmount() / 64))
                            .EUt(4)
                            .buildRawRecipe();
                }
                // Nothing to drain, so try to fill
                for (var fluidInput : fluidInputs) {
                    for (var obj2 : fluidInput.getContents()) {
                        if (!(obj2 instanceof FluidStack fluidStack)) continue;
                        if (fluidStack.isEmpty()) continue;
                        var filled = fluidHandler.fill(fluidStack, FluidAction.EXECUTE);
                        if (filled > 0) {
                            var copyFluid = new FluidStack(fluidStack, filled);
                            return GTRecipeTypes.CANNER_RECIPES
                                    .recipeBuilder("fill_" + GTStringUtils.itemStackToString(singleStack))
                                    .inputItems(singleStack)
                                    .inputFluids(copyFluid)
                                    .outputItems(fluidHandler.getContainer())
                                    .duration(Math.max(16, filled / 64))
                                    .EUt(4)
                                    .buildRawRecipe();
                        }
                    }
                }
            }
        }
        return null;
    }
}
