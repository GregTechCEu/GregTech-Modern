package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.client.EnvironmentalHazardClientHandler;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockColors.class)
public class BlockColorsMixin {

    @ModifyExpressionValue(method = "getColor(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;I)I",
                           at = @At(value = "INVOKE",
                                    target = "Lnet/minecraft/client/color/block/BlockColor;getColor(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;I)I"))
    private int gtceu$modifyBiomeAverageColorByHazard(int color, BlockState state,
                                                      @Nullable BlockAndTintGetter level,
                                                      @Nullable BlockPos pos,
                                                      int tintIndex) {
        if (!ConfigHolder.INSTANCE.gameplay.environmentalHazards) {
            return color;
        }

        if (pos == null) {
            return color;
        }
        var clientHandler = EnvironmentalHazardClientHandler.INSTANCE;
        ChunkPos chunkPos = new ChunkPos(pos);

        // because spotless decided to be dumb about if statement indentation
        // @formatter:off
        final Block block = state.getBlock();
        if (block instanceof GrassBlock) {
            return clientHandler.colorZone(color, chunkPos);
        } else if (state.is(BlockTags.LEAVES) || block instanceof TallGrassBlock || state.is(BlockTags.FLOWERS) || block instanceof DoublePlantBlock) {
            return clientHandler.colorZone(color, chunkPos);
        } else if (!state.getFluidState().isEmpty()) {
            return clientHandler.colorZone(color, chunkPos);
        }
        // @formatter:on

        return color;
    }
}
