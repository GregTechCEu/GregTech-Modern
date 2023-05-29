package com.gregtechceu.gtceu.api.data.worldgen.generator;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeatureConfiguration;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeatureEntry;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGenLayer;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.Map;

public class VeinCountFilter extends PlacementFilter {
    public static final PlacementModifierType<VeinCountFilter> VEIN_COUNT_FILTER = GTRegistries.register(Registry.PLACEMENT_MODIFIERS, GTCEu.id("count"), () -> VeinCountFilter.CODEC);

    public static final VeinCountFilter INSTANCE = new VeinCountFilter();
    public static final Codec<VeinCountFilter> CODEC = Codec.unit(() -> INSTANCE);
    private final Table<Map.Entry<WorldGenLevel, WorldGenLayer>, ChunkPos, GTOreFeatureEntry> GENERATED = HashBasedTable.create();

    @Override
    protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
        PlacedFeature placedFeature = context.topFeature().orElseThrow(() -> new IllegalStateException("Tried to count check an unregistered feature, or a feature that should not restrict the placement amount"));
        if (placedFeature.feature().value().config() instanceof GTOreFeatureConfiguration configuration) {
            ChunkPos chunkPos = new ChunkPos(pos);
            // if (GENERATED.contains(context.getLevel(), chunkPos)) return false;
            GTOreFeatureEntry entry = configuration.getEntry(context.getLevel(), context.getLevel().getBiome(pos), random);
            if (entry == null) return false;
            Map.Entry<WorldGenLevel, WorldGenLayer> layerEntry = Map.entry(context.getLevel(), entry.layer);
            for (int x = -1; x < 2; ++x) {
                for (int z = -1; z < 2; ++z) {
                    ChunkPos chunkPos2 = new ChunkPos(chunkPos.x + x, chunkPos.z + z);
                    if (GENERATED.contains(layerEntry, chunkPos2)) return false;
                }
            }
            GENERATED.put(layerEntry, chunkPos, entry);
            return true;
        }
        return true;
    }

    public static VeinCountFilter count() {
        return INSTANCE;
    }

    @Override
    public PlacementModifierType<?> type() {
        return VEIN_COUNT_FILTER;
    }
}
