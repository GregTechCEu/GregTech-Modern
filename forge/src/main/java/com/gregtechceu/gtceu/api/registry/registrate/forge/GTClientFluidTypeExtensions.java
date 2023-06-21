package com.gregtechceu.gtceu.api.registry.registrate.forge;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

import java.util.HashMap;
import java.util.Map;

public class GTClientFluidTypeExtensions implements IClientFluidTypeExtensions {
    public static final Map<ResourceLocation, GTClientFluidTypeExtensions> FLUID_TYPES = new HashMap<>();

    public GTClientFluidTypeExtensions(ResourceLocation still, ResourceLocation stillTexture, ResourceLocation flowingTexture, int tintColor) {
        this.stillTexture = stillTexture;
        this.flowingTexture = flowingTexture;
        this.tintColor = tintColor;
        FLUID_TYPES.put(still, this);
    }

    @Getter @Setter
    private ResourceLocation flowingTexture, stillTexture;
    @Getter @Setter
    private int tintColor;

}
