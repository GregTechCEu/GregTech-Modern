package com.gregtechceu.gtceu.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.builder.SoundEntryBuilder;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.common.data.worldgen.GTBiomeModifiers;
import com.gregtechceu.gtceu.common.data.worldgen.GTConfiguredFeatures;
import com.gregtechceu.gtceu.common.data.worldgen.GTDensityFunctions;
import com.gregtechceu.gtceu.common.data.worldgen.GTPlacedFeatures;
import com.gregtechceu.gtceu.common.registry.GTRegistration;
import com.gregtechceu.gtceu.core.mixins.registrate.RegistrateDataProviderAccessor;
import com.gregtechceu.gtceu.data.datamap.DataMapsHandler;
import com.gregtechceu.gtceu.data.lang.LangHandler;
import com.gregtechceu.gtceu.data.loot.GTLootModifications;
import com.gregtechceu.gtceu.data.loot.GTLootTables;
import com.gregtechceu.gtceu.data.model.BlockstateModelLoader;
import com.gregtechceu.gtceu.data.tags.*;

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

import com.tterrag.registrate.providers.ProviderType;

import java.util.Set;

@EventBusSubscriber(modid = GTCEu.MOD_ID)
public class GTDatagen {

    // we only register this so the class gets loaded. the key gets overwritten in #initPre.
    private static final ProviderType<GTBlockstateProvider> BLOCKSTATE_PROVIDER = ProviderType.registerProvider(
            "ex_blockstate",
            GTBlockstateProvider::new);

    public static void initPre() {
        // replace some default providers with ours
        RegistrateDataProviderAccessor.gtceu$getTypes().forcePut("blockstate", BLOCKSTATE_PROVIDER);

        GTRegistration.REGISTRATE.addDataGenerator(ProviderType.BLOCKSTATE,
                p -> BlockstateModelLoader.init((GTBlockstateProvider) p));
    }

    public static void initPost() {
        GTRegistration.REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, BlockTagLoader::init);
        GTRegistration.REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, ItemTagLoader::init);
        GTRegistration.REGISTRATE.addDataGenerator(ProviderType.FLUID_TAGS, FluidTagLoader::init);
        GTRegistration.REGISTRATE.addDataGenerator(ProviderType.ENTITY_TAGS, EntityTypeTagLoader::init);
        GTRegistration.REGISTRATE.addDataGenerator(GTProviderTypes.TAG_PREFIX_TAGS, TagPrefixTagLoader::init);

        GTRegistration.REGISTRATE.addDataGenerator(ProviderType.LANG, LangHandler::init);
        GTRegistration.REGISTRATE.addDataGenerator(ProviderType.DATA_MAP, DataMapsHandler::init);
    }

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
                            .add(Registries.PLACED_FEATURE, GTPlacedFeatures::bootstrap)
                            .add(Registries.DENSITY_FUNCTION, GTDensityFunctions::bootstrap)
                            .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, GTBiomeModifiers::bootstrap)
                            .add(Registries.JUKEBOX_SONG, GTJukeboxSongs::bootstrap)
                            .add(Registries.ENCHANTMENT_PROVIDER, GTEnchantmentProviders::bootstrap)
                            .add(GTRegistries.Keys.BEDROCK_FLUID, GTBedrockFluids::bootstrap)
                            .add(GTRegistries.Keys.ORE_VEIN, GTOreVeins::bootstrap),
                    set));
            generator.addProvider(true,
                    new DamageTypeTagsLoader(packOutput, provider.getRegistryProvider(), existingFileHelper));
            generator.addProvider(true, new GTLootTables(packOutput, provider.getRegistryProvider()));
            generator.addProvider(true, new GTLootModifications(packOutput, provider.getRegistryProvider()));
        }
    }
}
