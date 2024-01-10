package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.pattern.MultiblockWorldSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunk.class)
public class ChunkMixin {
    @Final @Shadow Level level;

    // We want to be as quick as possible here
    @Inject(method = "setBlockState", at =@At(value = "INVOKE", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/world/level/block/state/BlockState;onPlace(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)V"))
    private void onAddingBlock(BlockPos pos, BlockState state, boolean isMoving, CallbackInfoReturnable<BlockState> cir) {
        MinecraftServer server = level.getServer();
        if (server != null) {
            if (level instanceof ServerLevel serverLevel) {
                for (var structure : MultiblockWorldSavedData.getOrCreate(serverLevel)
                        .getControllerInChunk(((LevelChunk)(Object)this).getPos())) {
                    if (structure.isPosInCache(pos)) {
                        server.executeBlocking(() -> structure.onBlockStateChanged(pos, state));
                    }
                }
            }
        }
    }

}
