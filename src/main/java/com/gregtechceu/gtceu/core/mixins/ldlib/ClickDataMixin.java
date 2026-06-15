package com.gregtechceu.gtceu.core.mixins.ldlib;

import com.lowdragmc.lowdraglib.gui.util.ClickData;

import net.minecraft.client.gui.screens.Screen;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClickData.class, remap = false)
public class ClickDataMixin {

    @Mutable
    @Shadow(remap = false)
    @Final
    public boolean isShiftClick;

    @Mutable
    @Shadow(remap = false)
    @Final
    public boolean isCtrlClick;

    @Inject(method = "<init>()V", at = @At("RETURN"), remap = false)
    private void gtceu$useVanillaModifierKeys(CallbackInfo ci) {
        isShiftClick = Screen.hasShiftDown();
        isCtrlClick = Screen.hasControlDown();
    }
}
