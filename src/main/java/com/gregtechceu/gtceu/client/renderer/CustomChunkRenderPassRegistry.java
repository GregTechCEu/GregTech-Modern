package com.gregtechceu.gtceu.client.renderer;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.bloom.BloomRenderer;
import com.gregtechceu.gtceu.core.mixins.client.bloom.LevelRendererAccessor;

import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import org.jetbrains.annotations.Nullable;

import java.util.List;

@EventBusSubscriber(modid = GTCEu.MOD_ID, value = Dist.CLIENT)
public final class CustomChunkRenderPassRegistry {

    // Pass order here is also the order used by vanilla, Sodium, and Embeddium chunk buffers.
    private static final List<CustomChunkRenderPass> PASSES = List.of(
            new CustomChunkRenderPass(GTRenderTypes.bloom(), CustomChunkRenderPass.AlphaCutoff.ZERO, true,
                    CustomChunkRenderPass.DrawStage.MANUAL, CustomChunkRenderPass.TerrainPhase.CUSTOM,
                    BloomRenderer::usesCustomChunkPass));

    // Chunk buffer layers are fixed during client startup, so load conditions are resolved once.
    private static final List<CustomChunkRenderPass> ACTIVE_PASSES = PASSES.stream()
            .filter(pass -> pass.loadCondition().getAsBoolean())
            .toList();
    private static final List<CustomChunkRenderPass> AFTER_CUTOUT_PASSES = ACTIVE_PASSES.stream()
            .filter(pass -> pass.drawStage() == CustomChunkRenderPass.DrawStage.AFTER_CUTOUT)
            .toList();

    public static List<CustomChunkRenderPass> activePasses() {
        return ACTIVE_PASSES;
    }

    public static List<CustomChunkRenderPass> afterCutoutPasses() {
        return AFTER_CUTOUT_PASSES;
    }

    public static @Nullable CustomChunkRenderPass getPass(RenderType renderType) {
        for (CustomChunkRenderPass pass : ACTIVE_PASSES) {
            if (pass.renderType() == renderType) {
                return pass;
            }
        }
        return null;
    }

    @SubscribeEvent
    public static void renderAutomaticPasses(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) return;

        var cameraPosition = event.getCamera().getPosition();
        for (CustomChunkRenderPass pass : AFTER_CUTOUT_PASSES) {
            ((LevelRendererAccessor) event.getLevelRenderer()).invokeRenderSectionLayer(pass.renderType(),
                    cameraPosition.x, cameraPosition.y, cameraPosition.z,
                    event.getModelViewMatrix(), event.getProjectionMatrix());
        }
    }

    private CustomChunkRenderPassRegistry() {}
}
