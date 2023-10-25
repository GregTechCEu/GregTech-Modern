package com.gregtechceu.gtceu.core.fabric;

import com.gregtechceu.gtceu.client.TooltipsHandler;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRenderHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class GTFluidVariantRenderHandler implements FluidVariantRenderHandler {
    @Override
    public void appendTooltip(FluidVariant fluidVariant, List<Component> tooltip, TooltipFlag tooltipContext) {
        TooltipsHandler.appendFluidTooltips(fluidVariant.getFluid(), tooltip, tooltipContext);
    }
}
