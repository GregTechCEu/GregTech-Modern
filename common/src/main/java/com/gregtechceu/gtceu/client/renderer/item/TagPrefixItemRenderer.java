package com.gregtechceu.gtceu.client.renderer.item;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtlib.client.renderer.impl.IModelRenderer;

import java.util.HashMap;

/**
 * @author KilaBash
 * @date 2023/2/16
 * @implNote TagPrefixItemRenderer
 */
public class TagPrefixItemRenderer extends IModelRenderer {
    public static final Table<MaterialIconType, MaterialIconSet, TagPrefixItemRenderer> MODELS = Tables.newCustomTable(new HashMap<>(), HashMap::new);

    public TagPrefixItemRenderer(MaterialIconType type, MaterialIconSet iconSet) {
        super(type.getItemModelPath(iconSet));
    }

    public static TagPrefixItemRenderer getOrCreate(MaterialIconType type, MaterialIconSet iconSet) {
        if (!MODELS.contains(type, iconSet)) {
            MODELS.put(type, iconSet, new TagPrefixItemRenderer(type, iconSet));
        }
        return MODELS.get(type, iconSet);
    }
}
