package com.gregtechceu.gtceu.common.machine.trait.customlogic;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.utils.GTStringUtils;

import com.lowdragmc.lowdraglib.misc.ItemTransferList;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;

import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Objects;

public class FormingPressLogic implements GTRecipeType.ICustomRecipeLogic {

    @Override
    public @Nullable GTRecipe createCustomRecipe(IRecipeCapabilityHolder holder) {
        var itemInputs = Objects
                .requireNonNullElseGet(holder.getCapabilitiesProxy().get(IO.IN, ItemRecipeCapability.CAP),
                        ArrayList::new)
                .stream()
                .filter(IItemTransfer.class::isInstance).map(IItemTransfer.class::cast)
                .toArray(IItemTransfer[]::new);

        ItemTransferList inputs = new ItemTransferList(itemInputs);
        if (inputs.getSlots() > 1) {
            ItemStack moldStack = ItemStack.EMPTY;
            ItemStack item = ItemStack.EMPTY;
            for (int i = 0; i < inputs.getSlots(); i++) {
                var inputStack = inputs.getStackInSlot(i);

                if (!moldStack.isEmpty() && !item.isEmpty()) break;

                if (moldStack.isEmpty() && inputStack.is(GTItems.SHAPE_MOLD_NAME.asItem())) {
                    if (inputStack.getTag() != null &&
                            inputStack.getTag().contains(ItemStack.TAG_DISPLAY, Tag.TAG_COMPOUND)) {
                        moldStack = inputStack;
                    }
                } else if (item.isEmpty()) {
                    item = inputStack;
                }
            }

            if (!moldStack.isEmpty() && moldStack.getTag() != null && !item.isEmpty()) {
                ItemStack output = item.copyWithCount(1);

                output.setHoverName(moldStack.getHoverName());
                return GTRecipeTypes.FORMING_PRESS_RECIPES.recipeBuilder(GTStringUtils.itemStackToString(output))
                        .notConsumable(moldStack)
                        .inputItems(item.copyWithCount(1))
                        .outputItems(output)
                        .duration(40).EUt(4)
                        .buildRawRecipe();
            }
        }
        return null;
    }
}
