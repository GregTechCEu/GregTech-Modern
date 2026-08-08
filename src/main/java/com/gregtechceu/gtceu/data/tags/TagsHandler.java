package com.gregtechceu.gtceu.data.tags;

import net.minecraft.world.item.Items;

import static com.gregtechceu.gtceu.api.data.chemical.material.ItemMaterialData.registerMaterialEntry;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

public class TagsHandler {

    public static void initExtraUnificationEntries() {
        registerMaterialEntry(Items.CLAY_BALL, ingot, Clay);
    }
}
