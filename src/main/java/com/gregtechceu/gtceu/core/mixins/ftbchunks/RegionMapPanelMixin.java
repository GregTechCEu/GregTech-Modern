package com.gregtechceu.gtceu.core.mixins.ftbchunks;

import com.gregtechceu.gtceu.integration.map.ftbchunks.FTBChunksOptions;
import com.gregtechceu.gtceu.integration.map.ftbchunks.FTBChunksRenderer;
import com.gregtechceu.gtceu.integration.map.ftbchunks.veins.fluid.FluidChunkWidget;

import dev.ftb.mods.ftbchunks.client.gui.LargeMapScreen;
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

    @Inject(method = "addWidgets",
            at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbchunks/client/gui/RegionMapPanel;alignWidgets()V"))
    private void gtceu$injectAddWidgets(CallbackInfo ci) {
        if (FTBChunksOptions.showLayer("bedrock_fluids")) {
            FTBChunksRenderer.fluidElements.row(largeMap.currentDimension()).forEach((pos, info) -> {
                var widget = new FluidChunkWidget((RegionMapPanel) (Object) this, pos, info);
                add(widget);
            });
        }
    }

    @Inject(method = "alignWidgets",
            at = @At(value = "INVOKE",
                     target = "Ldev/ftb/mods/ftblibrary/ui/Widget;setPosAndSize(IIII)Ldev/ftb/mods/ftblibrary/ui/Widget;"))
    private void gtceu$injectAlignWidgets(CallbackInfo ci) {
        for (var widget : widgets) {
            if (!(widget instanceof FluidChunkWidget w)) continue;

            int regionSize = largeMap.getRegionTileSize();
            int chunkSize = largeMap.getRegionTileSize() / 32;
            var chunkPos = w.getChunkPos();

            var x = (chunkPos.getRegionX() - regionMinX) * regionSize + chunkPos.getRegionLocalX() * chunkSize;
            var y = (chunkPos.getRegionZ() - regionMinZ) * regionSize + chunkPos.getRegionLocalZ() * chunkSize;
            w.setPosAndSize(x, y, chunkSize, chunkSize);
        }
    }
}
