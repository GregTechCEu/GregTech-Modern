package com.gregtechceu.gtceu.api.data.worldgen.generator.veins;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.generator.VeinGenerator;
import com.gregtechceu.gtceu.api.data.worldgen.ores.OreBlockPlacer;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.material.FluidState;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Accessors(chain = true, fluent = true)
@AllArgsConstructor
public class GeodeVeinGenerator extends VeinGenerator {

    public static final Codec<Double> CHANCE_RANGE = Codec.doubleRange(0.0, 1.0);
    // spotless:off
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
            // Codec.INT.fieldOf("invalid_blocks_threshold").forGetter((config) -> config.invalidBlocksThreshold)
            MapCodec.unit(0).forGetter((config) -> config.invalidBlocksThreshold)
    ).apply(instance, GeodeVeinGenerator::new));
    // spotless:on

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
    public List<VeinEntry> getAllEntries() {
        RandomSource source = new LegacyRandomSource(0);
        return List.of(
                new VeinEntry(geodeBlockSettings.fillingProvider
                        .mapLeft(provider -> provider.getState(source, BlockPos.ZERO)), 1),
                new VeinEntry(geodeBlockSettings.innerLayerProvider
                        .mapLeft(provider -> provider.getState(source, BlockPos.ZERO)), 1),
                new VeinEntry(geodeBlockSettings.alternateInnerLayerProvider
                        .mapLeft(provider -> provider.getState(source, BlockPos.ZERO)), 1),
                new VeinEntry(geodeBlockSettings.middleLayerProvider
                        .mapLeft(provider -> provider.getState(source, BlockPos.ZERO)), 1),
                new VeinEntry(geodeBlockSettings.outerLayerProvider
                        .mapLeft(provider -> provider.getState(source, BlockPos.ZERO)), 1));
    }

    @Override
    public Long2ObjectMap<OreBlockPlacer> generate(WorldGenLevel level, RandomSource random, GTOreDefinition entry,
                                                   BlockPos origin) {
        Long2ObjectMap<OreBlockPlacer> generatedSections = new Long2ObjectOpenHashMap<>();

        int offset;
        int offset2;
        int distributionSample = this.distributionPoints.sample(random);

        List<ObjectIntPair<BlockPos>> points = new ArrayList<>(distributionSample);
        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(level.getSeed()));
        NormalNoise normalNoise = NormalNoise.create(worldgenRandom, -4, 1.0);
        List<BlockPos> offsets = new ArrayList<>(3);

        double wallDistance = (double) distributionSample / (double) this.outerWallDistance.getMaxValue();
        double crackSize = 1.0 / Math.sqrt(geodeCrackSettings.baseCrackSize + random.nextDouble() / 2.0 +
                (distributionSample > 3 ? wallDistance : 0.0));
        boolean doCrack = random.nextDouble() < geodeCrackSettings.generateCrackChance;

        for (offset2 = 0; offset2 < distributionSample; ++offset2) {
            offset = this.outerWallDistance.sample(random);
            BlockPos pos = origin.offset(offset, this.outerWallDistance.sample(random),
                    this.outerWallDistance.sample(random));
            points.add(ObjectIntPair.of(pos, this.pointOffset.sample(random)));
        }
        if (doCrack) {
            offset2 = random.nextInt(4);
            offset = distributionSample * 2 + 1;
            if (offset2 == 0) {
                offsets.add(origin.offset(offset, 7, 0));
                offsets.add(origin.offset(offset, 5, 0));
                offsets.add(origin.offset(offset, 1, 0));
            } else if (offset2 == 1) {
                offsets.add(origin.offset(0, 7, offset));
                offsets.add(origin.offset(0, 5, offset));
                offsets.add(origin.offset(0, 1, offset));
            } else if (offset2 == 2) {
                offsets.add(origin.offset(offset, 7, offset));
                offsets.add(origin.offset(offset, 5, offset));
                offsets.add(origin.offset(offset, 1, offset));
            } else {
                offsets.add(origin.offset(0, 7, 0));
                offsets.add(origin.offset(0, 5, 0));
                offsets.add(origin.offset(0, 1, 0));
            }
        }
        List<BlockPos> positions = new ArrayList<>();

        SectionPos
                .betweenClosedStream(origin.getX() - minGenOffset, origin.getY() - minGenOffset,
                        origin.getZ() - minGenOffset,
                        origin.getX() + maxGenOffset, origin.getY() + maxGenOffset, origin.getZ() + maxGenOffset)
                .forEach(pos -> {
                    final long randomSeed = random.nextLong(); // Fully deterministic regardless of chunk order
                    generatedSections.put(pos.asLong(), (worldGenLevel, access, section, sectionPos) -> {
                        placeSection(worldGenLevel, access, section, origin, sectionPos, randomSeed, distributionSample,
                                doCrack, points, normalNoise, offsets, positions, crackSize);
                    });
                });

        return generatedSections;
    }

    // TODO refactor this code it sucks
    private void placeSection(WorldGenLevel level, BulkSectionAccess access, LevelChunkSection section, BlockPos origin,
                              SectionPos sectionPos, long randomSeed, int distributionSample,
                              boolean doCrack, List<ObjectIntPair<BlockPos>> points, NormalNoise normalNoise,
                              List<BlockPos> offsets, List<BlockPos> positions, double crackSize) {
        RandomSource random = new XoroshiroRandomSource(randomSeed);

        double wallDistance = (double) distributionSample / (double) this.outerWallDistance.getMaxValue();
        double fillingSize = 1.0 / Math.sqrt(geodeLayerSettings.filling);
        double innerSize = 1.0 / Math.sqrt(geodeLayerSettings.innerLayer + wallDistance);
        double middleSize = 1.0 / Math.sqrt(geodeLayerSettings.middleLayer + wallDistance);
        double outerSize = 1.0 / Math.sqrt(geodeLayerSettings.outerLayer + wallDistance);

        Iterable<BlockPos> allBlocks = BlockPos.betweenClosed(origin.offset(minGenOffset, minGenOffset, minGenOffset),
                origin.offset(maxGenOffset, maxGenOffset, maxGenOffset));
        for (BlockPos pos : allBlocks) {
            if (SectionPos.blockToSection(pos.asLong()) != sectionPos.asLong()) continue;

            double noiseValue = normalNoise.getValue(pos.getX(), pos.getY(), pos.getZ()) * this.noiseMultiplier;
            double filling = 0.0;
            double crack = 0.0;
            for (var pair : points) {
                filling += Mth.invSqrt(pos.distSqr(pair.first()) + (double) pair.secondInt()) + noiseValue;
            }
            for (BlockPos off : offsets) {
                crack += Mth.invSqrt(pos.distSqr(off) + (double) geodeCrackSettings.crackPointOffset) + noiseValue;
            }
            if (filling < outerSize) continue;
            if (!level.ensureCanWrite(pos)) continue;

            if (doCrack && crack >= crackSize && filling < fillingSize) {
                this.safeSetBlock(access, section, pos, Blocks.AIR.defaultBlockState());
                for (Direction direction : GTUtil.DIRECTIONS) {
                    BlockPos rel = pos.relative(direction);
                    FluidState fluidState = level.getFluidState(rel);
                    if (fluidState.isEmpty()) continue;
                    level.scheduleTick(rel, fluidState.getType(), 0);
                }
                continue;
            }
            if (filling >= fillingSize) {
                this.safeSetBlock(access, section, pos,
                        getStateFromEither(geodeBlockSettings.fillingProvider, geodeBlockSettings, random, pos));
                continue;
            }
            if (filling >= innerSize) {
                boolean useAltLayer = random.nextDouble() < this.useAlternateLayer0Chance;
                if (useAltLayer) {
                    BlockState state = getStateFromEither(geodeBlockSettings.alternateInnerLayerProvider,
                            geodeBlockSettings, random, pos);
                    this.safeSetBlock(access, section, pos, state);
                } else {
                    BlockState state = getStateFromEither(geodeBlockSettings.innerLayerProvider, geodeBlockSettings,
                            random, pos);
                    this.safeSetBlock(access, section, pos, state);
                }
                if (this.placementsRequireLayer0Alternate && !useAltLayer ||
                        random.nextDouble() >= this.usePotentialPlacementsChance)
                    continue;

                positions.add(pos.immutable());
                continue;
            }
            if (filling >= middleSize) {
                this.safeSetBlock(access, section, pos,
                        getStateFromEither(geodeBlockSettings.middleLayerProvider, geodeBlockSettings, random, pos));
                continue;
            }
            if (filling < outerSize) continue;

            this.safeSetBlock(access, section, pos,
                    getStateFromEither(geodeBlockSettings.outerLayerProvider, geodeBlockSettings, random, pos));
        }

        placementLoop:
        for (BlockPos pos : positions) {
            BlockState state = Util.getRandom(geodeBlockSettings.innerPlacements, random);
            for (Direction dir : GTUtil.DIRECTIONS) {
                if (state.hasProperty(BlockStateProperties.FACING)) {
                    state = state.setValue(BlockStateProperties.FACING, dir);
                }
                BlockPos relativePos = pos.relative(dir);
                BlockState relative = access.getBlockState(relativePos);
                if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
                    state = state.setValue(BlockStateProperties.WATERLOGGED,
                            relative.getFluidState().isSource());
                }
                if (!level.ensureCanWrite(relativePos)) continue;

                this.safeSetBlock(access, section, relativePos, state);
                continue placementLoop;
            }
        }
    }

    protected void safeSetBlock(BulkSectionAccess level, LevelChunkSection section, BlockPos pos, BlockState state) {
        if (!level.getBlockState(pos).is(this.geodeBlockSettings.cannotReplace)) {
            int x = SectionPos.sectionRelative(pos.getX());
            int y = SectionPos.sectionRelative(pos.getY());
            int z = SectionPos.sectionRelative(pos.getZ());
            section.setBlockState(x, y, z, state, false);
        }
    }

    protected BlockState getStateFromEither(Either<BlockStateProvider, Material> either, GeodeBlockSettings settings,
                                            RandomSource random, BlockPos pos) {
        return either.map(provider -> provider.getState(random, pos),
                material -> ChemicalHelper.getBlockOrThrow(settings.providerMaterialPrefix, material)
                        .defaultBlockState());
    }

    @Override
    public VeinGenerator build() {
        return this;
    }

    @Override
    public VeinGenerator copy() {
        return new GeodeVeinGenerator(this.geodeBlockSettings, this.geodeLayerSettings, this.geodeCrackSettings,
                this.usePotentialPlacementsChance, this.useAlternateLayer0Chance, this.placementsRequireLayer0Alternate,
                this.outerWallDistance, this.distributionPoints, this.pointOffset, this.minGenOffset, this.maxGenOffset,
                this.noiseMultiplier, this.invalidBlocksThreshold);
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
                                     List<BlockState> innerPlacements, TagKey<Block> cannotReplace,
                                     TagKey<Block> invalidBlocks, @NotNull TagPrefix providerMaterialPrefix) {

        public static final Codec<GeodeBlockSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.either(BlockStateProvider.CODEC, GTRegistries.MATERIALS.codec()).fieldOf("filling_provider")
                        .forGetter(config -> config.fillingProvider),
                Codec.either(BlockStateProvider.CODEC, GTRegistries.MATERIALS.codec()).fieldOf("inner_layer_provider")
                        .forGetter(config -> config.innerLayerProvider),
                Codec.either(BlockStateProvider.CODEC, GTRegistries.MATERIALS.codec())
                        .fieldOf("alternate_inner_layer_provider")
                        .forGetter(config -> config.alternateInnerLayerProvider),
                Codec.either(BlockStateProvider.CODEC, GTRegistries.MATERIALS.codec())
                        .fieldOf("middle_layer_provider").forGetter(config -> config.middleLayerProvider),
                Codec.either(BlockStateProvider.CODEC, GTRegistries.MATERIALS.codec()).fieldOf("outer_layer_provider")
                        .forGetter(config -> config.outerLayerProvider),
                ExtraCodecs.nonEmptyList(BlockState.CODEC.listOf()).fieldOf("inner_placements")
                        .forGetter(config -> config.innerPlacements),
                TagKey.hashedCodec(Registries.BLOCK).fieldOf("cannot_replace")
                        .forGetter(config -> config.cannotReplace),
                TagKey.hashedCodec(Registries.BLOCK).fieldOf("invalid_blocks")
                        .forGetter(config -> config.invalidBlocks),
                GTRegistries.TAG_PREFIXES.codec().optionalFieldOf("provider_material_prefix", TagPrefix.block)
                        .forGetter(config -> config.providerMaterialPrefix))
                .apply(instance, GeodeBlockSettings::new));
    }
}
