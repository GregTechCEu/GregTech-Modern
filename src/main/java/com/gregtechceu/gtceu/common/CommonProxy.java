package com.gregtechceu.gtceu.common;

import com.google.common.collect.Multimaps;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialRegistryEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.event.PostMaterialEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGenLayers;
import com.gregtechceu.gtceu.api.gui.factory.CoverUIFactory;
import com.gregtechceu.gtceu.api.gui.factory.GTUIEditorFactory;
import com.gregtechceu.gtceu.api.gui.factory.MachineUIFactory;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.common.data.materials.GTFoods;
import com.gregtechceu.gtceu.common.item.tool.forge.ToolLootModifier;
import com.gregtechceu.gtceu.common.item.tool.rotation.CustomBlockRotations;
import com.gregtechceu.gtceu.common.registry.GTRegistration;
import com.gregtechceu.gtceu.common.unification.material.MaterialRegistryManager;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.mixins.AbstractRegistrateAccessor;
import com.gregtechceu.gtceu.data.GregTechDatagen;
import com.gregtechceu.gtceu.data.lang.MaterialLangGenerator;
import com.gregtechceu.gtceu.data.loot.ChestGenHooks;
import com.gregtechceu.gtceu.forge.AlloyBlastPropertyAddition;
import com.gregtechceu.gtceu.integration.kjs.GTCEuStartupEvents;
import com.gregtechceu.gtceu.integration.kjs.GTRegistryInfo;
import com.gregtechceu.gtceu.integration.kjs.events.MaterialModificationEventJS;
import com.gregtechceu.gtceu.integration.top.forge.TheOneProbePluginImpl;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.gui.factory.UIFactory;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import com.tterrag.registrate.providers.RegistrateProvider;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;
import net.minecraftforge.registries.ForgeDeferredRegistriesSetup;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

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

        GTRegistries.init(eventBus);
        GTFeatures.init(eventBus);
        // init common features
        GTRegistries.GLOBAL_LOOT_MODIFIES.register("tool", () -> ToolLootModifier.CODEC);
    }

    public static void init() {
        GTCEu.LOGGER.info("GTCEu common proxy init!");
        GTRegistries.COMPASS_NODES.unfreeze();

        UIFactory.register(MachineUIFactory.INSTANCE);
        UIFactory.register(CoverUIFactory.INSTANCE);
        UIFactory.register(GTUIEditorFactory.INSTANCE);
        GTPlacerTypes.init();
        GTRecipeCapabilities.init();
        GTRecipeConditions.init();
        GTElements.init();
        MaterialIconSet.init();
        MaterialIconType.init();
        initMaterials();
        TagPrefix.init();
        GTSoundEntries.init();
        GTDamageTypes.init();
        GTCompassSections.init();
        GTCompassNodes.init();
        GTCovers.init();
        GTFluids.init();
        GTCreativeModeTabs.init();
        GTBlocks.init();
        GTBlockEntities.init();
        GTRecipeTypes.init();
        GTMachines.init();
        GTFoods.init();
        GTItems.init();
        AddonFinder.getAddons().forEach(IGTAddon::initializeAddon);

        // fabric exclusive, squeeze this in here to register before stuff is used
        GTRegistration.REGISTRATE.registerRegistrate();

        GregTechDatagen.init();
        // Register all material manager registries, for materials with mod ids.
        GTCEuAPI.materialManager.getRegistries().forEach(registry -> {
            // Force the material lang generator to be at index 0, so that addons' lang generators can override it.
            AbstractRegistrateAccessor accessor = (AbstractRegistrateAccessor)registry.getRegistrate();
            if (accessor.getDoDatagen().get()) {
                //noinspection UnstableApiUsage
                List<NonNullConsumer<? extends RegistrateProvider>> providers = Multimaps.asMap(accessor.getDatagens()).get(ProviderType.LANG);
                if (providers.isEmpty()) {
                    providers.add((provider) -> MaterialLangGenerator.generate((RegistrateLangProvider) provider, registry));
                } else {
                    providers.add(0, (provider) -> MaterialLangGenerator.generate((RegistrateLangProvider) provider, registry));
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
        GTFeatures.init();
        GTFeatures.register();
        CustomBlockRotations.init();
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
        if (GTCEu.isKubeJSLoaded()) {
            KJSEventWrapper.materialRegistry();
        }

        // Fire Post-Material event, intended for when Materials need to be iterated over in-full before freezing
        // Block entirely new Materials from being added in the Post event
        managerInternal.closeRegistries();
        ModLoader.get().postEvent(new PostMaterialEvent());
        if (GTCEu.isKubeJSLoaded()) {
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

        });
        CraftingHelper.register(SizedIngredient.TYPE, SizedIngredient.SERIALIZER);
    }

    @SubscribeEvent
    public void loadComplete(FMLLoadCompleteEvent e) {
        e.enqueueWork(() -> {
            if (LDLib.isModLoaded(GTValues.MODID_TOP)) {
                GTCEu.LOGGER.info("TheOneProbe found. Enabling integration...");
                TheOneProbePluginImpl.init();
            }
        });
    }

    @SubscribeEvent
    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        GTCapability.register(event);
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
