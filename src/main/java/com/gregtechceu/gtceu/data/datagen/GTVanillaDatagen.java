package com.gregtechceu.gtceu.data.datagen;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.registrate.CompassNode;
import com.gregtechceu.gtceu.api.registry.registrate.CompassSection;
import com.gregtechceu.gtceu.api.registry.registrate.SoundEntryBuilder;
import com.gregtechceu.gtceu.data.damagesource.GTDamageTypes;
import com.gregtechceu.gtceu.data.datamap.GTDataMaps;
import com.gregtechceu.gtceu.data.jukebox.GTJukeboxSongs;
import com.gregtechceu.gtceu.data.tag.BiomeTagsLoader;
import com.gregtechceu.gtceu.data.tag.DamageTagsLoader;
import com.gregtechceu.gtceu.data.worldgen.GTBiomeModifiers;
import com.gregtechceu.gtceu.data.worldgen.GTConfiguredFeatures;
import com.gregtechceu.gtceu.data.worldgen.GTDensityFunctions;
import com.gregtechceu.gtceu.data.worldgen.GTPlacedFeatures;

import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Set;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class GTVanillaDatagen {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        var registries = event.getLookupProvider();
        if (event.includeClient()) {
            generator.addProvider(true, new SoundEntryBuilder.SoundEntryProvider(packOutput, GTCEu.MOD_ID));
            generator.addProvider(true, new CompassSection.CompassSectionProvider(packOutput, existingFileHelper));
            generator.addProvider(true, new CompassNode.CompassNodeProvider(packOutput, existingFileHelper));
        }
        if (event.includeServer()) {
            var set = Set.of(GTCEu.MOD_ID);
            generator.addProvider(true, new BiomeTagsLoader(packOutput, registries, existingFileHelper));
            generator.addProvider(true, new GTDataMaps(packOutput, registries));
            DatapackBuiltinEntriesProvider provider = generator.addProvider(true, new DatapackBuiltinEntriesProvider(
                    packOutput, registries, new RegistrySetBuilder()
                            .add(Registries.DAMAGE_TYPE, GTDamageTypes::bootstrap)

                            .add(Registries.CONFIGURED_FEATURE, GTConfiguredFeatures::bootstrap)
                            .add(Registries.PLACED_FEATURE, GTPlacedFeatures::bootstrap)
                            .add(Registries.DENSITY_FUNCTION, GTDensityFunctions::bootstrap)
                            .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, GTBiomeModifiers::bootstrap)
                            .add(Registries.JUKEBOX_SONG, GTJukeboxSongs::bootstrap),
                    set));
            generator.addProvider(true,
                    new DamageTagsLoader(packOutput, provider.getRegistryProvider(), existingFileHelper));
        }
    }
}
