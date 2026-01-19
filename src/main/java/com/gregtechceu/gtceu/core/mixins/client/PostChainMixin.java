package com.gregtechceu.gtceu.core.mixins.client;

import net.minecraft.client.renderer.PostChain;
import net.minecraft.util.GsonHelper;

import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
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

    @WrapOperation(method = "parseTargetNode",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/GsonHelper;getAsInt(Lcom/google/gson/JsonObject;Ljava/lang/String;I)I",
                    ordinal = 0))
    private int gtceu$scaleTargetWidth(JsonObject json, String name, int screenWidth, Operation<Integer> original) {
        if (GsonHelper.isNumberValue(json, "scale_width")) {
            return (int) (GsonHelper.getAsFloat(json, "scale_width", 1.0f) * screenWidth);
        }
        return original.call(json, name, screenWidth);
    }

    @WrapOperation(method = "parseTargetNode",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/GsonHelper;getAsInt(Lcom/google/gson/JsonObject;Ljava/lang/String;I)I",
                    ordinal = 1))
    private int gtceu$scaleTargetHeight(JsonObject json, String name, int screenHeight, Operation<Integer> original) {
        if (GsonHelper.isNumberValue(json, "scale_height")) {
            return (int) (GsonHelper.getAsFloat(json, "scale_height", 1.0f) * screenHeight);
        }
        return original.call(json, name, screenHeight);
    }

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
