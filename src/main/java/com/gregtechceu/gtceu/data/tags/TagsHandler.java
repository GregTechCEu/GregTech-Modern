package com.gregtechceu.gtceu.data.tags;

import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterials.Color;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import com.tterrag.registrate.providers.RegistrateTagsProvider;

import static com.gregtechceu.gtceu.api.data.chemical.material.ItemMaterialData.registerMaterialInfoItems;
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
        registerMaterialInfoItems(ingot, Clay, Items.CLAY_BALL);

        registerMaterialInfoItems(dye, Color.Black, Items.BLACK_DYE);
        registerMaterialInfoItems(dye, Color.Red, Items.RED_DYE);
        registerMaterialInfoItems(dye, Color.Green, Items.GREEN_DYE);
        registerMaterialInfoItems(dye, Color.Brown, Items.BROWN_DYE);
        registerMaterialInfoItems(dye, Color.Blue, Items.BLUE_DYE);
        registerMaterialInfoItems(dye, Color.Purple, Items.PURPLE_DYE);
        registerMaterialInfoItems(dye, Color.Cyan, Items.CYAN_DYE);
        registerMaterialInfoItems(dye, Color.LightGray, Items.LIGHT_GRAY_DYE);
        registerMaterialInfoItems(dye, Color.Gray, Items.GRAY_DYE);
        registerMaterialInfoItems(dye, Color.Pink, Items.PINK_DYE);
        registerMaterialInfoItems(dye, Color.Lime, Items.LIME_DYE);
        registerMaterialInfoItems(dye, Color.Yellow, Items.YELLOW_DYE);
        registerMaterialInfoItems(dye, Color.LightBlue, Items.LIGHT_BLUE_DYE);
        registerMaterialInfoItems(dye, Color.Magenta, Items.MAGENTA_DYE);
        registerMaterialInfoItems(dye, Color.Orange, Items.ORANGE_DYE);
        registerMaterialInfoItems(dye, Color.White, Items.WHITE_DYE);
    }
}
