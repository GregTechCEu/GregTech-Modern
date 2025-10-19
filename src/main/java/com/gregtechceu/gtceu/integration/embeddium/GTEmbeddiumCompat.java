package com.gregtechceu.gtceu.integration.embeddium;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import me.jellysquid.mods.sodium.client.render.texture.SpriteUtil;

import java.util.Collection;

public class GTEmbeddiumCompat {

    public static void markSpritesAsActive(Collection<TextureAtlasSprite> sprites) {
        for (TextureAtlasSprite sprite : sprites) {
            SpriteUtil.markSpriteActive(sprite);
        }
    }
}
