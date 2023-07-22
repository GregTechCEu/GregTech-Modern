package com.gregtechceu.gtceu.common.data.forge;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.data.GTPlacements;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.holdersets.AnyHolderSet;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class GTBiomeModifiers {
    public static final ResourceKey<BiomeModifier> ORE = ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, GTCEu.id("ore"));
    public static final ResourceKey<BiomeModifier> RUBBER = ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, GTCEu.id("rubber_tree"));


    public static void bootstrap(BootstapContext<BiomeModifier> ctx, CompletableFuture<HolderLookup.Provider> provider) {
        HolderGetter<Biome> biomeLookup = ctx.lookup(Registries.BIOME);
        HolderGetter<PlacedFeature> placedFeatureRegistry = ctx.lookup(Registries.PLACED_FEATURE);
        HolderSet<Biome> biomes; // ctx.registryLookup(Registries.BIOME).get()
        try {
            biomes = new AnyHolderSet<>(provider.get().lookupOrThrow(Registries.BIOME));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        Holder<PlacedFeature> featureHolder = placedFeatureRegistry.getOrThrow(GTPlacements.ORE);
        ctx.register(ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                biomes,
                HolderSet.direct(featureHolder),
                GenerationStep.Decoration.UNDERGROUND_ORES
        ));

        biomes = biomeLookup.getOrThrow(CustomTags.HAS_RUBBER_TREE);
        featureHolder = placedFeatureRegistry.getOrThrow(GTPlacements.RUBBER_CHECKED);
        ctx.register(RUBBER, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                biomes,
                HolderSet.direct(featureHolder),
                GenerationStep.Decoration.VEGETAL_DECORATION
        ));

    }
}
