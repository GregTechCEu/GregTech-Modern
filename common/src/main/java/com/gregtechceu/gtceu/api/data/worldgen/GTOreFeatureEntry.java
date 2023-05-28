package com.gregtechceu.gtceu.api.data.worldgen;

import com.gregtechceu.gtceu.common.data.GTFeatures;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author KilaBash
 * @date 2023/3/20
 * @implNote GTOreFeatureEntry
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GTOreFeatureEntry {
    public static final Map<ResourceLocation, GTOreFeatureEntry> ALL = new HashMap<>();
    public static final Codec<GTOreFeatureEntry> CODEC = ResourceLocation.CODEC.comapFlatMap(GTOreFeatureEntry::read, (entry) -> entry.id);
    public static final Codec<GTOreFeatureEntry> DIRECT_CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("id").forGetter(ft -> ft.id),
                Codec.INT.fieldOf("cluster_size").forGetter(ft -> ft.clusterSize),
                Codec.floatRange(0.0F, 1.0F).fieldOf("density").forGetter(ft -> ft.density),
                Codec.FLOAT.fieldOf("frequency").forGetter(ft -> ft.frequency),
                CountPlacement.CODEC.fieldOf("count").forGetter(ft -> ft.count),
                HeightRangePlacement.CODEC.fieldOf("height_range").forGetter(ft -> ft.range),
                Codec.floatRange(0.0F, 1.0F).fieldOf("discard_chance_on_air_exposure").forGetter(ft -> ft.discardChanceOnAirExposure),
                Codec.either(Codec.list(OreConfiguration.TargetBlockState.CODEC), Codec.list(GTLayerPattern.CODEC)).fieldOf("targets").fieldOf("layer_patterns").forGetter(ft -> ft.targets)
            ).apply(instance, GTOreFeatureEntry::new)
    );

    public final ResourceLocation id;
    public final int clusterSize;
    public final float density;
    public final float frequency;
    public final CountPlacement count;
    public final HeightRangePlacement range;
    public final float discardChanceOnAirExposure;
    public Either<List<OreConfiguration.TargetBlockState>, List<GTLayerPattern>> targets;

    public final List<PlacementModifier> modifiers;

    private DatagenExtension datagenExt;

    public GTOreFeatureEntry(ResourceLocation id, int clusterSize, float frequency, float density, CountPlacement count, HeightRangePlacement range, float discardChanceOnAirExposure, @Nullable Either<List<OreConfiguration.TargetBlockState>, List<GTLayerPattern>> targets) {
        this.id = id;
        this.clusterSize = clusterSize;
        this.density = density;
        this.frequency = frequency;
        this.count = count;
        this.range = range;
        this.discardChanceOnAirExposure = discardChanceOnAirExposure;
        this.targets = targets;

        this.modifiers = List.of(
                this.count,
                new FrequencyModifier(this.frequency),
                InSquarePlacement.spread(),
                this.range
        );
        ALL.put(id, this);
    }

    public StandardDatagenExtension standardDatagenExt() {
        if (this.datagenExt == null) {
            this.datagenExt = new GTOreFeatureEntry.StandardDatagenExtension();
        }
        return (StandardDatagenExtension) datagenExt;
    }

    public LayeredDatagenExtension layeredDatagenExt() {
        if (datagenExt == null) {
            datagenExt = new LayeredDatagenExtension();
        }
        return (LayeredDatagenExtension) datagenExt;
    }

    @Nullable
    public DatagenExtension datagenExt() {
        return this.datagenExt != null ? this.datagenExt : null;
    }

    public String getName() {
        return this.id.getPath();
    }

    public static DataResult<GTOreFeatureEntry> read(ResourceLocation id) {
        GTOreFeatureEntry entry = ALL.get(id);
        return entry != null ? DataResult.success(entry) : DataResult.error("Not a valid GTOreFeature: " + id);
    }

    public abstract class DatagenExtension {
        public TagKey<Biome> biomeTag;

        public DatagenExtension() {
        }

        public GTOreFeatureEntry.DatagenExtension biomeTag(TagKey<Biome> biomes) {
            this.biomeTag = biomes;
            return this;
        }

        public ConfiguredFeature<?, ?> createConfiguredFeature() {
            build();
            GTOreFeatureConfiguration config = new GTOreFeatureConfiguration(GTOreFeatureEntry.this);
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

        public abstract DatagenExtension build();

        public GTOreFeatureEntry parent() {
            return GTOreFeatureEntry.this;
        }
    }

    public class StandardDatagenExtension extends GTOreFeatureEntry.DatagenExtension {
        public NonNullSupplier<? extends Block> block;
        public NonNullSupplier<? extends Block> deepBlock;
        public NonNullSupplier<? extends Block> netherBlock;

        public StandardDatagenExtension() {
            super();
        }

        public GTOreFeatureEntry.StandardDatagenExtension withBlock(NonNullSupplier<? extends Block> block) {
            this.block = block;
            this.deepBlock = block;
            return this;
        }

        public GTOreFeatureEntry.StandardDatagenExtension withNetherBlock(NonNullSupplier<? extends Block> block) {
            this.netherBlock = block;
            return this;
        }

        public GTOreFeatureEntry.StandardDatagenExtension biomeTag(TagKey<Biome> biomes) {
            super.biomeTag(biomes);
            return this;
        }

        public DatagenExtension build() {
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

            GTOreFeatureEntry.this.targets = Either.left(targetStates);
            return this;
        }
    }

    public class LayeredDatagenExtension extends DatagenExtension {
        public final List<NonNullSupplier<GTLayerPattern>> layerPatterns = new ArrayList<>();

        public LayeredDatagenExtension withLayerPattern(NonNullSupplier<GTLayerPattern> pattern) {
            this.layerPatterns.add(pattern);
            return this;
        }

        @Override
        public LayeredDatagenExtension biomeTag(TagKey<Biome> biomes) {
            super.biomeTag(biomes);
            return this;
        }

        public DatagenExtension build() {
            List<GTLayerPattern> layerPatterns = this.layerPatterns.stream()
                    .map(NonNullSupplier::get)
                    .toList();
            GTOreFeatureEntry.this.targets = Either.right(layerPatterns);
            return this;
        }
    }

}
