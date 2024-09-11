package com.gregtechceu.gtceu.common.machine.trait.customlogic;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.utils.GTStringUtils;

import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

public class FormingPressLogic implements GTRecipeType.ICustomRecipeLogic {

    @Override
    public @Nullable GTRecipe createCustomRecipe(IRecipeCapabilityHolder holder) {
        var handlers = Objects
                .requireNonNullElseGet(holder.getCapabilitiesProxy().get(IO.IN, ItemRecipeCapability.CAP),
                        Collections::emptyList)
                .stream()
                .filter(NotifiableItemStackHandler.class::isInstance)
                .map(NotifiableItemStackHandler.class::cast)
                .filter(i -> i.getSlots() > 1)
                .collect(Collectors.groupingBy(NotifiableRecipeHandlerTrait::isDistinct));

        if (handlers.isEmpty()) return null;

        // Distinct first, reset our stacks for every inventory
        for (var handler : handlers.getOrDefault(true, Collections.emptyList())) {
            ItemStack mold = ItemStack.EMPTY;
            ItemStack item = ItemStack.EMPTY;
            GTRecipe recipe = findRecipe(mold, item, handler);
            if (recipe != null) return recipe;
        }

        // Non-distinct, return as soon as we find valid items
        ItemStack mold = ItemStack.EMPTY;
        ItemStack item = ItemStack.EMPTY;
        for (var handler : handlers.getOrDefault(false, Collections.emptyList())) {
            GTRecipe recipe = findRecipe(mold, item, handler);
            if (recipe != null) return recipe;
        }

        return null;
    }

    private @Nullable GTRecipe findRecipe(ItemStack mold, ItemStack item, NotifiableItemStackHandler handler) {
        for (int i = 0; i < handler.getSlots(); ++i) {
            if (!mold.isEmpty() && !item.isEmpty()) break;
            var input = handler.getStackInSlot(i);
            if (mold.isEmpty() && input.is(GTItems.SHAPE_MOLD_NAME.asItem())) {
                if (input.hasTag() && input.getTag().contains(ItemStack.TAG_DISPLAY, Tag.TAG_COMPOUND)) {
                    mold = input;
                }
            } else if (item.isEmpty()) {
                item = input;
            }
        }

        if (!mold.isEmpty() && !item.isEmpty()) {
            ItemStack output = item.copyWithCount(1);
            output.setHoverName(mold.getHoverName());
            return GTRecipeTypes.FORMING_PRESS_RECIPES.recipeBuilder(GTStringUtils.itemStackToString(output))
                    .notConsumable(mold)
                    .inputItems(item.copyWithCount(1))
                    .outputItems(output)
                    .duration(40).EUt(4)
                    .buildRawRecipe();
        }
        return null;
    }
}
