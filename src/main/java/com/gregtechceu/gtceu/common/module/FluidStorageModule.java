package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.module.CapabilityProviderItemModule;
import com.gregtechceu.gtceu.api.item.module.ModuleContext;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import java.util.List;

public class FluidStorageModule extends CapabilityProviderItemModule<IFluidHandlerItem> {

    public FluidStorageModule(ResourceLocation id) {
        super(id);
    }

    @Override
    public Component getInfo() {
        return Component.translatable("gtceu.module.fluid_storage");
    }

    @Override
    public void attachCapabilities(RegisterCapabilitiesEvent event, Item item) {
        event.registerItem(Capabilities.FluidHandler.ITEM, (s, v) -> {
            var modular = GTCapabilityHelper.getModularItem(s);
            if (modular == null) return null;
            var cap = modular.getAllModuleInstances().stream()
                    .filter(ctx -> ctx.getModule() instanceof FluidStorageModule).findFirst().orElse(null);
            if (cap == null) return null;
            else return cap.getModuleItem().getCapability(Capabilities.FluidHandler.ITEM);
        }, item);
    }

    @Override
    public void appendHoverText(ModuleContext moduleContext, Item.TooltipContext context, List<Component> tooltips,
                                TooltipFlag isAdvanced) {
        super.appendHoverText(moduleContext, context, tooltips, isAdvanced);
        tooltips.add(Component.translatable("metaarmor.tooltip.modifier.fluid_storage",
                moduleContext.getModuleItem().getHoverName()));
        IFluidHandlerItem fluidHandler = moduleContext.getModuleItem().getCapability(Capabilities.FluidHandler.ITEM);
        if (fluidHandler != null) {
            FluidStack fluid = fluidHandler.getFluidInTank(0);
            int capacity = fluidHandler.getTankCapacity(0);
            tooltips.add(Component.translatable("metaarmor.tooltip.modifier.fluid_storage.tooltip", fluid.getAmount(),
                    capacity, fluid.getHoverName()));
        }
    }
}
