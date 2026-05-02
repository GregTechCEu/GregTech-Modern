package com.gregtechceu.gtceu.core.mixins.client.bloom;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.core.PostPassExt;

import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.util.GsonHelper;

import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.pipeline.RenderTarget;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PostChain.class)
public abstract class PostChainMixin {

    @Shadow
    protected abstract RenderTarget getRenderTarget(String target);

    @Definition(id = "addTempTarget",
            method = "Lnet/minecraft/client/renderer/PostChain;addTempTarget(Ljava/lang/String;II)V")
    @Definition(id = "name", local = @Local(type = String.class)) // targeting locals is brittle, so minimize them
    @Expression("this.addTempTarget(name, ?, ?)")
    @Inject(method = "parseTargetNode", at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER))
    private void gtceu$makeTargetLinear(CallbackInfo ci, @Local JsonObject json, @Local String name) {
        if (GsonHelper.getAsBoolean(json, "bilinear", false)) {
            this.getRenderTarget(name).setFilterMode(GL11.GL_LINEAR);
        }
    }

    @ModifyExpressionValue(method = "parsePassNode",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/PostChain;addPass(Ljava/lang/String;Lcom/mojang/blaze3d/pipeline/RenderTarget;Lcom/mojang/blaze3d/pipeline/RenderTarget;)Lnet/minecraft/client/renderer/PostPass;"))
    private PostPass gtceu$setPassClearOutputValue(PostPass pass, @Local JsonObject json) {
        String copyDepthFrom = GsonHelper.getAsString(json, "copy_depth_from", null);
        if (copyDepthFrom != null) {
            RenderTarget source = this.getRenderTarget(copyDepthFrom);
            if (source != null) {
                ((PostPassExt) pass).gtceu$copyDepthFrom(source);
            } else {
                GTCEu.LOGGER.error("Cannot copy depth from invalid render target {}", copyDepthFrom);
            }
        }
        return pass;
    }
}
