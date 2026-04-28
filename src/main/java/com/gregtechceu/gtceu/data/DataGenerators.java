package com.gregtechceu.gtceu.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.SoundEntryBuilder;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.data.tags.BiomeTagsLoader;
import com.gregtechceu.gtceu.data.tags.DamageTypeTagsLoader;

import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Set;

@EventBusSubscriber(modid = GTCEu.MOD_ID)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        PackOutput packOutput = event.getGenerator().getPackOutput();
        event.addProvider(new SoundEntryBuilder.SoundEntryProvider(packOutput, GTCEu.MOD_ID));

        var set = Set.of(GTCEu.MOD_ID);
        event.createDatapackRegistryObjects(new RegistrySetBuilder()
                .add(Registries.DAMAGE_TYPE, GTDamageTypes::bootstrap)
                .add(Registries.CONFIGURED_FEATURE, GTConfiguredFeatures::bootstrap)
                .add(Registries.PLACED_FEATURE, GTPlacements::bootstrap)
                .add(Registries.DENSITY_FUNCTION, GTWorldgen::bootstrap)
                .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, GTBiomeModifiers::bootstrap)
                .add(Registries.JUKEBOX_SONG, GTJukeboxSongs::bootstrap)
                .add(Registries.ENCHANTMENT_PROVIDER, GTEnchantmentProviders::bootstrap)
                .add(GTRegistries.BEDROCK_FLUID_REGISTRY, GTBedrockFluids::bootstrap)
                .add(GTRegistries.ORE_VEIN_REGISTRY, GTOreVeins::bootstrap), set);
        event.addProvider(new BiomeTagsLoader(packOutput, event.getLookupProvider()));
        event.addProvider(new DamageTypeTagsLoader(packOutput, event.getLookupProvider()));
    }
}
