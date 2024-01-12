package com.gregtechceu.gtceu.data.forge;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.registrate.CompassNode;
import com.gregtechceu.gtceu.api.registry.registrate.CompassSection;
import com.gregtechceu.gtceu.api.registry.registrate.SoundEntryBuilder;
import com.gregtechceu.gtceu.common.data.GTConfiguredFeatures;
import com.gregtechceu.gtceu.common.data.GTDamageTypes;
import com.gregtechceu.gtceu.common.data.GTPlacements;
import com.gregtechceu.gtceu.common.data.GTWorldgen;
import com.gregtechceu.gtceu.common.data.forge.GTBiomeModifiers;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.data.worldgen.NoiseData;
import net.minecraft.data.worldgen.biome.BiomeData;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.levelgen.NoiseRouterData;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        var registries = event.getLookupProvider();
        if (event.includeServer()) {
            var set = Set.of(GTCEu.MOD_ID);
            generator.addProvider(true, new SoundEntryBuilder.SoundEntryProvider(generator.getPackOutput(), GTCEu.MOD_ID));
            generator.addProvider(true, new CompassSection.CompassSectionProvider(generator.getPackOutput(), rl -> event.getExistingFileHelper().exists(rl, PackType.CLIENT_RESOURCES)));
            generator.addProvider(true, new CompassNode.CompassNodeProvider(generator.getPackOutput(), rl -> event.getExistingFileHelper().exists(rl, PackType.CLIENT_RESOURCES)));
            generator.addProvider(true, new BiomeTagsProviderImpl(generator.getPackOutput(), registries, event.getExistingFileHelper()));
            generator.addProvider(true, new GTRegistriesDatapackGenerator(
                generator.getPackOutput(), registries, new RegistrySetBuilder()
                .add(Registries.DAMAGE_TYPE, GTDamageTypes::bootstrap), set, "DamageType Data"));
            generator.addProvider(true, new GTRegistriesDatapackGenerator(
                generator.getPackOutput(), registries, new RegistrySetBuilder()
                .add(Registries.CONFIGURED_FEATURE, GTConfiguredFeatures::bootstrap)
                .add(Registries.PLACED_FEATURE, GTPlacements::bootstrap)
                .add(Registries.NOISE, GTWorldgen::bootstrapNoises)
                .add(Registries.MATERIAL_RULE, GTWorldgen::bootstrapRules)
                .add(Registries.DENSITY_FUNCTION, GTWorldgen::bootstrapDensityFunctions)
                .add(ForgeRegistries.Keys.BIOME_MODIFIERS, GTBiomeModifiers::bootstrap),
                set, "Worldgen Data"));
        }
    }

    private static <T extends DataProvider> DataProvider.Factory<T> bindRegistries(
            BiFunction<PackOutput, CompletableFuture<HolderLookup.Provider>, T> factory,
            CompletableFuture<HolderLookup.Provider> factories) {
        return packOutput -> factory.apply(packOutput, factories);
    }
}