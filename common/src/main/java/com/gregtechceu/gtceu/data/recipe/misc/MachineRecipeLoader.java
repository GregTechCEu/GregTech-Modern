package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.integration.ae2.GTAEMachines;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTItems.*;
import static com.gregtechceu.gtceu.common.data.GTMachines.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public class MachineRecipeLoader {

    private MachineRecipeLoader() {
    }

    public static void init(Consumer<FinishedRecipe> provider) {
        ComputerRecipes.init(provider);

        registerDecompositionRecipes(provider);
        registerBlastFurnaceRecipes(provider);
        registerAssemblerRecipes(provider);
        registerAlloyRecipes(provider);
        registerBendingCompressingRecipes(provider);
        registerCokeOvenRecipes(provider);
        registerFluidRecipes(provider);
        registerMixingCrystallizationRecipes(provider);
        registerPrimitiveBlastFurnaceRecipes(provider);
        registerRecyclingRecipes(provider);
        registerStoneBricksRecipes(provider);
        registerNBTRemoval(provider);
        registerHatchConversion(provider);
    }

    private static void registerBendingCompressingRecipes(Consumer<FinishedRecipe> provider) {

        COMPRESSOR_RECIPES.recipeBuilder("compressed_fireclay")
                .inputItems(dust, Fireclay)
                .outputItems(COMPRESSED_FIRECLAY)
                .duration(80).EUt(4)
                .save(provider);

        FORMING_PRESS_RECIPES.recipeBuilder("credit_cupronickel")
                .duration(100).EUt(16)
                .notConsumable(SHAPE_MOLD_CREDIT)
                .inputItems(plate, Cupronickel, 1)
                .outputItems(CREDIT_CUPRONICKEL, 4)
                .save(provider);

        FORMING_PRESS_RECIPES.recipeBuilder("doge_coin")
                .duration(100).EUt(16)
                .notConsumable(SHAPE_MOLD_CREDIT)
                .inputItems(plate, Brass, 1)
                .outputItems(COIN_DOGE, 4)
                .save(provider);

        for (ItemEntry<Item> shapeMold : SHAPE_MOLDS) {
            FORMING_PRESS_RECIPES.recipeBuilder("copy_mold_" + shapeMold.get())
                    .duration(120).EUt(22)
                    .notConsumable(shapeMold)
                    .inputItems(SHAPE_EMPTY)
                    .outputItems(shapeMold)
                    .save(provider);
        }

        for (ItemEntry<Item> shapeExtruder : SHAPE_EXTRUDERS) {
            if (shapeExtruder == null) continue;
            FORMING_PRESS_RECIPES.recipeBuilder("copy_shape_" + shapeExtruder.get())
                    .duration(120).EUt(22)
                    .notConsumable(shapeExtruder)
                    .inputItems(SHAPE_EMPTY)
                    .outputItems(shapeExtruder)
                    .save(provider);
        }

        BENDER_RECIPES.recipeBuilder("empty_shape")
                .circuitMeta(4)
                .inputItems(plate, Steel, 4)
                .outputItems(SHAPE_EMPTY)
                .duration(180).EUt(12)
                .save(provider);

        BENDER_RECIPES.recipeBuilder("fluid_cell_tin")
                .circuitMeta(12)
                .inputItems(plate, Tin, 2)
                .outputItems(FLUID_CELL)
                .duration(200).EUt(VA[ULV])
                .save(provider);

        BENDER_RECIPES.recipeBuilder("fluid_cell_steel")
                .circuitMeta(12)
                .inputItems(plate, Steel)
                .outputItems(FLUID_CELL)
                .duration(100).EUt(VA[ULV])
                .save(provider);

        BENDER_RECIPES.recipeBuilder("fluid_cell_ptfe")
                .circuitMeta(12)
                .inputItems(plate, Polytetrafluoroethylene)
                .outputItems(FLUID_CELL, 4)
                .duration(100).EUt(VA[ULV])
                .save(provider);

        BENDER_RECIPES.recipeBuilder("fluid_cell_pbi")
                .circuitMeta(12)
                .inputItems(plate, Polybenzimidazole)
                .outputItems(FLUID_CELL, 16)
                .duration(100).EUt(VA[ULV])
                .save(provider);

        EXTRUDER_RECIPES.recipeBuilder("fluid_cell_tin")
                .inputItems(ingot, Tin, 2)
                .notConsumable(SHAPE_EXTRUDER_CELL)
                .outputItems(FLUID_CELL)
                .duration(128).EUt(VA[LV])
                .save(provider);

        EXTRUDER_RECIPES.recipeBuilder("fluid_cell_steel")
                .inputItems(ingot, Steel)
                .notConsumable(SHAPE_EXTRUDER_CELL)
                .outputItems(FLUID_CELL)
                .duration(128).EUt(VA[LV])
                .save(provider);

        EXTRUDER_RECIPES.recipeBuilder("fluid_cell_ptfe")
                .inputItems(ingot, Polytetrafluoroethylene)
                .notConsumable(SHAPE_EXTRUDER_CELL)
                .outputItems(FLUID_CELL, 4)
                .duration(128).EUt(VA[LV])
                .save(provider);

        EXTRUDER_RECIPES.recipeBuilder("fluid_cell_pbi")
                .inputItems(ingot, Polybenzimidazole)
                .notConsumable(SHAPE_EXTRUDER_CELL)
                .outputItems(FLUID_CELL, 16)
                .duration(128).EUt(VA[LV])
                .save(provider);

        EXTRUDER_RECIPES.recipeBuilder("glass_vial")
                .inputItems(dust, Glass)
                .notConsumable(SHAPE_EXTRUDER_CELL)
                .outputItems(FLUID_CELL_GLASS_VIAL, 4)
                .duration(128).EUt(VA[LV])
                .save(provider);

        COMPRESSOR_RECIPES.recipeBuilder("nether_quartz_plate")
                .inputItems(dust, NetherQuartz)
                .outputItems(plate, NetherQuartz)
                .duration(400).EUt(2).save(provider);

        COMPRESSOR_RECIPES.recipeBuilder("certus_quartz_plate")
                .inputItems(dust, CertusQuartz)
                .outputItems(plate, CertusQuartz)
                .duration(400).EUt(2).save(provider);

        COMPRESSOR_RECIPES.recipeBuilder("quartzite_plate")
                .inputItems(dust, Quartzite)
                .outputItems(plate, Quartzite)
                .duration(400).EUt(2).save(provider);

        COMPRESSOR_RECIPES.recipeBuilder("coke_bricks")
                .inputItems(COKE_OVEN_BRICK, 4)
                .outputItems(GTBlocks.CASING_COKE_BRICKS.asStack())
                .duration(300).EUt(2).save(provider);
    }

    private static void registerPrimitiveBlastFurnaceRecipes(Consumer<FinishedRecipe> provider) {
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_from_coal_gem").inputItems(ingot, Iron).inputItems(gem, Coal, 2).outputItems(ingot, Steel).outputItems(dustTiny, DarkAsh, 2).duration(1800).save(provider);
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_from_coal_dust").inputItems(ingot, Iron).inputItems(dust, Coal, 2).outputItems(ingot, Steel).outputItems(dustTiny, DarkAsh, 2).duration(1800).save(provider);
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_from_charcoal_gem").inputItems(ingot, Iron).inputItems(gem, Charcoal, 2).outputItems(ingot, Steel).outputItems(dustTiny, DarkAsh, 2).duration(1800).save(provider);
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_from_charcoal_dust").inputItems(ingot, Iron).inputItems(dust, Charcoal, 2).outputItems(ingot, Steel).outputItems(dustTiny, DarkAsh, 2).duration(1800).save(provider);
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_from_coke_gem").inputItems(ingot, Iron).inputItems(gem, Coke).outputItems(ingot, Steel).outputItems(dustTiny, Ash).duration(1500).save(provider);
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_from_coke_dust").inputItems(ingot, Iron).inputItems(dust, Coke).outputItems(ingot, Steel).outputItems(dustTiny, Ash).duration(1500).save(provider);

        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_from_coal_block").inputItems(block, Iron).inputItems(block, Coal, 2).outputItems(block, Steel).outputItems(dust, DarkAsh, 2).duration(16200).save(provider);
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_from_charcoal_block").inputItems(block, Iron).inputItems(block, Charcoal, 2).outputItems(block, Steel).outputItems(dust, DarkAsh, 2).duration(16200).save(provider);
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_from_coke_block").inputItems(block, Iron).inputItems(block, Coke).outputItems(block, Steel).outputItems(dust, Ash).duration(13500).save(provider);

        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_from_coal_gem_wrought").inputItems(ingot, WroughtIron).inputItems(gem, Coal, 2).outputItems(ingot, Steel).outputItems(dustTiny, DarkAsh, 2).duration(800).save(provider);
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_from_coal_dust_wrought").inputItems(ingot, WroughtIron).inputItems(dust, Coal, 2).outputItems(ingot, Steel).outputItems(dustTiny, DarkAsh, 2).duration(800).save(provider);
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_from_charcoal_gem_wrought").inputItems(ingot, WroughtIron).inputItems(gem, Charcoal, 2).outputItems(ingot, Steel).outputItems(dustTiny, DarkAsh, 2).duration(800).save(provider);
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_from_charcoal_dust_wrought").inputItems(ingot, WroughtIron).inputItems(dust, Charcoal, 2).outputItems(ingot, Steel).outputItems(dustTiny, DarkAsh, 2).duration(800).save(provider);
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_from_coke_gem_wrought").inputItems(ingot, WroughtIron).inputItems(gem, Coke).outputItems(ingot, Steel).outputItems(dustTiny, Ash).duration(600).save(provider);
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_from_coke_dust_wrought").inputItems(ingot, WroughtIron).inputItems(dust, Coke).outputItems(ingot, Steel).outputItems(dustTiny, Ash).duration(600).save(provider);

        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_from_coal_block_wrought").inputItems(block, WroughtIron).inputItems(block, Coal, 2).outputItems(block, Steel).outputItems(dust, DarkAsh, 2).duration(7200).save(provider);
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_from_charcoal_block_wrought").inputItems(block, WroughtIron).inputItems(block, Charcoal, 2).outputItems(block, Steel).outputItems(dust, DarkAsh, 2).duration(7200).save(provider);
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_from_coke_block_wrought").inputItems(block, WroughtIron).inputItems(block, Coke).outputItems(block, Steel).outputItems(dust, Ash).duration(5400).save(provider);
    }

    private static void registerCokeOvenRecipes(Consumer<FinishedRecipe> provider) {
        COKE_OVEN_RECIPES.recipeBuilder("log_to_charcoal").inputItems(ItemTags.LOGS).outputItems(gem, Charcoal).outputFluids(Creosote.getFluid(250)).duration(900).save(provider);
        COKE_OVEN_RECIPES.recipeBuilder("coal_to_coke").inputItems(gem, Coal).outputItems(gem, Coke).outputFluids(Creosote.getFluid(500)).duration(900).save(provider);
        COKE_OVEN_RECIPES.recipeBuilder("coal_to_coke_block").inputItems(block, Coal).outputItems(block, Coke).outputFluids(Creosote.getFluid(4500)).duration(8100).save(provider);
    }

    private static void registerStoneBricksRecipes(Consumer<FinishedRecipe> provider) {
        // TODO Stone type decorative blocks
        /*
        // normal variant -> cobble variant
        List<ItemStack> cobbles = Arrays.stream(BlockStoneCobble.BlockType.values()).map(MetaBlocks.STONE_COBBLE::getItemVariant).collect(Collectors.toList());
        List<ItemStack> mossCobbles = Arrays.stream(BlockStoneCobbleMossy.BlockType.values()).map(MetaBlocks.STONE_COBBLE_MOSSY::getItemVariant).collect(Collectors.toList());
        List<ItemStack> smooths = Arrays.stream(BlockStoneSmooth.BlockType.values()).map(MetaBlocks.STONE_SMOOTH::getItemVariant).collect(Collectors.toList());
        List<ItemStack> polisheds = Arrays.stream(BlockStonePolished.BlockType.values()).map(MetaBlocks.STONE_POLISHED::getItemVariant).collect(Collectors.toList());
        List<ItemStack> bricks = Arrays.stream(BlockStoneBricks.BlockType.values()).map(MetaBlocks.STONE_BRICKS::getItemVariant).collect(Collectors.toList());
        List<ItemStack> crackedBricks = Arrays.stream(BlockStoneBricksCracked.BlockType.values()).map(MetaBlocks.STONE_BRICKS_CRACKED::getItemVariant).collect(Collectors.toList());
        List<ItemStack> mossBricks = Arrays.stream(BlockStoneBricksMossy.BlockType.values()).map(MetaBlocks.STONE_BRICKS_MOSSY::getItemVariant).collect(Collectors.toList());
        List<ItemStack> chiseledBricks = Arrays.stream(BlockStoneChiseled.BlockType.values()).map(MetaBlocks.STONE_CHISELED::getItemVariant).collect(Collectors.toList());
        List<ItemStack> tiledBricks = Arrays.stream(BlockStoneTiled.BlockType.values()).map(MetaBlocks.STONE_TILED::getItemVariant).collect(Collectors.toList());
        List<ItemStack> smallTiledBricks = Arrays.stream(BlockStoneTiledSmall.BlockType.values()).map(MetaBlocks.STONE_TILED_SMALL::getItemVariant).collect(Collectors.toList());
        List<ItemStack> windmillA = Arrays.stream(BlockStoneWindmillA.BlockType.values()).map(MetaBlocks.STONE_WINDMILL_A::getItemVariant).collect(Collectors.toList());
        List<ItemStack> windmillB = Arrays.stream(BlockStoneWindmillB.BlockType.values()).map(MetaBlocks.STONE_WINDMILL_B::getItemVariant).collect(Collectors.toList());
        List<ItemStack> squareBricks = Arrays.stream(BlockStoneBricksSquare.BlockType.values()).map(MetaBlocks.STONE_BRICKS_SQUARE::getItemVariant).collect(Collectors.toList());
        List<ItemStack> smallBricks = Arrays.stream(BlockStoneBricksSmall.BlockType.values()).map(MetaBlocks.STONE_BRICKS_SMALL::getItemVariant).collect(Collectors.toList());


        registerSmoothRecipe(cobbles, smooths);
        registerCobbleRecipe(smooths, cobbles);
        registerMossRecipe(cobbles, mossCobbles);
        registerSmoothRecipe(smooths, polisheds);
        registerBricksRecipe(polisheds, bricks, MarkerColor.LightBlue);
        registerCobbleRecipe(bricks, crackedBricks);
        registerMossRecipe(bricks, mossBricks);
        registerBricksRecipe(polisheds, chiseledBricks, MarkerColor.White);
        registerBricksRecipe(polisheds, tiledBricks, MarkerColor.Red);
        registerBricksRecipe(tiledBricks, smallTiledBricks, MarkerColor.Red);
        registerBricksRecipe(polisheds, windmillA, MarkerColor.Blue);
        registerBricksRecipe(polisheds, windmillB, MarkerColor.Yellow);
        registerBricksRecipe(polisheds, squareBricks, MarkerColor.Green);
        registerBricksRecipe(polisheds, smallBricks, MarkerColor.Pink);

        for (int i = 0; i < smooths.size(); i++) {
            EXTRUDER_RECIPES.recipeBuilder()
                    .inputItems(smooths.get(i))
                    .notConsumable(SHAPE_EXTRUDER_INGOT)
                    .outputItems(bricks.get(i))
                    .duration(24).EUt(8).save(provider);
        }
         */
    }

    private static void registerMixingCrystallizationRecipes(Consumer<FinishedRecipe> provider) {

        AUTOCLAVE_RECIPES.recipeBuilder("silicon_dioxide_to_quartzite_gem")
                .inputItems(dust, SiliconDioxide)
                .inputFluids(DistilledWater.getFluid(250))
                .chancedOutput(ChemicalHelper.get(gem, Quartzite), 1000, 1000)
                .duration(1200).EUt(24).save(provider);

        //todo find UU-Matter replacement
//        AUTOCLAVE_RECIPES.recipeBuilder()
//            .inputItems(dust, NetherStar)
//            .inputFluids(UUMatter.getFluid(576))
//            .chancedOutput(new ItemStack(Items.NETHER_STAR), 3333, 3333)
//            .duration(72000).EUt(VA[HV]).save(provider);

        MIXER_RECIPES.recipeBuilder("indium_concentrate")
                .inputItems(crushedPurified, Sphalerite)
                .inputItems(crushedPurified, Galena)
                .inputFluids(SulfuricAcid.getFluid(4000))
                .outputFluids(IndiumConcentrate.getFluid(1000))
                .duration(60).EUt(150).save(provider);

        // TODO Asphalt
        /*
        MIXER_RECIPES.recipeBuilder()
                .inputItems(dust, Coal)
                .inputFluids(Concrete.getFluid(L))
                .outputItems(MetaBlocks.ASPHALT.getItemVariant(BlockAsphalt.BlockType.ASPHALT))
                .duration(60).EUt(16).save(provider);

        MIXER_RECIPES.recipeBuilder()
                .inputItems(dust, Charcoal)
                .inputFluids(Concrete.getFluid(L))
                .outputItems(MetaBlocks.ASPHALT.getItemVariant(BlockAsphalt.BlockType.ASPHALT))
                .duration(60).EUt(16).save(provider);

        MIXER_RECIPES.recipeBuilder()
                .inputItems(dust, Carbon)
                .inputFluids(Concrete.getFluid(L))
                .outputItems(MetaBlocks.ASPHALT.getItemVariant(BlockAsphalt.BlockType.ASPHALT))
                .duration(60).EUt(16).save(provider);
         */
    }

    private static final MaterialStack[][] alloySmelterList = {
            {new MaterialStack(Copper, 3L), new MaterialStack(Tin, 1), new MaterialStack(Bronze, 4L)},
            {new MaterialStack(Copper, 3L), new MaterialStack(Zinc, 1), new MaterialStack(Brass, 4L)},
            {new MaterialStack(Copper, 1), new MaterialStack(Nickel, 1), new MaterialStack(Cupronickel, 2L)},
            {new MaterialStack(Copper, 1), new MaterialStack(Redstone, 4L), new MaterialStack(RedAlloy, 1)},
            {new MaterialStack(AnnealedCopper, 3L), new MaterialStack(Tin, 1), new MaterialStack(Bronze, 4L)},
            {new MaterialStack(AnnealedCopper, 3L), new MaterialStack(Zinc, 1), new MaterialStack(Brass, 4L)},
            {new MaterialStack(AnnealedCopper, 1), new MaterialStack(Nickel, 1), new MaterialStack(Cupronickel, 2L)},
            {new MaterialStack(AnnealedCopper, 1), new MaterialStack(Redstone, 4L), new MaterialStack(RedAlloy, 1)},
            {new MaterialStack(Iron, 1), new MaterialStack(Tin, 1), new MaterialStack(TinAlloy, 2L)},
            {new MaterialStack(WroughtIron, 1), new MaterialStack(Tin, 1), new MaterialStack(TinAlloy, 2L)},
            {new MaterialStack(Iron, 2L), new MaterialStack(Nickel, 1), new MaterialStack(Invar, 3L)},
            {new MaterialStack(WroughtIron, 2L), new MaterialStack(Nickel, 1), new MaterialStack(Invar, 3L)},
            {new MaterialStack(Lead, 4L), new MaterialStack(Antimony, 1), new MaterialStack(BatteryAlloy, 5L)},
            {new MaterialStack(Gold, 1), new MaterialStack(Silver, 1), new MaterialStack(Electrum, 2L)},
            {new MaterialStack(Magnesium, 1), new MaterialStack(Aluminium, 2L), new MaterialStack(Magnalium, 3L)},
            {new MaterialStack(Silver, 1), new MaterialStack(Electrotine, 4), new MaterialStack(BlueAlloy, 1)}};

    private static void registerAlloyRecipes(Consumer<FinishedRecipe> provider) {
        for (MaterialStack[] stack : alloySmelterList) {
            String recipeNape = stack[0].material().getName() + "_%s_and_" + stack[1].material().getName() + "_%s_into_" + stack[2].material().getName();
            if (stack[0].material().hasProperty(PropertyKey.INGOT)) {
                ALLOY_SMELTER_RECIPES.recipeBuilder(String.format(recipeNape, "ingot", "dust"))
                        .duration((int) stack[2].amount() * 50).EUt(16)
                        .inputItems(ingot, stack[0].material(), (int) stack[0].amount())
                        .inputItems(dust, stack[1].material(), (int) stack[1].amount())
                        .outputItems(ChemicalHelper.get(ingot, stack[2].material(), (int) stack[2].amount()))
                        .save(provider);
            }
            if (stack[1].material().hasProperty(PropertyKey.INGOT)) {
                ALLOY_SMELTER_RECIPES.recipeBuilder(String.format(recipeNape, "dust", "ingot"))
                        .duration((int) stack[2].amount() * 50).EUt(16)
                        .inputItems(dust, stack[0].material(), (int) stack[0].amount())
                        .inputItems(ingot, stack[1].material(), (int) stack[1].amount())
                        .outputItems(ChemicalHelper.get(ingot, stack[2].material(), (int) stack[2].amount()))
                        .save(provider);
            }
            if (stack[0].material().hasProperty(PropertyKey.INGOT)
                    && stack[1].material().hasProperty(PropertyKey.INGOT)) {
                ALLOY_SMELTER_RECIPES.recipeBuilder(String.format(recipeNape, "ingot", "ingot"))
                        .duration((int) stack[2].amount() * 50).EUt(16)
                        .inputItems(ingot, stack[0].material(), (int) stack[0].amount())
                        .inputItems(ingot, stack[1].material(), (int) stack[1].amount())
                        .outputItems(ChemicalHelper.get(ingot, stack[2].material(), (int) stack[2].amount()))
                        .save(provider);
            }
            ALLOY_SMELTER_RECIPES.recipeBuilder(String.format(recipeNape, "dust", "dust"))
                    .duration((int) stack[2].amount() * 50).EUt(16)
                    .inputItems(dust, stack[0].material(), (int) stack[0].amount())
                    .inputItems(dust, stack[1].material(), (int) stack[1].amount())
                    .outputItems(ChemicalHelper.get(ingot, stack[2].material(), (int) stack[2].amount()))
                    .save(provider);
        }

        COMPRESSOR_RECIPES.recipeBuilder("carbon_mesh").inputItems(CARBON_FIBERS, 2).outputItems(CARBON_MESH).duration(100).EUt(2).save(provider);
        COMPRESSOR_RECIPES.recipeBuilder("carbon_fiber_plate").inputItems(CARBON_MESH).outputItems(CARBON_FIBER_PLATE).duration(200).EUt(2).save(provider);

        ALLOY_SMELTER_RECIPES.recipeBuilder("rubber_sheet").duration(10).EUt(VA[ULV]).inputItems(ingot, Rubber, 2).notConsumable(SHAPE_MOLD_PLATE).outputItems(plate, Rubber).save(provider);
        ALLOY_SMELTER_RECIPES.recipeBuilder("rubber_bar").duration(100).EUt(VA[ULV]).inputItems(dust, Sulfur).inputItems(dust, RawRubber, 3).outputItems(ingot, Rubber).save(provider);

        // todo tag stuff
        //ALLOY_SMELTER_RECIPES.recipeBuilder("coke_oven_brick").duration(150).EUt(VA[ULV]).inputItems(OreDictUnifier.get("sand")).inputItems(new ItemStack(Items.CLAY_BALL)).outputItems(COKE_OVEN_BRICK, 2).save(provider);
    }

    private static void registerAssemblerRecipes(Consumer<FinishedRecipe> provider) {
        for (int i = 0; i < CHEMICAL_DYES.length; i++) {
            CANNER_RECIPES.recipeBuilder("spray_can_" + CHEMICAL_DYES[i].getName())
                    .inputItems(SPRAY_EMPTY)
                    .inputFluids(CHEMICAL_DYES[i].getFluid(L * 4))
                    .outputItems(SPRAY_CAN_DYES[i])
                    .EUt(VA[ULV]).duration(200)
                    .save(provider);
        }

        CANNER_RECIPES.recipeBuilder("spray_can_solvent")
                .inputItems(SPRAY_EMPTY)
                .inputFluids(Acetone.getFluid(1000))
                .outputItems(SPRAY_SOLVENT)
                .EUt(VA[ULV]).duration(200)
                .save(provider);

        Material material = Iron;

        ASSEMBLER_RECIPES.recipeBuilder("cover_shutter")
                .inputItems(new ItemStack(Items.IRON_DOOR))
                .inputItems(plate, material, 2)
                .outputItems(COVER_SHUTTER, 2)
                .EUt(16).duration(100)
                .save(provider);

        // TODO Crafting station, crafting cover
        //ASSEMBLER_RECIPES.recipeBuilder("cover_crafting")
        //        .inputItems(WORKBENCH)
        //        .inputItems(plate, material)
        //        .outputItems(COVER_CRAFTING)
        //        .EUt(16).duration(100)
        //        .save(provider);

        FluidStack solder = SolderingAlloy.getFluid(L / 2);

        ASSEMBLER_RECIPES.recipeBuilder("cover_machine_controller")
                .inputItems(new ItemStack(Blocks.LEVER))
                .inputItems(plate, material)
                .inputFluids(solder)
                .outputItems(COVER_MACHINE_CONTROLLER)
                .EUt(16).duration(100)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("cover_energy_detector")
                .inputItems(cableGtSingle, Copper, 4)
                .inputItems(CustomTags.LV_CIRCUITS)
                .inputItems(plate, material)
                .inputFluids(solder)
                .outputItems(COVER_ENERGY_DETECTOR)
                .EUt(16).duration(100)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("cover_advanced_energy_detector")
                .inputItems(COVER_ENERGY_DETECTOR)
                .inputItems(SENSOR_HV)
                .inputFluids(solder)
                .outputItems(COVER_ENERGY_DETECTOR_ADVANCED)
                .EUt(16).duration(100)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("cover_activity_detector")
                .inputItems(new ItemStack(Blocks.REDSTONE_TORCH))
                .inputItems(plate, material)
                .inputFluids(solder)
                .outputItems(COVER_ACTIVITY_DETECTOR)
                .EUt(16).duration(100)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("cover_advanced_activity_cover")
                .inputItems(wireFine, Gold, 4)
                .inputItems(CustomTags.HV_CIRCUITS)
                .inputItems(plate, Aluminium)
                .inputFluids(solder)
                .outputItems(COVER_ACTIVITY_DETECTOR_ADVANCED)
                .EUt(16).duration(100)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("cover_fluid_detector")
                .inputItems(new ItemStack(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE))
                .inputItems(plate, material)
                .inputFluids(solder)
                .outputItems(COVER_FLUID_DETECTOR)
                .EUt(16).duration(100)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("cover_item_detector")
                .inputItems(new ItemStack(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE))
                .inputItems(plate, material)
                .inputFluids(solder)
                .outputItems(COVER_ITEM_DETECTOR)
                .EUt(16).duration(100)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("cover_advanced_fluid_detector")
                .inputItems(COVER_FLUID_DETECTOR)
                .inputItems(SENSOR_HV)
                .inputFluids(solder)
                .outputItems(COVER_FLUID_DETECTOR_ADVANCED)
                .EUt(16).duration(100)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("cover_advanced_item_detector")
                .inputItems(COVER_ITEM_DETECTOR)
                .inputItems(SENSOR_HV)
                .inputFluids(solder)
                .outputItems(COVER_ITEM_DETECTOR_ADVANCED)
                .EUt(16).duration(100)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("cover_maintenance_detector")
                .inputItems(EMITTER_LV)
                .inputItems(plate, Steel)
                .inputFluids(solder)
                .outputItems(COVER_MAINTENANCE_DETECTOR)
                .EUt(16).duration(100)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("cover_screen")
                .inputItems(plate, Glass)
                .inputItems(foil, Aluminium, 4)
                .inputItems(CustomTags.LV_CIRCUITS)
                .inputItems(wireFine, Copper, 4)
                .outputItems(COVER_SCREEN)
                .EUt(16).duration(50)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("cover_infinite_water")
                .inputItems(ELECTRIC_PUMP_HV, 2)
                .inputItems(new ItemStack(Items.CAULDRON))
                .inputItems(CustomTags.HV_CIRCUITS)
                .outputItems(COVER_INFINITE_WATER)
                .EUt(VA[HV]).duration(100)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("cover_ender_fluid_link")
                .inputItems(plate, EnderPearl, 9)
                .inputItems(plateDouble, StainlessSteel)
                .inputItems(SENSOR_HV)
                .inputItems(EMITTER_HV)
                .inputItems(ELECTRIC_PUMP_HV)
                .inputFluids(Polyethylene.getFluid(L * 2))
                .outputItems(COVER_ENDER_FLUID_LINK)
                .EUt(VA[HV]).duration(320)
                .save(provider);

        // TODO Storage cover
        //ASSEMBLER_RECIPES.recipeBuilder()
        //        .inputItems(OreDictNames.chestWood.toString())
        //        .inputItems(ELECTRIC_PISTON_LV)
        //        .inputItems(plate, Iron)
        //        .inputFluids(SolderingAlloy.getFluid(72))
        //        .outputItems(COVER_STORAGE)
        //        .EUt(16)
        //        .duration(100)
        //        .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("casing_ulv").EUt(16).inputItems(plate, WroughtIron, 8).outputItems(GTBlocks.MACHINE_CASING_ULV.asStack()).circuitMeta(8).duration(25).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("casing_lv").EUt(16).inputItems(plate, Steel, 8).outputItems(GTBlocks.MACHINE_CASING_LV.asStack()).circuitMeta(8).duration(50).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("casing_mv").EUt(16).inputItems(plate, Aluminium, 8).outputItems(GTBlocks.MACHINE_CASING_MV.asStack()).circuitMeta(8).duration(50).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("casing_hv").EUt(16).inputItems(plate, StainlessSteel, 8).outputItems(GTBlocks.MACHINE_CASING_HV.asStack()).circuitMeta(8).duration(50).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("casing_ev").EUt(16).inputItems(plate, Titanium, 8).outputItems(GTBlocks.MACHINE_CASING_EV.asStack()).circuitMeta(8).duration(50).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("casing_iv").EUt(16).inputItems(plate, TungstenSteel, 8).outputItems(GTBlocks.MACHINE_CASING_IV.asStack()).circuitMeta(8).duration(50).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("casing_luv").EUt(16).inputItems(plate, RhodiumPlatedPalladium, 8).outputItems(GTBlocks.MACHINE_CASING_LuV.asStack()).circuitMeta(8).duration(50).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("casing_zpm").EUt(16).inputItems(plate, NaquadahAlloy, 8).outputItems(GTBlocks.MACHINE_CASING_ZPM.asStack()).circuitMeta(8).duration(50).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("casing_uv").EUt(16).inputItems(plate, Darmstadtium, 8).outputItems(GTBlocks.MACHINE_CASING_UV.asStack()).circuitMeta(8).duration(50).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("casing_uhv").EUt(16).inputItems(plate, Neutronium, 8).outputItems(GTBlocks.MACHINE_CASING_UHV.asStack()).circuitMeta(8).duration(50).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("coil_cupronickel").EUt(VA[LV]).inputItems(wireGtDouble, Cupronickel, 8).inputItems(foil, Bronze, 8).inputFluids(TinAlloy.getFluid(GTValues.L)).outputItems(GTBlocks.COIL_CUPRONICKEL.asStack()).duration(200).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("coil_kanthal").EUt(VA[MV]).inputItems(wireGtDouble, Kanthal, 8).inputItems(foil, Aluminium, 8).inputFluids(Copper.getFluid(GTValues.L)).outputItems(GTBlocks.COIL_KANTHAL.asStack()).duration(300).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("coil_nichrome").EUt(VA[HV]).inputItems(wireGtDouble, Nichrome, 8).inputItems(foil, StainlessSteel, 8).inputFluids(Aluminium.getFluid(GTValues.L)).outputItems(GTBlocks.COIL_NICHROME.asStack()).duration(400).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("coil_tungstensteel").EUt(VA[EV]).inputItems(wireGtDouble, TungstenSteel, 8).inputItems(foil, VanadiumSteel, 8).inputFluids(Nichrome.getFluid(GTValues.L)).outputItems(GTBlocks.COIL_TUNGSTENSTEEL.asStack()).duration(500).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("coil_hssg").EUt(VA[IV]).inputItems(wireGtDouble, HSSG, 8).inputItems(foil, TungstenCarbide, 8).inputFluids(Tungsten.getFluid(GTValues.L)).outputItems(GTBlocks.COIL_HSSG.asStack()).duration(600).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("coil_naquadah").EUt(VA[LuV]).inputItems(wireGtDouble, Naquadah, 8).inputItems(foil, Osmium, 8).inputFluids(TungstenSteel.getFluid(GTValues.L)).outputItems(GTBlocks.COIL_NAQUADAH.asStack()).duration(700).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("coil_trinium").EUt(VA[ZPM]).inputItems(wireGtDouble, Trinium, 8).inputItems(foil, NaquadahEnriched, 8).inputFluids(Naquadah.getFluid(GTValues.L)).outputItems(GTBlocks.COIL_TRINIUM.asStack()).duration(800).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("coil_tritanium").EUt(VA[UV]).inputItems(wireGtDouble, Tritanium, 8).inputItems(foil, Naquadria, 8).inputFluids(Trinium.getFluid(GTValues.L)).outputItems(GTBlocks.COIL_TRITANIUM.asStack()).duration(900).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("casing_bronze_bricks").EUt(16).inputItems(plate, Bronze, 6).inputItems(new ItemStack(Blocks.BRICKS)).circuitMeta(6).outputItems(GTBlocks.CASING_BRONZE_BRICKS.asStack(2)).duration(50).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("casing_invar_heatproof").EUt(16).inputItems(plate, Invar, 6).inputItems(frameGt, Invar).circuitMeta(6).outputItems(GTBlocks.CASING_INVAR_HEATPROOF.asStack(2)).duration(50).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("casing_steel_solid").EUt(16).inputItems(plate, Steel, 6).inputItems(frameGt, Steel).circuitMeta(6).outputItems(GTBlocks.CASING_STEEL_SOLID.asStack(2)).duration(50).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("casing_aluminium_frostproof").EUt(16).inputItems(plate, Aluminium, 6).inputItems(frameGt, Aluminium).circuitMeta(6).outputItems(GTBlocks.CASING_ALUMINIUM_FROSTPROOF.asStack(2)).duration(50).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("casing_tungsteensteel_robust").EUt(16).inputItems(plate, TungstenSteel, 6).inputItems(frameGt, TungstenSteel).circuitMeta(6).outputItems(GTBlocks.CASING_TUNGSTENSTEEL_ROBUST.asStack(2)).duration(50).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("casing_stainless_clean").EUt(16).inputItems(plate, StainlessSteel, 6).inputItems(frameGt, StainlessSteel).circuitMeta(6).outputItems(GTBlocks.CASING_STAINLESS_CLEAN.asStack(2)).duration(50).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("casing_titanium_stable").EUt(16).inputItems(plate, Titanium, 6).inputItems(frameGt, Titanium).circuitMeta(6).outputItems(GTBlocks.CASING_TITANIUM_STABLE.asStack(2)).duration(50).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("casing_hsse_sturdy").EUt(16).inputItems(plate, HSSE, 6).inputItems(frameGt, Europium).circuitMeta(6).outputItems(GTBlocks.CASING_HSSE_STURDY.asStack(2)).duration(50).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("casing_palladium_substation").EUt(16).inputItems(plate, Palladium, 6).inputItems(frameGt, Iridium).circuitMeta(6).outputItems(GTBlocks.CASING_PALLADIUM_SUBSTATION.asStack(2)).duration(50).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("casing_ptfe_inert").EUt(16).inputItems(GTBlocks.CASING_STEEL_SOLID.asStack()).inputFluids(Polytetrafluoroethylene.getFluid(216)).circuitMeta(6).outputItems(GTBlocks.CASING_PTFE_INERT.asStack()).duration(50).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("superconducting_coil_luv").EUt(VA[LuV]).inputItems(wireGtDouble, IndiumTinBariumTitaniumCuprate, 32).inputItems(foil, NiobiumTitanium, 32).inputFluids(Trinium.getFluid(GTValues.L * 24)).outputItems(GTBlocks.SUPERCONDUCTING_COIL.asStack()).duration(100).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("superconducting_coil_zpm").EUt(VA[ZPM]).inputItems(wireGtDouble, UraniumRhodiumDinaquadide, 16).inputItems(foil, NiobiumTitanium, 16).inputFluids(Trinium.getFluid(GTValues.L * 16)).outputItems(GTBlocks.SUPERCONDUCTING_COIL.asStack()).duration(100).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("superconducting_coil_uv").EUt(VA[UV]).inputItems(wireGtDouble, EnrichedNaquadahTriniumEuropiumDuranide, 8).inputItems(foil, NiobiumTitanium, 8).inputFluids(Trinium.getFluid(GTValues.L * 8)).outputItems(GTBlocks.SUPERCONDUCTING_COIL.asStack()).duration(100).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("fusion_coil").EUt(VA[ZPM]).inputItems(GTBlocks.SUPERCONDUCTING_COIL.asStack()).inputItems(FIELD_GENERATOR_IV.asStack(2)).inputItems(ELECTRIC_PUMP_IV).inputItems(NEUTRON_REFLECTOR.asStack(2)).inputItems(CustomTags.LuV_CIRCUITS, 4).inputItems(pipeSmallFluid, Naquadah, 4).inputItems(plate, Europium, 4).inputFluids(VanadiumGallium.getFluid(GTValues.L * 4)).outputItems(GTBlocks.FUSION_COIL.asStack()).duration(100).cleanroom(CleanroomType.CLEANROOM).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("fusion_glass").EUt(VA[LuV]).inputItems(GTBlocks.CASING_LAMINATED_GLASS.asStack()).inputItems(plate, Naquadah, 4).inputItems(NEUTRON_REFLECTOR.asStack(4)).outputItems(GTBlocks.FUSION_GLASS.asStack(2)).inputFluids(Polybenzimidazole.getFluid(GTValues.L)).duration(50).cleanroom(CleanroomType.CLEANROOM).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("fusion_casing").EUt(VA[LuV]).inputItems(GTBlocks.MACHINE_CASING_LuV.asStack()).inputItems(GTBlocks.SUPERCONDUCTING_COIL.asStack()).inputItems(NEUTRON_REFLECTOR).inputItems(ELECTRIC_PUMP_LuV).inputItems(plate, TungstenSteel, 6).inputFluids(Polybenzimidazole.getFluid(GTValues.L)).outputItems(GTBlocks.FUSION_CASING.asStack(2)).duration(100).cleanroom(CleanroomType.CLEANROOM).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("fusion_casing_mk2").EUt(VA[ZPM]).inputItems(GTBlocks.MACHINE_CASING_ZPM.asStack()).inputItems(GTBlocks.FUSION_COIL.asStack()).inputItems(VOLTAGE_COIL_ZPM.asStack(2)).inputItems(FIELD_GENERATOR_LuV).inputItems(plate, Europium, 6).inputFluids(Polybenzimidazole.getFluid(GTValues.L * 2)).outputItems(GTBlocks.FUSION_CASING_MK2.asStack(2)).duration(100).cleanroom(CleanroomType.CLEANROOM).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("fusion_casing_mk3").EUt(VA[UV]).inputItems(GTBlocks.MACHINE_CASING_UV.asStack()).inputItems(GTBlocks.FUSION_COIL.asStack()).inputItems(VOLTAGE_COIL_UV.asStack(2)).inputItems(FIELD_GENERATOR_ZPM).inputItems(plate, Americium, 6).inputFluids(Polybenzimidazole.getFluid(GTValues.L * 4)).outputItems(GTBlocks.FUSION_CASING_MK3.asStack(2)).duration(100).cleanroom(CleanroomType.CLEANROOM).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("casing_steel_turbine").EUt(16).inputItems(plate, Magnalium, 6).inputItems(frameGt, BlueSteel, 1).circuitMeta(6).outputItems(GTBlocks.CASING_STEEL_TURBINE.asStack(2)).duration(50).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("casing_stainless_steel_turbine").EUt(16).inputItems(GTBlocks.CASING_STEEL_TURBINE.asStack()).inputItems(plate, StainlessSteel, 6).circuitMeta(6).outputItems(GTBlocks.CASING_STAINLESS_TURBINE.asStack(2)).duration(50).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("casing_titanium_turbine").EUt(16).inputItems(GTBlocks.CASING_STEEL_TURBINE.asStack()).inputItems(plate, Titanium, 6).circuitMeta(6).outputItems(GTBlocks.CASING_TITANIUM_TURBINE.asStack(2)).duration(50).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("casing_tungstensteel_turbine").EUt(16).inputItems(GTBlocks.CASING_STEEL_TURBINE.asStack()).inputItems(plate, TungstenSteel, 6).circuitMeta(6).outputItems(GTBlocks.CASING_TUNGSTENSTEEL_TURBINE.asStack(2)).duration(50).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("plascrete").EUt(48).inputItems(frameGt, Steel).inputItems(plate, Polyethylene, 6).inputFluids(Concrete.getFluid(L)).outputItems(GTBlocks.PLASTCRETE.asStack(2)).duration(200).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("cleanroom_glass").EUt(48).inputItems(frameGt, Steel).inputItems(plate, Polyethylene, 6).inputFluids(Glass.getFluid(L)).outputItems(GTBlocks.CLEANROOM_GLASS.asStack(2)).duration(200).save(provider);

        // If these recipes are changed, change the values in MaterialInfoLoader.java

        ASSEMBLER_RECIPES.recipeBuilder("hull_ulv").duration(25).EUt(16).inputItems(GTBlocks.MACHINE_CASING_ULV.asStack()).inputItems(cableGtSingle, RedAlloy, 2).inputFluids(Polyethylene.getFluid(L * 2)).outputItems(GTMachines.HULL[0]).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("hull_lv").duration(50).EUt(16).inputItems(GTBlocks.MACHINE_CASING_LV.asStack()).inputItems(cableGtSingle, Tin, 2).inputFluids(Polyethylene.getFluid(L * 2)).outputItems(GTMachines.HULL[1]).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("hull_mv").duration(50).EUt(16).inputItems(GTBlocks.MACHINE_CASING_MV.asStack()).inputItems(cableGtSingle, Copper, 2).inputFluids(Polyethylene.getFluid(L * 2)).outputItems(GTMachines.HULL[2]).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("hull_mv_annealed").duration(50).EUt(16).inputItems(GTBlocks.MACHINE_CASING_MV.asStack()).inputItems(cableGtSingle, AnnealedCopper, 2).inputFluids(Polyethylene.getFluid(L * 2)).outputItems(GTMachines.HULL[2]).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("hull_hv").duration(50).EUt(16).inputItems(GTBlocks.MACHINE_CASING_HV.asStack()).inputItems(cableGtSingle, Gold, 2).inputFluids(Polyethylene.getFluid(L * 2)).outputItems(GTMachines.HULL[3]).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("hull_ev").duration(50).EUt(16).inputItems(GTBlocks.MACHINE_CASING_EV.asStack()).inputItems(cableGtSingle, Aluminium, 2).inputFluids(Polyethylene.getFluid(L * 2)).outputItems(GTMachines.HULL[4]).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("hull_iv").duration(50).EUt(16).inputItems(GTBlocks.MACHINE_CASING_IV.asStack()).inputItems(cableGtSingle, Platinum, 2).inputFluids(Polytetrafluoroethylene.getFluid(L * 2)).outputItems(GTMachines.HULL[5]).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("hull_luv").duration(50).EUt(16).inputItems(GTBlocks.MACHINE_CASING_LuV.asStack()).inputItems(cableGtSingle, NiobiumTitanium, 2).inputFluids(Polytetrafluoroethylene.getFluid(L * 2)).outputItems(GTMachines.HULL[6]).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("hull_zpm").duration(50).EUt(16).inputItems(GTBlocks.MACHINE_CASING_ZPM.asStack()).inputItems(cableGtSingle, VanadiumGallium, 2).inputFluids(Polybenzimidazole.getFluid(L * 2)).outputItems(GTMachines.HULL[7]).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("hull_uv").duration(50).EUt(16).inputItems(GTBlocks.MACHINE_CASING_UV.asStack()).inputItems(cableGtSingle, YttriumBariumCuprate, 2).inputFluids(Polybenzimidazole.getFluid(L * 2)).outputItems(GTMachines.HULL[8]).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("hull_uhv").duration(50).EUt(16).inputItems(GTBlocks.MACHINE_CASING_UHV.asStack()).inputItems(cableGtSingle, Europium, 2).inputFluids(Polybenzimidazole.getFluid(L * 2)).outputItems(GTMachines.HULL[9]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("hopper_iron").EUt(2).inputItems(CustomTags.TAG_WOODEN_CHESTS).inputItems(plate, Iron, 5).outputItems(new ItemStack(Blocks.HOPPER)).duration(800).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("hopper_wrought_iron").EUt(2).inputItems(CustomTags.TAG_WOODEN_CHESTS).inputItems(plate, WroughtIron, 5).outputItems(new ItemStack(Blocks.HOPPER)).duration(800).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("wooden_crate").EUt(16).inputItems(ItemTags.PLANKS, 4).inputItems(screw, Iron, 4).outputItems(WOODEN_CRATE).duration(100).circuitMeta(5).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("bronze_crate").EUt(16).inputItems(rodLong, Bronze, 4).inputItems(plate, Bronze, 4).outputItems(BRONZE_CRATE).duration(200).circuitMeta(1).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("steel_crate").EUt(16).inputItems(rodLong, Steel, 4).inputItems(plate, Steel, 4).outputItems(STEEL_CRATE).duration(200).circuitMeta(1).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("aluminium_crate").EUt(16).inputItems(rodLong, Aluminium, 4).inputItems(plate, Aluminium, 4).outputItems(ALUMINIUM_CRATE).duration(200).circuitMeta(1).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("stainless_steel_crate").EUt(16).inputItems(rodLong, StainlessSteel, 4).inputItems(plate, StainlessSteel, 4).outputItems(STAINLESS_STEEL_CRATE).circuitMeta(1).duration(200).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("titanium_crate").EUt(16).inputItems(rodLong, Titanium, 4).inputItems(plate, Titanium, 4).outputItems(TITANIUM_CRATE).duration(200).circuitMeta(1).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("tungstensteel_crate").EUt(16).inputItems(rodLong, TungstenSteel, 4).inputItems(plate, TungstenSteel, 4).outputItems(TUNGSTENSTEEL_CRATE).duration(200).circuitMeta(1).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("wood_barrel").EUt(16).inputItems(ItemTags.PLANKS, 4).inputItems(rodLong, Iron, 2).outputItems(WOODEN_DRUM).duration(200).circuitMeta(2).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("bronze_drum").EUt(16).inputItems(rodLong, Bronze, 2).inputItems(plate, Bronze, 4).outputItems(BRONZE_DRUM).duration(200).circuitMeta(2).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("steel_drum").EUt(16).inputItems(rodLong, Steel, 2).inputItems(plate, Steel, 4).outputItems(STEEL_DRUM).duration(200).circuitMeta(2).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("aluminium_drum").EUt(16).inputItems(rodLong, Aluminium, 2).inputItems(plate, Aluminium, 4).outputItems(ALUMINIUM_DRUM).duration(200).circuitMeta(2).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("stainless_steel_drum").EUt(16).inputItems(rodLong, StainlessSteel, 2).inputItems(plate, StainlessSteel, 4).outputItems(STAINLESS_STEEL_DRUM).duration(200).circuitMeta(2).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("gold_drum").EUt(16).inputItems(rodLong, Gold, 2).inputItems(plate, Gold, 4).outputItems(GOLD_DRUM).duration(200).circuitMeta(2).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("titanium_drum").EUt(16).inputItems(rodLong, Titanium, 2).inputItems(plate, Titanium, 4).outputItems(TITANIUM_DRUM).duration(200).circuitMeta(2).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("tungstensteel_drum").EUt(16).inputItems(rodLong, TungstenSteel, 2).inputItems(plate, TungstenSteel, 4).outputItems(TUNGSTENSTEEL_DRUM).duration(200).circuitMeta(2).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("duct_tape_polyethylene").EUt(VA[LV]).inputItems(foil, Polyethylene, 4).inputItems(CARBON_MESH).inputFluids(Polyethylene.getFluid(288)).outputItems(DUCT_TAPE).duration(100).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("duct_tape_silicone_rubber").EUt(VA[LV]).inputItems(foil, SiliconeRubber, 2).inputItems(CARBON_MESH).inputFluids(Polyethylene.getFluid(288)).outputItems(DUCT_TAPE, 2).duration(100).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("duct_tape_polycaprolactam").EUt(VA[LV]).inputItems(foil, Polycaprolactam, 2).inputItems(CARBON_MESH).inputFluids(Polyethylene.getFluid(144)).outputItems(DUCT_TAPE, 4).duration(100).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("duct_tape_polybenzimidazole").EUt(VA[LV]).inputItems(foil, Polybenzimidazole).inputItems(CARBON_MESH).inputFluids(Polyethylene.getFluid(72)).outputItems(DUCT_TAPE, 8).duration(100).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("fluid_cell_large_steel")
                .inputItems(plateDouble, Steel, 2)
                .inputItems(ring, Bronze, 2)
                .outputItems(FLUID_CELL_LARGE_STEEL)
                .duration(200).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("fluid_cell_large_aluminium")
                .inputItems(plateDouble, Aluminium, 2)
                .inputItems(ring, Silver, 2)
                .outputItems(FLUID_CELL_LARGE_ALUMINIUM)
                .duration(200).EUt(64).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("fluid_cell_large_stainless_steel")
                .inputItems(plateDouble, StainlessSteel, 3)
                .inputItems(ring, Electrum, 3)
                .outputItems(FLUID_CELL_LARGE_STAINLESS_STEEL)
                .duration(200).EUt(VA[MV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("fluid_cell_large_titanium")
                .inputItems(plateDouble, Titanium, 3)
                .inputItems(ring, RoseGold, 3)
                .outputItems(FLUID_CELL_LARGE_TITANIUM)
                .duration(200).EUt(256).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("fluid_cell_large_tungstensteel")
                .inputItems(plateDouble, TungstenSteel, 4)
                .inputItems(ring, Platinum, 4)
                .outputItems(FLUID_CELL_LARGE_TUNGSTEN_STEEL)
                .duration(200).EUt(VA[HV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("fluid_drill_mv")
                .inputItems(HULL[MV])
                .inputItems(frameGt, Steel, 4)
                .inputItems(CustomTags.MV_CIRCUITS, 4)
                .inputItems(ELECTRIC_MOTOR_MV, 4)
                .inputItems(ELECTRIC_PUMP_MV, 4)
                .inputItems(gear, VanadiumSteel, 4)
                .circuitMeta(2)
                .outputItems(FLUID_DRILLING_RIG[MV])
                .duration(400).EUt(VA[MV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("fluid_drill_ev")
                .inputItems(HULL[EV])
                .inputItems(frameGt, Titanium, 4)
                .inputItems(CustomTags.EV_CIRCUITS, 4)
                .inputItems(ELECTRIC_MOTOR_EV, 4)
                .inputItems(ELECTRIC_PUMP_EV, 4)
                .inputItems(gear, TungstenCarbide, 4)
                .circuitMeta(2)
                .outputItems(FLUID_DRILLING_RIG[HV])
                .duration(400).EUt(VA[EV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("fluid_drill_luv")
                .inputItems(HULL[LuV])
                .inputItems(frameGt, TungstenSteel, 4)
                .inputItems(CustomTags.LuV_CIRCUITS, 4)
                .inputItems(ELECTRIC_MOTOR_LuV, 4)
                .inputItems(ELECTRIC_PUMP_LuV, 4)
                .inputItems(gear, Osmiridium, 4)
                .circuitMeta(2)
                .outputItems(FLUID_DRILLING_RIG[EV])
                .duration(400).EUt(VA[LuV]).save(provider);
    }

    private static void registerBlastFurnaceRecipes(Consumer<FinishedRecipe> provider) {
        BLAST_RECIPES.recipeBuilder("aluminium_from_ruby_dust").duration(400).EUt(100).inputItems(dust, Ruby).outputItems(nugget, Aluminium, 3).outputItems(dustTiny, DarkAsh).blastFurnaceTemp(1200).save(provider);
        BLAST_RECIPES.recipeBuilder("aluminium_from_ruby_gem").duration(320).EUt(100).inputItems(gem, Ruby).outputItems(nugget, Aluminium, 3).outputItems(dustTiny, DarkAsh).blastFurnaceTemp(1200).save(provider);
        BLAST_RECIPES.recipeBuilder("aluminium_from_green_sapphire_dust").duration(400).EUt(100).inputItems(dust, GreenSapphire).outputItems(nugget, Aluminium, 3).outputItems(dustTiny, DarkAsh).blastFurnaceTemp(1200).save(provider);
        BLAST_RECIPES.recipeBuilder("aluminium_from_green_sapphire_gem").duration(320).EUt(100).inputItems(gem, GreenSapphire).outputItems(nugget, Aluminium, 3).outputItems(dustTiny, DarkAsh).blastFurnaceTemp(1200).save(provider);
        BLAST_RECIPES.recipeBuilder("aluminium_from_sapphire_dust").duration(400).EUt(100).inputItems(dust, Sapphire).outputItems(nugget, Aluminium, 3).blastFurnaceTemp(1200).save(provider);
        BLAST_RECIPES.recipeBuilder("aluminium_from_sapphire_gem").duration(320).EUt(100).inputItems(gem, Sapphire).outputItems(nugget, Aluminium, 3).blastFurnaceTemp(1200).save(provider);
        BLAST_RECIPES.recipeBuilder("titanium_from_tetrachloride").duration(800).EUt(VA[HV]).inputItems(dust, Magnesium, 2).inputFluids(TitaniumTetrachloride.getFluid(1000)).outputItems(ingotHot, Titanium).outputItems(dust, MagnesiumChloride, 6).blastFurnaceTemp(Titanium.getBlastTemperature() + 200).save(provider);
        BLAST_RECIPES.recipeBuilder("steel_from_iron").duration(500).EUt(VA[MV]).inputItems(ingot, Iron).inputFluids(Oxygen.getFluid(200)).outputItems(ingot, Steel).outputItems(dustTiny, Ash).blastFurnaceTemp(1000).save(provider);
        BLAST_RECIPES.recipeBuilder("steel_from_wrought_iron").duration(300).EUt(VA[MV]).inputItems(ingot, WroughtIron).inputFluids(Oxygen.getFluid(200)).outputItems(ingot, Steel).outputItems(dustTiny, Ash).blastFurnaceTemp(1000).save(provider);

        BLAST_RECIPES.recipeBuilder("rutile_from_ilmenite")
                .inputItems(dust, Ilmenite, 10)
                .inputItems(dust, Carbon, 4)
                .outputItems(ingot, WroughtIron, 2)
                .outputItems(dust, Rutile, 4)
                .outputFluids(CarbonDioxide.getFluid(2000))
                .blastFurnaceTemp(1700)
                .duration(1600).EUt(VA[HV]).save(provider);

        //TODO Tempered Glass
        BLAST_RECIPES.recipeBuilder("tempered_glass_blasting")
                .inputItems(block, Glass)
                .inputFluids(Oxygen.getFluid(100))
                .outputItems(GTBlocks.CASING_TEMPERED_GLASS.asStack())
                .blastFurnaceTemp(1000)
                .duration(200).EUt(VA[MV]).save(provider);

        registerBlastFurnaceMetallurgyRecipes(provider);
    }

    private static void registerBlastFurnaceMetallurgyRecipes(Consumer<FinishedRecipe> provider) {
        createSulfurDioxideRecipe(provider, Stibnite, AntimonyTrioxide, 1500);
        createSulfurDioxideRecipe(provider, Sphalerite, Zincite, 1000);
        createSulfurDioxideRecipe(provider, Pyrite, Hematite, 2000);
        createSulfurDioxideRecipe(provider, Pentlandite, Garnierite, 1000);

        BLAST_RECIPES.recipeBuilder("tetrahedrite_metallurgy").duration(120).EUt(VA[MV]).blastFurnaceTemp(1200)
                .inputItems(dust, Tetrahedrite)
                .inputFluids(Oxygen.getFluid(3000))
                .outputItems(dust, CupricOxide)
                .outputItems(dustTiny, AntimonyTrioxide, 3)
                .outputFluids(SulfurDioxide.getFluid(2000))
                .save(provider);

        BLAST_RECIPES.recipeBuilder("cobaltite_metallurgy").duration(120).EUt(VA[MV]).blastFurnaceTemp(1200)
                .inputItems(dust, Cobaltite)
                .inputFluids(Oxygen.getFluid(3000))
                .outputItems(dust, CobaltOxide)
                .outputItems(dust, ArsenicTrioxide)
                .outputFluids(SulfurDioxide.getFluid(1000))
                .save(provider);

        BLAST_RECIPES.recipeBuilder("galena_metallurgy").duration(120).EUt(VA[MV]).blastFurnaceTemp(1200)
                .inputItems(dust, Galena)
                .inputFluids(Oxygen.getFluid(3000))
                .outputItems(dust, Massicot)
                .outputItems(nugget, Silver, 6)
                .outputFluids(SulfurDioxide.getFluid(1000))
                .save(provider);

        BLAST_RECIPES.recipeBuilder("chalcopyrite_metallurgy").duration(120).EUt(VA[MV]).blastFurnaceTemp(2273)
                .inputItems(dust, Chalcopyrite)
                .inputItems(dust, SiliconDioxide)
                .inputFluids(Oxygen.getFluid(3000))
                .outputItems(dust, CupricOxide)
                .outputItems(dust, Ferrosilite)
                .outputFluids(SulfurDioxide.getFluid(2000))
                .save(provider);

        BLAST_RECIPES.recipeBuilder("blast_silicon_dioxide").duration(240).EUt(VA[MV]).blastFurnaceTemp(1200)
                .inputItems(dust, SiliconDioxide)
                .inputItems(dust, Carbon, 2)
                .outputItems(ingotHot, Silicon)
                .outputItems(dustTiny, Ash)
                .outputFluids(CarbonMonoxide.getFluid(2000))
                .save(provider);
    }

    private static void createSulfurDioxideRecipe(Consumer<FinishedRecipe> provider, Material inputMaterial, Material outputMaterial, int sulfurDioxideAmount) {
        BLAST_RECIPES.recipeBuilder(inputMaterial.getName() + "_metallurgy").duration(120).EUt(VA[MV]).blastFurnaceTemp(1200)
                .inputItems(dust, inputMaterial)
                .inputFluids(Oxygen.getFluid(3000))
                .outputItems(dust, outputMaterial)
                .outputItems(dustTiny, Ash)
                .outputFluids(SulfurDioxide.getFluid(sulfurDioxideAmount))
                .save(provider);
    }

    private static void registerDecompositionRecipes(Consumer<FinishedRecipe> provider) {


        EXTRACTOR_RECIPES.recipeBuilder("raw_rubber_from_resin")
                .inputItems(STICKY_RESIN)
                .outputItems(dust, RawRubber, 3)
                .duration(150).EUt(2)
                .save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("raw_rubber_from_leaves").duration(300).EUt(2)
                .inputItems(GTBlocks.RUBBER_LEAVES.asStack(16))
                .outputItems(dust, RawRubber)
                .save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("raw_rubber_from_log").duration(300).EUt(2)
                .inputItems(GTBlocks.RUBBER_LOG.asStack())
                .outputItems(dust, RawRubber)
                .save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("raw_rubber_from_sapling").duration(300).EUt(2)
                .inputItems(GTBlocks.RUBBER_SAPLING.asStack())
                .outputItems(dust, RawRubber)
                .save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("raw_rubber_from_slime").duration(150).EUt(2)
                .inputItems(new ItemStack(Items.SLIME_BALL))
                .outputItems(dust, RawRubber, 2)
                .save(provider);

        // todo tag stuff
        //COMPRESSOR_RECIPES.recipeBuilder("plant_ball_from_sapling").duration(300).EUt(2).inputItems("treeSapling", 8).outputItems(PLANT_BALL).save(provider);
        COMPRESSOR_RECIPES.recipeBuilder("plant_ball_from_wheat").duration(300).EUt(2).inputItems(new ItemStack(Items.WHEAT, 8)).outputItems(PLANT_BALL).save(provider);
        COMPRESSOR_RECIPES.recipeBuilder("plant_ball_from_potato").duration(300).EUt(2).inputItems(new ItemStack(Items.POTATO, 8)).outputItems(PLANT_BALL).save(provider);
        COMPRESSOR_RECIPES.recipeBuilder("plant_ball_from_carrot").duration(300).EUt(2).inputItems(new ItemStack(Items.CARROT, 8)).outputItems(PLANT_BALL).save(provider);
        COMPRESSOR_RECIPES.recipeBuilder("plant_ball_from_cactus").duration(300).EUt(2).inputItems(new ItemStack(Blocks.CACTUS, 8)).outputItems(PLANT_BALL).save(provider);
        COMPRESSOR_RECIPES.recipeBuilder("plant_ball_from_sugar_cane").duration(300).EUt(2).inputItems(new ItemStack(Items.SUGAR_CANE, 8)).outputItems(PLANT_BALL).save(provider);
        COMPRESSOR_RECIPES.recipeBuilder("plant_ball_from_brown_mushroom").duration(300).EUt(2).inputItems(new ItemStack(Blocks.BROWN_MUSHROOM, 8)).outputItems(PLANT_BALL).save(provider);
        COMPRESSOR_RECIPES.recipeBuilder("plant_ball_from_red_mushroom").duration(300).EUt(2).inputItems(new ItemStack(Blocks.RED_MUSHROOM, 8)).outputItems(PLANT_BALL).save(provider);
        COMPRESSOR_RECIPES.recipeBuilder("plant_ball_from_beetroot").duration(300).EUt(2).inputItems(new ItemStack(Items.BEETROOT, 8)).outputItems(PLANT_BALL).save(provider);
        COMPRESSOR_RECIPES.recipeBuilder("plant_ball_from_moss").duration(300).EUt(2).inputItems(new ItemStack(Items.MOSS_BLOCK, 8)).outputItems(PLANT_BALL).save(provider);
        COMPRESSOR_RECIPES.recipeBuilder("plant_ball_from_nether_wart").duration(300).EUt(2).inputItems(new ItemStack(Items.NETHER_WART_BLOCK, 8)).outputItems(PLANT_BALL).save(provider);
        COMPRESSOR_RECIPES.recipeBuilder("plant_ball_from_crimson_stem").duration(300).EUt(2).inputItems(new ItemStack(Items.CRIMSON_STEM, 8)).outputItems(PLANT_BALL).save(provider);
        COMPRESSOR_RECIPES.recipeBuilder("plant_ball_from_warped_stem").duration(300).EUt(2).inputItems(new ItemStack(Items.WARPED_STEM, 8)).outputItems(PLANT_BALL).save(provider);
        COMPRESSOR_RECIPES.recipeBuilder("plant_ball_from_brain_coral").duration(300).EUt(2).inputItems(new ItemStack(Items.BRAIN_CORAL, 8)).outputItems(PLANT_BALL).save(provider);
        COMPRESSOR_RECIPES.recipeBuilder("plant_ball_from_bubble_coral").duration(300).EUt(2).inputItems(new ItemStack(Items.BUBBLE_CORAL, 8)).outputItems(PLANT_BALL).save(provider);
        COMPRESSOR_RECIPES.recipeBuilder("plant_ball_from_fire_coral").duration(300).EUt(2).inputItems(new ItemStack(Items.FIRE_CORAL, 8)).outputItems(PLANT_BALL).save(provider);
        COMPRESSOR_RECIPES.recipeBuilder("plant_ball_from_tube_coral").duration(300).EUt(2).inputItems(new ItemStack(Items.TUBE_CORAL, 8)).outputItems(PLANT_BALL).save(provider);
        COMPRESSOR_RECIPES.recipeBuilder("plant_ball_from_horn_coral").duration(300).EUt(2).inputItems(new ItemStack(Items.HORN_CORAL, 8)).outputItems(PLANT_BALL).save(provider);
    }

    private static void registerRecyclingRecipes(Consumer<FinishedRecipe> provider) {

        MACERATOR_RECIPES.recipeBuilder("macerate_end_stone")
                .inputItems(new ItemStack(Blocks.END_STONE))
                .outputItems(dust, Endstone)
                .chancedOutput(dustTiny, Tungstate, 1200, 280)
                .duration(150).EUt(2)
                .save(provider);

        MACERATOR_RECIPES.recipeBuilder("macerate_netherrack")
                .inputItems(new ItemStack(Blocks.NETHERRACK))
                .outputItems(dust, Netherrack)
                .chancedOutput(nugget, Gold, 500, 120)
                .duration(150).EUt(2)
                .save(provider);

        // TODO Stone-type tags?
        //if (!OreDictionary.getOres("stoneSoapstone").isEmpty())
        //    MACERATOR_RECIPES.recipeBuilder()
        //            .inputItems(stone, Soapstone)
        //            .outputItems(dustImpure, Talc)
        //            .chancedOutput(dustTiny, Chromite, 1000, 280)
        //            .duration(150).EUt(2)
        //            .save(provider);

        //if (!OreDictionary.getOres("stoneRedrock").isEmpty())
        //    MACERATOR_RECIPES.recipeBuilder()
        //            .inputItems(stone, Redrock)
        //            .outputItems(dust, Redrock)
        //            .chancedOutput(dust, Redrock, 1000, 380)
        //            .duration(150).EUt(2)
        //            .save(provider);

//        MACERATOR_RECIPES.recipeBuilder("macerate_marble")
//                .inputItems(block, Marble)
//                .outputItems(dust, Marble)
//                .chancedOutput(dust, Marble, 1000, 380)
//                .duration(150).EUt(2)
//                .save(provider);

        MACERATOR_RECIPES.recipeBuilder("macerate_basalt")
                .inputItems(Blocks.BASALT.asItem())
                .outputItems(dust, Basalt)
                .chancedOutput(dust, Basalt, 1000, 380)
                .duration(150).EUt(2)
                .save(provider);

        MACERATOR_RECIPES.recipeBuilder("macerate_deepslate")
                .inputItems(Blocks.DEEPSLATE.asItem())
                .outputItems(dust, Deepslate)
                .chancedOutput(dust, Thorium, 100, 40)
                .duration(150).EUt(2)
                .save(provider);

//        MACERATOR_RECIPES.recipeBuilder("macerate_red_granite")
//                .inputItems(block, GraniteRed)
//                .outputItems(dust, GraniteRed)
//                .chancedOutput(dustSmall, Uranium238, 100, 40)
//                .duration(150).EUt(2)
//                .save(provider);

        MACERATOR_RECIPES.recipeBuilder("macerate_andesite")
                .inputItems(Blocks.ANDESITE.asItem())
                .outputItems(dust, Andesite)
                .chancedOutput(dustSmall, Stone, 100, 40)
                .duration(150).EUt(2)
                .save(provider);

        MACERATOR_RECIPES.recipeBuilder("macerate_diorite")
                .inputItems(Blocks.DIORITE.asItem())
                .outputItems(dust, Diorite)
                .chancedOutput(dustSmall, Stone, 100, 40)
                .duration(150).EUt(2)
                .save(provider);

        MACERATOR_RECIPES.recipeBuilder("macerate_granite")
                .inputItems(Blocks.GRANITE.asItem())
                .outputItems(dust, Granite)
                .chancedOutput(dustSmall, Stone, 100, 40)
                .duration(150).EUt(2)
                .save(provider);

        // TODO add other foods
        MACERATOR_RECIPES.recipeBuilder("macerate_pork_chop")
                .inputItems(new ItemStack(Items.PORKCHOP))
                .outputItems(dustSmall, Meat, 6)
                .outputItems(dustTiny, Bone)
                .duration(102).EUt(2).save(provider);

        // TODO all fish
        //MACERATOR_RECIPES.recipeBuilder("macerate_fish")
        //        .inputItems(new ItemStack(Items.FISH, 1, GTValues.W))
        //        .outputItems(dustSmall, Meat, 6)
        //        .outputItems(dustTiny, Bone)
        //        .duration(102).EUt(2).save(provider);

        MACERATOR_RECIPES.recipeBuilder("macerate_chicken")
                .inputItems(new ItemStack(Items.CHICKEN))
                .outputItems(dust, Meat)
                .outputItems(dustTiny, Bone)
                .duration(102).EUt(2).save(provider);

        MACERATOR_RECIPES.recipeBuilder("macerate_steak")
                .inputItems(new ItemStack(Items.BEEF))
                .outputItems(dustSmall, Meat, 6)
                .outputItems(dustTiny, Bone)
                .duration(102).EUt(2).save(provider);

        MACERATOR_RECIPES.recipeBuilder("macerate_rabbit")
                .inputItems(new ItemStack(Items.RABBIT))
                .outputItems(dustSmall, Meat, 6)
                .outputItems(dustTiny, Bone)
                .duration(102).EUt(2).save(provider);

        MACERATOR_RECIPES.recipeBuilder("macerate_mutton")
                .inputItems(new ItemStack(Items.MUTTON))
                .outputItems(dust, Meat)
                .outputItems(dustTiny, Bone)
                .duration(102).EUt(2).save(provider);
    }

    private static void registerFluidRecipes(Consumer<FinishedRecipe> provider) {

        FLUID_HEATER_RECIPES.recipeBuilder("heat_ice_to_water").duration(32).EUt(4)
                .inputFluids(Ice.getFluid(L))
                .circuitMeta(1)
                .outputFluids(Water.getFluid(L)).save(provider);

        FLUID_SOLIDFICATION_RECIPES.recipeBuilder("solidify_gelled_toluene")
                .inputFluids(Toluene.getFluid(100))
                .notConsumable(SHAPE_MOLD_BALL)
                .outputItems(GELLED_TOLUENE)
                .duration(100).EUt(16).save(provider);

        for (int i = 0; i < CHEMICAL_DYES.length; i++) {
            FLUID_SOLIDFICATION_RECIPES.recipeBuilder("solidify_" + CHEMICAL_DYES[i].getName() + "_to_ball")
                    .inputFluids(CHEMICAL_DYES[i].getFluid(L / 2))
                    .notConsumable(SHAPE_MOLD_BALL)
                    .outputItems(DYE_ONLY_ITEMS[i])
                    .duration(100).EUt(16).save(provider);
        }

        FLUID_HEATER_RECIPES.recipeBuilder("heat_water_to_steam").duration(30).EUt(VA[LV]).inputFluids(Water.getFluid(6)).circuitMeta(1).outputFluids(Steam.getFluid(960)).save(provider);
        FLUID_HEATER_RECIPES.recipeBuilder("heat_distilled_to_steam").duration(30).EUt(VA[LV]).inputFluids(DistilledWater.getFluid(6)).circuitMeta(1).outputFluids(Steam.getFluid(960)).save(provider);
    }

    // TODO stone types
    /*
    private static void registerSmoothRecipe(Consumer<FinishedRecipe> provider, List<ItemStack> roughStack, List<ItemStack> smoothStack) {
        for (int i = 0; i < roughStack.size(); i++) {
            ModHandler.addSmeltingRecipe(roughStack.get(i), smoothStack.get(i), 0.1f);

            EXTRUDER_RECIPES.recipeBuilder()
                    .inputItems(roughStack.get(i))
                    .notConsumable(SHAPE_EXTRUDER_BLOCK)
                    .outputItems(smoothStack.get(i))
                    .duration(24).EUt(8).save(provider);
        }
    }

    private static void registerCobbleRecipe(Consumer<FinishedRecipe> provider, List<ItemStack> smoothStack, List<ItemStack> cobbleStack) {
        for (int i = 0; i < smoothStack.size(); i++) {
            FORGE_HAMMER_RECIPES.recipeBuilder()
                    .inputItems(smoothStack.get(i))
                    .outputItems(cobbleStack.get(i))
                    .duration(12).EUt(4).save(provider);
        }
    }

    private static void registerBricksRecipe(Consumer<FinishedRecipe> provider, List<ItemStack> polishedStack, List<ItemStack> brickStack, MarkerMaterial color) {
        for (int i = 0; i < polishedStack.size(); i++) {
            LASER_ENGRAVER_RECIPES.recipeBuilder()
                    .inputItems(polishedStack.get(i))
                    .notConsumable(craftingLens, color)
                    .outputItems(brickStack.get(i))
                    .duration(50).EUt(16).save(provider);
        }
    }

    private static void registerMossRecipe(Consumer<FinishedRecipe> provider, List<ItemStack> regularStack, List<ItemStack> mossStack) {
        for (int i = 0; i < regularStack.size(); i++) {
            CHEMICAL_BATH_RECIPES.recipeBuilder()
                    .inputItems(regularStack.get(i))
                    .inputFluids(Water.getFluid(100))
                    .outputItems(mossStack.get(i))
                    .duration(50).EUt(16).save(provider);
        }
    }
     */

    // TODO NBT removal recipes
    private static void registerNBTRemoval(Consumer<FinishedRecipe> provider) {
        /*
        for (MetaTileEntityQuantumChest chest : MetaTileEntities.QUANTUM_CHEST)
            if (chest != null) {
                ModHandler.addShapelessNBTClearingRecipe("quantum_chest_nbt_" + chest.getTier() + chest.getMetaName(), chest, chest);
            }

        for (MetaTileEntityQuantumTank tank : MetaTileEntities.QUANTUM_TANK)
            if (tank != null) {
                ModHandler.addShapelessNBTClearingRecipe("quantum_tank_nbt_" + tank.getTier() + tank.getMetaName(), tank, tank);
            }

        //Drums
        ModHandler.addShapelessNBTClearingRecipe("drum_nbt_wood", MetaTileEntities.WOODEN_DRUM, MetaTileEntities.WOODEN_DRUM);
        ModHandler.addShapelessNBTClearingRecipe("drum_nbt_bronze", MetaTileEntities.BRONZE_DRUM, MetaTileEntities.BRONZE_DRUM);
        ModHandler.addShapelessNBTClearingRecipe("drum_nbt_steel", MetaTileEntities.STEEL_DRUM, MetaTileEntities.STEEL_DRUM);
        ModHandler.addShapelessNBTClearingRecipe("drum_nbt_aluminium", MetaTileEntities.ALUMINIUM_DRUM, MetaTileEntities.ALUMINIUM_DRUM);
        ModHandler.addShapelessNBTClearingRecipe("drum_nbt_stainless_steel", MetaTileEntities.STAINLESS_STEEL_DRUM, MetaTileEntities.STAINLESS_STEEL_DRUM);
        ModHandler.addShapelessNBTClearingRecipe("drum_nbt_gold", MetaTileEntities.GOLD_DRUM, MetaTileEntities.GOLD_DRUM);
        ModHandler.addShapelessNBTClearingRecipe("drum_nbt_titanium", MetaTileEntities.TITANIUM_DRUM, MetaTileEntities.TITANIUM_DRUM);
        ModHandler.addShapelessNBTClearingRecipe("drum_nbt_tungstensteel", MetaTileEntities.TUNGSTENSTEEL_DRUM, MetaTileEntities.TUNGSTENSTEEL_DRUM);

        // Cells
        ModHandler.addShapedNBTClearingRecipe("cell_nbt_regular", FLUID_CELL, " C", "  ", 'C', FLUID_CELL);
        ModHandler.addShapedNBTClearingRecipe("cell_nbt_universal", FLUID_CELL_UNIVERSAL, " C", "  ", 'C', FLUID_CELL_UNIVERSAL);
        ModHandler.addShapelessNBTClearingRecipe("cell_nbt_steel", FLUID_CELL_LARGE_STEEL, FLUID_CELL_LARGE_STEEL);
        ModHandler.addShapelessNBTClearingRecipe("cell_nbt_aluminium", FLUID_CELL_LARGE_ALUMINIUM, FLUID_CELL_LARGE_ALUMINIUM);
        ModHandler.addShapelessNBTClearingRecipe("cell_nbt_stainless_steel", FLUID_CELL_LARGE_STAINLESS_STEEL, FLUID_CELL_LARGE_STAINLESS_STEEL);
        ModHandler.addShapelessNBTClearingRecipe("cell_nbt_titanium", FLUID_CELL_LARGE_TITANIUM, FLUID_CELL_LARGE_TITANIUM);
        ModHandler.addShapelessNBTClearingRecipe("cell_nbt_tungstensteel", FLUID_CELL_LARGE_TUNGSTEN_STEEL, FLUID_CELL_LARGE_TUNGSTEN_STEEL);
        ModHandler.addShapelessNBTClearingRecipe("cell_vial_nbt", FLUID_CELL_GLASS_VIAL, FLUID_CELL_GLASS_VIAL);


        //Jetpacks
        // TODO armor
        //ModHandler.addShapelessRecipe("fluid_jetpack_clear", SEMIFLUID_JETPACK, SEMIFLUID_JETPACK);
        */
    }

    private static void registerHatchConversion(Consumer<FinishedRecipe> provider) {
        for (int i = 0; i < FLUID_IMPORT_HATCH.length; i++) {
            if (FLUID_IMPORT_HATCH[i] != null && FLUID_EXPORT_HATCH[i] != null) {

                VanillaRecipeHelper.addShapedRecipe(provider, "fluid_hatch_output_to_input_" + FLUID_IMPORT_HATCH[i].getTier(), FLUID_IMPORT_HATCH[i].asStack(),
                        "d", "B", 'B', FLUID_EXPORT_HATCH[i].asStack());
                VanillaRecipeHelper.addShapedRecipe(provider, "fluid_hatch_input_to_output_" + FLUID_EXPORT_HATCH[i].getTier(), FLUID_EXPORT_HATCH[i].asStack(),
                        "d", "B", 'B', FLUID_IMPORT_HATCH[i].asStack());
            }
        }
        for (int i = 0; i < ITEM_IMPORT_BUS.length; i++) {
            if (ITEM_IMPORT_BUS[i] != null && ITEM_EXPORT_BUS[i] != null) {

                VanillaRecipeHelper.addShapedRecipe(provider, "item_bus_output_to_input_" + ITEM_IMPORT_BUS[i].getTier(), ITEM_IMPORT_BUS[i].asStack(),
                        "d", "B", 'B', ITEM_EXPORT_BUS[i].asStack());
                VanillaRecipeHelper.addShapedRecipe(provider, "item_bus_input_to_output_" + ITEM_EXPORT_BUS[i].getTier(), ITEM_EXPORT_BUS[i].asStack(),
                        "d", "B", 'B', ITEM_IMPORT_BUS[i].asStack());
            }
        }

        for (int tier : MULTI_HATCH_TIERS) {
            var tierName = VN[tier].toLowerCase();

            var importHatch4x = FLUID_IMPORT_HATCH_4X[tier];
            var exportHatch4x = FLUID_EXPORT_HATCH_4X[tier];
            var importHatch9x = FLUID_IMPORT_HATCH_9X[tier];
            var exportHatch9x = FLUID_EXPORT_HATCH_9X[tier];

            VanillaRecipeHelper.addShapedRecipe(
                    provider, "fluid_hatch_4x_output_to_input_" + tierName,
                    importHatch4x.asStack(), "d", "B",
                    'B', exportHatch4x.asStack()
            );
            VanillaRecipeHelper.addShapedRecipe(
                    provider, "fluid_hatch_4x_input_to_output_" + tierName,
                    exportHatch4x.asStack(), "d", "B",
                    'B', importHatch4x.asStack()
            );

            VanillaRecipeHelper.addShapedRecipe(
                    provider, "fluid_hatch_9x_output_to_input_" + tierName,
                    importHatch9x.asStack(), "d", "B",
                    'B', exportHatch9x.asStack()
            );
            VanillaRecipeHelper.addShapedRecipe(
                    provider, "fluid_hatch_9x_input_to_output_" + tierName,
                    exportHatch9x.asStack(), "d", "B",
                    'B', importHatch9x.asStack()
            );
        }

        //Steam
        VanillaRecipeHelper.addShapedRecipe(provider, "steam_bus_output_to_input", STEAM_EXPORT_BUS.asStack(),
                "d", "B", 'B', STEAM_IMPORT_BUS.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, "steam_bus_input_to_output", STEAM_IMPORT_BUS.asStack(),
                "d", "B", 'B', STEAM_EXPORT_BUS.asStack());

        if (GTCEu.isAE2Loaded()) {
            VanillaRecipeHelper.addShapedRecipe(provider, "me_fluid_hatch_output_to_input", GTAEMachines.FLUID_IMPORT_HATCH.asStack(), "d", "B", 'B', GTAEMachines.FLUID_EXPORT_HATCH.asStack());
            VanillaRecipeHelper.addShapedRecipe(provider, "me_fluid_hatch_input_to_output", GTAEMachines.FLUID_EXPORT_HATCH.asStack(), "d", "B", 'B', GTAEMachines.FLUID_IMPORT_HATCH.asStack());
            VanillaRecipeHelper.addShapedRecipe(provider, "me_item_bus_output_to_input", GTAEMachines.ITEM_IMPORT_BUS.asStack(), "d", "B", 'B', GTAEMachines.ITEM_EXPORT_BUS.asStack());
            VanillaRecipeHelper.addShapedRecipe(provider, "me_item_bus_input_to_output", GTAEMachines.ITEM_EXPORT_BUS.asStack(), "d", "B", 'B', GTAEMachines.ITEM_IMPORT_BUS.asStack());
        }
    }
}
