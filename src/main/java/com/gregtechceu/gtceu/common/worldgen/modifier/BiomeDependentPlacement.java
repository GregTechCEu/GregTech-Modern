package com.gregtechceu.gtceu.common.worldgen.modifier;

import com.gregtechceu.gtceu.api.data.worldgen.BiomeWeightModifier;

import com.gregtechceu.gtceu.common.data.worldgen.GTPlacementModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import com.mojang.serialization.Codec;

import java.util.List;
import java.util.stream.Stream;

public class BiomeDependentPlacement extends PlacementModifier {

    public static final Codec<BiomeDependentPlacement> CODEC = BiomeWeightModifier.CODEC.listOf().fieldOf("modifiers")
            .xmap(BiomeDependentPlacement::new, placement -> placement.modifiers).codec();

    public final List<BiomeWeightModifier> modifiers;

    public BiomeDependentPlacement(List<BiomeWeightModifier> modifiers) {
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
        return GTPlacementModifiers.BIOME_DEPENDENT.get();
    }
}
