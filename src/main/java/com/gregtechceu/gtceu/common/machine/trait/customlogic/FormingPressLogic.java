package com.gregtechceu.gtceu.common.machine.trait.customlogic;

import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.utils.GTStringUtils;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import org.jetbrains.annotations.Nullable;

public enum FormingPressLogic implements GTRecipeType.ICustomRecipeLogic {

    INSTANCE;

    @Override
    public @Nullable GTRecipe createCustomRecipe(IRecipeCapabilityHolder holder) {
        var inventoryGroups = ItemRecipeCapability.getInputContentsByGroup(holder);
        if (inventoryGroups.isEmpty()) return null;

        for (Object2LongMap<ItemStack> group : inventoryGroups) {
            if (group.isEmpty()) continue;

            ItemStack mold = ItemStack.EMPTY;
            ItemStack item = ItemStack.EMPTY;
            for (ItemStack stack : group.keySet()) {
                boolean isMold = GTItems.SHAPE_MOLD_NAME.isIn(stack) && stack.has(DataComponents.CUSTOM_NAME);
                if (mold.isEmpty() && isMold) {
                    mold = stack;
                } else if (item.isEmpty() && !isMold) {
                    item = stack;
                }

                if (!mold.isEmpty() && !item.isEmpty()) {
                    ItemStack output = item.copyWithCount(1);
                    output.set(DataComponents.CUSTOM_NAME, mold.getHoverName());
                    return GTRecipeTypes.FORMING_PRESS_RECIPES.recipeBuilder(GTStringUtils.itemStackToString(output))
                            .notConsumable(mold)
                            .inputItems(item.copyWithCount(1))
                            .outputItems(output)
                            .duration(40).EUt(4)
                            .build();
                }
            }
        }

        return null;
    }

    @Override
    public void buildRepresentativeRecipes() {
        ItemStack press = GTItems.SHAPE_MOLD_NAME.asStack();
        press.set(DataComponents.CUSTOM_NAME, Component.translatable("gtceu.forming_press.naming.press"));
        ItemStack toName = new ItemStack(Items.NAME_TAG);
        toName.set(DataComponents.CUSTOM_NAME, Component.translatable("gtceu.forming_press.naming.to_name"));
        ItemStack named = new ItemStack(Items.NAME_TAG);
        named.set(DataComponents.CUSTOM_NAME, Component.translatable("gtceu.forming_press.naming.named"));
        GTRecipe recipe = GTRecipeTypes.FORMING_PRESS_RECIPES.recipeBuilder("name_item")
                .notConsumable(press)
                .inputItems(toName)
                .outputItems(named)
                .duration(40)
                .EUt(4)
                .build();
        // for EMI to detect it's a synthetic recipe (not ever in JSON)
        recipe.setId(recipe.getId().withPrefix("/"));
        GTRecipeTypes.FORMING_PRESS_RECIPES.addToMainCategory(recipe);
    }
}
