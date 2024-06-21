package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.GTHashMaps;
import com.gregtechceu.gtceu.utils.ItemStackHashStrategy;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.gregtechceu.gtceu.utils.IngredientEquality.INGREDIENT_COMPARATOR;

public class AssemblyLineMachine extends WorkableElectricMultiblockMachine {
    public AssemblyLineMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        if(ConfigHolder.INSTANCE.machines.orderedAssemblyLineItems) {

            var recipeInputs = recipe.inputs.get(ItemRecipeCapability.CAP);
            var itemInputInventory = Objects.requireNonNullElseGet(getCapabilitiesProxy().get(IO.IN, ItemRecipeCapability.CAP), Collections::<IRecipeHandler<?>>emptyList)
                    .stream()
                    .filter(handler -> !handler.isProxy())
                    .map(container -> container.getContents().stream().filter(ItemStack.class::isInstance)
                            .map(ItemStack.class::cast).toList())
                    .filter(container -> !container.isEmpty())
                    .toList();

            if(itemInputInventory.size() < recipeInputs.size()) return false;

            for( int i = 0; i < recipeInputs.size(); i++) {
                var itemStack = itemInputInventory.get(i).get(0);
                ItemStack recipeStack = ItemRecipeCapability.CAP.of(recipeInputs.get(i).content).getItems()[0];
                if(INGREDIENT_COMPARATOR.compare(Ingredient.of(recipeStack), Ingredient.of(itemStack)) == 0) {
                    return false;
                }
            }

            if(ConfigHolder.INSTANCE.machines.orderedAssemblyLineFluids) {
                recipeInputs = recipe.inputs.get(FluidRecipeCapability.CAP);
                var itemFluidInventory = getCapabilitiesProxy().get(IO.IN, FluidRecipeCapability.CAP);

                if (itemFluidInventory.size() < recipeInputs.size()) return false;

                for (int i = 0; i < recipeInputs.size(); i++) {
                    var fluidStack = (FluidStack) itemFluidInventory.get(i).getContents().get(0);
                    FluidStack recipeStack = FluidRecipeCapability.CAP.of(recipeInputs.get(i).content).getStacks()[0];
                    if (recipeStack.getFluid() != fluidStack.getFluid() || fluidStack.getAmount() < recipeStack.getAmount()) {
                        return false;
                    }
                }
            }
        }
        return super.beforeWorking(recipe);
    }
}
