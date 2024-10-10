package com.gregtechceu.gtceu.client.renderer.pipe.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record TextureInformation(ResourceLocation texture, int colorID) {

    public boolean colorable() {
        return colorID >= 0;
    }
}
