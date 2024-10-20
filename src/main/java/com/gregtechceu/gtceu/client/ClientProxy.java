package com.gregtechceu.gtceu.client;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockore.BedrockOreDefinition;
import com.gregtechceu.gtceu.api.gui.compass.GTCompassUIConfig;
import com.gregtechceu.gtceu.api.gui.compass.GTRecipeViewCreator;
import com.gregtechceu.gtceu.api.gui.compass.MultiblockAction;
import com.gregtechceu.gtceu.client.particle.HazardParticle;
import com.gregtechceu.gtceu.client.renderer.entity.GTBoatRenderer;
import com.gregtechceu.gtceu.client.renderer.entity.GTExplosiveRenderer;
import com.gregtechceu.gtceu.client.shader.GTShaders;
import com.gregtechceu.gtceu.client.util.BloomEffectUtil;
import com.gregtechceu.gtceu.common.CommonProxy;
import com.gregtechceu.gtceu.common.data.GTBlockEntities;
import com.gregtechceu.gtceu.common.data.GTEntityTypes;
import com.gregtechceu.gtceu.common.data.GTParticleTypes;
import com.gregtechceu.gtceu.common.entity.GTBoat;
import com.gregtechceu.gtceu.integration.embeddium.GTEmbeddiumCompat;
import com.gregtechceu.gtceu.utils.input.KeyBind;

import com.lowdragmc.lowdraglib.gui.compass.CompassManager;
import com.lowdragmc.lowdraglib.gui.compass.component.RecipeComponent;

import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.ChestBoatModel;
import net.minecraft.client.renderer.blockentity.HangingSignRenderer;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * @author KilaBash
 * @date 2023/7/30
 * @implNote ClientProxy
 */
@OnlyIn(Dist.CLIENT)
public class ClientProxy extends CommonProxy {

    public static final BiMap<ResourceLocation, GTOreDefinition> CLIENT_ORE_VEINS = HashBiMap.create();
    public static final BiMap<ResourceLocation, BedrockFluidDefinition> CLIENT_FLUID_VEINS = HashBiMap.create();
    public static final BiMap<ResourceLocation, BedrockOreDefinition> CLIENT_BEDROCK_ORE_VEINS = HashBiMap.create();

    public ClientProxy() {
        super();
        init();
    }

    public static void init() {
        RecipeComponent.registerRecipeViewCreator(new GTRecipeViewCreator());
        CompassManager.INSTANCE.registerUIConfig(GTCEu.MOD_ID, new GTCompassUIConfig());
        CompassManager.INSTANCE.registerAction("multiblock", MultiblockAction::new);
        BloomEffectUtil.init();
        if (GTCEu.isEmbeddiumLoaded()) {
            MinecraftForge.EVENT_BUS.register(GTEmbeddiumCompat.class);
        }
    }

    @SubscribeEvent
    public void onRegisterEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(GTEntityTypes.DYNAMITE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(GTEntityTypes.POWDERBARREL.get(), GTExplosiveRenderer::new);
        event.registerEntityRenderer(GTEntityTypes.INDUSTRIAL_TNT.get(), GTExplosiveRenderer::new);

        event.registerBlockEntityRenderer(GTBlockEntities.GT_SIGN.get(), SignRenderer::new);
        event.registerBlockEntityRenderer(GTBlockEntities.GT_HANGING_SIGN.get(), HangingSignRenderer::new);

        event.registerEntityRenderer(GTEntityTypes.BOAT.get(), c -> new GTBoatRenderer(c, false));
        event.registerEntityRenderer(GTEntityTypes.CHEST_BOAT.get(), c -> new GTBoatRenderer(c, true));
    }

    @SubscribeEvent
    public void onRegisterEntityLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        for (var type : GTBoat.BoatType.values()) {
            event.registerLayerDefinition(GTBoatRenderer.getBoatModelName(type), BoatModel::createBodyModel);
            event.registerLayerDefinition(GTBoatRenderer.getChestBoatModelName(type),
                    ChestBoatModel::createBodyModel);
        }
    }

    @SubscribeEvent
    public void registerKeyBindings(RegisterKeyMappingsEvent event) {
        KeyBind.onRegisterKeyBinds(event);
    }

    @SubscribeEvent
    public void onRegisterGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("hud", new HudGuiOverlay());
    }

    @SubscribeEvent
    public void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(GTParticleTypes.HAZARD_PARTICLE.get(), HazardParticle.Provider::new);
    }

    @SubscribeEvent
    public void onRegisterShaders(RegisterShadersEvent event) {
        GTShaders.onRegisterShaders(event);
    }
}
