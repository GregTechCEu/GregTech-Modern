package com.gregtechceu.gtceu.data.tags;

import com.gregtechceu.gtceu.common.data.GTItems;
import com.tterrag.registrate.providers.RegistrateTagsProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import  com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterials.Color;
import net.minecraft.world.level.material.Fluid;

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
        FluidTagLoader.addPlatformSpecificFluidTags(provider);
    }

    public static void initExtraUnificationEntries() {
        registerUnificationItems(ingot, Clay, Items.CLAY_BALL);

        registerUnificationItems(lens, Color.White, GTItems.MATERIAL_ITEMS.get(lens, Glass).get(), GTItems.MATERIAL_ITEMS.get(lens, NetherStar).get());
        registerUnificationItems(lens, Color.LightBlue, GTItems.MATERIAL_ITEMS.get(lens, Diamond).get());
        registerUnificationItems(lens, Color.Red, GTItems.MATERIAL_ITEMS.get(lens, Ruby).get());
        registerUnificationItems(lens, Color.Green, GTItems.MATERIAL_ITEMS.get(lens, Emerald).get());
        registerUnificationItems(lens, Color.Blue, GTItems.MATERIAL_ITEMS.get(lens, Sapphire).get());
        
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
