package com.gregtechceu.gtceu.core.mixins.kjs;

import com.gregtechceu.gtceu.integration.kjs.helpers.IGTDummyBuilder;

import net.minecraft.resources.ResourceLocation;

import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryCallback;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Supplier;

/**
 * Some GT builders register multiple objects (e.g. machine builders registered multiple machines for different tiers),
 * or have registration behaviour that KJS cannot handle.
 * This mixin handles those builders, which implement {@link IGTDummyBuilder}
 */
@Mixin(value = RegistryInfo.class, remap = false)
public abstract class RegistryInfoMixin<T> {

    @Redirect(
              method = "registerObjects",
              at = @At(
                       value = "INVOKE",
                       target = "Ldev/latvian/mods/kubejs/registry/RegistryCallback;accept(Lnet/minecraft/resources/ResourceLocation;Ljava/util/function/Supplier;)V"))
    private void redirectAccept(
                                RegistryCallback<T> function, ResourceLocation location, Supplier<T> supplier,
                                @Local(name = "builder") BuilderBase<T> builder) {
        if (builder instanceof IGTDummyBuilder<?> b) {
            b.createObject();
        } else {
            function.accept(location, supplier);
        }
    }
}
