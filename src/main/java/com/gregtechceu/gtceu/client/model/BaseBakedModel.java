package com.gregtechceu.gtceu.client.model;

import com.gregtechceu.gtceu.client.model.compat.IDynamicBakedModel;
import com.gregtechceu.gtceu.client.model.compat.ItemOverrides;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public abstract class BaseBakedModel implements IDynamicBakedModel {

    public BaseBakedModel() {}

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public TextureAtlasSprite getParticleIcon() {
        return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(TextureAtlas.LOCATION_BLOCKS)
                .getSprite(MissingTextureAtlasSprite.getLocation());
    }
}
