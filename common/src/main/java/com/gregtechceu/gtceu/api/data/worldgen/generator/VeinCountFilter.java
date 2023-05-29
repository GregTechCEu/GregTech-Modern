package com.gregtechceu.gtceu.api.data.worldgen.generator;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeatureConfiguration;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeatureEntry;
import com.gregtechceu.gtceu.api.data.worldgen.IWorldGenLayer;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class VeinCountFilter extends PlacementFilter {
    public static final PlacementModifierType<VeinCountFilter> VEIN_COUNT_FILTER = GTRegistries.register(Registry.PLACEMENT_MODIFIERS, GTCEu.id("count"), () -> VeinCountFilter.CODEC);

    public static final VeinCountFilter INSTANCE = new VeinCountFilter();
    public static final Codec<VeinCountFilter> CODEC = Codec.unit(() -> INSTANCE);
    private static final Object2ObjectLinkedOpenHashMap<Cell, GTOreFeatureEntry> GENERATED = new Object2ObjectLinkedOpenHashMap<>();

    @Override
    protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
        PlacedFeature placedFeature = context.topFeature().orElseThrow(() -> new IllegalStateException("Tried to count check an unregistered feature, or a feature that should not restrict the placement amount"));
        if (placedFeature.feature().value().config() instanceof GTOreFeatureConfiguration configuration) {
            ChunkPos chunkPos = new ChunkPos(pos);
            GTOreFeatureEntry entry = configuration.getEntry(context.getLevel(), context.getLevel().getBiome(pos), random);
            if (entry == null) return false;
            Cell startCell = new Cell(context.getLevel().getLevel(), entry.layer, chunkPos);
            // if (GENERATED.contains(startCell, chunkPos)) return false;
            for (int x = -1; x <= 1; ++x) {
                for (int z = -1; z <= 1; ++z) {
                    ChunkPos chunkPos2 = new ChunkPos(chunkPos.x + x, chunkPos.z + z);
                    Cell mapCell = new Cell(context.getLevel().getLevel(), entry.layer, chunkPos2);
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
        GENERATED.remove(new Cell(level.getLevel().getLevel(), entry.layer, new ChunkPos(pos)));
    }

    public static VeinCountFilter count() {
        return INSTANCE;
    }

    @Override
    public PlacementModifierType<?> type() {
        return VEIN_COUNT_FILTER;
    }

    public static class Cell {
        @Getter
        private final ServerLevel level;
        @Getter
        private final IWorldGenLayer layer;
        @Getter
        private final ChunkPos pos;

        public Cell(ServerLevel level, IWorldGenLayer layer, ChunkPos pos) {
            this.level = level;
            this.layer = layer;
            this.pos = pos;
        }

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
