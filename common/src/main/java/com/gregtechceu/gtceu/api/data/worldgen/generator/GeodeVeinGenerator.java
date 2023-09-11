package com.gregtechceu.gtceu.api.data.worldgen.generator;

import com.google.common.collect.Lists;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BuddingAmethystBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.GeodeCrackSettings;
import net.minecraft.world.level.levelgen.GeodeLayerSettings;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.GeodeFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

@Accessors(chain = true, fluent = true)
@AllArgsConstructor
public class GeodeVeinGenerator extends VeinGenerator {
    private static final Direction[] DIRECTIONS = Direction.values();

    public static final Codec<Double> CHANCE_RANGE = Codec.doubleRange(0.0, 1.0);

    public static final Codec<GeodeVeinGenerator> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                    GeodeBlockSettings.CODEC.fieldOf("blocks").forGetter((config) -> config.geodeBlockSettings),
                    GeodeLayerSettings.CODEC.fieldOf("layers").forGetter((config) -> config.geodeLayerSettings),
                    GeodeCrackSettings.CODEC.fieldOf("crack").forGetter((config) -> config.geodeCrackSettings),
                    CHANCE_RANGE.fieldOf("use_potential_placements_chance").orElse(0.35).forGetter((config) -> config.usePotentialPlacementsChance),
                    CHANCE_RANGE.fieldOf("use_alternate_layer0_chance").orElse(0.0).forGetter((config) -> config.useAlternateLayer0Chance),
                    Codec.BOOL.fieldOf("placements_require_layer0_alternate").orElse(true).forGetter((config) -> config.placementsRequireLayer0Alternate),
                    IntProvider.codec(1, 20).fieldOf("outer_wall_distance").orElse(UniformInt.of(4, 5)).forGetter((config) -> config.outerWallDistance),
                    IntProvider.codec(1, 20).fieldOf("distribution_points").orElse(UniformInt.of(3, 4)).forGetter((config) -> config.distributionPoints),
                    IntProvider.codec(0, 10).fieldOf("point_offset").orElse(UniformInt.of(1, 2)).forGetter((config) -> config.pointOffset),
                    Codec.INT.fieldOf("min_gen_offset").orElse(-16).forGetter((config) -> config.minGenOffset),
                    Codec.INT.fieldOf("max_gen_offset").orElse(16).forGetter((config) -> config.maxGenOffset),
                    CHANCE_RANGE.fieldOf("noise_multiplier").orElse(0.05).forGetter((config) -> config.noiseMultiplier),
                    Codec.INT.fieldOf("invalid_blocks_threshold").forGetter((config) -> config.invalidBlocksThreshold))
            .apply(instance, GeodeVeinGenerator::new));

    @Setter
    public GeodeBlockSettings geodeBlockSettings;
    @Setter
    public GeodeLayerSettings geodeLayerSettings;
    @Setter
    public GeodeCrackSettings geodeCrackSettings;
    @Setter
    public double usePotentialPlacementsChance = 0.5;
    @Setter
    public double useAlternateLayer0Chance = 0.0;
    @Setter
    public boolean placementsRequireLayer0Alternate = false;
    @Setter
    public IntProvider outerWallDistance = ConstantInt.of(0);
    @Setter
    public IntProvider distributionPoints = ConstantInt.of(0);
    @Setter
    public IntProvider pointOffset = ConstantInt.of(0);
    @Setter
    public int minGenOffset = 0;
    @Setter
    public int maxGenOffset = 0;
    @Setter
    public double noiseMultiplier = 1.0;
    @Setter
    public int invalidBlocksThreshold = 0;

    public GeodeVeinGenerator(GTOreDefinition entry) {
        super(entry);
    }

    @Override
    public List<Map.Entry<Either<BlockState, Material>, Integer>> getAllEntries() {
        RandomSource source = new LegacyRandomSource(0);
        return List.of(
                Map.entry(geodeBlockSettings.fillingProvider.mapBoth(provider -> provider.getState(source, BlockPos.ZERO), Function.identity()), 1),
                Map.entry(geodeBlockSettings.innerLayerProvider.mapBoth(provider -> provider.getState(source, BlockPos.ZERO), Function.identity()), 1),
                Map.entry(geodeBlockSettings.alternateInnerLayerProvider.mapBoth(provider -> provider.getState(source, BlockPos.ZERO), Function.identity()), 1),
                Map.entry(geodeBlockSettings.middleLayerProvider.mapBoth(provider -> provider.getState(source, BlockPos.ZERO), Function.identity()), 1),
                Map.entry(geodeBlockSettings.outerLayerProvider.mapBoth(provider -> provider.getState(source, BlockPos.ZERO), Function.identity()), 1)
        );
    }

    @Override
    public boolean generate(WorldGenLevel level, RandomSource random, GTOreDefinition entry, BlockPos origin) {
        BlockState blockState;
        int offset;
        int offset2;
        int minOffset = this.minGenOffset;
        int maxOffset = this.maxGenOffset;
        LinkedList<Pair<BlockPos, Integer>> points = Lists.newLinkedList();
        int distributionSample = this.distributionPoints.sample(random);
        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(level.getSeed()));
        NormalNoise normalNoise = NormalNoise.create(worldgenRandom, -4, 1.0);
        LinkedList<BlockPos> list2 = Lists.newLinkedList();
        double wallDistance = (double)distributionSample / (double)this.outerWallDistance.getMaxValue();
        double fillingSize = 1.0 / Math.sqrt(geodeLayerSettings.filling);
        double innerSize = 1.0 / Math.sqrt(geodeLayerSettings.innerLayer + wallDistance);
        double middleSize = 1.0 / Math.sqrt(geodeLayerSettings.middleLayer + wallDistance);
        double outerSize = 1.0 / Math.sqrt(geodeLayerSettings.outerLayer + wallDistance);
        double crackSize = 1.0 / Math.sqrt(geodeCrackSettings.baseCrackSize + random.nextDouble() / 2.0 + (distributionSample > 3 ? wallDistance : 0.0));
        boolean doCrack = (double)random.nextFloat() < geodeCrackSettings.generateCrackChance;
        int invalidBlocksCount = 0;
        for (offset2 = 0; offset2 < distributionSample; ++offset2) {
            offset = this.outerWallDistance.sample(random);
            BlockPos origin2 = origin.offset(offset, this.outerWallDistance.sample(random), this.outerWallDistance.sample(random));
            blockState = level.getBlockState(origin2);
            if ((blockState.isAir() || blockState.is(BlockTags.GEODE_INVALID_BLOCKS)) && ++invalidBlocksCount > this.invalidBlocksThreshold) {
                return false;
            }
            points.add(Pair.of(origin2, this.pointOffset.sample(random)));
        }
        if (doCrack) {
            offset2 = random.nextInt(4);
            offset = distributionSample * 2 + 1;
            if (offset2 == 0) {
                list2.add(origin.offset(offset, 7, 0));
                list2.add(origin.offset(offset, 5, 0));
                list2.add(origin.offset(offset, 1, 0));
            } else if (offset2 == 1) {
                list2.add(origin.offset(0, 7, offset));
                list2.add(origin.offset(0, 5, offset));
                list2.add(origin.offset(0, 1, offset));
            } else if (offset2 == 2) {
                list2.add(origin.offset(offset, 7, offset));
                list2.add(origin.offset(offset, 5, offset));
                list2.add(origin.offset(offset, 1, offset));
            } else {
                list2.add(origin.offset(0, 7, 0));
                list2.add(origin.offset(0, 5, 0));
                list2.add(origin.offset(0, 1, 0));
            }
        }
        ArrayList<BlockPos> positions = Lists.newArrayList();
        Predicate<BlockState> placementPredicate = GeodeFeature.isReplaceable(this.geodeBlockSettings.cannotReplace);
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(minOffset, minOffset, minOffset), origin.offset(maxOffset, maxOffset, maxOffset))) {
            double noiseValue = normalNoise.getValue(pos.getX(), pos.getY(), pos.getZ()) * this.noiseMultiplier;
            double s = 0.0;
            double t = 0.0;
            for (var pair : points) {
                s += Mth.fastInvSqrt(pos.distSqr(pair.getFirst()) + (double) pair.getSecond()) + noiseValue;
            }
            for (BlockPos origin4 : list2) {
                t += Mth.fastInvSqrt(pos.distSqr(origin4) + (double)geodeCrackSettings.crackPointOffset) + noiseValue;
            }
            if (s < outerSize) continue;
            if (doCrack && t >= crackSize && s < fillingSize) {
                this.safeSetBlock(level, pos, Blocks.AIR.defaultBlockState(), placementPredicate);
                for (Direction direction : DIRECTIONS) {
                    BlockPos origin5 = pos.relative(direction);
                    FluidState fluidState = level.getFluidState(origin5);
                    if (fluidState.isEmpty()) continue;
                    level.scheduleTick(origin5, fluidState.getType(), 0);
                }
                continue;
            }
            if (s >= fillingSize) {
                this.safeSetBlock(level, pos, getStateFromEither(geodeBlockSettings.fillingProvider, geodeBlockSettings, random, pos), placementPredicate);
                continue;
            }
            if (s >= innerSize) {
                boolean useAltLayer = (double)random.nextFloat() < this.useAlternateLayer0Chance;
                if (useAltLayer) {
                    this.safeSetBlock(level, pos, getStateFromEither(geodeBlockSettings.alternateInnerLayerProvider, geodeBlockSettings, random, pos), placementPredicate);
                } else {
                    this.safeSetBlock(level, pos, getStateFromEither(geodeBlockSettings.innerLayerProvider, geodeBlockSettings, random, pos), placementPredicate);
                }
                if (this.placementsRequireLayer0Alternate && !useAltLayer || !((double)random.nextFloat() < this.usePotentialPlacementsChance)) continue;
                positions.add(pos.immutable());
                continue;
            }
            if (s >= middleSize) {
                this.safeSetBlock(level, pos, getStateFromEither(geodeBlockSettings.middleLayerProvider, geodeBlockSettings, random, pos), placementPredicate);
                continue;
            }
            if (!(s >= outerSize)) continue;
            this.safeSetBlock(level, pos, getStateFromEither(geodeBlockSettings.outerLayerProvider, geodeBlockSettings, random, pos), placementPredicate);
        }
        List<BlockState> innerPlacements = geodeBlockSettings.innerPlacements;
        block5: for (BlockPos origin2 : positions) {
            blockState = Util.getRandom(innerPlacements, random);
            for (Direction direction2 : DIRECTIONS) {
                if (blockState.hasProperty(BlockStateProperties.FACING)) {
                    blockState = blockState.setValue(BlockStateProperties.FACING, direction2);
                }
                BlockPos origin6 = origin2.relative(direction2);
                BlockState blockState2 = level.getBlockState(origin6);
                if (blockState.hasProperty(BlockStateProperties.WATERLOGGED)) {
                    blockState = blockState.setValue(BlockStateProperties.WATERLOGGED, blockState2.getFluidState().isSource());
                }
                if (!BuddingAmethystBlock.canClusterGrowAtState(blockState2)) continue;
                this.safeSetBlock(level, origin6, blockState, placementPredicate);
                continue block5;
            }
        }
        return true;
    }


    protected void safeSetBlock(WorldGenLevel level, BlockPos pos, BlockState state, Predicate<BlockState> oldState) {
        if (oldState.test(level.getBlockState(pos))) {
            level.setBlock(pos, state, 2);
        }
    }

    protected BlockState getStateFromEither(Either<BlockStateProvider, Material> either, GeodeBlockSettings settings, RandomSource random, BlockPos pos) {
        return either.map(provider -> provider.getState(random, pos), material -> ChemicalHelper.getBlock(settings.providerMaterialPrefix, material).defaultBlockState());
    }

    @Override
    public VeinGenerator build() {
        return this;
    }

    @Override
    public VeinGenerator copy() {
        return new GeodeVeinGenerator(this.geodeBlockSettings, this.geodeLayerSettings, this.geodeCrackSettings, this.usePotentialPlacementsChance, this.useAlternateLayer0Chance, this.placementsRequireLayer0Alternate, this.outerWallDistance, this.distributionPoints, this.pointOffset, this.minGenOffset, this.maxGenOffset, this.noiseMultiplier, this.invalidBlocksThreshold);
    }

    @Override
    public Codec<? extends VeinGenerator> codec() {
        return CODEC;
    }

    public record GeodeBlockSettings(Either<BlockStateProvider, Material> fillingProvider,
                                     Either<BlockStateProvider, Material> innerLayerProvider,
                                     Either<BlockStateProvider, Material> alternateInnerLayerProvider,
                                     Either<BlockStateProvider, Material> middleLayerProvider,
                                     Either<BlockStateProvider, Material> outerLayerProvider,
                                     List<BlockState> innerPlacements, TagKey<Block> cannotReplace, TagKey<Block> invalidBlocks, @Nullable TagPrefix providerMaterialPrefix) {
        public static final Codec<GeodeBlockSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.either(BlockStateProvider.CODEC, GTRegistries.MATERIALS.codec()).fieldOf("filling_provider").forGetter(config -> config.fillingProvider),
                Codec.either(BlockStateProvider.CODEC, GTRegistries.MATERIALS.codec()).fieldOf("inner_layer_provider").forGetter(config -> config.innerLayerProvider),
                Codec.either(BlockStateProvider.CODEC, GTRegistries.MATERIALS.codec()).fieldOf("alternate_inner_layer_provider").forGetter(config -> config.alternateInnerLayerProvider),
                Codec.either(BlockStateProvider.CODEC, GTRegistries.MATERIALS.codec()).fieldOf("middle_layer_provider").forGetter(config -> config.middleLayerProvider),
                Codec.either(BlockStateProvider.CODEC, GTRegistries.MATERIALS.codec()).fieldOf("outer_layer_provider").forGetter(config -> config.outerLayerProvider),
                ExtraCodecs.nonEmptyList(BlockState.CODEC.listOf()).fieldOf("inner_placements").forGetter(config -> config.innerPlacements),
                TagKey.hashedCodec(Registries.BLOCK).fieldOf("cannot_replace").forGetter(config -> config.cannotReplace),
                TagKey.hashedCodec(Registries.BLOCK).fieldOf("invalid_blocks").forGetter(config -> config.invalidBlocks),
                TagPrefix.CODEC.optionalFieldOf("provider_material_prefix", TagPrefix.block).forGetter(config -> config.providerMaterialPrefix)
        ).apply(instance, GeodeBlockSettings::new));
    }
}
