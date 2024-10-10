package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.datafixer.DataFixesInternals;

import net.minecraft.client.HotbarManager;
import net.minecraft.nbt.CompoundTag;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HotbarManager.class)
public abstract class HotbarManagerMixin {

    @Inject(
            method = "save",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/nbt/NbtIo;write(Lnet/minecraft/nbt/CompoundTag;Ljava/io/File;)V",
                     shift = At.Shift.AFTER))
    private void addModDataVersions(CallbackInfo ci, @Local CompoundTag tag) {
        DataFixesInternals.get().addModDataVersions(tag);
    }
}
