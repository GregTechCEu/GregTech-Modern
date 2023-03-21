package com.gregtechceu.gtceu.api.data.worldgen;

import com.gregtechceu.gtceu.common.data.GTFeatures;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.*;
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
    public static final Codec<GTOreFeatureEntry> CODEC;
    static {
        CODEC = ResourceLocation.CODEC.comapFlatMap(GTOreFeatureEntry::read, (entry) -> entry.id);
    }
    public final ResourceLocation id;
    public final int clusterSize;
    public final float frequency;
    public final CountPlacement count;
    public final HeightRangePlacement range;
    private DatagenExtension datagenExt;

    public GTOreFeatureEntry(ResourceLocation id, int clusterSize, float frequency, CountPlacement count, HeightRangePlacement range) {
        this.id = id;
        this.clusterSize = clusterSize;
        this.frequency = frequency;
        this.count = count;
        this.range = range;
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

        public abstract ConfiguredFeature<?, ?> createConfiguredFeature(RegistryAccess var1);

        public PlacedFeature createPlacedFeature(RegistryAccess registryAccess) {
            Registry<ConfiguredFeature<?, ?>> featureRegistry = registryAccess.registryOrThrow(Registry.CONFIGURED_FEATURE_REGISTRY);
            Holder<ConfiguredFeature<?, ?>> featureHolder = featureRegistry.getOrCreateHolderOrThrow(ResourceKey.create(Registry.CONFIGURED_FEATURE_REGISTRY, GTOreFeatureEntry.this.id));
            return new PlacedFeature(featureHolder, List.of(
                    count,
                    new FrequencyModifier(frequency),
                    range
            ));
        }

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

        public ConfiguredFeature<?, ?> createConfiguredFeature(RegistryAccess registryAccess) {
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

            GTOreFeatureConfiguration config = new GTOreFeatureConfiguration(GTOreFeatureEntry.this, 0.0F, targetStates);
            return new ConfiguredFeature<>(GTFeatures.ORE, config);
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

        @Override
        public ConfiguredFeature<?, ?> createConfiguredFeature(RegistryAccess registryAccess) {
            List<GTLayerPattern> layerPatterns = this.layerPatterns.stream()
                    .map(NonNullSupplier::get)
                    .toList();

            GTLayerOreFeatureConfiguration config = new GTLayerOreFeatureConfiguration(GTOreFeatureEntry.this, 0, layerPatterns);
            return new ConfiguredFeature<>(GTFeatures.LAYER_ORE, config);
        }
    }

}
