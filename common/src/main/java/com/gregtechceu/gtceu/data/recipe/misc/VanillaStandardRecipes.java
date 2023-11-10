package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTItems.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public class VanillaStandardRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        compressingRecipes(provider);
        glassRecipes(provider);
        smashingRecipes(provider);
        woodRecipes(provider);
        cuttingRecipes(provider);
        dyingCleaningRecipes(provider);
        redstoneRecipes(provider);
        metalRecipes(provider);
        miscRecipes(provider);
        mixingRecipes(provider);
        dyeRecipes(provider);
    }

    /**
     * + Adds compression recipes for vanilla items
     */
    private static void compressingRecipes(Consumer<FinishedRecipe> provider) {
        COMPRESSOR_RECIPES.recipeBuilder("stone_from_dust").duration(300).EUt(2)
                .inputItems(plate, Stone, 9)
                .outputItems(new ItemStack(Blocks.STONE))
                .save(provider);

        //todo autogenerate 2x2 recipes?
        COMPRESSOR_RECIPES.recipeBuilder("sandstone").duration(300).EUt(2)
                .inputItems(new ItemStack(Blocks.SAND, 4))
                .outputItems(new ItemStack(Blocks.SANDSTONE))
                .save(provider);

        COMPRESSOR_RECIPES.recipeBuilder("red_sandstone").duration(300).EUt(2)
                .inputItems(new ItemStack(Blocks.RED_SAND))
                .outputItems(new ItemStack(Blocks.RED_SANDSTONE))
                .save(provider);

        COMPRESSOR_RECIPES.recipeBuilder("bricks").duration(300).EUt(2)
                .inputItems(new ItemStack(Items.BRICK, 4))
                .outputItems(new ItemStack(Blocks.BRICKS))
                .save(provider);

        COMPRESSOR_RECIPES.recipeBuilder("nether_bricks").duration(300).EUt(2)
                .inputItems(new ItemStack(Items.NETHER_BRICK, 4))
                .outputItems(new ItemStack(Blocks.NETHER_BRICKS))
                .save(provider);

        COMPRESSOR_RECIPES.recipeBuilder("ice_from_snow").duration(300).EUt(2)
                .inputItems(new ItemStack(Blocks.SNOW))
                .outputItems(new ItemStack(Blocks.ICE))
                .save(provider);

        COMPRESSOR_RECIPES.recipeBuilder("clay").duration(300).EUt(2)
                .inputItems(new ItemStack(Items.CLAY_BALL, 4))
                .outputItems(new ItemStack(Blocks.CLAY))
                .save(provider);

        COMPRESSOR_RECIPES.recipeBuilder("glowstone").duration(300).EUt(2)
                .inputItems(new ItemStack(Items.GLOWSTONE_DUST, 4))
                .outputItems(new ItemStack(Blocks.GLOWSTONE))
                .save(provider);

        COMPRESSOR_RECIPES.recipeBuilder("packed_ice").inputItems(new ItemStack(Blocks.ICE, 9)).outputItems(new ItemStack(Blocks.PACKED_ICE)).save(provider);
        COMPRESSOR_RECIPES.recipeBuilder("blue_ice").inputItems(new ItemStack(Blocks.PACKED_ICE, 9)).outputItems(new ItemStack(Blocks.BLUE_ICE)).save(provider);
        COMPRESSOR_RECIPES.recipeBuilder("ice_from_dust").inputItems(dust, Ice).outputItems(new ItemStack(Blocks.ICE)).save(provider);

        PACKER_RECIPES.recipeBuilder("hay_block")
                .inputItems(new ItemStack(Items.WHEAT, 9))
                .circuitMeta(9)
                .outputItems(new ItemStack(Blocks.HAY_BLOCK))
                .duration(200).EUt(2)
                .save(provider);

        PACKER_RECIPES.recipeBuilder("melon")
                .inputItems(new ItemStack(Items.MELON, 9))
                .circuitMeta(9)
                .outputItems(new ItemStack(Blocks.MELON))
                .duration(200).EUt(2)
                .save(provider);
    }

    /**
     * + Adds new glass related recipes
     * + Adds steam age manual glass recipes
     * - Removes some glass related recipes based on configs
     */
    private static void glassRecipes(Consumer<FinishedRecipe> provider) {
        VanillaRecipeHelper.addShapedRecipe(provider, "glass_dust_hammer", ChemicalHelper.get(dust, Glass), "hG", 'G', new ItemStack(Blocks.GLASS));

        VanillaRecipeHelper.addShapedRecipe(provider, "quartz_sand", ChemicalHelper.get(dust, QuartzSand), "S", "m", 'S', new ItemStack(Blocks.SAND));

        MACERATOR_RECIPES.recipeBuilder("quartz_sand_from_sand")
                .inputItems(new ItemStack(Blocks.SAND))
                .outputItems(dust, QuartzSand)
                .duration(30).EUt(2).save(provider);

        VanillaRecipeHelper.addShapelessRecipe(provider, "glass_dust_flint", ChemicalHelper.get(dust, Glass),
                new UnificationEntry(dust, QuartzSand),
                new UnificationEntry(dustTiny, Flint));

        MIXER_RECIPES.recipeBuilder("glass_from_quartzite").duration(160).EUt(VA[ULV])
                .inputItems(dustSmall, Flint)
                .inputItems(dust, Quartzite, 4)
                .outputItems(dust, Glass, 5)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("glass_from_quartz_sand").duration(200).EUt(VA[ULV])
                .inputItems(dustSmall, Flint)
                .inputItems(dust, QuartzSand, 4)
                .outputItems(dust, Glass, 4)
                .save(provider);

        ARC_FURNACE_RECIPES.recipeBuilder("glass_from_sand").duration(20).EUt(VA[LV])
                .inputItems(new ItemStack(Blocks.SAND))
                .outputItems(new ItemStack(Blocks.GLASS, 2))
                .save(provider);

        FORMING_PRESS_RECIPES.recipeBuilder("form_glass").duration(80).EUt(VA[LV])
                .inputItems(dust, Glass)
                .notConsumable(SHAPE_MOLD_BLOCK)
                .outputItems(new ItemStack(Blocks.GLASS))
                .save(provider);

        ALLOY_SMELTER_RECIPES.recipeBuilder("glass_bottle").duration(64).EUt(4)
                .inputItems(dust, Glass)
                .notConsumable(SHAPE_MOLD_BOTTLE)
                .outputItems(new ItemStack(Items.GLASS_BOTTLE))
                .save(provider);

        EXTRUDER_RECIPES.recipeBuilder("glass_bottle").duration(32).EUt(16)
                .inputItems(dust, Glass)
                .notConsumable(SHAPE_EXTRUDER_BOTTLE)
                .outputItems(new ItemStack(Items.GLASS_BOTTLE))
                .save(provider);

        FLUID_SOLIDFICATION_RECIPES.recipeBuilder("glass_bottle").duration(12).EUt(4)
                .inputFluids(Glass.getFluid(L))
                .notConsumable(SHAPE_MOLD_BOTTLE)
                .outputItems(new ItemStack(Items.GLASS_BOTTLE))
                .save(provider);

        FLUID_SOLIDFICATION_RECIPES.recipeBuilder("glass").duration(12).EUt(4)
                .inputFluids(Glass.getFluid(L))
                .notConsumable(SHAPE_MOLD_BLOCK)
                .outputItems(new ItemStack(Blocks.GLASS))
                .save(provider);

        ALLOY_SMELTER_RECIPES.recipeBuilder("glass").duration(120).EUt(16)
                .inputItems(dust, Glass)
                .notConsumable(SHAPE_MOLD_BLOCK)
                .outputItems(new ItemStack(Blocks.GLASS, 1))
                .save(provider);

        //for (int i = 0; i < 16; i++) {

            //ModHandler.addShapedRecipe("stained_glass_pane_" + i, new ItemStack(Blocks.STAINED_GLASS_PANE, 2, i), "sG", 'G', new ItemStack(Blocks.STAINED_GLASS, 1, i));

            //CUTTER_RECIPES.recipeBuilder().duration(50).EUt(VA[ULV])
            //        .inputItems(new ItemStack(Blocks.STAINED_GLASS, 3, i))
            //        .outputItems(new ItemStack(Blocks.STAINED_GLASS_PANE, 8, i))
            //        .save(provider);
        //}

        VanillaRecipeHelper.addShapedRecipe(provider, "glass_pane", new ItemStack(Blocks.GLASS_PANE, 2), "sG", 'G', new ItemStack(Blocks.GLASS));

        CUTTER_RECIPES.recipeBuilder("cut_glass_panes").duration(50).EUt(VA[ULV])
                .inputItems(new ItemStack(Blocks.GLASS, 3))
                .outputItems(new ItemStack(Blocks.GLASS_PANE, 8))
                .save(provider);
    }

    /**
     * Adds smashing related recipes for vanilla blocks and items
     */
    private static void smashingRecipes(Consumer<FinishedRecipe> provider) {
        VanillaRecipeHelper.addShapedRecipe(provider, "cobblestone_hammer", new ItemStack(Blocks.COBBLESTONE), "h", "C", 'C', new ItemStack(Blocks.STONE));
        VanillaRecipeHelper.addShapedRecipe(provider, "cobbled_deepslate_hammer", new ItemStack(Blocks.COBBLED_DEEPSLATE), "h", "C", 'C', new ItemStack(Blocks.DEEPSLATE));

        FORGE_HAMMER_RECIPES.recipeBuilder("stone_to_cobblestone")
                .inputItems(new ItemStack(Blocks.STONE))
                .outputItems(new ItemStack(Blocks.COBBLESTONE))
                .EUt(16).duration(10)
                .save(provider);

        FORGE_HAMMER_RECIPES.recipeBuilder("deepslate_to_cobbled_deepslate")
                .inputItems(new ItemStack(Blocks.DEEPSLATE))
                .outputItems(new ItemStack(Blocks.COBBLED_DEEPSLATE))
                .EUt(16).duration(10)
                .save(provider);

        FORGE_HAMMER_RECIPES.recipeBuilder("cobblestone_to_gravel")
                .inputItems(ItemTags.STONE_CRAFTING_MATERIALS)
                .outputItems(new ItemStack(Blocks.GRAVEL))
                .EUt(16).duration(10)
                .save(provider);

        FORGE_HAMMER_RECIPES.recipeBuilder("gravel_to_sand")
                .inputItems(new ItemStack(Blocks.GRAVEL))
                .outputItems(new ItemStack(Blocks.SAND))
                .EUt(16).duration(10)
                .save(provider);

        MACERATOR_RECIPES.recipeBuilder("gravel_to_flint")
                .inputItems(new ItemStack(Blocks.GRAVEL, 1))
                .outputItems(dust, Stone)
                .chancedOutput(new ItemStack(Items.FLINT), 1000, 1000)
                .duration(400).EUt(2)
                .save(provider);

        // todo other sandstone types?
        FORGE_HAMMER_RECIPES.recipeBuilder("sandstone_to_sand")
                .inputItems(new ItemStack(Blocks.SANDSTONE))
                .outputItems(new ItemStack(Blocks.SAND))
                .EUt(2).duration(400).save(provider);

        FORGE_HAMMER_RECIPES.recipeBuilder("smooth_sandstone_to_sand")
                .inputItems(new ItemStack(Blocks.SMOOTH_SANDSTONE))
                .outputItems(new ItemStack(Blocks.SAND))
                .EUt(2).duration(400).save(provider);

        FORGE_HAMMER_RECIPES.recipeBuilder("chiseled_sandstone_to_sand")
                .inputItems(new ItemStack(Blocks.CHISELED_SANDSTONE))
                .outputItems(new ItemStack(Blocks.SAND))
                .EUt(2).duration(400).save(provider);

        FORGE_HAMMER_RECIPES.recipeBuilder("red_sandstone_to_red_sand")
                .inputItems(new ItemStack(Blocks.RED_SANDSTONE))
                .outputItems(new ItemStack(Blocks.RED_SAND))
                .EUt(2).duration(400).save(provider);

        FORGE_HAMMER_RECIPES.recipeBuilder("smooth_red_sandstone_to_red_sand")
                .inputItems(new ItemStack(Blocks.SMOOTH_RED_SANDSTONE))
                .outputItems(new ItemStack(Blocks.RED_SAND))
                .EUt(2).duration(400).save(provider);

        FORGE_HAMMER_RECIPES.recipeBuilder("chiseled_red_sandstone_to_red_sand")
                .inputItems(new ItemStack(Blocks.CHISELED_RED_SANDSTONE))
                .outputItems(new ItemStack(Blocks.RED_SAND))
                .EUt(2).duration(400).save(provider);

        FORGE_HAMMER_RECIPES.recipeBuilder("cracked_stone_bricks")
                .inputItems(new ItemStack(Blocks.STONE_BRICKS))
                .outputItems(new ItemStack(Blocks.CRACKED_STONE_BRICKS))
                .EUt(2).duration(400).save(provider);

        VanillaRecipeHelper.addShapelessRecipe(provider, "clay_block_to_dust", ChemicalHelper.get(dust, Clay), 'm', Blocks.CLAY);
        VanillaRecipeHelper.addShapelessRecipe(provider, "clay_ball_to_dust", ChemicalHelper.get(dustSmall, Clay), 'm', Items.CLAY_BALL);
        VanillaRecipeHelper.addShapelessRecipe(provider, "brick_block_to_dust", ChemicalHelper.get(dust, Brick), 'm', Blocks.BRICKS);
        VanillaRecipeHelper.addShapelessRecipe(provider, "brick_to_dust", ChemicalHelper.get(dustSmall, Brick), 'm', Items.BRICK);
        VanillaRecipeHelper.addShapelessRecipe(provider, "wheat_to_dust", ChemicalHelper.get(dust, Wheat), 'm', Items.WHEAT);
        VanillaRecipeHelper.addShapelessRecipe(provider, "gravel_to_flint", new ItemStack(Items.FLINT), 'm', Blocks.GRAVEL);
        VanillaRecipeHelper.addShapelessRecipe(provider, "bone_to_bone_meal", new ItemStack(Items.BONE_MEAL), 'm', Items.BONE);
        VanillaRecipeHelper.addShapelessRecipe(provider, "blaze_rod_to_powder", new ItemStack(Items.BLAZE_POWDER, 3), 'm', Items.BLAZE_ROD);

        MACERATOR_RECIPES.recipeBuilder("macerate_cocoa")
                .inputItems(new ItemStack(Items.COCOA_BEANS))
                .outputItems(dust, Cocoa)
                .duration(400).EUt(2)
                .save(provider);

        MACERATOR_RECIPES.recipeBuilder("macerate_sugar_cane")
                .inputItems(new ItemStack(Items.SUGAR_CANE))
                .outputItems(new ItemStack(Items.SUGAR))
                .duration(400).EUt(2)
                .save(provider);

        MACERATOR_RECIPES.recipeBuilder("macerate_melon_block")
                .inputItems(new ItemStack(Blocks.MELON))
                .outputItems(new ItemStack(Items.MELON_SLICE, 8))
                .chancedOutput(new ItemStack(Items.MELON_SEEDS), 8000, 500)
                .duration(400).EUt(2)
                .save(provider);

        MACERATOR_RECIPES.recipeBuilder("macerate_pumpkin")
                .inputItems(new ItemStack(Blocks.PUMPKIN))
                .outputItems(new ItemStack(Items.PUMPKIN_SEEDS, 4))
                .duration(400).EUt(2)
                .save(provider);

        MACERATOR_RECIPES.recipeBuilder("macerate_melon_slice")
                .inputItems(new ItemStack(Items.MELON))
                .outputItems(new ItemStack(Items.MELON_SEEDS))
                .duration(400).EUt(2)
                .save(provider);

        MACERATOR_RECIPES.recipeBuilder("macerate_wool")
                .inputItems(ItemTags.WOOL)
                .outputItems(new ItemStack(Items.STRING))
                .chancedOutput(new ItemStack(Items.STRING), 9000, 0)
                .chancedOutput(new ItemStack(Items.STRING), 5000, 0)
                .chancedOutput(new ItemStack(Items.STRING), 2000, 0)
                .duration(200).EUt(2)
                .save(provider);
    }

    /**
     * + Adds new recipes for wood related items and blocks
     */
    private static void woodRecipes(Consumer<FinishedRecipe> provider) {
        MACERATOR_RECIPES.recipeBuilder("macerate_logs")
                .inputItems(ItemTags.LOGS)
                .outputItems(dust, Wood, 6)
                .chancedOutput(dust, Wood, 8000, 680)
                .duration(150).EUt(2)
                .save(provider);

        LATHE_RECIPES.recipeBuilder("lathe_planks")
                .inputItems(ItemTags.PLANKS)
                .outputItems(new ItemStack(Items.STICK, 2))
                .duration(10).EUt(VA[ULV])
                .save(provider);

        LATHE_RECIPES.recipeBuilder("lathe_logs")
                .inputItems(ItemTags.LOGS)
                .outputItems(rodLong, Wood, 4)
                .outputItems(dust, Wood, 2)
                .duration(160).EUt(VA[ULV])
                .save(provider);

        LATHE_RECIPES.recipeBuilder("lathe_saplings")
                .inputItems(ItemTags.SAPLINGS)
                .outputItems(new ItemStack(Items.STICK))
                .outputItems(dustTiny, Wood)
                .duration(16).EUt(VA[ULV])
                .save(provider);

        LATHE_RECIPES.recipeBuilder("lathe_wood_slabs")
                .inputItems(ItemTags.WOODEN_SLABS)
                .outputItems(new ItemStack(Items.BOWL))
                .outputItems(dustSmall, Wood)
                .duration(50).EUt(VA[ULV])
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("bookshelf")
                .inputItems(ItemTags.PLANKS, 6)
                .inputItems(new ItemStack(Items.BOOK, 3))
                .outputItems(new ItemStack(Blocks.BOOKSHELF))
                .duration(100).EUt(4)
                .save(provider);

        // todo trapdoors
        //ASSEMBLER_RECIPES.recipeBuilder()
        //        .inputItems(ItemTags.PLANKS, 3).circuitMeta(3)
        //        .outputItems(new ItemStack(Blocks.TRAPDOOR, 2))
        //        .duration(100).EUt(4)
        //        .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("chest")
                .inputItems(ItemTags.PLANKS, 8)
                .outputItems(new ItemStack(Blocks.CHEST))
                .duration(100).EUt(4).circuitMeta(8)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("torch_coal")
                .inputItems(ItemTags.COALS)
                .inputItems(new ItemStack(Items.STICK))
                .outputItems(new ItemStack(Blocks.TORCH, 4))
                .duration(100).EUt(1).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("torch_coal_dust")
                .inputItems(dust, Coal)
                .inputItems(new ItemStack(Items.STICK))
                .outputItems(new ItemStack(Blocks.TORCH, 4))
                .duration(100).EUt(1).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("torch_charcoal_dust")
                .inputItems(dust, Charcoal)
                .inputItems(new ItemStack(Items.STICK))
                .outputItems(new ItemStack(Blocks.TORCH, 4))
                .duration(100).EUt(1).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("torch_coke_gem")
                .inputItems(gem, Coke)
                .inputItems(new ItemStack(Items.STICK))
                .outputItems(new ItemStack(Blocks.TORCH, 8))
                .duration(100).EUt(1).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("torch_coke_dust")
                .inputItems(dust, Coke)
                .inputItems(new ItemStack(Items.STICK))
                .outputItems(new ItemStack(Blocks.TORCH, 8))
                .duration(100).EUt(1).save(provider);


        ASSEMBLER_RECIPES.recipeBuilder("oak_fence")
                .inputItems(new ItemStack(Blocks.OAK_PLANKS, 1))
                .outputItems(new ItemStack(Blocks.OAK_FENCE))
                .circuitMeta(1)
                .duration(100).EUt(4).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("spruce_fence")
                .inputItems(new ItemStack(Blocks.SPRUCE_PLANKS, 1))
                .outputItems(new ItemStack(Blocks.SPRUCE_FENCE))
                .circuitMeta(1)
                .duration(100).EUt(4).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("birch_fence")
                .inputItems(new ItemStack(Blocks.BIRCH_PLANKS, 1))
                .outputItems(new ItemStack(Blocks.BIRCH_FENCE))
                .circuitMeta(1)
                .duration(100).EUt(4).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("jungle_fence")
                .inputItems(new ItemStack(Blocks.JUNGLE_PLANKS, 1))
                .outputItems(new ItemStack(Blocks.JUNGLE_FENCE))
                .circuitMeta(1)
                .duration(100).EUt(4).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("acacia_fence")
                .inputItems(new ItemStack(Blocks.ACACIA_PLANKS, 1))
                .outputItems(new ItemStack(Blocks.ACACIA_FENCE))
                .circuitMeta(1)
                .duration(100).EUt(4).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("dark_oak_fence")
                .inputItems(new ItemStack(Blocks.DARK_OAK_PLANKS, 1))
                .outputItems(new ItemStack(Blocks.DARK_OAK_FENCE))
                .circuitMeta(1)
                .duration(100).EUt(4).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("oak_fence_gate")
                .inputItems(new ItemStack(Blocks.OAK_PLANKS, 2))
                .inputItems(Items.STICK, 2)
                .outputItems(new ItemStack(Blocks.OAK_FENCE_GATE))
                .circuitMeta(2)
                .duration(100).EUt(4).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("spruce_fence_gate")
                .inputItems(new ItemStack(Blocks.SPRUCE_PLANKS, 2))
                .inputItems(Items.STICK, 2)
                .outputItems(new ItemStack(Blocks.SPRUCE_FENCE_GATE))
                .circuitMeta(2)
                .duration(100).EUt(4).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("birch_fence_gate")
                .inputItems(new ItemStack(Blocks.BIRCH_PLANKS, 2))
                .inputItems(Items.STICK, 2)
                .outputItems(new ItemStack(Blocks.BIRCH_FENCE_GATE))
                .circuitMeta(2)
                .duration(100).EUt(4).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("jungle_fence_gate")
                .inputItems(new ItemStack(Blocks.JUNGLE_PLANKS, 2))
                .inputItems(Items.STICK, 2)
                .outputItems(new ItemStack(Blocks.JUNGLE_FENCE_GATE))
                .circuitMeta(2)
                .duration(100).EUt(4).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("acacia_fence_gate")
                .inputItems(new ItemStack(Blocks.ACACIA_PLANKS, 2))
                .inputItems(Items.STICK, 2)
                .outputItems(new ItemStack(Blocks.ACACIA_FENCE_GATE))
                .circuitMeta(2)
                .duration(100).EUt(4).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("dark_oak_fence_gate")
                .inputItems(new ItemStack(Blocks.DARK_OAK_PLANKS, 2))
                .inputItems(Items.STICK, 2)
                .outputItems(new ItemStack(Blocks.DARK_OAK_FENCE_GATE))
                .circuitMeta(2)
                .duration(100).EUt(4).save(provider);

        VanillaRecipeHelper.addShapedRecipe(provider, "sticky_resin_torch", new ItemStack(Blocks.TORCH, 3), "X", "Y", 'X', STICKY_RESIN, 'Y', new ItemStack(Items.STICK));
        VanillaRecipeHelper.addShapedRecipe(provider, "torch_sulfur", new ItemStack(Blocks.TORCH, 2), "C", "S", 'C', new UnificationEntry(dust, Sulfur), 'S', new ItemStack(Items.STICK));
        VanillaRecipeHelper.addShapedRecipe(provider, "torch_phosphorus", new ItemStack(Blocks.TORCH, 6), "C", "S", 'C', new UnificationEntry(dust, Phosphorus), 'S', new ItemStack(Items.STICK));
        VanillaRecipeHelper.addShapedRecipe(provider, "torch_coal_dust", new ItemStack(Blocks.TORCH, 4), "C", "S", 'C', new UnificationEntry(dust, Coal), 'S', new ItemStack(Items.STICK));
        VanillaRecipeHelper.addShapedRecipe(provider, "torch_charcoal_dust", new ItemStack(Blocks.TORCH, 4), "C", "S", 'C', new UnificationEntry(dust, Charcoal), 'S', new ItemStack(Items.STICK));
        VanillaRecipeHelper.addShapedRecipe(provider, "torch_coke", new ItemStack(Blocks.TORCH, 8), "C", "S", 'C', new UnificationEntry(gem, Coke), 'S', new ItemStack(Items.STICK));
        VanillaRecipeHelper.addShapedRecipe(provider, "torch_coke_dust", new ItemStack(Blocks.TORCH, 8), "C", "S", 'C', new UnificationEntry(dust, Coke), 'S', new ItemStack(Items.STICK));
        VanillaRecipeHelper.addShapedRecipe(provider, "torch_creosote", new ItemStack(Blocks.TORCH, 16), "WB", "S ", 'W', ItemTags.WOOL, 'S', new ItemStack(Items.STICK), 'B', Creosote.getBucket());

        ASSEMBLER_RECIPES.recipeBuilder("redstone_torch").EUt(1).inputItems(dust, Redstone).inputItems(new ItemStack(Items.STICK)).outputItems(new ItemStack(Blocks.REDSTONE_TORCH, 1)).duration(100).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("torch_sulfur").EUt(1).inputItems(new ItemStack(Items.STICK)).inputItems(dust, Sulfur).outputItems(new ItemStack(Blocks.TORCH, 2)).duration(100).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("torch_phosphorus").EUt(1).inputItems(new ItemStack(Items.STICK)).inputItems(dust, Phosphorus).outputItems(new ItemStack(Blocks.TORCH, 6)).duration(100).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("oak_stairs").EUt(1).duration(100).circuitMeta(7).inputItems(new ItemStack(Blocks.OAK_PLANKS, 6)).outputItems(new ItemStack(Blocks.OAK_STAIRS, 4)).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("spruce_stairs").EUt(1).duration(100).circuitMeta(7).inputItems(new ItemStack(Blocks.SPRUCE_PLANKS, 6)).outputItems(new ItemStack(Blocks.SPRUCE_STAIRS, 4)).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("birch_stairs").EUt(1).duration(100).circuitMeta(7).inputItems(new ItemStack(Blocks.BIRCH_PLANKS, 6)).outputItems(new ItemStack(Blocks.BIRCH_STAIRS, 4)).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("jungle_stairs").EUt(1).duration(100).circuitMeta(7).inputItems(new ItemStack(Blocks.JUNGLE_PLANKS, 6)).outputItems(new ItemStack(Blocks.JUNGLE_STAIRS, 4)).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("acacia_stairs").EUt(1).duration(100).circuitMeta(7).inputItems(new ItemStack(Blocks.ACACIA_PLANKS, 6)).outputItems(new ItemStack(Blocks.ACACIA_STAIRS, 4)).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("dark_oak_stairs").EUt(1).duration(100).circuitMeta(7).inputItems(new ItemStack(Blocks.DARK_OAK_PLANKS, 6)).outputItems(new ItemStack(Blocks.DARK_OAK_STAIRS, 4)).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("ladder").EUt(1).duration(40).circuitMeta(7).inputItems(new ItemStack(Items.STICK, 7)).outputItems(new ItemStack(Blocks.LADDER, 2)).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("chest_minecart").EUt(4).duration(100).inputItems(new ItemStack(Items.MINECART)).inputItems(CustomTags.TAG_WOODEN_CHESTS).outputItems(new ItemStack(Items.CHEST_MINECART)).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("furnace_minecart").EUt(4).duration(100).inputItems(new ItemStack(Items.MINECART)).inputItems(new ItemStack(Blocks.FURNACE)).outputItems(new ItemStack(Items.FURNACE_MINECART)).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("tnt_minecart").EUt(4).duration(100).inputItems(new ItemStack(Items.MINECART)).inputItems(new ItemStack(Blocks.TNT)).outputItems(new ItemStack(Items.TNT_MINECART)).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("hopper_minecart").EUt(4).duration(100).inputItems(new ItemStack(Items.MINECART)).inputItems(new ItemStack(Blocks.HOPPER)).outputItems(new ItemStack(Items.HOPPER_MINECART)).save(provider);
    }

    /**
     * + Adds cutting recipes for vanilla blocks
     */
    private static void cuttingRecipes(Consumer<FinishedRecipe> provider) {
        CUTTER_RECIPES.recipeBuilder("snow_layer")
                .inputItems(new ItemStack(Blocks.SNOW_BLOCK))
                .outputItems(new ItemStack(Blocks.SNOW, 12))
                .duration(25).EUt(VA[ULV]).save(provider);
    }

    /**
     * + Adds dying and cleaning recipes for vanilla blocks
     */
    private static void dyingCleaningRecipes(Consumer<FinishedRecipe> provider) {
        /*
        for (int i = 0; i < 16; i++) {
            MIXER_RECIPES.recipeBuilder().duration(200).EUt(VA[ULV])
                    .inputItems(new ItemStack(Blocks.SAND, 4))
                    .inputItems(new ItemStack(Blocks.GRAVEL, 4))
                    .inputFluids(CHEMICAL_DYES[i].getFluid(L))
                    .outputItems(new ItemStack(Blocks.CONCRETE_POWDER, 8, i))
                    .save(provider);

            CHEMICAL_BATH_RECIPES.recipeBuilder().duration(20).EUt(VA[ULV])
                    .inputItems(new ItemStack(Blocks.CONCRETE_POWDER, 1, i))
                    .inputFluids(Water.getFluid(1000))
                    .outputItems(new ItemStack(Blocks.CONCRETE, 1, i))
                    .save(provider);

            if(i != 0) {
                CHEMICAL_BATH_RECIPES.recipeBuilder().duration(20).EUt(VA[ULV])
                        .inputItems(new ItemStack(Blocks.CONCRETE))
                        .inputFluids(CHEMICAL_DYES[i].getFluid(L / 8))
                        .outputItems(new ItemStack(Blocks.CONCRETE, 1, i))
                        .save(provider);
            }

            CHEMICAL_BATH_RECIPES.recipeBuilder().duration(20).EUt(VA[ULV])
                    .inputItems(new ItemStack(Blocks.HARDENED_CLAY))
                    .inputFluids(CHEMICAL_DYES[i].getFluid(L / 8))
                    .outputItems(new ItemStack(Blocks.STAINED_HARDENED_CLAY, 1, i))
                    .save(provider);

            CHEMICAL_BATH_RECIPES.recipeBuilder().duration(20).EUt(VA[ULV])
                    .inputItems(new ItemStack(Blocks.GLASS))
                    .inputFluids(CHEMICAL_DYES[i].getFluid(L / 8))
                    .outputItems(new ItemStack(Blocks.STAINED_GLASS, 1, i))
                    .save(provider);

            CHEMICAL_BATH_RECIPES.recipeBuilder().duration(20).EUt(VA[ULV])
                    .inputItems(new ItemStack(Blocks.GLASS_PANE))
                    .inputFluids(CHEMICAL_DYES[i].getFluid(L / 8))
                    .outputItems(new ItemStack(Blocks.STAINED_GLASS_PANE, 1, i))
                    .save(provider);

            if(i != 0) {
                CHEMICAL_BATH_RECIPES.recipeBuilder().duration(20).EUt(VA[ULV])
                        .inputItems(new ItemStack(Blocks.WOOL))
                        .inputFluids(CHEMICAL_DYES[i].getFluid(L))
                        .outputItems(new ItemStack(Blocks.WOOL, 1, i))
                        .save(provider);
            }

            CUTTER_RECIPES.recipeBuilder().duration(20).EUt(VA[ULV])
                    .inputItems(new ItemStack(Blocks.WOOL, 2, i))
                    .outputItems(new ItemStack(Blocks.CARPET, 3, i))
                    .save(provider);

            ASSEMBLER_RECIPES.recipeBuilder().duration(20).EUt(VA[ULV])
                    .circuitMeta(6)
                    .inputItems(new ItemStack(Items.STICK))
                    .inputItems(new ItemStack(Blocks.WOOL, 6, i))
                    .outputItems(new ItemStack(Items.BANNER, 1, 16 - 1 - i))
                    .save(provider);
        }
         */

        // todo new tags to avoid white -> white recipe?
        CHEMICAL_BATH_RECIPES.recipeBuilder("decolor_wool")
                .inputItems(ItemTags.WOOL)
                .inputFluids(Chlorine.getFluid(50))
                .outputItems(new ItemStack(Blocks.WHITE_WOOL))
                .duration(400).EUt(2).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("decolor_carpet")
                .inputItems(ItemTags.WOOL_CARPETS)
                .inputFluids(Chlorine.getFluid(25))
                .outputItems(new ItemStack(Blocks.WHITE_CARPET))
                .duration(400).EUt(2).save(provider);

        //CHEMICAL_BATH_RECIPES.recipeBuilder("decolor_terracotta")
        //        .inputItems(Blocks.STAINED_HARDENED_CLAY, 1, true)
        //        .inputFluids(Chlorine.getFluid(50))
        //        .outputItems(Blocks.HARDENED_CLAY)
        //        .duration(400).EUt(2).save(provider);

        //CHEMICAL_BATH_RECIPES.recipeBuilder("decolor_stained_glass")
        //        .inputItems(Blocks.STAINED_GLASS, 1, true)
        //        .inputFluids(Chlorine.getFluid(50))
        //        .outputItems(Blocks.GLASS)
        //        .duration(400).EUt(2).save(provider);

        //CHEMICAL_BATH_RECIPES.recipeBuilder("decolor_stained_glass_pane")
        //        .inputItems(Blocks.STAINED_GLASS_PANE, 1, true)
        //        .inputFluids(Chlorine.getFluid(20))
        //        .outputItems(Blocks.GLASS_PANE)
        //        .duration(400).EUt(2).save(provider);

        //CHEMICAL_BATH_RECIPES.recipeBuilder("decolor_concrete")
        //        .inputItems(Blocks.CONCRETE, 1, true)
        //        .inputFluids(Chlorine.getFluid(20))
        //        .outputItems(Blocks.CONCRETE)
        //        .duration(400).EUt(2).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("sticky_piston_to_piston")
                .inputItems(new ItemStack(Blocks.STICKY_PISTON))
                .inputFluids(Chlorine.getFluid(10))
                .outputItems(new ItemStack(Blocks.PISTON))
                .duration(30).EUt(VA[LV]).save(provider);
    }

    /**
     * + Adds more redstone related recipes
     */
    private static void redstoneRecipes(Consumer<FinishedRecipe> provider) {
        ASSEMBLER_RECIPES.recipeBuilder("sticky_piston_resin")
                .inputItems(STICKY_RESIN)
                .inputItems(new ItemStack(Blocks.PISTON))
                .outputItems(new ItemStack(Blocks.STICKY_PISTON))
                .duration(100).EUt(4).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("sticky_piston_slime")
                .inputItems(new ItemStack(Items.SLIME_BALL))
                .inputItems(new ItemStack(Blocks.PISTON))
                .outputItems(new ItemStack(Blocks.STICKY_PISTON))
                .duration(100).EUt(4).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("sticky_piston_glue")
                .inputItems(new ItemStack(Blocks.PISTON))
                .inputFluids(Glue.getFluid(100))
                .outputItems(new ItemStack(Blocks.STICKY_PISTON))
                .duration(100).EUt(4).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("tripwire_hook_iron")
                .inputItems(Items.STICK, 2)
                .inputItems(ring, Iron, 2)
                .outputItems(new ItemStack(Blocks.TRIPWIRE_HOOK, 1))
                .duration(100).EUt(4).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("tripwire_hook_wrought_iron")
                .inputItems(Items.STICK, 2)
                .inputItems(ring, WroughtIron, 2)
                .outputItems(new ItemStack(Blocks.TRIPWIRE_HOOK, 1))
                .duration(100).EUt(4).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("redstone_lamp")
                .inputItems(dust, Redstone, 4)
                .inputItems(dust, Glowstone, 4)
                .outputItems(new ItemStack(Blocks.REDSTONE_LAMP))
                .duration(100).EUt(1).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("repeater")
                .inputItems(new ItemStack(Blocks.REDSTONE_TORCH, 2))
                .inputItems(dust, Redstone)
                .inputFluids(Concrete.getFluid(L))
                .outputItems(new ItemStack(Items.REPEATER))
                .duration(100).EUt(10).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("comparator_nether_quartz")
                .inputItems(new ItemStack(Blocks.REDSTONE_TORCH, 3))
                .inputItems(gem, NetherQuartz)
                .inputFluids(Concrete.getFluid(L))
                .outputItems(new ItemStack(Items.COMPARATOR))
                .duration(100).EUt(1).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("comparator_certus_quartz")
                .inputItems(new ItemStack(Blocks.REDSTONE_TORCH, 3))
                .inputItems(gem, CertusQuartz)
                .inputFluids(Concrete.getFluid(L))
                .outputItems(new ItemStack(Items.COMPARATOR))
                .duration(100).EUt(1).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("comparator_quartzite")
                .inputItems(new ItemStack(Blocks.REDSTONE_TORCH, 3))
                .inputItems(gem, Quartzite)
                .inputFluids(Concrete.getFluid(L))
                .outputItems(new ItemStack(Items.COMPARATOR))
                .duration(100).EUt(1).save(provider);
    }

    /**
     * + Adds metal related recipes
     * + Adds horse armor and chainmail recipes
     */
    private static void metalRecipes(Consumer<FinishedRecipe> provider) {
        BENDER_RECIPES.recipeBuilder("bucket")
                .circuitMeta(12)
                .inputItems(plate, Iron, 3)
                .outputItems(new ItemStack(Items.BUCKET))
                .duration(100).EUt(4)
                .save(provider);

        VanillaRecipeHelper.addShapedRecipe(provider, "iron_horse_armor", new ItemStack(Items.IRON_HORSE_ARMOR), "hdH", "PCP", "LSL",
                'H', new ItemStack(Items.IRON_HELMET),
                'P', new UnificationEntry(plate, Iron),
                'C', new ItemStack(Items.IRON_CHESTPLATE),
                'L', new ItemStack(Items.IRON_LEGGINGS),
                'S', new UnificationEntry(screw, Iron)
        );

        VanillaRecipeHelper.addShapedRecipe(provider, "golden_horse_armor", new ItemStack(Items.GOLDEN_HORSE_ARMOR), "hdH", "PCP", "LSL",
                'H', new ItemStack(Items.GOLDEN_HELMET),
                'P', new UnificationEntry(plate, Gold),
                'C', new ItemStack(Items.GOLDEN_CHESTPLATE),
                'L', new ItemStack(Items.GOLDEN_LEGGINGS),
                'S', new UnificationEntry(screw, Gold)
        );

        VanillaRecipeHelper.addShapedRecipe(provider, "diamond_horse_armor", new ItemStack(Items.DIAMOND_HORSE_ARMOR), "hdH", "PCP", "LSL",
                'H', new ItemStack(Items.DIAMOND_HELMET),
                'P', new UnificationEntry(plate, Diamond),
                'C', new ItemStack(Items.DIAMOND_CHESTPLATE),
                'L', new ItemStack(Items.DIAMOND_LEGGINGS),
                'S', new UnificationEntry(bolt, Diamond)
        );

        VanillaRecipeHelper.addShapedRecipe(provider, "chainmail_helmet", new ItemStack(Items.CHAINMAIL_HELMET), "PPP", "PhP",
                'P', new UnificationEntry(ring, Iron)
        );

        VanillaRecipeHelper.addShapedRecipe(provider, "chainmail_chestplate", new ItemStack(Items.CHAINMAIL_CHESTPLATE), "PhP", "PPP", "PPP",
                'P', new UnificationEntry(ring, Iron)
        );

        VanillaRecipeHelper.addShapedRecipe(provider, "chainmail_leggings", new ItemStack(Items.CHAINMAIL_LEGGINGS), "PPP", "PhP", "P P",
                'P', new UnificationEntry(ring, Iron)
        );

        VanillaRecipeHelper.addShapedRecipe(provider, "chainmail_boots", new ItemStack(Items.CHAINMAIL_BOOTS), "P P", "PhP",
                'P', new UnificationEntry(ring, Iron)
        );

        ASSEMBLER_RECIPES.recipeBuilder("cauldron")
                .inputItems(plate, Iron, 7)
                .outputItems(new ItemStack(Items.CAULDRON, 1))
                .circuitMeta(7)
                .duration(700).EUt(4).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("iron_nars")
                .inputItems(rod, Iron, 3)
                .outputItems(new ItemStack(Blocks.IRON_BARS, 4))
                .circuitMeta(3)
                .duration(300).EUt(4).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("iron_trapdoor")
                .inputItems(plate, Iron, 4)
                .circuitMeta(4)
                .outputItems(new ItemStack(Blocks.IRON_TRAPDOOR))
                .duration(100).EUt(16).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("iron_door")
                .inputItems(plate, Iron, 6)
                .circuitMeta(6)
                .outputItems(new ItemStack(Items.IRON_DOOR))
                .duration(100).EUt(16).save(provider);
    }

    /**
     * Adds miscellaneous vanilla recipes
     * Adds vanilla fluid solidification recipes
     * Adds anvil recipes
     * Adds Slime to rubber
     * Adds alternative gunpowder recipes
     * Adds polished stone variant autoclave recipes
     */
    private static void miscRecipes(Consumer<FinishedRecipe> provider) {
        ASSEMBLER_RECIPES.recipeBuilder("book_from_leather")
                .inputItems(new ItemStack(Items.PAPER, 3))
                .inputItems(new ItemStack(Items.LEATHER))
                .inputFluids(Glue.getFluid(20))
                .outputItems(new ItemStack(Items.BOOK))
                .duration(32).EUt(VA[ULV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("book_from_pvc")
                .inputItems(new ItemStack(Items.PAPER, 3))
                .inputItems(foil, PolyvinylChloride)
                .inputFluids(Glue.getFluid(20))
                .outputItems(new ItemStack(Items.BOOK))
                .duration(20).EUt(16).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("map")
                .inputItems(new ItemStack(Items.PAPER, 8))
                .inputItems(new ItemStack(Items.COMPASS))
                .outputItems(new ItemStack(Items.MAP))
                .duration(100).EUt(VA[ULV]).save(provider);

        ALLOY_SMELTER_RECIPES.recipeBuilder("form_nether_brick")
                .inputItems(dust, Netherrack)
                .notConsumable(SHAPE_MOLD_INGOT)
                .outputItems(new ItemStack(Items.NETHER_BRICK))
                .duration(200).EUt(2).save(provider);

        ALLOY_SMELTER_RECIPES.recipeBuilder("form_brick")
                .inputItems(new ItemStack(Items.CLAY_BALL))
                .notConsumable(SHAPE_MOLD_INGOT)
                .outputItems(new ItemStack(Items.BRICK))
                .duration(200).EUt(2).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("lead")
                .inputItems(new ItemStack(Items.STRING))
                .inputItems(new ItemStack(Items.SLIME_BALL))
                .outputItems(new ItemStack(Items.LEAD, 2))
                .duration(100).EUt(2).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("name_tag")
                .inputItems(new ItemStack(Items.LEATHER))
                .inputItems(new ItemStack(Items.LEAD))
                .inputFluids(Glue.getFluid(100))
                .outputItems(new ItemStack(Items.NAME_TAG))
                .duration(100).EUt(VA[ULV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("bow")
                .inputItems(new ItemStack(Items.STRING, 3))
                .inputItems(Items.STICK, 3)
                .outputItems(new ItemStack(Items.BOW, 1))
                .duration(100).EUt(4).save(provider);

        FLUID_SOLIDFICATION_RECIPES.recipeBuilder("snowball").duration(128).EUt(4).notConsumable(SHAPE_MOLD_BALL).inputFluids(Water.getFluid(250)).outputItems(new ItemStack(Items.SNOWBALL)).save(provider);
        FLUID_SOLIDFICATION_RECIPES.recipeBuilder("snowball_distilled").duration(128).EUt(4).notConsumable(SHAPE_MOLD_BALL).inputFluids(DistilledWater.getFluid(250)).outputItems(new ItemStack(Items.SNOWBALL)).save(provider);
        FLUID_SOLIDFICATION_RECIPES.recipeBuilder("snow_block").duration(512).EUt(4).notConsumable(SHAPE_MOLD_BLOCK).inputFluids(Water.getFluid(1000)).outputItems(new ItemStack(Blocks.SNOW_BLOCK)).save(provider);
        FLUID_SOLIDFICATION_RECIPES.recipeBuilder("snow_block_distilled").duration(512).EUt(4).notConsumable(SHAPE_MOLD_BLOCK).inputFluids(DistilledWater.getFluid(1000)).outputItems(new ItemStack(Blocks.SNOW_BLOCK)).save(provider);
        FLUID_SOLIDFICATION_RECIPES.recipeBuilder("obsidian").duration(1024).EUt(16).notConsumable(SHAPE_MOLD_BLOCK).inputFluids(Lava.getFluid(1000)).outputItems(new ItemStack(Blocks.OBSIDIAN)).save(provider);

        FLUID_SOLIDFICATION_RECIPES.recipeBuilder("solidify_anvil").duration(1680).EUt(16).notConsumable(SHAPE_MOLD_ANVIL).inputFluids(Iron.getFluid(L * 31)).outputItems(new ItemStack(Blocks.ANVIL)).save(provider);
        ALLOY_SMELTER_RECIPES.recipeBuilder("anvil").inputItems(ingot, Iron, 31).notConsumable(SHAPE_MOLD_ANVIL).outputItems(new ItemStack(Blocks.ANVIL)).duration(1680).EUt(16).save(provider);

        VanillaRecipeHelper.addSmeltingRecipe(provider, "sticky_resin_from_slime", new ItemStack(Items.SLIME_BALL), STICKY_RESIN.asStack(), 0.3f);

        ASSEMBLER_RECIPES.recipeBuilder("wool_from_string")
                .inputItems(new ItemStack(Items.STRING, 4))
                .circuitMeta(4)
                .outputItems(new ItemStack(Blocks.WHITE_WOOL))
                .duration(100).EUt(4).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("mossy_cobblestone")
                .inputItems(new ItemStack(Blocks.COBBLESTONE))
                .inputItems(new ItemStack(Blocks.VINE))
                .outputItems(new ItemStack(Blocks.MOSSY_COBBLESTONE))
                .duration(40).EUt(1).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("mossy_stone_bricks")
                .inputItems(new ItemStack(Blocks.STONE_BRICKS))
                .inputItems(new ItemStack(Blocks.VINE))
                .outputItems(new ItemStack(Blocks.MOSSY_STONE_BRICKS))
                .duration(40).EUt(1).save(provider);



        CANNER_RECIPES.recipeBuilder("jack_o_lantern").EUt(4).duration(100).inputItems(new ItemStack(Blocks.PUMPKIN)).inputItems(new ItemStack(Blocks.TORCH)).outputItems(new ItemStack(Blocks.JACK_O_LANTERN)).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("sea_lantern").EUt(4).duration(40).inputItems(new ItemStack(Items.PRISMARINE_CRYSTALS, 5)).inputItems(new ItemStack(Items.PRISMARINE_SHARD, 4)).outputItems(new ItemStack(Blocks.SEA_LANTERN)).save(provider);

        ALLOY_SMELTER_RECIPES.recipeBuilder("red_nether_bricks").EUt(4).duration(40).inputItems(new ItemStack(Items.NETHER_BRICK, 2)).inputItems(new ItemStack(Items.NETHER_WART, 2)).outputItems(new ItemStack(Blocks.RED_NETHER_BRICKS)).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("nether_brick_fence").duration(100).EUt(4).circuitMeta(3).inputItems(new ItemStack(Blocks.NETHER_BRICKS)).outputItems(new ItemStack(Blocks.NETHER_BRICK_FENCE)).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("ender_chest").duration(100).EUt(4).inputItems(new ItemStack(Blocks.OBSIDIAN, 8)).inputItems(new ItemStack(Items.ENDER_EYE)).outputItems(new ItemStack(Blocks.ENDER_CHEST)).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("end_rod").duration(100).EUt(4).inputItems(new ItemStack(Items.POPPED_CHORUS_FRUIT)).inputItems(new ItemStack(Items.BLAZE_ROD)).outputItems(new ItemStack(Blocks.END_ROD, 4)).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("purple_shulker_box").duration(100).EUt(VA[ULV]).inputItems(CustomTags.TAG_WOODEN_CHESTS).inputItems(new ItemStack(Items.SHULKER_SHELL, 2)).outputItems(new ItemStack(Blocks.PURPLE_SHULKER_BOX)).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("painting").duration(100).EUt(4).circuitMeta(1).inputItems(ItemTags.WOOL).inputItems(new ItemStack(Items.STICK, 8)).outputItems(new ItemStack(Items.PAINTING)).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("item_frame").duration(100).EUt(4).inputItems(new ItemStack(Items.LEATHER)).inputItems(new ItemStack(Items.STICK, 8)).outputItems(new ItemStack(Items.ITEM_FRAME)).save(provider);

        // todo signs
        //ASSEMBLER_RECIPES.recipeBuilder().duration(100).EUt(4).inputItems(CommonTags.TAG_PLANKS, 6).inputItems(new ItemStack(Items.STICK)).circuitMeta(1).outputItems(CommonTags.TAG_SIGNS, 3).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("flower_pot").duration(10).EUt(2).inputItems(new ItemStack(Items.BRICK, 3)).outputItems(new ItemStack(Items.FLOWER_POT)).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("armor_stand").duration(30).EUt(VA[ULV]).inputItems(new ItemStack(Blocks.STONE_SLAB)).inputItems(new ItemStack(Items.STICK, 6)).outputItems(new ItemStack(Items.ARMOR_STAND)).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("end_crystal").duration(30).EUt(16).inputItems(new ItemStack(Items.GHAST_TEAR)).inputItems(new ItemStack(Items.ENDER_EYE)).outputItems(new ItemStack(Items.END_CRYSTAL)).inputFluids(Glass.getFluid(L * 7)).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("rail")
                .inputItems(rod, Iron, 12)
                .inputItems(new ItemStack(Items.STICK))
                .circuitMeta(1)
                .outputItems(new ItemStack(Blocks.RAIL, 32))
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("powered_rail")
                .inputItems(rod, Gold, 12)
                .inputItems(new ItemStack(Items.STICK))
                .inputItems(dust, Redstone)
                .circuitMeta(1)
                .outputItems(new ItemStack(Blocks.POWERED_RAIL, 12))
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("detector_rail")
                .inputItems(rod, Iron, 12)
                .inputItems(new ItemStack(Items.STICK))
                .inputItems(dust, Redstone)
                .circuitMeta(5)
                .outputItems(new ItemStack(Blocks.DETECTOR_RAIL, 12))
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("activator_rail")
                .inputItems(rod, Iron, 12)
                .inputItems(new ItemStack(Items.STICK, 2))
                .inputItems(new ItemStack(Blocks.REDSTONE_TORCH))
                .circuitMeta(5)
                .outputItems(new ItemStack(Blocks.ACTIVATOR_RAIL, 12))
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("minecart")
                .inputItems(plate, Iron, 3)
                .inputItems(ring, Iron, 4)
                .outputItems(new ItemStack(Items.MINECART))
                .duration(100).EUt(4).save(provider);

        VanillaRecipeHelper.addShapedRecipe(provider, "saddle", new ItemStack(Items.SADDLE), "LLL", "LCL", "RSR",
                'L', new ItemStack(Items.LEATHER),
                'C', ItemTags.WOOL_CARPETS,
                'R', new UnificationEntry(ring, Iron),
                'S', new ItemStack(Items.STRING)
        );

        AUTOCLAVE_RECIPES.recipeBuilder("clay_from_dust")
                .inputItems(dust, Clay)
                .inputFluids(Water.getFluid(250))
                .outputItems(new ItemStack(Items.CLAY_BALL))
                .duration(600).EUt(24).save(provider);

        AUTOCLAVE_RECIPES.recipeBuilder("clay_from_dust_distilled")
                .inputItems(dust, Clay)
                .inputFluids(DistilledWater.getFluid(250))
                .outputItems(new ItemStack(Items.CLAY_BALL))
                .duration(300).EUt(24).save(provider);

        COMPRESSOR_RECIPES.recipeBuilder("redstone_block")
                .inputItems(dust, Redstone, 9)
                .outputItems(new ItemStack(Blocks.REDSTONE_BLOCK))
                .duration(300).EUt(2).save(provider);

        COMPRESSOR_RECIPES.recipeBuilder("bone_block")
                .inputItems(dust, Bone, 9)
                .outputItems(new ItemStack(Blocks.BONE_BLOCK))
                .duration(300).EUt(2).save(provider);

        COMPRESSOR_RECIPES.recipeBuilder("purpur_block")
                .inputItems(new ItemStack(Items.POPPED_CHORUS_FRUIT, 4))
                .outputItems(new ItemStack(Blocks.PURPUR_BLOCK, 4))
                .duration(300).EUt(2).save(provider);

        COMPRESSOR_RECIPES.recipeBuilder("magma_block")
                .inputItems(new ItemStack(Items.MAGMA_CREAM, 4))
                .outputItems(new ItemStack(Blocks.MAGMA_BLOCK))
                .duration(300).EUt(2).save(provider);

        COMPRESSOR_RECIPES.recipeBuilder("slime_block")
                .inputItems(new ItemStack(Items.SLIME_BALL, 9))
                .outputItems(new ItemStack(Blocks.SLIME_BLOCK))
                .duration(300).EUt(2).save(provider);

        PACKER_RECIPES.recipeBuilder("nether_wart_block")
                .inputItems(new ItemStack(Items.NETHER_WART, 9))
                .circuitMeta(9)
                .outputItems(new ItemStack(Blocks.NETHER_WART_BLOCK))
                .duration(200).EUt(2).save(provider);

        PACKER_RECIPES.recipeBuilder("prismarine")
                .inputItems(new ItemStack(Items.PRISMARINE_SHARD, 4))
                .circuitMeta(4)
                .outputItems(new ItemStack(Blocks.PRISMARINE))
                .duration(100).EUt(2).save(provider);

        PACKER_RECIPES.recipeBuilder("prismarine_bricks")
                .inputItems(new ItemStack(Items.PRISMARINE_SHARD, 9))
                .circuitMeta(9)
                .outputItems(new ItemStack(Blocks.PRISMARINE_BRICKS))
                .duration(200).EUt(2).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("eye_of_ender")
                .inputFluids(Blaze.getFluid(L))
                .inputItems(gem, EnderPearl)
                .outputItems(new ItemStack(Items.ENDER_EYE))
                .duration(50).EUt(VA[HV]).save(provider);

        COMPRESSOR_RECIPES.recipeBuilder("blaze_rod")
                .inputItems(dust, Blaze, 4)
                .outputItems(new ItemStack(Items.BLAZE_ROD))
                .save(provider);

        FLUID_HEATER_RECIPES.recipeBuilder("mud_to_clay")
                .inputItems(Items.MUD)
                .outputItems(Items.CLAY)
                .duration(40).EUt(VA[LV]).save(provider);
    }

    /**
     * Adds various mixer recipes for vanilla items and blocks
     */
    private static void mixingRecipes(Consumer<FinishedRecipe> provider) {
        MIXER_RECIPES.recipeBuilder("fire_charge")
                .inputItems(dust, Coal)
                .inputItems(dust, Gunpowder)
                .inputItems(dust, Blaze)
                .outputItems(new ItemStack(Items.FIRE_CHARGE, 3))
                .duration(100).EUt(VA[LV]).save(provider);

        MIXER_RECIPES.recipeBuilder("coarse_dirt")
                .inputItems(new ItemStack(Blocks.GRAVEL))
                .inputItems(new ItemStack(Blocks.DIRT))
                .outputItems(new ItemStack(Blocks.COARSE_DIRT, 2))
                .duration(100).EUt(4).save(provider);

        MIXER_RECIPES.recipeBuilder("mud")
                .inputItems(new ItemStack(Blocks.DIRT))
                .inputFluids(Water.getFluid(L))
                .outputItems(new ItemStack(Blocks.COARSE_DIRT, 2))
                .duration(100).EUt(4).save(provider);
    }

    private static void dyeRecipes(Consumer<FinishedRecipe> provider) {

        EXTRACTOR_RECIPES.recipeBuilder("poppy_dye")
                .inputItems(new ItemStack(Blocks.POPPY))
                .outputItems(new ItemStack(Items.RED_DYE, 2))
                .save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("blue_orchid_dye")
                .inputItems(new ItemStack(Blocks.BLUE_ORCHID))
                .outputItems(new ItemStack(Items.LIGHT_BLUE_DYE, 2))
                .save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("allium_dye")
                .inputItems(new ItemStack(Blocks.ALLIUM))
                .outputItems(new ItemStack(Items.MAGENTA_DYE, 2))
                .save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("azure_bluet_dye")
                .inputItems(new ItemStack(Blocks.AZURE_BLUET))
                .outputItems(new ItemStack(Items.LIGHT_GRAY_DYE))
                .save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("red_tulip_dye")
                .inputItems(new ItemStack(Blocks.RED_TULIP))
                .outputItems(new ItemStack(Items.RED_DYE, 2))
                .save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("orange_tulip_dye")
                .inputItems(new ItemStack(Blocks.ORANGE_TULIP))
                .outputItems(new ItemStack(Items.ORANGE_DYE, 2))
                .save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("white_tulip_dye")
                .inputItems(new ItemStack(Blocks.WHITE_TULIP))
                .outputItems(new ItemStack(Items.LIGHT_GRAY_DYE, 2))
                .save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("pink_tulip_dye")
                .inputItems(new ItemStack(Blocks.PINK_TULIP))
                .outputItems(new ItemStack(Items.PINK_DYE))
                .save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("oxeye_daisy_dye")
                .inputItems(new ItemStack(Blocks.OXEYE_DAISY))
                .outputItems(new ItemStack(Items.LIGHT_GRAY_DYE, 2))
                .save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("dandelion_dye")
                .inputItems(new ItemStack(Blocks.DANDELION))
                .outputItems(new ItemStack(Items.YELLOW_DYE, 2))
                .save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("cornflower_dye")
                .inputItems(new ItemStack(Blocks.CORNFLOWER))
                .outputItems(new ItemStack(Items.BLUE_DYE, 2))
                .save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("lily_of_the_valley_dye")
                .inputItems(new ItemStack(Blocks.LILY_OF_THE_VALLEY))
                .outputItems(new ItemStack(Items.WHITE_DYE, 2))
                .save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("wither_rose_dye")
                .inputItems(new ItemStack(Blocks.WITHER_ROSE))
                .outputItems(new ItemStack(Items.BLACK_DYE, 2))
                .save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("sunflower_dye")
                .inputItems(new ItemStack(Blocks.SUNFLOWER))
                .outputItems(new ItemStack(Items.YELLOW_DYE, 3))
                .save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("lilac_dye")
                .inputItems(new ItemStack(Blocks.LILAC))
                .outputItems(new ItemStack(Items.MAGENTA_DYE, 3))
                .save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("rose_bush_dye")
                .inputItems(new ItemStack(Blocks.ROSE_BUSH))
                .outputItems(new ItemStack(Items.RED_DYE, 3))
                .save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("peony_dye")
                .inputItems(new ItemStack(Blocks.PEONY))
                .outputItems(new ItemStack(Items.PINK_DYE, 3))
                .save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("beetroot_dye")
                .inputItems(new ItemStack(Items.BEETROOT))
                .outputItems(new ItemStack(Items.RED_DYE, 2))
                .save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("dark_prismarine")
                .inputItems(new ItemStack(Items.PRISMARINE_SHARD, 8))
                .inputFluids(DyeBlack.getFluid(144))
                .outputItems(new ItemStack(Blocks.DARK_PRISMARINE))
                .duration(20).EUt(VA[ULV]).save(provider);
    }
}
