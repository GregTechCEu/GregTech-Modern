package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.pattern.MultiblockWorldSavedData;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Level.class)
public abstract class LevelMixin implements LevelAccessor {

    @SuppressWarnings("ConstantValue")
    @Inject(method = "markAndNotifyBlock",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/world/level/Level;blockUpdated(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;)V",
                     remap = true),
            remap = false)
    private void gtceu$updateChunkMultiblocks(BlockPos pos, LevelChunk chunk,
                                              BlockState oldState, BlockState newState, int flags, int recursionLeft,
                                              CallbackInfo ci) {
        if (!(((Object) this) instanceof ServerLevel serverLevel)) return;

        MultiblockWorldSavedData mwsd = MultiblockWorldSavedData.getOrCreate(serverLevel);
        for (var structure : mwsd.getControllersInChunk(chunk.getPos())) {
            if (structure.isPosInCache(pos)) {
                serverLevel.getServer().executeBlocking(() -> structure.onBlockStateChanged(pos, newState));
            }
        }
    }
}
