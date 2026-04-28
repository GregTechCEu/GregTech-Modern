package com.gregtechceu.gtceu.core.mixins.ftbchunks;

import com.gregtechceu.gtceu.integration.map.ftbchunks.veins.fluid.FluidVeinIcon;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

import com.llamalad7.mixinextras.sugar.Local;
import dev.ftb.mods.ftbchunks.api.client.icon.MapIcon;
import dev.ftb.mods.ftbchunks.client.FTBChunksClient;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FTBChunksClient.class, remap = false)
public class FTBChunksClientMixin {

    @Unique
    private boolean gtceu$iconCheck;
    @Unique
    private double gtceu$d;
    @Unique
    private float gtceu$minimapRotation;

    @Inject(method = "renderHud",
            at = @At(value = "INVOKE",
                     target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V",
                     shift = At.Shift.AFTER,
                     remap = true),
            slice = @Slice(from = @At(value = "INVOKE",
                                      target = "Ldev/ftb/mods/ftbchunks/api/client/icon/MapIcon;getPos(F)Lnet/minecraft/world/phys/Vec3;"),
                           to = @At(value = "INVOKE",
                                    target = "Ldev/ftb/mods/ftbchunks/api/client/icon/MapIcon;draw(Ldev/ftb/mods/ftbchunks/api/client/icon/MapType;Lnet/minecraft/client/gui/GuiGraphics;IIIIZI)V")))
    private void gtceu$injectRenderHud(GuiGraphics graphics, DeltaTracker tickDelta, CallbackInfo ci,
                                       @Local MapIcon icon) {
        if (gtceu$iconCheck) {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthFunc(GL11.GL_GEQUAL);
            var poseStack = graphics.pose();
            poseStack.rotateAbout((float) Math.toRadians(gtceu$minimapRotation + 180f), 0.5f, 0.5f);
            poseStack.scale(1.143f, 1.143f);
        }
    }

    @Inject(method = "renderHud",
            at = @At(value = "INVOKE",
                     target = "Ldev/ftb/mods/ftbchunks/api/client/icon/MapIcon;isVisible(Ldev/ftb/mods/ftbchunks/api/client/icon/MapType;DZ)Z",
                     shift = At.Shift.AFTER),
            remap = false)
    private void gtceu$saveLocals(GuiGraphics graphics, DeltaTracker tickDelta, CallbackInfo ci,
                                  @Local(name = "icon") MapIcon icon,
                                  @Local(ordinal = 6, name = "minimapRotation") float minimapRotation,
                                  @Local(ordinal = 1, name = "d") double d) {
        gtceu$iconCheck = icon instanceof FluidVeinIcon;
        gtceu$d = d;
        gtceu$minimapRotation = minimapRotation;
    }

    @ModifyVariable(method = "renderHud",
                    name = "d",
                    at = @At(value = "STORE"),
                    slice = @Slice(from = @At(value = "INVOKE",
                                              target = "Ldev/ftb/mods/ftbchunks/api/client/icon/MapIcon;isVisible(Ldev/ftb/mods/ftbchunks/api/client/icon/MapType;DZ)Z"),
                                   to = @At(value = "INVOKE",
                                            target = "Ldev/ftb/mods/ftbchunks/api/client/icon/MapIcon;getIconScale(Ldev/ftb/mods/ftbchunks/api/client/icon/MapType;)D")),
                    remap = false)
    private double gtceu$valueLoad(double d) {
        if (gtceu$iconCheck) {
            return gtceu$d;
        }
        return d;
    }

    @Inject(method = "renderHud",
            at = @At(value = "INVOKE",
                     target = "Ldev/ftb/mods/ftbchunks/api/client/icon/MapIcon;draw(Ldev/ftb/mods/ftbchunks/api/client/icon/MapType;Lnet/minecraft/client/gui/GuiGraphics;IIIIZI)V",
                     shift = At.Shift.AFTER),
            remap = false)
    private void gtceu$injectRenderHudPost(GuiGraphics graphics, DeltaTracker tickDelta, CallbackInfo ci,
                                           @Local MapIcon icon) {
        if (gtceu$iconCheck) {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDepthFunc(GL11.GL_LEQUAL);
        }
    }
}
