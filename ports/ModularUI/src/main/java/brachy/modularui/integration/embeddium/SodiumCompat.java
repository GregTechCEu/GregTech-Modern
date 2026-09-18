package brachy.modularui.integration.embeddium;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import net.caffeinemc.mods.sodium.api.texture.SpriteUtil;

import java.util.Collection;

public class SodiumCompat {

    @SuppressWarnings("UnstableApiUsage")
    public static void markSpritesAsActive(Collection<TextureAtlasSprite> sprites) {
        for (TextureAtlasSprite sprite : sprites) {
            SpriteUtil.INSTANCE.markSpriteActive(sprite);
        }
    }
}
