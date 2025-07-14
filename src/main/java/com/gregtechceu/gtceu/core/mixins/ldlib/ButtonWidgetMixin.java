package com.gregtechceu.gtceu.core.mixins.ldlib;

import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = ButtonWidget.class, remap = false)
public class ButtonWidgetMixin {

    // Patch out the button == 0 check by making it 0 == 0
    @ModifyVariable(method = "mouseClicked", at = @At(value = "LOAD", ordinal = 0), argsOnly = true)
    private int gtceu$fixButtonRightClick(int button) {
        return 0;
    }
}
