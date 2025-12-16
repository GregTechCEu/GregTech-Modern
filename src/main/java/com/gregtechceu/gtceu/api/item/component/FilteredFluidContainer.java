package com.gregtechceu.gtceu.api.item.component;

import com.gregtechceu.gtceu.api.item.component.forge.IComponentCapability;
import com.gregtechceu.gtceu.api.misc.forge.FilteredFluidHandlerItemStack;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class FilteredFluidContainer implements IItemComponent, IComponentCapability, IAddInformation {

    public final int capacity;
    public final boolean allowPartialFill;
    public Predicate<FluidStack> filter;

    public FilteredFluidContainer(int capacity, boolean allowPartialFill, Predicate<FluidStack> filter) {
        this.allowPartialFill = allowPartialFill;
        this.capacity = capacity;
        this.filter = filter;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(ItemStack itemStack, @NotNull Capability<T> cap) {
        return ForgeCapabilities.FLUID_HANDLER_ITEM.orEmpty(cap,
                LazyOptional.of(() -> new FilteredFluidHandlerItemStack(itemStack, capacity, filter)));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        FluidUtil.getFluidContained(stack).ifPresent(fluid -> tooltipComponents
                .add(Component.translatable("gtceu.universal.tooltip.fluid_stored", fluid.getDisplayName(),
                        fluid.getAmount())));
    }
}
