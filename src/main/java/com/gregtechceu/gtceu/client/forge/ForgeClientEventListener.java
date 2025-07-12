package com.gregtechceu.gtceu.client.forge;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.cosmetics.CapeRegistry;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.client.EnvironmentalHazardClientHandler;
import com.gregtechceu.gtceu.client.TooltipsHandler;
import com.gregtechceu.gtceu.client.renderer.BlockHighlightRenderer;
import com.gregtechceu.gtceu.client.renderer.MultiblockInWorldPreviewRenderer;
import com.gregtechceu.gtceu.client.renderer.cover.FacadeCoverRenderer;
import com.gregtechceu.gtceu.client.util.TooltipHelper;
import com.gregtechceu.gtceu.common.commands.GTClientCommands;
import com.gregtechceu.gtceu.core.mixins.client.AbstractClientPlayerAccessor;
import com.gregtechceu.gtceu.core.mixins.client.PlayerInfoAccessor;
import com.gregtechceu.gtceu.integration.map.ClientCacheManager;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = GTCEu.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class ForgeClientEventListener {

    @SubscribeEvent
    public static void onRenderLevelStageEvent(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            // to render the preview after block entities, before the translucent. so it can be seen through the
            // transparent blocks.
            MultiblockInWorldPreviewRenderer.renderInWorldPreview(event.getPoseStack(), event.getCamera(),
                    event.getPartialTick());
        }
    }

    private static final Map<UUID, ResourceLocation> DEFAULT_CAPES = new Object2ObjectOpenHashMap<>();

    @SubscribeEvent
    public static void onPlayerRender(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        AbstractClientPlayerAccessor clientPlayer = (AbstractClientPlayerAccessor) player;
        if (clientPlayer.gtceu$getPlayerInfo() != null) {
            PlayerInfoAccessor playerInfo = ((PlayerInfoAccessor) clientPlayer.gtceu$getPlayerInfo());
            Map<MinecraftProfileTexture.Type, ResourceLocation> playerTextures = playerInfo.getTextureLocations();

            UUID uuid = player.getUUID();
            ResourceLocation defaultPlayerCape;
            if (!DEFAULT_CAPES.containsKey(uuid)) {
                defaultPlayerCape = playerTextures.get(MinecraftProfileTexture.Type.CAPE);
                DEFAULT_CAPES.put(uuid, defaultPlayerCape);
            } else {
                defaultPlayerCape = DEFAULT_CAPES.get(uuid);
            }

            ResourceLocation cape = CapeRegistry.getPlayerCapeTexture(uuid);
            playerTextures.put(MinecraftProfileTexture.Type.CAPE, cape == null ? defaultPlayerCape : cape);
        }
    }

    @SubscribeEvent
    public static void onBlockHighlightEvent(RenderHighlightEvent.Block event) {
        BlockHighlightRenderer.renderBlockHighlight(event.getPoseStack(), event.getCamera(), event.getTarget(),
                event.getMultiBufferSource(), event.getPartialTick());
    }

    @SubscribeEvent
    public static void onTooltipEvent(ItemTooltipEvent event) {
        TooltipsHandler.appendTooltips(event.getItemStack(), event.getFlags(), event.getToolTip());
    }

    @SubscribeEvent
    public static void onClientTickEvent(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            TooltipHelper.onClientTick();
            MultiblockInWorldPreviewRenderer.onClientTick();
            EnvironmentalHazardClientHandler.INSTANCE.onClientTick();
            GTValues.CLIENT_TIME++;
        }
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
        if (!mc.options.renderDebug || mc.showOnlyReducedInfo()) return;
        Entity cameraEntity = mc.getCameraEntity();
        if (cameraEntity == null || mc.level == null) return;

        BlockHitResult hit = ToolHelper.entityPickBlock(cameraEntity, ForgeGui.rayTraceDistance, 0, false);
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
}
