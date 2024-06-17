package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.datafixer.DataFixesInternals;

import net.minecraft.util.datafix.DataFixTypes;

import com.mojang.datafixers.DSL;
import com.mojang.serialization.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(DataFixTypes.class)
public class DataFixTypesMixin {

    @Shadow
    @Final
    private DSL.TypeReference type;

    // ModifyArg to inject our fixes *before* vanilla ones
    @ModifyArg(method = "updateToCurrentVersion(Lcom/mojang/datafixers/DataFixer;Lcom/mojang/serialization/Dynamic;I)Lcom/mojang/serialization/Dynamic;",
               at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/util/datafix/DataFixTypes;update(Lcom/mojang/datafixers/DataFixer;Lcom/mojang/serialization/Dynamic;II)Lcom/mojang/serialization/Dynamic;"),
               index = 1)
    private Dynamic<?> gtceu$injectDataFixers(Dynamic<?> value) {
        return DataFixesInternals.get().updateWithAllFixers(this.type, value);
    }
}
