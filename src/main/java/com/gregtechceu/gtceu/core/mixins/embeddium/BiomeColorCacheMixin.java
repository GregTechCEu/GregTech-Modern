package com.gregtechceu.gtceu.core.mixins.embeddium;

import com.gregtechceu.gtceu.client.EnvironmentalHazardClientHandler;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.jellysquid.mods.sodium.client.world.biome.BiomeColorCache;
import me.jellysquid.mods.sodium.client.world.biome.BiomeColorSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = BiomeColorCache.class, remap = false)
public class BiomeColorCacheMixin {

    @ModifyExpressionValue(method = "getColor",
                           at = @At(value = "INVOKE",
                                    target = "Lme/jellysquid/mods/sodium/client/util/color/BoxBlur$ColorBuffer;get(II)I"))
    private int gtceu$modifyBiomeAverageColorByHazard(int color,
                                                      BiomeColorSource source, int blockX, int blockY, int blockZ) {
        if (!ConfigHolder.INSTANCE.gameplay.environmentalHazards) {
            return color;
        }

        var clientHandler = EnvironmentalHazardClientHandler.INSTANCE;
        BlockPos pos = new BlockPos(blockX, blockY, blockZ);
        ChunkPos chunkPos = new ChunkPos(pos);

        // because spotless decided to be dumb about if statement indentation
        if (source == BiomeColorSource.GRASS) {
            return clientHandler.colorGrass(color, chunkPos);
        } else if (source == BiomeColorSource.FOLIAGE) {
            return clientHandler.colorFoliage(color, chunkPos);
        } else if (source == BiomeColorSource.WATER) {
            return clientHandler.colorLiquid(color, chunkPos);
        }

        return color;
    }
}
