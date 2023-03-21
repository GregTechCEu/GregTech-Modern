package com.gregtechceu.gtceu.data.recipe;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterials;
import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterials.Color;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

public class TagLoader {

    public static final String OREDICT_FUEL_COKE = "fuelCoke";
    public static final String OREDICT_BLOCK_FUEL_COKE = "blockFuelCoke";
    
    public static void init() {
        //OreDictionary.registerUnificationEntry(OREDICT_FUEL_COKE, ChemicalHelper.get(TagPrefix.gem, GTMaterials.Coke));
        //OreDictionary.registerUnificationEntry(OREDICT_BLOCK_FUEL_COKE, ChemicalHelper.get(TagPrefix.block, GTMaterials.Coke));
        //OreDictionary.registerUnificationEntry("crystalCertusQuartz", ChemicalHelper.get(TagPrefix.gem, GTMaterials.CertusQuartz));

        ChemicalHelper.registerUnificationEntry(Blocks.CLAY.asItem(), TagPrefix.block, GTMaterials.Clay);
        ChemicalHelper.registerUnificationEntry(Blocks.BRICKS.asItem(), TagPrefix.block, GTMaterials.Brick);
        ChemicalHelper.registerUnificationEntry(Items.CLAY_BALL, TagPrefix.ingot, GTMaterials.Clay);
        ChemicalHelper.registerUnificationEntry(Items.FLINT, TagPrefix.gem, GTMaterials.Flint);

        // TODO Expand for all terracottas, move to Info Loader
        //ChemicalHelper.registerUnificationEntry(new ItemStack(Blocks.HARDENED_CLAY, 1, W), new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, M * 4)));
        //ChemicalHelper.registerUnificationEntry(new ItemStack(Blocks.STAINED_HARDENED_CLAY, 1, W), new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, M * 4)));

        // TODO add to planks mc tag?
        //for (Material material : new Material[]{GTMaterials.Wood, GTMaterials.TreatedWood}) {
        //    for (ItemLike woodPlateStack : ChemicalHelper.getItems(new UnificationEntry(TagPrefix.plate, material))) {
        //        ChemicalHelper.registerUnificationEntry(woodPlateStack, TagPrefix.plank, material);
        //    }
        //}

        // todo add to dye colors?
        //for (Material material : new Material[]{GTMaterials.Lapis, GTMaterials.Lazurite, GTMaterials.Sodalite}) {
        //    ChemicalHelper.registerUnificationEntry(ChemicalHelper.get(TagPrefix.gem, material).getItem(), TagPrefix.dye, Color.Blue);
        //    ChemicalHelper.registerUnificationEntry(ChemicalHelper.get(TagPrefix.dust, material).getItem(), TagPrefix.dye, Color.Blue);
        //}
        //ChemicalHelper.registerUnificationEntry(ChemicalHelper.get(TagPrefix.dust, GTMaterials.MetalMixture).getItem(), TagPrefix.dye, Color.Brown);

        ChemicalHelper.registerUnificationEntry(Blocks.COAL_ORE.asItem(), TagPrefix.ore, GTMaterials.Coal);
        ChemicalHelper.registerUnificationEntry(Blocks.IRON_ORE, TagPrefix.ore, GTMaterials.Iron);
        ChemicalHelper.registerUnificationEntry(Blocks.LAPIS_ORE, TagPrefix.ore, GTMaterials.Lapis);
        ChemicalHelper.registerUnificationEntry(Blocks.REDSTONE_ORE, TagPrefix.ore, GTMaterials.Redstone);
        ChemicalHelper.registerUnificationEntry(Blocks.GOLD_ORE, TagPrefix.ore, GTMaterials.Gold);
        ChemicalHelper.registerUnificationEntry(Blocks.DIAMOND_ORE, TagPrefix.ore, GTMaterials.Diamond);
        ChemicalHelper.registerUnificationEntry(Blocks.EMERALD_ORE, TagPrefix.ore, GTMaterials.Emerald);
        ChemicalHelper.registerUnificationEntry(Blocks.NETHER_QUARTZ_ORE, TagPrefix.ore, GTMaterials.NetherQuartz);
        ChemicalHelper.registerUnificationEntry(Items.LAPIS_LAZULI, TagPrefix.gem, GTMaterials.Lapis);
        ChemicalHelper.registerUnificationEntry(Items.ENDER_EYE, TagPrefix.gem, GTMaterials.EnderEye);
        ChemicalHelper.registerUnificationEntry(Items.ENDER_PEARL, TagPrefix.gem, GTMaterials.EnderPearl);
        ChemicalHelper.registerUnificationEntry(Items.DIAMOND, TagPrefix.gem, GTMaterials.Diamond);
        ChemicalHelper.registerUnificationEntry(Items.EMERALD, TagPrefix.gem, GTMaterials.Emerald);
        ChemicalHelper.registerUnificationEntry(Items.COAL, TagPrefix.gem, GTMaterials.Coal);
        ChemicalHelper.registerUnificationEntry(Items.CHARCOAL, TagPrefix.gem, GTMaterials.Charcoal);
        ChemicalHelper.registerUnificationEntry(Items.QUARTZ, TagPrefix.gem, GTMaterials.NetherQuartz);
        ChemicalHelper.registerUnificationEntry(Items.NETHER_STAR, TagPrefix.gem, GTMaterials.NetherStar);
        ChemicalHelper.registerUnificationEntry(Items.GOLD_NUGGET, TagPrefix.nugget, GTMaterials.Gold);
        ChemicalHelper.registerUnificationEntry(Items.GOLD_INGOT, TagPrefix.ingot, GTMaterials.Gold);
        ChemicalHelper.registerUnificationEntry(Items.IRON_INGOT, TagPrefix.ingot, GTMaterials.Iron);
        ChemicalHelper.registerUnificationEntry(Items.PAPER, TagPrefix.plate, GTMaterials.Paper);
        ChemicalHelper.registerUnificationEntry(Items.SUGAR, TagPrefix.dust, GTMaterials.Sugar);
        ChemicalHelper.registerUnificationEntry(Items.STICK, TagPrefix.stick, GTMaterials.Wood);
        ChemicalHelper.registerUnificationEntry(Items.REDSTONE, TagPrefix.dust, GTMaterials.Redstone);
        ChemicalHelper.registerUnificationEntry(Items.GUNPOWDER, TagPrefix.dust, GTMaterials.Gunpowder);
        ChemicalHelper.registerUnificationEntry(Items.GLOWSTONE_DUST, TagPrefix.dust, GTMaterials.Glowstone);
        ChemicalHelper.registerUnificationEntry(Blocks.GLOWSTONE, TagPrefix.block, GTMaterials.Glowstone);
        ChemicalHelper.registerUnificationEntry(Items.BONE_MEAL, TagPrefix.dust, GTMaterials.Bone);
        ChemicalHelper.registerUnificationEntry(Items.BONE, TagPrefix.stick, GTMaterials.Bone);
        ChemicalHelper.registerUnificationEntry(Items.BLAZE_POWDER, TagPrefix.dust, GTMaterials.Blaze);
        ChemicalHelper.registerUnificationEntry(Items.BLAZE_ROD, TagPrefix.stick, GTMaterials.Blaze);
        ChemicalHelper.registerUnificationEntry(Blocks.IRON_BLOCK, TagPrefix.block, GTMaterials.Iron);
        ChemicalHelper.registerUnificationEntry(Blocks.GOLD_BLOCK, TagPrefix.block, GTMaterials.Gold);
        ChemicalHelper.registerUnificationEntry(Blocks.DIAMOND_BLOCK, TagPrefix.block, GTMaterials.Diamond);
        ChemicalHelper.registerUnificationEntry(Blocks.EMERALD_BLOCK, TagPrefix.block, GTMaterials.Emerald);
        ChemicalHelper.registerUnificationEntry(Blocks.LAPIS_BLOCK, TagPrefix.block, GTMaterials.Lapis);
        ChemicalHelper.registerUnificationEntry(Blocks.COAL_BLOCK, TagPrefix.block, GTMaterials.Coal);
        ChemicalHelper.registerUnificationEntry(Blocks.REDSTONE_BLOCK, TagPrefix.block, GTMaterials.Redstone);
        ChemicalHelper.registerUnificationEntry(Blocks.QUARTZ_BLOCK, TagPrefix.block, GTMaterials.NetherQuartz);
        ChemicalHelper.registerUnificationEntry(Blocks.BONE_BLOCK, TagPrefix.block, GTMaterials.Bone);
        ChemicalHelper.registerUnificationEntry(Blocks.ICE, TagPrefix.block, GTMaterials.Ice);
        ChemicalHelper.registerUnificationEntry(Blocks.OBSIDIAN, TagPrefix.block, GTMaterials.Obsidian);
        ChemicalHelper.registerUnificationEntry(Blocks.GLASS, TagPrefix.block, GTMaterials.Glass);

        ChemicalHelper.registerUnificationEntry(Blocks.GRANITE, TagPrefix.stone, GTMaterials.Granite);
        ChemicalHelper.registerUnificationEntry(Blocks.POLISHED_GRANITE, TagPrefix.stone, GTMaterials.Granite);
        ChemicalHelper.registerUnificationEntry(Blocks.ANDESITE, TagPrefix.stone, GTMaterials.Andesite);
        ChemicalHelper.registerUnificationEntry(Blocks.POLISHED_ANDESITE, TagPrefix.stone, GTMaterials.Andesite);
        ChemicalHelper.registerUnificationEntry(Blocks.DIORITE, TagPrefix.stone, GTMaterials.Diorite);
        ChemicalHelper.registerUnificationEntry(Blocks.POLISHED_DIORITE, TagPrefix.stone, GTMaterials.Diorite);

        // TODO GT stone types
        //ChemicalHelper.registerUnificationEntry(MetaBlocks.STONE_SMOOTH.getItemVariant(BlockStoneSmooth.BlockType.BLACK_GRANITE, 1), TagPrefix.stone, GTMaterials.GraniteBlack);
        //ChemicalHelper.registerUnificationEntry(MetaBlocks.STONE_SMOOTH.getItemVariant(BlockStoneSmooth.BlockType.RED_GRANITE, 1), TagPrefix.stone, GTMaterials.GraniteRed);
        //ChemicalHelper.registerUnificationEntry(MetaBlocks.STONE_SMOOTH.getItemVariant(BlockStoneSmooth.BlockType.MARBLE, 1), TagPrefix.stone, GTMaterials.Marble);
        //ChemicalHelper.registerUnificationEntry(MetaBlocks.STONE_SMOOTH.getItemVariant(BlockStoneSmooth.BlockType.BASALT, 1), TagPrefix.stone, GTMaterials.Basalt);
        //ChemicalHelper.registerUnificationEntry(MetaBlocks.STONE_SMOOTH.getItemVariant(BlockStoneSmooth.BlockType.CONCRETE_LIGHT, 1), TagPrefix.block, GTMaterials.Concrete);
        //ChemicalHelper.registerUnificationEntry(MetaBlocks.STONE_SMOOTH.getItemVariant(BlockStoneSmooth.BlockType.CONCRETE_DARK, 1), TagPrefix.block, GTMaterials.Concrete);

        // TODO?
        /*
        ChemicalHelper.registerUnificationEntry(new ItemStack(Blocks.ANVIL), "craftingAnvil");
        ChemicalHelper.registerUnificationEntry(new ItemStack(Blocks.OBSIDIAN, 1, W), TagPrefix.stone, GTMaterials.Obsidian);
        ChemicalHelper.registerUnificationEntry(new ItemStack(Blocks.MOSSY_COBBLESTONE, 1, W), "stoneMossy");
        ChemicalHelper.registerUnificationEntry(new ItemStack(Blocks.MOSSY_COBBLESTONE, 1, W), "stoneCobble");
        ChemicalHelper.registerUnificationEntry(new ItemStack(Blocks.COBBLESTONE, 1, W), "stoneCobble");
        ChemicalHelper.registerUnificationEntry(new ItemStack(Blocks.STONE), "stoneSmooth");
        ChemicalHelper.registerUnificationEntry(new ItemStack(Blocks.STONE_BRICKS), "stoneBricks");
        ChemicalHelper.registerUnificationEntry(new ItemStack(Blocks.MOSSY_STONE_BRICKS), "stoneMossy");
        ChemicalHelper.registerUnificationEntry(new ItemStack(Blocks.CRACKED_STONE_BRICKS), "stoneCracked");
        ChemicalHelper.registerUnificationEntry(new ItemStack(Blocks.CHISELED_STONE_BRICKS), "stoneChiseled");
        ChemicalHelper.registerUnificationEntry(new ItemStack(Blocks.NETHERRACK), TagPrefix.stone, GTMaterials.Netherrack);
        ChemicalHelper.registerUnificationEntry(new ItemStack(Blocks.END_STONE), TagPrefix.stone, GTMaterials.Endstone);

        ChemicalHelper.registerUnificationEntry(new ItemStack(Blocks.REDSTONE_TORCH), "craftingRedstoneTorch");

        ChemicalHelper.registerUnificationEntry(new ItemStack(Blocks.PISTON, 1, W), "craftingPiston");
        ChemicalHelper.registerUnificationEntry(new ItemStack(Blocks.STICKY_PISTON, 1, W), "craftingPiston");

        ChemicalHelper.registerUnificationEntry(new ItemStack(Blocks.CHEST, 1, W), "chestWood");
        ChemicalHelper.registerUnificationEntry(new ItemStack(Blocks.TRAPPED_CHEST, 1, W), "chestWood");

        ChemicalHelper.registerUnificationEntry(new ItemStack(Blocks.FURNACE, 1, W), "craftingFurnace");

        ChemicalHelper.registerUnificationEntry(new ItemStack(Items.FEATHER, 1, W), "craftingFeather");

        ChemicalHelper.registerUnificationEntry(new ItemStack(Items.WHEAT, 1, W), "itemWheat");
        ChemicalHelper.registerUnificationEntry(new ItemStack(Items.PAPER, 1, W), "paperEmpty");
        ChemicalHelper.registerUnificationEntry(new ItemStack(Items.MAP, 1, W), "paperMap");
        ChemicalHelper.registerUnificationEntry(new ItemStack(Items.FILLED_MAP, 1, W), "paperMap");
        ChemicalHelper.registerUnificationEntry(new ItemStack(Items.BOOK, 1, W), "bookEmpty");
        ChemicalHelper.registerUnificationEntry(new ItemStack(Items.WRITABLE_BOOK, 1, W), "bookWritable");
        ChemicalHelper.registerUnificationEntry(new ItemStack(Items.WRITTEN_BOOK, 1, W), "bookWritten");
        ChemicalHelper.registerUnificationEntry(new ItemStack(Items.ENCHANTED_BOOK, 1, W), "bookEnchanted");
        ChemicalHelper.registerUnificationEntry(new ItemStack(Items.BOOK, 1, W), "craftingBook");
        ChemicalHelper.registerUnificationEntry(new ItemStack(Items.WRITABLE_BOOK, 1, W), "craftingBook");
        ChemicalHelper.registerUnificationEntry(new ItemStack(Items.WRITTEN_BOOK, 1, W), "craftingBook");
        ChemicalHelper.registerUnificationEntry(new ItemStack(Items.ENCHANTED_BOOK, 1, W), "craftingBook");
         */

        // "crafting" prefix tags
        ChemicalHelper.registerUnificationEntry(Blocks.CHEST, TagPrefix.crafting, MarkerMaterials.Misc.Chest);
        ChemicalHelper.registerUnificationEntry(Blocks.TRAPPED_CHEST, TagPrefix.crafting, MarkerMaterials.Misc.Chest);
        ChemicalHelper.registerUnificationEntry(Blocks.PISTON, TagPrefix.crafting, MarkerMaterials.Misc.Piston);
        ChemicalHelper.registerUnificationEntry(Blocks.STICKY_PISTON, TagPrefix.crafting, MarkerMaterials.Misc.Piston);
    }
}
