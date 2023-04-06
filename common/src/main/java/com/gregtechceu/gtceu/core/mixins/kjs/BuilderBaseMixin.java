package com.gregtechceu.gtceu.core.mixins.kjs;

import com.gregtechceu.gtceu.integration.kjs.builders.GTRecipeTypeBuilder;
import com.gregtechceu.gtceu.integration.kjs.builders.machine.MachineBuilder;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.latvian.mods.kubejs.BuilderBase;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.Supplier;

@Mixin(value = BuilderBase.class, remap = false)
public abstract class BuilderBaseMixin<T> implements Supplier<T> {

    @Inject(remap = false, method = "registerObject(Z)Z", cancellable = true, at = @At(value = "INVOKE", target = "Ldev/architectury/registry/registries/DeferredRegister;register(Lnet/minecraft/resources/ResourceLocation;Ljava/util/function/Supplier;)Ldev/architectury/registry/registries/RegistrySupplier;"))
    public void gtceu$registerObject(boolean all, CallbackInfoReturnable<Boolean> cir) {
        if (((BuilderBase<T>)(Object)this) instanceof MachineBuilder builder) {
            //builder.createObject();
            cir.setReturnValue(true);
            cir.cancel();
        } else if (((BuilderBase<T>)(Object)this) instanceof GTRecipeTypeBuilder builder) {
            //builder.createObject();
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}
