package com.gregtechceu.gtceu.core.mixins.xaerominimap;

import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.map.xaeros.minimap.ore.OreVeinElementRenderer;

import net.minecraft.client.Minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.common.IXaeroMinimap;
import xaero.common.minimap.MinimapProcessor;
import xaero.common.minimap.render.MinimapFBORenderer;
import xaero.common.minimap.render.MinimapRenderer;
import xaero.hud.minimap.Minimap;
import xaero.hud.minimap.compass.render.CompassRenderer;
import xaero.hud.minimap.element.render.map.MinimapElementMapRendererHandler;
import xaero.hud.minimap.waypoint.render.WaypointsGuiRenderer;

// TODO move to xaeros api once that exists
@Mixin(value = MinimapFBORenderer.class, remap = false)
public abstract class MinimapFBORendererMixin extends MinimapRenderer {

    @Shadow
    private MinimapElementMapRendererHandler minimapElementMapRendererHandler;

    @Unique
    private OreVeinElementRenderer gtceu$oreVeinElementRenderer;

    public MinimapFBORendererMixin(IXaeroMinimap modMain, Minecraft mc, WaypointsGuiRenderer waypointsGuiRenderer,
                                   Minimap minimap, CompassRenderer compassRenderer) {
        super(modMain, mc, waypointsGuiRenderer, minimap, compassRenderer);
    }

    @Inject(method = "loadFrameBuffer",
            at = @At(value = "INVOKE", target = "Lxaero/common/mods/SupportMods;worldmap()Z"))
    private void gtceu$injectProspectionMarkers(MinimapProcessor minimapProcessor, CallbackInfo ci) {
        if (!ConfigHolder.INSTANCE.compat.minimap.toggle.xaerosMapIntegration) return;
        this.gtceu$oreVeinElementRenderer = OreVeinElementRenderer.Builder.begin().build();
        minimapElementMapRendererHandler.add(this.gtceu$oreVeinElementRenderer);
        this.minimap.getOverMapRendererHandler().add(this.gtceu$oreVeinElementRenderer);
    }
}
