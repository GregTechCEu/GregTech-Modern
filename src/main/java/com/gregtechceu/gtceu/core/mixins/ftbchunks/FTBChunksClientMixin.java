package com.gregtechceu.gtceu.core.mixins.ftbchunks;

import com.gregtechceu.gtceu.integration.map.ftbchunks.veins.fluid.FluidVeinIcon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.math.Axis;
import dev.ftb.mods.ftbchunks.api.client.icon.MapIcon;
import dev.ftb.mods.ftbchunks.client.FTBChunksClient;
import dev.ftb.mods.ftbchunks.client.FTBChunksClientConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FTBChunksClient.class)
public class FTBChunksClientMixin {

    @Inject(method = "renderHud",
            at = @At(value = "INVOKE",
                     target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V",
                     shift = At.Shift.AFTER),
            slice = @Slice(from = @At(value = "INVOKE",
                                      target = "Ldev/ftb/mods/ftbchunks/api/client/icon/MapIcon;getPos(F)Lnet/minecraft/world/phys/Vec3;"),
                           to = @At(value = "INVOKE",
                                    target = "Ldev/ftb/mods/ftbchunks/api/client/icon/MapIcon;draw(Ldev/ftb/mods/ftbchunks/api/client/icon/MapType;Lnet/minecraft/client/gui/GuiGraphics;IIIIZI)V")))
    private void gtceu$injectRenderHud(GuiGraphics graphics, float tickDelta, CallbackInfo ci, @Local MapIcon icon) {
        if (icon instanceof FluidVeinIcon) {
            var mc = Minecraft.getInstance();
            var rotationLocked = FTBChunksClientConfig.MINIMAP_LOCKED_NORTH.get() ||
                    FTBChunksClientConfig.SQUARE_MINIMAP.get();
            var poseStack = graphics.pose();
            var minimapRotation = (rotationLocked ? 180F : -mc.player.getYRot()) % 360F;
            poseStack.translate(0.5, 0.5, 0);
            poseStack.mulPose(Axis.ZP.rotationDegrees(minimapRotation + 180f));
            poseStack.translate(-0.5, -0.5, 0);
        }
    }
}
