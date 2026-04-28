package com.gregtechceu.gtceu.client;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.cosmetics.event.RegisterGTCapesEvent;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.color.GTItemTintSource;
import com.gregtechceu.gtceu.client.model.LegacyCustomBlockStateModel;
import com.gregtechceu.gtceu.client.model.LegacyCustomItemModel;
import com.gregtechceu.gtceu.client.model.item.FacadeUnbakedModel;
import com.gregtechceu.gtceu.client.model.item.GTItemModelProperties;
import com.gregtechceu.gtceu.client.model.machine.MachineModelLoader;
import com.gregtechceu.gtceu.client.model.pipe.PipeModel;
import com.gregtechceu.gtceu.client.model.pipe.PipeModelLoader;
import com.gregtechceu.gtceu.client.particle.HazardParticle;
import com.gregtechceu.gtceu.client.particle.MufflerParticle;
import com.gregtechceu.gtceu.client.renderer.block.MaterialBlockRenderer;
import com.gregtechceu.gtceu.client.renderer.block.OreBlockRenderer;
import com.gregtechceu.gtceu.client.renderer.block.SurfaceRockRenderer;
import com.gregtechceu.gtceu.client.renderer.entity.GTExplosiveRenderer;
import com.gregtechceu.gtceu.client.renderer.item.ArmorItemRenderer;
import com.gregtechceu.gtceu.client.renderer.item.TagPrefixItemRenderer;
import com.gregtechceu.gtceu.client.renderer.item.ToolItemRenderer;
import com.gregtechceu.gtceu.client.renderer.item.decorator.GTComponentItemDecorator;
import com.gregtechceu.gtceu.client.renderer.item.decorator.GTLampItemOverlayRenderer;
import com.gregtechceu.gtceu.client.renderer.item.decorator.GTTankItemFluidPreview;
import com.gregtechceu.gtceu.client.renderer.item.decorator.GTToolBarRenderer;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderManager;
import com.gregtechceu.gtceu.client.renderer.machine.impl.*;
import com.gregtechceu.gtceu.client.renderer.machine.impl.BoilerMultiPartRender;
import com.gregtechceu.gtceu.common.CommonEventListener;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTEntityTypes;
import com.gregtechceu.gtceu.common.data.GTFluids;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;
import com.gregtechceu.gtceu.common.data.GTParticleTypes;
import com.gregtechceu.gtceu.common.data.models.GTModels;
import com.gregtechceu.gtceu.common.item.DrumMachineItem;
import com.gregtechceu.gtceu.common.item.LampBlockItem;
import com.gregtechceu.gtceu.common.item.QuantumTankMachineItem;
import com.gregtechceu.gtceu.common.network.packets.SCPacketMonitorGroupNBTChange;
import com.gregtechceu.gtceu.common.network.packets.SCPacketShareProspection;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.model.builder.PipeModelBuilder;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;
import com.gregtechceu.gtceu.data.pack.event.RegisterDynamicResourcesEvent;
import com.gregtechceu.gtceu.integration.kjs.GregTechKubeJSPlugin;
import com.gregtechceu.gtceu.integration.map.ClientCacheManager;
import com.gregtechceu.gtceu.integration.map.cache.client.GTClientCache;
import com.gregtechceu.gtceu.integration.map.ftbchunks.FTBChunksPlugin;
import com.gregtechceu.gtceu.integration.map.layer.Layers;
import com.gregtechceu.gtceu.integration.map.layer.builtin.FluidRenderLayer;
import com.gregtechceu.gtceu.integration.map.layer.builtin.OreRenderLayer;
import com.gregtechceu.gtceu.utils.data.RuntimeBlockstateProvider;
import com.gregtechceu.gtceu.utils.input.SyncedKeyMapping;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.components.debug.DebugScreenProfile;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

import java.util.ArrayList;

public class ClientProxy {

    public static void init(IEventBus modBus) {
        modBus.register(ClientProxy.class);
        if (!GTCEu.isDataGen()) {
            ClientCacheManager.registerClientCache(GTClientCache.instance, "gtceu");
            Layers.registerLayer(OreRenderLayer::new, "ore_veins");
            Layers.registerLayer(FluidRenderLayer::new, "bedrock_fluids");
            CommonEventListener.registerCapes(new RegisterGTCapesEvent());
        }
        initializeDynamicRenders();
    }

    @SubscribeEvent
    public static void onRegisterEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(GTEntityTypes.DYNAMITE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(GTEntityTypes.POWDERBARREL.get(), GTExplosiveRenderer::new);
        event.registerEntityRenderer(GTEntityTypes.INDUSTRIAL_TNT.get(), GTExplosiveRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterItemDecorations(RegisterItemDecorationsEvent event) {
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof IComponentItem) {
                event.register(item, GTComponentItemDecorator.INSTANCE);
            }
            if (item instanceof IGTTool) {
                event.register(item, GTToolBarRenderer.INSTANCE);
            }
            if (item instanceof LampBlockItem) {
                event.register(item, GTLampItemOverlayRenderer.INSTANCE);
            }
            if (item instanceof DrumMachineItem) {
                event.register(item, GTTankItemFluidPreview.DRUM);
            }
            if (item instanceof QuantumTankMachineItem) {
                event.register(item, GTTankItemFluidPreview.QUANTUM_TANK);
            }
        }
    }

    @SubscribeEvent
    public static void registerKeyBindings(RegisterKeyMappingsEvent event) {
        SyncedKeyMapping.onRegisterKeyBinds(event);
    }

    @SubscribeEvent
    public static void registerClientPayloadHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(SCPacketMonitorGroupNBTChange.TYPE, SCPacketMonitorGroupNBTChange::execute);
        event.register(SCPacketShareProspection.TYPE, SCPacketShareProspection::execute);
    }

    @SubscribeEvent
    public static void onRegisterGuiOverlays(RegisterGuiLayersEvent event) {
        event.registerAboveAll(GTCEu.id("hud"), new HudGuiOverlay());
    }

    @SubscribeEvent
    public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(GTParticleTypes.HAZARD_PARTICLE.get(), HazardParticle.Provider::new);
        event.registerSpriteSet(GTParticleTypes.MUFFLER_PARTICLE.get(), MufflerParticle.Provider::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        if (ConfigHolder.INSTANCE.compat.minimap.toggle.ftbChunksIntegration &&
                GTCEu.isModLoaded(GTValues.MODID_FTB_CHUNKS)) {
            FTBChunksPlugin.addEventListeners();
        }
    }

    public static void initializeDynamicRenders() {
        DynamicRenderManager.register(GTCEu.id("quantum_tank_fluid"), QuantumTankFluidRender.TYPE);
        DynamicRenderManager.register(GTCEu.id("quantum_chest_item"), QuantumChestItemRender.TYPE);

        DynamicRenderManager.register(GTCEu.id("fusion_ring"), FusionRingRender.TYPE);
        DynamicRenderManager.register(GTCEu.id("boiler_multi_parts"), BoilerMultiPartRender.TYPE);

        DynamicRenderManager.register(GTCEu.id("fluid_area"), FluidAreaRender.TYPE);
        DynamicRenderManager.register(GTCEu.id("growing_plant"), GrowingPlantRender.TYPE);

        DynamicRenderManager.register(GTCEu.id("central_monitor"), CentralMonitorRender.TYPE);
    }

    @SubscribeEvent
    public static void registerModelLoaders(ModelEvent.RegisterLoaders event) {
        event.register(MachineModelLoader.ID, MachineModelLoader.INSTANCE);
        event.register(PipeModelLoader.ID, PipeModelLoader.INSTANCE);
        event.register(GTCEu.id("facade"), FacadeUnbakedModel.Loader.INSTANCE);
    }

    @SubscribeEvent
    public static void registerBlockStateModels(RegisterBlockStateModels event) {
        event.registerModel(LegacyCustomBlockStateModel.ID, LegacyCustomBlockStateModel.MAP_CODEC);
    }

    @SubscribeEvent
    public static void registerItemModels(RegisterItemModelsEvent event) {
        event.register(LegacyCustomItemModel.ID, LegacyCustomItemModel.MAP_CODEC);
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(IClientFluidTypeExtensions.DEFAULT, GTFluids.POTION.getType());
    }

    @SubscribeEvent
    public static void registerDebugEntries(RegisterDebugEntriesEvent event) {
        Identifier id = GTCEu.id("machine_debug");
        event.register(id, (displayer, serverOrClientLevel, clientChunk, serverChunk) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.showOnlyReducedInfo()) return;
            Entity cameraEntity = mc.getCameraEntity();
            if (cameraEntity == null || mc.level == null) return;

            BlockHitResult hit = ToolHelper.entityPickBlock(cameraEntity, 20.0, 0, false);
            if (hit.getType() == HitResult.Type.MISS) return;
            BlockPos hitPos = hit.getBlockPos();
            BlockEntity blockEntity = mc.level.getBlockEntity(hitPos);
            if (!(blockEntity instanceof MetaMachine machine)) return;

            var lines = new ArrayList<String>();
            machine.addDebugOverlayText(lines::add);
            if (!lines.isEmpty()) {
                displayer.addToGroup(DebugScreenEntries.LOOKING_AT_BLOCK_STATE, lines);
            }
        });
        event.includeInProfile(id, DebugScreenProfile.DEFAULT, DebugScreenEntryStatus.IN_OVERLAY);
    }

    @SubscribeEvent
    public static void registerItemTintSources(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(GTCEu.id("item_color"), GTItemTintSource.MAP_CODEC);
    }

    @SubscribeEvent
    public static void registerRangeItemModelProperties(RegisterRangeSelectItemModelPropertyEvent event) {
        GTItemModelProperties.registerRangeProperties(event);
    }

    @SubscribeEvent
    public static void registerConditionalItemModelProperties(RegisterConditionalItemModelPropertyEvent event) {
        GTItemModelProperties.registerConditionalProperties(event);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void preRegisterDynamicAssets(RegisterDynamicResourcesEvent event) {
        PipeModel.DYNAMIC_MODELS.clear();
    }

    @SubscribeEvent
    public static void registerDynamicAssets(RegisterDynamicResourcesEvent event) {
        // regenerate all pipe models in case their textures changed
        // cables may do this, others too if something's removed
        for (var block : GTMaterialBlocks.CABLE_BLOCKS.values()) {
            if (block == null) continue;
            block.get().createPipeModel(RuntimeBlockstateProvider.INSTANCE).dynamicModel();
        }
        for (var block : GTMaterialBlocks.FLUID_PIPE_BLOCKS.values()) {
            if (block == null) continue;
            block.get().createPipeModel(RuntimeBlockstateProvider.INSTANCE).dynamicModel();
        }
        for (var block : GTMaterialBlocks.ITEM_PIPE_BLOCKS.values()) {
            if (block == null) continue;
            block.get().createPipeModel(RuntimeBlockstateProvider.INSTANCE).dynamicModel();
        }

        MaterialBlockRenderer.reinitModels();
        TagPrefixItemRenderer.reinitModels();
        OreBlockRenderer.reinitModels();
        ToolItemRenderer.reinitModels();
        ArmorItemRenderer.reinitModels();
        SurfaceRockRenderer.reinitModels();
        GTModels.registerMaterialFluidModels();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void postRegisterDynamicAssets(RegisterDynamicResourcesEvent event) {
        // do this last so addons can easily add new variants to the registered model set
        PipeModel.initDynamicModels();

        if (GTCEu.Mods.isKubeJSLoaded()) {
            GregTechKubeJSPlugin.generateMachineBlockModels();
        }
        RuntimeBlockstateProvider.INSTANCE.run();
        registerLegacyModelBlockStates();
        PipeModelBuilder.clearRestrictorModelCache();
    }

    private static void registerLegacyModelBlockStates() {
        for (MachineDefinition definition : GTRegistries.MACHINES) {
            Identifier blockId = BuiltInRegistries.BLOCK.getKey(definition.getBlock());
            Identifier modelId = definition.getId().withPrefix("block/machine/");
            GTDynamicResourcePack.addBlockState(blockId, LegacyCustomBlockStateModel.singleVariantJson(modelId));
            GTDynamicResourcePack.addItemDefinition(blockId, LegacyCustomItemModel.itemDefinitionJson(modelId));
        }

        if (GTMaterialBlocks.CABLE_BLOCKS != null) {
            GTMaterialBlocks.CABLE_BLOCKS.values().forEach(block -> {
                if (block != null) registerLegacyPipeBlockState(block.get());
            });
        }
        if (GTMaterialBlocks.FLUID_PIPE_BLOCKS != null) {
            GTMaterialBlocks.FLUID_PIPE_BLOCKS.values().forEach(block -> {
                if (block != null) registerLegacyPipeBlockState(block.get());
            });
        }
        if (GTMaterialBlocks.ITEM_PIPE_BLOCKS != null) {
            GTMaterialBlocks.ITEM_PIPE_BLOCKS.values().forEach(block -> {
                if (block != null) registerLegacyPipeBlockState(block.get());
            });
        }

        for (var block : GTBlocks.DUCT_PIPES) {
            if (block != null) registerLegacyPipeBlockState(block.get());
        }
        for (var block : GTBlocks.LASER_PIPES) {
            if (block != null) registerLegacyActivePipeBlockState(block.get());
        }
        for (var block : GTBlocks.OPTICAL_PIPES) {
            if (block != null) registerLegacyActivePipeBlockState(block.get());
        }
    }

    private static void registerLegacyPipeBlockState(PipeBlock<?, ?, ?> block) {
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);
        GTDynamicResourcePack.addBlockState(blockId,
                LegacyCustomBlockStateModel.singleVariantJson(blockId.withPrefix("block/")));
    }

    private static void registerLegacyActivePipeBlockState(PipeBlock<?, ?, ?> block) {
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);
        Identifier inactiveModel = blockId.withPrefix("block/");
        GTDynamicResourcePack.addBlockState(blockId,
                LegacyCustomBlockStateModel.activeVariantJson(inactiveModel, inactiveModel.withSuffix("_active")));
    }
}
