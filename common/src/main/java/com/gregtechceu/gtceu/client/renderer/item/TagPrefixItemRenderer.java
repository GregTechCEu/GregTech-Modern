package com.gregtechceu.gtceu.client.renderer.item;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.models.model.DelegatedModel;
import net.minecraft.world.item.Item;

import java.util.HashMap;

/**
 * @author KilaBash
 * @date 2023/2/16
 * @implNote TagPrefixItemRenderer
 */
public class TagPrefixItemRenderer {
    private static final Table<MaterialIconType, MaterialIconSet, TagPrefixItemRenderer> MODELS = Tables.newCustomTable(new HashMap<>(), HashMap::new);

    public static void reinitModels() {
        for (TagPrefixItemRenderer model : MODELS.values()) {
            GTDynamicResourcePack.addItemModel(BuiltInRegistries.ITEM.getKey(model.item), new DelegatedModel(model.type.getItemModelPath(model.iconSet, true)));
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

    public static TagPrefixItemRenderer getOrCreate(Item item, MaterialIconType type, MaterialIconSet iconSet) {
        if (!MODELS.contains(type, iconSet)) {
            MODELS.put(type, iconSet, new TagPrefixItemRenderer(item, type, iconSet));
        }
        return MODELS.get(type, iconSet);
    }
}
