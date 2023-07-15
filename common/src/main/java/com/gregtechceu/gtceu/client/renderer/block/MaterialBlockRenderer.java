package com.gregtechceu.gtceu.client.renderer.block;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * @author KilaBash
 * @date 2023/2/27
 * @implNote MaterialBlockRenderer
 */
public class MaterialBlockRenderer extends TextureOverrideRenderer {
    private static final Table<MaterialIconType, MaterialIconSet, MaterialBlockRenderer> MODELS = Tables.newCustomTable(new HashMap<>(), HashMap::new);

    public static MaterialBlockRenderer getOrCreate(MaterialIconType type, MaterialIconSet iconSet) {
        if (!MODELS.contains(type, iconSet)) {
            MODELS.put(type, iconSet, new MaterialBlockRenderer(type, iconSet));
        }
        return MODELS.get(type, iconSet);
    }

    protected MaterialBlockRenderer(MaterialIconType type, MaterialIconSet iconSet) {
        super(GTCEu.id("block/tinted_cube_all"), () -> Map.of("all", type.getBlockTexturePath(iconSet, true)));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean useAO() {
        return true;
    }
}
