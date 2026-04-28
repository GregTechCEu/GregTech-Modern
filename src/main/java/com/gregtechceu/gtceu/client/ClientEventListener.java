package com.gregtechceu.gtceu.client;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.cosmetics.CapeRegistry;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.renderer.BlockHighlightRenderer;
import com.gregtechceu.gtceu.client.renderer.MultiblockInWorldPreviewRenderer;
import com.gregtechceu.gtceu.client.renderer.cover.FacadeCoverRenderer;
import com.gregtechceu.gtceu.client.util.TooltipHelper;
import com.gregtechceu.gtceu.common.commands.GTClientCommands;
import com.gregtechceu.gtceu.common.data.GTAttributeModifierIds;
import com.gregtechceu.gtceu.common.data.GTMobEffects;
import com.gregtechceu.gtceu.core.mixins.client.AbstractClientPlayerAccessor;
import com.gregtechceu.gtceu.core.mixins.client.PlayerSkinAccessor;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.integration.map.ClientCacheManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.core.ClientAsset;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.event.PlayerHeartTypeEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = GTCEu.MOD_ID, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class ClientEventListener {

    @SubscribeEvent
    public static void onRenderLevelStageEvent(RenderLevelStageEvent.AfterOpaqueBlocks event) {
        // to render the preview after opaque blocks, before the translucent pass.
        Minecraft minecraft = Minecraft.getInstance();
        MultiblockInWorldPreviewRenderer.renderInWorldPreview(event.getPoseStack(),
                minecraft.gameRenderer.getMainCamera(),
                minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false));
    }

    private static final Map<UUID, ClientAsset.Texture> DEFAULT_CAPES = new Object2ObjectOpenHashMap<>();

    @SubscribeEvent
    public static void onPlayerRender(RenderPlayerEvent.Pre<?> event) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        AbstractClientPlayerAccessor clientPlayer = (AbstractClientPlayerAccessor) player;
        if (clientPlayer.gtceu$getPlayerInfo() != null) {
            var playerSkin = event.getRenderState().skin;

            UUID uuid = player.getUUID();
            ClientAsset.Texture defaultPlayerCape;
            if (!DEFAULT_CAPES.containsKey(uuid)) {
                defaultPlayerCape = playerSkin.cape();
                DEFAULT_CAPES.put(uuid, defaultPlayerCape);
            } else {
                defaultPlayerCape = DEFAULT_CAPES.get(uuid);
            }
            Identifier cape = CapeRegistry.getPlayerCapeTexture(uuid);
            ((PlayerSkinAccessor) (Object) playerSkin)
                    .gtceu$setCape(cape == null ? defaultPlayerCape : new ClientAsset.ResourceTexture(cape));
        }
    }

    @SubscribeEvent
    public static void updateFOV(ComputeFovModifierEvent event) {
        Player player = event.getPlayer();

        AttributeInstance speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute == null || !speedAttribute.hasModifier(GTAttributeModifierIds.BLOCK_SPEED_BOOST)) {
            return;
        }

        float multiplier = 1.0f;
        BlockState state = player.level().getBlockState(player.getOnPos());

        // inverse of the math done with the speed attribute in AbstractClientPlayer
        if (state.is(CustomTags.VERY_FAST_WALKABLE_BLOCKS)) {
            // base speed is 0.1, boost is 0.1*0.6 -> boosted speed = 0.16
            // the FOV modifier is `1 + (speed / base speed + 1) / 2`, so `1 + (0.16 / 0.1 + 1) / 2 = 1.3`
            // thus, divide by 1.3 to get back to original FOV before the 'fast block boost' modifier
            multiplier /= 1.3f;
        } else if (state.is(CustomTags.FAST_WALKABLE_BLOCKS)) {
            // same as above but the speed boost is 0.25
            multiplier /= 1.125f;
        }

        multiplier = (float) Mth.lerp(Minecraft.getInstance().options.fovEffectScale().get(), 1.0, multiplier);
        event.setNewFovModifier(event.getNewFovModifier() * multiplier);
    }

    private static double getValueWithoutWalkingBoost(AttributeInstance attribute) {
        double base = attribute.getBaseValue();
        Map<AttributeModifier.Operation, List<AttributeModifier>> modifiers = attribute.getModifiers().stream()
                .collect(Collectors.groupingBy(AttributeModifier::operation));

        for (AttributeModifier mod : modifiers.get(AttributeModifier.Operation.ADD_VALUE)) {
            base += mod.amount();
        }

        double applied = base;
        for (AttributeModifier mod : modifiers.get(AttributeModifier.Operation.ADD_MULTIPLIED_BASE)) {
            if (mod.id() == GTAttributeModifierIds.BLOCK_SPEED_BOOST) continue;
            applied += base * mod.amount();
        }

        for (AttributeModifier mod : modifiers.get(AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)) {
            applied *= 1 + mod.amount();
        }

        return attribute.getAttribute().value().sanitizeValue(applied);
    }

    @SubscribeEvent
    public static void onBlockHighlightEvent(ExtractBlockOutlineRenderStateEvent event) {
        event.addCustomRenderer((outline, buffer, poseStack, highContrast, levelRenderState) -> {
            BlockHighlightRenderer.renderBlockHighlight(poseStack, event.getCamera(), event.getHitResult(),
                    buffer, Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false));
            return false;
        });
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
    public static void onLevelUnloadEvent(LevelEvent.Unload event) {
        FacadeCoverRenderer.clearItemModelCache();
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
    public static void recipesSynced(RecipesReceivedEvent event) {
        RecipeMap manager = event.getRecipeMap();
        for (var category : GTRegistries.RECIPE_CATEGORIES) {
            GTRecipeType type = category.getRecipeType();
            for (GTRecipe recipe : type.getRecipesInCategory(category)) {
                var holder = manager.byKey(ResourceKey.create(Registries.RECIPE, recipe.id));
                if (holder != null) {
                    recipe.setId(holder.id().identifier());
                }
            }
        }
    }
}
