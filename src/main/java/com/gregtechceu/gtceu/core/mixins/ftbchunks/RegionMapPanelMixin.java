package com.gregtechceu.gtceu.core.mixins.ftbchunks;

import com.gregtechceu.gtceu.integration.map.ftbchunks.veins.fluid.FluidVeinIcon;

import dev.ftb.mods.ftbchunks.client.gui.LargeMapScreen;
import dev.ftb.mods.ftbchunks.client.gui.MapIconWidget;
import dev.ftb.mods.ftbchunks.client.gui.RegionMapPanel;
import dev.ftb.mods.ftblibrary.ui.Panel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RegionMapPanel.class, remap = false)
public abstract class RegionMapPanelMixin extends Panel {

    @Shadow
    @Final
    LargeMapScreen largeMap;

    @Shadow
    int regionMinX;

    @Shadow
    int regionMinZ;

    public RegionMapPanelMixin(Panel panel) {
        super(panel);
    }

    @Inject(method = "alignWidgets", at = @At(value = "TAIL"))
    private void gtceu$injectAlignWidgets(CallbackInfo ci) {
        for (var widget : widgets) {
            if (!(widget instanceof MapIconWidget w) || !(w.getMapIcon() instanceof FluidVeinIcon icon)) continue;

            var regionSize = largeMap.getRegionTileSize();
            var chunkSize = largeMap.getRegionTileSize() / 32;
            var chunkPos = icon.getChunkPos();

            var x = (chunkPos.getRegionX() - regionMinX) * regionSize + chunkPos.getRegionLocalX() * chunkSize;
            var y = (chunkPos.getRegionZ() - regionMinZ) * regionSize + chunkPos.getRegionLocalZ() * chunkSize;
            icon.setWidget(w);
            icon.setSize(chunkSize);
            w.setPosAndSize(x, y, chunkSize, chunkSize);
        }
    }
}
