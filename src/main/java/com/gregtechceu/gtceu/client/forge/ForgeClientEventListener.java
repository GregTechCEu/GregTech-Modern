package com.gregtechceu.gtceu.client.forge;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.kind.GTRecipe;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.EnvironmentalHazardClientHandler;
import com.gregtechceu.gtceu.client.renderer.BlockHighlightRenderer;
import com.gregtechceu.gtceu.client.renderer.MultiblockInWorldPreviewRenderer;
import com.gregtechceu.gtceu.client.util.TooltipHelper;
import com.gregtechceu.gtceu.data.command.GTClientCommands;
import com.gregtechceu.gtceu.data.effect.GTMobEffects;
import com.gregtechceu.gtceu.integration.map.ClientCacheManager;

import net.minecraft.client.gui.Gui;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.event.entity.player.PlayerHeartTypeEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

@EventBusSubscriber(modid = GTCEu.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class ForgeClientEventListener {

    @SubscribeEvent
    public static void onRenderLevelStageEvent(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            // to render the preview after block entities, before the translucent. so it can be seen through the
            // transparent blocks.
            MultiblockInWorldPreviewRenderer.renderInWorldPreview(event.getPoseStack(), event.getCamera(),
                    event.getPartialTick().getGameTimeDeltaTicks());
        }
    }

    @SubscribeEvent
    public static void onBlockHighlightEvent(RenderHighlightEvent.Block event) {
        BlockHighlightRenderer.renderBlockHighlight(event.getPoseStack(), event.getCamera(), event.getTarget(),
                event.getMultiBufferSource(), event.getDeltaTracker().getGameTimeDeltaTicks());
    }

    @SubscribeEvent
    public static void onRenderPlayerHearts(PlayerHeartTypeEvent event) {
        if (event.getEntity().hasEffect(GTMobEffects.WEAK_POISON)) {
            event.setType(Gui.HeartType.POISIONED);
        }
    }

    @SubscribeEvent
    public static void onClientTickEvent(ClientTickEvent.Post event) {
        TooltipHelper.onClientTick();
        MultiblockInWorldPreviewRenderer.onClientTick();
        EnvironmentalHazardClientHandler.INSTANCE.onClientTick();
        GTValues.CLIENT_TIME++;
    }

    @SubscribeEvent
    public static void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientCacheManager.allowReinit();
    }

    @SubscribeEvent
    public static void registerClientCommand(RegisterClientCommandsEvent event) {
        GTClientCommands.register(event.getDispatcher(), event.getBuildContext());
    }

    @SubscribeEvent
    public static void serverStopped(ServerStoppedEvent event) {
        ClientCacheManager.clearCaches();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void recipesSynced(RecipesUpdatedEvent event) {
        RecipeManager manager = event.getRecipeManager();
        for (var category : GTRegistries.RECIPE_CATEGORIES) {
            GTRecipeType type = category.getRecipeType();
            for (GTRecipe recipe : type.getRecipesInCategory(category)) {
                manager.byKey(recipe.id).ifPresent(holder -> recipe.setId(holder.id()));
            }
        }
    }
}
