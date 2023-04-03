package com.gregtechceu.gtceu.forge.core.mixins.kjs;

import com.gregtechceu.gtceu.common.CommonProxy;
import dev.latvian.mods.kubejs.forge.KubeJSForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegisterEvent;
import org.apache.commons.lang3.NotImplementedException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Consumer;

@Mixin(KubeJSForge.class)
public class KubeJSForgeMixin {

    @Shadow
    private static void initRegistries(RegisterEvent event) {
        throw new NotImplementedException("Mixin failed to apply");
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraftforge/eventbus/api/IEventBus;addListener(Ljava/util/function/Consumer;)V"))
    public void init(IEventBus bus, Consumer<? extends Event> consumer) {
        CommonProxy.onKubeJSSetup();
        bus.addListener(consumer);
    }
}
