package com.gregtechceu.gtceu.api.data.worldgen.generator;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeature;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeatureEntry;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import org.apache.commons.lang3.mutable.MutableBoolean;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@AllArgsConstructor
public class VeinedVeinGenerator extends VeinGenerator {
    public static final Codec<Either<List<OreConfiguration.TargetBlockState>, Material>> BLOCK_ENTRY_CODEC = Codec.either(OreConfiguration.TargetBlockState.CODEC.listOf(), GTRegistries.MATERIALS.codec());

    public static final Codec<VeinedVeinGenerator> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            BLOCK_ENTRY_CODEC.fieldOf("ore_block").forGetter(it -> it.oreBlock),
            BLOCK_ENTRY_CODEC.fieldOf("dense_block").forGetter(it -> it.denseBlock),
            BlockState.CODEC.fieldOf("filler_block").orElse(Blocks.AIR.defaultBlockState()).forGetter(it -> it.fillerBlock),
            Codec.INT.fieldOf("min_y").forGetter(it -> it.minYLevel),
            Codec.INT.fieldOf("max_y").forGetter(it -> it.maxYLevel),
            Codec.FLOAT.fieldOf("veininess_threshold").orElse(0.4f).forGetter(it -> it.veininessThreshold),
            Codec.INT.fieldOf("edge_roundoff_begin").orElse(20).forGetter(it -> it.edgeRoundoffBegin),
            Codec.DOUBLE.fieldOf("max_edge_roundoff").orElse(0.2).forGetter(it -> it.maxEdgeRoundoff),
            Codec.FLOAT.fieldOf("min_richness").orElse(0.1f).forGetter(it -> it.minRichness),
            Codec.FLOAT.fieldOf("max_richness").orElse(0.3f).forGetter(it -> it.maxRichness),
            Codec.FLOAT.fieldOf("max_richness_threshold").orElse(0.6f).forGetter(it -> it.maxRichnessThreshold),
            Codec.FLOAT.fieldOf("dense_block_chance").orElse(0.02f).forGetter(it -> it.denseBlockChance)//,
            //Codec.FLOAT.fieldOf("ore_gap_noise_skip_threshold").orElse(-0.3f).forGetter(it -> it.oreGapNoiseSkipThreshold)
    ).apply(instance, VeinedVeinGenerator::new));

    @Setter
    public Either<List<OreConfiguration.TargetBlockState>, Material> oreBlock;
    @Setter
    public Either<List<OreConfiguration.TargetBlockState>, Material> denseBlock;
    @Setter
    public BlockState fillerBlock;
    @Setter
    public int minYLevel;
    @Setter
    public int maxYLevel;
    @Setter
    public float veininessThreshold = 0.4f;
    @Setter
    public int edgeRoundoffBegin = 20;
    @Setter
    public double maxEdgeRoundoff = 0.2;
    @Setter
    public float minRichness = 0.1f;
    @Setter
    public float maxRichness = 0.3f;
    @Setter
    public float maxRichnessThreshold = 0.6f;
    @Setter
    public float denseBlockChance = 0.02f;
    //@Setter
    //public float oreGapNoiseSkipThreshold = -0.3f;

    public VeinedVeinGenerator(GTOreFeatureEntry entry) {
        super(entry);
    }

    @Override
    public Map<Either<BlockState, Material>, Integer> getAllEntries() {
        return Map.of(
                oreBlock.map(state ->
                                state.stream().map(target -> Either.<BlockState, Material>left(target.state)),
                        material -> Stream.of(Either.<BlockState, Material>right(material))), 1,
                denseBlock.map(state ->
                                state.stream().map(target -> Either.<BlockState, Material>left(target.state)),
                        material -> Stream.of(Either.<BlockState, Material>right(material))), 1,
                Stream.of(Either.<BlockState, Material>left(fillerBlock)), 1
        ).entrySet().stream().flatMap(entry -> entry.getKey().map(either -> Map.entry(either, entry.getValue()))).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public boolean generate(WorldGenLevel level, RandomSource random, GTOreFeatureEntry entry, BlockPos origin) {
        RandomState randomState = level.getLevel().getChunkSource().randomState();
        NoiseRouter router = randomState.router();
        Blender blender;
        if (level instanceof WorldGenRegion region) {
            blender = Blender.of(region);
        } else {
            blender = Blender.empty();
        }

        final Blender finalizedBlender = blender;
        DensityFunction veinToggle = router.veinToggle();
        DensityFunction veinRidged = router.veinRidged();
        //DensityFunction veinGap = router.veinGap();

        int size = entry.getClusterSize();
        int placedCount = 0;

        for (int x = origin.getX(); x < origin.getX() + size; ++x) {
            for (int y = origin.getY(); y < origin.getY() + size; ++y) {
                for (int z = origin.getZ(); z < origin.getZ() + size; ++z) {
                    final int finalX = x;
                    final int finalY = y;
                    final int finalZ = z;
                    DensityFunction.FunctionContext functionContext = new DensityFunction.FunctionContext()  {
                        @Override
                        public int blockX() {
                            return finalX;
                        }

                        @Override
                        public int blockY() {
                            return finalY;
                        }

                        @Override
                        public int blockZ() {
                            return finalZ;
                        }

                        @Override
                        public Blender getBlender() {
                            return finalizedBlender;
                        }
                    };

                    double height = veinToggle.compute(functionContext);
                    int blockY = origin.getY();
                    double absHeight = Math.abs(height);
                    int minY = blockY - this.minYLevel;
                    int maxY = this.maxYLevel - blockY;
                    if (minY < 0 || maxY < 0) {
                        continue;
                    }
                    int lowY = Math.min(maxY, minY);
                    double edgeRoundoff = Mth.clampedMap(lowY, 0.0, edgeRoundoffBegin, -maxEdgeRoundoff, 0.0);
                    if (absHeight + edgeRoundoff < veininessThreshold) {
                        continue;
                    }
                    if (random.nextFloat() > entry.getDensity()) {
                        continue;
                    }
                    if (veinRidged.compute(functionContext) >= 0.0) {
                        continue;
                    }
                    double chance = Mth.clampedMap(absHeight, veininessThreshold, maxRichnessThreshold, minRichness, maxRichness);

                    BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(finalX, finalY, finalZ);
                    BlockState current = level.getBlockState(pos);
                    boolean placed = false;
                    if (random.nextFloat() <= entry.getDensity()) {
                        if (random.nextFloat() < chance/* && veinGap.compute(functionContext) > oreGapNoiseSkipThreshold*/) {
                            if (random.nextFloat() < denseBlockChance) {
                                placed = placeOre(denseBlock, current, level, random, pos, entry);
                            } else {
                                placed = placeOre(oreBlock, current, level, random, pos, entry);
                            }
                        } else {
                            if (fillerBlock.isAir())
                                continue;
                            if (!GTOreFeature.canPlaceOre(current, level::getBlockState, random, entry, pos))
                                continue;
                            level.setBlock(pos, fillerBlock, 2);
                            if (level.getBlockState(pos) != current) placed = true;
                        }
                    }

                    if (placed)  {
                        ++placedCount;
                    }
                }
            }

        }
        return placedCount > 0;
    }

    protected static boolean placeOre(Either<List<OreConfiguration.TargetBlockState>, Material> block, BlockState current, WorldGenLevel level, RandomSource random, BlockPos.MutableBlockPos pos, GTOreFeatureEntry entry) {
        MutableBoolean returnValue = new MutableBoolean(false);
        block.ifLeft(blockStates -> {
            for (OreConfiguration.TargetBlockState targetState : blockStates) {
                if (!GTOreFeature.canPlaceOre(current, level::getBlockState, random, entry, targetState, pos))
                    continue;
                if (targetState.state.isAir())
                    continue;
                level.setBlock(pos, targetState.state, 2);
                returnValue.setTrue();
                break;
            }
        }).ifRight(material -> {
            if (!GTOreFeature.canPlaceOre(current, level::getBlockState, random, entry, pos))
                return;
            BlockState currentState = level.getBlockState(pos);
            var prefix = ChemicalHelper.ORES_INVERSE.get(currentState);
            if (prefix == null) return;
            Block toPlace = ChemicalHelper.getBlock(prefix, material);
            if (toPlace == null || toPlace.defaultBlockState().isAir())
                return;
            level.setBlock(pos, toPlace.defaultBlockState(), 2);
            returnValue.setTrue();
        });
        return returnValue.isTrue();
    }

    @Override
    public VeinGenerator build() {
        return this;
    }

    @Override
    public Codec<? extends VeinGenerator> codec() {
        return CODEC;
    }
}
