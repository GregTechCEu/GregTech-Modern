package com.gregtechceu.gtceu.core.mixins.client;

import net.minecraft.client.renderer.PostChain;
import net.minecraft.util.GsonHelper;

import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.pipeline.RenderTarget;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(PostChain.class)
public class PostChainMixin {

    @Shadow
    @Final
    private Map<String, RenderTarget> customRenderTargets;

    @Inject(method = "parseTargetNode",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/PostChain;addTempTarget(Ljava/lang/String;II)V",
                     ordinal = 1,
                     shift = At.Shift.AFTER))
    private void gtceu$makeTargetLinear(CallbackInfo ci, @Local JsonObject json, @Local String name) {
        if (GsonHelper.getAsBoolean(json, "bilinear", false)) {
            this.customRenderTargets.get(name).setFilterMode(GL11.GL_LINEAR);
        }
    }
}
