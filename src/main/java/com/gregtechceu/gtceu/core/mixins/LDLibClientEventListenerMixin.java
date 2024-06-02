package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.config.ConfigHolder;

import com.lowdragmc.lowdraglib.client.forge.ClientEventListener;

import net.minecraftforge.client.event.RenderTooltipEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientEventListener.class, remap = false)
public class LDLibClientEventListenerMixin {

    @Inject(method = "appendRenderTooltips", at = @At("HEAD"), cancellable = true)
    private static void gtceu$disableCompass(RenderTooltipEvent.GatherComponents event, CallbackInfo ci) {
        if (!ConfigHolder.INSTANCE.gameplay.enableCompass) {
            ci.cancel();
        }
    }
}
