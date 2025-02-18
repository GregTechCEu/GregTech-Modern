package com.gregtechceu.gtceu.core.mixins.xaerominimap;

import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.map.xaeros.minimap.fluid.FluidChunkHighlighter;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.common.minimap.highlight.AbstractHighlighter;
import xaero.common.minimap.highlight.HighlighterRegistry;

// TODO move to xaeros api once that exists
@Mixin(value = HighlighterRegistry.class, remap = false)
public abstract class HighlighterRegistryMixin {

    @Shadow
    public abstract void register(AbstractHighlighter highlighter);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void gtceu$registerHighlighters(CallbackInfo ci) {
        if (!ConfigHolder.INSTANCE.compat.minimap.toggle.xaerosMapIntegration) return;
        this.register(new FluidChunkHighlighter());
    }
}
