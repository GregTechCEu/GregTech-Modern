package com.gregtechceu.gtceu.common;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialRegistryEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.event.PostMaterialEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGenLayers;
import com.gregtechceu.gtceu.api.data.worldgen.generator.IndicatorGenerators;
import com.gregtechceu.gtceu.api.data.worldgen.generator.VeinGenerators;
import com.gregtechceu.gtceu.api.gui.factory.CoverUIFactory;
import com.gregtechceu.gtceu.api.gui.factory.GTUIEditorFactory;
import com.gregtechceu.gtceu.api.gui.factory.MachineUIFactory;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.ingredient.*;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.*;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.fluid.*;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item.*;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.common.data.GTPlaceholders;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.common.data.materials.GTFoods;
import com.gregtechceu.gtceu.common.item.tool.rotation.CustomBlockRotations;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.FusionReactorMachine;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.common.registry.GTRegistration;
import com.gregtechceu.gtceu.common.unification.material.MaterialRegistryManager;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.mixins.registrate.AbstractRegistrateAccessor;
import com.gregtechceu.gtceu.data.GregTechDatagen;
import com.gregtechceu.gtceu.data.lang.MaterialLangGenerator;
import com.gregtechceu.gtceu.data.loot.ChestGenHooks;
import com.gregtechceu.gtceu.data.loot.DungeonLootLoader;
import com.gregtechceu.gtceu.data.pack.GTDynamicDataPack;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;
import com.gregtechceu.gtceu.data.pack.GTPackSource;
import com.gregtechceu.gtceu.data.recipe.GTCraftingComponents;
import com.gregtechceu.gtceu.forge.AlloyBlastPropertyAddition;
import com.gregtechceu.gtceu.integration.ae2.GTAEPlaceholders;
import com.gregtechceu.gtceu.integration.cctweaked.CCTweakedPlugin;
import com.gregtechceu.gtceu.integration.create.GTCreateIntegration;
import com.gregtechceu.gtceu.integration.kjs.GTCEuStartupEvents;
import com.gregtechceu.gtceu.integration.kjs.GTRegistryInfo;
import com.gregtechceu.gtceu.integration.kjs.events.MaterialModificationEventJS;
import com.gregtechceu.gtceu.integration.map.WaypointManager;
import com.gregtechceu.gtceu.integration.top.forge.TheOneProbePluginImpl;
import com.gregtechceu.gtceu.utils.input.KeyBind;
import com.gregtechceu.gtceu.utils.input.SyncedKeyMappings;

import com.lowdragmc.lowdraglib.gui.factory.UIFactory;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IntersectionIngredient;
import net.minecraftforge.common.crafting.PartialNBTIngredient;
import net.minecraftforge.common.crafting.StrictNBTIngredient;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;
import net.minecraftforge.registries.RegisterEvent;

import com.google.common.collect.Multimaps;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import com.tterrag.registrate.providers.RegistrateProvider;
import com.tterrag.registrate.util.nullness.NonNullConsumer;

import java.util.List;

public class CommonProxy {

    public CommonProxy() {
        // used for forge events (ClientProxy + CommonProxy)
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.register(this);
        eventBus.addListener(AlloyBlastPropertyAddition::addAlloyBlastProperties);
        // must be set here because of KubeJS compat
        // trying to read this before the pre-init stage
        GTCEuAPI.materialManager = MaterialRegistryManager.getInstance();
        ConfigHolder.init();
        GTCEuAPI.initializeHighTier();
        if (GTCEu.isDev()) {
            ConfigHolder.INSTANCE.recipes.generateLowQualityGems = true;
            ConfigHolder.INSTANCE.compat.energy.enableFEConverters = true;
        }

        GTValueProviderTypes.init(eventBus);
        GTRegistries.init(eventBus);
        GTFeatures.init(eventBus);
        GTCommandArguments.init(eventBus);
        GTMobEffects.init(eventBus);
        GTParticleTypes.init(eventBus);
    }

    public static void init() {
        GTCEu.LOGGER.info("GTCEu common proxy init!");
        GTNetwork.init();
        UIFactory.register(MachineUIFactory.INSTANCE);
        UIFactory.register(CoverUIFactory.INSTANCE);
        UIFactory.register(GTUIEditorFactory.INSTANCE);

        // Initialize the model generator before any content is loaded so machine models can use the generated data
        GregTechDatagen.initPre();

        GTRecipeCapabilities.init();
        GTRecipeConditions.init();
        GTToolTiers.init();
        GTElements.init();
        MaterialIconSet.init();
        MaterialIconType.init();
        initMaterials();
        TagPrefix.init();
        GTSoundEntries.init();
        GTDamageTypes.init();
        GTPlaceholders.initPlaceholders();
        if (ConfigHolder.INSTANCE.compat.createCompat && GTCEu.Mods.isCreateLoaded()) {
            GTCreateIntegration.init();
        }
        if (GTCEu.Mods.isAE2Loaded()) {
            GTAEPlaceholders.init();
        }

        GTCovers.init();
        GTCreativeModeTabs.init();
        GTBlocks.init();
        GTFluids.init();
        GTEntityTypes.init();
        GTBlockEntities.init();
        GTRecipeTypes.init();
        GTRecipeCategories.init();
        GTMachineUtils.init();
        GTMachines.init();

        GTFoods.init();
        GTItems.init();
        GTDimensionMarkers.init();
        ChanceLogic.init();
        WaypointManager.init();
        AddonFinder.getAddons().forEach(IGTAddon::initializeAddon);

        GTRegistration.REGISTRATE.registerRegistrate();

        GregTechDatagen.initPost();
        // Register all material manager registries, for materials with mod ids.
        GTCEuAPI.materialManager.getRegistries().forEach(registry -> {
            // Force the material lang generator to be at index 0, so that addons' lang generators can override it.
            AbstractRegistrateAccessor accessor = (AbstractRegistrateAccessor) registry.getRegistrate();
            if (accessor.getDoDatagen().get()) {
                // noinspection UnstableApiUsage
                List<NonNullConsumer<? extends RegistrateProvider>> providers = Multimaps.asMap(accessor.getDatagens())
                        .get(ProviderType.LANG);
                NonNullConsumer<? extends RegistrateProvider> generator = (provider) -> MaterialLangGenerator
                        .generate((RegistrateLangProvider) provider, registry);
                if (providers == null) {
                    accessor.getDatagens().put(ProviderType.LANG, generator);
                } else {
                    providers.add(0, generator);
                }
            }

            registry.getRegistrate()
                    .registerEventListeners(ModList.get().getModContainerById(registry.getModid())
                            .filter(FMLModContainer.class::isInstance)
                            .map(FMLModContainer.class::cast)
                            .map(FMLModContainer::getEventBus)
                            .orElse(FMLJavaModLoadingContext.get().getModEventBus()));
        });

        WorldGenLayers.registerAll();
        VeinGenerators.registerAddonGenerators();
        IndicatorGenerators.registerAddonGenerators();

        GTFeatures.init();
        GTFeatures.register();
        CustomBlockRotations.init();
        KeyBind.init();
        SyncedKeyMappings.init();
        MachineOwner.init();

        FusionReactorMachine.registerFusionTier(GTValues.LuV, " (MKI)");
        FusionReactorMachine.registerFusionTier(GTValues.ZPM, " (MKII)");
        FusionReactorMachine.registerFusionTier(GTValues.UV, " (MKIII)");
    }

    private static void initMaterials() {
        // First, register other mods' Registries
        MaterialRegistryManager managerInternal = (MaterialRegistryManager) GTCEuAPI.materialManager;

        GTCEu.LOGGER.info("Registering material registries");
        ModLoader.get().postEvent(new MaterialRegistryEvent());

        // First, register CEu Materials
        managerInternal.unfreezeRegistries();
        GTCEu.LOGGER.info("Registering GTCEu Materials");
        GTMaterials.init();
        MaterialRegistryManager.getInstance()
                .getRegistry(GTCEu.MOD_ID)
                .setFallbackMaterial(GTMaterials.Aluminium);

        // Then, register addon Materials
        GTCEu.LOGGER.info("Registering addon Materials");
        MaterialEvent materialEvent = new MaterialEvent();
        ModLoader.get().postEvent(materialEvent);
        if (GTCEu.Mods.isKubeJSLoaded()) {
            KJSEventWrapper.materialRegistry();
        }

        // Fire Post-Material event, intended for when Materials need to be iterated over in-full before freezing
        // Block entirely new Materials from being added in the Post event
        managerInternal.closeRegistries();
        ModLoader.get().postEvent(new PostMaterialEvent());
        if (GTCEu.Mods.isKubeJSLoaded()) {
            KJSEventWrapper.materialModification();
        }

        // Freeze Material Registry before processing Items, Blocks, and Fluids
        managerInternal.freezeRegistries();
        /* End Material Registration */
    }

    @SubscribeEvent
    public void register(RegisterEvent event) {
        if (event.getRegistryKey().equals(BuiltInRegistries.LOOT_FUNCTION_TYPE.key()))
            ChestGenHooks.RandomWeightLootFunction.init();
    }

    @SubscribeEvent
    public void modConstruct(FMLConstructModEvent event) {
        // this is done to delay initialization of content to be after KJS has set up.
        event.enqueueWork(CommonProxy::init);
    }

    @SubscribeEvent
    public void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            CraftingHelper.register(SizedIngredient.TYPE, SizedIngredient.SERIALIZER);
            CraftingHelper.register(IntCircuitIngredient.TYPE, IntCircuitIngredient.SERIALIZER);
            CraftingHelper.register(IntProviderIngredient.TYPE, IntProviderIngredient.SERIALIZER);
            CraftingHelper.register(NBTPredicateIngredient.TYPE, NBTPredicateIngredient.Serializer.INSTANCE);
            CraftingHelper.register(FluidContainerIngredient.TYPE, FluidContainerIngredient.SERIALIZER);

            // register the map ingredient converters for all of our ingredients
            MapIngredientTypeManager.registerMapIngredient(FluidIngredient.class, FluidTagMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(FluidIngredient.class, FluidStackMapIngredient::from);

            MapIngredientTypeManager.registerMapIngredient(FluidStack.class, FluidTagMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(FluidStack.class, FluidStackMapIngredient::from);

            // spotless:off
            MapIngredientTypeManager.registerMapIngredient(SizedIngredient.class,
                    (ingredient) -> MapIngredientTypeManager.getFrom(ingredient.getInner(), ItemRecipeCapability.CAP));
            MapIngredientTypeManager.registerMapIngredient(IntProviderIngredient.class,
                    (ingredient) -> MapIngredientTypeManager.getFrom(ingredient.getInner(), ItemRecipeCapability.CAP));

            MapIngredientTypeManager.registerMapIngredient(StrictNBTIngredient.class, StrictNBTItemStackMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(PartialNBTIngredient.class, PartialNBTItemStackMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(NBTPredicateIngredient.class, NBTPredicateItemStackMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(IntersectionIngredient.class, IntersectionMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(Ingredient.class, ItemTagMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(Ingredient.class, ItemStackMapIngredient::from);

            MapIngredientTypeManager.registerMapIngredient(ItemStack.class, ItemStackMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(ItemStack.class, ItemTagMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(ItemStack.class, StrictNBTItemStackMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(ItemStack.class, PartialNBTItemStackMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(ItemStack.class, NBTPredicateItemStackMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(ItemStack.class, IntersectionMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(ItemStack.class, CustomMapIngredient::from);
            // spotless:on

            if (GTCEu.Mods.isCCTweakedLoaded()) {
                GTCEu.LOGGER.info("CC: Tweaked found. Enabling integration...");
                CCTweakedPlugin.init();
            }
        });
    }

    @SubscribeEvent
    public void loadComplete(FMLLoadCompleteEvent e) {
        e.enqueueWork(() -> {
            if (GTCEu.isModLoaded(GTValues.MODID_TOP)) {
                GTCEu.LOGGER.info("TheOneProbe found. Enabling integration...");
                TheOneProbePluginImpl.init();
            }
        });
    }

    @SubscribeEvent
    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        GTCapability.register(event);
    }

    @SubscribeEvent
    public void registerPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            // Clear old data
            GTDynamicResourcePack.clearClient();

            event.addRepositorySource(new GTPackSource("gtceu:dynamic_assets",
                    event.getPackType(),
                    Pack.Position.BOTTOM,
                    GTDynamicResourcePack::new));
        } else if (event.getPackType() == PackType.SERVER_DATA) {
            // Clear old data
            GTDynamicDataPack.clearServer();

            long startTime = System.currentTimeMillis();
            GTCraftingComponents.init();
            GTRecipes.recipeRemoval();
            GTRecipes.recipeAddition(GTDynamicDataPack::addRecipe);
            // Initialize dungeon loot additions
            DungeonLootLoader.init();
            GTCEu.LOGGER.info("GregTech Data loading took {}ms", System.currentTimeMillis() - startTime);

            event.addRepositorySource(new GTPackSource("gtceu:dynamic_data",
                    event.getPackType(),
                    Pack.Position.BOTTOM,
                    GTDynamicDataPack::new));
        }
    }

    public static final class KJSEventWrapper {

        public static void materialRegistry() {
            GTRegistryInfo.registerFor(GTCEuAPI.materialManager.getRegistry(GTCEu.MOD_ID).getRegistryName());
        }

        public static void materialModification() {
            GTCEuStartupEvents.MATERIAL_MODIFICATION.post(new MaterialModificationEventJS());
        }
    }
}
