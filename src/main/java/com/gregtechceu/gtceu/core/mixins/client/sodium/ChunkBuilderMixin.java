package com.gregtechceu.gtceu.core.mixins.client.sodium;

import com.gregtechceu.gtceu.integration.sodium.GlobalChunkBuildContext;

import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildContext;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.executor.ChunkBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.executor.ChunkJob;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import net.minecraft.client.multiplayer.ClientLevel;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// priority 0 to always apply our mixin first to try and avoid bugs with gtceu$wrapThread
@Mixin(value = ChunkBuilder.class, remap = false, priority = 0)
public class ChunkBuilderMixin {

    @Inject(method = "<init>", at = @At("HEAD"))
    private static void gtceu$markAsMainThread(ClientLevel level, ChunkVertexType vertexType, CallbackInfo ci) {
        GlobalChunkBuildContext.setMainThread();
    }

    @WrapOperation(method = "<init>", at = @At(value = "NEW", target = "java/lang/Thread"))
    private static Thread gtceu$wrapThread(Runnable task, String name, Operation<Thread> original,
                                           @Local(name = "context") ChunkBuildContext context) {
        return new GlobalChunkBuildContext.WorkerThread(task, name, context);
    }

    @Inject(method = "tryStealTask",
            at = @At(value = "INVOKE",
                     target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/executor/ChunkJob;execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;)V"))
    private static void gtceu$captureLocalContext(ChunkJob job, CallbackInfo ci,
                                                  @Local(name = "localContext") ChunkBuildContext localContext) {
        GlobalChunkBuildContext.bindMainThread(localContext);
    }

    @Inject(method = "tryStealTask",
            at = @At(value = "INVOKE",
                     target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;cleanup()V"))
    private static void gtceu$clearLocalContext(ChunkJob job, CallbackInfo ci) {
        GlobalChunkBuildContext.bindMainThread(null);
    }
}
