package com.gregtechceu.gtceu.api.data.worldgen.modifier;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeatureConfiguration;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeatureEntry;
import com.gregtechceu.gtceu.api.data.worldgen.IWorldGenLayer;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.concurrent.ConcurrentHashMap;

public class VeinCountFilter extends PlacementFilter {
    public static final PlacementModifierType<VeinCountFilter> VEIN_COUNT_FILTER = GTRegistries.register(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, GTCEu.id("count"), () -> VeinCountFilter.CODEC);

    public static final VeinCountFilter INSTANCE = new VeinCountFilter();
    public static final Codec<VeinCountFilter> CODEC = Codec.unit(() -> INSTANCE);
    private static final ConcurrentHashMap<Cell, GTOreFeatureEntry> GENERATED = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
        PlacedFeature placedFeature = context.topFeature().orElseThrow(() -> new IllegalStateException("Tried to count check an unregistered feature, or a feature that should not restrict the placement amount"));
        if (placedFeature.feature().value().config() instanceof GTOreFeatureConfiguration configuration) {
            ChunkPos chunkPos = new ChunkPos(pos);
            GTOreFeatureEntry entry = configuration.getEntry(context.getLevel(), context.getLevel().getBiome(pos), random);
            if (entry == null) return false;
            Cell startCell = new Cell(context.getLevel().getLevel(), entry.getLayer(), chunkPos);
            // Search for a radius of (default 3) chunks for other veins, to avoid veins getting too close to eachother (they may originate in weird places)
            int radius = ConfigHolder.INSTANCE.worldgen.oreVeinScanRadius;
            for (int x = -radius; x <= radius; ++x) {
                for (int z = -radius; z <= radius; ++z) {
                    ChunkPos chunkPos2 = new ChunkPos(chunkPos.x + x, chunkPos.z + z);
                    Cell mapCell = new Cell(context.getLevel().getLevel(), entry.getLayer(), chunkPos2);
                    if (GENERATED.containsKey(mapCell)) {
                        //GTCEu.LOGGER.info("CAN NOT place vein " + entry.id + " at chunk " + chunkPos + ", as there is already a vein nearby.");
                        configuration.setEntry(null);
                        return false;
                    }
                }
            }
            GENERATED.put(startCell, entry);
            //GTCEu.LOGGER.info("CAN place vein " + entry.id + " at chunk " + chunkPos + ". No veins found in close proximity!");
            return true;
        }
        return true;
    }

    public static void didNotPlace(WorldGenLevel level, BlockPos pos, GTOreFeatureEntry entry) {
        GENERATED.remove(new Cell(level.getLevel().getLevel(), entry.getLayer(), new ChunkPos(pos)));
    }

    public static VeinCountFilter count() {
        return INSTANCE;
    }

    @Override
    public PlacementModifierType<?> type() {
        return VEIN_COUNT_FILTER;
    }

    public record Cell(ServerLevel level, IWorldGenLayer layer, ChunkPos pos) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Cell cell)) return false;

            if (level != cell.level) return false;
            if (!layer.equals(cell.layer)) return false;
            return pos.equals(cell.pos);
        }

        @Override
        public int hashCode() {
            int result = level.hashCode();
            result = 31 * result + layer.hashCode();
            result = 31 * result + pos.hashCode();
            return result;
        }
    }
}
