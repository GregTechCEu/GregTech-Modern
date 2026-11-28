package com.gregtechceu.gtceu.core.mixins.datafixer;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.datafixer.DataFixHelper;
import com.gregtechceu.gtceu.common.data.datafixer.GTReferences;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraftforge.common.ForgeHooks;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = ForgeHooks.class, remap = false)
public class ForgeHooksMixin {

    @ModifyVariable(method = "writeAdditionalLevelSaveData", at = @At(value = "STORE"), name = "fmlData")
    private static CompoundTag gtceu$addDataVersions(CompoundTag fmlData) {
        return NbtUtils.addCurrentDataVersion(fmlData);
    }

    @ModifyVariable(method = "readAdditionalLevelSaveData", at = @At(value = "STORE"), name = "regs")
    private static CompoundTag gtceu$fixRegistriesTag(CompoundTag regs,
                                                      @Local(name = "tag") CompoundTag tag) {
        int currentVersion = DataFixHelper.getGTDataVersion(tag);
        return DataFixHelper.update(GTReferences.FORGE_REGISTRY_DATA, regs, currentVersion, GTCEu.GT_DATA_VERSION);
    }
}
