package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.client.ClientProxy;
import com.gregtechceu.gtceu.client.mui.screen.ClientScreenHandler;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "runTick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Timer;advanceTime(J)I", shift = At.Shift.AFTER))
    public void timer(CallbackInfo ci) {
        ClientProxy.getTimer60Fps().advanceTime(Util.getMillis());
        for (int j = 0; j < Math.min(20, ClientProxy.getTimer60Fps().tickDelta); ++j) {
            ClientScreenHandler.onFrameUpdate();
        }
    }
}
