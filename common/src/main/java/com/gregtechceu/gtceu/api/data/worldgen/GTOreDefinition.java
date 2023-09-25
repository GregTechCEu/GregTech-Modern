package com.gregtechceu.gtceu.api.data.worldgen;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.worldgen.generator.*;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Supplier;

/**
 * @author Screret
 * @date 2023/6/14
 * @implNote GTOreDefinition
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@Accessors(chain = true)
public class GTOreDefinition {
    public static final Codec<GTOreDefinition> CODEC = ResourceLocation.CODEC
            .flatXmap(rl -> Optional.ofNullable(GTRegistries.ORE_VEINS.get(rl))
                            .map(DataResult::success)
                            .orElseGet(() -> DataResult.error(() -> "No GTOreDefinition with id " + rl + " registered")),
                    obj -> Optional.ofNullable(GTRegistries.ORE_VEINS.getKey(obj))
                            .map(DataResult::success)
                            .orElseGet(() -> DataResult.error(() -> "GTOreDefinition " + obj + " not registered")));
    public static final Codec<GTOreDefinition> FULL_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.INT.fieldOf("cluster_size").forGetter(ft -> ft.clusterSize),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("density").forGetter(ft -> ft.density),
                    Codec.INT.fieldOf("weight").forGetter(ft -> ft.weight),
                    IWorldGenLayer.CODEC.fieldOf("layer").forGetter(ft -> ft.layer),
                    ResourceKey.codec(Registries.DIMENSION).listOf().fieldOf("dimension_filter").forGetter(ft -> new ArrayList<>(ft.dimensionFilter)),
                    HeightRangePlacement.CODEC.fieldOf("height_range").forGetter(ft -> ft.range),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("discard_chance_on_air_exposure").forGetter(ft -> ft.discardChanceOnAirExposure),
                    RegistryCodecs.homogeneousList(Registries.BIOME).optionalFieldOf("biomes", null).forGetter(ext -> ext.biomes == null ? null : ext.biomes.get()),
                    BiomeWeightModifier.CODEC.optionalFieldOf("weight_modifier", null).forGetter(ext -> ext.biomeWeightModifier),
                    VeinGenerator.DIRECT_CODEC.fieldOf("generator").forGetter(ft -> ft.veinGenerator)
            ).apply(instance, (clusterSize, density, weight, layer, dimensionFilter, range, discardChanceOnAirExposure, biomes, biomeWeightModifier, veinGenerator) ->
                    new GTOreDefinition(clusterSize, density, weight, layer, new HashSet<>(dimensionFilter), range, discardChanceOnAirExposure, biomes == null ? null : () -> biomes, biomeWeightModifier, veinGenerator))
    );

    @Getter @Setter
    private int clusterSize;
    @Getter @Setter
    private float density;
    @Getter @Setter
    private int weight;
    @Getter @Setter
    private IWorldGenLayer layer;
    @Getter @Setter
    private Set<ResourceKey<Level>> dimensionFilter;
    @Getter @Setter
    private HeightRangePlacement range;
    @Getter @Setter
    private float discardChanceOnAirExposure;
    @Getter @Setter
    private Supplier<HolderSet<Biome>> biomes;
    @Getter @Setter
    private BiomeWeightModifier biomeWeightModifier;

    @Getter
    private List<PlacementModifier> modifiers;

    @Getter @Setter
    private VeinGenerator veinGenerator;

    @Getter @Setter
    private int minimumYield, maximumYield, depletedYield, depletionChance, depletionAmount = 1;
    @Setter
    private List<Map.Entry<Integer, Material>> bedrockVeinMaterial;

    public GTOreDefinition(ResourceLocation id, int clusterSize, float density, int weight, IWorldGenLayer layer, Set<ResourceKey<Level>> dimensionFilter, HeightRangePlacement range, float discardChanceOnAirExposure, @Nullable Supplier<HolderSet<Biome>> biomes, @Nullable BiomeWeightModifier biomeWeightModifier, @Nullable VeinGenerator veinGenerator) {
        this(clusterSize, density, weight, layer, dimensionFilter, range, discardChanceOnAirExposure, biomes, biomeWeightModifier, veinGenerator);
        if (GTRegistries.ORE_VEINS.containKey(id)) {
            GTRegistries.ORE_VEINS.replace(id, this);
        } else {
            GTRegistries.ORE_VEINS.register(id, this);
        }
    }

    public GTOreDefinition(int clusterSize, float density, int weight, IWorldGenLayer layer, Set<ResourceKey<Level>> dimensionFilter, HeightRangePlacement range, float discardChanceOnAirExposure, @Nullable Supplier<HolderSet<Biome>> biomes, @Nullable BiomeWeightModifier biomeWeightModifier, @Nullable VeinGenerator veinGenerator) {
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

        this.maximumYield = (int) (density * 100) * clusterSize;
        this.minimumYield = this.maximumYield / 7;
        this.depletedYield = (int) (clusterSize / density / 10);
        this.depletionChance = (int) (weight * density / 5);
    }

    public GTOreDefinition biomes(TagKey<Biome> biomes) {
        this.biomes = () -> GTRegistries.builtinRegistry().lookupOrThrow(Registries.BIOME).getOrThrow(biomes);
        return this;
    }

    public GTOreDefinition biomes(Supplier<HolderSet<Biome>> biomes) {
        this.biomes = biomes;
        return this;
    }

    public GTOreDefinition range(HeightRangePlacement range) {
        this.range = range;
        return this;
    }

    public List<Map.Entry<Integer, Material>> getBedrockVeinMaterials() {
        if (bedrockVeinMaterial == null) {
            if (ConfigHolder.INSTANCE.machines.doBedrockOres) {
                bedrockVeinMaterial = this.getVeinGenerator().getValidMaterialsChances();
            } else {
                bedrockVeinMaterial = List.of();
            }
        }
        return bedrockVeinMaterial;
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

    public GeodeVeinGenerator geodeVeinGenerator() {
        if (veinGenerator == null) {
            veinGenerator = new GeodeVeinGenerator(this);
        }
        return (GeodeVeinGenerator) veinGenerator;
    }

    public DikeVeinGenerator dikeVeinGenerator() {
        if (veinGenerator == null) {
            veinGenerator = new DikeVeinGenerator(this);
        }
        return (DikeVeinGenerator) veinGenerator;
    }

    public VeinedVeinGenerator veinedVeinGenerator() {
        if (veinGenerator == null) {
            veinGenerator = new VeinedVeinGenerator(this);
        }
        return (VeinedVeinGenerator) veinGenerator;
    }

    @Nullable
    public VeinGenerator generator(ResourceLocation id) {
        if (veinGenerator == null) {
            veinGenerator = WorldGeneratorUtils.VEIN_GENERATOR_FUNCTIONS.containsKey(id) ? WorldGeneratorUtils.VEIN_GENERATOR_FUNCTIONS.get(id).apply(this) : null;
        }
        return veinGenerator;
    }

}
