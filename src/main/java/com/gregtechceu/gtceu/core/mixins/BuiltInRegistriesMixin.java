package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;

@Mixin(BuiltInRegistries.class)
public class BuiltInRegistriesMixin {

    static {
        GTRegistries.init();
    }

    @WrapOperation(method = "validate",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/core/Registry;forEach(Ljava/util/function/Consumer;)V"))
    private static <T extends Registry<?>> void gtceu$skipRegistryValidation(Registry<T> instance, Consumer<T> consumer,
                                                                             Operation<Void> original) {
        Consumer<T> callback = (t) -> {
            if (!t.key().location().getNamespace().equals(GTCEu.MOD_ID))
                consumer.accept(t);
        };

        original.call(instance, callback);
    }
}
