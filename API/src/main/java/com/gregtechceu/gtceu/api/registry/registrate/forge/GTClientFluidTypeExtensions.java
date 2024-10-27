package com.gregtechceu.gtceu.api.registry.registrate.forge;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

import lombok.Getter;
import lombok.Setter;

public class GTClientFluidTypeExtensions implements IClientFluidTypeExtensions {

    public static final ResourceLocation FLUID_SCREEN_OVERLAY = GTCEu.id("textures/misc/fluid_screen_overlay.png");

    public GTClientFluidTypeExtensions(ResourceLocation stillTexture, ResourceLocation flowingTexture, int tintColor) {
        this.stillTexture = stillTexture;
        this.flowingTexture = flowingTexture;
        this.tintColor = tintColor;
    }

    @Getter
    @Setter
    private ResourceLocation flowingTexture, stillTexture;
    @Getter
    @Setter
    private int tintColor;

    @Override
    public ResourceLocation getRenderOverlayTexture(Minecraft mc) {
        return FLUID_SCREEN_OVERLAY;
    }
}
