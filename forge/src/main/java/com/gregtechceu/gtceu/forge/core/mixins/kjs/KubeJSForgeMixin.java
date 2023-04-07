package com.gregtechceu.gtceu.forge.core.mixins.kjs;

import com.gregtechceu.gtceu.common.CommonProxy;
import dev.latvian.mods.kubejs.forge.KubeJSForge;
import net.minecraft.core.Registry;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegisterEvent;
import org.apache.commons.lang3.NotImplementedException;
import org.checkerframework.checker.units.qual.C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(KubeJSForge.class)
public class KubeJSForgeMixin {

    @Inject(remap = false, method = "<init>", at = @At(value = "RETURN"))
    private void gtceu$init(CallbackInfo ci) {
        CommonProxy.register();
    }
}
