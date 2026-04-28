package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.pattern.MultiblockState;
import com.gregtechceu.gtceu.api.pattern.MultiblockWorldSavedData;

import com.lowdragmc.lowdraglib.async.AsyncThreadData;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;

@Mixin(Level.class)
public abstract class LevelMixin implements LevelAccessor {

    @Shadow
    @Final
    public boolean isClientSide;

    @Shadow
    @Final
    private Thread thread;

    @Inject(method = "getBlockEntity", at = @At(value = "HEAD"), cancellable = true)
    private void gtceu$getBlockEntityOffThread(BlockPos pos, CallbackInfoReturnable<BlockEntity> cir) {
        if (Thread.currentThread() == this.thread) return;
        if (this.isClientSide) return;
        if (!MultiblockWorldSavedData.isThreadService() && !AsyncThreadData.isThreadService()) return;

        int chunkX = pos.getX() >> 4, chunkZ = pos.getZ() >> 4;
        if (!this.getChunkSource().hasChunk(chunkX, chunkZ)) return;

        ChunkAccess chunk = this.getChunkSource().getChunkNow(chunkX, chunkZ);
        if (chunk instanceof LevelChunk levelChunk) {
            cir.setReturnValue(levelChunk.getBlockEntities().get(pos));
        }
    }

    @Inject(method = "getBlockState", at = @At(value = "HEAD"), cancellable = true)
    private void gtceu$getBlockStateOffThread(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        if (Thread.currentThread() == this.thread) return;
        if (this.isClientSide) return;
        if (!MultiblockWorldSavedData.isThreadService() && !AsyncThreadData.isThreadService()) return;

        int chunkX = pos.getX() >> 4, chunkZ = pos.getZ() >> 4;
        if (!this.getChunkSource().hasChunk(chunkX, chunkZ)) return;

        ChunkAccess chunk = this.getChunkSource().getChunkNow(chunkX, chunkZ);
        if (chunk != null) {
            cir.setReturnValue(chunk.getBlockState(pos));
        }
    }

    @SuppressWarnings("ConstantValue")
    @Inject(method = "markAndNotifyBlock",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/world/level/Level;setBlocksDirty(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)V",
                     remap = true),
            remap = false)
    private void gtceu$updateChunkMultiblocks(BlockPos pos, LevelChunk chunk,
                                              BlockState oldState, BlockState newState, int flags, int recursionLeft,
                                              CallbackInfo ci) {
        if (!(((Object) this) instanceof ServerLevel serverLevel)) return;

        MultiblockWorldSavedData mwsd = MultiblockWorldSavedData.getOrCreate(serverLevel);
        Set<MultiblockState> defensiveCopy = new HashSet<>(mwsd.getControllersInChunk(chunk.getPos()));
        for (MultiblockState structure : defensiveCopy) {
            if (structure.isPosInCache(pos)) {
                serverLevel.getServer().executeBlocking(() -> structure.onBlockStateChanged(pos, newState));
            }
        }
    }
}
