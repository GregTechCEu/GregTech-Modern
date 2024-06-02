package com.gregtechceu.gtceu.common;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.capability.forge.compat.GTEnergyWrapper;
import com.gregtechceu.gtceu.api.gui.factory.CoverUIFactory;
import com.gregtechceu.gtceu.api.gui.factory.GTUIEditorFactory;
import com.gregtechceu.gtceu.api.gui.factory.MachineUIFactory;
import com.gregtechceu.gtceu.api.item.DrumMachineItem;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.forge.GTBucketItem;
import com.gregtechceu.gtceu.api.material.ChemicalHelper;
import com.gregtechceu.gtceu.api.material.material.event.MaterialEvent;
import com.gregtechceu.gtceu.api.material.material.event.MaterialRegistryEvent;
import com.gregtechceu.gtceu.api.material.material.event.PostMaterialEvent;
import com.gregtechceu.gtceu.api.material.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.material.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.gregtechceu.gtceu.api.worldgen.WorldGenLayers;
import com.gregtechceu.gtceu.common.block.CableBlock;
import com.gregtechceu.gtceu.common.block.FluidPipeBlock;
import com.gregtechceu.gtceu.common.block.ItemPipeBlock;
import com.gregtechceu.gtceu.common.block.LaserPipeBlock;
import com.gregtechceu.gtceu.common.item.armor.GTArmorMaterials;
import com.gregtechceu.gtceu.common.item.tool.forge.ToolLootModifier;
import com.gregtechceu.gtceu.common.item.tool.rotation.CustomBlockRotations;
import com.gregtechceu.gtceu.common.material.MaterialRegistryManager;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.common.registry.GTRegistration;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.mixins.AbstractRegistrateAccessor;
import com.gregtechceu.gtceu.data.*;
import com.gregtechceu.gtceu.data.attachment.GTAttachmentTypes;
import com.gregtechceu.gtceu.data.block.GTBlocks;
import com.gregtechceu.gtceu.data.blockentity.GTBlockEntities;
import com.gregtechceu.gtceu.data.command.GTCommandArguments;
import com.gregtechceu.gtceu.data.compass.GTCompassNodes;
import com.gregtechceu.gtceu.data.compass.GTCompassSections;
import com.gregtechceu.gtceu.data.cover.GTCovers;
import com.gregtechceu.gtceu.data.damagesource.GTDamageTypes;
import com.gregtechceu.gtceu.data.effect.GTMobEffects;
import com.gregtechceu.gtceu.data.entity.GTEntityTypes;
import com.gregtechceu.gtceu.data.fluid.GTFluids;
import com.gregtechceu.gtceu.data.item.GTItems;
import com.gregtechceu.gtceu.data.lang.MaterialLangGenerator;
import com.gregtechceu.gtceu.data.loot.ChestGenHooks;
import com.gregtechceu.gtceu.data.loot.DungeonLootLoader;
import com.gregtechceu.gtceu.data.machine.GTMachines;
import com.gregtechceu.gtceu.data.material.GTElements;
import com.gregtechceu.gtceu.data.material.GTFoods;
import com.gregtechceu.gtceu.data.material.GTMaterials;
import com.gregtechceu.gtceu.data.pack.GTDynamicDataPack;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;
import com.gregtechceu.gtceu.data.pack.GTPackSource;
import com.gregtechceu.gtceu.data.recipe.GTRecipeCapabilities;
import com.gregtechceu.gtceu.data.recipe.GTRecipeConditions;
import com.gregtechceu.gtceu.data.recipe.GTRecipeTypes;
import com.gregtechceu.gtceu.data.sound.GTSoundEntries;
import com.gregtechceu.gtceu.data.tag.GTDataComponents;
import com.gregtechceu.gtceu.data.tag.GTIngredientTypes;
import com.gregtechceu.gtceu.data.tag.GregTechDatagen;
import com.gregtechceu.gtceu.data.tools.GTToolBehaviors;
import com.gregtechceu.gtceu.data.tools.GTToolTiers;
import com.gregtechceu.gtceu.data.worldgen.GTFeatures;
import com.gregtechceu.gtceu.data.worldgen.GTPlacerTypes;
import com.gregtechceu.gtceu.forge.AlloyBlastPropertyAddition;
import com.gregtechceu.gtceu.integration.kjs.GTCEuStartupEvents;
import com.gregtechceu.gtceu.integration.kjs.GTRegistryInfo;
import com.gregtechceu.gtceu.integration.kjs.events.MaterialModificationEventJS;
import com.gregtechceu.gtceu.integration.top.forge.TheOneProbePluginImpl;
import com.gregtechceu.gtceu.utils.input.KeyBind;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.gui.factory.UIFactory;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.neoforged.neoforge.registries.RegisterEvent;

import com.google.common.collect.Multimaps;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import com.tterrag.registrate.providers.RegistrateProvider;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

public class CommonProxy {

    // DO NOT USE OUTSIDE GTCEuM!!!
    @ApiStatus.Internal
    public static IEventBus modBus;

    public CommonProxy(IEventBus modBus) {
        CommonProxy.modBus = modBus;
        // used for forge events (ClientProxy + CommonProxy)
        modBus.register(this);
        modBus.addListener(AlloyBlastPropertyAddition::addAlloyBlastProperties);
        modBus.addListener(GTNetwork::registerPayloads);
        // must be set here because of KubeJS compat
        // trying to read this before the pre-init stage
        GTCEuAPI.materialManager = MaterialRegistryManager.getInstance();
        ConfigHolder.init();
        GTCEuAPI.initializeHighTier();

        GTRegistries.init(modBus);
        GTFeatures.init(modBus);
        GTCommandArguments.init(modBus);
        GTMobEffects.init(modBus);
        GTAttachmentTypes.init(modBus);
        // init common features
        if (GTCEu.isKubeJSLoaded()) {
            synchronized (LOCK) {
                if (!isKubeJSSetup) {
                    try { LOCK.wait(); } catch (InterruptedException ignored) {}
                }
            }
        }
        CommonProxy.init(modBus);
        GTRegistries.GLOBAL_LOOT_MODIFIES.register("tool", () -> ToolLootModifier.CODEC);
    }

    /**
     * If kjs is loaded, make sure our mod is loaded after it.
     */
    public static void onKubeJSSetup() {
        synchronized (LOCK) {
            isKubeJSSetup = true;
            LOCK.notify();
        }
    }

    public static void init(final IEventBus modBus) {
        GTCEu.LOGGER.info("GTCEu common proxy init!");
        GTRegistries.COMPASS_NODES.unfreeze();

        UIFactory.register(MachineUIFactory.INSTANCE);
        UIFactory.register(CoverUIFactory.INSTANCE);
        UIFactory.register(GTUIEditorFactory.INSTANCE);
        GTPlacerTypes.init();
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
        GTCompassSections.init();
        GTCompassNodes.init();
        GTCovers.init();
        GTFluids.init();
        GTCreativeModeTabs.init();
        GTBlocks.init();
        GTEntityTypes.init();
        GTBlockEntities.init();
        GTRecipeTypes.init();
        GTMachines.init();
        GTFoods.init();
        GTToolBehaviors.init();
        GTDataComponents.DATA_COMPONENTS.register(modBus);
        GTArmorMaterials.ARMOR_MATERIALS.register(modBus);
        GTItems.init();
        AddonFinder.getAddons().forEach(IGTAddon::initializeAddon);
        GTIngredientTypes.INGREDIENT_TYPES.register(modBus);

        // fabric exclusive, squeeze this in here to register before stuff is used
        GTRegistration.REGISTRATE.registerRegistrate(modBus);

        GregTechDatagen.init();
        // Register all material manager registries, for materials with mod ids.
        GTCEuAPI.materialManager.getRegistries().forEach(registry -> {
            // Force the material lang generator to be at index 0, so that addons' lang generators can override it.
            AbstractRegistrateAccessor accessor = (AbstractRegistrateAccessor) registry.getRegistrate();
            if (accessor.getDoDatagen().get()) {
                List<NonNullConsumer<? extends RegistrateProvider>> providers = Multimaps.asMap(accessor.getDatagens())
                        .get(ProviderType.LANG);
                providers.addFirst(
                        (provider) -> MaterialLangGenerator.generate((RegistrateLangProvider) provider, registry));
            }

            registry.getRegistrate()
                    .registerEventListeners(ModList.get().getModContainerById(registry.getModid())
                            .map(ModContainer::getEventBus)
                            .orElse(modBus));
        });

        WorldGenLayers.registerAll();
        GTFeatures.init();
        CustomBlockRotations.init();
        KeyBind.init();
    }

    private static void initMaterials() {
        // First, register other mods' Registries
        MaterialRegistryManager managerInternal = (MaterialRegistryManager) GTCEuAPI.materialManager;

        GTCEu.LOGGER.info("Registering material registries");
        ModLoader.postEvent(new MaterialRegistryEvent());

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
        ModLoader.postEvent(materialEvent);
        if (GTCEu.isKubeJSLoaded()) {
            KJSEventWrapper.materialRegistry();
        }

        // Fire Post-Material event, intended for when Materials need to be iterated over in-full before freezing
        // Block entirely new Materials from being added in the Post event
        managerInternal.closeRegistries();
        ModLoader.postEvent(new PostMaterialEvent());
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
            CraftingHelper.register(SizedIngredient.TYPE, SizedIngredient.SERIALIZER);
            CraftingHelper.register(IntCircuitIngredient.TYPE, IntCircuitIngredient.SERIALIZER);
        });
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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (Block block : BuiltInRegistries.BLOCK) {
            if (ConfigHolder.INSTANCE.compat.energy.nativeEUToFE &&
                    event.isBlockRegistered(Capabilities.EnergyStorage.BLOCK, block)) {
                event.registerBlock(GTCapability.CAPABILITY_ENERGY_CONTAINER,
                        (level, pos, state, blockEntity, side) -> {
                            IEnergyStorage forgeEnergy = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos,
                                    state, blockEntity, side);
                            if (forgeEnergy != null) {
                                return new GTEnergyWrapper(forgeEnergy);
                            }
                            return null;
                        }, block);
            }

            if (block instanceof FluidPipeBlock fluidPipe) {
                fluidPipe.attachCapabilities(event);
            } else if (block instanceof CableBlock cable) {
                cable.attachCapabilities(event);
            } else if (block instanceof ItemPipeBlock itemPipe) {
                itemPipe.attachCapabilities(event);
            } else if (block instanceof LaserPipeBlock laserPipe) {
                laserPipe.attachCapabilities(event);
            } else if (block instanceof IMachineBlock machine) {
                machine.attachCapabilities(event);
            }
        }

        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof IComponentItem componentItem) {
                componentItem.attachCapabilities(event);
            } else if (item instanceof IGTTool tool) {
                tool.attachCapabilities(event);
            } else if (item instanceof DrumMachineItem drum) {
                drum.attachCapabilities(event);
            } else if (item instanceof GTBucketItem bucket) {
                event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new FluidBucketWrapper(stack),
                        bucket);
            }
        }
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
        } else {
            // Clear old data
            GTDynamicDataPack.clearServer();

            // Register recipes & unification data again
            long startTime = System.currentTimeMillis();
            ChemicalHelper.reinitializeUnification();
            // recipes and loot tables have been moved to ReloadableServerResourcesMixin.
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
