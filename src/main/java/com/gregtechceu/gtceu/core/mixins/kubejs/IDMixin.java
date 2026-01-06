package com.gregtechceu.gtceu.core.mixins.kubejs;

import com.gregtechceu.gtceu.integration.kjs.helpers.GTResourceLocation;

import dev.latvian.mods.kubejs.util.ID;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ID.class)
public interface IDMixin {

    /// This mixin is injected just before the {@code s = "kubejs:" + s} line, inside the if statement.
    /// That way we don't need to duplicate the implicit namespace checks.
    /// @author screret
    @ModifyVariable(method = "of", name = "s", at = @At(value = "LOAD", ordinal = 1))
    private static String gtceu$hookInferredKubeJSNamespace(String s, @Nullable Object o, boolean preferKJS) {
        GTResourceLocation.nextKubeResLocNamespaceIsImplicit();
        return s;
    }
}
