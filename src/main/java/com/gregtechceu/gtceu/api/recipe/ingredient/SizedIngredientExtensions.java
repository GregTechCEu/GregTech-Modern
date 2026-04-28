package com.gregtechceu.gtceu.api.recipe.ingredient;

import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import org.jetbrains.annotations.Nullable;

public class SizedIngredientExtensions {

    public static SizedFluidIngredient shrink(SizedFluidIngredient self, int amount) {
        return new SizedFluidIngredient(self.ingredient(), self.amount() - amount);
    }

    public static SizedFluidIngredient copy(SizedFluidIngredient self) {
        FluidIngredient ingredient = self.ingredient();
        if (ingredient instanceof IntProviderFluidIngredient intProv) {
            ingredient = intProv.copy();
        }
        return new SizedFluidIngredient(ingredient, self.amount());
    }

    public static SizedFluidIngredient copyWithAmount(SizedFluidIngredient self, int amount) {
        return new SizedFluidIngredient(self.ingredient(), amount);
    }

    public static IntProvider getCountProvider(SizedFluidIngredient self) {
        if (self.ingredient() instanceof IRangedIngredient rangedIngredient) {
            return rangedIngredient.getCountProvider();
        }
        return ConstantInt.of(self.amount());
    }

    public static FluidStack[] getFluids(SizedFluidIngredient self) {
        return self.ingredient().fluids().stream()
                .map(fluid -> new FluidStack(fluid, self.amount()))
                .toArray(FluidStack[]::new);
    }

    public static @Nullable ICustomIngredient getContainedCustom(SizedIngredient self) {
        return self.ingredient().getCustomIngredient();
    }

    public static SizedIngredient shrink(SizedIngredient self, int count) {
        return new SizedIngredient(self.ingredient(), self.count() - count);
    }

    public static SizedIngredient copy(SizedIngredient self) {
        if (getContainedCustom(self) instanceof IntCircuitIngredient) {
            return new SizedIngredient(self.ingredient(), self.count());
        } else if (getContainedCustom(self) instanceof IntProviderIngredient intProviderIngredient) {
            return new SizedIngredient(copyIntProvider(intProviderIngredient), self.count());
        }
        return new SizedIngredient(self.ingredient(), self.count());
    }

    public static SizedIngredient copyWithCount(SizedIngredient self, int count) {
        if (self.ingredient().getCustomIngredient() instanceof IntCircuitIngredient) {
            return new SizedIngredient(self.ingredient(), count);
        } else if (self.ingredient().getCustomIngredient() instanceof IntProviderIngredient intProviderIngredient) {
            return new SizedIngredient(copyIntProvider(intProviderIngredient), count);
        }
        return new SizedIngredient(self.ingredient(), count);
    }

    public static IntProvider getCountProvider(SizedIngredient self) {
        if (getContainedCustom(self) instanceof IRangedIngredient rangedIngredient) {
            return rangedIngredient.getCountProvider();
        }
        return ConstantInt.of(self.count());
    }

    public static ItemStack[] getItems(SizedIngredient self) {
        return self.ingredient().items()
                .map(item -> new ItemStack(item, self.count()))
                .toArray(ItemStack[]::new);
    }

    private static Ingredient copyIntProvider(IntProviderIngredient toCopy) {
        var copied = new IntProviderIngredient(toCopy.inner, toCopy.countProvider);
        copied.itemStacks = toCopy.itemStacks;
        copied.sampledCount = toCopy.sampledCount;
        return copied.toVanilla();
    }
}
