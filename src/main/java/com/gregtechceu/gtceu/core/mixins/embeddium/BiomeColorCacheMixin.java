package com.gregtechceu.gtceu.core.mixins.embeddium;

import com.gregtechceu.gtceu.client.EnvironmentalHazardClientHandler;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ColorResolver;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.jellysquid.mods.sodium.client.world.biome.BiomeColorCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = BiomeColorCache.class, remap = false)
public class BiomeColorCacheMixin {

    @ModifyExpressionValue(method = "getColor(Lnet/minecraft/world/level/ColorResolver;III)I",
                           at = @At(value = "INVOKE",
                                    target = "Lme/jellysquid/mods/sodium/client/util/color/BoxBlur$ColorBuffer;get(II)I"))
    private int gtceu$modifyBiomeAverageColorByHazard(int color,
                                                      ColorResolver source, int blockX, int blockY, int blockZ) {
        if (!ConfigHolder.INSTANCE.gameplay.environmentalHazards) {
            return color;
        }

        var clientHandler = EnvironmentalHazardClientHandler.INSTANCE;
        BlockPos pos = new BlockPos(blockX, blockY, blockZ);
        ChunkPos chunkPos = new ChunkPos(pos);

        // because spotless decided to be dumb about if statement indentation
        if (source == BiomeColors.GRASS_COLOR_RESOLVER) {
            return clientHandler.colorZone(color, chunkPos);
        } else if (source == BiomeColors.FOLIAGE_COLOR_RESOLVER) {
            return clientHandler.colorZone(color, chunkPos);
        } else if (source == BiomeColors.WATER_COLOR_RESOLVER) {
            return clientHandler.colorZone(color, chunkPos);
        }

        return color;
    }
}
