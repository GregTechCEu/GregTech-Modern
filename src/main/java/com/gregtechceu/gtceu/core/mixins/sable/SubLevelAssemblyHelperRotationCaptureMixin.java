package com.gregtechceu.gtceu.core.mixins.sable;

import com.gregtechceu.gtceu.integration.sable.SableAssemblyRotation;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Sable hands each assembled block to its listener through afterMove, but that callback never carries
 * the angle the contraption was assembled at, and a pipe or machine has to turn its connection mask and
 * covers by the same amount to keep facing the right way. moveBlocks runs the whole batch on one thread
 * in a single pass, so holding the transform's rotation for the length of the call lets the per-block
 * listeners read it without Sable having to widen its interface.
 */
@Mixin(value = SubLevelAssemblyHelper.class, remap = false)
public abstract class SubLevelAssemblyHelperRotationCaptureMixin {

    @Inject(
            method = "moveBlocks(Lnet/minecraft/server/level/ServerLevel;Ldev/ryanhcode/sable/api/SubLevelAssemblyHelper$AssemblyTransform;Ljava/lang/Iterable;)V",
            at = @At("HEAD"),
            remap = false)
    private static void gtceu$captureRotation(ServerLevel level,
                                              SubLevelAssemblyHelper.AssemblyTransform transform,
                                              Iterable<BlockPos> positions, CallbackInfo ci) {
        SableAssemblyRotation.set(transform.getRotation());
    }

    @Inject(
            method = "moveBlocks(Lnet/minecraft/server/level/ServerLevel;Ldev/ryanhcode/sable/api/SubLevelAssemblyHelper$AssemblyTransform;Ljava/lang/Iterable;)V",
            at = @At("RETURN"),
            remap = false)
    private static void gtceu$clearRotation(ServerLevel level,
                                            SubLevelAssemblyHelper.AssemblyTransform transform,
                                            Iterable<BlockPos> positions, CallbackInfo ci) {
        SableAssemblyRotation.clear();
    }
}
