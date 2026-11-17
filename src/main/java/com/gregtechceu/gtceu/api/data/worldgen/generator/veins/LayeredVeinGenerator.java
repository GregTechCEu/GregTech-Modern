package com.gregtechceu.gtceu.api.data.worldgen.generator.veins;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.worldgen.GTLayerPattern;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.generator.VeinGenerator;
import com.gregtechceu.gtceu.api.data.worldgen.ores.OreBlockPlacer;
import com.gregtechceu.gtceu.api.data.worldgen.ores.OreVeinUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static net.minecraft.core.SectionPos.blockToSectionCoord;

public class LayeredVeinGenerator extends VeinGenerator {

    // spotless:off
    public static final Codec<LayeredVeinGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GTLayerPattern.CODEC.listOf().fieldOf("layer_patterns").forGetter(LayeredVeinGenerator::getLayerPatterns)
    ).apply(instance, LayeredVeinGenerator::new));
    // spotless:on

    private final List<NonNullSupplier<GTLayerPattern>> bakingLayerPatterns = new ArrayList<>();

    public List<GTLayerPattern> layerPatterns;

    public LayeredVeinGenerator(GTOreDefinition entry) {
        super(entry);
    }

    public List<GTLayerPattern> getLayerPatterns() {
        if (layerPatterns == null || this.layerPatterns.isEmpty()) {
            layerPatterns = bakingLayerPatterns.stream().map(Supplier::get).collect(Collectors.toList());
        }
        return layerPatterns;
    }

    @Override
    public List<VeinEntry> getAllEntries() {
        return getLayerPatterns().stream()
                .flatMap(pattern -> pattern.layers.stream())
                .flatMap(GTLayerPattern.Layer::asVeinEntries)
                .distinct()
                .toList();
    }

    @Override
    public Long2ObjectMap<OreBlockPlacer> generate(WorldGenLevel level, RandomSource random, GTOreDefinition entry,
                                                   BlockPos origin) {
        Long2ObjectMap<OreBlockPlacer> generatedSections = new Long2ObjectOpenHashMap<>();
        List<GTLayerPattern> patternPool = this.getLayerPatterns();
        if (patternPool.isEmpty()) return generatedSections;

        // minor optimization, usually only one layer pool exists
        GTLayerPattern layerPattern = patternPool.size() == 1 ?
                patternPool.get(0) : patternPool.get(random.nextInt(patternPool.size()));

        int size = entry.clusterSize().sample(random);
        int radius = Mth.ceil(size / 2f);

        int xMin = origin.getX() - radius;
        int yMin = origin.getY() - radius;
        int zMin = origin.getZ() - radius;

        int layerCoordinate = random.nextInt(4);
        int slantyCoordinate = random.nextInt(3);
        float slope = random.nextFloat() * 0.75f;
        List<GTLayerPattern.Layer> resolvedLayers = new ArrayList<>();
        FloatList layerDiameterOffsets = new FloatArrayList();
        resolveLayers(size, layerPattern, resolvedLayers, layerDiameterOffsets,
                layerCoordinate, slantyCoordinate, slope, random);

        int sectionX0 = blockToSectionCoord(xMin), sectionX1 = blockToSectionCoord(xMin + size);
        int sectionY0 = blockToSectionCoord(yMin), sectionY1 = blockToSectionCoord(yMin + size);
        int sectionZ0 = blockToSectionCoord(zMin), sectionZ1 = blockToSectionCoord(zMin + size);

        SectionPos.betweenClosedStream(sectionX0, sectionY0, sectionZ0, sectionX1, sectionY1, sectionZ1)
                .forEach(pos -> {
                    final long randomSeed = random.nextLong(); // Fully deterministic regardless of chunk order
                    generatedSections.put(pos.asLong(), (worldGenLevel, access, section, sectionPos) -> {
                        placeSection(worldGenLevel, access, section, sectionPos, randomSeed, entry, size,
                                resolvedLayers, layerDiameterOffsets, layerCoordinate, slantyCoordinate, slope,
                                xMin, yMin, zMin);
                    });
                });

        return generatedSections;
    }

    private static void resolveLayers(int size, GTLayerPattern layerPattern,
                                      List<GTLayerPattern.Layer> resolvedLayers, FloatList layerDiameterOffsets,
                                      int layerCoordinate, int slantyCoordinate, float slope, RandomSource random) {
        // layers are resolved as if the vein is facing primarily up
        int maxLayer = size;
        if (slantyCoordinate != layerCoordinate) {
            maxLayer += Mth.floor(size * slope);
        }
        while (maxLayer >= resolvedLayers.size()) {
            GTLayerPattern.Layer previous = resolvedLayers.isEmpty() ? null :
                    resolvedLayers.get(resolvedLayers.size() - 1);
            GTLayerPattern.Layer next = layerPattern.rollNext(previous, random);

            // value between 0.5 and 1.0
            float offset = random.nextFloat() * 0.5f + 0.5f;

            if (next == null) {
                // insert the previous layer again if this one is null (e.g. invalid)
                if (previous == null) {
                    continue;
                }
                resolvedLayers.add(previous);
                layerDiameterOffsets.add(offset);
            } else {
                int layerThickness = random.nextIntBetweenInclusive(next.minSize, next.maxSize);
                for (int i = 0; i < layerThickness; i++) {
                    resolvedLayers.add(next);
                    layerDiameterOffsets.add(offset);
                }
            }
        }
    }

    private void placeSection(WorldGenLevel level, BulkSectionAccess access, LevelChunkSection section,
                              SectionPos sectionPos, long randomSeed, GTOreDefinition entry, int size,
                              List<GTLayerPattern.Layer> resolvedLayers, FloatList layerDiameterOffsets,
                              int layerCoordinate, int slantyCoordinate, float slope,
                              int xMin, int yMin, int zMin) {
        RandomSource random = new XoroshiroRandomSource(randomSeed);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        // `1 / radius` -> `1 / (diameter / 2)` -> `2 / diameter`
        float inverseRadius = 2f / size;
        for (int dx = 0; dx < size; dx++) {
            if (blockToSectionCoord(xMin + dx) != sectionPos.x()) continue;

            // `dx * (1 / radius)` -> `dx * (1 / (size / 2))` -> `dx * (2 / size)`
            float xDistance = dx * inverseRadius - 1;
            float xDistanceSqr = xDistance * xDistance;
            if (xDistanceSqr > 1f) continue;

            for (int dy = 0; dy < size; dy++) {
                if (blockToSectionCoord(yMin + dy) != sectionPos.y()) continue;

                float yDistance = dy * inverseRadius - 1;
                float yDistanceSqr = yDistance * yDistance;
                if ((xDistanceSqr + yDistanceSqr) > 1f) continue;
                if (level.isOutsideBuildHeight(yMin + dy)) continue;

                for (int dz = 0; dz < size; dz++) {
                    if (blockToSectionCoord(zMin + dz) != sectionPos.z()) continue;

                    float zDistance = dz * inverseRadius - 1;
                    float zDistanceSqr = zDistance * zDistance;
                    float distanceSquared = xDistanceSqr + yDistanceSqr + zDistanceSqr;
                    if (distanceSquared > 1f) continue;

                    if (random.nextFloat() > entry.density()) return;

                    // layerCoordinate is between 0 and 3, so this is weighted to favor making
                    // veins that primarily face up
                    int layerIndex = layerCoordinate == 0 ? dz : layerCoordinate == 1 ? dx : dy;
                    if (slantyCoordinate != layerCoordinate) {
                        layerIndex += Mth.floor((slantyCoordinate == 0 ? dz : slantyCoordinate == 1 ? dx : dy) * slope);
                    }

                    if (distanceSquared > layerDiameterOffsets.getFloat(layerIndex)) continue;

                    GTLayerPattern.Layer layer = resolvedLayers.get(layerIndex);
                    Either<List<OreConfiguration.TargetBlockState>, Material> state = layer.rollBlock(random);

                    pos.set(xMin + dx, yMin + dy, zMin + dz);
                    OreVeinUtil.placeOreBlock(state, access, section, random, entry, pos);
                }
            }
        }
    }

    public LayeredVeinGenerator(List<GTLayerPattern> layerPatterns) {
        super();
        this.layerPatterns = layerPatterns;
    }

    public LayeredVeinGenerator buildLayerPattern(Consumer<GTLayerPattern.Builder> config) {
        var builder = GTLayerPattern.builder(parent().layer().getTarget());
        config.accept(builder);

        return withLayerPattern(builder::build);
    }

    public LayeredVeinGenerator withLayerPattern(NonNullSupplier<GTLayerPattern> pattern) {
        this.bakingLayerPatterns.add(pattern);
        return this;
    }

    public VeinGenerator build() {
        if (this.layerPatterns != null && !this.layerPatterns.isEmpty()) return this;
        this.layerPatterns = this.bakingLayerPatterns.stream()
                .map(NonNullSupplier::get)
                .toList();
        return this;
    }

    @Override
    public VeinGenerator copy() {
        return new LayeredVeinGenerator(new ArrayList<>(this.layerPatterns));
    }

    @Override
    public Codec<? extends VeinGenerator> codec() {
        return CODEC;
    }
}
