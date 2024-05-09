package com.gregtechceu.gtceu.api.blocks;

import com.gregtechceu.gtceu.api.materials.material.Material;
import com.gregtechceu.gtceu.api.tags.TagPrefix;
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
