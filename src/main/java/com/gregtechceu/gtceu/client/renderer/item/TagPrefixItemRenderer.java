package com.gregtechceu.gtceu.client.renderer.item;

import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;

import net.minecraft.client.data.models.model.DelegatedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import java.util.HashSet;
import java.util.Set;

public class TagPrefixItemRenderer {

    private static final Set<TagPrefixItemRenderer> MODELS = new HashSet<>();

    public static void create(Item item, MaterialIconType type, MaterialIconSet iconSet) {
        MODELS.add(new TagPrefixItemRenderer(item, type, iconSet));
    }

    public static void reinitModels() {
        for (TagPrefixItemRenderer model : MODELS) {
            Identifier itemId = BuiltInRegistries.ITEM.getKey(model.item);
            // 4 tints covers layer0..layer3 (max layer count in material item models).
            // GTItemColors handler returns -1 (white) for unused indexes, so over-allocating is safe.
            GTDynamicResourcePack.addTintedItemModel(itemId,
                    new DelegatedModel(model.type.getItemModelPath(model.iconSet, true)).get(),
                    4);
        }
    }

    private final Item item;
    private final MaterialIconType type;
    private final MaterialIconSet iconSet;

    private TagPrefixItemRenderer(Item item, MaterialIconType type, MaterialIconSet iconSet) {
        this.item = item;
        this.type = type;
        this.iconSet = iconSet;
    }
}
