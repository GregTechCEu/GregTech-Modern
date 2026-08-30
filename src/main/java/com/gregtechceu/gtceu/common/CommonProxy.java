package com.gregtechceu.gtceu.common;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.capability.compat.EUToFEProvider;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.material.event.PostMaterialEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGenLayers;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockore.BedrockOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.generator.IndicatorGenerators;
import com.gregtechceu.gtceu.api.data.worldgen.generator.VeinGenerators;
import com.gregtechceu.gtceu.api.events.ModifyMachineEvent;
import com.gregtechceu.gtceu.api.events.RegisterSpoilablesEvent;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.misc.forge.QuantumFluidHandlerItemStack;
import com.gregtechceu.gtceu.api.mui.factory.CoverUIFactory;
import com.gregtechceu.gtceu.api.mui.factory.MachineUIFactory;
import com.gregtechceu.gtceu.api.multiblock.error.GTPatternErrors;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntCircuitIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderFluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.IntersectionMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.MapIngredientTypeManager;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.fluid.CustomFluidMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.fluid.FluidDataComponentMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.fluid.FluidStackMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.fluid.FluidTagMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item.*;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.entry.MachineEntry;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;
import com.gregtechceu.gtceu.common.block.*;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTPlacementModifierTypes;
import com.gregtechceu.gtceu.common.data.item.*;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.common.data.materials.GTFoods;
import com.gregtechceu.gtceu.common.fluid.potion.BottleItemFluidHandler;
import com.gregtechceu.gtceu.common.fluid.potion.PotionItemFluidHandler;
import com.gregtechceu.gtceu.common.item.DrumMachineItem;
import com.gregtechceu.gtceu.common.item.GTBucketItem;
import com.gregtechceu.gtceu.common.item.armor.GTArmorMaterials;
import com.gregtechceu.gtceu.common.item.behavior.SpoilableBehavior;
import com.gregtechceu.gtceu.common.item.tool.rotation.CustomBlockRotations;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.FusionReactorMachine;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;
import com.gregtechceu.gtceu.common.machine.storage.QuantumTankMachine;
import com.gregtechceu.gtceu.common.mui.GTGuiTheme;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.mixins.registrate.AbstractRegistrateAccessor;
import com.gregtechceu.gtceu.data.GregTechDatagen;
import com.gregtechceu.gtceu.data.lang.MaterialLangGenerator;
import com.gregtechceu.gtceu.data.loot.ChestGenHooks;
import com.gregtechceu.gtceu.data.pack.GTDynamicDataPack;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;
import com.gregtechceu.gtceu.data.pack.GTPackSource;
import com.gregtechceu.gtceu.data.recipe.*;
import com.gregtechceu.gtceu.integration.cctweaked.CCTweakedPlugin;
import com.gregtechceu.gtceu.integration.create.GTCreateIntegration;
import com.gregtechceu.gtceu.integration.kjs.GTCEuStartupEvents;
import com.gregtechceu.gtceu.integration.kjs.events.MaterialModificationEventJS;
import com.gregtechceu.gtceu.integration.kjs.helpers.KubeGTRegistryEventHandler;
import com.gregtechceu.gtceu.integration.map.WaypointManager;
import com.gregtechceu.gtceu.utils.input.SyncedKeyMappings;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.Capabilities.FluidHandler;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.common.crafting.IntersectionIngredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.neoforged.neoforge.fluids.crafting.*;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import brachy.modularui.factory.GuiManager;
import com.google.common.collect.Multimaps;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import com.tterrag.registrate.providers.RegistrateProvider;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public class CommonProxy {

    public static void init(final IEventBus modBus) {
        if (GTCEu.Mods.isKubeJSLoaded()) {
            // initialize this before the class's static listeners
            // so KubeJS materials are registered before the material registry is closed.
            modBus.register(KubeGTRegistryEventHandler.class);
        }
        modBus.register(CommonProxy.class);

        // Initialize the model generator before any content is loaded so machine models can use the generated data
        GregTechDatagen.initPre();

        GTRegistries.init(modBus);
        REGISTRATE.registerEventListeners(modBus);

        GTElements.init();
        MaterialIconSet.init();
        MaterialIconType.init();
        GTMaterials.init();
        GTMedicalConditions.init();
        TagPrefix.init();

        GTSoundEntries.init();
        GTDamageTypes.init();
        GTPlaceholders.init();

        if (ConfigHolder.INSTANCE.compat.createCompat && GTCEu.Mods.isCreateLoaded()) {
            GTCreateIntegration.init();
        }

        GTCovers.init();
        GTCreativeModeTabs.init();

        GTBlocks.init();
        GTFluids.init();

        GTDimensionMarkers.init(modBus);
        GTRecipeCapabilities.init();
        GTRecipeConditions.init();
        ChanceLogic.init();
        GTRecipeTypes.init();
        GTRecipeCategories.init();
        GTPatternErrors.init(modBus);

        GTFoods.init();
        GTToolTiers.init();
        GTToolBehaviors.init();
        GTDataComponents.init(modBus);
        GTArmorMaterials.init(modBus);
        GTItems.init();

        GTMachineUtils.init();
        GTCovers.init();
        GTMachines.init();

        GTEntityTypes.init();
        GTIngredientTypes.init(modBus);
        GTRecipeSerializers.init(modBus);

        GTMobEffects.init(modBus);
        GTParticleTypes.init(modBus);
        WorldGenLayers.init();

        GregTechDatagen.initPost();
        GTValueProviderTypes.init(modBus);
        GTFeatures.init(modBus);
        GTPlacementModifierTypes.init(modBus);
        VeinGenerators.registerAddonGenerators();
        IndicatorGenerators.registerAddonGenerators();
        WaypointManager.init();

        CustomBlockRotations.init();
        SyncedKeyMappings.init();
        MachineOwner.init();
        ChestGenHooks.init(modBus);

        GTCreativeModeTabs.init();
        GTAttachmentTypes.init(modBus);

        FusionReactorMachine.registerFusionTier(GTValues.LuV, "MKI");
        FusionReactorMachine.registerFusionTier(GTValues.ZPM, "MKII");
        FusionReactorMachine.registerFusionTier(GTValues.UV, "MKIII");

        // MUI stuff
        GuiManager.registerFactory(MachineUIFactory.INSTANCE);
        GuiManager.registerFactory(CoverUIFactory.INSTANCE);

        GTGuiTheme.registerThemes();
        SpoilableBehavior.init();
    }

    // Fire post material events after all other material registry events.
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRegisterLowest(RegisterEvent event) {
        if (event.getRegistryKey() == GTRegistries.Keys.MATERIAL) {
            // Fire Post-Material event, intended for when Materials need to be iterated over in-full before freezing
            // Block entirely new Materials from being added in the Post event
            GTCEu.LOGGER.info("Firing material register late event");
            GTRegistries.MATERIALS.close();
            ModLoader.postEventWrapContainerInModOrder(new PostMaterialEvent());
            if (GTCEu.Mods.isKubeJSLoaded()) {
                KJSEventWrapper.materialModification();
            }

            GTRegistries.MATERIALS.getUsedNamespaces().forEach(namespace -> {
                // Force the material lang generator to be at index 0, so that addons' lang generators can override it.
                var registrate = GTRegistrate.createIgnoringListenerErrors(namespace);
                AbstractRegistrateAccessor accessor = (AbstractRegistrateAccessor) registrate;
                if (accessor.getDoDatagen().get()) {
                    List<NonNullConsumer<? extends RegistrateProvider>> providers = Multimaps
                            .asMap(accessor.getDatagens())
                            .get(ProviderType.LANG);
                    providers.addFirst(
                            (provider) -> MaterialLangGenerator.generate((RegistrateLangProvider) provider, namespace));
                }
            });
        } else if (event.getRegistryKey() == GTRegistries.Keys.MACHINE) {
            // Prepare machine render states after all machines have been registered
            for (MachineDefinition machine : GTRegistries.MACHINES) {
                for (MachineRenderState renderState : machine.getStateDefinition().getPossibleStates()) {
                    MachineDefinition.RENDER_STATE_REGISTRY.add(renderState);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void registerMaterialContent(RegisterEvent event) {
        if (event.getRegistryKey() == Registries.BLOCK) {
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
            GTMaterialItems.generateArmors();
            // --spacer--
        } else if (event.getRegistryKey() == Registries.FLUID) {
            GTFluids.registerMaterialFluids();
        } else if (event.getRegistryKey() == Registries.BLOCK_ENTITY_TYPE) {
            GTBlockEntities.init();
        }
    }

    @SubscribeEvent
    public static void addSpoilTransferModifier(ModifyMachineEvent event) {
        event.getBuilder().addRecipeModifier(GTRecipeModifiers.SPOILAGE_TRANSFER);
    }

    @SubscribeEvent
    public static void registerDevSpoilables(RegisterSpoilablesEvent event) {
        if (GTCEu.isDev()) { // for testing purposes
            event.getBuilder()
                    .ticks(10)
                    .result(Items.DIRT)
                    .build()
                    .attachTo(Items.JIGSAW);
            event.getBuilder()
                    .ticks(10)
                    .result(Items.STRUCTURE_BLOCK)
                    .build()
                    .attachTo(Items.APPLE);
            event.getBuilder()
                    .ticks(40)
                    .result(Items.STRUCTURE_VOID)
                    .build()
                    .attachTo(Items.STRUCTURE_BLOCK);
            event.getBuilder()
                    .ticks(10)
                    .result(Items.JIGSAW)
                    .build()
                    .attachTo(Items.STRUCTURE_VOID);
            event.getBuilder()
                    .ticks(10)
                    .result(Items.DRAGON_EGG)
                    .result(EntityType.PIG)
                    .multiplyResult(3)
                    .build()
                    .attachTo(Items.EGG);
        }
    }

    @SubscribeEvent
    public static void registerRegistries(NewRegistryEvent event) {
        GTRegistries.getRegistries().forEach(event::register);
    }

    @SubscribeEvent
    public static void registerDataPackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(GTRegistries.Keys.ORE_VEIN,
                GTOreDefinition.DIRECT_CODEC, GTOreDefinition.DIRECT_CODEC);
        event.dataPackRegistry(GTRegistries.Keys.BEDROCK_FLUID,
                BedrockFluidDefinition.DIRECT_CODEC, BedrockFluidDefinition.DIRECT_CODEC);
        event.dataPackRegistry(GTRegistries.Keys.BEDROCK_ORE,
                BedrockOreDefinition.DIRECT_CODEC, BedrockOreDefinition.DIRECT_CODEC);
    }

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // register the map ingredient converters for all of our ingredients
            // spotless:off
            MapIngredientTypeManager.registerMapIngredient(SizedFluidIngredient.class, (ingredient) -> {
                FluidIngredient inner = ingredient.ingredient();
                return MapIngredientTypeManager.getFrom(inner, FluidRecipeCapability.CAP);
            });
            MapIngredientTypeManager.registerMapIngredient(IntProviderFluidIngredient.class, (ingredient) -> {
                FluidIngredient inner = ingredient.getInner();
                return MapIngredientTypeManager.getFrom(inner, FluidRecipeCapability.CAP);
            });
            MapIngredientTypeManager.registerMapIngredient(CompoundFluidIngredient.class, (ingredient) -> {
                List<AbstractMapIngredient> list = new ObjectArrayList<>();
                for (FluidIngredient child : ingredient.children()) {
                    list.addAll(MapIngredientTypeManager.getFrom(child, FluidRecipeCapability.CAP));
                }
                return list;
            });

            MapIngredientTypeManager.registerMapIngredient(DataComponentFluidIngredient.class, FluidDataComponentMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(FluidIngredient.class, FluidTagMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(SingleFluidIngredient.class, FluidStackMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(IntersectionFluidIngredient.class, IntersectionMapIngredient::from);

            MapIngredientTypeManager.registerMapIngredient(FluidStack.class, FluidTagMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(FluidStack.class, FluidStackMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(FluidStack.class, FluidDataComponentMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(FluidStack.class, CustomFluidMapIngredient::from);

            MapIngredientTypeManager.registerMapIngredient(SizedIngredient.class, (ingredient) -> {
                Ingredient inner = ingredient.ingredient();
                if (inner.isCustom()) {
                    return MapIngredientTypeManager.getFrom(inner.getCustomIngredient(), ItemRecipeCapability.CAP);
                } else {
                    return MapIngredientTypeManager.getFrom(inner, ItemRecipeCapability.CAP);
                }
            });
            MapIngredientTypeManager.registerMapIngredient(IntProviderIngredient.class, (ingredient) -> {
                Ingredient inner = ingredient.getInner();
                if (inner.isCustom()) {
                    return MapIngredientTypeManager.getFrom(inner.getCustomIngredient(), ItemRecipeCapability.CAP);
                } else {
                    return MapIngredientTypeManager.getFrom(inner, ItemRecipeCapability.CAP);
                }
            });

            MapIngredientTypeManager.registerMapIngredient(DataComponentIngredient.class, ItemDataComponentMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(Ingredient.class, ItemTagMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(Ingredient.class, ItemStackMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(IntersectionIngredient.class, IntersectionMapIngredient::from);

            MapIngredientTypeManager.registerMapIngredient(ItemStack.class, ItemStackMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(ItemStack.class, ItemTagMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(ItemStack.class, ItemDataComponentMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(ItemStack.class, IntersectionMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(ItemStack.class, CustomItemMapIngredient::from);

            MapIngredientTypeManager.registerMapIngredient(IntCircuitIngredient.class, custom ->
                    List.of(new ItemStackMapIngredient(GTItems.PROGRAMMED_CIRCUIT.asStack(), custom.toVanilla())));
            // spotless:on

            if (GTCEu.Mods.isCCTweakedLoaded()) {
                GTCEu.LOGGER.info("CC: Tweaked found. Enabling integration...");
                CCTweakedPlugin.init();
            }
        });
    }

    @SubscribeEvent
    public static void loadComplete(FMLLoadCompleteEvent event) {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(FluidHandler.ITEM, BottleItemFluidHandler::new, Items.GLASS_BOTTLE);

        Stream<MachineEntry<MachineDefinition>> quantumTanks = Stream.of(GTMachines.SUPER_TANK, GTMachines.QUANTUM_TANK)
                .flatMap(Arrays::stream);
        quantumTanks = Stream.concat(quantumTanks, Stream.of(GTMachines.CREATIVE_FLUID));
        event.registerItem(FluidHandler.ITEM, (stack, ctx) -> {
            if (!(stack.getItem() instanceof MetaMachineItem machineItem)) {
                return null;
            }
            long capacity = QuantumTankMachine.TANK_CAPACITY.getLong(machineItem.getDefinition());
            if (capacity == -1) {
                return null;
            }
            return new QuantumFluidHandlerItemStack(stack, capacity);
        }, quantumTanks.filter(Objects::nonNull).map(MachineEntry::getItem).toArray(Item[]::new));

        for (Block block : BuiltInRegistries.BLOCK) {
            if (ConfigHolder.INSTANCE.compat.energy.nativeEUToFE &&
                    event.isBlockRegistered(Capabilities.EnergyStorage.BLOCK, block)) {
                event.registerBlock(GTCapability.CAPABILITY_ENERGY_CONTAINER,
                        (level, pos, state, blockEntity, side) -> {
                            IEnergyStorage forgeEnergy = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos,
                                    state, blockEntity, side);
                            if (forgeEnergy != null) {
                                return new EUToFEProvider(forgeEnergy);
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
            } else if (block instanceof MetaMachineBlock machine) {
                machine.attachCapabilities(event);
            } else if (block instanceof OpticalPipeBlock optical) {
                optical.attachCapabilities(event);
            }
        }

        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof IComponentItem componentItem) {
                componentItem.attachCapabilities(event);
            } else if (item instanceof IGTTool tool) {
                tool.attachCapabilities(event);
            } else if (item instanceof DrumMachineItem drum) {
                drum.attachCapabilities(event);
            } else if (item instanceof GTBucketItem) {
                event.registerItem(Capabilities.FluidHandler.ITEM,
                        (stack, ctx) -> new FluidBucketWrapper(stack), item);
            } else if (item instanceof PotionItem) {
                event.registerItem(Capabilities.FluidHandler.ITEM, PotionItemFluidHandler::new, item);
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
            GTCEuStartupEvents.MATERIAL_MODIFICATION.post(new MaterialModificationEventJS());
        }
    }
}
