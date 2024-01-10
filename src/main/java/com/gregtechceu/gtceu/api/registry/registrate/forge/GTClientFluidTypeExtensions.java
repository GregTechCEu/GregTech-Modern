package com.gregtechceu.gtceu.api.registry.registrate.forge;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

public class GTClientFluidTypeExtensions implements IClientFluidTypeExtensions {

    public GTClientFluidTypeExtensions(ResourceLocation stillTexture, ResourceLocation flowingTexture, int tintColor) {
        this.stillTexture = stillTexture;
        this.flowingTexture = flowingTexture;
        this.tintColor = tintColor;
    }

    @Getter @Setter
    private ResourceLocation flowingTexture, stillTexture;
    @Getter @Setter
    private int tintColor;

}
