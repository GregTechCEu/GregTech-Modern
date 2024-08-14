package com.gregtechceu.gtceu.integration.ae2.utils;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import net.minecraft.world.item.ItemStack;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static com.gregtechceu.gtceu.utils.GTMath.split;

public class AEUtil {

    public static @Nullable GenericStack fromFluidStack(FluidStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        var key = AEFluidKey.of(stack.getFluid(), stack.getTag());
        return new GenericStack(key, stack.getAmount());
    }

    public static FluidStack toFluidStack(GenericStack stack) {
        var key = stack.what();
        if (key instanceof AEFluidKey fluidKey) {
            return toFluidStack(fluidKey, stack.amount());
        }
        return FluidStack.empty();
    }

    public static FluidStack toFluidStack(AEFluidKey key, long amount) {
        return FluidStack.create(key.getFluid(), amount, key.getTag());
    }

    public static ItemStack[] toItemStacks(GenericStack stack) {
        var key = stack.what();
        if (key instanceof AEItemKey itemKey) {
            return toItemStacks(itemKey, stack.amount());
        }
        return new ItemStack[0];
    }

    public static ItemStack[] toItemStacks(AEItemKey key, long amount) {
        var ints = split(amount);
        var itemStacks = new ItemStack[ints.length];
        for (int i = 0; i < ints.length; i++) {
            itemStacks[i] = key.toStack(ints[i]);
        }
        return itemStacks;
    }

    public static boolean matches(AEFluidKey key, FluidStack stack) {
        return !stack.isEmpty() && key.getFluid().isSame(stack.getFluid()) &&
                Objects.equals(key.getTag(), stack.getTag());
    }
}
