package com.gregtechceu.gtceu.integration.map.xaeros.common.ore;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinMetadata;
import com.gregtechceu.gtceu.integration.map.WaypointManager;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import lombok.Getter;

public class OreVeinElement {

    @Getter
    private final GeneratedVeinMetadata vein;
    @Getter
    private final Component name;
    @Getter
    private final int cachedNameLength;

    public OreVeinElement(GeneratedVeinMetadata vein, Component name) {
        this.vein = vein;
        this.name = name;
        this.cachedNameLength = Minecraft.getInstance().font.width(this.getName());
    }

    public void onMouseSelect() {
        int color = getFirstMaterial().getMaterialARGB();
        // TODO generalize to all possible layer types
        BlockPos center = this.vein.center();
        WaypointManager.toggleWaypoint("ore_veins", this.name.getString(), color, null, center);
    }

    public void toggleDepleted() {
        this.vein.depleted(!this.vein.depleted());
    }

    public Material getFirstMaterial() {
        return vein.definition().value().veinGenerator().getAllMaterials().getFirst();
    }
}
