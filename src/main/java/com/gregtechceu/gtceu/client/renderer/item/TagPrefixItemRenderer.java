package com.gregtechceu.gtceu.client.renderer.item;

import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.models.model.DelegatedModel;
import net.minecraft.resources.ResourceLocation;
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
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(model.item);
            GTDynamicResourcePack.addItemModel(itemId,
                    new DelegatedModel(model.type.getItemModelPath(model.iconSet, true)));
            // ModelTemplates.FLAT_ITEM.create(GTDynamicResourcePack.getItemModelLocation(itemId),
            // TextureMapping.layer0(itemId.withPrefix("item/")), GTDynamicResourcePack::addItemModel);
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
