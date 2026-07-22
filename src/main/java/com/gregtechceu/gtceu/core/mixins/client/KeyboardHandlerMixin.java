package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.client.CharTypedEvent;
import com.gregtechceu.gtceu.client.EarlyKeyPressEvent;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "charTyped", at = @At(value = "HEAD"), cancellable = true)
    private void onCharTyped(long windowPointer, int codePoint, int modifiers, CallbackInfo ci) {
        if (windowPointer == this.minecraft.getWindow().getWindow()) {
            CharTypedEvent event = new CharTypedEvent((char) codePoint, modifiers);
            if (MinecraftForge.EVENT_BUS.post(event)) ci.cancel();
        }
    }

    @Inject(method = "keyPress", at = @At(value = "HEAD"), cancellable = true)
    private void onKeyPressed(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        if (windowPointer == this.minecraft.getWindow().getWindow()) {
            EarlyKeyPressEvent event = new EarlyKeyPressEvent(key, scanCode, action, modifiers);
            if (MinecraftForge.EVENT_BUS.post(event)) ci.cancel();
        }
    }
}
