package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.api.mui.overlay.OverlayStack;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This mixin fixes some visual bugs that can happen with overlays.
 */
@Mixin(AbstractWidget.class)
public abstract class AbstractWidgetMixin {

    @Shadow
    protected boolean isHovered;

    @Inject(method = "render",
            at = @At(value = "FIELD",
                     opcode = Opcodes.PUTFIELD,
                     target = "Lnet/minecraft/client/gui/components/AbstractWidget;isHovered:Z"))
    public void gtceu$fixHoveredState(GuiGraphics guiGraphics, int mouseX, int mouseY,
                                      float partialTick, CallbackInfo ci) {
        // fixes buttons being hovered when an overlay element is already hovered
        this.isHovered &= !OverlayStack.isHoveringOverlay();
    }
}
