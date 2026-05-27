package com.gregtechceu.gtceu.core.mixins.datafixer;

import com.gregtechceu.gtceu.api.datafixer.DataFixesInternals;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.datafixers.DSL;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.DataFixTypes;
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
    private <T> Dynamic<T> gtceu$injectDataFixers(Dynamic<T> value) {
        // skip applying datafixers to options.txt; doing that loads the fixers too early
        if ((Object) this == DataFixTypes.OPTIONS) return value;

        return DataFixesInternals.get().updateToCurrentVersion(this.type, value);
    }
}
