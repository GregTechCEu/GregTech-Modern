package com.gregtechceu.gtceu.api.data.worldgen.modifier;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.BiomeWeightModifier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.mojang.serialization.MapCodec;

import java.util.List;
import java.util.stream.Stream;

public class BiomePlacement extends PlacementModifier {

    public static final PlacementModifierType<BiomePlacement> BIOME_PLACEMENT = () -> BiomePlacement.CODEC;

    public static final MapCodec<BiomePlacement> CODEC = BiomeWeightModifier.CODEC.listOf().fieldOf("modifiers")
            .xmap(BiomePlacement::new, placement -> placement.modifiers);

    public final List<BiomeWeightModifier> modifiers;

    public BiomePlacement(List<BiomeWeightModifier> modifiers) {
        this.modifiers = modifiers;
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
        Stream<BlockPos> positions = Stream.of(pos);
        for (BiomeWeightModifier modifier : modifiers) {
            if (modifier.biomes.contains(context.getLevel().getBiome(pos))) {
                if (modifier.addedWeight < 100 && random.nextInt(100) >= modifier.addedWeight) {
                    return Stream.of();
                }
            }
        }
        return positions;
    }

    public static void init(IEventBus modBus) {
        var register = DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, GTCEu.MOD_ID);
        register.register(modBus);
        register.register("biome_placement", () -> BIOME_PLACEMENT);
    }

    @Override
    public PlacementModifierType<?> type() {
        return BIOME_PLACEMENT;
    }
}
