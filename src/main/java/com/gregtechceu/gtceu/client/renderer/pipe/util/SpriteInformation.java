package com.gregtechceu.gtceu.client.renderer.pipe.util;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record SpriteInformation(TextureAtlasSprite sprite, int colorID) {

    public boolean colorable() {
        return colorID >= 0;
    }
}
