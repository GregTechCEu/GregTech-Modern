package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.datafixer.DataFixesInternals;

import net.minecraft.util.datafix.DataFixTypes;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.serialization.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DataFixTypes.class)
public class DataFixTypesMixin {

    @ModifyReturnValue(method = "updateToCurrentVersion(Lcom/mojang/datafixers/DataFixer;Lcom/mojang/serialization/Dynamic;I)Lcom/mojang/serialization/Dynamic;",
                       at = @At("TAIL"))
    private Dynamic<?> gtceu$injectDataFixers(Dynamic<?> value) {
        return DataFixesInternals.get().updateWithAllFixers((DataFixTypes) (Object) this, value);
    }
}
