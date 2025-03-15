package com.gregtechceu.gtceu.common.machine.trait.customlogic;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.utils.GTStringUtils;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FormingPressLogic implements GTRecipeType.ICustomRecipeLogic {

    // Data class so that item data can be kept between searches
    private static class RecipeData {

        public ItemStack mold = ItemStack.EMPTY;
        public ItemStack item = ItemStack.EMPTY;

        public boolean found() {
            return !mold.isEmpty() && !item.isEmpty();
        }

        public void clear() {
            mold = ItemStack.EMPTY;
            item = ItemStack.EMPTY;
        }

        public boolean isEmpty() {
            return mold.isEmpty() && item.isEmpty();
        }
    }

    @Override
    public @Nullable GTRecipe createCustomRecipe(IRecipeCapabilityHolder holder) {
        var handlerLists = holder.getCapabilitiesForIO(IO.IN).stream()
                .filter(rhl -> rhl.hasCapability(ItemRecipeCapability.CAP))
                .collect(Collectors.partitioningBy(RecipeHandlerList::isDistinct));

        if (handlerLists.isEmpty()) return null;

        RecipeData data = new RecipeData();

        // Distinct first, reset our stacks for every inventory
        for (var handlerList : handlerLists.getOrDefault(true, Collections.emptyList())) {
            data.clear();
            GTRecipe recipe = search(data, handlerList.getCapability(ItemRecipeCapability.CAP));
            if (recipe != null) return recipe;
        }

        data.clear();
        // Non-distinct, return as soon as we find valid items
        for (var handlerList : handlerLists.getOrDefault(false, Collections.emptyList())) {
            GTRecipe recipe = search(data, handlerList.getCapability(ItemRecipeCapability.CAP));
            if (recipe != null) return recipe;
        }

        if (data.isEmpty()) return null;

        // If we found one of the two, search for the other in the distinct handlers.
        ItemStack existingMold = data.mold;
        ItemStack existingItem = data.item;
        for (var handlerList : handlerLists.getOrDefault(true, Collections.emptyList())) {
            data.mold = existingMold;
            data.item = existingItem;
            GTRecipe recipe = search(data, handlerList.getCapability(ItemRecipeCapability.CAP));
            if (recipe != null) return recipe;
        }

        return null;
    }

    private @Nullable GTRecipe search(final RecipeData data, List<IRecipeHandler<?>> recipeHandlers) {
        for (var rh : recipeHandlers) {
            for (var obj : rh.getContents()) {
                if (!(obj instanceof ItemStack stack)) continue;
                if (stack.isEmpty()) continue;
                // Skip programmed circuits to avoid using circuit inventory - TODO: Think of better way to skip it
                if (GTItems.PROGRAMMED_CIRCUIT.isIn(stack)) continue;
                if (data.mold.isEmpty() && GTItems.SHAPE_MOLD_NAME.isIn(stack) && stack.hasCustomHoverName()) {
                    data.mold = stack;
                } else if (data.item.isEmpty()) {
                    data.item = stack;
                }
                if (data.found()) break;
            }
            if (data.found()) break;
        }

        if (data.found()) {
            ItemStack output = data.item.copyWithCount(1);
            output.setHoverName(data.mold.getHoverName());
            return GTRecipeTypes.FORMING_PRESS_RECIPES.recipeBuilder(GTStringUtils.itemStackToString(output))
                    .notConsumable(data.mold)
                    .inputItems(data.item.copyWithCount(1))
                    .outputItems(output)
                    .duration(40).EUt(4)
                    .buildRawRecipe();
        }

        return null;
    }

    @Override
    public void buildRepresentativeRecipes() {
        ItemStack press = GTItems.SHAPE_MOLD_NAME.asStack();
        press.setHoverName(Component.translatable("gtceu.forming_press.naming.press"));
        ItemStack toName = new ItemStack(Items.NAME_TAG);
        toName.setHoverName(Component.translatable("gtceu.forming_press.naming.to_name"));
        ItemStack named = new ItemStack(Items.NAME_TAG);
        named.setHoverName(Component.translatable("gtceu.forming_press.naming.named"));
        GTRecipe recipe = GTRecipeTypes.FORMING_PRESS_RECIPES.recipeBuilder("name_item")
                .notConsumable(press)
                .inputItems(toName)
                .outputItems(named)
                .duration(40)
                .EUt(4)
                .buildRawRecipe();
        // for EMI to detect it's a synthetic recipe (not ever in JSON)
        recipe.setId(recipe.getId().withPrefix("/"));
        GTRecipeTypes.FORMING_PRESS_RECIPES.addToMainCategory(recipe);
    }
}
