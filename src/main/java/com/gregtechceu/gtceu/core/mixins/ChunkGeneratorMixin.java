package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.data.worldgen.ores.OrePlacer;
import com.gregtechceu.gtceu.core.util.extensions.ChunkGeneratorExt;

import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkGenerator.class)
public class ChunkGeneratorMixin implements ChunkGeneratorExt {

    @Unique
    private final OrePlacer gtceu$orePlacer = new OrePlacer();

    @Inject(method = "applyBiomeDecoration", at = @At("TAIL"))
    private void gtceu$applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager,
                                            CallbackInfo ci) {
        gtceu$orePlacer.placeOres(level, (ChunkGenerator) ((Object) this), chunk);
    }

    @Override
    public OrePlacer gtceu$getOrePlacer() {
        return this.gtceu$orePlacer;
    }
}
