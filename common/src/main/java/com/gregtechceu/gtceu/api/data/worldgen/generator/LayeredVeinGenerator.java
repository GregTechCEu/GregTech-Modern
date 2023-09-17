package com.gregtechceu.gtceu.api.data.worldgen.generator;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.worldgen.GTLayerPattern;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeature;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LayeredVeinGenerator extends VeinGenerator {
    public static final Codec<LayeredVeinGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    GTLayerPattern.CODEC.listOf().fieldOf("layer_patterns").forGetter(ft -> ft.layerPatterns != null ? ft.layerPatterns : ft.bakingLayerPatterns.stream().map(Supplier::get).collect(Collectors.toList()))
            ).apply(instance, LayeredVeinGenerator::new)
    );

    private final List<NonNullSupplier<GTLayerPattern>> bakingLayerPatterns = new ArrayList<>();

    public List<GTLayerPattern> layerPatterns;

    public LayeredVeinGenerator(GTOreDefinition entry) {
        super(entry);
    }

    @Override
    public List<Map.Entry<Either<BlockState, Material>, Integer>> getAllEntries() {
        return layerPatterns.stream()
                .flatMap(pattern -> pattern.layers.stream())
                .map(layer -> Map.entry(layer.targets.stream().flatMap(entry ->
                                entry.map(blockStates -> blockStates.stream().map(state -> Either.<BlockState, Material>left(state.state)),
                                        material -> Stream.of(Either.<BlockState, Material>right(material)))).toList(),
                        layer.weight))
                .flatMap(entry -> {
                    var iterator = entry.getKey().iterator();
                    return Stream.generate(() -> Map.entry(iterator.next(), entry.getValue())).limit(entry.getKey().size());
                })
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public boolean generate(WorldGenLevel level, RandomSource random, GTOreDefinition entry, BlockPos origin) {
        var patternPool = this.layerPatterns;

        if (patternPool.isEmpty())
            return false;

        GTLayerPattern layerPattern = patternPool.get(random.nextInt(patternPool.size()));

        MutableInt placedAmount = new MutableInt(0);
        int size = entry.getClusterSize();
        float density = entry.getDensity();

        // Limit to a radius of 22 to avoid generating outside the allowed 3x3 chunk area for features
        int radius = Math.min(Mth.ceil(size / 2f), 22);

        int xMin = origin.getX() - radius;
        int yMin = origin.getY() - radius;
        int zMin = origin.getZ() - radius;
        int width = (radius * 2) + 1;
        int length = (radius * 2) + 1;
        int height = (radius * 2) + 1;

        if (origin.getY() >= level.getMaxBuildHeight())
            return false;


        List<GTLayerPattern.Layer> resolvedLayers = new ArrayList<>();
        List<Float> layerDiameterOffsets = new ArrayList<>();

        BlockPos.MutableBlockPos posCursor = new BlockPos.MutableBlockPos();
        BulkSectionAccess access = new BulkSectionAccess(level);
        int layerCoordinate = random.nextInt(4);
        int slantyCoordinate = random.nextInt(3);
        float slope = random.nextFloat() * .75f;

        try {

            for (int xOffset = 0; xOffset < width; xOffset++) {
                float sizeFractionX = xOffset * 2f / width - 1;
                if ((sizeFractionX * sizeFractionX) > 1)
                    continue;

                for (int yOffset = 0; yOffset < height; yOffset++) {
                    float sizeFractionY = yOffset * 2f / height - 1;
                    if ((sizeFractionX * sizeFractionX) + (sizeFractionY * sizeFractionY) > 1)
                        continue;
                    if (level.isOutsideBuildHeight(yMin + yOffset))
                        continue;

                    for (int zOffset = 0; zOffset < length; zOffset++) {
                        float sizeFractionZ = zOffset * 2f / length - 1;

                        int layerIndex = layerCoordinate == 0 ? zOffset : layerCoordinate == 1 ? xOffset : yOffset;
                        if (slantyCoordinate != layerCoordinate)
                            layerIndex += Mth.floor(slantyCoordinate == 0 ? zOffset : slantyCoordinate == 1 ? xOffset : yOffset) * slope;

                        while (layerIndex >= resolvedLayers.size()) {
                            GTLayerPattern.Layer next = layerPattern.rollNext(
                                    resolvedLayers.isEmpty() ? null : resolvedLayers.get(resolvedLayers.size() - 1),
                                    random);
                            float offset = random.nextFloat() * .5f + .5f;
                            for (int i = 0; i < next.minSize + random.nextInt(1 + next.maxSize - next.minSize); i++) {
                                resolvedLayers.add(next);
                                layerDiameterOffsets.add(offset);
                            }
                        }

                        if ((sizeFractionX * sizeFractionX) + (sizeFractionY * sizeFractionY) + (sizeFractionZ * sizeFractionZ) > 1 * layerDiameterOffsets.get(layerIndex))
                            continue;

                        GTLayerPattern.Layer layer = resolvedLayers.get(layerIndex);
                        Either<List<OreConfiguration.TargetBlockState>, Material> state = layer.rollBlock(random);

                        int currentX = xMin + xOffset;
                        int currentY = yMin + yOffset;
                        int currentZ = zMin + zOffset;

                        posCursor.set(currentX, currentY, currentZ);
                        if (!level.ensureCanWrite(posCursor))
                            continue;
                        LevelChunkSection levelchunksection = access.getSection(posCursor);
                        if (levelchunksection == null)
                            continue;

                        int x = SectionPos.sectionRelative(currentX);
                        int y = SectionPos.sectionRelative(currentY);
                        int z = SectionPos.sectionRelative(currentZ);
                        BlockState blockstate = levelchunksection.getBlockState(x, y, z);

                        if (random.nextFloat() <= density) {
                            state.ifLeft(blockStates -> {
                                for (OreConfiguration.TargetBlockState targetState : blockStates) {
                                    if (!GTOreFeature.canPlaceOre(blockstate, access::getBlockState, random, entry, targetState, posCursor))
                                        continue;
                                    if (targetState.state.isAir())
                                        continue;
                                    levelchunksection.setBlockState(x, y, z, targetState.state, false);
                                    placedAmount.increment();
                                    break;
                                }
                            }).ifRight(material -> {
                                if (!GTOreFeature.canPlaceOre(blockstate, access::getBlockState, random, entry, posCursor))
                                    return;
                                BlockState currentState = access.getBlockState(posCursor);
                                var prefix = ChemicalHelper.ORES_INVERSE.get(currentState);
                                if (prefix == null) return;
                                Block toPlace = ChemicalHelper.getBlock(prefix, material);
                                if (toPlace == null || toPlace.defaultBlockState().isAir())
                                    return;
                                levelchunksection.setBlockState(x, y, z, toPlace.defaultBlockState(), false);
                                placedAmount.increment();
                            });
                        }

                    }
                }
            }

        } catch (Throwable throwable1) {
            try {
                access.close();
            } catch (Throwable throwable) {
                throwable1.addSuppressed(throwable);
            }

            throw throwable1;
        }

        access.close();
        return placedAmount.getValue() > 0;
    }

    public LayeredVeinGenerator(List<GTLayerPattern> layerPatterns) {
        super();
        this.layerPatterns = layerPatterns;
    }

    public LayeredVeinGenerator withLayerPattern(NonNullSupplier<GTLayerPattern> pattern) {
        this.bakingLayerPatterns.add(pattern);
        return this;
    }

    public VeinGenerator build() {
        if (this.layerPatterns != null && !this.layerPatterns.isEmpty()) return this;
        List<GTLayerPattern> layerPatterns = this.bakingLayerPatterns.stream()
                .map(NonNullSupplier::get)
                .toList();
        this.layerPatterns = layerPatterns;
        return this;
    }

    @Override
    public VeinGenerator copy() {
        return new LayeredVeinGenerator(this.layerPatterns);
    }

    @Override
    public Codec<? extends VeinGenerator> codec() {
        return CODEC;
    }
}

