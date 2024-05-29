package com.gregtechceu.gtceu.api.data.worldgen.modifier;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.BiomeWeightModifier;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import com.mojang.serialization.Codec;

import java.util.List;
import java.util.stream.Stream;

public class BiomePlacement extends PlacementModifier {

    public static final PlacementModifierType<BiomePlacement> BIOME_PLACEMENT = GTRegistries.register(
            BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, GTCEu.id("biome_placement"), () -> BiomePlacement.CODEC);

    public static final Codec<BiomePlacement> CODEC = BiomeWeightModifier.CODEC.listOf().fieldOf("modifiers")
            .xmap(BiomePlacement::new, placement -> placement.modifiers).codec();

    public final List<BiomeWeightModifier> modifiers;

    public BiomePlacement(List<BiomeWeightModifier> modifiers) {
        this.modifiers = modifiers;
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
        Stream<BlockPos> positions = Stream.of(pos);
        for (BiomeWeightModifier modifier : modifiers) {
            if (modifier.addedWeight < 100 && random.nextInt(100) >= modifier.addedWeight) {
                if (modifier.biomes.get().contains(context.getLevel().getBiome(pos))) {
                    return Stream.of();
                }
            }
        }
        return positions;
    }

    @Override
    public PlacementModifierType<?> type() {
        return BIOME_PLACEMENT;
    }
}
