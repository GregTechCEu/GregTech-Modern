package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.datafixer.DataFixesInternals;

import net.minecraft.util.datafix.DataFixTypes;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.datafixers.DSL;
import com.mojang.serialization.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DataFixTypes.class)
public class DataFixTypesMixin {

    @Shadow
    @Final
    private DSL.TypeReference type;

    // ModifyReturnValue to inject our fixes *after* vanilla ones
    @ModifyReturnValue(method = "update(Lcom/mojang/datafixers/DataFixer;Lcom/mojang/serialization/Dynamic;II)Lcom/mojang/serialization/Dynamic;",
                       at = @At(value = "RETURN"))
    private Dynamic<?> gtceu$injectDataFixers(Dynamic<?> value) {
        return DataFixesInternals.get().updateWithAllFixers(this.type, value);
    }
}
