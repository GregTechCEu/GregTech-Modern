package com.gregtechceu.gtceu.api.misc.forge;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStackSimple;

import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class FilteredFluidHandlerItemStackSimple extends FluidHandlerItemStackSimple {

    protected final Predicate<FluidStack> filter;

    /**
     * @param container The container itemStack, data is stored on it directly as NBT.
     * @param capacity  The maximum capacity of this fluid tank.
     */
    public FilteredFluidHandlerItemStackSimple(@NotNull ItemStack container, int capacity,
                                               Predicate<FluidStack> filter) {
        super(container, capacity);
        this.filter = filter;
    }

    @Override
    public boolean canFillFluidType(FluidStack fluid) {
        return filter.test(fluid);
    }
}
