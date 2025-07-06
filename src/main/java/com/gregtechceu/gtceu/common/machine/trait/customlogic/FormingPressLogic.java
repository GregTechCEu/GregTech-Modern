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

        GTRecipe buildRecipe() {
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
    public @Nullable GTRecipe createCustomRecipe(IRecipeCapabilityHolder holder) {
        var rhlList = holder.getCapabilitiesForIO(IO.IN);
        if (rhlList.isEmpty()) return null;

        List<RecipeHandlerList> distinct = new ArrayList<>();
        List<IRecipeHandler<?>> indistinct = new ArrayList<>();

        for (var rhl : rhlList) {
            if (rhl.isDistinct() && rhl.hasCapability(ItemRecipeCapability.CAP)) {
                distinct.add(rhl);
            } else if (rhl.hasCapability(ItemRecipeCapability.CAP)) {
                indistinct.addAll(rhl.getCapability(ItemRecipeCapability.CAP));
            }
        }

        RecipeData data = new RecipeData();

        for (var rhl : distinct) {
            var stacks = collect(rhl);
            if (stacks.isEmpty()) continue;
            data.mold = ItemStack.EMPTY;
            data.item = ItemStack.EMPTY;
            for (var stack : stacks) {
                boolean isMold = GTItems.SHAPE_MOLD_NAME.isIn(stack);
                if (data.mold.isEmpty() && isMold && stack.hasCustomHoverName()) {
                    data.mold = stack;
                } else if (data.item.isEmpty() && !(isMold && stack.hasCustomHoverName())) {
                    data.item = stack;
                }

                if (data.found()) return data.buildRecipe();
            }
        }

        var stacks = collect(indistinct);
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

    private static List<ItemStack> collect(RecipeHandlerList rhl) {
        return collect(rhl.getCapability(ItemRecipeCapability.CAP));
    }

    private static List<ItemStack> collect(List<IRecipeHandler<?>> handlers) {
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
