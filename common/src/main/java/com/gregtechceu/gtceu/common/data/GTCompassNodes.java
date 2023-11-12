package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.registry.registrate.CompassNode;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import net.minecraft.world.item.Items;

/**
 * @author KilaBash
 * @date 2023/7/31
 * @implNote GTCompassNodes
 */
public class GTCompassNodes {
    public final static CompassNode COVER = CompassNode.getOrCreate(GTCompassSections.COVERS, "cover")
            .icon(() -> new ItemStackTexture(GTItems.ITEM_FILTER.asStack()))
            .position(50, 100)
            .size(40)
            .lang("What is Cover?");

    public final static CompassNode ORE = CompassNode.getOrCreate(GTCompassSections.GENERATIONS, "ore")
            .icon(() -> new ItemStackTexture(Items.DIAMOND_PICKAXE))
            .position(50, 100)
            .size(40)
            .lang("How to find Ore?");

    public final static CompassNode STEAM = CompassNode.getOrCreate(GTCompassSections.STEAM, "steam_age")
            .icon(() -> new ItemStackTexture(GTItems.MATERIAL_ITEMS.get(TagPrefix.plate, GTMaterials.Bronze).asStack()))
            .position(50, 50)
            .size(40)
            .lang("Steam Age");

    public static void init() {

    }
}
