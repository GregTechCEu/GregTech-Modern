package com.gregtechceu.gtceu.common;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.capability.compat.GTEnergyWrapper;
import com.gregtechceu.gtceu.api.gui.factory.CoverUIFactory;
import com.gregtechceu.gtceu.api.gui.factory.GTUIEditorFactory;
import com.gregtechceu.gtceu.api.gui.factory.MachineUIFactory;
import com.gregtechceu.gtceu.api.item.GTBucketItem;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.material.material.event.PostMaterialEvent;
import com.gregtechceu.gtceu.api.material.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.material.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.gregtechceu.gtceu.api.worldgen.OreVeinDefinition;
import com.gregtechceu.gtceu.api.worldgen.WorldGenLayers;
import com.gregtechceu.gtceu.api.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.worldgen.bedrockore.BedrockOreDefinition;
import com.gregtechceu.gtceu.api.worldgen.generator.IndicatorGenerators;
import com.gregtechceu.gtceu.api.worldgen.generator.VeinGenerators;
import com.gregtechceu.gtceu.common.block.*;
import com.gregtechceu.gtceu.common.item.DrumMachineItem;
import com.gregtechceu.gtceu.common.item.tool.rotation.CustomBlockRotations;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.FusionReactorMachine;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;
import com.gregtechceu.gtceu.common.pack.GTDynamicDataPack;
import com.gregtechceu.gtceu.common.pack.GTDynamicResourcePack;
import com.gregtechceu.gtceu.common.pack.GTPackSource;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.mixins.AbstractRegistrateAccessor;
import com.gregtechceu.gtceu.data.block.GTBlocks;
import com.gregtechceu.gtceu.data.block.GTMaterialBlocks;
import com.gregtechceu.gtceu.data.blockentity.GTBlockEntities;
import com.gregtechceu.gtceu.data.command.GTCommandArguments;
import com.gregtechceu.gtceu.data.cover.GTCovers;
import com.gregtechceu.gtceu.data.damagesource.GTDamageTypes;
import com.gregtechceu.gtceu.data.datafixer.GTDataFixers;
import com.gregtechceu.gtceu.data.datagen.GTRegistrateDatagen;
import com.gregtechceu.gtceu.data.datagen.lang.MaterialLangGenerator;
import com.gregtechceu.gtceu.data.effect.GTMobEffects;
import com.gregtechceu.gtceu.data.entity.GTEntityTypes;
import com.gregtechceu.gtceu.data.fluid.GTFluids;
import com.gregtechceu.gtceu.data.item.*;
import com.gregtechceu.gtceu.data.loot.ChestGenHooks;
import com.gregtechceu.gtceu.data.machine.GTMachineUtils;
import com.gregtechceu.gtceu.data.machine.GTMachines;
import com.gregtechceu.gtceu.data.material.GTElements;
import com.gregtechceu.gtceu.data.material.GTMaterials;
import com.gregtechceu.gtceu.data.misc.GTAttachmentTypes;
import com.gregtechceu.gtceu.data.misc.GTCreativeModeTabs;
import com.gregtechceu.gtceu.data.misc.GTDimensionMarkers;
import com.gregtechceu.gtceu.data.misc.GTValueProviderTypes;
import com.gregtechceu.gtceu.data.particle.GTParticleTypes;
import com.gregtechceu.gtceu.data.recipe.*;
import com.gregtechceu.gtceu.data.sound.GTSoundEntries;
import com.gregtechceu.gtceu.data.tag.GTIngredientTypes;
import com.gregtechceu.gtceu.data.tools.GTToolBehaviors;
import com.gregtechceu.gtceu.data.tools.GTToolTiers;
import com.gregtechceu.gtceu.data.worldgen.GTFeatures;
import com.gregtechceu.gtceu.integration.kjs.GTCEuStartupEvents;
import com.gregtechceu.gtceu.integration.kjs.events.MaterialModificationKubeEvent;
import com.gregtechceu.gtceu.integration.map.WaypointManager;
import com.gregtechceu.gtceu.integration.top.TheOneProbePlugin;
import com.gregtechceu.gtceu.utils.input.KeyBind;

import com.lowdragmc.lowdraglib.gui.factory.UIFactory;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.ModifyRegistriesEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.callback.BakeCallback;

import com.google.common.collect.Multimaps;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import com.tterrag.registrate.providers.RegistrateProvider;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import mcjty.theoneprobe.api.ITheOneProbe;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.function.Function;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public class CommonInit {

    private static IEventBus modBus;

    public static void init(final IEventBus modBus) {
        CommonInit.modBus = modBus;
        modBus.register(CommonInit.class);

        UIFactory.register(MachineUIFactory.INSTANCE);
        UIFactory.register(CoverUIFactory.INSTANCE);
        UIFactory.register(GTUIEditorFactory.INSTANCE);

        GTRegistries.init(modBus);
        GTCreativeModeTabs.init();
        GTAttachmentTypes.ATTACHMENT_TYPES.register(modBus);

        FusionReactorMachine.registerFusionTier(GTValues.LuV, "MKI");
        FusionReactorMachine.registerFusionTier(GTValues.ZPM, "MKII");
        FusionReactorMachine.registerFusionTier(GTValues.UV, "MKIII");

        AddonFinder.getAddonList().forEach(IGTAddon::gtInitComplete);
    }

    // Only register everything once.
    private static boolean didRunRegistration = false;

    @SubscribeEvent
    public static void onRegister(RegisterEvent event) {
        if (didRunRegistration) {
            return;
        }
        didRunRegistration = true;

        GTElements.init();
        MaterialIconSet.init();
        MaterialIconType.init();
        initMaterials();
        TagPrefix.init();

        GTSoundEntries.init();
        GTDamageTypes.init();

        GTBlocks.init();
        GTFluids.init();

        GTDimensionMarkers.init();
        GTRecipeCapabilities.init();
        GTRecipeConditions.init();
        ChanceLogic.init();
        GTRecipeTypes.init();
        GTRecipeCategories.init();

        GTFoods.init();
        GTToolTiers.init();
        GTToolBehaviors.init();
        GTDataComponents.DATA_COMPONENTS.register(modBus);
        GTArmorMaterials.ARMOR_MATERIALS.register(modBus);
        GTItems.init();

        GTMachineUtils.init();
        GTCovers.init();
        GTMachines.init();

        GTEntityTypes.init();
        GTIngredientTypes.ITEM_INGREDIENT_TYPES.register(modBus);
        GTIngredientTypes.FLUID_INGREDIENT_TYPES.register(modBus);
        GTRecipeSerializers.RECIPE_SERIALIZERS.register(modBus);

        GTCommandArguments.COMMAND_ARGUMENT_TYPES.register(modBus);
        GTMobEffects.MOB_EFFECTS.register(modBus);
        GTParticleTypes.PARTICLE_TYPES.register(modBus);

        GTRegistrateDatagen.init();
        GTValueProviderTypes.register(modBus);
        GTFeatures.register(modBus);
        WorldGenLayers.registerAll();
        VeinGenerators.registerAddonGenerators();
        IndicatorGenerators.registerAddonGenerators();
        WaypointManager.init();

        CustomBlockRotations.init();
        KeyBind.init();
        MachineOwner.init();
        ChestGenHooks.init();
        GTDataFixers.init();
    }

    @ApiStatus.Internal
    public static void initMaterials() {
        GTCEu.LOGGER.info("Registering GTCEu Materials");
        GTMaterials.init();
        GTRegistries.MATERIALS.setFallbackMaterial(GTCEu.MOD_ID, GTMaterials.Aluminium);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRegisterVeryLate(RegisterEvent event) {
        // Material event *should* happen before any of the others here
        if (event.getRegistryKey() == GTRegistries.MATERIAL_REGISTRY) {
            // Fire Post-Material event, intended for when Materials need to be iterated over in-full before freezing
            // Block entirely new Materials from being added in the Post event
            GTRegistries.MATERIALS.close();
            ModLoader.postEventWrapContainerInModOrder(new PostMaterialEvent());
            if (GTCEu.Mods.isKubeJSLoaded()) {
                KJSEventWrapper.materialModification();
            }
            // --spacer--
        } else if (event.getRegistryKey() == Registries.FLUID) {
            // Material fluids
            GTFluids.generateMaterialFluids();
            // --spacer--
        } else if (event.getRegistryKey() == Registries.BLOCK) {
            // Material Blocks
            REGISTRATE.creativeModeTab(GTCreativeModeTabs.MATERIAL_BLOCK);
            GTMaterialBlocks.generateMaterialBlocks();   // Compressed Blocks
            GTMaterialBlocks.generateOreBlocks();        // Ore Blocks
            GTMaterialBlocks.generateOreIndicators();    // Ore Indicators
            GTMaterialBlocks.buildMaterialBlockTable();

            // Material Pipes/Wires
            REGISTRATE.creativeModeTab(GTCreativeModeTabs.MATERIAL_PIPE);
            GTMaterialBlocks.generateCableBlocks();        // Cable & Wire Blocks
            GTMaterialBlocks.generateFluidPipeBlocks();    // Fluid Pipe Blocks
            GTMaterialBlocks.generateItemPipeBlocks();     // Item Pipe Blocks
            // --spacer--
        } else if (event.getRegistryKey() == Registries.ITEM) {
            // Material Items & Tools
            GTMaterialItems.generateMaterialItems();
            GTMaterialItems.generateTools();
            // --spacer--
        } else if (event.getRegistryKey() == Registries.BLOCK_ENTITY_TYPE) {
            GTBlockEntities.init();
        }
    }

    private static void postInitMaterials() {
        // Register all material manager registries, for materials with mod ids.
        GTCEuAPI.materialManager.getUsedNamespaces().forEach(namespace -> {
            // Force the material lang generator to be at index 0, so that addons' lang generators can override it.
            GTRegistrate registrate = GTRegistrate.createIgnoringListenerErrors(namespace);
            AbstractRegistrateAccessor accessor = (AbstractRegistrateAccessor) registrate;
            if (accessor.getDoDatagen().get()) {
                List<NonNullConsumer<? extends RegistrateProvider>> providers = Multimaps.asMap(accessor.getDatagens())
                        .get(ProviderType.LANG);
                providers.addFirst(
                        (provider) -> MaterialLangGenerator.generate((RegistrateLangProvider) provider, namespace));
            }

            ModList.get().getModContainerById(namespace)
                    .map(ModContainer::getEventBus)
                    .ifPresent(registrate::registerEventListeners);
        });
    }

    @SubscribeEvent
    public static void registerRegistries(NewRegistryEvent event) {
        GTRegistries.getRegistries().forEach(event::register);
    }

    @SubscribeEvent
    public static void registerDataPackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(GTRegistries.ORE_VEIN_REGISTRY,
                OreVeinDefinition.DIRECT_CODEC, OreVeinDefinition.DIRECT_CODEC);
        event.dataPackRegistry(GTRegistries.BEDROCK_FLUID_REGISTRY,
                BedrockFluidDefinition.DIRECT_CODEC, BedrockFluidDefinition.DIRECT_CODEC);
        event.dataPackRegistry(GTRegistries.BEDROCK_ORE_REGISTRY,
                BedrockOreDefinition.DIRECT_CODEC, BedrockOreDefinition.DIRECT_CODEC);
    }

    @SubscribeEvent
    public static void modifyRegistries(ModifyRegistriesEvent event) {
        // noinspection UnstableApiUsage
        GTRegistries.MATERIALS.addCallback((BakeCallback<Material>) registry -> postInitMaterials());
    }

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {}

    @SubscribeEvent
    public static void loadComplete(FMLLoadCompleteEvent event) {}

    @SubscribeEvent
    public static void interModProcess(InterModProcessEvent event) {
        InterModComms.sendTo("theoneprobe", "getTheOneProbe",
                () -> (Function<ITheOneProbe, Void>) TheOneProbePlugin::init);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
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
            } else if (block instanceof DuctPipeBlock duct) {
                duct.attachCapabilities(event);
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
                event.registerItem(Capabilities.FluidHandler.ITEM,
                        (stack, ctx) -> new FluidBucketWrapper(stack), bucket);
            }
        }
    }

    @SubscribeEvent
    public static void registerPackFinders(AddPackFindersEvent event) {
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

            // LOADING MOVED TO ReloadableServerResourcesMixin

            event.addRepositorySource(new GTPackSource("gtceu:dynamic_data",
                    event.getPackType(),
                    Pack.Position.BOTTOM,
                    GTDynamicDataPack::new));
        }
    }

    @SubscribeEvent
    public static void addValidBlocksToBETypes(BlockEntityTypeAddBlocksEvent event) {
        event.modify(BlockEntityType.SIGN,
                GTBlocks.RUBBER_SIGN.get(),
                GTBlocks.RUBBER_WALL_SIGN.get(),
                GTBlocks.TREATED_WOOD_SIGN.get(),
                GTBlocks.TREATED_WOOD_WALL_SIGN.get());
        event.modify(BlockEntityType.HANGING_SIGN,
                GTBlocks.RUBBER_HANGING_SIGN.get(),
                GTBlocks.RUBBER_WALL_HANGING_SIGN.get(),
                GTBlocks.TREATED_WOOD_HANGING_SIGN.get(),
                GTBlocks.TREATED_WOOD_WALL_HANGING_SIGN.get());
    }

    public static final class KJSEventWrapper {

        public static void materialModification() {
            GTCEuStartupEvents.MATERIAL_MODIFICATION.post(new MaterialModificationKubeEvent());
        }
    }
}
