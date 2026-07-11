package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.client.util.SpriteFunctionWrapper;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(targets = { "net.minecraft.client.resources.model.ModelBakery$ModelBakerImpl" })
public abstract class ModelBakerImplMixin {

    @Mutable
    @Shadow
    @Final
    private Function<Material, TextureAtlasSprite> modelTextureGetter;

    @ModifyVariable(at = @At("HEAD"),
                    method = "bake(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/resources/model/ModelState;Ljava/util/function/Function;)Lnet/minecraft/client/resources/model/BakedModel;",
                    argsOnly = true,
                    remap = false)
    private Function<Material, TextureAtlasSprite> gtceu$injectTextureScraper(Function<Material, TextureAtlasSprite> spriteGetter,
                                                                              ResourceLocation modelLocation,
                                                                              ModelState transform) {
        return new SpriteFunctionWrapper(spriteGetter, modelLocation);
    }

    @Inject(method = "<init>(Lnet/minecraft/client/resources/model/ModelBakery;Lnet/minecraft/client/resources/model/ModelBakery$TextureGetter;Lnet/minecraft/client/resources/model/ModelResourceLocation;)V",
            at = @At("TAIL"),
            remap = false)
    private void gtceu$wrapFieldTextureScraper(ModelBakery outer,
                                               ModelBakery.TextureGetter textureGetter,
                                               ModelResourceLocation modelLocation,
                                               CallbackInfo ci) {
        this.modelTextureGetter = new SpriteFunctionWrapper(this.modelTextureGetter, modelLocation.id());
    }
}
