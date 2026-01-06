package com.gregtechceu.gtceu.core.mixins.kubejs;

import com.gregtechceu.gtceu.integration.kjs.helpers.GTResourceLocation;

import net.minecraft.resources.ResourceLocation;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import dev.latvian.mods.kubejs.util.ID;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ID.class)
public interface IDMixin {

    /// This mixin is injected just before the {@code s = "kubejs:" + s} line, inside the if statement.
    /// That way we don't need to duplicate the implicit namespace checks.
    /// @author screret
    @ModifyVariable(method = "of", name = "s", at = @At(value = "LOAD", ordinal = 1))
    private static String gtceu$hookInferredKubeJSNamespace(String s, @Nullable Object o, boolean preferKJS,
                                                            @Share("isString") LocalBooleanRef isString) {
        GTResourceLocation.nextKubeResLocNamespaceIsImplicit();
        isString.set(true);
        return s;
    }

    /// this injection is here to set up the local ref in time.
    @Inject(method = "of", at = @At("HEAD"))
    private static void gtceu$hookInferredNamespaceGuard1(Object o, boolean preferKJS,
                                                          CallbackInfoReturnable<ResourceLocation> cir,
                                                          @Share("isString") LocalBooleanRef isString) {
        isString.set(false);
    }

    @Inject(method = "of", at = @At("RETURN"))
    private static void gtceu$hookInferredNamespaceGuard2(Object o, boolean preferKJS,
                                                          CallbackInfoReturnable<ResourceLocation> cir,
                                                          @Share("isString") LocalBooleanRef isString) {
        if (!isString.get()) {
            GTResourceLocation.clearCurrentTrackedImplicitValue();
        }
    }
}
