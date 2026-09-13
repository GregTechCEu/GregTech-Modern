package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.item.module.ICapabilityModule;
import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.api.item.module.ItemModuleType;
import com.gregtechceu.gtceu.common.data.GTItemModules;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import com.mojang.serialization.Codec;

import java.util.List;

public class FluidStorageModule extends ItemModule implements ICapabilityModule {

    public static final Codec<FluidStorageModule> CODEC = simpleCodec(FluidStorageModule::new);

    public FluidStorageModule(boolean isEnabled, ItemStack moduleItem) {
        super(isEnabled, moduleItem);
    }

    public FluidStorageModule(ItemStack moduleItem) {
        super(moduleItem);
    }

    @Override
    public ItemModuleType<FluidStorageModule> type() {
        return GTItemModules.FLUID_STORAGE;
    }

    @Override
    public Component getInfo() {
        return Component.translatable("gtceu.module.fluid_storage");
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap) {
        if (cap == ForgeCapabilities.FLUID_HANDLER_ITEM)
            return getModuleItem().getCapability(cap);
        return LazyOptional.empty();
    }

    @Override
    public void appendHoverText(Level level, TooltipFlag isAdvanced, List<Component> tooltips) {
        super.appendHoverText(level, isAdvanced, tooltips);
        tooltips.add(Component.translatable("metaarmor.tooltip.modifier.fluid_storage",
                getModuleItem().getHoverName()));
        IFluidHandlerItem fluidHandler = getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).resolve()
                .orElse(null);
        if (fluidHandler != null) {
            FluidStack fluid = fluidHandler.getFluidInTank(0);
            int capacity = fluidHandler.getTankCapacity(0);
            tooltips.add(Component.translatable("metaarmor.tooltip.modifier.fluid_storage.tooltip", fluid.getAmount(),
                    capacity, fluid.getDisplayName()));
        }
    }
}
