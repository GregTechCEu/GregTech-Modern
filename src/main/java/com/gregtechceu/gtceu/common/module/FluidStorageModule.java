package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.item.module.AppliedItemModule;
import com.gregtechceu.gtceu.api.item.module.ICapabilityModule;
import com.gregtechceu.gtceu.api.item.module.ItemModule;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FluidStorageModule extends ItemModule implements ICapabilityModule {

    public FluidStorageModule(ResourceLocation id) {
        super(id);
    }

    @Override
    public Component getInfo() {
        return Component.translatable("gtceu.module.fluid_storage");
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(AppliedItemModule module, @NotNull Capability<T> cap) {
        if (cap == ForgeCapabilities.FLUID_HANDLER_ITEM)
            return module.getModuleItem().getCapability(cap);
        return LazyOptional.empty();
    }

    @Override
    public void appendHoverText(Level level, TooltipFlag isAdvanced, List<Component> tooltips,
                                AppliedItemModule module) {
        super.appendHoverText(level, isAdvanced, tooltips, module);
        tooltips.add(Component.translatable("metaarmor.tooltip.modifier.fluid_storage",
                module.getModuleItem().getHoverName()));
        IFluidHandlerItem fluidHandler = getCapability(module, ForgeCapabilities.FLUID_HANDLER_ITEM).resolve()
                .orElse(null);
        if (fluidHandler != null) {
            FluidStack fluid = fluidHandler.getFluidInTank(0);
            int capacity = fluidHandler.getTankCapacity(0);
            tooltips.add(Component.translatable("metaarmor.tooltip.modifier.fluid_storage.tooltip", fluid.getAmount(),
                    capacity, fluid.getDisplayName()));
        }
    }
}
