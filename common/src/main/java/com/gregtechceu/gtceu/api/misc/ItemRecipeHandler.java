package com.gregtechceu.gtceu.api.misc;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import lombok.Getter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/7/13
 * @implNote ItemRecipeHandler
 */
public class ItemRecipeHandler implements IRecipeHandler<Ingredient> {
    @Getter
    public final IO handlerIO;
    public final ItemStackTransfer storage;

    public ItemRecipeHandler(IO handlerIO, int slots) {
        this.handlerIO = handlerIO;
        this.storage = new ItemStackTransfer(slots);
    }

    @Override
    public List<Ingredient> handleRecipeInner(IO io, GTRecipe recipe, List<Ingredient> left, @Nullable String slotName, boolean simulate) {
        if (io != this.handlerIO) return left;
        var capability = storage;
        Iterator<Ingredient> iterator = left.iterator();
        var lastStatus = simulate ? storage.serializeNBT() : null;
        if (io == IO.IN) {
            while (iterator.hasNext()) {
                Ingredient ingredient = iterator.next();
                SLOT_LOOKUP:
                for (int i = 0; i < capability.getSlots(); i++) {
                    ItemStack itemStack = capability.getStackInSlot(i);
                    //Does not look like a good implementation, but I think it's at least equal to vanilla Ingredient::test
                    if (ingredient.test(itemStack)) {
                        ItemStack[] ingredientStacks = ingredient.getItems();
                        for (ItemStack ingredientStack : ingredientStacks) {
                            if (ingredientStack.is(itemStack.getItem())) {
                                ItemStack extracted = capability.extractItem(i, ingredientStack.getCount(), false);
                                ingredientStack.setCount(ingredientStack.getCount() - extracted.getCount());
                                if (ingredientStack.isEmpty()) {
                                    iterator.remove();
                                    break SLOT_LOOKUP;
                                }
                            }
                        }
                    }
                }
            }
        } else if (io == IO.OUT) {
            while (iterator.hasNext()) {
                Ingredient ingredient = iterator.next();
                ItemStack output = ingredient.getItems()[0];
                if (!output.isEmpty()) {
                    for (int i = 0; i < capability.getSlots(); i++) {
                        ItemStack leftStack = capability.insertItem(i, output.copy(), false);
                        output.setCount(leftStack.getCount());
                        if (output.isEmpty()) break;
                    }
                }
                if (output.isEmpty()) iterator.remove();
            }
        }
        if (lastStatus != null) {
            var lastOnChange = storage.getOnContentsChanged();
            storage.setOnContentsChanged(() -> {});
            storage.deserializeNBT(lastStatus);
            storage.setOnContentsChanged(lastOnChange);
        }
        return left.isEmpty() ? null : left;
    }

    @Override
    public long getTimeStamp() {
        return 0;
    }

    @Override
    public void setTimeStamp(long timeStamp) {

    }

    @Override
    public RecipeCapability<Ingredient> getCapability() {
        return ItemRecipeCapability.CAP;
    }
}
