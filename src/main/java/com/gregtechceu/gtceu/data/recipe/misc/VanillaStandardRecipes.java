package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidContainerIngredient;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeCategories;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;

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

        COMPRESSOR_RECIPES.recipeBuilder("sandstone").duration(300).EUt(2)
                .inputItems(new ItemStack(Blocks.SAND, 4))
                .outputItems(new ItemStack(Blocks.SANDSTONE))
                .save(provider);

        COMPRESSOR_RECIPES.recipeBuilder("red_sandstone").duration(300).EUt(2)
                .inputItems(new ItemStack(Blocks.RED_SAND), 4)
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

        COMPRESSOR_RECIPES.recipeBuilder("snowballs_to_snow").duration(200).EUt(2)
                .inputItems(new ItemStack(Items.SNOWBALL, 4))
                .outputItems(new ItemStack(Items.SNOW_BLOCK))
                .save(provider);

        COMPRESSOR_RECIPES.recipeBuilder("glowstone").duration(300).EUt(2)
                .inputItems(new ItemStack(Items.GLOWSTONE_DUST, 4))
                .outputItems(new ItemStack(Blocks.GLOWSTONE))
                .save(provider);

        COMPRESSOR_RECIPES.recipeBuilder("packed_ice")
                .inputItems(new ItemStack(Blocks.ICE, 9))
                .outputItems(new ItemStack(Blocks.PACKED_ICE))
                .duration(300).EUt(2)
                .addMaterialInfo(true)
                .save(provider);

        COMPRESSOR_RECIPES.recipeBuilder("blue_ice")
                .inputItems(new ItemStack(Blocks.PACKED_ICE, 9))
                .outputItems(new ItemStack(Blocks.BLUE_ICE))
                .duration(300).EUt(2)
                .addMaterialInfo(true)
                .save(provider);

        COMPRESSOR_RECIPES.recipeBuilder("ice_from_dust")
                .inputItems(dust, Ice)
                .outputItems(new ItemStack(Blocks.ICE))
                .duration(300).EUt(2)
                .save(provider);

        COMPRESSOR_RECIPES.recipeBuilder("dripstone_block_from_pointed_dripstone")
                .inputItems(new ItemStack(Items.POINTED_DRIPSTONE, 4))
                .outputItems(new ItemStack(Blocks.DRIPSTONE_BLOCK))
                .duration(300).EUt(2)
                .save(provider);

        FORGE_HAMMER_RECIPES.recipeBuilder("pointed_dripstone_from_dripstone_block")
                .inputItems(new ItemStack(Blocks.DRIPSTONE_BLOCK))
                .outputItems(new ItemStack(Items.POINTED_DRIPSTONE, 4))
                .duration(300).EUt(2)
                .save(provider);

        PACKER_RECIPES.recipeBuilder("hay_block")
                .inputItems(new ItemStack(Items.WHEAT, 9))
                .circuitMeta(8)
                .outputItems(new ItemStack(Blocks.HAY_BLOCK))
                .duration(200).EUt(2)
                .save(provider);

        PACKER_RECIPES.recipeBuilder("wheat")
                .inputItems(new ItemStack(Blocks.HAY_BLOCK))
                .outputItems(new ItemStack(Items.WHEAT, 9))
                .circuitMeta(9)
                .duration(200).EUt(2)
                .save(provider);

        FORGE_HAMMER_RECIPES.recipeBuilder("wheat_from_hay_block")
                .inputItems(new ItemStack(Blocks.HAY_BLOCK))
                .outputItems(new ItemStack(Items.WHEAT, 9))
                .duration(200).EUt(2)
                .save(provider);

        PACKER_RECIPES.recipeBuilder("melon")
                .inputItems(new ItemStack(Items.MELON_SLICE, 9))
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
        VanillaRecipeHelper.addShapedRecipe(provider, "glass_dust_hammer", ChemicalHelper.get(dust, Glass), "hG", 'G',
                new ItemStack(Blocks.GLASS));

        VanillaRecipeHelper.addShapedRecipe(provider, "quartz_sand", ChemicalHelper.get(dust, QuartzSand), "S", "m",
                'S', new ItemStack(Blocks.SAND));

        MACERATOR_RECIPES.recipeBuilder("quartz_sand_from_sand")
                .inputItems(new ItemStack(Blocks.SAND))
                .outputItems(dust, QuartzSand)
                .duration(30).EUt(2).save(provider);

        VanillaRecipeHelper.addShapelessRecipe(provider, "glass_dust_flint", ChemicalHelper.get(dust, Glass),
                new MaterialEntry(dust, QuartzSand),
                new MaterialEntry(dustTiny, Flint));

        VanillaRecipeHelper.addShapelessRecipe(provider, "glass_full_dust_flint", ChemicalHelper.get(dust, Glass, 8),
                new MaterialEntry(dust, QuartzSand),
                new MaterialEntry(dust, QuartzSand),
                new MaterialEntry(dust, QuartzSand),
                new MaterialEntry(dust, QuartzSand),
                new MaterialEntry(dust, QuartzSand),
                new MaterialEntry(dust, QuartzSand),
                new MaterialEntry(dust, QuartzSand),
                new MaterialEntry(dust, QuartzSand),
                new MaterialEntry(dust, Flint));

        MIXER_RECIPES.recipeBuilder("glass_from_quartzite").duration(160).EUt(VA[ULV])
                .inputItems(dustSmall, Flint)
                .inputItems(dust, Quartzite, 4)
                .outputItems(dust, Glass, 5)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("full_dust_glass_from_quartzite").duration(640).EUt(VA[ULV])
                .inputItems(dust, Flint)
                .inputItems(dust, Quartzite, 16)
                .outputItems(dust, Glass, 20)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("glass_from_quartz_sand").duration(200).EUt(VA[ULV])
                .inputItems(dustSmall, Flint)
                .inputItems(dust, QuartzSand, 4)
                .outputItems(dust, Glass, 4)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("full_dust_glass_from_sand").duration(800).EUt(VA[ULV])
                .inputItems(dust, Flint)
                .inputItems(dust, QuartzSand, 16)
                .outputItems(dust, Glass, 16)
                .save(provider);

        ARC_FURNACE_RECIPES.recipeBuilder("glass_from_sand").duration(20).EUt(VA[LV])
                .inputItems(ItemTags.SMELTS_TO_GLASS)
                .outputItems(new ItemStack(Blocks.GLASS, 2))
                .save(provider);

        FORMING_PRESS_RECIPES.recipeBuilder("form_glass").duration(80).EUt(VA[LV])
                .inputItems(dust, Glass)
                .notConsumable(SHAPE_MOLD_BLOCK)
                .outputItems(new ItemStack(Blocks.GLASS))
                .addMaterialInfo(true)
                .save(provider);

        ALLOY_SMELTER_RECIPES.recipeBuilder("glass_bottle").duration(64).EUt(4)
                .inputItems(dust, Glass)
                .notConsumable(SHAPE_MOLD_BOTTLE)
                .outputItems(new ItemStack(Items.GLASS_BOTTLE))
                .addMaterialInfo(true)
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

        FLUID_SOLIDFICATION_RECIPES.recipeBuilder("solidify_glass_block").duration(12).EUt(4)
                .inputFluids(Glass.getFluid(L))
                .notConsumable(SHAPE_MOLD_BLOCK)
                .outputItems(new ItemStack(Blocks.GLASS))
                .save(provider);

        ALLOY_SMELTER_RECIPES.recipeBuilder("glass").duration(120).EUt(16)
                .inputItems(dust, Glass)
                .notConsumable(SHAPE_MOLD_BLOCK)
                .outputItems(new ItemStack(Blocks.GLASS, 1))
                .save(provider);

        CUTTER_RECIPES.recipeBuilder("cut_glass_block_to_plate").duration(50).EUt(VA[ULV])
                .inputItems(new ItemStack(Blocks.GLASS, 3))
                .outputItems(new ItemStack(Blocks.GLASS_PANE, 8))
                .addMaterialInfo(true)
                .save(provider);
    }

    /**
     * Adds smashing related recipes for vanilla blocks and items
     */
    private static void smashingRecipes(Consumer<FinishedRecipe> provider) {
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
                .chancedOutput(new ItemStack(Items.FLINT), 3300, 0)
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

        VanillaRecipeHelper.addShapelessRecipe(provider, "clay_block_to_dust", ChemicalHelper.get(dust, Clay), 'm',
                Blocks.CLAY);
        VanillaRecipeHelper.addShapelessRecipe(provider, "clay_ball_to_dust", ChemicalHelper.get(dustSmall, Clay), 'm',
                Items.CLAY_BALL);
        VanillaRecipeHelper.addShapelessRecipe(provider, "brick_block_to_dust", ChemicalHelper.get(dust, Brick), 'm',
                Blocks.BRICKS);
        VanillaRecipeHelper.addShapelessRecipe(provider, "brick_to_dust", ChemicalHelper.get(dustSmall, Brick), 'm',
                Items.BRICK);
        VanillaRecipeHelper.addShapelessRecipe(provider, "wheat_to_dust", ChemicalHelper.get(dust, Wheat), 'm',
                Items.WHEAT);
        VanillaRecipeHelper.addShapelessRecipe(provider, "gravel_to_flint", new ItemStack(Items.FLINT), 'm',
                Blocks.GRAVEL);
        VanillaRecipeHelper.addShapelessRecipe(provider, "bone_to_bone_meal", new ItemStack(Items.BONE_MEAL, 4), 'm',
                Items.BONE);
        VanillaRecipeHelper.addShapelessRecipe(provider, "blaze_rod_to_powder", new ItemStack(Items.BLAZE_POWDER, 3),
                'm', Items.BLAZE_ROD);

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
                .chancedOutput(new ItemStack(Items.MELON_SEEDS), 8500, 0)
                .duration(400).EUt(2)
                .save(provider);

        MACERATOR_RECIPES.recipeBuilder("macerate_pumpkin")
                .inputItems(new ItemStack(Blocks.PUMPKIN))
                .outputItems(new ItemStack(Items.PUMPKIN_SEEDS, 4))
                .duration(400).EUt(2)
                .save(provider);

        MACERATOR_RECIPES.recipeBuilder("macerate_melon_slice")
                .inputItems(new ItemStack(Items.MELON_SLICE))
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
                .chancedOutput(dust, Wood, 8500, 0)
                .duration(150).EUt(2)
                .save(provider);

        LATHE_RECIPES.recipeBuilder("lathe_planks")
                .inputItems(ItemTags.PLANKS)
                .outputItems(new ItemStack(Items.STICK, 2))
                .duration(10).EUt(VA[ULV])
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
                .addMaterialInfo(true)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("bookshelf")
                .inputItems(ItemTags.PLANKS, 6)
                .inputItems(new ItemStack(Items.BOOK, 3))
                .outputItems(new ItemStack(Blocks.BOOKSHELF))
                .duration(100).EUt(4)
                .addMaterialInfo(true)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("chest")
                .inputItems(ItemTags.PLANKS, 8)
                .outputItems(new ItemStack(Blocks.CHEST))
                .duration(100).EUt(4).circuitMeta(8)
                .addMaterialInfo(true)
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

        ASSEMBLER_RECIPES.recipeBuilder("soul_torch")
                .inputItems(new ItemStack(Blocks.TORCH))
                .inputItems(ItemTags.SOUL_FIRE_BASE_BLOCKS)
                .outputItems(new ItemStack(Blocks.SOUL_TORCH))
                .duration(100).EUt(1).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("soul_lantern_from_lantern")
                .inputItems(new ItemStack(Blocks.LANTERN))
                .inputItems(ItemTags.SOUL_FIRE_BASE_BLOCKS)
                .outputItems(new ItemStack(Blocks.SOUL_LANTERN))
                .duration(100).EUt(1).save(provider);

        VanillaRecipeHelper.addShapedRecipe(provider, "sticky_resin_torch", new ItemStack(Blocks.TORCH, 3), "X", "Y",
                'X', STICKY_RESIN, 'Y', new ItemStack(Items.STICK));
        VanillaRecipeHelper.addShapedRecipe(provider, "torch_sulfur", new ItemStack(Blocks.TORCH, 2), "C", "S", 'C',
                new MaterialEntry(dust, Sulfur), 'S', new ItemStack(Items.STICK));
        VanillaRecipeHelper.addShapedRecipe(provider, "torch_phosphorus", new ItemStack(Blocks.TORCH, 6), "C", "S", 'C',
                new MaterialEntry(dust, Phosphorus), 'S', new ItemStack(Items.STICK));
        VanillaRecipeHelper.addShapedRecipe(provider, "torch_coal_dust", new ItemStack(Blocks.TORCH, 4), "C", "S", 'C',
                new MaterialEntry(dust, Coal), 'S', new ItemStack(Items.STICK));
        VanillaRecipeHelper.addShapedRecipe(provider, "torch_charcoal_dust", new ItemStack(Blocks.TORCH, 4), "C", "S",
                'C', new MaterialEntry(dust, Charcoal), 'S', new ItemStack(Items.STICK));
        VanillaRecipeHelper.addShapedRecipe(provider, "torch_coke", new ItemStack(Blocks.TORCH, 8), "C", "S", 'C',
                new MaterialEntry(gem, Coke), 'S', new ItemStack(Items.STICK));
        VanillaRecipeHelper.addShapedRecipe(provider, "torch_coke_dust", new ItemStack(Blocks.TORCH, 8), "C", "S", 'C',
                new MaterialEntry(dust, Coke), 'S', new ItemStack(Items.STICK));
        VanillaRecipeHelper.addShapedRecipe(provider, "torch_creosote", new ItemStack(Blocks.TORCH, 16), "WB", "S ",
                'W', ItemTags.WOOL, 'S', new ItemStack(Items.STICK), 'B',
                new FluidContainerIngredient(Creosote.getFluidTag(), 1000));
        VanillaRecipeHelper.addShapedRecipe(provider, "soul_torch", new ItemStack(Blocks.SOUL_TORCH, 1), "WB",
                'W', ItemTags.SOUL_FIRE_BASE_BLOCKS, 'B', new ItemStack(Blocks.TORCH));
        if (!ConfigHolder.INSTANCE.recipes.hardMiscRecipes) {
            VanillaRecipeHelper.addShapedRecipe(provider, "soul_lantern_from_lantern",
                    new ItemStack(Blocks.SOUL_LANTERN, 1), "WB",
                    'W', ItemTags.SOUL_FIRE_BASE_BLOCKS, 'B', new ItemStack(Blocks.LANTERN));

            ASSEMBLER_RECIPES.recipeBuilder("soul_lantern_from_lantern")
                    .inputItems(new ItemStack(Blocks.LANTERN))
                    .inputItems(ItemTags.SOUL_FIRE_BASE_BLOCKS)
                    .outputItems(new ItemStack(Blocks.SOUL_LANTERN))
                    .duration(100).EUt(1).save(provider);
        }

        ASSEMBLER_RECIPES.recipeBuilder("redstone_torch").EUt(4).inputItems(dust, Redstone)
                .inputItems(new ItemStack(Items.STICK)).outputItems(new ItemStack(Blocks.REDSTONE_TORCH, 1))
                .circuitMeta(3).duration(100).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("torch_sulfur").EUt(4).inputItems(new ItemStack(Items.STICK))
                .inputItems(dust, Sulfur).outputItems(new ItemStack(Blocks.TORCH, 2)).duration(100).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("torch_phosphorus").EUt(4).inputItems(new ItemStack(Items.STICK))
                .inputItems(dust, Phosphorus).outputItems(new ItemStack(Blocks.TORCH, 6)).duration(100).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("ladder").EUt(4).duration(40).circuitMeta(7)
                .inputItems(new ItemStack(Items.STICK, 7)).outputItems(new ItemStack(Blocks.LADDER, 2)).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("barrel")
                .inputItems(ItemTags.PLANKS, 7)
                .outputItems(new ItemStack(Blocks.BARREL))
                .circuitMeta(24)
                .duration(100).EUt(4)
                .save(provider);
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
        for (DyeColor color : DyeColor.values()) {
            String dyeName = color.getName();
            MIXER_RECIPES.recipeBuilder(dyeName + "_concrete_powder").duration(200).EUt(VA[ULV])
                    .inputItems(Tags.Items.SAND, 4)
                    .inputItems(Tags.Items.GRAVEL, 4)
                    .inputFluids(CHEMICAL_DYES[color.ordinal()].getFluid(L))
                    .outputItems(new ItemStack(
                            BuiltInRegistries.ITEM.get(new ResourceLocation(dyeName + "_concrete_powder")), 8))
                    .save(provider);

            CHEMICAL_BATH_RECIPES.recipeBuilder(dyeName + "_concrete").duration(20).EUt(VA[ULV])
                    .inputItems(new ItemStack(
                            BuiltInRegistries.ITEM.get(new ResourceLocation(dyeName + "_concrete_powder"))))
                    .inputFluids(Water.getFluid(1000))
                    .outputItems(new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(dyeName + "_concrete"))))
                    .category(GTRecipeCategories.CHEM_DYES)
                    .save(provider);

            if (color != DyeColor.WHITE) {
                CHEMICAL_BATH_RECIPES.recipeBuilder("dye_concrete_to_" + dyeName).duration(20).EUt(VA[ULV])
                        .inputItems(CustomTags.CONCRETE_ITEM)
                        .inputFluids(CHEMICAL_DYES[color.ordinal()].getFluid(L / 8))
                        .outputItems(
                                new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(dyeName + "_concrete"))))
                        .category(GTRecipeCategories.CHEM_DYES)
                        .save(provider);
            }

            CHEMICAL_BATH_RECIPES.recipeBuilder("dye_terracotta_to_" + dyeName).duration(20).EUt(VA[ULV])
                    .inputItems(new ItemStack(Blocks.TERRACOTTA))
                    .inputFluids(CHEMICAL_DYES[color.ordinal()].getFluid(L / 8))
                    .outputItems(
                            new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(dyeName + "_terracotta"))))
                    .category(GTRecipeCategories.CHEM_DYES)
                    .save(provider);

            CHEMICAL_BATH_RECIPES.recipeBuilder("dye_glass_to_" + dyeName).duration(20).EUt(VA[ULV])
                    .inputItems(new ItemStack(Blocks.GLASS))
                    .inputFluids(CHEMICAL_DYES[color.ordinal()].getFluid(L / 8))
                    .outputItems(
                            new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(dyeName + "_stained_glass"))))
                    .category(GTRecipeCategories.CHEM_DYES)
                    .save(provider);

            CHEMICAL_BATH_RECIPES.recipeBuilder("dye_glass_pane_to_" + dyeName).duration(20).EUt(VA[ULV])
                    .inputItems(new ItemStack(Blocks.GLASS_PANE))
                    .inputFluids(CHEMICAL_DYES[color.ordinal()].getFluid(L / 8))
                    .outputItems(new ItemStack(
                            BuiltInRegistries.ITEM.get(new ResourceLocation(dyeName + "_stained_glass_pane"))))
                    .category(GTRecipeCategories.CHEM_DYES)
                    .save(provider);

            CUTTER_RECIPES.recipeBuilder("cut_" + dyeName + "_glass_to_pane").duration(20).EUt(VA[ULV])
                    .inputItems(BuiltInRegistries.ITEM.get(new ResourceLocation(dyeName + "_stained_glass")), 3)
                    .outputItems(BuiltInRegistries.ITEM.get(new ResourceLocation(dyeName + "_stained_glass_pane")), 8)
                    .save(provider);

            CHEMICAL_BATH_RECIPES.recipeBuilder("dye_candle_to_" + dyeName).duration(20).EUt(VA[ULV])
                    .inputItems(new ItemStack(Items.CANDLE))
                    .inputFluids(CHEMICAL_DYES[color.ordinal()].getFluid(L / 8))
                    .outputItems(new ItemStack(
                            BuiltInRegistries.ITEM.get(new ResourceLocation(dyeName + "_candle"))))
                    .category(GTRecipeCategories.CHEM_DYES)
                    .save(provider);

            if (color != DyeColor.WHITE) {
                CHEMICAL_BATH_RECIPES.recipeBuilder("dye_wool_to_" + dyeName).duration(20).EUt(VA[ULV])
                        .inputItems(new ItemStack(Blocks.WHITE_WOOL))
                        .inputFluids(CHEMICAL_DYES[color.ordinal()].getFluid(L))
                        .outputItems(new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(dyeName + "_wool"))))
                        .category(GTRecipeCategories.CHEM_DYES)
                        .save(provider);

                CHEMICAL_BATH_RECIPES.recipeBuilder("dye_bed_to_" + dyeName).duration(20).EUt(VA[ULV])
                        .inputItems(new ItemStack(Blocks.WHITE_BED))
                        .inputFluids(CHEMICAL_DYES[color.ordinal()].getFluid(L))
                        .outputItems(new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(dyeName + "_bed"))))
                        .category(GTRecipeCategories.CHEM_DYES)
                        .save(provider);
            }

            CUTTER_RECIPES.recipeBuilder("cut_" + dyeName + "_wool_to_carpet").duration(20).EUt(VA[ULV])
                    .inputItems(new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(dyeName + "_wool")), 1))
                    .outputItems(
                            new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(dyeName + "_carpet")), 2))
                    .save(provider);

            ASSEMBLER_RECIPES.recipeBuilder(dyeName + "_banner").duration(20).EUt(VA[ULV])
                    .circuitMeta(6)
                    .inputItems(new ItemStack(Items.STICK))
                    .inputItems(new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(dyeName + "_wool")), 6))
                    .outputItems(new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(dyeName + "_banner"))))
                    .save(provider);
        }

        // todo new tags to avoid white -> white recipe?
        CHEMICAL_BATH_RECIPES.recipeBuilder("decolor_wool")
                .inputItems(ItemTags.WOOL)
                .inputFluids(Chlorine.getFluid(50))
                .outputItems(new ItemStack(Blocks.WHITE_WOOL))
                .category(GTRecipeCategories.CHEM_DYES)
                .duration(400).EUt(2).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("decolor_carpet")
                .inputItems(ItemTags.WOOL_CARPETS)
                .inputFluids(Chlorine.getFluid(25))
                .outputItems(new ItemStack(Blocks.WHITE_CARPET))
                .category(GTRecipeCategories.CHEM_DYES)
                .duration(400).EUt(2).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("decolor_terracotta")
                .inputItems(ItemTags.TERRACOTTA)
                .inputFluids(Chlorine.getFluid(50))
                .outputItems(Items.TERRACOTTA)
                .category(GTRecipeCategories.CHEM_DYES)
                .duration(400).EUt(2).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("decolor_stained_glass")
                .inputItems(Tags.Items.STAINED_GLASS)
                .inputFluids(Chlorine.getFluid(50))
                .outputItems(Items.GLASS)
                .category(GTRecipeCategories.CHEM_DYES)
                .duration(400).EUt(2).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("decolor_stained_glass_pane")
                .inputItems(Tags.Items.STAINED_GLASS_PANES)
                .inputFluids(Chlorine.getFluid(20))
                .outputItems(Items.GLASS_PANE)
                .category(GTRecipeCategories.CHEM_DYES)
                .duration(400).EUt(2).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("decolor_concrete")
                .inputItems(CustomTags.CONCRETE_ITEM)
                .inputFluids(Chlorine.getFluid(20))
                .outputItems(Items.WHITE_CONCRETE)
                .category(GTRecipeCategories.CHEM_DYES)
                .duration(400).EUt(2).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("sticky_piston_to_piston")
                .inputItems(new ItemStack(Blocks.STICKY_PISTON))
                .inputFluids(Chlorine.getFluid(10))
                .outputItems(new ItemStack(Blocks.PISTON))
                .duration(30).EUt(VA[LV]).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("decolor_candle")
                .inputItems(ItemTags.CANDLES)
                .inputFluids(Chlorine.getFluid(20))
                .outputItems(Items.CANDLE)
                .category(GTRecipeCategories.CHEM_DYES)
                .duration(400).EUt(2).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("decolor_bed")
                .inputItems(ItemTags.BEDS)
                .inputFluids(Chlorine.getFluid(20))
                .outputItems(Items.WHITE_BED)
                .category(GTRecipeCategories.CHEM_DYES)
                .duration(400).EUt(2).save(provider);
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

        if (!ConfigHolder.INSTANCE.recipes.hardRedstoneRecipes) {
            CUTTER_RECIPES.recipeBuilder("stone_pressure_plate")
                    .inputItems(new ItemStack(Blocks.STONE_SLAB))
                    .outputItems(new ItemStack(Blocks.STONE_PRESSURE_PLATE, 8))
                    .duration(250).EUt(VA[ULV]).save(provider);

            CUTTER_RECIPES.recipeBuilder("polished_blackstone_pressure_plate")
                    .inputItems(new ItemStack(Blocks.POLISHED_BLACKSTONE_SLAB))
                    .outputItems(new ItemStack(Blocks.POLISHED_BLACKSTONE_PRESSURE_PLATE, 8))
                    .duration(250).EUt(VA[ULV]).save(provider);
        }
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
                .addMaterialInfo(true)
                .save(provider);

        VanillaRecipeHelper.addShapedRecipe(provider, "leather_horse_armor", new ItemStack(Items.LEATHER_HORSE_ARMOR),
                "hdH",
                "PCP", "LSL",
                'H', new ItemStack(Items.LEATHER_HELMET),
                'P', new ItemStack(Items.LEATHER),
                'C', new ItemStack(Items.LEATHER_CHESTPLATE),
                'L', new ItemStack(Items.LEATHER_LEGGINGS),
                'S', new MaterialEntry(screw, Iron));

        VanillaRecipeHelper.addShapedRecipe(provider, true, "iron_horse_armor", new ItemStack(Items.IRON_HORSE_ARMOR),
                "hdH",
                "PCP", "LSL",
                'H', new ItemStack(Items.IRON_HELMET),
                'P', new MaterialEntry(plate, Iron),
                'C', new ItemStack(Items.IRON_CHESTPLATE),
                'L', new ItemStack(Items.IRON_LEGGINGS),
                'S', new MaterialEntry(screw, Iron));

        VanillaRecipeHelper.addShapedRecipe(provider, true, "golden_horse_armor",
                new ItemStack(Items.GOLDEN_HORSE_ARMOR),
                "hdH", "PCP", "LSL",
                'H', new ItemStack(Items.GOLDEN_HELMET),
                'P', new MaterialEntry(plate, Gold),
                'C', new ItemStack(Items.GOLDEN_CHESTPLATE),
                'L', new ItemStack(Items.GOLDEN_LEGGINGS),
                'S', new MaterialEntry(screw, Gold));

        VanillaRecipeHelper.addShapedRecipe(provider, true, "diamond_horse_armor",
                new ItemStack(Items.DIAMOND_HORSE_ARMOR),
                "hdH", "PCP", "LSL",
                'H', new ItemStack(Items.DIAMOND_HELMET),
                'P', new MaterialEntry(plate, Diamond),
                'C', new ItemStack(Items.DIAMOND_CHESTPLATE),
                'L', new ItemStack(Items.DIAMOND_LEGGINGS),
                'S', new MaterialEntry(bolt, Diamond));

        VanillaRecipeHelper.addShapedRecipe(provider, true, "chainmail_helmet", new ItemStack(Items.CHAINMAIL_HELMET),
                "PPP",
                "PhP",
                'P', Items.CHAIN);

        VanillaRecipeHelper.addShapedRecipe(provider, true, "chainmail_chestplate",
                new ItemStack(Items.CHAINMAIL_CHESTPLATE),
                "PhP", "PPP", "PPP",
                'P', Items.CHAIN);

        VanillaRecipeHelper.addShapedRecipe(provider, true, "chainmail_leggings",
                new ItemStack(Items.CHAINMAIL_LEGGINGS),
                "PPP", "PhP", "P P",
                'P', Items.CHAIN);

        VanillaRecipeHelper.addShapedRecipe(provider, true, "chainmail_boots", new ItemStack(Items.CHAINMAIL_BOOTS),
                "P P",
                "PhP",
                'P', Items.CHAIN);

        ASSEMBLER_RECIPES.recipeBuilder("cauldron")
                .inputItems(plate, Iron, 7)
                .outputItems(new ItemStack(Items.CAULDRON, 1))
                .circuitMeta(7)
                .duration(700).EUt(4)
                .addMaterialInfo(true).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("iron_bars")
                .inputItems(rod, Iron, 3)
                .outputItems(new ItemStack(Blocks.IRON_BARS, 4))
                .circuitMeta(3)
                .duration(300).EUt(4)
                .addMaterialInfo(true).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("iron_trapdoor")
                .inputItems(plate, Iron, 4)
                .circuitMeta(4)
                .outputItems(new ItemStack(Blocks.IRON_TRAPDOOR))
                .duration(100).EUt(16)
                .addMaterialInfo(true).save(provider);

        if (!ConfigHolder.INSTANCE.recipes.hardAdvancedIronRecipes) {
            ASSEMBLER_RECIPES.recipeBuilder("iron_door")
                    .inputItems(TagPrefix.plate, GTMaterials.Iron, 6)
                    .circuitMeta(6)
                    .outputItems(new ItemStack(Items.IRON_DOOR, 3))
                    .duration(100).EUt(16)
                    .addMaterialInfo(true).save(provider);
        }
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
        if (ConfigHolder.INSTANCE.recipes.hardToolArmorRecipes) {
            ASSEMBLER_RECIPES.recipeBuilder("fishing_rod")
                    .inputItems(new ItemStack(Items.STRING))
                    .inputItems(rodLong, Wood, 2)
                    .inputItems(ring, Iron)
                    .outputItems(new ItemStack(Items.FISHING_ROD, 1))
                    .circuitMeta(16)
                    .duration(100).EUt(4).save(provider);
        } else {
            ASSEMBLER_RECIPES.recipeBuilder("fishing_rod")
                    .inputItems(new ItemStack(Items.STRING, 2))
                    .inputItems(rod, Wood, 3)
                    .outputItems(new ItemStack(Items.FISHING_ROD, 1))
                    .circuitMeta(16)
                    .duration(100).EUt(4).save(provider);
        }

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
                .circuitMeta(10)
                .duration(100).EUt(4).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("crossbow")
                .inputItems(new ItemStack(Items.STRING, 2))
                .inputItems(Items.STICK, 3)
                .inputItems(Items.TRIPWIRE_HOOK)
                .outputItems(new ItemStack(Items.CROSSBOW, 1))
                .circuitMeta(11)
                .duration(100).EUt(4).save(provider);

        FLUID_SOLIDFICATION_RECIPES.recipeBuilder("snowball").duration(128).EUt(4).notConsumable(SHAPE_MOLD_BALL)
                .inputFluids(Water.getFluid(250)).outputItems(new ItemStack(Items.SNOWBALL)).save(provider);
        FLUID_SOLIDFICATION_RECIPES.recipeBuilder("snowball_distilled").duration(128).EUt(4)
                .notConsumable(SHAPE_MOLD_BALL).inputFluids(DistilledWater.getFluid(250))
                .outputItems(new ItemStack(Items.SNOWBALL)).save(provider);
        FLUID_SOLIDFICATION_RECIPES.recipeBuilder("snow_block").duration(512).EUt(4).notConsumable(SHAPE_MOLD_BLOCK)
                .inputFluids(Water.getFluid(1000)).outputItems(new ItemStack(Blocks.SNOW_BLOCK)).save(provider);
        FLUID_SOLIDFICATION_RECIPES.recipeBuilder("snow_block_distilled").duration(512).EUt(4)
                .notConsumable(SHAPE_MOLD_BLOCK).inputFluids(DistilledWater.getFluid(1000))
                .outputItems(new ItemStack(Blocks.SNOW_BLOCK)).save(provider);
        FLUID_SOLIDFICATION_RECIPES.recipeBuilder("obsidian").duration(1024).EUt(16).notConsumable(SHAPE_MOLD_BLOCK)
                .inputFluids(Lava.getFluid(1000)).outputItems(new ItemStack(Blocks.OBSIDIAN)).save(provider);

        FLUID_SOLIDFICATION_RECIPES.recipeBuilder("solidify_anvil").duration(1680).EUt(16)
                .notConsumable(SHAPE_MOLD_ANVIL).inputFluids(Iron.getFluid(L * 31))
                .outputItems(new ItemStack(Blocks.ANVIL)).save(provider);
        ALLOY_SMELTER_RECIPES.recipeBuilder("anvil").inputItems(ingot, Iron, 31).notConsumable(SHAPE_MOLD_ANVIL)
                .outputItems(new ItemStack(Blocks.ANVIL)).duration(1680).EUt(16).save(provider);

        VanillaRecipeHelper.addSmeltingRecipe(provider, "sticky_resin_from_slime", new ItemStack(Items.SLIME_BALL),
                STICKY_RESIN.asStack(), 0.3f);

        ASSEMBLER_RECIPES.recipeBuilder("wool_from_string")
                .inputItems(new ItemStack(Items.STRING, 4))
                .circuitMeta(4)
                .outputItems(new ItemStack(Blocks.WHITE_WOOL))
                .duration(100).EUt(4).save(provider);

        MIXER_RECIPES.recipeBuilder("mossy_cobblestone_from_vine")
                .inputItems(new ItemStack(Blocks.COBBLESTONE))
                .inputItems(new ItemStack(Blocks.VINE))
                .inputFluids(Water.getFluid(250))
                .outputItems(new ItemStack(Blocks.MOSSY_COBBLESTONE))
                .duration(40).EUt(1).save(provider);

        MIXER_RECIPES.recipeBuilder("mossy_cobblestone_from_moss_block")
                .inputItems(new ItemStack(Blocks.COBBLESTONE))
                .inputItems(new ItemStack(Blocks.MOSS_BLOCK))
                .inputFluids(Water.getFluid(250))
                .outputItems(new ItemStack(Blocks.MOSSY_COBBLESTONE))
                .duration(40).EUt(1).save(provider);

        MIXER_RECIPES.recipeBuilder("mossy_stone_bricks_from_vine")
                .inputItems(new ItemStack(Blocks.STONE_BRICKS))
                .inputItems(new ItemStack(Blocks.VINE))
                .inputFluids(Water.getFluid(250))
                .outputItems(new ItemStack(Blocks.MOSSY_STONE_BRICKS))
                .duration(40).EUt(1).save(provider);

        MIXER_RECIPES.recipeBuilder("mossy_stone_bricks_from_moss_block")
                .inputItems(new ItemStack(Blocks.STONE_BRICKS))
                .inputItems(new ItemStack(Blocks.MOSS_BLOCK))
                .inputFluids(Water.getFluid(250))
                .outputItems(new ItemStack(Blocks.MOSSY_STONE_BRICKS))
                .duration(40).EUt(1).save(provider);

        CANNER_RECIPES.recipeBuilder("jack_o_lantern").EUt(4).duration(100).inputItems(new ItemStack(Blocks.PUMPKIN))
                .inputItems(new ItemStack(Blocks.TORCH)).outputItems(new ItemStack(Blocks.JACK_O_LANTERN))
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("sea_lantern").EUt(4).duration(40)
                .inputItems(new ItemStack(Items.PRISMARINE_CRYSTALS, 5))
                .inputItems(new ItemStack(Items.PRISMARINE_SHARD, 4)).outputItems(new ItemStack(Blocks.SEA_LANTERN))
                .save(provider);

        ALLOY_SMELTER_RECIPES.recipeBuilder("red_nether_bricks").EUt(4).duration(40)
                .inputItems(new ItemStack(Items.NETHER_BRICK, 2)).inputItems(new ItemStack(Items.NETHER_WART, 2))
                .outputItems(new ItemStack(Blocks.RED_NETHER_BRICKS)).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("nether_brick_fence").duration(100).EUt(4).circuitMeta(3)
                .inputItems(new ItemStack(Blocks.NETHER_BRICKS)).outputItems(new ItemStack(Blocks.NETHER_BRICK_FENCE))
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("end_rod").duration(100).EUt(4)
                .inputItems(new ItemStack(Items.POPPED_CHORUS_FRUIT)).inputItems(new ItemStack(Items.BLAZE_ROD))
                .outputItems(new ItemStack(Blocks.END_ROD, 4)).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("shulker_box").duration(100).EUt(VA[ULV])
                .inputItems(Tags.Items.CHESTS_WOODEN).inputItems(new ItemStack(Items.SHULKER_SHELL, 2))
                .outputItems(new ItemStack(Blocks.SHULKER_BOX)).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("painting").duration(100).EUt(4).circuitMeta(1).inputItems(ItemTags.WOOL)
                .inputItems(new ItemStack(Items.STICK, 8)).outputItems(new ItemStack(Items.PAINTING))
                .addMaterialInfo(true).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("item_frame").duration(100).EUt(4).inputItems(new ItemStack(Items.LEATHER))
                .inputItems(new ItemStack(Items.STICK, 8)).outputItems(new ItemStack(Items.ITEM_FRAME))
                .addMaterialInfo(true).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("flower_pot").duration(10).EUt(2).inputItems(new ItemStack(Items.BRICK, 3))
                .outputItems(new ItemStack(Items.FLOWER_POT))
                .addMaterialInfo(true).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("end_crystal").duration(30).EUt(16).inputItems(new ItemStack(Items.GHAST_TEAR))
                .inputItems(new ItemStack(Items.ENDER_EYE))
                .inputFluids(Glass.getFluid(L * 7))
                .outputItems(new ItemStack(Items.END_CRYSTAL))
                .addMaterialInfo(true, true).save(provider);

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
                'R', new MaterialEntry(ring, Iron),
                'S', new ItemStack(Items.STRING));

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

        COMPRESSOR_RECIPES.recipeBuilder("mud_to_clay")
                .inputItems(Items.MUD)
                .outputItems(Items.CLAY)
                .duration(40).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("ender_chest").duration(100).EUt(4)
                .inputItems(new ItemStack(Blocks.OBSIDIAN, 8)).inputItems(new ItemStack(Items.ENDER_EYE))
                .outputItems(new ItemStack(Blocks.ENDER_CHEST))
                .addMaterialInfo(!ConfigHolder.INSTANCE.recipes.hardMiscRecipes).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("armor_stand").duration(30).EUt(VA[ULV])
                .inputItems(new ItemStack(Blocks.SMOOTH_STONE_SLAB, 1)).inputItems(new ItemStack(Items.STICK, 6))
                .outputItems(new ItemStack(Items.ARMOR_STAND)).save(provider);

        FLUID_SOLIDFICATION_RECIPES.recipeBuilder("candle")
                .inputFluids(Wax.getFluid(L))
                .inputItems(new ItemStack(Items.STRING))
                .outputItems(new ItemStack(Blocks.CANDLE, 2))
                .duration(20).EUt(1).save(provider);

        FORGE_HAMMER_RECIPES.recipeBuilder("disc_fragment_5")
                .inputItems(new ItemStack(Items.MUSIC_DISC_5))
                .outputItems(new ItemStack(Items.DISC_FRAGMENT_5, 9))
                .duration(100).EUt(6).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("chest_minecart").EUt(4).duration(100).inputItems(new ItemStack(Items.MINECART))
                .inputItems(Tags.Items.CHESTS_WOODEN).outputItems(new ItemStack(Items.CHEST_MINECART)).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("furnace_minecart").EUt(4).duration(100)
                .inputItems(new ItemStack(Items.MINECART)).inputItems(new ItemStack(Blocks.FURNACE))
                .outputItems(new ItemStack(Items.FURNACE_MINECART)).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("tnt_minecart").EUt(4).duration(100).inputItems(new ItemStack(Items.MINECART))
                .inputItems(new ItemStack(Blocks.TNT)).outputItems(new ItemStack(Items.TNT_MINECART)).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("hopper_minecart").EUt(4).duration(100)
                .inputItems(new ItemStack(Items.MINECART)).inputItems(new ItemStack(Blocks.HOPPER))
                .outputItems(new ItemStack(Items.HOPPER_MINECART)).save(provider);

        VanillaRecipeHelper.addShapelessRecipe(provider, "hay_block_to_hay", new ItemStack(Items.WHEAT, 9),
                Items.HAY_BLOCK, 'k');

        ASSEMBLER_RECIPES.recipeBuilder("conduit")
                .inputItems(new ItemStack(Items.HEART_OF_THE_SEA))
                .inputItems(new ItemStack(Items.NAUTILUS_SHELL, 8))
                .outputItems(new ItemStack(Blocks.CONDUIT))
                .duration(200).EUt(16).save(provider);

        ALLOY_SMELTER_RECIPES.recipeBuilder("granite")
                .inputItems(new ItemStack(Items.DIORITE))
                .inputItems(new ItemStack(Items.QUARTZ))
                .outputItems(new ItemStack(Items.GRANITE))
                .duration(80).EUt(4).save(provider);
        ALLOY_SMELTER_RECIPES.recipeBuilder("diorite")
                .inputItems(new ItemStack(Items.COBBLESTONE, 2))
                .inputItems(new ItemStack(Items.QUARTZ, 2))
                .outputItems(new ItemStack(Items.DIORITE, 2))
                .duration(80).EUt(4).save(provider);
        ALLOY_SMELTER_RECIPES.recipeBuilder("andesite")
                .inputItems(new ItemStack(Items.DIORITE))
                .inputItems(new ItemStack(Items.COBBLESTONE))
                .outputItems(new ItemStack(Items.ANDESITE, 2))
                .duration(80).EUt(4).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("assemble_block_of_quartz_into_quartz_pillar")
                .inputItems(new ItemStack(Items.QUARTZ_BLOCK))
                .circuitMeta(5)
                .outputItems(new ItemStack(Items.QUARTZ_PILLAR))
                .duration(80).EUt(1).save(provider);

        MIXER_RECIPES.recipeBuilder("packed_mud")
                .inputItems(new ItemStack(Items.MUD))
                .inputItems(new ItemStack(Items.WHEAT))
                .outputItems(new ItemStack(Items.PACKED_MUD))
                .duration(80).EUt(4).save(provider);
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
                .circuitMeta(1)
                .inputFluids(Water.getFluid(L))
                .outputItems(new ItemStack(Blocks.MUD, 1))
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

        EXTRACTOR_RECIPES.recipeBuilder("torchflower_dye")
                .inputItems(new ItemStack(Items.TORCHFLOWER))
                .outputItems(new ItemStack(Items.ORANGE_DYE, 2))
                .save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("pitcher_dye")
                .inputItems(new ItemStack(Items.PITCHER_PLANT))
                .outputItems(new ItemStack(Items.CYAN_DYE, 3))
                .save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("pink_petals_dye")
                .inputItems(new ItemStack(Items.PINK_PETALS))
                .outputItems(new ItemStack(Items.PINK_DYE, 2))
                .save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("sea_pickle_dye")
                .inputItems(new ItemStack(Items.SEA_PICKLE))
                .outputItems(new ItemStack(Items.LIME_DYE, 2))
                .save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("white_dye")
                .inputItems(new ItemStack(Items.BONE_MEAL))
                .outputItems(new ItemStack(Items.WHITE_DYE, 1))
                .save(provider);
        EXTRACTOR_RECIPES.recipeBuilder("lapis_dye")
                .inputItems(new ItemStack(Items.LAPIS_LAZULI))
                .outputItems(new ItemStack(Items.BLUE_DYE))
                .save(provider);
        EXTRACTOR_RECIPES.recipeBuilder("ink_dye")
                .inputItems(new ItemStack(Items.INK_SAC))
                .outputItems(new ItemStack(Items.BLACK_DYE))
                .save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("dark_prismarine")
                .inputItems(new ItemStack(Items.PRISMARINE_SHARD, 4))
                .inputFluids(DyeBlack.getFluid(L))
                .outputItems(new ItemStack(Blocks.DARK_PRISMARINE))
                .duration(20).EUt(VA[ULV]).save(provider);
    }
}
