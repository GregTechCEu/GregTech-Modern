package com.gregtechceu.gtceu.data.tags;

import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterials.Color;

import net.minecraft.world.item.Items;

import static com.gregtechceu.gtceu.api.data.chemical.material.ItemMaterialData.registerItemMaterialEntry;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

public class TagsHandler {

    public static void initExtraUnificationEntries() {
        registerItemMaterialEntry(Items.CLAY_BALL, ingot, Clay);

        registerItemMaterialEntry(Items.BLACK_DYE, dye, Color.Black);
        registerItemMaterialEntry(Items.RED_DYE, dye, Color.Red);
        registerItemMaterialEntry(Items.GREEN_DYE, dye, Color.Green);
        registerItemMaterialEntry(Items.BROWN_DYE, dye, Color.Brown);
        registerItemMaterialEntry(Items.BLUE_DYE, dye, Color.Blue);
        registerItemMaterialEntry(Items.PURPLE_DYE, dye, Color.Purple);
        registerItemMaterialEntry(Items.CYAN_DYE, dye, Color.Cyan);
        registerItemMaterialEntry(Items.LIGHT_GRAY_DYE, dye, Color.LightGray);
        registerItemMaterialEntry(Items.GRAY_DYE, dye, Color.Gray);
        registerItemMaterialEntry(Items.PINK_DYE, dye, Color.Pink);
        registerItemMaterialEntry(Items.LIME_DYE, dye, Color.Lime);
        registerItemMaterialEntry(Items.YELLOW_DYE, dye, Color.Yellow);
        registerItemMaterialEntry(Items.LIGHT_BLUE_DYE, dye, Color.LightBlue);
        registerItemMaterialEntry(Items.MAGENTA_DYE, dye, Color.Magenta);
        registerItemMaterialEntry(Items.ORANGE_DYE, dye, Color.Orange);
        registerItemMaterialEntry(Items.WHITE_DYE, dye, Color.White);
    }
}
