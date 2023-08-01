package com.gregtechceu.gtceu.data.fabric;

import appeng.core.definitions.AEDamageTypes;
import appeng.init.worldgen.InitBiomes;
import appeng.init.worldgen.InitDimensionTypes;
import appeng.init.worldgen.InitStructures;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.CompassNode;
import com.gregtechceu.gtceu.api.registry.registrate.CompassSection;
import com.gregtechceu.gtceu.api.registry.registrate.SoundEntryBuilder;
import com.gregtechceu.gtceu.common.data.GTConfiguredFeatures;
import com.gregtechceu.gtceu.common.data.GTDamageTypes;
import com.gregtechceu.gtceu.common.data.GTPlacements;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.loader.api.FabricLoader;
import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataProvider;
import net.minecraft.data.registries.VanillaRegistries;

import java.util.concurrent.CompletableFuture;
import net.minecraft.server.packs.PackType;

/**
 * @author KilaBash
 * @date 2023/3/17
 * @implNote GregTechDatagen
 */
public class GregTechDatagen implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        // registrate
        var rootPath = FabricLoader.getInstance().getGameDir().normalize().getParent().getParent();
        ExistingFileHelper helper = ExistingFileHelper.withResources(
                rootPath.resolve("common").resolve("src").resolve("main").resolve("resources"),
                rootPath.resolve("fabric").resolve("src").resolve("main").resolve("resources"));
        var pack = generator.createPack();
        GTRegistries.REGISTRATE.setupDatagen(pack, helper);
        // sound
        pack.addProvider((FabricDataGenerator.Pack.Factory<DataProvider>) SoundEntryBuilder.SoundEntryProvider::new);
        // compass
        pack.addProvider((FabricDataGenerator.Pack.Factory<CompassSection.CompassSectionProvider>) packOutput -> new CompassSection.CompassSectionProvider(packOutput, rl -> helper.exists(rl, PackType.CLIENT_RESOURCES)));
        pack.addProvider((FabricDataGenerator.Pack.Factory<DataProvider>) packOutput -> new CompassNode.CompassNodeProvider(packOutput, rl -> helper.exists(rl, PackType.CLIENT_RESOURCES)));
        // biome tags
        var registryAccess = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        var registries = createProvider(registryAccess);
        pack.addProvider((FabricDataGenerator.Pack.Factory<DataProvider>) output -> new BiomeTagsProviderImpl(output, registries));
        pack.addProvider((FabricDataGenerator.Pack.Factory<DataProvider>) output -> new GTRegistriesDatapackGenerator(
                output, registries, new RegistrySetBuilder()
                .add(Registries.DAMAGE_TYPE, GTDamageTypes::bootstrap), "DamageType Data"));
        pack.addProvider((FabricDataGenerator.Pack.Factory<DataProvider>) output -> new GTRegistriesDatapackGenerator(
                output, registries, new RegistrySetBuilder()
                .add(Registries.CONFIGURED_FEATURE, GTConfiguredFeatures::bootstrap)
                .add(Registries.PLACED_FEATURE, GTPlacements::bootstrap), "Worldgen Data"));
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
