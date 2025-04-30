package com.gregtechceu.gtceu.data.tags;

import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterials.Color;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import com.tterrag.registrate.providers.RegistrateTagsProvider;

import static com.gregtechceu.gtceu.api.data.chemical.material.ItemMaterialData.registerMaterialEntry;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

/**
 * @author KilaBash
 * @date 2023/2/28
 * @implNote ItemTagsHandler
 */
public class TagsHandler {

    public static void initItem(RegistrateTagsProvider<Item> provider) {
        ItemTagLoader.init(provider);
    }

    public static void initBlock(RegistrateTagsProvider<Block> provider) {
        BlockTagLoader.init(provider);
    }

    public static void initFluid(RegistrateTagsProvider<Fluid> provider) {
        FluidTagLoader.init(provider);
    }

    public static void initEntity(RegistrateTagsProvider<EntityType<?>> provider) {
        EntityTypeTagLoader.init(provider);
    }

    public static void initExtraUnificationEntries() {
        registerMaterialEntry(Items.CLAY_BALL, ingot, Clay);

        registerMaterialEntry(Items.BLACK_DYE, dye, Color.Black);
        registerMaterialEntry(Items.RED_DYE, dye, Color.Red);
        registerMaterialEntry(Items.GREEN_DYE, dye, Color.Green);
        registerMaterialEntry(Items.BROWN_DYE, dye, Color.Brown);
        registerMaterialEntry(Items.BLUE_DYE, dye, Color.Blue);
        registerMaterialEntry(Items.PURPLE_DYE, dye, Color.Purple);
        registerMaterialEntry(Items.CYAN_DYE, dye, Color.Cyan);
        registerMaterialEntry(Items.LIGHT_GRAY_DYE, dye, Color.LightGray);
        registerMaterialEntry(Items.GRAY_DYE, dye, Color.Gray);
        registerMaterialEntry(Items.PINK_DYE, dye, Color.Pink);
        registerMaterialEntry(Items.LIME_DYE, dye, Color.Lime);
        registerMaterialEntry(Items.YELLOW_DYE, dye, Color.Yellow);
        registerMaterialEntry(Items.LIGHT_BLUE_DYE, dye, Color.LightBlue);
        registerMaterialEntry(Items.MAGENTA_DYE, dye, Color.Magenta);
        registerMaterialEntry(Items.ORANGE_DYE, dye, Color.Orange);
        registerMaterialEntry(Items.WHITE_DYE, dye, Color.White);
    }
}
