package com.gregtechceu.gtceu.integration.map.xaeros.minimap.ore;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinMetadata;

import net.minecraft.client.Minecraft;

import lombok.Getter;

public class OreVeinElement {

    @Getter
    private GeneratedVeinMetadata vein;
    @Getter
    private final String name;
    @Getter
    private final int cachedNameLength;

    public OreVeinElement(GeneratedVeinMetadata vein, String name) {
        this.vein = vein;
        this.name = name;

        this.cachedNameLength = Minecraft.getInstance().font.width(this.getName());
    }

    public Material getFirstMaterial() {
        return vein.definition().veinGenerator().getAllMaterials().get(0);
    }
}
