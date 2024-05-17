package com.gregtechceu.gtceu.api.registry.registrate.forge;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

import lombok.Getter;
import lombok.Setter;

public class GTClientFluidTypeExtensions implements IClientFluidTypeExtensions {

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
}
