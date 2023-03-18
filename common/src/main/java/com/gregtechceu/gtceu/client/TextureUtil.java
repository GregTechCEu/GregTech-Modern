package com.gregtechceu.gtceu.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

/**
 * @author KilaBash
 * @date 2023/2/20
 * @implNote TextureUtil
 */
@Environment(EnvType.CLIENT)
public class TextureUtil {

    public static TextureAtlasSprite getBlockSprite(ResourceLocation location) {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(location);
    }

}
