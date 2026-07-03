package com.gregtechceu.gtceu.common.machine.trait.customlogic;

import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerGroup;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.utils.GTStringUtils;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum FormingPressLogic implements GTRecipeType.ICustomRecipeLogic {

    INSTANCE;

    // Data class so that item data can be kept between searches
    private static class RecipeData {

        ItemStack mold = ItemStack.EMPTY;
        ItemStack item = ItemStack.EMPTY;

        boolean found() {
            return !mold.isEmpty() && !item.isEmpty();
        }

        GTRecipeDefinition buildRecipe() {
            ItemStack output = item.copyWithCount(1);
            output.setHoverName(mold.getHoverName());
            return GTRecipeTypes.FORMING_PRESS_RECIPES.recipeBuilder(GTStringUtils.itemStackToString(output))
                    .notConsumable(mold)
                    .inputItems(item.copyWithCount(1))
                    .outputItems(output)
                    .duration(40).EUt(4)
                    .buildRawRecipe();
        }
    }

    @Override
    public @Nullable GTRecipeDefinition createCustomRecipe(RecipeHandlerGroup holder) {
        var itemHandlers = holder.getInputHandlerMap().getOrDefault(ItemRecipeCapability.CAP, List.of());
        if (itemHandlers.isEmpty()) return null;

        RecipeData data = new RecipeData();
        var stacks = collect(itemHandlers);
        if (stacks.isEmpty()) return null;
        for (var stack : stacks) {
            if (data.mold.isEmpty() && GTItems.SHAPE_MOLD_NAME.isIn(stack) && stack.hasCustomHoverName()) {
                data.mold = stack;
            } else if (data.item.isEmpty()) {
                data.item = stack;
            }

            if (data.found()) return data.buildRecipe();
        }

        return null;
    }

    private static List<ItemStack> collect(List<? extends IRecipeHandler<?>> handlers) {
        if (handlers.isEmpty()) return Collections.emptyList();
        List<ItemStack> list = new ArrayList<>();
        for (var handler : handlers) {
            for (var content : handler.getContents()) {
                if (content instanceof ItemStack stack && !stack.isEmpty()) {
                    list.add(stack);
                }
            }
        }
        return list;
    }

    @Override
    public void buildRepresentativeRecipes() {
        ItemStack press = GTItems.SHAPE_MOLD_NAME.asStack();
        press.setHoverName(Component.translatable("gtceu.forming_press.naming.press"));
        ItemStack toName = new ItemStack(Items.NAME_TAG);
        toName.setHoverName(Component.translatable("gtceu.forming_press.naming.to_name"));
        ItemStack named = new ItemStack(Items.NAME_TAG);
        named.setHoverName(Component.translatable("gtceu.forming_press.naming.named"));
        GTRecipeDefinition recipe = GTRecipeTypes.FORMING_PRESS_RECIPES.recipeBuilder("name_item")
                .notConsumable(press)
                .inputItems(toName)
                .outputItems(named)
                .duration(40)
                .EUt(4)
                .buildRawRecipe();
        // for EMI to detect it's a synthetic recipe (not ever in JSON)
        recipe = recipe.withId(recipe.getId().withPrefix("/"));
        GTRecipeTypes.FORMING_PRESS_RECIPES.addToMainCategory(recipe);
    }
}
