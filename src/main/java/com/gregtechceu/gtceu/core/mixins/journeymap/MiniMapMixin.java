package com.gregtechceu.gtceu.core.mixins.journeymap;

import com.gregtechceu.gtceu.integration.map.GenericMapRenderer;

import com.gregtechceu.gtceu.integration.map.journeymap.JourneymapRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;

import com.mojang.blaze3d.vertex.PoseStack;
import journeymap.client.model.MapState;
import journeymap.client.properties.MiniMapProperties;
import journeymap.client.render.map.GridRenderer;
import journeymap.client.ui.minimap.DisplayVars;
import journeymap.client.ui.minimap.MiniMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MiniMap.class, remap = false)
public abstract class MiniMapMixin {

    @Shadow
    private DisplayVars dv;
    @Shadow
    private MiniMapProperties miniMapProperties;
    @Shadow
    @Final
    private static MapState state;
    @Shadow
    @Final
    private static GridRenderer gridRenderer;

    @Shadow
    @Final
    private Minecraft mc;

    @Unique
    private GenericMapRenderer gtceu$renderer;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void gtceu$injectInit(MiniMapProperties miniMapProperties, CallbackInfo ci) {
        gtceu$renderer = new JourneymapRenderer();
    }

    @Inject(method = "drawOnMapEntities", at = @At("HEAD"))
    private void gtceu$injectDrawMinimap(GuiGraphics graphics, MultiBufferSource buffers, double rotation,
                                         CallbackInfo ci) {
        float scale = (float) Math.pow(2, miniMapProperties.zoomLevel.get());
        double rw = dv.minimapWidth / scale;
        double rh = dv.minimapHeight / scale;
        gtceu$renderer.updateVisibleArea(state.getDimension(), (int) (gridRenderer.getCenterBlockX() - rw / 2),
                (int) (gridRenderer.getCenterBlockZ() - rh / 2), (int) rw, (int) rh);

        PoseStack poseStack = graphics.pose();

        poseStack.pushPose();
        poseStack.translate(mc.getWindow().getScreenWidth() / 2.0, mc.getWindow().getScreenHeight() / 2.0, 0);
        poseStack.scale(scale, scale, 1);
        poseStack.translate(-gridRenderer.getCenterBlockX(), -gridRenderer.getCenterBlockZ(), 0);

        gtceu$renderer.render(graphics, gridRenderer.getCenterBlockX(), gridRenderer.getCenterBlockZ(), scale);

        poseStack.popPose();
    }
}
