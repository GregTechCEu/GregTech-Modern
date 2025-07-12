package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.client.model.SpriteCapturer;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(BlockModel.class)
public class BlockModelMixin {

    @Shadow
    public String name;
    @Unique
    private final ThreadLocal<SpriteCapturer> gtceu$spriteCapturer = new ThreadLocal<>();

    @Inject(method = "bake(Lnet/minecraft/client/resources/model/ModelBaker;Lnet/minecraft/client/renderer/block/model/BlockModel;Ljava/util/function/Function;Lnet/minecraft/client/resources/model/ModelState;Lnet/minecraft/resources/ResourceLocation;Z)Lnet/minecraft/client/resources/model/BakedModel;",
            at = @At(value = "HEAD"))
    private void gtceu$beforeBake(ModelBaker baker, BlockModel model,
                                  Function<Material, TextureAtlasSprite> spriteGetter, ModelState state,
                                  ResourceLocation location, boolean guiLight3d,
                                  CallbackInfoReturnable<BakedModel> cir) {
        if (spriteGetter instanceof SpriteCapturer spriteCapturer) {
            gtceu$spriteCapturer.set(spriteCapturer);
        }
    }

    @Inject(method = "bake(Lnet/minecraft/client/resources/model/ModelBaker;Lnet/minecraft/client/renderer/block/model/BlockModel;Ljava/util/function/Function;Lnet/minecraft/client/resources/model/ModelState;Lnet/minecraft/resources/ResourceLocation;Z)Lnet/minecraft/client/resources/model/BakedModel;",
            at = @At(value = "RETURN"))
    private void gtceu$afterBake(ModelBaker baker, BlockModel model,
                                 Function<Material, TextureAtlasSprite> spriteGetter, ModelState state,
                                 ResourceLocation location, boolean guiLight3d,
                                 CallbackInfoReturnable<BakedModel> cir) {
        if (spriteGetter instanceof SpriteCapturer) {
            gtceu$spriteCapturer.remove();
        }
    }

    @Inject(method = "getMaterial", at = @At(value = "RETURN"))
    private void gtceu$captureMaterialNames(String name, CallbackInfoReturnable<Material> cir) {
        Material material = cir.getReturnValue();
        if (material.texture().equals(MissingTextureAtlasSprite.getLocation())) {
            return;
        }
        SpriteCapturer capturer = gtceu$spriteCapturer.get();
        if (capturer != null) {
            capturer.captureMaterialName(material, name);
        }
    }
}
