package com.gregtechceu.gtceu.api.block;

import com.gregtechceu.gtceu.api.chemical.material.Material;
import com.gregtechceu.gtceu.api.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.gregtechceu.gtceu.client.renderer.block.OreBlockRenderer;
import com.lowdragmc.lowdraglib.Platform;

public class OreBlock extends MaterialBlock {
    public OreBlock(Properties properties, TagPrefix tagPrefix, Material material, boolean registerModel) {
        super(properties, tagPrefix, material, false);
        if (registerModel && Platform.isClient()) {
            OreBlockRenderer.create(this);
        }
    }
}
