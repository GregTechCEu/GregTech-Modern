package com.gregtechceu.gtceu.api.misc.forge;



import com.gregtechceu.gtceu.data.tag.GTDataComponents;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class FilteredFluidHandlerItemStack extends FluidHandlerItemStack {

    Predicate<FluidStack> filter;

    /**
     * @param container The container itemStack, data is stored on it directly as NBT.
     * @param capacity  The maximum capacity of this fluid tank.
     */
    public FilteredFluidHandlerItemStack(@NotNull ItemStack container, int capacity,
                                         Predicate<FluidStack> filter) {
        super(GTDataComponents.FLUID_CONTENT, container, capacity);
        this.filter = filter;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        FluidStack drained = super.drain(resource, action);
        this.removeTagWhenEmpty(action);
        return drained;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        FluidStack drained = super.drain(maxDrain, action);
        this.removeTagWhenEmpty(action);
        return drained;
    }

    private void removeTagWhenEmpty(FluidAction action) {
        if (getFluid().isEmpty() && action == FluidAction.EXECUTE) {
            this.setContainerToEmpty();
        }
    }

    @Override
    public boolean canFillFluidType(FluidStack fluid) {
        return filter.test(fluid);
    }
}
