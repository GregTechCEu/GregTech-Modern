package com.gregtechceu.gtceu.client.forge;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.BlockAttributes;
import com.gregtechceu.gtceu.api.cosmetics.CapeRegistry;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.kind.GTRecipe;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.EnvironmentalHazardClientHandler;
import com.gregtechceu.gtceu.client.renderer.BlockHighlightRenderer;
import com.gregtechceu.gtceu.client.renderer.MultiblockInWorldPreviewRenderer;
import com.gregtechceu.gtceu.client.renderer.cover.FacadeCoverRenderer;
import com.gregtechceu.gtceu.client.util.TooltipHelper;
import com.gregtechceu.gtceu.core.mixins.client.AbstractClientPlayerAccessor;
import com.gregtechceu.gtceu.core.mixins.client.PlayerSkinAccessor;
import com.gregtechceu.gtceu.data.command.GTClientCommands;
import com.gregtechceu.gtceu.data.effect.GTMobEffects;
import com.gregtechceu.gtceu.data.tag.CustomTags;
import com.gregtechceu.gtceu.integration.map.ClientCacheManager;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.event.entity.player.PlayerHeartTypeEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = GTCEu.MOD_ID, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class ForgeClientEventListener {

    @SubscribeEvent
    public static void onRenderLevelStageEvent(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            // to render the preview after block entities, before the translucent. so it can be seen through the
            // transparent blocks.
            MultiblockInWorldPreviewRenderer.renderInWorldPreview(event.getPoseStack(), event.getCamera(),
                    event.getPartialTick().getGameTimeDeltaPartialTick(false));
        }
    }

    private static final Map<UUID, ResourceLocation> DEFAULT_CAPES = new Object2ObjectOpenHashMap<>();

    @SubscribeEvent
    public static void onPlayerRender(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        AbstractClientPlayerAccessor clientPlayer = (AbstractClientPlayerAccessor) player;
        if (clientPlayer.gtceu$getPlayerInfo() != null) {
            PlayerSkin playerSkin = clientPlayer.gtceu$getPlayerInfo().getSkin();

            UUID uuid = player.getUUID();
            ResourceLocation defaultPlayerCape;
            if (!DEFAULT_CAPES.containsKey(uuid)) {
                defaultPlayerCape = playerSkin.capeTexture();
                DEFAULT_CAPES.put(uuid, defaultPlayerCape);
            } else {
                defaultPlayerCape = DEFAULT_CAPES.get(uuid);
            }
            ResourceLocation cape = CapeRegistry.getPlayerCapeTexture(uuid);
            ((PlayerSkinAccessor) (Object) playerSkin).gtceu$setCapeTexture(cape == null ? defaultPlayerCape : cape);
        }
    }

    @SubscribeEvent
    public static void updateFOV(ComputeFovModifierEvent event) {
        Player player = event.getPlayer();

        AttributeInstance moveSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (moveSpeed == null || moveSpeed.getModifier(BlockAttributes.BLOCK_SPEED_BOOST) == null) return;

        float multi = 1;
        var state = player.level().getBlockState(player.getOnPos());

        if (state.is(CustomTags.VERY_FAST_WALKABLE_BLOCKS)) multi /= 1.2F;

        multi = (float) Mth.lerp(Minecraft.getInstance().options.fovEffectScale().get(), 1.0F, multi);
        event.setNewFovModifier(event.getNewFovModifier() * multi);
    }

    private static double getValueWithoutWalkingBoost(AttributeInstance attrib) {
        double base = attrib.getBaseValue();
        Map<AttributeModifier.Operation, List<AttributeModifier>> mods = attrib.getModifiers().stream()
                .collect(Collectors.groupingBy(t -> t.operation()));

        for (AttributeModifier mod : mods.get(AttributeModifier.Operation.ADD_VALUE)) {
            base += mod.amount();
        }

        double applied = base;
        for (AttributeModifier mod : mods.get(AttributeModifier.Operation.ADD_MULTIPLIED_BASE)) {
            if (mod.id() == BlockAttributes.BLOCK_SPEED_BOOST) continue;
            applied += base * mod.amount();
        }

        for (AttributeModifier mod : mods.get(AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)) {
            applied *= 1 + mod.amount();
        }

        return attrib.getAttribute().value().sanitizeValue(applied);
    }

    @SubscribeEvent
    public static void onBlockHighlightEvent(RenderHighlightEvent.Block event) {
        BlockHighlightRenderer.renderBlockHighlight(event.getPoseStack(), event.getCamera(), event.getTarget(),
                event.getMultiBufferSource(), event.getDeltaTracker().getGameTimeDeltaPartialTick(false));
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

    private static final String BLOCK_INFO_LINE_START = ChatFormatting.UNDERLINE + "Targeted Block: ";

    @SubscribeEvent
    public static void onDebugTextEvent(CustomizeGuiOverlayEvent.DebugText event) {
        Minecraft mc = Minecraft.getInstance();
        // don't render machine state information if F3 screen isn't up or reduced debug info is enabled
        if (!mc.getDebugOverlay().showDebugScreen() || mc.showOnlyReducedInfo()) return;
        Entity cameraEntity = mc.getCameraEntity();
        if (cameraEntity == null || mc.level == null) return;

        BlockHitResult hit = ToolHelper.entityPickBlock(cameraEntity, 20.0, 0, false);
        if (hit.getType() == HitResult.Type.MISS) return;
        BlockPos hitPos = hit.getBlockPos();
        BlockEntity blockEntity = mc.level.getBlockEntity(hitPos);
        // only try to find the correct location if we have a valid machine
        if (!(blockEntity instanceof IMachineBlockEntity machineBE)) return;

        final List<String> rightLines = event.getRight();
        int lineCount = rightLines.size();

        // look for the empty line after the "Targeted Block" section
        // and default to the end if there isn't anything after it
        int targetedBlockLine = -1;
        int afterBlockSection = lineCount - 1;
        for (int i = 0; i < lineCount; i++) {
            String line = rightLines.get(i);
            // this is formatted like this so we don't need to check targetedBlockLine == -1 twice
            if (targetedBlockLine == -1) {
                if (line.startsWith(BLOCK_INFO_LINE_START)) {
                    targetedBlockLine = i;
                }
            } else {
                if (line.isBlank()) {
                    afterBlockSection = i;
                    // we can break here because targetedBlockLine must be not -1 for this branch to be reached
                    break;
                }
            }
        }
        if (targetedBlockLine == -1) {
            // couldn't find the start of the targeted block info, exit
            return;
        }

        // actually add the text lines
        MutableInt index = new MutableInt(afterBlockSection);

        rightLines.add(index.getAndIncrement(), "");
        machineBE.getMetaMachine().addDebugOverlayText(line -> rightLines.add(index.getAndIncrement(), line));
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
