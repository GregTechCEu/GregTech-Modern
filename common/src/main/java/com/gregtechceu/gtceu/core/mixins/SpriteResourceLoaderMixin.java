package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.core.MixinHelpers;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

@Mixin(SpriteResourceLoader.class)
public class SpriteResourceLoaderMixin {
    @Inject(method = "load", at = @At(value = "RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void injectLoad(ResourceManager resourceManager, ResourceLocation location, CallbackInfoReturnable<SpriteResourceLoader> cir, ResourceLocation resourceLocation, List<SpriteSource> list) {
        if (!location.equals(new ResourceLocation("blocks"))) return;
        MixinHelpers.initializeDynamicTextures();
    }
}
