package brachy.modularui.integration.jei;

import brachy.modularui.integration.recipeviewer.entry.fluid.FluidEntryList;
import brachy.modularui.integration.recipeviewer.entry.item.ItemEntryList;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import mezz.jei.common.input.ClickableIngredient;
import mezz.jei.common.util.ImmutableRect2i;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

import static brachy.modularui.integration.jei.ModularUIJeiPlugin.jeiHelpers;

public class JeiIngredientHandler {

    public static List<ItemStack> toJeiIngredient(ItemEntryList list) {
        return list.getStacks()
                .stream()
                .filter(stack -> !stack.isEmpty())
                .collect(Collectors.toList());
    }

    public static List<FluidStack> toJeiIngredient(FluidEntryList list) {
        return list.getStacks()
                .stream()
                .filter(stack -> !stack.isEmpty())
                .toList();
    }

    public static <T> @Nullable ClickableIngredient<T> getJEIStackClickable(T stack, int x, int y, int w, int h) {
        return jeiHelpers.getIngredientManager().createTypedIngredient(stack)
                .map(typedIngredient -> new ClickableIngredient<>(typedIngredient, new ImmutableRect2i(x, y, w, h)))
                .orElse(null);
    }

    public static List<ClickableIngredient<ItemStack>> toJeiIngredientClickable(ItemEntryList list, int x, int y, int w, int h) {
        return list.getStacks()
                .stream()
                .filter(stack -> !stack.isEmpty())
                .map(stack -> getJEIStackClickable(stack, x, y, w, h))
                .collect(Collectors.toList());
    }

    public static List<ClickableIngredient<FluidStack>> toJeiIngredientClickable(FluidEntryList list, int x, int y, int w, int h) {
        return list.getStacks()
                .stream()
                .filter(stack -> !stack.isEmpty())
                .map(stack -> getJEIStackClickable(stack, x, y, w, h))
                .toList();
    }
}
