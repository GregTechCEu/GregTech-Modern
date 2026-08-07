package com.gregtechceu.gtceu.api.item.component;

import com.gregtechceu.gtceu.api.misc.forge.FilteredFluidHandlerItemStack;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

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
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        FluidStack tank = FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
        if (!tank.isEmpty()) {
            tooltipComponents.add(Component.translatable("gtceu.universal.tooltip.fluid_stored",
                    tank.getHoverName(), FormattingUtil.formatNumbers(tank.getAmount())));
        }
    }

    @Override
    public void attachCapabilities(RegisterCapabilitiesEvent event, Item item) {
        event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> {
            return new FilteredFluidHandlerItemStack(stack, capacity, filter);
        }, item);
    }
}
