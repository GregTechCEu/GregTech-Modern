package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.client.shader.GTShaders;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.PostPass;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author KilaBash
 * @date 2022/05/03
 * @implNote PostChainMixin, add more features for vanilla PostChain stuff
 */
@Mixin(PostPass.class)
public abstract class PostPassMixin {
    @Shadow
    @Final
    private EffectInstance effect;

    @Inject(method = "process",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/EffectInstance;safeGetUniform(Ljava/lang/String;)Lcom/mojang/blaze3d/shaders/AbstractUniform;", ordinal = 1))
    private void injectParseTargetNode(float pPartialTicks, CallbackInfo ci) {
        this.effect.safeGetUniform("iTime").set(GTShaders.getITime(pPartialTicks));
    }
}
