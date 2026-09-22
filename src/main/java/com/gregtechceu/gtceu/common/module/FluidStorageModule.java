package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.component.ThermalFluidStats;
import com.gregtechceu.gtceu.api.item.module.CapabilityProviderItemModule;
import com.gregtechceu.gtceu.api.item.module.IModularItem;
import com.gregtechceu.gtceu.api.item.module.ModuleContext;

import com.gregtechceu.gtceu.api.misc.forge.SimpleThermalFluidHandlerItemStack;
import com.gregtechceu.gtceu.api.misc.forge.ThermalFluidHandlerItemStack;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FluidStorageModule extends CapabilityProviderItemModule<IFluidHandlerItem> {

    public FluidStorageModule(ResourceLocation id) {
        super(id);
    }

    @Override
    public Capability<IFluidHandlerItem> getCapability() {
        return ForgeCapabilities.FLUID_HANDLER_ITEM;
    }

    @Override
    public @Nullable IFluidHandlerItem createCapabilityForStack(ModuleContext context, ItemStack stack) {
        if (!(stack.getItem() instanceof IComponentItem componentItem)) return null;
        ThermalFluidStats thermalStats = null;
        for (var component: componentItem.getComponents()) {
            if (component instanceof ThermalFluidStats stats) {
                thermalStats = stats;
                break;
            }
        }
        if (thermalStats == null) return null;
        if (thermalStats.allowPartialFill) {
            return new ThermalFluidHandlerItemStack(stack, thermalStats);
        }
        return new SimpleThermalFluidHandlerItemStack(stack, thermalStats);
    }

    @Override
    public void clearCapabilityFromStack(ModuleContext context, ItemStack stack) {
        stack.remove(GTDataComponents.FLUID_CONTENT);
    }

    @Override
    public Component getInfo() {
        return Component.translatable(getDescriptionLanguageKey());
    }
    @Override
    public void appendHoverText(ModuleContext moduleContext, Level level, List<Component> tooltips,
                                TooltipFlag isAdvanced) {
        super.appendHoverText(moduleContext, level, tooltips, isAdvanced);
        tooltips.add(Component.translatable(getLanguageKey(),
                moduleContext.getData().getModuleItem().getHoverName()));
        IFluidHandlerItem fluidHandler = moduleContext.getAppliedTo().getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).resolve().orElse(null);
        if (fluidHandler != null) {
            FluidStack fluid = fluidHandler.getFluidInTank(0);
            int capacity = fluidHandler.getTankCapacity(0);
            tooltips.add(Component.translatable("module.gtceu.fluid_storage.current_stored", fluid.getAmount(),
                    capacity, fluid.getDisplayName()));
        }
    }
}
