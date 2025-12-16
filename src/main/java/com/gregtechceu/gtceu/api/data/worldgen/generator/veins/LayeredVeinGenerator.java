package com.gregtechceu.gtceu.api.data.worldgen.generator.veins;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
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
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class LayeredVeinGenerator extends VeinGenerator {

    public static final Codec<LayeredVeinGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GTLayerPattern.CODEC.listOf().fieldOf("layer_patterns")
                    .forGetter(LayeredVeinGenerator::getLayerPatterns))
            .apply(instance, LayeredVeinGenerator::new));

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
    public Map<BlockPos, OreBlockPlacer> generate(WorldGenLevel level, RandomSource random, GTOreDefinition entry,
                                                  BlockPos origin) {
        Map<BlockPos, OreBlockPlacer> generatedBlocks = new Object2ObjectOpenHashMap<>();
        var patternPool = this.getLayerPatterns();

        if (patternPool.isEmpty())
            return Map.of();

        // minor optimization, usually only one layer pool exists
        GTLayerPattern layerPattern = patternPool.size() == 1 ?
                patternPool.get(0) : patternPool.get(random.nextInt(patternPool.size()));

        int size = entry.clusterSize().sample(random);
        float density = entry.density();

        int radius = Mth.ceil(size / 2f);

        int xMin = origin.getX() - radius;
        int yMin = origin.getY() - radius;
        int zMin = origin.getZ() - radius;
        int width = (radius * 2) + 1;
        int length = (radius * 2) + 1;
        int height = (radius * 2) + 1;

        if (origin.getY() >= level.getMaxBuildHeight())
            return Map.of();

        List<GTLayerPattern.Layer> resolvedLayers = new ArrayList<>();
        FloatList layerDiameterOffsets = new FloatArrayList();

        int layerCoordinate = random.nextInt(4);
        int slantyCoordinate = random.nextInt(3);
        float slope = random.nextFloat() * .75f;

        for (int xOffset = 0; xOffset < width; xOffset++) {
            float sizeFractionX = xOffset * 2f / width - 1;
            float xSizeSqr = sizeFractionX * sizeFractionX;
            if (xSizeSqr > 1)
                continue;

            for (int yOffset = 0; yOffset < height; yOffset++) {
                float sizeFractionY = yOffset * 2f / height - 1;
                float ySizeSqr = sizeFractionY * sizeFractionY;
                if (xSizeSqr + ySizeSqr > 1)
                    continue;
                if (level.isOutsideBuildHeight(yMin + yOffset))
                    continue;

                for (int zOffset = 0; zOffset < length; zOffset++) {
                    float sizeFractionZ = zOffset * 2f / length - 1;
                    float zSizeSqr = sizeFractionZ * sizeFractionZ;
                    // OPTIMIZATION: all values in layerDiameterOffsets are in the [0,1] range, so
                    // check if the size is >1 before doing any of that math
                    if (xSizeSqr + ySizeSqr + zSizeSqr > 1)
                        continue;

                    int layerIndex = layerCoordinate == 0 ? zOffset : layerCoordinate == 1 ? xOffset : yOffset;
                    if (slantyCoordinate != layerCoordinate) {
                        layerIndex += Mth.floor(
                                slantyCoordinate == 0 ? zOffset : slantyCoordinate == 1 ? xOffset : yOffset) * slope;
                    }

                    while (layerIndex >= resolvedLayers.size()) {
                        GTLayerPattern.Layer next = layerPattern.rollNext(
                                resolvedLayers.isEmpty() ? null : resolvedLayers.get(resolvedLayers.size() - 1),
                                random);

                        float offset = random.nextFloat() * 0.5f + 0.5f;
                        // insert the previous layer if this one is null (e.g. invalid)
                        if (next == null) {
                            if (resolvedLayers.isEmpty()) {
                                continue;
                            }
                            resolvedLayers.add(resolvedLayers.get(resolvedLayers.size() - 1));
                            layerDiameterOffsets.add(offset);
                            continue;
                        }
                        for (int i = 0; i < next.minSize + random.nextInt(1 + next.maxSize - next.minSize); i++) {
                            resolvedLayers.add(next);
                            layerDiameterOffsets.add(offset);
                        }
                    }

                    if (xSizeSqr + ySizeSqr + zSizeSqr > layerDiameterOffsets.getFloat(layerIndex))
                        continue;

                    GTLayerPattern.Layer layer = resolvedLayers.get(layerIndex);
                    Either<List<OreConfiguration.TargetBlockState>, Material> state = layer.rollBlock(random);

                    int currentX = xMin + xOffset;
                    int currentY = yMin + yOffset;
                    int currentZ = zMin + zOffset;

                    final var randomSeed = random.nextLong(); // Fully deterministic regardless of chunk order

                    BlockPos currentPos = new BlockPos(currentX, currentY, currentZ);
                    generatedBlocks.put(currentPos, (access, section) -> placeBlock(access, section, randomSeed, entry,
                            density, state, currentPos));
                }
            }
        }

        return generatedBlocks;
    }

    private static void placeBlock(BulkSectionAccess access, LevelChunkSection section, long randomSeed,
                                   GTOreDefinition entry, float density,
                                   Either<List<OreConfiguration.TargetBlockState>, Material> state, BlockPos pos) {
        RandomSource random = new XoroshiroRandomSource(randomSeed);
        int x = SectionPos.sectionRelative(pos.getX());
        int y = SectionPos.sectionRelative(pos.getY());
        int z = SectionPos.sectionRelative(pos.getZ());

        BlockState blockState = section.getBlockState(x, y, z);
        BlockPos.MutableBlockPos posCursor = pos.mutable();

        if (random.nextFloat() <= density) {
            state.ifLeft(blockStates -> {
                for (OreConfiguration.TargetBlockState targetState : blockStates) {
                    if (!OreVeinUtil.canPlaceOre(blockState, access::getBlockState, random, entry, targetState,
                            posCursor))
                        continue;
                    if (targetState.state.isAir())
                        continue;
                    section.setBlockState(x, y, z, targetState.state, false);
                    break;
                }
            }).ifRight(material -> {
                if (!OreVeinUtil.canPlaceOre(blockState, access::getBlockState, random, entry, posCursor))
                    return;
                BlockState currentState = access.getBlockState(posCursor);
                var prefix = ChemicalHelper.getOrePrefix(currentState);
                if (prefix.isEmpty()) return;
                Block toPlace = ChemicalHelper.getBlock(prefix.get(), material);
                if (toPlace == null || toPlace.defaultBlockState().isAir())
                    return;
                section.setBlockState(x, y, z, toPlace.defaultBlockState(), false);
            });
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
