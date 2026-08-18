package com.gregtechceu.gtceu.common;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.event.PostMaterialEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGenLayers;
import com.gregtechceu.gtceu.api.data.worldgen.generator.IndicatorGenerators;
import com.gregtechceu.gtceu.api.data.worldgen.generator.VeinGenerators;
import com.gregtechceu.gtceu.api.events.ModifyMachineEvent;
import com.gregtechceu.gtceu.api.events.RegisterSpoilablesEvent;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.mui.factory.CoverUIFactory;
import com.gregtechceu.gtceu.api.mui.factory.MachineUIFactory;
import com.gregtechceu.gtceu.api.multiblock.error.GTPatternErrors;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.ingredient.*;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.*;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.fluid.*;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item.*;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.common.data.loot.*;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.common.data.materials.AlloyBlastPropertyAddition;
import com.gregtechceu.gtceu.common.data.materials.GTFoods;
import com.gregtechceu.gtceu.common.data.worldgen.*;
import com.gregtechceu.gtceu.common.data.worldgen.GTFeatures;
import com.gregtechceu.gtceu.common.data.worldgen.GTPlacementModifiers;
import com.gregtechceu.gtceu.common.item.behavior.SpoilableBehavior;
import com.gregtechceu.gtceu.common.item.tool.rotation.CustomBlockRotations;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.FusionReactorMachine;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;
import com.gregtechceu.gtceu.common.mui.GTGuiTheme;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.mixins.registrate.AbstractRegistrateAccessor;
import com.gregtechceu.gtceu.data.GregTechDatagen;
import com.gregtechceu.gtceu.data.lang.MaterialLangGenerator;
import com.gregtechceu.gtceu.data.pack.GTDynamicDataPack;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;
import com.gregtechceu.gtceu.data.pack.GTPackSource;
import com.gregtechceu.gtceu.data.recipe.GTCraftingComponents;
import com.gregtechceu.gtceu.integration.cctweaked.CCTweakedPlugin;
import com.gregtechceu.gtceu.integration.create.GTCreateIntegration;
import com.gregtechceu.gtceu.integration.jade.GTJadePlugin;
import com.gregtechceu.gtceu.integration.kjs.GTCEuStartupEvents;
import com.gregtechceu.gtceu.integration.kjs.events.MaterialModificationEventJS;
import com.gregtechceu.gtceu.integration.map.WaypointManager;
import com.gregtechceu.gtceu.utils.input.KeyBind;
import com.gregtechceu.gtceu.utils.input.SyncedKeyMappings;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IntersectionIngredient;
import net.minecraftforge.common.crafting.PartialNBTIngredient;
import net.minecraftforge.common.crafting.StrictNBTIngredient;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DataPackRegistryEvent;
import net.minecraftforge.registries.RegisterEvent;

import brachy.modularui.factory.GuiManager;
import com.google.common.collect.Multimaps;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import com.tterrag.registrate.providers.RegistrateProvider;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

@ApiStatus.Internal
public class CommonProxy {

    public static void init(IEventBus modBus) {
        GTCEu.LOGGER.info("GTCEu common proxy init!");

        // used for forge events (ClientProxy + CommonProxy)
        modBus.register(CommonProxy.class);
        ConfigHolder.init();
        GTCEuAPI.initializeHighTier();

        if (GTCEu.isDev()) {
            ConfigHolder.INSTANCE.recipes.generateLowQualityGems = true;
            ConfigHolder.INSTANCE.compat.energy.enableFEConverters = true;
        }

        modBus.addListener(AlloyBlastPropertyAddition::addAlloyBlastProperties);

        GTNetwork.init();
        GTRegistries.init(modBus);

        // Initialize the model generator before any content is loaded so machine models can use the generated data
        GregTechDatagen.initPre();

        GTValueProviderTypes.init(modBus);
        GTPlacementModifiers.init(modBus);
        GTGlobalLootModifiers.init(modBus);
        GTLootConditions.init(modBus);
        GTLootFunctions.init(modBus);
        GTFeatures.init(modBus);
        GTCommandArguments.init(modBus);
        GTMobEffects.init(modBus);
        GTParticleTypes.init(modBus);
        SpoilableBehavior.init(modBus);

        GTRecipeCapabilities.init();
        GTRecipeConditions.init();
        GTToolTiers.init();
        GTElements.init();
        MaterialIconSet.init();
        MaterialIconType.init();
        initMaterials();
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
        GTEntityTypes.init();
        GTRecipeTypes.init();
        GTRecipeCategories.init();
        GTPatternErrors.init();
        GTMachineUtils.init();
        GTMachines.init();

        GTFoods.init();
        GTItems.init();
        GTDimensionMarkers.init();
        ChanceLogic.init();
        WaypointManager.init();
        AddonFinder.getAddons().forEach(IGTAddon::initializeAddon);

        GregTechDatagen.initPost();

        WorldGenLayers.init();
        VeinGenerators.registerAddonGenerators();
        IndicatorGenerators.registerAddonGenerators();

        CustomBlockRotations.init();
        KeyBind.init();
        SyncedKeyMappings.init();
        MachineOwner.init();

        // MUI stuff
        GuiManager.registerFactory(MachineUIFactory.INSTANCE);
        GuiManager.registerFactory(CoverUIFactory.INSTANCE);

        GTGuiTheme.registerThemes();

        FusionReactorMachine.registerFusionTier(GTValues.LuV, " (MKI)");
        FusionReactorMachine.registerFusionTier(GTValues.ZPM, " (MKII)");
        FusionReactorMachine.registerFusionTier(GTValues.UV, " (MKIII)");
    }

    private static void initMaterials() {
        // First, register CEu Materials
        GTCEu.LOGGER.info("Registering GTCEu Materials");
        GTMaterials.init();

        // Then, register addon Materials
        GTCEu.LOGGER.info("Registering addon Materials");
        MaterialEvent materialEvent = new MaterialEvent();
        ModLoader.get().postEvent(materialEvent);
    }

    // Fire post material events after all other material registry events.
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRegisterLowest(RegisterEvent event) {
        if (event.getRegistryKey() == GTRegistries.Keys.MATERIAL) {
            // Fire Post-Material event, intended for when Materials need to be iterated over in-full before freezing
            // Block entirely new Materials from being added in the Post event
            GTCEu.LOGGER.info("Firing material register late event");
            GTRegistries.MATERIALS.closeRegistry();
            ModLoader.get().postEventWrapContainerInModOrder(new PostMaterialEvent());
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
                    NonNullConsumer<? extends RegistrateProvider> generator = (provider) -> MaterialLangGenerator
                            .generate((RegistrateLangProvider) provider, namespace);
                    if (providers == null) {
                        accessor.getDatagens().put(ProviderType.LANG, generator);
                    } else {
                        providers.add(0, generator);
                    }
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
            GTCEu.LOGGER.info("Generating material blocks...");

            // Material Blocks
            REGISTRATE.creativeModeTab(GTCreativeModeTabs.MATERIAL_BLOCK);
            GTMaterialBlocks.generateMaterialBlocks();   // Compressed Blocks
            GTMaterialBlocks.generateOreBlocks();        // Ore Blocks
            GTMaterialBlocks.generateOreIndicators();    // Ore Indicators

            // Material Pipes/Wires
            REGISTRATE.creativeModeTab(GTCreativeModeTabs.MATERIAL_PIPE);
            GTMaterialBlocks.generateCableBlocks();        // Cable & Wire Blocks
            GTMaterialBlocks.generateFluidPipeBlocks();    // Fluid Pipe Blocks
            GTMaterialBlocks.generateItemPipeBlocks();     // Item Pipe Blocks

            GTMaterialBlocks.finaliseMaterialBlocks();

        } else if (event.getRegistryKey() == Registries.ITEM) {
            GTCEu.LOGGER.info("Generating material items...");

            // Material Items & Tools
            GTMaterialItems.generateMaterialItems();
            GTMaterialItems.generateTools();
            GTMaterialItems.generateArmors();
            if (GTCEu.Mods.isJadeLoaded()) GTJadePlugin.registerToolHandlers();

        } else if (event.getRegistryKey() == Registries.FLUID) {
            GTCEu.LOGGER.info("Generating material fluids...");
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
    public static void registerDataPackRegistries(DataPackRegistryEvent.NewRegistry event) {
        /*
         * event.dataPackRegistry(GTRegistries.Keys.ORE_VEIN,
         * GTOreDefinition.CODEC, GTOreDefinition.CODEC);
         * event.dataPackRegistry(GTRegistries.Keys.BEDROCK_FLUID,
         * BedrockFluidDefinition.FULL_CODEC, BedrockFluidDefinition.FULL_CODEC);
         * event.dataPackRegistry(GTRegistries.Keys.BEDROCK_ORE,
         * BedrockOreDefinition.FULL_CODEC, BedrockOreDefinition.FULL_CODEC);
         */
    }

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
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
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        GTCapability.register(event);
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

            long startTime = System.currentTimeMillis();
            GTCraftingComponents.init();
            GTRecipes.recipeRemoval();
            GTRecipes.recipeAddition(GTDynamicDataPack::addRecipe);
            GTCEu.LOGGER.info("GregTech Data loading took {}ms", System.currentTimeMillis() - startTime);

            event.addRepositorySource(new GTPackSource("gtceu:dynamic_data",
                    event.getPackType(),
                    Pack.Position.BOTTOM,
                    GTDynamicDataPack::new));
        }
    }

    public static final class KJSEventWrapper {

        public static void materialModification() {
            GTCEuStartupEvents.MATERIAL_MODIFICATION.post(new MaterialModificationEventJS());
        }
    }
}
