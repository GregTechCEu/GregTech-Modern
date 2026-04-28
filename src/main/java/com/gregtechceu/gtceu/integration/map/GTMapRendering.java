package com.gregtechceu.gtceu.integration.map;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.client.util.RenderUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

public final class GTMapRendering {

    private GTMapRendering() {}

    public static int getFluidColor(Fluid fluid) {
        int color = RenderUtil.getFluidTint(fluid);
        if (color == -1) {
            color = 0xFFFFFFFF;
        }
        var material = ChemicalHelper.getMaterial(fluid);
        if (!material.isNull()) {
            color = material.getMaterialARGB();
        }
        return color;
    }

    public static TextureAtlasSprite getFluidSprite(Fluid fluid) {
        return RenderUtil.FluidTextureType.STILL.map(fluid);
    }

    public static ResourceLocation getFluidSpriteId(Fluid fluid) {
        return GTMapIds.toResourceLocation(getFluidSprite(fluid).contents().name());
    }

    public static TextureAtlasSprite getBlockSprite(Identifier texture) {
        return Minecraft.getInstance()
                .getAtlasManager()
                .getAtlasOrThrow(TextureAtlas.LOCATION_BLOCKS)
                .getSprite(texture);
    }
}
