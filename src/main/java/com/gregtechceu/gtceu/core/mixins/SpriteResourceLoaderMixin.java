package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.core.MixinHelpers;

import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Mixin(SpriteResourceLoader.class)
public class SpriteResourceLoaderMixin {

    // try to load all renderer textures
    @Inject(method = "load",
            at = @At(value = "RETURN"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private static void injectLoad(ResourceManager resourceManager, ResourceLocation location,
                                   CallbackInfoReturnable<SpriteResourceLoader> cir, ResourceLocation resourceLocation,
                                   List<SpriteSource> list) {
        ResourceLocation atlas = new ResourceLocation(location.getNamespace(),
                "textures/atlas/%s.png".formatted(location.getPath()));
        Set<ResourceLocation> sprites = new HashSet<>();
        MixinHelpers.loadBakedModelTextures(atlas, sprites::add);
        for (ResourceLocation sprite : sprites) {
            list.add(new SingleFile(sprite, Optional.empty()));
        }
    }
}
