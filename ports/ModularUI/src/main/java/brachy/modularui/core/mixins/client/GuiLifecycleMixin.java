package brachy.modularui.core.mixins.client;

import brachy.modularui.screen.ClientScreenHandler;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Screen ownership moved from Minecraft to Gui in 26.2. */
@Mixin(Gui.class)
public abstract class GuiLifecycleMixin {
    @Shadow public abstract Screen screen();

    @Inject(method = "setScreen", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/Screen;removed()V"))
    private void modularui$closeScreens(Screen next, CallbackInfo ci) {
        // Opening another screen must not close the stacked ModularUI containers.
        if (next == null) ClientScreenHandler.onCloseScreens(screen());
    }
}
