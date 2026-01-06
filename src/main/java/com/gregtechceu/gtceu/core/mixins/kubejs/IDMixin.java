package com.gregtechceu.gtceu.core.mixins.kubejs;

import com.gregtechceu.gtceu.integration.kjs.helpers.GTResourceLocation;

import dev.latvian.mods.kubejs.util.ID;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ID.class)
public interface IDMixin {

    @ModifyVariable(method = "of", name = "s", at = @At(value = "LOAD", ordinal = 0))
    private static String gtceu$hookInferredKubeJSNamespace(String s, @Nullable Object o, boolean preferKJS) {
        if (preferKJS && s.indexOf(':') == -1) {
            GTResourceLocation.nextKubeResLocNamespaceIsImplicit();
        }
        return s;
    }
}
