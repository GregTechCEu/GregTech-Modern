package com.gregtechceu.gtceu.client.model.runtimegen;

import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.models.model.DelegatedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.HashSet;
import java.util.Set;

public class TagPrefixItemModelGenerator {

    private static final Set<TagPrefixItemModelGenerator> MODELS = new HashSet<>();

    public static void reinitModels() {
        for (TagPrefixItemModelGenerator model : MODELS) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(model.item);
            GTDynamicResourcePack.addItemModel(itemId,
                    new DelegatedModel(model.type.getItemModelPath(model.iconSet, true)));
        }
    }

    private final Item item;
    private final MaterialIconType type;
    private final MaterialIconSet iconSet;

    private TagPrefixItemModelGenerator(Item item, MaterialIconType type, MaterialIconSet iconSet) {
        this.item = item;
        this.type = type;
        this.iconSet = iconSet;
    }

    public static void add(Item item, MaterialIconType type, MaterialIconSet iconSet) {
        MODELS.add(new TagPrefixItemModelGenerator(item, type, iconSet));
    }
}
