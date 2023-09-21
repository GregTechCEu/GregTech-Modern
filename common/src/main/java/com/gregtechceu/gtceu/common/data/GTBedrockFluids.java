package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author KilaBash
 * @date 2023/7/11
 * @implNote GTBedrockFluids
 */
public class GTBedrockFluids {
    public static final Map<ResourceLocation, BedrockFluidDefinition> toReRegister = new HashMap<>();


    //////////////////////////////////////
    //********     OVERWORLD    ********//
    //////////////////////////////////////
    public static BedrockFluidDefinition HEAVY_OIL = BedrockFluidDefinition.builder(GTCEu.id("heavy_oil_deposit"))
            .fluid(GTMaterials.OilHeavy::getFluid)
            .weight(15)
            .yield(100, 200)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(20)
            .biomes(5, BiomeTags.IS_OCEAN)
            .biomes(10, CustomTags.IS_SANDY)
            .register();

    public static BedrockFluidDefinition LIGHT_OIL = BedrockFluidDefinition.builder(GTCEu.id("light_oil_deposit"))
            .fluid(GTMaterials.OilLight::getFluid)
            .weight(25)
            .yield(175, 300)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(25)
            .register();

    public static BedrockFluidDefinition NATURAL_GAS = BedrockFluidDefinition.builder(GTCEu.id("natural_gas_deposit"))
            .fluid(GTMaterials.NaturalGas::getFluid)
            .weight(15)
            .yield(100, 175)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(20)
            .register();

    public static BedrockFluidDefinition OIL = BedrockFluidDefinition.builder(GTCEu.id("oil_deposit"))
            .fluid(GTMaterials.Oil::getFluid)
            .weight(20)
            .yield(175, 300)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(25)
            .biomes(5, BiomeTags.IS_OCEAN)
            .biomes(5, CustomTags.IS_SANDY)
            .register();

    public static BedrockFluidDefinition RAW_OIL = BedrockFluidDefinition.builder(GTCEu.id("raw_oil_deposit"))
            .fluid(GTMaterials.RawOil::getFluid)
            .weight(20)
            .yield(200, 300)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(25)
            .register();

    public static BedrockFluidDefinition SALT_WATER = BedrockFluidDefinition.builder(GTCEu.id("salt_water_deposit"))
            .fluid(GTMaterials.SaltWater::getFluid)
            .weight(0)
            .yield(50, 100)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(15)
            .biomes(200, Biomes.DEEP_OCEAN, Biomes.DEEP_COLD_OCEAN, Biomes.DEEP_FROZEN_OCEAN)
            .biomes(150, BiomeTags.IS_OCEAN)
            .register();

    //////////////////////////////////////
    //********     OVERWORLD    ********//
    //////////////////////////////////////
    public static BedrockFluidDefinition LAVA = BedrockFluidDefinition.builder(GTCEu.id("lava_deposit"))
            .fluid(GTMaterials.Lava::getFluid)
            .weight(65)
            .yield(125, 250)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(30)
            .dimensions(GTBedrockFluids::nether)
            .register();

    public static BedrockFluidDefinition NETHER_NATURAL_GAS = BedrockFluidDefinition.builder(GTCEu.id("nether_natural_gas_deposit"))
            .fluid(GTMaterials.NaturalGas::getFluid)
            .weight(35)
            .yield(150, 300)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(40)
            .dimensions(GTBedrockFluids::nether)
            .register();

    public static HolderSet<DimensionType> overworld() {
        return HolderSet.direct(GTRegistries.builtinRegistry().registryOrThrow(Registries.DIMENSION_TYPE).getHolderOrThrow(BuiltinDimensionTypes.OVERWORLD));
    }

    public static HolderSet<DimensionType> nether() {
        return HolderSet.direct(GTRegistries.builtinRegistry().registryOrThrow(Registries.DIMENSION_TYPE).getHolderOrThrow(BuiltinDimensionTypes.NETHER));
    }

    public static HolderSet<DimensionType> end() {
        return HolderSet.direct(GTRegistries.builtinRegistry().registryOrThrow(Registries.DIMENSION_TYPE).getHolderOrThrow(BuiltinDimensionTypes.END));
    }

    public static void init() {
        toReRegister.forEach(GTRegistries.BEDROCK_FLUID_DEFINITIONS::registerOrOverride);
    }
}
