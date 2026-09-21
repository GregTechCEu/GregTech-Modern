package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.item.module.ICapabilityModule;
import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.api.item.module.ModuleContext;

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
        return Component.translatable(getDescriptionLanguageKey());
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(ModuleContext moduleContext, @NotNull Capability<T> cap) {
        if (cap == ForgeCapabilities.FLUID_HANDLER_ITEM)
            return moduleContext.getModuleItem().getCapability(cap);
        return LazyOptional.empty();
    }

    @Override
    public void appendHoverText(ModuleContext moduleContext, Level level, List<Component> tooltips,
                                TooltipFlag isAdvanced) {
        super.appendHoverText(moduleContext, level, tooltips, isAdvanced);
        tooltips.add(Component.translatable(getLanguageKey(),
                moduleContext.getModuleItem().getHoverName()));
        IFluidHandlerItem fluidHandler = getCapability(moduleContext, ForgeCapabilities.FLUID_HANDLER_ITEM).resolve()
                .orElse(null);
        if (fluidHandler != null) {
            FluidStack fluid = fluidHandler.getFluidInTank(0);
            int capacity = fluidHandler.getTankCapacity(0);
            tooltips.add(Component.translatable("module.gtceu.fluid_storage.current_stored", fluid.getAmount(),
                    capacity, fluid.getDisplayName()));
        }
    }
}
