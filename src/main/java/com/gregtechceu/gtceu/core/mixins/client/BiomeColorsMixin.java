package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.client.EnvironmentalHazardClientHandler;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ColorResolver;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BiomeColors.class)
public class BiomeColorsMixin {

    @Shadow
    @Final
    @Mutable
    public static ColorResolver WATER_COLOR_RESOLVER;
    @Shadow
    @Final
    @Mutable
    public static ColorResolver GRASS_COLOR_RESOLVER;
    @Shadow
    @Final
    @Mutable
    public static ColorResolver FOLIAGE_COLOR_RESOLVER;

    @Unique
    private static ColorResolver gtceu$wrapResolver(ColorResolver resolver) {
        return (biome, x, z) -> {
            var originalColor = resolver.getColor(biome, x, z);
            if (!ConfigHolder.INSTANCE.gameplay.environmentalHazards) {
                return originalColor;
            }

            var clientHandler = EnvironmentalHazardClientHandler.INSTANCE;
            var chunkPos = new ChunkPos(SectionPos.posToSectionCoord(x), SectionPos.posToSectionCoord(z));
            return clientHandler.colorZone(originalColor, chunkPos);
        };
    }

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void addPollutionWrapper(CallbackInfo ci) {
        WATER_COLOR_RESOLVER = gtceu$wrapResolver(WATER_COLOR_RESOLVER);
        GRASS_COLOR_RESOLVER = gtceu$wrapResolver(GRASS_COLOR_RESOLVER);
        FOLIAGE_COLOR_RESOLVER = gtceu$wrapResolver(FOLIAGE_COLOR_RESOLVER);
    }
}
