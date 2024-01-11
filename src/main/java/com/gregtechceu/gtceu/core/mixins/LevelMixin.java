package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.pattern.MultiblockWorldSavedData;
import com.lowdragmc.lowdraglib.async.AsyncThreadData;
import net.minecraft.core.BlockPos;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class LevelMixin implements LevelAccessor {

    @Shadow @Final public boolean isClientSide;

    @Shadow @Final private Thread thread;

    @Shadow public abstract boolean isLoaded(BlockPos pPos);

    private ChunkAccess getChunkNow(int pX, int pZ) {
        return this.getChunkSource().getChunkNow(pX, pZ);
    }

    @Inject(method = "getBlockEntity", at = @At(value = "HEAD"), cancellable = true)
    private void getTileEntity(BlockPos pos, CallbackInfoReturnable<BlockEntity> cir) {
        if (!this.isClientSide && Thread.currentThread() != this.thread && (MultiblockWorldSavedData.isThreadService() || AsyncThreadData.isThreadService()) && isLoaded(pos)) {
            ChunkAccess chunk = this.getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
            if (chunk instanceof LevelChunk levelChunk) {
                cir.setReturnValue(levelChunk.getBlockEntities().get(pos));
            }
        }
    }

    @Inject(method = "getBlockState", at = @At(value = "HEAD"), cancellable = true)
    private void getBlockState(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        if (!this.isClientSide && Thread.currentThread() != this.thread && (MultiblockWorldSavedData.isThreadService() || AsyncThreadData.isThreadService()) && isLoaded(pos)) {
            ChunkAccess chunk = this.getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
            if (chunk != null) {
                cir.setReturnValue(chunk.getBlockState(pos));
            }
        }
    }

}
