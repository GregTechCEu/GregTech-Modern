package com.gregtechceu.gtceu.client.renderer.item;

import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.models.model.DelegatedModel;
import net.minecraft.world.item.Item;

import java.util.HashSet;
import java.util.Set;

public class ToolItemRenderer {

    private static final Set<ToolItemRenderer> MODELS = new HashSet<>();

    public static void reinitModels() {
        for (ToolItemRenderer model : MODELS) {
            GTDynamicResourcePack.addItemModel(BuiltInRegistries.ITEM.getKey(model.item),
                    new DelegatedModel(model.toolType.modelLocation));
        }
    }

    private final Item item;
    private final GTToolType toolType;

    protected ToolItemRenderer(Item item, GTToolType toolType) {
        this.item = item;
        this.toolType = toolType;
    }

    public static void create(Item item, GTToolType toolType) {
        MODELS.add(new ToolItemRenderer(item, toolType));
    }
}
