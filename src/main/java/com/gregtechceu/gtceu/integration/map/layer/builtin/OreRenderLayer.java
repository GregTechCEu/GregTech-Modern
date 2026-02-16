package com.gregtechceu.gtceu.integration.map.layer.builtin;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinMetadata;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.map.GenericMapRenderer;
import com.gregtechceu.gtceu.integration.map.layer.MapRenderLayer;
import com.gregtechceu.gtceu.integration.xei.widgets.GTOreVeinWidget;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class OreRenderLayer extends MapRenderLayer {

    public OreRenderLayer(String key, GenericMapRenderer renderer) {
        super(key, renderer);
    }

    public static String getId(GeneratedVeinMetadata vein) {
        BlockPos center = vein.center();
        return "ore_veins@[" + center.getX() + "," + center.getY() + "," + center.getZ() + "]";
    }

    public static MutableComponent getName(GeneratedVeinMetadata vein) {
        // noinspection ConstantValue IDK, it crashed
        if (vein == null || vein.definition() == null || vein.definition().unwrapKey().isEmpty()) {
            return Component.translatable("gtceu.minimap.ore_vein.depleted");
        }
        return Component.translatable(GTOreVeinWidget.getOreName(vein.definition()));
    }

    public static @NotNull Material getMaterial(@NotNull GeneratedVeinMetadata vein) {
        Material firstMaterial = null;
        GTOreDefinition definition = vein.definition().value();
        if (!definition.indicatorGenerators().isEmpty()) {
            var blockOrMaterial = definition.indicatorGenerators().getFirst().block();
            firstMaterial = blockOrMaterial == null ? null : blockOrMaterial.map(
                    state -> {
                        var matStack = ChemicalHelper.getMaterialStack(state.getBlock());
                        return matStack.isEmpty() ? GTMaterials.NULL : matStack.material();
                    },
                    Function.identity());
        }
        if (firstMaterial == null) {
            firstMaterial = definition.veinGenerator().getAllMaterials().getFirst();
        }
        return firstMaterial;
    }

    public static List<Component> getTooltip(String name, GeneratedVeinMetadata vein) {
        final List<Component> tooltip = new ArrayList<>();
        var title = Component.literal(name);
        if (vein.depleted()) {
            title.append(" (").append(Component.translatable("gtceu.minimap.ore_vein.depleted")).append(")");
        }
        tooltip.add(title);

        for (var filler : vein.definition().value().veinGenerator().getAllEntries()) {
            filler.vein().ifLeft(state -> {
                tooltip.add(Component.literal(ConfigHolder.INSTANCE.compat.minimap.oreNamePrefix)
                        .append(state.getBlock().getName()));
            }).ifRight(material -> {
                tooltip.add(Component.literal(ConfigHolder.INSTANCE.compat.minimap.oreNamePrefix)
                        .append(TagPrefix.ore.getLocalizedName(material)));
            });
        }
        return tooltip;
    }
}
