package com.gregtechceu.gtceu.data.forge;

import appeng.core.definitions.AEDamageTypes;
import appeng.init.worldgen.InitBiomes;
import appeng.init.worldgen.InitDimensionTypes;
import appeng.init.worldgen.InitStructures;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.registrate.CompassNode;
import com.gregtechceu.gtceu.api.registry.registrate.CompassSection;
import com.gregtechceu.gtceu.api.registry.registrate.SoundEntryBuilder;
import com.gregtechceu.gtceu.common.data.GTConfiguredFeatures;
import com.gregtechceu.gtceu.common.data.GTDamageTypes;
import com.gregtechceu.gtceu.common.data.GTPlacements;
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
import net.minecraft.server.packs.PackType;
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
        var registryAccess = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        var registries = createProvider(registryAccess);
        if (event.includeServer()) {
            var set = Set.of(GTCEu.MOD_ID);
            generator.addProvider(true, new SoundEntryBuilder.SoundEntryProvider(generator.getPackOutput()));
            generator.addProvider(true, new CompassSection.CompassSectionProvider(generator.getPackOutput(), rl -> event.getExistingFileHelper().exists(rl, PackType.CLIENT_RESOURCES)));
            generator.addProvider(true, new CompassNode.CompassNodeProvider(generator.getPackOutput(), rl -> event.getExistingFileHelper().exists(rl, PackType.CLIENT_RESOURCES)));
            generator.addProvider(true, bindRegistries(BiomeTagsProviderImpl::new, registries));
            generator.addProvider(true, bindRegistries((output, provider) -> new GTRegistriesDatapackGenerator(
                    output, registries, new RegistrySetBuilder()
                    .add(Registries.DAMAGE_TYPE, GTDamageTypes::bootstrap), set, "DamageType Data"), registries));
            generator.addProvider(true, bindRegistries((output, provider) -> new GTRegistriesDatapackGenerator(
                    output, registries, new RegistrySetBuilder()
                    .add(Registries.CONFIGURED_FEATURE, GTConfiguredFeatures::bootstrap)
                    .add(Registries.PLACED_FEATURE, GTPlacements::bootstrap)
                    .add(ForgeRegistries.Keys.BIOME_MODIFIERS, ctx -> GTBiomeModifiers.bootstrap(ctx, provider)),
                    set, "Worldgen Data"), registries));
        }
    }

    private static <T extends DataProvider> DataProvider.Factory<T> bindRegistries(
            BiFunction<PackOutput, CompletableFuture<HolderLookup.Provider>, T> factory,
            CompletableFuture<HolderLookup.Provider> factories) {
        return packOutput -> factory.apply(packOutput, factories);
    }

    /**
     * See {@link VanillaRegistries#createLookup()}
     */
    private static CompletableFuture<HolderLookup.Provider> createProvider(RegistryAccess registryAccess) {

        var vanillaLookup = CompletableFuture.supplyAsync(VanillaRegistries::createLookup, Util.backgroundExecutor());

        return vanillaLookup.thenApply(provider -> {
            var builder = new RegistrySetBuilder()
                    .add(Registries.DIMENSION_TYPE, InitDimensionTypes::init)
                    .add(Registries.STRUCTURE, InitStructures::initDatagenStructures)
                    .add(Registries.STRUCTURE_SET, InitStructures::initDatagenStructureSets)
                    .add(Registries.BIOME, InitBiomes::init)
                    .add(Registries.DAMAGE_TYPE, AEDamageTypes::init);

            return builder.buildPatch(registryAccess, provider);
        });
    }
}