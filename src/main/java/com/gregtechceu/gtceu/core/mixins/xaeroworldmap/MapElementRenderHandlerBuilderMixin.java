package com.gregtechceu.gtceu.core.mixins.xaeroworldmap;

import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.map.xaeros.worldmap.ore.OreVeinElementRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xaero.map.element.MapElementRenderHandler;
import xaero.map.element.MapElementRenderer;

import java.util.List;

@Mixin(value = MapElementRenderHandler.Builder.class, remap = false)
public class MapElementRenderHandlerBuilderMixin {

    @ModifyVariable(method = "build", at = @At(value = "LOAD", ordinal = 3))
    private List<MapElementRenderer<?, ?, ?>> gtceu$addOreRenderer(List<MapElementRenderer<?, ?, ?>> value) {
        if (ConfigHolder.INSTANCE.compat.minimap.toggle.xaerosMapIntegration) {
            value.add(OreVeinElementRenderer.Builder.begin().build());
        }
        return value;
    }
}
