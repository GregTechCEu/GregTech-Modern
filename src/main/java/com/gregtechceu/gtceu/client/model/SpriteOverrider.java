package com.gregtechceu.gtceu.client.model;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Map;
import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public record SpriteOverrider(Map<String, ResourceLocation> override)
        implements Function<Material, TextureAtlasSprite> {

    @Override
    public TextureAtlasSprite apply(Material material) {
        return material.sprite();
    }
}
