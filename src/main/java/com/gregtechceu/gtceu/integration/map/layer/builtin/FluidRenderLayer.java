package com.gregtechceu.gtceu.integration.map.layer.builtin;

import com.gregtechceu.gtceu.api.item.component.prospector.ProspectorMode;
import com.gregtechceu.gtceu.integration.map.GenericMapRenderer;
import com.gregtechceu.gtceu.integration.map.layer.MapRenderLayer;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.List;

public class FluidRenderLayer extends MapRenderLayer {

    public FluidRenderLayer(String key, GenericMapRenderer renderer) {
        super(key, renderer);
    }

    public static String getId(ProspectorMode.FluidInfo vein, ChunkPos pos) {
        return "bedrock_fluids@[" + pos.x + "," + pos.z + "]";
    }

    public static Component getName(ProspectorMode.FluidInfo entry) {
        return entry.asStack().getDisplayName();
    }

    public static List<Component> getTooltip(Component name, ProspectorMode.FluidInfo entry) {
        final List<Component> tooltip = new ArrayList<>();

        MutableComponent title = name.copy();
        if (entry.left() <= 0) {
            title.append(" (").append(Component.translatable("gtceu.minimap.ore_vein.depleted")).append(")");
        } else {
            title.append(" --- %s (%s%%)".formatted(entry.yield(), entry.left()));
        }
        tooltip.add(title);

        return tooltip;
    }
}
