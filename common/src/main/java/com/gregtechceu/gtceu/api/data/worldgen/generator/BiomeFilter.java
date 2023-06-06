package com.gregtechceu.gtceu.api.data.worldgen.generator;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeatureConfiguration;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeatureEntry;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class BiomeFilter extends PlacementFilter {
    public static final PlacementModifierType<BiomeFilter> BIOME_FILTER = GTRegistries.register(Registry.PLACEMENT_MODIFIERS, GTCEu.id("biome"), () -> BiomeFilter.CODEC);

    public static final BiomeFilter INSTANCE = new BiomeFilter();
    public static final Codec<BiomeFilter> CODEC = Codec.unit(() -> INSTANCE);


    @Override
    protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
        PlacedFeature placedFeature = context.topFeature().orElseThrow(() -> new IllegalStateException("Tried to biome check an unregistered feature, or a feature that should not restrict the biome"));
        if (placedFeature.feature().value().config() instanceof GTOreFeatureConfiguration configuration) {
            GTOreFeatureEntry entry = configuration.getEntry(context.getLevel(), context.getLevel().getBiome(pos), random);
            if (entry == null) return false;
            HolderSet<Biome> checkingBiomes = entry.biomes;
            Holder<Biome> holder = context.getLevel().getBiome(pos);
            if (checkingBiomes != null && !checkingBiomes.contains(holder)) {
                // VeinCountFilter.didNotPlace(context.getLevel(), pos, entry);
                return false;
            }
        }
        return true;
    }

    public static BiomeFilter biome() {
        return INSTANCE;
    }


    @Override
    public PlacementModifierType<?> type() {
        return BIOME_FILTER;
    }
}
