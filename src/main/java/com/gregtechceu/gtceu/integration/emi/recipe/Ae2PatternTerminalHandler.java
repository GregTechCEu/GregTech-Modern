package com.gregtechceu.gtceu.integration.emi.recipe;

import com.gregtechceu.gtceu.integration.emi.multipage.MultiblockInfoEmiRecipe;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.integration.modules.jeirei.EncodingHelper;
import appeng.menu.me.items.PatternEncodingTermMenu;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.screen.RecipeScreen;

import java.util.ArrayList;
import java.util.List;

public class Ae2PatternTerminalHandler<T extends PatternEncodingTermMenu> implements EmiRecipeHandler<T> {

    private List<Slot> getInputSources(T handler) {
        return handler.slots;
    }

    @Override
    public EmiPlayerInventory getInventory(AbstractContainerScreen<T> screen) {
        return new EmiPlayerInventory(
                getInputSources(screen.getMenu()).stream().map(Slot::getItem).map(EmiStack::of).toList());
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe instanceof GTEmiRecipe || recipe instanceof MultiblockInfoEmiRecipe;
    }

    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<T> context) {
        return true;
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<T> context) {
        T menu = context.getScreenHandler();
        EncodingHelper.encodeProcessingRecipe(menu,
                ofInputs(recipe),
                ofOutputs(recipe));
        if (Minecraft.getInstance().screen instanceof RecipeScreen e) {
            e.onClose();
        }
        return true;
    }

    public static List<List<GenericStack>> ofInputs(EmiRecipe emiRecipe) {
        return emiRecipe.getInputs()
                .stream()
                .map(Ae2PatternTerminalHandler::intoGenericStack)
                .toList();
    }

    public static List<GenericStack> ofOutputs(EmiRecipe emiRecipe) {
        return emiRecipe.getOutputs()
                .stream()
                .flatMap(slot -> intoGenericStack(slot).stream().limit(1))
                .toList();
    }

    private static List<GenericStack> intoGenericStack(EmiIngredient ingredient) {
        if (ingredient.isEmpty()) {
            return new ArrayList<>();
        }
        return ingredient.getEmiStacks().stream().map(stack -> fromEmiStack(stack, ingredient.getAmount())).toList();
    }

    private static GenericStack fromEmiStack(EmiStack stack, long amount) {
        if (stack.getKey() instanceof Item item) {
            return new GenericStack(AEItemKey.of(item.getDefaultInstance()), amount);
        } else if (stack.getKey() instanceof Fluid fluid) {
            return new GenericStack(AEFluidKey.of(fluid), amount);
        }
        return new GenericStack(AEItemKey.of(ItemStack.EMPTY), 0);
    }
}
