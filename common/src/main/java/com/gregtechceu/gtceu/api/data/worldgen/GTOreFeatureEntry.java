package com.gregtechceu.gtceu.api.data.worldgen;

import com.google.common.collect.HashBiMap;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.worldgen.generator.BiomeFilter;
import com.gregtechceu.gtceu.api.data.worldgen.generator.VeinCountFilter;
import com.gregtechceu.gtceu.api.data.worldgen.generator.WorldGeneratorUtils;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTFeatures;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.*;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.*;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Screret
 * @date 2023/6/14
 * @implNote GTOreFeatureEntry
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@Accessors(chain = true)
public class GTOreFeatureEntry {
    public static final HashBiMap<ResourceLocation, GTOreFeatureEntry> ALL = HashBiMap.create();


    public static final Codec<GTOreFeatureEntry> CODEC = ResourceLocation.CODEC
            .flatXmap(rl -> Optional.ofNullable(ALL.get(rl))
                            .map(DataResult::success)
                            .orElseGet(() -> DataResult.error("No GTOreFeatureEntry with id " + rl + " registered")),
                    obj -> Optional.ofNullable(ALL.inverse().get(obj))
                            .map(DataResult::success)
                            .orElseGet(() -> DataResult.error("GTOreFeatureEntry " + obj + " not registered")));
    public static final Codec<GTOreFeatureEntry> FULL_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.INT.fieldOf("cluster_size").forGetter(ft -> ft.clusterSize),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("density").forGetter(ft -> ft.density),
                    Codec.INT.fieldOf("weight").forGetter(ft -> ft.weight),
                    IWorldGenLayer.CODEC.fieldOf("layer").forGetter(ft -> ft.layer),
                    RegistryCodecs.homogeneousList(Registry.DIMENSION_TYPE_REGISTRY).fieldOf("dimension_filter").forGetter(ft -> ft.dimensionFilter),
                    HeightRangePlacement.CODEC.fieldOf("height_range").forGetter(ft -> ft.range),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("discard_chance_on_air_exposure").forGetter(ft -> ft.discardChanceOnAirExposure),
                    RegistryCodecs.homogeneousList(Registry.BIOME_REGISTRY).fieldOf("biomes").forGetter(ext -> ext.biomes),
                    BiomeWeightModifier.CODEC.optionalFieldOf("weight_modifier", null).forGetter(ext -> ext.biomeWeightModifier),
                    VeinGenerator.DIRECT_CODEC.fieldOf("generator").forGetter(ft -> ft.veinGenerator)
            ).apply(instance, GTOreFeatureEntry::new)
    );

    public int clusterSize;
    public float density;
    public int weight;
    public IWorldGenLayer layer;
    public HolderSet<DimensionType> dimensionFilter;
    @Getter
    protected HeightRangePlacement range;
    public float discardChanceOnAirExposure;
    public HolderSet<Biome> biomes;
    public BiomeWeightModifier biomeWeightModifier;

    @Getter
    private List<PlacementModifier> modifiers;

    @Getter @Setter
    private VeinGenerator veinGenerator;

    public GTOreFeatureEntry(ResourceLocation id, int clusterSize, float density, int weight, IWorldGenLayer layer, HolderSet<DimensionType> dimensionFilter, HeightRangePlacement range, float discardChanceOnAirExposure, @Nullable HolderSet<Biome> biomes, @Nullable BiomeWeightModifier biomeWeightModifier, @Nullable GTOreFeatureEntry.VeinGenerator veinGenerator) {
        this(clusterSize, density, weight, layer, dimensionFilter, range, discardChanceOnAirExposure, biomes, biomeWeightModifier, veinGenerator);
        ALL.put(id, this);
    }

    public GTOreFeatureEntry(int clusterSize, float density, int weight, IWorldGenLayer layer, HolderSet<DimensionType> dimensionFilter, HeightRangePlacement range, float discardChanceOnAirExposure, @Nullable HolderSet<Biome> biomes, @Nullable BiomeWeightModifier biomeWeightModifier, @Nullable GTOreFeatureEntry.VeinGenerator veinGenerator) {
        this.clusterSize = clusterSize;
        this.density = density;
        this.weight = weight;
        this.layer = layer;
        this.dimensionFilter = dimensionFilter;
        this.range = range;
        this.discardChanceOnAirExposure = discardChanceOnAirExposure;
        this.biomes = biomes;
        this.biomeWeightModifier = biomeWeightModifier;
        this.veinGenerator = veinGenerator;

        this.modifiers = List.of(
                VeinCountFilter.count(),
                BiomeFilter.biome(),
                InSquarePlacement.spread(),
                this.range
        );
    }

    public GTOreFeatureEntry biomes(TagKey<Biome> biomes) {
        this.biomes = BuiltinRegistries.BIOME.getOrCreateTag(biomes);
        return this;
    }

    public GTOreFeatureEntry biomes(HolderSet<Biome> biomes) {
        this.biomes = biomes;
        return this;
    }

    public GTOreFeatureEntry range(HeightRangePlacement range) {
        this.range = range;
        this.modifiers = List.of(
                VeinCountFilter.count(),
                BiomeFilter.biome(),
                InSquarePlacement.spread(),
                this.range
        );
        return this;
    }

    public StandardVeinGenerator standardVeinGenerator() {
        if (this.veinGenerator == null) {
            this.veinGenerator = new StandardVeinGenerator(this);
        }
        return (StandardVeinGenerator) veinGenerator;
    }

    public LayeredVeinGenerator layeredVeinGenerator() {
        if (veinGenerator == null) {
            veinGenerator = new LayeredVeinGenerator(this);
        }
        return (LayeredVeinGenerator) veinGenerator;
    }

    public static abstract class VeinGenerator {
        public static final Codec<Codec<? extends VeinGenerator>> REGISTRY_CODEC = ResourceLocation.CODEC
                .flatXmap(rl -> Optional.ofNullable(WorldGeneratorUtils.VEIN_GENERATORS.get(rl))
                                .map(DataResult::success)
                                .orElseGet(() -> DataResult.error("No VeinGenerator with id " + rl + " registered")),
                        obj -> Optional.ofNullable(WorldGeneratorUtils.VEIN_GENERATORS.inverse().get(obj))
                                .map(DataResult::success)
                                .orElseGet(() -> DataResult.error("VeinGenerator " + obj + " not registered")));
        public static final Codec<VeinGenerator> DIRECT_CODEC = REGISTRY_CODEC.dispatchStable(VeinGenerator::codec, Function.identity());

        protected GTOreFeatureEntry entry;

        public VeinGenerator() {
        }

        public VeinGenerator(GTOreFeatureEntry entry) {
            this.entry = entry;
        }

        public ConfiguredFeature<?, ?> createConfiguredFeature() {
            build();
            GTOreFeatureConfiguration config = new GTOreFeatureConfiguration(entry);
            return new ConfiguredFeature<>(GTFeatures.ORE, config);
        }

        /*public PlacedFeature createPlacedFeature(RegistryAccess registryAccess) {
            Registry<ConfiguredFeature<?, ?>> featureRegistry = registryAccess.registryOrThrow(Registry.CONFIGURED_FEATURE_REGISTRY);
            Holder<ConfiguredFeature<?, ?>> featureHolder = featureRegistry.getOrCreateHolderOrThrow(ResourceKey.create(Registry.CONFIGURED_FEATURE_REGISTRY, GTOreFeatureEntry.this.id));
            return new PlacedFeature(featureHolder, List.of(
                this.count,
                new FrequencyModifier(this.frequency),
                InSquarePlacement.spread()
                this.range
            ));
        }*/

        public abstract boolean generate(WorldGenLevel level, RandomSource random, GTOreFeatureEntry entry, BlockPos origin);

        public abstract VeinGenerator build();

        public GTOreFeatureEntry parent() {
            return entry;
        }

        public abstract Codec<? extends VeinGenerator> codec();
    }

    public static class NoopVeinGenerator extends VeinGenerator {
        public static final NoopVeinGenerator INSTANCE = new NoopVeinGenerator();
        public static final Codec<NoopVeinGenerator> CODEC = Codec.unit(() -> INSTANCE);

        @Override
        public boolean generate(WorldGenLevel level, RandomSource random, GTOreFeatureEntry entry, BlockPos origin) {
            return true;
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

    public static class StandardVeinGenerator extends VeinGenerator {
        public static final Codec<StandardVeinGenerator> CODEC_SEPARATE = RecordCodecBuilder.create(instance -> instance.group(
                Registry.BLOCK.byNameCodec().fieldOf("block").forGetter(ext -> ext.block.get()),
                Registry.BLOCK.byNameCodec().fieldOf("deep_block").forGetter(ext -> ext.deepBlock.get()),
                Registry.BLOCK.byNameCodec().fieldOf("nether_block").forGetter(ext -> ext.netherBlock.get())
        ).apply(instance, StandardVeinGenerator::new));
        public static final Codec<StandardVeinGenerator> CODEC_LIST = RecordCodecBuilder.create(instance -> instance.group(
                Codec.either(OreConfiguration.TargetBlockState.CODEC.listOf(), GTRegistries.MATERIALS.codec()).fieldOf("targets").forGetter(ext -> ext.blocks)
        ).apply(instance, StandardVeinGenerator::new));
        public static final Codec<StandardVeinGenerator> CODEC = Codec.either(CODEC_SEPARATE, CODEC_LIST).xmap(either -> either.map(Function.identity(), Function.identity()), Either::left);

        public NonNullSupplier<? extends Block> block;
        public NonNullSupplier<? extends Block> deepBlock;
        public NonNullSupplier<? extends Block> netherBlock;

        public Either<List<OreConfiguration.TargetBlockState>, Material> blocks;

        public StandardVeinGenerator(GTOreFeatureEntry entry) {
            super(entry);
        }

        public StandardVeinGenerator(Block block, Block deepBlock, Block netherBlock) {
            this.block = NonNullSupplier.of(() -> block);
            this.deepBlock = NonNullSupplier.of(() -> deepBlock);
            this.netherBlock = NonNullSupplier.of(() -> netherBlock);
        }

        public StandardVeinGenerator(Either<List<OreConfiguration.TargetBlockState>, Material> blocks) {
            this.blocks = blocks;
        }

        public StandardVeinGenerator withBlock(NonNullSupplier<? extends Block> block) {
            this.block = block;
            this.deepBlock = block;
            return this;
        }

        public StandardVeinGenerator withNetherBlock(NonNullSupplier<? extends Block> block) {
            this.netherBlock = block;
            return this;
        }

        public StandardVeinGenerator withMaterial(Material material) {
            this.blocks = Either.right(material);
            return this;
        }

        public VeinGenerator build() {
            if (this.blocks != null) return this;
            // if (this.blocks.left().isPresent() && !this.blocks.left().get().isEmpty()) return this;
            List<OreConfiguration.TargetBlockState> targetStates = new ArrayList<>();
            if (this.block != null) {
                targetStates.add(OreConfiguration.target(OreFeatures.STONE_ORE_REPLACEABLES, this.block.get().defaultBlockState()));
            }

            if (this.deepBlock != null) {
                targetStates.add(OreConfiguration.target(OreFeatures.DEEPSLATE_ORE_REPLACEABLES, this.deepBlock.get().defaultBlockState()));
            }

            if (this.netherBlock != null) {
                targetStates.add(OreConfiguration.target(OreFeatures.NETHER_ORE_REPLACEABLES, this.netherBlock.get().defaultBlockState()));
            }

            this.blocks = Either.left(targetStates);
            return this;
        }

        @Override
        public Codec<? extends VeinGenerator> codec() {
            return CODEC;
        }

        @Override
        public boolean generate(WorldGenLevel level, RandomSource random, GTOreFeatureEntry entry, BlockPos origin) {
            float f = random.nextFloat() * (float)Math.PI;
            float f1 = (float)entry.clusterSize / 8.0F;
            int i = Mth.ceil(((float)entry.clusterSize / 16.0F * 2.0F + 1.0F) / 2.0F);
            double d0 = origin.getX() + Math.sin(f) * f1;
            double d1 = origin.getX() - Math.sin(f) * f1;
            double d2 = origin.getZ() + Math.cos(f) * f1;
            double d3 = origin.getZ() - Math.cos(f) * f1;
            double d4 = origin.getY() + random.nextInt(3) - 2;
            double d5 = origin.getY() + random.nextInt(3) - 2;
            int k = origin.getX() - Mth.ceil(f1) - i;
            int l = origin.getY() - 2 - i;
            int i1 = origin.getZ() - Mth.ceil(f1) - i;
            int j1 = 2 * (Mth.ceil(f1) + i);
            int k1 = 2 * (2 + i);

            for(int l1 = k; l1 <= k + j1; ++l1) {
                for(int i2 = i1; i2 <= i1 + j1; ++i2) {
                    if (l <= level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, l1, i2)) {
                        if (this.doPlaceNormal(level, random, entry, this.blocks, d0, d1, d2, d3, d4, d5, k, l, i1, j1, k1)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        protected boolean doPlaceNormal(WorldGenLevel level, RandomSource random, GTOreFeatureEntry entry, Either<List<OreConfiguration.TargetBlockState>, Material> targets,
                                        double pMinX, double pMaxX, double pMinZ, double pMaxZ, double pMinY, double pMaxY, int pX, int pY, int pZ,
                                        int pWidth, int pHeight) {
            MutableInt placedAmount = new MutableInt(1);
            BitSet placedBlocks = new BitSet(pWidth * pHeight * pWidth);
            BlockPos.MutableBlockPos posCursor = new BlockPos.MutableBlockPos();
            int size = entry.clusterSize;
            float density = entry.density;
            double[] shape = new double[size * 4];

            for(int k = 0; k < size; ++k) {
                float f = (float)k / (float)size;
                double d0 = Mth.lerp(f, pMinX, pMaxX);
                double d1 = Mth.lerp(f, pMinY, pMaxY);
                double d2 = Mth.lerp(f, pMinZ, pMaxZ);
                double d3 = random.nextDouble() * (double)size / 16.0D;
                double d4 = ((double)(Mth.sin((float)Math.PI * f) + 1.0F) * d3 + 1.0D) / 2.0D;
                shape[k * 4] = d0;
                shape[k * 4 + 1] = d1;
                shape[k * 4 + 2] = d2;
                shape[k * 4 + 3] = d4;
            }

            for(int l3 = 0; l3 < size - 1; ++l3) {
                if (!(shape[l3 * 4 + 3] <= 0.0D)) {
                    for(int i4 = l3 + 1; i4 < size; ++i4) {
                        if (!(shape[i4 * 4 + 3] <= 0.0D)) {
                            double d8 = shape[l3 * 4] - shape[i4 * 4];
                            double d10 = shape[l3 * 4 + 1] - shape[i4 * 4 + 1];
                            double d12 = shape[l3 * 4 + 2] - shape[i4 * 4 + 2];
                            double d14 = shape[l3 * 4 + 3] - shape[i4 * 4 + 3];
                            if (d14 * d14 > d8 * d8 + d10 * d10 + d12 * d12) {
                                if (d14 > 0.0D) {
                                    shape[i4 * 4 + 3] = -1.0D;
                                } else {
                                    shape[l3 * 4 + 3] = -1.0D;
                                }
                            }
                        }
                    }
                }
            }

            BulkSectionAccess access = new BulkSectionAccess(level);

            try {
                for(int j4 = 0; j4 < size; ++j4) {
                    double d9 = shape[j4 * 4 + 3];
                    if (!(d9 < 0.0D)) {
                        double x = shape[j4 * 4];
                        double y = shape[j4 * 4 + 1];
                        double z = shape[j4 * 4 + 2];
                        int k4 = Math.max(Mth.floor(x - d9), pX);
                        int l = Math.max(Mth.floor(y - d9), pY);
                        int i1 = Math.max(Mth.floor(z - d9), pZ);
                        int j1 = Math.max(Mth.floor(x + d9), k4);
                        int k1 = Math.max(Mth.floor(y + d9), l);
                        int l1 = Math.max(Mth.floor(z + d9), i1);

                        for(int posX = k4; posX <= j1; ++posX) {
                            double radX = ((double)posX + 0.5D - x) / d9;
                            if (radX * radX < 1.0D) {
                                for(int posY = l; posY <= k1; ++posY) {
                                    double radY = ((double)posY + 0.5D - y) / d9;
                                    if (radX * radX + radY * radY < 1.0D) {
                                        for(int posZ = i1; posZ <= l1; ++posZ) {
                                            double radZ = ((double)posZ + 0.5D - z) / d9;
                                            if (radX * radX + radY * radY + radZ * radZ < 1.0D && !level.isOutsideBuildHeight(posY)) {
                                                int isPlaced = posX - pX + (posY - pY) * pWidth + (posZ - pZ) * pWidth * pHeight;
                                                if (!placedBlocks.get(isPlaced)) {
                                                    placedBlocks.set(isPlaced);
                                                    posCursor.set(posX, posY, posZ);
                                                    if (level.ensureCanWrite(posCursor)) {
                                                        LevelChunkSection levelchunksection = access.getSection(posCursor);
                                                        if (levelchunksection != null) {
                                                            int i3 = SectionPos.sectionRelative(posX);
                                                            int j3 = SectionPos.sectionRelative(posY);
                                                            int k3 = SectionPos.sectionRelative(posZ);
                                                            BlockState blockstate = levelchunksection.getBlockState(i3, j3, k3);

                                                            if (random.nextFloat() <= density) {
                                                                targets.ifLeft(blockStates -> {
                                                                    for(OreConfiguration.TargetBlockState targetState : blockStates) {
                                                                        if (GTOreFeature.canPlaceOre(blockstate, access::getBlockState, random, entry, targetState, posCursor)) {
                                                                            levelchunksection.setBlockState(i3, j3, k3, targetState.state, false);
                                                                            placedAmount.increment();
                                                                            break;
                                                                        }
                                                                    }
                                                                }).ifRight(material -> {
                                                                    if (!GTOreFeature.canPlaceOre(blockstate, access::getBlockState, random, entry, material, posCursor))
                                                                        return;
                                                                    BlockState currentState = access.getBlockState(posCursor);
                                                                    var prefix = ChemicalHelper.ORES_INVERSE.get(currentState);
                                                                    if (prefix == null) return;
                                                                    Block toPlace = ChemicalHelper.getBlock(prefix, material);
                                                                    if (toPlace == null || toPlace.defaultBlockState().isAir())
                                                                        return;
                                                                    levelchunksection.setBlockState(i3, j3, k3, toPlace.defaultBlockState(), false);
                                                                    placedAmount.increment();
                                                                });
                                                            }

                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
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
    }

    public static class LayeredVeinGenerator extends VeinGenerator {
        public static final Codec<LayeredVeinGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        GTLayerPattern.CODEC.listOf().fieldOf("layer_patterns").forGetter(ft -> ft.layerPatterns != null ? ft.layerPatterns : ft.bakingLayerPatterns.stream().map(Supplier::get).collect(Collectors.toList()))
                ).apply(instance, LayeredVeinGenerator::new)
        );

        private final List<NonNullSupplier<GTLayerPattern>> bakingLayerPatterns = new ArrayList<>();

        public List<GTLayerPattern> layerPatterns;

        public LayeredVeinGenerator(GTOreFeatureEntry entry) {
            super(entry);
        }

        @Override
        public boolean generate(WorldGenLevel level, RandomSource random, GTOreFeatureEntry entry, BlockPos origin) {
            var patternPool = this.layerPatterns;

            if (patternPool.isEmpty())
                return false;

            GTLayerPattern layerPattern = patternPool.get(random.nextInt(patternPool.size()));

            MutableInt placedAmount = new MutableInt(0);
            int size = entry.clusterSize;
            float density = entry.density;
            int radius = Mth.ceil(entry.clusterSize / 2f);
            int x0 = origin.getX() - radius;
            int y0 = origin.getY() - radius;
            int z0 = origin.getZ() - radius;
            int width = size + 1;
            int length = size + 1;
            int height = size + 1;

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

                for (int xC = 0; xC < width; xC++) {
                    float dx = xC * 2f / width - 1;
                    if (dx * dx > 1)
                        continue;

                    for (int yC = 0; yC < height; yC++) {
                        float dy = yC * 2f / height - 1;
                        if (dx * dx + dy * dy > 1)
                            continue;
                        if (level.isOutsideBuildHeight(y0 + yC))
                            continue;

                        for (int zC = 0; zC < length; zC++) {
                            float dz = zC * 2f / height - 1;

                            int layerIndex = layerCoordinate == 0 ? zC : layerCoordinate == 1 ? xC : yC;
                            if (slantyCoordinate != layerCoordinate)
                                layerIndex += Mth.floor(slantyCoordinate == 0 ? zC : slantyCoordinate == 1 ? xC : yC) * slope;

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

                            if (dx * dx + dy * dy + dz * dz > 1 * layerDiameterOffsets.get(layerIndex))
                                continue;

                            GTLayerPattern.Layer layer = resolvedLayers.get(layerIndex);
                            Either<List<OreConfiguration.TargetBlockState>, Material> state = layer.rollBlock(random);

                            int currentX = x0 + xC;
                            int currentY = y0 + yC;
                            int currentZ = z0 + zC;

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
                                    if (!GTOreFeature.canPlaceOre(blockstate, access::getBlockState, random, entry, material, posCursor))
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
        public Codec<? extends VeinGenerator> codec() {
            return CODEC;
        }
    }

}
