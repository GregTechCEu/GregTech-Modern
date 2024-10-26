package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class AssemblyLineMachine extends WorkableElectricMultiblockMachine {

    public AssemblyLineMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        if (ConfigHolder.INSTANCE.machines.orderedAssemblyLineItems) {

            var recipeInputs = recipe.inputs.get(ItemRecipeCapability.CAP);
            var itemInputInventory = Objects
                    .requireNonNullElseGet(getCapabilitiesProxy().get(IO.IN, ItemRecipeCapability.CAP),
                            Collections::<IRecipeHandler<?>>emptyList)
                    .stream()
                    .filter(handler -> !handler.isProxy())
                    .map(container -> container.getContents().stream().filter(ItemStack.class::isInstance)
                            .map(ItemStack.class::cast).toList())
                    .filter(container -> !container.isEmpty())
                    .toList();

            if (itemInputInventory.size() < recipeInputs.size()) return false;

            for (int i = 0; i < recipeInputs.size(); i++) {
                var itemStack = itemInputInventory.get(i).get(0);
                Ingredient recipeStack = ItemRecipeCapability.CAP.of(recipeInputs.get(i).content);
                if (!recipeStack.test(itemStack)) {
                    return false;
                }
            }

            if (ConfigHolder.INSTANCE.machines.orderedAssemblyLineFluids) {
                recipeInputs = recipe.inputs.get(FluidRecipeCapability.CAP);
                var itemFluidInventory = Objects
                        .requireNonNullElseGet(getCapabilitiesProxy().get(IO.IN, FluidRecipeCapability.CAP),
                                Collections::<IRecipeHandler<?>>emptyList)
                        .stream()
                        .map(container -> container.getContents().stream().filter(FluidStack.class::isInstance)
                                .map(FluidStack.class::cast).toList())
                        .filter(container -> !container.isEmpty())
                        .toList();

                if (itemFluidInventory.size() < recipeInputs.size()) return false;

                for (int i = 0; i < recipeInputs.size(); i++) {
                    var fluidStack = (FluidStack) itemFluidInventory.get(i).get(0);
                    FluidIngredient recipeStack = FluidRecipeCapability.CAP.of(recipeInputs.get(i).content);
                    if (!recipeStack.test(fluidStack) || recipeStack.getAmount() > fluidStack.getAmount()) {
                        return false;
                    }
                }
            }
        }
        return super.beforeWorking(recipe);
    }

    @Override
    public void onStructureFormed() {
        getDefinition().setPartSorter(Comparator.comparing(it -> multiblockPartSorter().apply(it.self().getPos())));
        super.onStructureFormed();
    }

    private Function<BlockPos, Integer> multiblockPartSorter() {
        return RelativeDirection.RIGHT.getSorter(getFrontFacing(), getUpwardsFacing(), isFlipped());
    }
}
