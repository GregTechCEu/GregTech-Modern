package com.lowdragmc.gtceu.client.renderer.item;

import com.lowdragmc.gtceu.api.item.tool.GTToolType;
import com.lowdragmc.lowdraglib.client.renderer.impl.IModelRenderer;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author KilaBash
 * @date 2023/2/16
 * @implNote TagPrefixItemRenderer
 */
public class ToolItemRenderer extends IModelRenderer {
    private static final Map<GTToolType, ToolItemRenderer> MODELS = new EnumMap<>(GTToolType.class);

    protected ToolItemRenderer(GTToolType toolType) {
        super(toolType.modelLocation);
    }

    public static ToolItemRenderer getOrCreate(GTToolType toolType) {
        if (!MODELS.containsKey(toolType)) {
            MODELS.put(toolType, new ToolItemRenderer(toolType));
        }
        return MODELS.get(toolType);
    }

}
