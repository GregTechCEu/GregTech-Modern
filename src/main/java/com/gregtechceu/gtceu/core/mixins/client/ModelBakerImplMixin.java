package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.client.util.SpriteFunctionWrapper;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Function;

@Mixin(targets = { "net.minecraft.client.resources.model.ModelBakery$ModelBakerImpl" })
public abstract class ModelBakerImplMixin {

    // the parameters aren't remapped because Parchment can't remap Forge's patches
    @SuppressWarnings("NameDoesntMatchTargetClass")
    // Note: We don't remap this method as it's added by forge
    @ModifyVariable(at = @At("HEAD"),
                    method = "bake(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/resources/model/ModelState;Ljava/util/function/Function;)Lnet/minecraft/client/resources/model/BakedModel;",
                    argsOnly = true,
                    remap = false)
    private Function<Material, TextureAtlasSprite> gtceu$injectTextureScraper(Function<Material, TextureAtlasSprite> spriteGetter,
                                                                              ResourceLocation modelLocation,
                                                                              ModelState transform) {
        return new SpriteFunctionWrapper(spriteGetter, modelLocation);
    }
}
