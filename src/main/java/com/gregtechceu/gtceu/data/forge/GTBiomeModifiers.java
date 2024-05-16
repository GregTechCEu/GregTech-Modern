package com.gregtechceu.gtceu.data.forge;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.worldgen.GTPlacements;

import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class GTBiomeModifiers {
    public static final ResourceKey<BiomeModifier> RUBBER = ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, GTCEu.id("rubber_tree"));
    public static final ResourceKey<BiomeModifier> STONE_BLOB = ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, GTCEu.id("stone_blob"));
    public static final ResourceKey<BiomeModifier> RAW_OIL_SPROUT = ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, GTCEu.id("raw_oil_sprout"));

    public static final ResourceKey<BiomeModifier> RUBBER = ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS,
            GTCEu.id("rubber_tree"));
    public static final ResourceKey<BiomeModifier> STONE_BLOB = ResourceKey
            .create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, GTCEu.id("stone_blob"));

    public static void bootstrap(BootstapContext<BiomeModifier> ctx) {
        HolderGetter<Biome> biomeLookup = ctx.lookup(Registries.BIOME);
        HolderGetter<PlacedFeature> placedFeatureRegistry = ctx.lookup(Registries.PLACED_FEATURE);

        HolderSet<Biome> biomes = biomeLookup.getOrThrow(CustomTags.HAS_RUBBER_TREE);
        Holder<PlacedFeature> rubberTree = placedFeatureRegistry.getOrThrow(GTPlacements.RUBBER_CHECKED);
        ctx.register(RUBBER, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes,
                HolderSet.direct(rubberTree),
                GenerationStep.Decoration.VEGETAL_DECORATION));

        HolderSet<Biome> overworld = biomeLookup.getOrThrow(BiomeTags.IS_OVERWORLD);
        Holder<PlacedFeature> redGraniteBlob = placedFeatureRegistry.getOrThrow(GTPlacements.RED_GRANITE_BLOB);
        Holder<PlacedFeature> marbleBlob = placedFeatureRegistry.getOrThrow(GTPlacements.MARBLE_BLOB);
        ctx.register(STONE_BLOB, new BiomeModifiers.AddFeaturesBiomeModifier(
            overworld,
            HolderSet.direct(redGraniteBlob, marbleBlob),
            GenerationStep.Decoration.UNDERGROUND_ORES
        ));

        Holder<PlacedFeature> rawOilSprout = placedFeatureRegistry.getOrThrow(GTPlacements.RAW_OIL_SPROUT);
        ctx.register(RAW_OIL_SPROUT, new BiomeModifiers.AddFeaturesBiomeModifier(
            overworld,
            HolderSet.direct(rawOilSprout),
            GenerationStep.Decoration.FLUID_SPRINGS
        ));
    }
}
