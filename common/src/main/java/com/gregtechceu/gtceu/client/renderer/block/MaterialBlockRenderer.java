package com.gregtechceu.gtceu.client.renderer.block;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.models.model.*;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author KilaBash
 * @date 2023/2/27
 * @implNote MaterialBlockRenderer
 */
public class MaterialBlockRenderer {
    public static final ModelTemplate CUBE_ALL = new ModelTemplate(Optional.of(GTCEu.id("block/tinted_cube_all")), Optional.empty(), TextureSlot.ALL);
    private static final Table<MaterialIconType, MaterialIconSet, MaterialBlockRenderer> MODELS = Tables.newCustomTable(new HashMap<>(), HashMap::new);

    public static MaterialBlockRenderer getOrCreate(Block block, MaterialIconType type, MaterialIconSet iconSet) {
        if (!MODELS.contains(type, iconSet)) {
            MODELS.put(type, iconSet, new MaterialBlockRenderer(block, type, iconSet));
        }
        return MODELS.get(type, iconSet);
    }

    public static void reinitModels() {
        for (MaterialBlockRenderer model : MODELS.values()) {
            ModelTemplates.CUBE_ALL.create(model.block, TextureMapping.cube(model.type.getBlockTexturePath(model.iconSet, true)), GTDynamicResourcePack::addBlockModel);
            GTDynamicResourcePack.addItemModel(BuiltInRegistries.ITEM.getKey(model.block.asItem()), new DelegatedModel(ModelLocationUtils.getModelLocation(model.block)));
        }
    }

    private final Block block;
    private final MaterialIconType type;
    private final MaterialIconSet iconSet;

    protected MaterialBlockRenderer(Block block, MaterialIconType type, MaterialIconSet iconSet) {
        this.block = block;
        this.type = type;
        this.iconSet = iconSet;
    }
}
