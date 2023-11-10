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

import static com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler.handleIngredient;

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
        return handleIngredient(io, left, simulate, this.handlerIO, storage);
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
