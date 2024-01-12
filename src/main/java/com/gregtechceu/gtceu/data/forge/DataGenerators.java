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
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;

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
                .add(Registries.DENSITY_FUNCTION, GTWorldgen::bootstrapDensityFunctions)
                .add(ForgeRegistries.Keys.BIOME_MODIFIERS, GTBiomeModifiers::bootstrap),
                set, "Worldgen Data"));
        }
    }
}