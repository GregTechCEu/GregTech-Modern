package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.block.StoneType;
import com.gregtechceu.gtceu.common.worldgen.strata.StrataChunkGenerator;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(targets = "net/minecraft/world/level/levelgen/presets/WorldPresets$Bootstrap")
public abstract class WorldPresets$BootstrapMixin {

    @Shadow @Final
    private Registry<StructureSet> structureSets;
    @Shadow @Final
    private Registry<NormalNoise.NoiseParameters> noises;
    @Shadow @Final
    private Registry<NoiseGeneratorSettings> noiseSettings;

    @Shadow
    protected abstract Holder<WorldPreset> registerCustomOverworldPreset(ResourceKey<WorldPreset> generator, LevelStem dimension);

    @Shadow
    protected abstract LevelStem makeOverworld(ChunkGenerator generator);

    @Inject(method = "run",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/presets/WorldPresets$Bootstrap;registerCustomOverworldPreset(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/level/dimension/LevelStem;)Lnet/minecraft/core/Holder;", ordinal = 0),
            locals = LocalCapture.CAPTURE_FAILHARD)
    public void gtceu$addStrataPreset(CallbackInfoReturnable<Holder<WorldPreset>> cir, MultiNoiseBiomeSource multiNoiseBiomeSource, Holder<NoiseGeneratorSettings> holder) {
        this.registerCustomOverworldPreset(ResourceKey.create(Registry.WORLD_PRESET_REGISTRY, GTCEu.id("strata")),
                this.makeOverworld(new StrataChunkGenerator(List.of(StoneType.RED_GRANITE, StoneType.MARBLE, StoneType.ANDESITE, StoneType.BASALT, StoneType.DEEPSLATE),
                        new NoiseBasedChunkGenerator(structureSets, noises, multiNoiseBiomeSource, holder))
                ));
    }

}
