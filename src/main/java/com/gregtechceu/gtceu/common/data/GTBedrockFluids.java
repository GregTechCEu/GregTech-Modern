package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class GTBedrockFluids {

    public static final Map<ResourceLocation, BedrockFluidDefinition> toReRegister = new HashMap<>();

    //////////////////////////////////////
    // ******** OVERWORLD ********//
    //////////////////////////////////////
    public static BedrockFluidDefinition HEAVY_OIL = create(GTCEu.id("heavy_oil_deposit"), builder -> builder
            .fluid(GTMaterials.OilHeavy::getFluid)
            .weight(15)
            .yield(100, 200)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(20)
            .biomes(5, BiomeTags.IS_OCEAN)
            .biomes(10, CustomTags.IS_SANDY)
            .dimensions(overworld()));

    public static BedrockFluidDefinition LIGHT_OIL = create(GTCEu.id("light_oil_deposit"), builder -> builder
            .fluid(GTMaterials.OilLight::getFluid)
            .weight(25)
            .yield(175, 300)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(25)
            .dimensions(overworld()));

    public static BedrockFluidDefinition NATURAL_GAS = create(GTCEu.id("natural_gas_deposit"), builder -> builder
            .fluid(GTMaterials.NaturalGas::getFluid)
            .weight(15)
            .yield(100, 175)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(20)
            .dimensions(overworld()));

    public static BedrockFluidDefinition OIL = create(GTCEu.id("oil_deposit"), builder -> builder
            .fluid(GTMaterials.Oil::getFluid)
            .weight(20)
            .yield(175, 300)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(25)
            .biomes(5, BiomeTags.IS_OCEAN)
            .biomes(5, CustomTags.IS_SANDY)
            .dimensions(overworld()));

    public static BedrockFluidDefinition RAW_OIL = create(GTCEu.id("raw_oil_deposit"), builder -> builder
            .fluid(GTMaterials.RawOil::getFluid)
            .weight(20)
            .yield(200, 300)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(25)
            .dimensions(overworld()));

    public static BedrockFluidDefinition SALT_WATER = create(GTCEu.id("salt_water_deposit"), builder -> builder
            .fluid(GTMaterials.SaltWater::getFluid)
            .weight(0)
            .yield(50, 100)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(15)
            .dimensions(overworld())
            .biomes(200, Biomes.DEEP_OCEAN, Biomes.DEEP_COLD_OCEAN, Biomes.DEEP_FROZEN_OCEAN)
            .biomes(150, BiomeTags.IS_OCEAN));

    //////////////////////////////////////
    // ******** NETHER ********//
    //////////////////////////////////////
    public static BedrockFluidDefinition LAVA = create(GTCEu.id("lava_deposit"), builder -> builder
            .fluid(GTMaterials.Lava::getFluid)
            .weight(65)
            .yield(125, 250)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(30)
            .dimensions(nether()));

    public static BedrockFluidDefinition NETHER_NATURAL_GAS = create(GTCEu.id("nether_natural_gas_deposit"),
            builder -> builder.fluid(GTMaterials.NaturalGas::getFluid)
                    .weight(35)
                    .yield(150, 300)
                    .depletionAmount(1)
                    .depletionChance(100)
                    .depletedYield(40)
                    .dimensions(nether()));

    public static void init() {
        toReRegister.forEach(GTRegistries.BEDROCK_FLUID_DEFINITIONS::registerOrOverride);
    }

    public static BedrockFluidDefinition create(ResourceLocation id,
                                                Consumer<BedrockFluidDefinition.Builder> consumer) {
        BedrockFluidDefinition.Builder builder = BedrockFluidDefinition.builder(id);
        consumer.accept(builder);

        BedrockFluidDefinition definition = builder.build();
        toReRegister.put(id, definition);
        return definition;
    }

    public static Set<ResourceKey<Level>> nether() {
        return Set.of(Level.NETHER);
    }

    public static Set<ResourceKey<Level>> overworld() {
        return Set.of(Level.OVERWORLD);
    }
}
