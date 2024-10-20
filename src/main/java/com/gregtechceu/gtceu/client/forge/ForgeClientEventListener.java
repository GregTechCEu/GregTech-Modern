package com.gregtechceu.gtceu.client.forge;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.client.EnvironmentalHazardClientHandler;
import com.gregtechceu.gtceu.client.TooltipsHandler;
import com.gregtechceu.gtceu.client.renderer.BlockHighLightRenderer;
import com.gregtechceu.gtceu.client.renderer.MultiblockInWorldPreviewRenderer;
import com.gregtechceu.gtceu.client.shader.GTShaders;
import com.gregtechceu.gtceu.client.util.BloomEffectUtil;
import com.gregtechceu.gtceu.client.util.TooltipHelper;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.mojang.blaze3d.vertex.PoseStack;

/**
 * @author KilaBash
 * @date 2022/8/27
 * @implNote ForgeCommonEventListener
 */
@Mod.EventBusSubscriber(modid = GTCEu.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class ForgeClientEventListener {

    @SubscribeEvent
    public static void onRenderLevelStageEvent(RenderLevelStageEvent event) {
        Camera camera = event.getCamera();
        PoseStack poseStack = event.getPoseStack();
        float partialTick = event.getPartialTick();
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            // to render the preview after block entities, before the translucent. so it can be seen through the
            // transparent blocks.
            MultiblockInWorldPreviewRenderer.renderInWorldPreview(poseStack, camera, partialTick);
        } else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            BloomEffectUtil.renderBloom(camera.getPosition().x, camera.getPosition().y, camera.getPosition().z,
                    poseStack, event.getProjectionMatrix(), event.getFrustum(), partialTick, camera.getEntity());
        }
    }

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START && Minecraft.getInstance().level != null) {
            if (GTShaders.allowedShader()) {
                GTShaders.BLOOM_TARGET.clear(Minecraft.ON_OSX);
                Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
            }
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        BloomEffectUtil.invalidateLevelTickets(event.getLevel());
    }

    @SubscribeEvent
    public static void onBlockHighlightEvent(RenderHighlightEvent.Block event) {
        BlockHighLightRenderer.renderBlockHighLight(event.getPoseStack(), event.getCamera(), event.getTarget(),
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
}
