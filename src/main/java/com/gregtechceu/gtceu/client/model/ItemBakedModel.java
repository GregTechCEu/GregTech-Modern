package com.gregtechceu.gtceu.client.model;

import com.gregtechceu.gtceu.client.model.compat.BakedModel;
import com.gregtechceu.gtceu.client.model.compat.ItemOverrides;
import com.gregtechceu.gtceu.client.model.compat.ItemTransforms;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ItemBakedModel extends BakedModel {

    @Override
    default boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    default boolean isGui3d() {
        return true;
    }

    @Override
    default boolean usesBlockLight() {
        return true;
    }

    @Override
    default boolean isCustomRenderer() {
        return false;
    }

    @Override
    default TextureAtlasSprite getParticleIcon() {
        return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(TextureAtlas.LOCATION_BLOCKS)
                .getSprite(MissingTextureAtlasSprite.getLocation());
    }

    @Override
    default ItemTransforms getTransforms() {
        return ItemTransforms.BLOCK;
    }

    @Override
    default ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}
