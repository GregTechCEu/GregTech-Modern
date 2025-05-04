package com.gregtechceu.gtceu.data.worldgen;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.data.material.GTMaterials;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.common.Tags;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

import java.util.Set;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class GTBedrockFluids {

    public static final Set<ResourceKey<BedrockFluidDefinition>> ALL_KEYS = new ReferenceOpenHashSet<>();

    //////////////////////////////////////
    // ******** OVERWORLD ********//
    //////////////////////////////////////

    public static final ResourceKey<BedrockFluidDefinition> HEAVY_OIL = create(GTCEu.id("heavy_oil_deposit"));
    public static final ResourceKey<BedrockFluidDefinition> LIGHT_OIL = create(GTCEu.id("light_oil_deposit"));
    public static final ResourceKey<BedrockFluidDefinition> NATURAL_GAS = create(GTCEu.id("natural_gas_deposit"));
    public static final ResourceKey<BedrockFluidDefinition> OIL = create(GTCEu.id("oil_deposit"));
    public static final ResourceKey<BedrockFluidDefinition> RAW_OIL = create(GTCEu.id("raw_oil_deposit"));
    public static final ResourceKey<BedrockFluidDefinition> SALT_WATER = create(GTCEu.id("salt_water_deposit"));

    //////////////////////////////////////
    // ******** NETHER ********//
    //////////////////////////////////////

    public static final ResourceKey<BedrockFluidDefinition> LAVA = create(GTCEu.id("lava_deposit"));
    public static final ResourceKey<BedrockFluidDefinition> NETHER_NATURAL_GAS = create(
            GTCEu.id("nether_natural_gas_deposit"));

    public static ResourceKey<BedrockFluidDefinition> create(ResourceLocation id) {
        var key = ResourceKey.create(GTRegistries.BEDROCK_FLUID_REGISTRY, id);
        ALL_KEYS.add(key);
        return key;
    }

    public static Set<ResourceKey<Level>> nether() {
        return Set.of(Level.NETHER);
    }

    public static Set<ResourceKey<Level>> overworld() {
        return Set.of(Level.OVERWORLD);
    }

    private static void register(BootstrapContext<BedrockFluidDefinition> context,
                                 ResourceKey<BedrockFluidDefinition> key,
                                 Consumer<BedrockFluidDefinition.Builder> consumer) {
        BedrockFluidDefinition.Builder builder = BedrockFluidDefinition.builder(context.lookup(Registries.BIOME));
        consumer.accept(builder);
        context.register(key, builder.build());
    }

    public static void bootstrap(BootstrapContext<BedrockFluidDefinition> context) {
        register(context, HEAVY_OIL, builder -> builder
                .fluid(GTMaterials.HeavyOil.getFluid())
                .weight(15)
                .yield(100, 200)
                .depletionAmount(1)
                .depletionChance(100)
                .depletedYield(20)
                .biomes(5, BiomeTags.IS_OCEAN)
                .biomes(10, Tags.Biomes.IS_SANDY)
                .dimensions(overworld())
                .build());
        register(context, LIGHT_OIL, builder -> builder
                .fluid(GTMaterials.LightOil.getFluid())
                .weight(25)
                .yield(175, 300)
                .depletionAmount(1)
                .depletionChance(100)
                .depletedYield(25)
                .dimensions(overworld()));
        register(context, NATURAL_GAS, builder -> builder
                .fluid(GTMaterials.NaturalGas.getFluid())
                .weight(15)
                .yield(100, 175)
                .depletionAmount(1)
                .depletionChance(100)
                .depletedYield(20)
                .dimensions(overworld()));
        register(context, OIL, builder -> builder
                .fluid(GTMaterials.Oil.getFluid())
                .weight(20)
                .yield(175, 300)
                .depletionAmount(1)
                .depletionChance(100)
                .depletedYield(25)
                .biomes(5, BiomeTags.IS_OCEAN)
                .biomes(5, Tags.Biomes.IS_SANDY)
                .dimensions(overworld()));
        register(context, RAW_OIL, builder -> builder
                .fluid(GTMaterials.RawOil.getFluid())
                .weight(20)
                .yield(200, 300)
                .depletionAmount(1)
                .depletionChance(100)
                .depletedYield(25)
                .dimensions(overworld()));
        register(context, SALT_WATER, builder -> builder
                .fluid(GTMaterials.SaltWater.getFluid())
                .weight(0)
                .yield(50, 100)
                .depletionAmount(1)
                .depletionChance(100)
                .depletedYield(15)
                .dimensions(overworld())
                .biomes(200, Biomes.DEEP_OCEAN, Biomes.DEEP_COLD_OCEAN, Biomes.DEEP_FROZEN_OCEAN)
                .biomes(150, BiomeTags.IS_OCEAN));

        register(context, LAVA, builder -> builder
                .fluid(GTMaterials.Lava.getFluid())
                .weight(65)
                .yield(125, 250)
                .depletionAmount(1)
                .depletionChance(100)
                .depletedYield(30)
                .dimensions(nether()));
        register(context, NETHER_NATURAL_GAS, builder -> builder
                .fluid(GTMaterials.NaturalGas.getFluid())
                .weight(35)
                .yield(150, 300)
                .depletionAmount(1)
                .depletionChance(100)
                .depletedYield(40)
                .dimensions(nether()));
    }
}
