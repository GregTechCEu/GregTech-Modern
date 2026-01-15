package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.client.renderer.block.model.MultiVariant;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.function.Function;

import javax.annotation.Nullable;

@Mixin(MultiVariant.class)
public class MultiVariantMixin {

    /**
     * @author RubenVerg
     * @reason Use two-argument version of <code>bake</code>
     */
    @Nullable
    @Overwrite
    @SuppressWarnings("deprecation")
    public BakedModel bake(ModelBaker pBaker, Function<Material, TextureAtlasSprite> pSpriteGetter, ModelState pState,
                           ResourceLocation pLocation) {
        var builder = new WeightedBakedModel.Builder();
        for (Variant variant : ((MultiVariant) ((Object) this)).getVariants()) {
            final var baked = pBaker.bake(variant.getModelLocation(), variant);
            builder.add(baked, variant.getWeight());
        }
        return builder.build();
    }
}
