package com.gregtechceu.gtceu.api.registry.registrate.forge;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

import lombok.Getter;
import lombok.Setter;

public class GTClientFluidTypeExtensions implements IClientFluidTypeExtensions {

    public static final Identifier FLUID_SCREEN_OVERLAY = GTCEu.id("textures/misc/fluid_screen_overlay.png");

    public GTClientFluidTypeExtensions(Identifier stillTexture, Identifier flowingTexture, int tintColor) {
        this.stillTexture = stillTexture;
        this.flowingTexture = flowingTexture;
        this.tintColor = tintColor;
    }

    @Getter
    @Setter
    private Identifier flowingTexture, stillTexture;
    @Getter
    @Setter
    private int tintColor;

    @Override
    public Identifier getRenderOverlayTexture(Minecraft mc) {
        return FLUID_SCREEN_OVERLAY;
    }
}
