package com.gregtechceu.gtceu.integration.map.xaeros;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinMetadata;
import com.gregtechceu.gtceu.integration.map.WaypointManager;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

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

    public void onMouseSelect() {
        Material firstMaterial = vein.definition().veinGenerator().getAllMaterials().get(0);
        int color = firstMaterial.getMaterialARGB();
        ResourceLocation texture = MaterialIconType.ore.getBlockTexturePath(firstMaterial.getMaterialIconSet(), true);

        // TODO unhardcode
        BlockPos center = vein.center();
        WaypointManager.toggleWaypoint("ore_veins", name, color,
                null, center.getX(), center.getY(), center.getZ(),
                texture);
    }

    public void toggleDepleted() {
        vein = vein.setDepleted(!vein.depleted());
    }

    public Material getFirstMaterial() {
        return vein.definition().veinGenerator().getAllMaterials().get(0);
    }
}
