package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.client.ClientProxy;
import com.gregtechceu.gtceu.client.mui.screen.ClientScreenHandler;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Shadow
    @Nullable
    public Screen screen;

    @Inject(method = "runTick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Timer;advanceTime(J)I", shift = At.Shift.AFTER))
    public void timer(CallbackInfo ci) {
        ClientProxy.getTimer60Fps().advanceTime(Util.getMillis());
        for (int j = 0; j < Math.min(20, ClientProxy.getTimer60Fps().tickDelta); ++j) {
            ClientScreenHandler.onFrameUpdate();
        }
    }

    @Inject(method = "setScreen",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;removed()V"))
    public void setScreen(Screen guiScreen, CallbackInfo ci) {
        if (guiScreen == null) {
            // the ScreenEvent.Closing is also closed when the screen is transitioning to another screen,
            // but we only want to know when the next screen null is, so that all screens close.
            ClientScreenHandler.onCloseScreens(this.screen);
        }
    }
}
