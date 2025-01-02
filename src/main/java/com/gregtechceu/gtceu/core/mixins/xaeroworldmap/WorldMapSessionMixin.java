package com.gregtechceu.gtceu.core.mixins.xaeroworldmap;

import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.map.xaeros.worldmap.fluid.FluidChunkHighlighter;

import net.minecraft.client.multiplayer.ClientPacketListener;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import xaero.map.WorldMapSession;
import xaero.map.highlight.HighlighterRegistry;

// TODO move to xaeros api once that exists
@Mixin(value = WorldMapSession.class, remap = false)
public abstract class WorldMapSessionMixin {

    @Inject(
            method = "init",
            at = @At(value = "INVOKE", target = "Lxaero/map/highlight/HighlighterRegistry;end()V"),
            locals = LocalCapture.CAPTURE_FAILSOFT)
    private void cadmus$registerHighlighters(ClientPacketListener connection, long biomeZoomSeed, CallbackInfo ci,
                                             @Local HighlighterRegistry highlightRegistry) {
        if (!ConfigHolder.INSTANCE.compat.minimap.toggle.xaerosMapIntegration) return;
        highlightRegistry.register(new FluidChunkHighlighter());
    }
}
