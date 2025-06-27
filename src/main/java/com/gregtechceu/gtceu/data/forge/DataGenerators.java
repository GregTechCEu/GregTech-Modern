package com.gregtechceu.gtceu.data.forge;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.registrate.SoundEntryBuilder;
import com.gregtechceu.gtceu.common.data.GTBiomeModifiers;
import com.gregtechceu.gtceu.common.data.GTConfiguredFeatures;
import com.gregtechceu.gtceu.common.data.GTDamageTypes;
import com.gregtechceu.gtceu.common.data.GTPlacements;
import com.gregtechceu.gtceu.common.data.GTWorldgen;
import com.gregtechceu.gtceu.data.tags.BiomeTagsLoader;
import com.gregtechceu.gtceu.data.tags.DamageTagsLoader;

import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
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
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        var registries = event.getLookupProvider();
        if (event.includeClient()) {
            generator.addProvider(true, new SoundEntryBuilder.SoundEntryProvider(packOutput, GTCEu.MOD_ID));
        }
        if (event.includeServer()) {
            var set = Set.of(GTCEu.MOD_ID);
            generator.addProvider(true, new BiomeTagsLoader(packOutput, registries, existingFileHelper));
            DatapackBuiltinEntriesProvider provider = generator.addProvider(true, new DatapackBuiltinEntriesProvider(
                    packOutput, registries, new RegistrySetBuilder()
                            .add(Registries.DAMAGE_TYPE, GTDamageTypes::bootstrap)
                            .add(Registries.CONFIGURED_FEATURE, GTConfiguredFeatures::bootstrap)
                            .add(Registries.PLACED_FEATURE, GTPlacements::bootstrap)
                            .add(Registries.DENSITY_FUNCTION, GTWorldgen::bootstrapDensityFunctions)
                            .add(ForgeRegistries.Keys.BIOME_MODIFIERS, GTBiomeModifiers::bootstrap),
                    set));
            generator.addProvider(true,
                    new DamageTagsLoader(packOutput, provider.getRegistryProvider(), existingFileHelper));
        }
    }
}
