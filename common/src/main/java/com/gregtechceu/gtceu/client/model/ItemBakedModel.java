package com.gregtechceu.gtceu.client.model;

import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;

/**
 * @author KilaBash
 * @date 2023/2/20
 * @implNote ItemBakedModel
 */
@Environment(EnvType.CLIENT)
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
        return ModelFactory.getBlockSprite(MissingTextureAtlasSprite.getLocation());
    }

    @Override
    default ItemTransforms getTransforms() {
        return ModelFactory.MODEL_TRANSFORM_BLOCK;
    }

    @Override
    default ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}
