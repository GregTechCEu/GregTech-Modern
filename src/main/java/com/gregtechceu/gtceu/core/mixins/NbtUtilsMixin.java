package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.datafixer.DataFixesInternals;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NbtUtils.class)
public class NbtUtilsMixin {

    @ModifyReturnValue(method = "addDataVersion", at = @At("RETURN"))
    private static CompoundTag gtceu$addModDataVersion(CompoundTag tag) {
        return DataFixesInternals.get().addModDataVersions(tag);
    }

}
