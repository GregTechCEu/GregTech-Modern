package com.gregtechceu.gtceu.integration.map.layer.builtin;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinMetadata;
import com.gregtechceu.gtceu.client.ClientProxy;
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
        if (vein == null || vein.definition() == null ||
                ClientProxy.CLIENT_ORE_VEINS.inverse().get(vein.definition()) == null) {
            return Component.translatable("gtceu.minimap.ore_vein.depleted");
        }
        return Component.translatable("gtceu.jei.ore_vein." + GTOreVeinWidget.getOreName(vein.definition()));
    }

    public static @NotNull Material getMaterial(@NotNull GeneratedVeinMetadata vein) {
        Material firstMaterial = null;
        if (!vein.definition().indicatorGenerators().isEmpty()) {
            var blockOrMaterial = vein.definition().indicatorGenerators().get(0).block();
            firstMaterial = blockOrMaterial == null ? null : blockOrMaterial.map(
                    state -> {
                        var matStack = ChemicalHelper.getMaterialStack(state.getBlock());
                        return matStack.isEmpty() ? GTMaterials.NULL : matStack.material();
                    },
                    Function.identity());
        }
        if (firstMaterial == null) {
            firstMaterial = vein.definition().veinGenerator().getAllMaterials().get(0);
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

        for (var filler : vein.definition().veinGenerator().getAllEntries()) {
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
