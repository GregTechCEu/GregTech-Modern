package com.gregtechceu.gtceu.client;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.cosmetics.event.RegisterGTCapesEvent;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.LampBlockItem;
import com.gregtechceu.gtceu.client.model.item.FacadeUnbakedModel;
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
import com.gregtechceu.gtceu.common.item.DrumMachineItem;
import com.gregtechceu.gtceu.common.item.QuantumTankMachineItem;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.block.GTMaterialBlocks;
import com.gregtechceu.gtceu.data.datagen.model.builder.PipeModelBuilder;
import com.gregtechceu.gtceu.data.entity.GTEntityTypes;
import com.gregtechceu.gtceu.data.fluid.GTFluids;
import com.gregtechceu.gtceu.data.model.GTModels;
import com.gregtechceu.gtceu.data.pack.event.RegisterDynamicResourcesEvent;
import com.gregtechceu.gtceu.data.particle.GTParticleTypes;
import com.gregtechceu.gtceu.forge.CommonEventListener;
import com.gregtechceu.gtceu.integration.kjs.GTKubeJSPlugin;
import com.gregtechceu.gtceu.integration.map.ClientCacheManager;
import com.gregtechceu.gtceu.integration.map.cache.client.GTClientCache;
import com.gregtechceu.gtceu.integration.map.ftbchunks.FTBChunksPlugin;
import com.gregtechceu.gtceu.integration.map.layer.Layers;
import com.gregtechceu.gtceu.integration.map.layer.builtin.FluidRenderLayer;
import com.gregtechceu.gtceu.integration.map.layer.builtin.OreRenderLayer;
import com.gregtechceu.gtceu.utils.data.RuntimeBlockstateProvider;
import com.gregtechceu.gtceu.utils.input.SyncedKeyMapping;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

public class ClientInit {

    public static void init(IEventBus modBus) {
        modBus.register(ClientInit.class);
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
        MachineOwner.init();
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
    public static void registerModelLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register(MachineModelLoader.ID, MachineModelLoader.INSTANCE);
        event.register(PipeModelLoader.ID, PipeModelLoader.INSTANCE);
        event.register(GTCEu.id("facade"), FacadeUnbakedModel.Loader.INSTANCE);
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {

            private static final ResourceLocation TEXTURE = GTCEu.id("block/fluids/fluid.potion");

            @Override
            public @NotNull ResourceLocation getStillTexture() {
                return TEXTURE;
            }

            @Override
            public @NotNull ResourceLocation getFlowingTexture() {
                return TEXTURE;
            }

            @Override
            public int getTintColor(@NotNull FluidStack stack) {
                return stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY)
                        .getColor() | 0xff000000;
            }
        }, GTFluids.POTION.getType());
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
            GTKubeJSPlugin.generateMachineBlockModels();
        }
        RuntimeBlockstateProvider.INSTANCE.run();
        PipeModelBuilder.clearRestrictorModelCache();
    }
}
