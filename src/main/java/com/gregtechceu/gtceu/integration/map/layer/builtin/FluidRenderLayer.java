package com.gregtechceu.gtceu.integration.map.layer.builtin;

import com.gregtechceu.gtceu.api.gui.misc.ProspectorMode;
import com.gregtechceu.gtceu.integration.map.GenericMapRenderer;
import com.gregtechceu.gtceu.integration.map.layer.MapRenderLayer;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.ChunkPos;

import java.util.Collections;
import java.util.List;

public class FluidRenderLayer extends MapRenderLayer {

    public FluidRenderLayer(String key, GenericMapRenderer renderer) {
        super(key, renderer);
    }

    public static String getId(ProspectorMode.FluidInfo vein, ChunkPos pos) {
        return "bedrock_fluids@[" + pos.x + "," + pos.z + "]";
    }

    public static Component getName(ProspectorMode.FluidInfo entry) {
        FluidStack fluidStack = FluidStack.create(entry.fluid(), entry.left());
        return fluidStack.getDisplayName();
    }

    public static List<Component> getTooltip(ProspectorMode.FluidInfo entry) {
        FluidStack fluidStack = FluidStack.create(entry.fluid(), entry.left());
        return Collections.singletonList(((MutableComponent) fluidStack.getDisplayName())
                .append(" --- %s (%s%%)".formatted(entry.yield(), entry.left())));
    }
}
