package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.datafixer.DataFixesInternals;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.IOWorker;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(IOWorker.class)
public class IOWorkerMixin {

    @Inject(method = "store", at = @At("HEAD"))
    private void storeDFUVersion(ChunkPos chunkPos, @Nullable CompoundTag chunkData,
                                 CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        if (chunkData != null)
            DataFixesInternals.get().addModDataVersions(chunkData);
    }
}
