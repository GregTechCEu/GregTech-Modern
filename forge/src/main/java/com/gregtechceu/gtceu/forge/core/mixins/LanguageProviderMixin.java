package com.gregtechceu.gtceu.forge.core.mixins;

import com.gregtechceu.gtceu.data.data.LangHandler;
import net.minecraftforge.common.data.LanguageProvider;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LanguageProvider.class)
public final class LanguageProviderMixin {

    @SuppressWarnings("MethodMayBeStatic")
    @Inject(method = "add(Ljava/lang/String;Ljava/lang/String;)V",
            at = @At(value = "INVOKE", target = "Ljava/lang/IllegalStateException;<init>(Ljava/lang/String;)V"),
            cancellable = true, remap = false)
    public void add(@NotNull String key, @NotNull String value, @NotNull CallbackInfo ci) {
        if (LangHandler.MANUAL_OVERRIDE_PHASE) {
            // this prevents throwing an exception when adding duplicate lang keys to a provider
            // Removing the thrown exception allows for replacement of values for cases such as manual overrides
            ci.cancel();
        }
    }
}
