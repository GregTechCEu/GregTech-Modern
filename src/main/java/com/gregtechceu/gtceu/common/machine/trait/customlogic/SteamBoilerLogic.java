package com.gregtechceu.gtceu.common.machine.trait.customlogic;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.misc.ItemTransferList;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.gregtechceu.gtceu.data.recipe.GTRecipeTypes.STEAM_BOILER_RECIPES;

@NoArgsConstructor
public class SteamBoilerLogic implements GTRecipeType.ICustomRecipeLogic {

    @Override
    public @Nullable RecipeHolder<GTRecipe> createCustomRecipe(IRecipeCapabilityHolder holder) {
        var itemInputs = Objects
                .requireNonNullElseGet(holder.getCapabilitiesProxy().get(IO.IN, ItemRecipeCapability.CAP),
                        ArrayList::new)
                .stream()
                .filter(IItemHandlerModifiable.class::isInstance).map(IItemHandlerModifiable.class::cast)
                .toArray(IItemHandlerModifiable[]::new);
        var inputs = new ItemTransferList(itemInputs);
        for (int i = 0; i < inputs.getSlots(); ++i) {
            ItemStack input = inputs.getStackInSlot(i);
            if (input.isEmpty() || !FluidTransferHelper.getFluidContained(input).isEmpty()) {
                continue;
            }
            var burnTime = GTUtil.getItemBurnTime(input);
            if (burnTime > 0) {
                return STEAM_BOILER_RECIPES.recipeBuilder(BuiltInRegistries.ITEM.getKey(input.getItem()))
                        .inputItems(input.copyWithCount(1))
                        .duration(burnTime * 12) // remove the * 12 if SteamBoilerMachine:240 is uncommented
                        .build();
            }
        }
        return null;
    }

    @Override
    public @Nullable List<RecipeHolder<GTRecipe>> getRepresentativeRecipes() {
        List<RecipeHolder<GTRecipe>> recipes = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (!FluidTransferHelper.getFluidContained(item.getDefaultInstance()).isEmpty()) {
                continue;
            }
            var burnTime = GTUtil.getItemBurnTime(item.getDefaultInstance());
            if (burnTime > 0) {
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
                RecipeHolder<GTRecipe> recipe = STEAM_BOILER_RECIPES.recipeBuilder(id)
                        .inputItems(item)
                        .duration(burnTime * 12) // remove the * 12 if SteamBoilerMachine:240 is uncommented
                        .build();
                recipes.add(recipe);
            }
        }
        return recipes;
    }
}
