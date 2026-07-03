package com.gregtechceu.gtceu.common.machine.trait.customlogic;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerGroup;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.minecraftforge.fluids.capability.IFluidHandler.*;

public enum CannerLogic implements GTRecipeType.ICustomRecipeLogic {

    INSTANCE;

    @Override
    public @Nullable GTRecipeDefinition createCustomRecipe(RecipeHandlerGroup holder) {
        var itemHandlers = holder.getInputHandlerMap().getOrDefault(ItemRecipeCapability.CAP, List.of());
        var fluidHandlers = holder.getInputHandlerMap().getOrDefault(FluidRecipeCapability.CAP, List.of());
        if (itemHandlers.isEmpty()) return null;

        List<ItemStack> itemStacks = new ArrayList<>();
        List<FluidStack> fluidStacks = new ArrayList<>();
        collect(itemHandlers, fluidHandlers, itemStacks, fluidStacks);
        if (itemStacks.isEmpty()) return null;

        for (var itemStack : itemStacks) {
            var single = itemStack.copyWithCount(1);
            var copy = itemStack.copyWithCount(1);
            var fluidHandler = FluidUtil.getFluidHandler(copy).resolve().orElse(null);
            if (fluidHandler == null) continue;
            // Try to drain first
            var fluid = fluidHandler.drain(Integer.MAX_VALUE, FluidAction.EXECUTE);
            if (!fluid.isEmpty()) {
                return GTRecipeTypes.CANNER_RECIPES
                        .recipeBuilder("drain_fluid")
                        .inputItems(single)
                        .outputItems(fluidHandler.getContainer())
                        .outputFluids(fluid)
                        .duration(Math.max(16, fluid.getAmount() / 64))
                        .EUt(4)
                        .buildRawRecipe();
            }

            for (var fluidStack : fluidStacks) {
                var fluidCopy = fluidStack.copy();
                var filled = fluidHandler.fill(fluidCopy, FluidAction.EXECUTE);
                if (filled == 0) continue;
                fluidCopy.setAmount(filled);
                return GTRecipeTypes.CANNER_RECIPES
                        .recipeBuilder("fill_fluid")
                        .inputItems(single)
                        .inputFluids(fluidCopy)
                        .outputItems(fluidHandler.getContainer())
                        .duration(Math.max(16, filled / 64))
                        .EUt(4)
                        .buildRawRecipe();
            }
        }

        return null;
    }

    private static void collect(List<? extends IRecipeHandler<?>> itemHandlers,
                                List<? extends IRecipeHandler<?>> fluidHandlers,
                                List<ItemStack> itemStacks, List<FluidStack> fluidStacks) {
        for (var handler : itemHandlers) {
            for (var content : handler.getContents()) {
                if (content instanceof ItemStack stack && !stack.isEmpty()) {
                    itemStacks.add(stack);
                }
            }
        }

        for (var handler : fluidHandlers) {
            for (var content : handler.getContents()) {
                if (content instanceof FluidStack stack && !stack.isEmpty()) {
                    fluidStacks.add(stack);
                }
            }
        }
    }
}
