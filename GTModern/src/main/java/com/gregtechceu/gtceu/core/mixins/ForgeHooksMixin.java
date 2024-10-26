package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.pattern.MultiblockWorldSavedData;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.BlockSnapshot;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ForgeHooks.class, remap = false)
public class ForgeHooksMixin {

    @Inject(method = "onPlaceItemIntoWorld",
            at = @At(value = "INVOKE",
                     remap = true,
                     target = "Lnet/minecraft/world/level/block/state/BlockState;onPlace(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)V"))
    private static void gtceu$onPlaceBlock(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir,
                                           @Local Level level, @Local BlockSnapshot snap) {
        MinecraftServer server = level.getServer();
        if (server != null) {
            if (level instanceof ServerLevel serverLevel) {
                for (var structure : MultiblockWorldSavedData.getOrCreate(serverLevel)
                        .getControllerInChunk(new ChunkPos(snap.getPos()))) {
                    if (structure.isPosInCache(snap.getPos())) {
                        server.executeBlocking(
                                () -> structure.onBlockStateChanged(snap.getPos(), level.getBlockState(snap.getPos())));
                    }
                }
            }
        }
    }
}
