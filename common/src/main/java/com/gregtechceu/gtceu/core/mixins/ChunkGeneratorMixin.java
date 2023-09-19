package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeature;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeatureConfiguration;
import com.gregtechceu.gtceu.config.ConfigHolder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ChunkGenerator.class)
public class ChunkGeneratorMixin {
    @Unique
    private final GTOreFeature ORE = new GTOreFeature();

    @Inject(method = "applyBiomeDecoration", at = @At("TAIL"))
    private void gtceu$applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager, CallbackInfo ci) {
        int gridSize = ConfigHolder.INSTANCE.worldgen.oreVeinScanRadius;
        ChunkPos chunkPos = chunk.getPos();

        var featureConfiguration = new GTOreFeatureConfiguration(null);
        var random = new XoroshiroRandomSource(level.getSeed() ^ chunkPos.toLong());

        if (chunkPos.x % gridSize != 0 || chunkPos.z % gridSize != 0)
            return;

        ORE.place(
                featureConfiguration,
                level,
                (ChunkGenerator) ((Object) this),
                random,
                chunkPos.getBlockAt(0, 0, 0)
        );
    }
}
