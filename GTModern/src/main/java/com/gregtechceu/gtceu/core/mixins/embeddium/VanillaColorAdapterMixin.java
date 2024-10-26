package com.gregtechceu.gtceu.core.mixins.embeddium;

import com.gregtechceu.gtceu.client.EnvironmentalHazardClientHandler;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.state.BlockState;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.jellysquid.mods.sodium.client.model.quad.ModelQuadView;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "me.jellysquid.mods.sodium.client.model.color.DefaultColorProviders$VanillaAdapter")
public class VanillaColorAdapterMixin {

    @ModifyExpressionValue(method = "getColors(Lme/jellysquid/mods/sodium/client/world/WorldSlice;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lme/jellysquid/mods/sodium/client/model/quad/ModelQuadView;[I)V",
                           at = @At(value = "INVOKE",
                                    target = "Lnet/minecraft/client/color/block/BlockColor;getColor(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;I)I"))
    private int gtceu$modifyBiomeAverageColorByHazard(int color,
                                                      WorldSlice view, BlockPos pos, BlockState state,
                                                      ModelQuadView quad, int[] output) {
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
