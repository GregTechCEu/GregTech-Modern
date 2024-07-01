package com.gregtechceu.gtceu.common.machine.trait.customlogic;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.data.item.GTItems;
import com.gregtechceu.gtceu.data.recipe.GTRecipeTypes;
import com.gregtechceu.gtceu.utils.GTStringUtils;

import com.lowdragmc.lowdraglib.misc.ItemTransferList;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.items.IItemHandlerModifiable;
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
                .filter(IItemHandlerModifiable.class::isInstance).map(IItemHandlerModifiable.class::cast)
                .toArray(IItemHandlerModifiable[]::new);

        ItemTransferList inputs = new ItemTransferList(itemInputs);
        if (inputs.getSlots() > 1) {
            ItemStack moldStack = ItemStack.EMPTY;
            ItemStack item = ItemStack.EMPTY;
            for (int i = 0; i < inputs.getSlots(); i++) {
                var inputStack = inputs.getStackInSlot(i);

                if (!moldStack.isEmpty() && !item.isEmpty()) break;

                if (moldStack.isEmpty() && inputStack.is(GTItems.SHAPE_MOLD_NAME.asItem())) {
                    if (inputStack.has(DataComponents.CUSTOM_NAME)) {
                        moldStack = inputStack;
                    }
                } else if (item.isEmpty()) {
                    item = inputStack;
                }
            }

            if (!moldStack.isEmpty() && !item.isEmpty()) {
                ItemStack output = item.copyWithCount(1);

                output.set(DataComponents.CUSTOM_NAME, moldStack.getHoverName());
                return GTRecipeTypes.FORMING_PRESS_RECIPES.recipeBuilder(GTStringUtils.itemStackToString(output))
                        .notConsumable(moldStack)
                        .inputItems(item.copyWithCount(1))
                        .outputItems(output)
                        .duration(40).EUt(4)
                        .build();
            }
        }
        return null;
    }
}
