package com.gregtechceu.gtceu.data.tags;

import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterials.Color;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import com.tterrag.registrate.providers.RegistrateTagsProvider;

import static com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper.registerUnificationItems;
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
        registerUnificationItems(ingot, Clay, Items.CLAY_BALL);

        registerUnificationItems(dye, Color.Black, Items.BLACK_DYE);
        registerUnificationItems(dye, Color.Red, Items.RED_DYE);
        registerUnificationItems(dye, Color.Green, Items.GREEN_DYE);
        registerUnificationItems(dye, Color.Brown, Items.BROWN_DYE);
        registerUnificationItems(dye, Color.Blue, Items.BLUE_DYE);
        registerUnificationItems(dye, Color.Purple, Items.PURPLE_DYE);
        registerUnificationItems(dye, Color.Cyan, Items.CYAN_DYE);
        registerUnificationItems(dye, Color.LightGray, Items.LIGHT_GRAY_DYE);
        registerUnificationItems(dye, Color.Gray, Items.GRAY_DYE);
        registerUnificationItems(dye, Color.Pink, Items.PINK_DYE);
        registerUnificationItems(dye, Color.Lime, Items.LIME_DYE);
        registerUnificationItems(dye, Color.Yellow, Items.YELLOW_DYE);
        registerUnificationItems(dye, Color.LightBlue, Items.LIGHT_BLUE_DYE);
        registerUnificationItems(dye, Color.Magenta, Items.MAGENTA_DYE);
        registerUnificationItems(dye, Color.Orange, Items.ORANGE_DYE);
        registerUnificationItems(dye, Color.White, Items.WHITE_DYE);
    }
}
