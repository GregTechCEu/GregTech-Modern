package com.gregtechceu.gtceu.core.mixins.datafixer;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.datafixer.DataFixesInternals;
import com.gregtechceu.gtceu.common.data.datafixer.GTReferences;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraftforge.common.ForgeHooks;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ForgeHooks.class, remap = false)
public class ForgeHooksMixin {

    @Definition(id = "CompoundTag", type = CompoundTag.class)
    @Definition(id = "fmlData", local = @Local(name = "fmlData", type = CompoundTag.class), remap = false)
    @Expression("fmlData = @(new CompoundTag())")
    @ModifyExpressionValue(method = "writeAdditionalLevelSaveData", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static CompoundTag gtceu$addDataVersions(CompoundTag fmlData) {
        return NbtUtils.addCurrentDataVersion(fmlData);
    }

    @Definition(id = "tag", local = @Local(type = CompoundTag.class, name = "tag"))
    @Definition(id = "getCompound", method = "Lnet/minecraft/nbt/CompoundTag;getCompound(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;", remap = true)
    @Expression("tag.getCompound('Registries')")
    @ModifyExpressionValue(method = "readAdditionalLevelSaveData", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private static CompoundTag gtceu$fixRegistriesTag(CompoundTag regs,
                                                      @Local(name = "tag") CompoundTag tag) {
        int currentVersion = DataFixesInternals.getGTDataVersion(tag);
        return DataFixesInternals.get()
                .update(GTReferences.FORGE_REGISTRY_DATA, regs, currentVersion, GTCEu.GT_DATA_VERSION);
    }
}
