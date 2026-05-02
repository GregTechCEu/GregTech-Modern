package com.gregtechceu.gtceu.core.mixins.client.bloom;

import com.gregtechceu.gtceu.core.PostPassExt;

import net.minecraft.client.renderer.PostPass;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.mojang.blaze3d.pipeline.RenderTarget;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.IntSupplier;

@Mixin(PostPass.class)
public class PostPassMixin implements PostPassExt {

    @Shadow
    @Final
    public RenderTarget outTarget;

    @Unique
    private @Nullable RenderTarget gtceu$copyDepthFrom = null;

    @Definition(id = "outTarget", field = "Lnet/minecraft/client/renderer/PostPass;outTarget:Lcom/mojang/blaze3d/pipeline/RenderTarget;")
    @Definition(id = "unbindWrite", method = "Lcom/mojang/blaze3d/pipeline/RenderTarget;unbindWrite()V")
    @Expression("this.outTarget.unbindWrite()")
    @Inject(method = "process", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private void gtceu$copyInputDepthToOutput(float partialTicks, CallbackInfo ci) {
        if (gtceu$copyDepthFrom != null) {
            this.outTarget.copyDepthFrom(gtceu$copyDepthFrom);
        }
    }

    @Override
    public void gtceu$copyDepthFrom(RenderTarget source) {
        this.gtceu$copyDepthFrom = source;
    }
}
