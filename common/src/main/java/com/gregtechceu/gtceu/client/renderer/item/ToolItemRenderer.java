package com.gregtechceu.gtceu.client.renderer.item;

import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;
import com.lowdragmc.lowdraglib.client.renderer.impl.IModelRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.models.model.DelegatedModel;
import net.minecraft.world.item.Item;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author KilaBash
 * @date 2023/2/16
 * @implNote TagPrefixItemRenderer
 */
public class ToolItemRenderer extends IModelRenderer {
    private static final Map<GTToolType, ToolItemRenderer> MODELS = new EnumMap<>(GTToolType.class);

    public static void reinitModels() {
        for (ToolItemRenderer model : MODELS.values()) {
            GTDynamicResourcePack.addItemModel(BuiltInRegistries.ITEM.getKey(model.item), new DelegatedModel(model.toolType.modelLocation));
        }
    }

    private final Item item;
    private final GTToolType toolType;

    protected ToolItemRenderer(Item item, GTToolType toolType) {
        super(toolType.modelLocation);
        this.item = item;
        this.toolType = toolType;
    }

    public static ToolItemRenderer getOrCreate(Item item, GTToolType toolType) {
        if (!MODELS.containsKey(toolType)) {
            MODELS.put(toolType, new ToolItemRenderer(item, toolType));
        }
        return MODELS.get(toolType);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean isGui3d() {
        return false;
    }
}
