package com.gregtechceu.gtceu.data.recipe.configurable;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.*;
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
import static com.gregtechceu.gtceu.common.data.GTItems.COMPRESSED_CLAY;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ASSEMBLER_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.LATHE_RECIPES;

public class RecipeAddition {

    public static void init(Consumer<FinishedRecipe> provider) {
        hardMiscRecipes(provider);
        hardRedstoneRecipes(provider);
        disableManualCompression(provider);
        hardToolArmorRecipes(provider);
        harderRods(provider);
        nerfWoodCrafting(provider);
        harderBrickRecipes(provider);
        steelSteamMultiblocks(provider);
        if (ConfigHolder.INSTANCE.recipes.hardWoodRecipes) hardWoodRecipes(provider);
        if (ConfigHolder.INSTANCE.recipes.hardIronRecipes) hardIronRecipes(provider);
        if (ConfigHolder.INSTANCE.recipes.hardGlassRecipes) hardGlassRecipes(provider);
        if (ConfigHolder.INSTANCE.recipes.nerfPaperCrafting) nerfPaperCrafting(provider);
        if (ConfigHolder.INSTANCE.recipes.hardAdvancedIronRecipes) hardAdvancedIronRecipes(provider);
        if (ConfigHolder.INSTANCE.recipes.hardDyeRecipes) hardDyeRecipes(provider);
        if (ConfigHolder.INSTANCE.recipes.harderCharcoalRecipe) harderCharcoalRecipes(provider);
        if (ConfigHolder.INSTANCE.recipes.flintAndSteelRequireSteel) flintAndSteelRequireSteel(provider);
        if (ConfigHolder.INSTANCE.recipes.removeVanillaBlockRecipes) removeVanillaBlockRecipes(provider);
    }

    private static void steelSteamMultiblocks(Consumer<FinishedRecipe> provider) {
        if (ConfigHolder.INSTANCE.machines.steelSteamMultiblocks) {
            VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_oven", GTMachines.STEAM_OVEN.asStack(), "CGC", "FMF", "CGC", 'F', GTBlocks.FIREBOX_STEEL.asStack(), 'C', GTBlocks.CASING_STEEL_SOLID.asStack(), 'M', GTMachines.STEAM_FURNACE.right().asStack(), 'G', new UnificationEntry(TagPrefix.gear, GTMaterials.Invar));
            VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_grinder", GTMachines.STEAM_GRINDER.asStack(), "CGC", "CFC", "CGC", 'G', new UnificationEntry(TagPrefix.gear, GTMaterials.Potin), 'F', GTMachines.STEAM_MACERATOR.right().asStack(), 'C', GTBlocks.CASING_STEEL_SOLID.asStack());
            VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_hatch", GTMachines.STEAM_HATCH.asStack(), "BPB", "BTB", "BPB", 'B', new UnificationEntry(TagPrefix.plate, GTMaterials.Steel), 'P', new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.Steel), 'T', GTMachines.STEEL_DRUM.asStack());
            VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_input_bus", GTMachines.STEAM_IMPORT_BUS.asStack(), "C", "H", 'H', GTBlocks.STEEL_HULL.asStack(), 'C', CustomTags.TAG_WOODEN_CHESTS);
            VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_output_bus", GTMachines.STEAM_EXPORT_BUS.asStack(), "H", "C", 'H', GTBlocks.STEEL_HULL.asStack(), 'C', CustomTags.TAG_WOODEN_CHESTS);
        } else {
            VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_oven", GTMachines.STEAM_OVEN.asStack(), "CGC", "FMF", "CGC", 'F', GTBlocks.FIREBOX_BRONZE.asStack(), 'C', GTBlocks.CASING_BRONZE_BRICKS.asStack(), 'M', GTMachines.STEAM_FURNACE.left().asStack(), 'G', new UnificationEntry(TagPrefix.gear, GTMaterials.Invar));
            VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_grinder", GTMachines.STEAM_GRINDER.asStack(), "CGC", "CFC", "CGC", 'G', new UnificationEntry(TagPrefix.gear, GTMaterials.Potin), 'F', GTMachines.STEAM_MACERATOR.left().asStack(), 'C', GTBlocks.CASING_BRONZE_BRICKS.asStack());
            VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_hatch", GTMachines.STEAM_HATCH.asStack(), "BPB", "BTB", "BPB", 'B', new UnificationEntry(TagPrefix.plate, GTMaterials.Bronze), 'P', new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.Bronze), 'T', GTMachines.BRONZE_DRUM.asStack());
            VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_input_bus", GTMachines.STEAM_IMPORT_BUS.asStack(), "C", "H", 'H', GTBlocks.BRONZE_HULL.asStack(), 'C', CustomTags.TAG_WOODEN_CHESTS);
            VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_output_bus", GTMachines.STEAM_EXPORT_BUS.asStack(), "H", "C", 'H', GTBlocks.BRONZE_HULL.asStack(), 'C', CustomTags.TAG_WOODEN_CHESTS);
        }
    }

    private static void disableManualCompression(Consumer<FinishedRecipe> provider) {
        if (!ConfigHolder.INSTANCE.recipes.disableManualCompression) {
            VanillaRecipeHelper.addShapelessRecipe(provider, "nether_quartz_block_to_nether_quartz", new ItemStack(Items.QUARTZ, 4), Blocks.QUARTZ_BLOCK);
        }
    }

    private static void harderBrickRecipes(Consumer<FinishedRecipe> provider) {
        if (ConfigHolder.INSTANCE.recipes.harderBrickRecipes) {
            VanillaRecipeHelper.addShapedRecipe(provider, "brick_from_water", new ItemStack(Blocks.BRICKS, 2), "BBB", "BWB", "BBB",
                    'B', new ItemStack(Items.BRICK),
                    'W', new ItemStack(Items.WATER_BUCKET));

            VanillaRecipeHelper.addShapedRecipe(provider, "bucket_of_concrete", new ItemStack(Concrete.getBucket()),
                    "CBS", "CWQ", " L ",
                    'C', new UnificationEntry(dust, Calcite),
                    'S', new UnificationEntry(dust, Stone),
                    'W', new ItemStack(Items.WATER_BUCKET),
                    'Q', new UnificationEntry(dust, QuartzSand),
                    'L', new UnificationEntry(dust, Clay),
                    'B', new ItemStack(Items.BUCKET));

            VanillaRecipeHelper.addShapedRecipe(provider, "casing_primitive_bricks", GTBlocks.CASING_PRIMITIVE_BRICKS.asStack(),
                    "BGB", "BCB", "BGB",
                    'B', GTItems.FIRECLAY_BRICK.asStack(),
                    'G', new UnificationEntry(dust, Gypsum),
                    'C', new ItemStack(Concrete.getBucket()));

            VanillaRecipeHelper.addShapelessRecipe(provider, "compressed_clay", COMPRESSED_CLAY.asStack(), WOODEN_FORM_BRICK.asStack(), new ItemStack(Items.CLAY_BALL));
            VanillaRecipeHelper.addSmeltingRecipe(provider, "brick_from_compressed_clay", COMPRESSED_CLAY.asStack(), new ItemStack(Items.BRICK), 0.3f);
        } else {
            VanillaRecipeHelper.addShapedRecipe(provider, "casing_primitive_bricks", GTBlocks.CASING_PRIMITIVE_BRICKS.asStack(),
                    "XX", "XX",
                    'X', GTItems.FIRECLAY_BRICK);
        }
    }

    private static void nerfWoodCrafting(Consumer<FinishedRecipe> provider) {
        boolean nerfed = ConfigHolder.INSTANCE.recipes.nerfWoodCrafting;
        if (nerfed) {
            VanillaRecipeHelper.addShapedRecipe(provider, "stick_saw", new ItemStack(Items.STICK, 4), "s", "P", "P", 'P', ItemTags.PLANKS);
            VanillaRecipeHelper.addShapedRecipe(provider, "stick_normal", new ItemStack(Items.STICK, 2), "P", "P", 'P', ItemTags.PLANKS);
            VanillaRecipeHelper.addShapedRecipe(provider, "treated_wood_stick_saw", ChemicalHelper.get(rod, TreatedWood, 4), "s", "P", "P", 'P', GTBlocks.TREATED_WOOD_PLANK.asStack());
            VanillaRecipeHelper.addShapedRecipe(provider, "treated_wood_stick_normal", ChemicalHelper.get(rod, TreatedWood, 2), "P", "P", 'P', GTBlocks.TREATED_WOOD_PLANK.asStack());

            VanillaRecipeHelper.addShapedRecipe(provider, "oak_planks", new ItemStack(Items.OAK_PLANKS, 2), "L", 'L', Items.OAK_LOG.getDefaultInstance());
            VanillaRecipeHelper.addShapedRecipe(provider, "spruce_planks", new ItemStack(Items.SPRUCE_PLANKS, 2), "L", 'L', Items.SPRUCE_LOG.getDefaultInstance());
            VanillaRecipeHelper.addShapedRecipe(provider, "birch_planks", new ItemStack(Items.BIRCH_PLANKS, 2), "L", 'L', Items.BIRCH_LOG.getDefaultInstance());
            VanillaRecipeHelper.addShapedRecipe(provider, "jungle_planks", new ItemStack(Items.JUNGLE_PLANKS, 2), "L", 'L', Items.JUNGLE_LOG.getDefaultInstance());
            VanillaRecipeHelper.addShapedRecipe(provider, "acacia_planks", new ItemStack(Items.ACACIA_PLANKS, 2), "L", 'L', Items.ACACIA_LOG.getDefaultInstance());
            VanillaRecipeHelper.addShapedRecipe(provider, "dark_oak_planks", new ItemStack(Items.DARK_OAK_PLANKS, 2), "L", 'L', Items.DARK_OAK_LOG.getDefaultInstance());
            VanillaRecipeHelper.addShapedRecipe(provider, "mangrove_planks", new ItemStack(Items.MANGROVE_PLANKS, 2), "L", 'L', Items.MANGROVE_LOG.getDefaultInstance());
            VanillaRecipeHelper.addShapedRecipe(provider, "crimson_planks", new ItemStack(Items.CRIMSON_PLANKS, 2), "L", 'L', Items.CRIMSON_STEM.getDefaultInstance());
            VanillaRecipeHelper.addShapedRecipe(provider, "warped_planks", new ItemStack(Items.WARPED_PLANKS, 2), "L", 'L', Items.WARPED_STEM.getDefaultInstance());
        } else {
            VanillaRecipeHelper.addShapedRecipe(provider, "treated_wood_stick_normal", ChemicalHelper.get(rod, TreatedWood, 4), "L", "L", 'L', GTBlocks.TREATED_WOOD_PLANK.asStack());
        }

        VanillaRecipeHelper.addShapedRecipe(provider, "oak_planks_saw", new ItemStack(Items.OAK_PLANKS, nerfed ? 4 : 6), "s", "L", 'L', Items.OAK_LOG.getDefaultInstance());
        VanillaRecipeHelper.addShapedRecipe(provider, "spruce_planks_saw", new ItemStack(Items.SPRUCE_PLANKS, nerfed ? 4 : 6), "s", "L", 'L', Items.SPRUCE_LOG.getDefaultInstance());
        VanillaRecipeHelper.addShapedRecipe(provider, "birch_planks_saw", new ItemStack(Items.BIRCH_PLANKS, nerfed ? 4 : 6), "s", "L", 'L', Items.BIRCH_LOG.getDefaultInstance());
        VanillaRecipeHelper.addShapedRecipe(provider, "jungle_planks_saw", new ItemStack(Items.JUNGLE_PLANKS, nerfed ? 4 : 6), "s", "L", 'L', Items.JUNGLE_LOG.getDefaultInstance());
        VanillaRecipeHelper.addShapedRecipe(provider, "acacia_planks_saw", new ItemStack(Items.ACACIA_PLANKS, nerfed ? 4 : 6), "s", "L", 'L', Items.ACACIA_LOG.getDefaultInstance());
        VanillaRecipeHelper.addShapedRecipe(provider, "dark_oak_planks_saw", new ItemStack(Items.DARK_OAK_PLANKS, nerfed ? 4 : 6), "s", "L", 'L', Items.DARK_OAK_LOG.getDefaultInstance());
        VanillaRecipeHelper.addShapedRecipe(provider, "mangrove_planks_saw", new ItemStack(Items.MANGROVE_PLANKS, nerfed ? 4 : 6), "s", "L", 'L', Items.MANGROVE_LOG.getDefaultInstance());
        VanillaRecipeHelper.addShapedRecipe(provider, "crimson_planks_saw", new ItemStack(Items.CRIMSON_PLANKS, nerfed ? 4 : 6), "s", "L", 'L', Items.CRIMSON_STEM.getDefaultInstance());
        VanillaRecipeHelper.addShapedRecipe(provider, "warped_planks_saw", new ItemStack(Items.WARPED_PLANKS, nerfed ? 4 : 6), "s", "L", 'L', Items.WARPED_STEM.getDefaultInstance());
    }

    private static void hardWoodRecipes(Consumer<FinishedRecipe> provider) {
        VanillaRecipeHelper.addShapedRecipe(provider, "ladder", new ItemStack(Blocks.LADDER, 2), "SrS", "SRS", "ShS", 'S', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood), 'R', new UnificationEntry(TagPrefix.bolt, GTMaterials.Wood));


        VanillaRecipeHelper.addShapedRecipe(provider, "oak_door", new ItemStack(Items.OAK_DOOR), "PTd", "PRS", "PPs",
                'P', new ItemStack(Blocks.OAK_PLANKS),
                'T', new ItemStack(Blocks.OAK_TRAPDOOR),
                'R', new UnificationEntry(TagPrefix.ring, GTMaterials.Iron),
                'S', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron)
        );

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("oak_door")
                .inputItems(new ItemStack(Blocks.OAK_TRAPDOOR))
                .inputItems(new ItemStack(Blocks.OAK_PLANKS, 4))
                .inputFluids(GTMaterials.Iron.getFluid(GTValues.L / 9))
                .outputItems(new ItemStack(Items.OAK_DOOR))
                .duration(400).EUt(4).save(provider);


        VanillaRecipeHelper.addShapedRecipe(provider, "spruce_door", new ItemStack(Items.SPRUCE_DOOR), "PTd", "PRS", "PPs",
                'P', new ItemStack(Blocks.SPRUCE_PLANKS),
                'T', new ItemStack(Blocks.SPRUCE_TRAPDOOR),
                'R', new UnificationEntry(TagPrefix.ring, GTMaterials.Iron),
                'S', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron)
        );

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("spruce_door")
                .inputItems(new ItemStack(Blocks.SPRUCE_TRAPDOOR))
                .inputItems(new ItemStack(Blocks.SPRUCE_PLANKS, 4))
                .inputFluids(GTMaterials.Iron.getFluid(GTValues.L / 9))
                .outputItems(new ItemStack(Items.SPRUCE_DOOR))
                .duration(400).EUt(4).save(provider);


        VanillaRecipeHelper.addShapedRecipe(provider, "birch_door", new ItemStack(Items.BIRCH_DOOR), "PTd", "PRS", "PPs",
                'P', new ItemStack(Blocks.BIRCH_PLANKS),
                'T', new ItemStack(Blocks.BIRCH_TRAPDOOR),
                'R', new UnificationEntry(TagPrefix.ring, GTMaterials.Iron),
                'S', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron)
        );

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("birch_door")
                .inputItems(new ItemStack(Blocks.BIRCH_TRAPDOOR))
                .inputItems(new ItemStack(Blocks.BIRCH_PLANKS, 4))
                .inputFluids(GTMaterials.Iron.getFluid(GTValues.L / 9))
                .outputItems(new ItemStack(Items.BIRCH_DOOR))
                .duration(400).EUt(4).save(provider);


        VanillaRecipeHelper.addShapedRecipe(provider, "jungle_door", new ItemStack(Items.JUNGLE_DOOR), "PTd", "PRS", "PPs",
                'P', new ItemStack(Blocks.JUNGLE_PLANKS),
                'T', new ItemStack(Blocks.JUNGLE_TRAPDOOR),
                'R', new UnificationEntry(TagPrefix.ring, GTMaterials.Iron),
                'S', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron)
        );

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("jungle_door")
                .inputItems(new ItemStack(Blocks.JUNGLE_TRAPDOOR))
                .inputItems(new ItemStack(Blocks.JUNGLE_PLANKS, 4))
                .inputFluids(GTMaterials.Iron.getFluid(GTValues.L / 9))
                .outputItems(new ItemStack(Items.JUNGLE_DOOR))
                .duration(400).EUt(4).save(provider);


        VanillaRecipeHelper.addShapedRecipe(provider, "acacia_door", new ItemStack(Items.ACACIA_DOOR), "PTd", "PRS", "PPs",
                'P', new ItemStack(Blocks.ACACIA_PLANKS),
                'T', new ItemStack(Blocks.ACACIA_TRAPDOOR),
                'R', new UnificationEntry(TagPrefix.ring, GTMaterials.Iron),
                'S', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron)
        );

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("acacia_door")
                .inputItems(new ItemStack(Blocks.ACACIA_TRAPDOOR))
                .inputItems(new ItemStack(Blocks.ACACIA_PLANKS, 4))
                .inputFluids(GTMaterials.Iron.getFluid(GTValues.L / 9))
                .outputItems(new ItemStack(Items.ACACIA_DOOR))
                .duration(400).EUt(4).save(provider);


        VanillaRecipeHelper.addShapedRecipe(provider, "dark_oak_door", new ItemStack(Items.DARK_OAK_DOOR), "PTd", "PRS", "PPs",
                'P', new ItemStack(Blocks.DARK_OAK_PLANKS),
                'T', new ItemStack(Blocks.DARK_OAK_TRAPDOOR),
                'R', new UnificationEntry(TagPrefix.ring, GTMaterials.Iron),
                'S', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron)
        );

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("dark_oak_door")
                .inputItems(new ItemStack(Blocks.DARK_OAK_TRAPDOOR))
                .inputItems(new ItemStack(Blocks.DARK_OAK_PLANKS, 4))
                .inputFluids(GTMaterials.Iron.getFluid(GTValues.L / 9))
                .outputItems(new ItemStack(Items.DARK_OAK_DOOR))
                .duration(400).EUt(4).save(provider);

        VanillaRecipeHelper.addShapedRecipe(provider, "mangrove_door", new ItemStack(Items.MANGROVE_DOOR), "PTd", "PRS", "PPs",
                'P', new ItemStack(Blocks.MANGROVE_PLANKS),
                'T', new ItemStack(Blocks.MANGROVE_TRAPDOOR),
                'R', new UnificationEntry(TagPrefix.ring, GTMaterials.Iron),
                'S', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron)
        );

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("mangrove_door")
                .inputItems(new ItemStack(Blocks.MANGROVE_PLANKS, 4))
                .inputItems(new ItemStack(Blocks.MANGROVE_TRAPDOOR))
                .inputFluids(GTMaterials.Iron.getFluid(GTValues.L / 9))
                .outputItems(new ItemStack(Items.MANGROVE_DOOR))
                .duration(400).EUt(4).save(provider);

        VanillaRecipeHelper.addShapedRecipe(provider, "crimson_door", new ItemStack(Items.CRIMSON_DOOR), "PTd", "PRS", "PPs",
                'P', new ItemStack(Blocks.CRIMSON_PLANKS),
                'T', new ItemStack(Blocks.CRIMSON_TRAPDOOR),
                'R', new UnificationEntry(TagPrefix.ring, GTMaterials.Iron),
                'S', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron)
        );

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("crimson_door")
                .inputItems(new ItemStack(Blocks.CRIMSON_TRAPDOOR))
                .inputItems(new ItemStack(Blocks.CRIMSON_PLANKS, 4))
                .inputFluids(GTMaterials.Iron.getFluid(GTValues.L / 9))
                .outputItems(new ItemStack(Items.CRIMSON_DOOR))
                .duration(400).EUt(4).save(provider);

        VanillaRecipeHelper.addShapedRecipe(provider, "warped_door", new ItemStack(Items.WARPED_DOOR), "PTd", "PRS", "PPs",
                'P', new ItemStack(Blocks.WARPED_PLANKS),
                'T', new ItemStack(Blocks.WARPED_TRAPDOOR),
                'R', new UnificationEntry(TagPrefix.ring, GTMaterials.Iron),
                'S', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron)
        );

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("warped_door")
                .inputItems(new ItemStack(Blocks.WARPED_TRAPDOOR))
                .inputItems(new ItemStack(Blocks.WARPED_PLANKS, 4))
                .inputFluids(GTMaterials.Iron.getFluid(GTValues.L / 9))
                .outputItems(new ItemStack(Items.WARPED_DOOR))
                .duration(400).EUt(4).save(provider);

        VanillaRecipeHelper.addShapedRecipe(provider, "bamboo_door", new ItemStack(Items.BAMBOO_DOOR), "PTd", "PRS", "PPs",
                'P', new ItemStack(Blocks.BAMBOO_PLANKS),
                'T', new ItemStack(Blocks.BAMBOO_TRAPDOOR),
                'R', new UnificationEntry(TagPrefix.ring, GTMaterials.Iron),
                'S', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron)
        );

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("bamboo_door")
                .inputItems(new ItemStack(Blocks.BAMBOO_TRAPDOOR))
                .inputItems(new ItemStack(Blocks.BAMBOO_PLANKS, 4))
                .inputFluids(GTMaterials.Iron.getFluid(GTValues.L / 9))
                .outputItems(new ItemStack(Items.BAMBOO_DOOR))
                .duration(400).EUt(4).save(provider);

        VanillaRecipeHelper.addShapedRecipe(provider, "cherry_door", new ItemStack(Items.CHERRY_DOOR), "PTd", "PRS", "PPs",
                'P', new ItemStack(Blocks.CHERRY_PLANKS),
                'T', new ItemStack(Blocks.CHERRY_TRAPDOOR),
                'R', new UnificationEntry(TagPrefix.ring, GTMaterials.Iron),
                'S', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron)
        );

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("cherry_door")
                .inputItems(new ItemStack(Blocks.CHERRY_TRAPDOOR))
                .inputItems(new ItemStack(Blocks.CHERRY_PLANKS, 4))
                .inputFluids(GTMaterials.Iron.getFluid(GTValues.L / 9))
                .outputItems(new ItemStack(Items.BAMBOO_DOOR))
                .duration(400).EUt(4).save(provider);

        VanillaRecipeHelper.addShapedRecipe(provider, "oak_trapdoor", new ItemStack(Blocks.OAK_TRAPDOOR), "SRS", "RRR", "SRS",
                'S', new ItemStack(Blocks.OAK_SLAB),
                'R', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood)
        );

        VanillaRecipeHelper.addShapedRecipe(provider, "spruce_trapdoor", new ItemStack(Blocks.SPRUCE_TRAPDOOR), "SRS", "RRR", "SRS",
                'S', new ItemStack(Blocks.SPRUCE_SLAB),
                'R', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood)
        );

        VanillaRecipeHelper.addShapedRecipe(provider, "birch_trapdoor", new ItemStack(Blocks.BIRCH_TRAPDOOR), "SRS", "RRR", "SRS",
                'S', new ItemStack(Blocks.BIRCH_SLAB),
                'R', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood)
        );

        VanillaRecipeHelper.addShapedRecipe(provider, "jungle_trapdoor", new ItemStack(Blocks.JUNGLE_TRAPDOOR), "SRS", "RRR", "SRS",
                'S', new ItemStack(Blocks.JUNGLE_SLAB),
                'R', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood)
        );

        VanillaRecipeHelper.addShapedRecipe(provider, "acacia_trapdoor", new ItemStack(Blocks.ACACIA_TRAPDOOR), "SRS", "RRR", "SRS",
                'S', new ItemStack(Blocks.ACACIA_SLAB),
                'R', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood)
        );

        VanillaRecipeHelper.addShapedRecipe(provider, "dark_oak_trapdoor", new ItemStack(Blocks.DARK_OAK_TRAPDOOR), "SRS", "RRR", "SRS",
                'S', new ItemStack(Blocks.DARK_OAK_SLAB),
                'R', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood)
        );

        VanillaRecipeHelper.addShapedRecipe(provider, "mangrove_trapdoor", new ItemStack(Blocks.MANGROVE_TRAPDOOR), "SRS", "RRR", "SRS",
                'S', new ItemStack(Blocks.MANGROVE_SLAB),
                'R', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood)
        );

        VanillaRecipeHelper.addShapedRecipe(provider, "crimson_trapdoor", new ItemStack(Blocks.CRIMSON_TRAPDOOR), "SRS", "RRR", "SRS",
                'S', new ItemStack(Blocks.CRIMSON_SLAB),
                'R', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood)
        );

        VanillaRecipeHelper.addShapedRecipe(provider, "warped_trapdoor", new ItemStack(Blocks.WARPED_TRAPDOOR), "SRS", "RRR", "SRS",
                'S', new ItemStack(Blocks.WARPED_SLAB),
                'R', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood)
        );

        VanillaRecipeHelper.addShapedRecipe(provider, "bamboo_trapdoor", new ItemStack(Blocks.BAMBOO_TRAPDOOR), "SRS", "RRR", "SRS",
                'S', new ItemStack(Blocks.BAMBOO_SLAB),
                'R', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood)
        );

        VanillaRecipeHelper.addShapedRecipe(provider, "cherry_trapdoor", new ItemStack(Blocks.CHERRY_TRAPDOOR), "SRS", "RRR", "SRS",
                'S', new ItemStack(Blocks.CHERRY_SLAB),
                'R', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood)
        );

        VanillaRecipeHelper.addShapedRecipe(provider, "bowl", new ItemStack(Items.BOWL), "k", "X", 'X', ItemTags.PLANKS);

        VanillaRecipeHelper.addShapedRecipe(provider, "chest", new ItemStack(Blocks.CHEST), "LPL", "PFP", "LPL",
                'L', ItemTags.LOGS,
                'P', ItemTags.PLANKS,
                'F', new ItemStack(Items.FLINT));

        VanillaRecipeHelper.addShapedRecipe(provider, "oak_boat", new ItemStack(Items.OAK_BOAT), "PHP", "PkP", "SSS", 'P', new ItemStack(Blocks.OAK_PLANKS), 'S', new ItemStack(Blocks.OAK_SLAB), 'H', new ItemStack(Items.WOODEN_SHOVEL));
        VanillaRecipeHelper.addShapedRecipe(provider, "spruce_boat", new ItemStack(Items.SPRUCE_BOAT), "PHP", "PkP", "SSS", 'P', new ItemStack(Blocks.SPRUCE_PLANKS), 'S', new ItemStack(Blocks.SPRUCE_SLAB), 'H', new ItemStack(Items.WOODEN_SHOVEL));
        VanillaRecipeHelper.addShapedRecipe(provider, "birch_boat", new ItemStack(Items.BIRCH_BOAT), "PHP", "PkP", "SSS", 'P', new ItemStack(Blocks.BIRCH_PLANKS), 'S', new ItemStack(Blocks.BIRCH_SLAB), 'H', new ItemStack(Items.WOODEN_SHOVEL));
        VanillaRecipeHelper.addShapedRecipe(provider, "jungle_boat", new ItemStack(Items.JUNGLE_BOAT), "PHP", "PkP", "SSS", 'P', new ItemStack(Blocks.JUNGLE_PLANKS), 'S', new ItemStack(Blocks.JUNGLE_SLAB), 'H', new ItemStack(Items.WOODEN_SHOVEL));
        VanillaRecipeHelper.addShapedRecipe(provider, "acacia_boat", new ItemStack(Items.ACACIA_BOAT), "PHP", "PkP", "SSS", 'P', new ItemStack(Blocks.ACACIA_PLANKS), 'S', new ItemStack(Blocks.ACACIA_SLAB), 'H', new ItemStack(Items.WOODEN_SHOVEL));
        VanillaRecipeHelper.addShapedRecipe(provider, "dark_oak_boat", new ItemStack(Items.DARK_OAK_BOAT), "PHP", "PkP", "SSS", 'P', new ItemStack(Blocks.DARK_OAK_PLANKS), 'S', new ItemStack(Blocks.DARK_OAK_SLAB), 'H', new ItemStack(Items.WOODEN_SHOVEL));
        VanillaRecipeHelper.addShapedRecipe(provider, "mangrove_boat", new ItemStack(Items.MANGROVE_BOAT), "PHP", "PkP", "SSS", 'P', new ItemStack(Blocks.MANGROVE_PLANKS), 'S', new ItemStack(Blocks.MANGROVE_SLAB), 'H', new ItemStack(Items.WOODEN_SHOVEL));
        VanillaRecipeHelper.addShapedRecipe(provider, "bamboo_raft", new ItemStack(Items.BAMBOO_RAFT), "PHP", "PkP", "SSS", 'P', new ItemStack(Blocks.BAMBOO_PLANKS), 'S', new ItemStack(Blocks.BAMBOO_SLAB), 'H', new ItemStack(Items.WOODEN_SHOVEL));
        VanillaRecipeHelper.addShapedRecipe(provider, "cherry_boat", new ItemStack(Items.CHERRY_BOAT), "PHP", "PkP", "SSS", 'P', new ItemStack(Blocks.CHERRY_PLANKS), 'S', new ItemStack(Blocks.CHERRY_SLAB), 'H', new ItemStack(Items.WOODEN_SHOVEL));

        VanillaRecipeHelper.addShapedRecipe(provider, "oak_fence", new ItemStack(Blocks.OAK_FENCE), "PSP", "PSP", "PSP", 'P', new ItemStack(Blocks.OAK_PLANKS), 'S', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood));
        VanillaRecipeHelper.addShapedRecipe(provider, "spruce_fence", new ItemStack(Blocks.SPRUCE_FENCE), "PSP", "PSP", "PSP", 'P', new ItemStack(Blocks.SPRUCE_PLANKS), 'S', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood));
        VanillaRecipeHelper.addShapedRecipe(provider, "birch_fence", new ItemStack(Blocks.BIRCH_FENCE), "PSP", "PSP", "PSP", 'P', new ItemStack(Blocks.BIRCH_PLANKS), 'S', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood));
        VanillaRecipeHelper.addShapedRecipe(provider, "jungle_fence", new ItemStack(Blocks.JUNGLE_FENCE), "PSP", "PSP", "PSP", 'P', new ItemStack(Blocks.JUNGLE_PLANKS), 'S', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood));
        VanillaRecipeHelper.addShapedRecipe(provider, "acacia_fence", new ItemStack(Blocks.ACACIA_FENCE), "PSP", "PSP", "PSP", 'P', new ItemStack(Blocks.ACACIA_PLANKS), 'S', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood));
        VanillaRecipeHelper.addShapedRecipe(provider, "dark_oak_fence", new ItemStack(Blocks.DARK_OAK_FENCE), "PSP", "PSP", "PSP", 'P', new ItemStack(Blocks.DARK_OAK_PLANKS), 'S', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood));
        VanillaRecipeHelper.addShapedRecipe(provider, "mangrove_fence", new ItemStack(Blocks.MANGROVE_FENCE), "PSP", "PSP", "PSP", 'P', new ItemStack(Blocks.MANGROVE_PLANKS), 'S', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood));
        VanillaRecipeHelper.addShapedRecipe(provider, "crimson_fence", new ItemStack(Blocks.CRIMSON_FENCE), "PSP", "PSP", "PSP", 'P', new ItemStack(Blocks.CRIMSON_PLANKS), 'S', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood));
        VanillaRecipeHelper.addShapedRecipe(provider, "warped_fence", new ItemStack(Blocks.WARPED_FENCE), "PSP", "PSP", "PSP", 'P', new ItemStack(Blocks.WARPED_PLANKS), 'S', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood));
        VanillaRecipeHelper.addShapedRecipe(provider, "bamboo_fence", new ItemStack(Blocks.BAMBOO_FENCE), "PSP", "PSP", "PSP", 'P', new ItemStack(Blocks.BAMBOO_PLANKS), 'S', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood));
        VanillaRecipeHelper.addShapedRecipe(provider, "cherry_fence", new ItemStack(Blocks.CHERRY_FENCE), "PSP", "PSP", "PSP", 'P', new ItemStack(Blocks.CHERRY_PLANKS), 'S', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood));

        VanillaRecipeHelper.addShapedRecipe(provider, "oak_fence_gate", new ItemStack(Blocks.OAK_FENCE_GATE), "F F", "SPS", "SPS", 'P', new ItemStack(Blocks.OAK_PLANKS), 'S', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood), 'F', new ItemStack(Items.FLINT));
        VanillaRecipeHelper.addShapedRecipe(provider, "spruce_fence_gate", new ItemStack(Blocks.SPRUCE_FENCE_GATE), "F F", "SPS", "SPS", 'P', new ItemStack(Blocks.SPRUCE_PLANKS), 'S', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood), 'F', new ItemStack(Items.FLINT));
        VanillaRecipeHelper.addShapedRecipe(provider, "birch_fence_gate", new ItemStack(Blocks.BIRCH_FENCE_GATE), "F F", "SPS", "SPS", 'P', new ItemStack(Blocks.BIRCH_PLANKS), 'S', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood), 'F', new ItemStack(Items.FLINT));
        VanillaRecipeHelper.addShapedRecipe(provider, "jungle_fence_gate", new ItemStack(Blocks.JUNGLE_FENCE_GATE), "F F", "SPS", "SPS", 'P', new ItemStack(Blocks.JUNGLE_PLANKS), 'S', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood), 'F', new ItemStack(Items.FLINT));
        VanillaRecipeHelper.addShapedRecipe(provider, "acacia_fence_gate", new ItemStack(Blocks.ACACIA_FENCE_GATE), "F F", "SPS", "SPS", 'P', new ItemStack(Blocks.ACACIA_PLANKS), 'S', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood), 'F', new ItemStack(Items.FLINT));
        VanillaRecipeHelper.addShapedRecipe(provider, "dark_oak_fence_gate", new ItemStack(Blocks.DARK_OAK_FENCE_GATE), "F F", "SPS", "SPS", 'P', new ItemStack(Blocks.DARK_OAK_PLANKS), 'S', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood), 'F', new ItemStack(Items.FLINT));
        VanillaRecipeHelper.addShapedRecipe(provider, "mangrove_fence_gate", new ItemStack(Blocks.MANGROVE_FENCE_GATE), "F F", "SPS", "SPS", 'P', new ItemStack(Blocks.MANGROVE_PLANKS), 'S', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood), 'F', new ItemStack(Items.FLINT));
        VanillaRecipeHelper.addShapedRecipe(provider, "crimson_fence_gate", new ItemStack(Blocks.CRIMSON_FENCE_GATE), "F F", "SPS", "SPS", 'P', new ItemStack(Blocks.CRIMSON_PLANKS), 'S', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood), 'F', new ItemStack(Items.FLINT));
        VanillaRecipeHelper.addShapedRecipe(provider, "warped_fence_gate", new ItemStack(Blocks.WARPED_FENCE_GATE), "F F", "SPS", "SPS", 'P', new ItemStack(Blocks.WARPED_PLANKS), 'S', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood), 'F', new ItemStack(Items.FLINT));
        VanillaRecipeHelper.addShapedRecipe(provider, "bamboo_fence_gate", new ItemStack(Blocks.BAMBOO_FENCE_GATE), "F F", "SPS", "SPS", 'P', new ItemStack(Blocks.BAMBOO_PLANKS), 'S', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood), 'F', new ItemStack(Items.FLINT));
        VanillaRecipeHelper.addShapedRecipe(provider, "cherry_fence_gate", new ItemStack(Blocks.CHERRY_FENCE_GATE), "F F", "SPS", "SPS", 'P', new ItemStack(Blocks.CHERRY_PLANKS), 'S', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood), 'F', new ItemStack(Items.FLINT));

        VanillaRecipeHelper.addShapedRecipe(provider, "oak_fence_gate_screws", new ItemStack(Blocks.OAK_FENCE_GATE, 2), "IdI", "SPS", "SPS", 'P', new ItemStack(Blocks.OAK_PLANKS), 'S', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood), 'I', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron));
        VanillaRecipeHelper.addShapedRecipe(provider, "spruce_fence_gate_screws", new ItemStack(Blocks.SPRUCE_FENCE_GATE, 2), "IdI", "SPS", "SPS", 'P', new ItemStack(Blocks.SPRUCE_PLANKS), 'S', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood), 'I', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron));
        VanillaRecipeHelper.addShapedRecipe(provider, "birch_fence_gate_screws", new ItemStack(Blocks.BIRCH_FENCE_GATE, 2), "IdI", "SPS", "SPS", 'P', new ItemStack(Blocks.BIRCH_PLANKS), 'S', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood), 'I', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron));
        VanillaRecipeHelper.addShapedRecipe(provider, "jungle_fence_gate_screws", new ItemStack(Blocks.JUNGLE_FENCE_GATE, 2), "IdI", "SPS", "SPS", 'P', new ItemStack(Blocks.JUNGLE_PLANKS), 'S', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood), 'I', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron));
        VanillaRecipeHelper.addShapedRecipe(provider, "acacia_fence_gate_screws", new ItemStack(Blocks.ACACIA_FENCE_GATE, 2), "IdI", "SPS", "SPS", 'P', new ItemStack(Blocks.ACACIA_PLANKS), 'S', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood), 'I', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron));
        VanillaRecipeHelper.addShapedRecipe(provider, "dark_oak_fence_gate_screws", new ItemStack(Blocks.DARK_OAK_FENCE_GATE, 2), "IdI", "SPS", "SPS", 'P', new ItemStack(Blocks.DARK_OAK_PLANKS), 'S', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood), 'I', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron));
        VanillaRecipeHelper.addShapedRecipe(provider, "mangrove_fence_gate_screws", new ItemStack(Blocks.MANGROVE_FENCE_GATE, 2), "IdI", "SPS", "SPS", 'P', new ItemStack(Blocks.MANGROVE_PLANKS), 'S', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood), 'I', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron));
        VanillaRecipeHelper.addShapedRecipe(provider, "crimson_fence_gate_screws", new ItemStack(Blocks.CRIMSON_FENCE_GATE, 2), "IdI", "SPS", "SPS", 'P', new ItemStack(Blocks.CRIMSON_PLANKS), 'S', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood), 'I', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron));
        VanillaRecipeHelper.addShapedRecipe(provider, "warped_fence_gate_screws", new ItemStack(Blocks.WARPED_FENCE_GATE, 2), "IdI", "SPS", "SPS", 'P', new ItemStack(Blocks.WARPED_PLANKS), 'S', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood), 'I', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron));
        VanillaRecipeHelper.addShapedRecipe(provider, "bamboo_fence_gate_screws", new ItemStack(Blocks.BAMBOO_FENCE_GATE, 2), "IdI", "SPS", "SPS", 'P', new ItemStack(Blocks.BAMBOO_PLANKS), 'S', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood), 'I', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron));
        VanillaRecipeHelper.addShapedRecipe(provider, "cherry_fence_gate_screws", new ItemStack(Blocks.CHERRY_FENCE_GATE, 2), "IdI", "SPS", "SPS", 'P', new ItemStack(Blocks.CHERRY_PLANKS), 'S', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood), 'I', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron));
    }

    private static void hardIronRecipes(Consumer<FinishedRecipe> provider) {
        VanillaRecipeHelper.addShapedRecipe(provider, "cauldron", new ItemStack(Items.CAULDRON), "X X", "XhX", "XXX",
                'X', new UnificationEntry(TagPrefix.plate, GTMaterials.Iron)
        );


        VanillaRecipeHelper.addShapedRecipe(provider, "hopper", new ItemStack(Blocks.HOPPER), "XCX", "XGX", "wXh",
                'X', new UnificationEntry(TagPrefix.plate, GTMaterials.Iron),
                'C', CustomTags.TAG_WOODEN_CHESTS,
                'G', new UnificationEntry(TagPrefix.gearSmall, GTMaterials.Iron)
        );


        VanillaRecipeHelper.addShapedRecipe(provider, "iron_bars", new ItemStack(Blocks.IRON_BARS, 8), " h ", "XXX", "XXX",
                'X', new UnificationEntry(TagPrefix.rod, GTMaterials.Iron)
        );

        VanillaRecipeHelper.addShapedRecipe(provider, "iron_bucket", new ItemStack(Items.BUCKET), "XhX", " X ", 'X', new UnificationEntry(TagPrefix.plate, GTMaterials.Iron));
    }

    private static void hardRedstoneRecipes(Consumer<FinishedRecipe> provider) {
        if (ConfigHolder.INSTANCE.recipes.hardRedstoneRecipes) {
            VanillaRecipeHelper.addShapedRecipe(provider, "dispenser", new ItemStack(Blocks.DISPENSER), "CRC", "STS", "GAG",
                    'C', ItemTags.STONE_CRAFTING_MATERIALS,
                    'R', new UnificationEntry(TagPrefix.ring, GTMaterials.Iron),
                    'S', new UnificationEntry(TagPrefix.spring, GTMaterials.Iron),
                    'T', new ItemStack(Items.STRING),
                    'G', new UnificationEntry(TagPrefix.gearSmall, GTMaterials.Iron),
                    'A', new UnificationEntry(TagPrefix.rod, GTMaterials.RedAlloy));

            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("dispenser").duration(100).EUt(VA[LV])
                    .inputItems(ItemTags.STONE_CRAFTING_MATERIALS, 2)
                    .inputItems(TagPrefix.ring, GTMaterials.Iron)
                    .inputItems(TagPrefix.spring, GTMaterials.Iron, 2)
                    .inputItems(TagPrefix.gearSmall, GTMaterials.Iron, 2)
                    .inputItems(TagPrefix.rod, GTMaterials.RedAlloy)
                    .inputItems(new ItemStack(Items.STRING))
                    .outputItems(new ItemStack(Blocks.DISPENSER))
                    .save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, "sticky_piston", new ItemStack(Blocks.STICKY_PISTON), "h", "R", "P",
                    'R', new ItemStack(Items.SLIME_BALL),
                    'P', new ItemStack(Blocks.PISTON)
            );

            VanillaRecipeHelper.addShapedRecipe(provider, "piston_iron", new ItemStack(Blocks.PISTON), "WWW", "GFG", "CRC",
                    'W', ItemTags.PLANKS,
                    'C', ItemTags.STONE_CRAFTING_MATERIALS,
                    'R', new UnificationEntry(TagPrefix.plate, GTMaterials.RedAlloy),
                    'G', new UnificationEntry(TagPrefix.gearSmall, GTMaterials.Iron),
                    'F', ItemTags.WOODEN_FENCES
            );

            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("piston_iron")
                    .inputItems(TagPrefix.rod, GTMaterials.Iron)
                    .inputItems(TagPrefix.gearSmall, GTMaterials.Iron)
                    .inputItems(ItemTags.WOODEN_SLABS)
                    .inputItems(ItemTags.STONE_CRAFTING_MATERIALS)
                    .inputFluids(GTMaterials.RedAlloy.getFluid(GTValues.L))
                    .outputItems(new ItemStack(Blocks.PISTON))
                    .duration(240).EUt(VA[ULV]).save(provider);

            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("piston_steel")
                    .inputItems(TagPrefix.rod, GTMaterials.Steel)
                    .inputItems(TagPrefix.gearSmall, GTMaterials.Steel)
                    .inputItems(ItemTags.WOODEN_SLABS, 2)
                    .inputItems(ItemTags.STONE_CRAFTING_MATERIALS, 2)
                    .inputFluids(GTMaterials.RedAlloy.getFluid(GTValues.L * 2))
                    .outputItems(new ItemStack(Blocks.PISTON, 2))
                    .duration(240).EUt(16).save(provider);

            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("piston_aluminium")
                    .inputItems(TagPrefix.rod, GTMaterials.Aluminium)
                    .inputItems(TagPrefix.gearSmall, GTMaterials.Aluminium)
                    .inputItems(ItemTags.WOODEN_SLABS, 4)
                    .inputItems(ItemTags.STONE_CRAFTING_MATERIALS, 4)
                    .inputFluids(GTMaterials.RedAlloy.getFluid(GTValues.L * 3))
                    .outputItems(new ItemStack(Blocks.PISTON, 4))
                    .duration(240).EUt(VA[LV]).save(provider);

            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("piston_stainless_steel")
                    .inputItems(TagPrefix.rod, GTMaterials.StainlessSteel)
                    .inputItems(TagPrefix.gearSmall, GTMaterials.StainlessSteel)
                    .inputItems(ItemTags.WOODEN_SLABS, 8)
                    .inputItems(ItemTags.STONE_CRAFTING_MATERIALS, 8)
                    .inputFluids(GTMaterials.RedAlloy.getFluid(GTValues.L * 4))
                    .outputItems(new ItemStack(Blocks.PISTON, 8))
                    .duration(600).EUt(VA[LV]).save(provider);

            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("piston_titanium")
                    .inputItems(TagPrefix.rod, GTMaterials.Titanium)
                    .inputItems(TagPrefix.gearSmall, GTMaterials.Titanium)
                    .inputItems(ItemTags.WOODEN_SLABS, 16)
                    .inputItems(ItemTags.STONE_CRAFTING_MATERIALS, 16)
                    .inputFluids(GTMaterials.RedAlloy.getFluid(GTValues.L * 8))
                    .outputItems(new ItemStack(Blocks.PISTON, 16))
                    .duration(800).EUt(VA[LV]).save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, "stone_pressure_plate", new ItemStack(Blocks.STONE_PRESSURE_PLATE, 2), "ShS", "LCL", "SdS",
                    'S', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron),
                    'L', new ItemStack(Blocks.STONE_SLAB),
                    'C', new UnificationEntry(TagPrefix.spring, GTMaterials.Iron)
            );

            VanillaRecipeHelper.addShapedRecipe(provider, "oak_pressure_plate", new ItemStack(Blocks.OAK_PRESSURE_PLATE, 2), "SrS", "LCL", "SdS",
                    'S', new UnificationEntry(TagPrefix.bolt, GTMaterials.Wood),
                    'L', Blocks.OAK_SLAB.asItem(),
                    'C', new UnificationEntry(TagPrefix.spring, GTMaterials.Iron)
            );

            VanillaRecipeHelper.addShapedRecipe(provider, "birch_pressure_plate", new ItemStack(Blocks.BIRCH_PRESSURE_PLATE, 2), "SrS", "LCL", "SdS",
                    'S', new UnificationEntry(TagPrefix.bolt, GTMaterials.Wood),
                    'L', Blocks.BIRCH_SLAB.asItem(),
                    'C', new UnificationEntry(TagPrefix.spring, GTMaterials.Iron)
            );

            VanillaRecipeHelper.addShapedRecipe(provider, "spruce_pressure_plate", new ItemStack(Blocks.SPRUCE_PRESSURE_PLATE, 2), "SrS", "LCL", "SdS",
                    'S', new UnificationEntry(TagPrefix.bolt, GTMaterials.Wood),
                    'L', Blocks.SPRUCE_SLAB.asItem(),
                    'C', new UnificationEntry(TagPrefix.spring, GTMaterials.Iron)
            );

            VanillaRecipeHelper.addShapedRecipe(provider, "jungle_pressure_plate", new ItemStack(Blocks.JUNGLE_PRESSURE_PLATE, 2), "SrS", "LCL", "SdS",
                    'S', new UnificationEntry(TagPrefix.bolt, GTMaterials.Wood),
                    'L', Blocks.JUNGLE_SLAB.asItem(),
                    'C', new UnificationEntry(TagPrefix.spring, GTMaterials.Iron)
            );

            VanillaRecipeHelper.addShapedRecipe(provider, "acacia_pressure_plate", new ItemStack(Blocks.ACACIA_PRESSURE_PLATE, 2), "SrS", "LCL", "SdS",
                    'S', new UnificationEntry(TagPrefix.bolt, GTMaterials.Wood),
                    'L', Blocks.ACACIA_SLAB.asItem(),
                    'C', new UnificationEntry(TagPrefix.spring, GTMaterials.Iron)
            );

            VanillaRecipeHelper.addShapedRecipe(provider, "dark_oak_pressure_plate", new ItemStack(Blocks.DARK_OAK_PRESSURE_PLATE, 2), "SrS", "LCL", "SdS",
                    'S', new UnificationEntry(TagPrefix.bolt, GTMaterials.Wood),
                    'L', Blocks.DARK_OAK_SLAB.asItem(),
                    'C', new UnificationEntry(TagPrefix.spring, GTMaterials.Iron)
            );

            VanillaRecipeHelper.addShapedRecipe(provider, "crimson_pressure_plate", new ItemStack(Blocks.CRIMSON_PRESSURE_PLATE, 2), "SrS", "LCL", "SdS",
                    'S', new UnificationEntry(TagPrefix.bolt, GTMaterials.Wood),
                    'L', Blocks.CRIMSON_SLAB.asItem(),
                    'C', new UnificationEntry(TagPrefix.spring, GTMaterials.Iron)
            );

            VanillaRecipeHelper.addShapedRecipe(provider, "warped_pressure_plate", new ItemStack(Blocks.WARPED_PRESSURE_PLATE, 2), "SrS", "LCL", "SdS",
                    'S', new UnificationEntry(TagPrefix.bolt, GTMaterials.Wood),
                    'L', Blocks.WARPED_SLAB.asItem(),
                    'C', new UnificationEntry(TagPrefix.spring, GTMaterials.Iron)
            );

            VanillaRecipeHelper.addShapedRecipe(provider, "mangrove_pressure_plate", new ItemStack(Blocks.MANGROVE_PRESSURE_PLATE, 2), "SrS", "LCL", "SdS",
                    'S', new UnificationEntry(TagPrefix.bolt, GTMaterials.Wood),
                    'L', Blocks.MANGROVE_SLAB.asItem(),
                    'C', new UnificationEntry(TagPrefix.spring, GTMaterials.Iron)
            );

            VanillaRecipeHelper.addShapedRecipe(provider, "heavy_weighted_pressure_plate", new ItemStack(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE), "ShS", "LCL", "SdS",
                    'S', new UnificationEntry(TagPrefix.screw, GTMaterials.Steel),
                    'L', new UnificationEntry(TagPrefix.plate, GTMaterials.Gold),
                    'C', new UnificationEntry(TagPrefix.spring, GTMaterials.Steel)
            );

            VanillaRecipeHelper.addShapedRecipe(provider, "light_weighted_pressure_plate", new ItemStack(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE), "ShS", "LCL", "SdS",
                    'S', new UnificationEntry(TagPrefix.screw, GTMaterials.Steel),
                    'L', new UnificationEntry(TagPrefix.plate, GTMaterials.Iron),
                    'C', new UnificationEntry(TagPrefix.spring, GTMaterials.Steel)
            );

            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("stone_pressure_plate")
                    .inputItems(TagPrefix.spring, GTMaterials.Iron)
                    .inputItems(new ItemStack(Blocks.STONE_SLAB, 2))
                    .outputItems(new ItemStack(Blocks.STONE_PRESSURE_PLATE, 2))
                    .duration(100).EUt(VA[ULV]).save(provider);

            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("oak_pressure_plate")
                    .inputItems(TagPrefix.spring, GTMaterials.Iron)
                    .inputItems(new ItemStack(Blocks.OAK_SLAB, 2))
                    .outputItems(new ItemStack(Blocks.OAK_PRESSURE_PLATE, 2))
                    .duration(100).EUt(VA[ULV]).save(provider);

            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("birch_pressure_plate")
                    .inputItems(TagPrefix.spring, GTMaterials.Iron)
                    .inputItems(new ItemStack(Blocks.BIRCH_SLAB, 2))
                    .outputItems(new ItemStack(Blocks.BIRCH_PRESSURE_PLATE, 2))
                    .duration(100).EUt(VA[ULV]).save(provider);

            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("spruce_pressure_plate")
                    .inputItems(TagPrefix.spring, GTMaterials.Iron)
                    .inputItems(new ItemStack(Blocks.SPRUCE_SLAB, 2))
                    .outputItems(new ItemStack(Blocks.SPRUCE_PRESSURE_PLATE, 2))
                    .duration(100).EUt(VA[ULV]).save(provider);

            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("jungle_pressure_plate")
                    .inputItems(TagPrefix.spring, GTMaterials.Iron)
                    .inputItems(new ItemStack(Blocks.JUNGLE_SLAB, 2))
                    .outputItems(new ItemStack(Blocks.JUNGLE_PRESSURE_PLATE, 2))
                    .duration(100).EUt(VA[ULV]).save(provider);

            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("acacia_pressure_plate")
                    .inputItems(TagPrefix.spring, GTMaterials.Iron)
                    .inputItems(new ItemStack(Blocks.ACACIA_SLAB, 2))
                    .outputItems(new ItemStack(Blocks.ACACIA_PRESSURE_PLATE, 2))
                    .duration(100).EUt(VA[ULV]).save(provider);

            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("dark_oak_pressure_plate")
                    .inputItems(TagPrefix.spring, GTMaterials.Iron)
                    .inputItems(new ItemStack(Blocks.DARK_OAK_SLAB, 2))
                    .outputItems(new ItemStack(Blocks.DARK_OAK_PRESSURE_PLATE, 2))
                    .duration(100).EUt(VA[ULV]).save(provider);

            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("crimson_pressure_plate")
                    .inputItems(TagPrefix.spring, GTMaterials.Iron)
                    .inputItems(new ItemStack(Blocks.CRIMSON_SLAB, 2))
                    .outputItems(new ItemStack(Blocks.CRIMSON_PRESSURE_PLATE, 2))
                    .duration(100).EUt(VA[ULV]).save(provider);

            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("warped_pressure_plate")
                    .inputItems(TagPrefix.spring, GTMaterials.Iron)
                    .inputItems(new ItemStack(Blocks.WARPED_SLAB, 2))
                    .outputItems(new ItemStack(Blocks.WARPED_PRESSURE_PLATE, 2))
                    .duration(100).EUt(VA[ULV]).save(provider);

            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("mangrove_pressure_plate")
                    .inputItems(TagPrefix.spring, GTMaterials.Iron)
                    .inputItems(new ItemStack(Blocks.MANGROVE_SLAB, 2))
                    .outputItems(new ItemStack(Blocks.MANGROVE_PRESSURE_PLATE, 2))
                    .duration(100).EUt(VA[ULV]).save(provider);

            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("light_weighted_pressure_plate")
                    .inputItems(TagPrefix.spring, GTMaterials.Steel)
                    .inputItems(TagPrefix.plate, GTMaterials.Gold)
                    .outputItems(new ItemStack(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE))
                    .duration(200).EUt(16).save(provider);

            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("heavy_weighted_pressure_plate")
                    .inputItems(TagPrefix.spring, GTMaterials.Steel)
                    .inputItems(TagPrefix.plate, GTMaterials.Iron)
                    .outputItems(new ItemStack(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE))
                    .duration(200).EUt(16).save(provider);


            VanillaRecipeHelper.addShapedRecipe(provider, "stone_button", new ItemStack(Blocks.STONE_BUTTON, 6), "sP",
                    'P', new ItemStack(Blocks.STONE_PRESSURE_PLATE));

            VanillaRecipeHelper.addShapedRecipe(provider, "oak_button", new ItemStack(Blocks.OAK_BUTTON, 6), "sP",
                    'P', new ItemStack(Blocks.OAK_PRESSURE_PLATE));

            VanillaRecipeHelper.addShapedRecipe(provider, "birch_button", new ItemStack(Blocks.BIRCH_BUTTON, 6), "sP",
                    'P', new ItemStack(Blocks.BIRCH_PRESSURE_PLATE));

            VanillaRecipeHelper.addShapedRecipe(provider, "spruce_button", new ItemStack(Blocks.SPRUCE_BUTTON, 6), "sP",
                    'P', new ItemStack(Blocks.SPRUCE_PRESSURE_PLATE));

            VanillaRecipeHelper.addShapedRecipe(provider, "jungle_button", new ItemStack(Blocks.JUNGLE_BUTTON, 6), "sP",
                    'P', new ItemStack(Blocks.JUNGLE_PRESSURE_PLATE));

            VanillaRecipeHelper.addShapedRecipe(provider, "acacia_button", new ItemStack(Blocks.ACACIA_BUTTON, 6), "sP",
                    'P', new ItemStack(Blocks.ACACIA_PRESSURE_PLATE));

            VanillaRecipeHelper.addShapedRecipe(provider, "dark_oak_button", new ItemStack(Blocks.DARK_OAK_BUTTON, 6), "sP",
                    'P', new ItemStack(Blocks.DARK_OAK_PRESSURE_PLATE));

            VanillaRecipeHelper.addShapedRecipe(provider, "crimson_button", new ItemStack(Blocks.CRIMSON_BUTTON, 6), "sP",
                    'P', new ItemStack(Blocks.CRIMSON_PRESSURE_PLATE));

            VanillaRecipeHelper.addShapedRecipe(provider, "warped_button", new ItemStack(Blocks.WARPED_BUTTON, 6), "sP",
                    'P', new ItemStack(Blocks.WARPED_PRESSURE_PLATE));

            VanillaRecipeHelper.addShapedRecipe(provider, "mangrove_button", new ItemStack(Blocks.MANGROVE_BUTTON, 6), "sP",
                    'P', new ItemStack(Blocks.MANGROVE_PRESSURE_PLATE));

            GTRecipeTypes.CUTTER_RECIPES.recipeBuilder("stone_button")
                    .inputItems(new ItemStack(Blocks.STONE_PRESSURE_PLATE))
                    .outputItems(new ItemStack(Blocks.STONE_BUTTON, 12))
                    .duration(25).EUt(VA[ULV]).save(provider);

            GTRecipeTypes.CUTTER_RECIPES.recipeBuilder("oak_button")
                    .inputItems(new ItemStack(Blocks.OAK_PRESSURE_PLATE))
                    .outputItems(new ItemStack(Blocks.OAK_BUTTON, 12))
                    .duration(25).EUt(VA[ULV]).save(provider);

            GTRecipeTypes.CUTTER_RECIPES.recipeBuilder("birch_button")
                    .inputItems(new ItemStack(Blocks.BIRCH_PRESSURE_PLATE))
                    .outputItems(new ItemStack(Blocks.BIRCH_BUTTON, 12))
                    .duration(25).EUt(VA[ULV]).save(provider);

            GTRecipeTypes.CUTTER_RECIPES.recipeBuilder("spruce_button")
                    .inputItems(new ItemStack(Blocks.SPRUCE_PRESSURE_PLATE))
                    .outputItems(new ItemStack(Blocks.SPRUCE_BUTTON, 12))
                    .duration(25).EUt(VA[ULV]).save(provider);

            GTRecipeTypes.CUTTER_RECIPES.recipeBuilder("jungle_button")
                    .inputItems(new ItemStack(Blocks.JUNGLE_PRESSURE_PLATE))
                    .outputItems(new ItemStack(Blocks.JUNGLE_BUTTON, 12))
                    .duration(25).EUt(VA[ULV]).save(provider);

            GTRecipeTypes.CUTTER_RECIPES.recipeBuilder("acacia_button")
                    .inputItems(new ItemStack(Blocks.ACACIA_PRESSURE_PLATE))
                    .outputItems(new ItemStack(Blocks.ACACIA_BUTTON, 12))
                    .duration(25).EUt(VA[ULV]).save(provider);

            GTRecipeTypes.CUTTER_RECIPES.recipeBuilder("dark_oak_button")
                    .inputItems(new ItemStack(Blocks.DARK_OAK_PRESSURE_PLATE))
                    .outputItems(new ItemStack(Blocks.DARK_OAK_BUTTON, 12))
                    .duration(25).EUt(VA[ULV]).save(provider);

            GTRecipeTypes.CUTTER_RECIPES.recipeBuilder("crimson_button")
                    .inputItems(new ItemStack(Blocks.CRIMSON_PRESSURE_PLATE))
                    .outputItems(new ItemStack(Blocks.CRIMSON_BUTTON, 12))
                    .duration(25).EUt(VA[ULV]).save(provider);

            GTRecipeTypes.CUTTER_RECIPES.recipeBuilder("warped_button")
                    .inputItems(new ItemStack(Blocks.WARPED_PRESSURE_PLATE))
                    .outputItems(new ItemStack(Blocks.WARPED_BUTTON, 12))
                    .duration(25).EUt(VA[ULV]).save(provider);

            GTRecipeTypes.CUTTER_RECIPES.recipeBuilder("mangrove_button")
                    .inputItems(new ItemStack(Blocks.MANGROVE_PRESSURE_PLATE))
                    .outputItems(new ItemStack(Blocks.MANGROVE_BUTTON, 12))
                    .duration(25).EUt(VA[ULV]).save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, "lever", new ItemStack(Blocks.LEVER), "B", "S",
                    'B', new ItemStack(Blocks.STONE_BUTTON),
                    'S', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood)
            );

            VanillaRecipeHelper.addShapedRecipe(provider, "daylight_detector", new ItemStack(Blocks.DAYLIGHT_DETECTOR), "GGG", "PPP", "SRS",
                    'G', new ItemStack(Blocks.GLASS),
                    'P', new UnificationEntry(TagPrefix.plate, GTMaterials.NetherQuartz),
                    'S', ItemTags.WOODEN_SLABS,
                    'R', new UnificationEntry(TagPrefix.rod, GTMaterials.RedAlloy)
            );

            VanillaRecipeHelper.addShapedRecipe(provider, "daylight_detector_certus", new ItemStack(Blocks.DAYLIGHT_DETECTOR), "GGG", "PPP", "SRS",
                    'G', new ItemStack(Blocks.GLASS),
                    'P', new UnificationEntry(TagPrefix.plate, GTMaterials.CertusQuartz),
                    'S', ItemTags.WOODEN_SLABS,
                    'R', new UnificationEntry(TagPrefix.rod, GTMaterials.RedAlloy)
            );

            VanillaRecipeHelper.addShapedRecipe(provider, "daylight_detector_quartzite", new ItemStack(Blocks.DAYLIGHT_DETECTOR), "GGG", "PPP", "SRS",
                    'G', new ItemStack(Blocks.GLASS, 1),
                    'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Quartzite),
                    'S', ItemTags.WOODEN_SLABS,
                    'R', new UnificationEntry(TagPrefix.rod, GTMaterials.RedAlloy)
            );

            VanillaRecipeHelper.addShapedRecipe(provider, "redstone_lamp", new ItemStack(Blocks.REDSTONE_LAMP), "PPP", "PGP", "PRP",
                    'P', new ItemStack(Blocks.GLASS_PANE),
                    'G', new ItemStack(Blocks.GLOWSTONE),
                    'R', new UnificationEntry(TagPrefix.rod, GTMaterials.RedAlloy)
            );

            VanillaRecipeHelper.addShapedRecipe(provider, "tripwire_hook", new ItemStack(Blocks.TRIPWIRE_HOOK), "IRI", "SRS", " S ",
                    'I', new UnificationEntry(TagPrefix.ring, GTMaterials.Iron),
                    'R', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood),
                    'S', new ItemStack(Items.STRING)
            );

            VanillaRecipeHelper.addShapedRecipe(provider, "dropper", new ItemStack(Blocks.DROPPER), "CRC", "STS", "GAG",
                    'C', ItemTags.STONE_CRAFTING_MATERIALS,
                    'R', new UnificationEntry(TagPrefix.ring, GTMaterials.Iron),
                    'S', new UnificationEntry(TagPrefix.springSmall, GTMaterials.Iron),
                    'T', new ItemStack(Items.STRING),
                    'G', new UnificationEntry(TagPrefix.gearSmall, GTMaterials.Iron),
                    'A', new UnificationEntry(TagPrefix.rod, GTMaterials.RedAlloy)
            );

            VanillaRecipeHelper.addShapedRecipe(provider, "observer", new ItemStack(Blocks.OBSERVER), "RCR", "CQC", "GSG",
                    'R', new UnificationEntry(TagPrefix.ring, GTMaterials.Iron),
                    'C', ItemTags.STONE_CRAFTING_MATERIALS,
                    'Q', new UnificationEntry(TagPrefix.plate, GTMaterials.NetherQuartz),
                    'G', new UnificationEntry(TagPrefix.gearSmall, GTMaterials.Iron),
                    'S', new UnificationEntry(TagPrefix.rod, GTMaterials.RedAlloy)
            );

            VanillaRecipeHelper.addShapedRecipe(provider, "observer_certus", new ItemStack(Blocks.OBSERVER), "RCR", "CQC", "GSG",
                    'R', new UnificationEntry(TagPrefix.ring, GTMaterials.Iron),
                    'C', ItemTags.STONE_CRAFTING_MATERIALS,
                    'Q', new UnificationEntry(TagPrefix.plate, GTMaterials.CertusQuartz),
                    'G', new UnificationEntry(TagPrefix.gearSmall, GTMaterials.Iron),
                    'S', new UnificationEntry(TagPrefix.rod, GTMaterials.RedAlloy)
            );

            VanillaRecipeHelper.addShapedRecipe(provider, "observer_quartzite", new ItemStack(Blocks.OBSERVER), "RCR", "CQC", "GSG",
                    'R', new UnificationEntry(TagPrefix.ring, GTMaterials.Iron),
                    'C', ItemTags.STONE_CRAFTING_MATERIALS,
                    'Q', new UnificationEntry(TagPrefix.plate, GTMaterials.Quartzite),
                    'G', new UnificationEntry(TagPrefix.gearSmall, GTMaterials.Iron),
                    'S', new UnificationEntry(TagPrefix.rod, GTMaterials.RedAlloy)
            );

            VanillaRecipeHelper.addShapedRecipe(provider, "repeater", new ItemStack(Items.REPEATER), "S S", "TdT", "PRP",
                    'S', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron),
                    'T', new ItemStack(Blocks.REDSTONE_TORCH),
                    'P', new ItemStack(Blocks.STONE_PRESSURE_PLATE),
                    'R', new UnificationEntry(TagPrefix.rod, GTMaterials.RedAlloy)
            );

            VanillaRecipeHelper.addShapedRecipe(provider, "comparator", new ItemStack(Items.COMPARATOR), "STS", "TQT", "PdP",
                    'S', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron),
                    'T', new ItemStack(Blocks.REDSTONE_TORCH),
                    'Q', new UnificationEntry(TagPrefix.plate, GTMaterials.NetherQuartz),
                    'P', new ItemStack(Blocks.STONE_PRESSURE_PLATE)
            );

            VanillaRecipeHelper.addShapedRecipe(provider, "comparator_certus", new ItemStack(Items.COMPARATOR), "STS", "TQT", "PdP",
                    'S', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron),
                    'T', new ItemStack(Blocks.REDSTONE_TORCH),
                    'Q', new UnificationEntry(TagPrefix.plate, GTMaterials.CertusQuartz),
                    'P', new ItemStack(Blocks.STONE_PRESSURE_PLATE)
            );

            VanillaRecipeHelper.addShapedRecipe(provider, "comparator_quartzite", new ItemStack(Items.COMPARATOR), "STS", "TQT", "PdP",
                    'S', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron),
                    'T', new ItemStack(Blocks.REDSTONE_TORCH),
                    'Q', new UnificationEntry(TagPrefix.plate, GTMaterials.Quartzite),
                    'P', new ItemStack(Blocks.STONE_PRESSURE_PLATE)
            );

            VanillaRecipeHelper.addShapedRecipe(provider, "powered_rail", new ItemStack(Blocks.POWERED_RAIL, 6), "SPS", "IWI", "GdG",
                    'S', new UnificationEntry(TagPrefix.screw, GTMaterials.Steel),
                    'P', new UnificationEntry(TagPrefix.plate, GTMaterials.RedAlloy),
                    'I', new UnificationEntry(TagPrefix.rod, GTMaterials.Iron),
                    'W', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood),
                    'G', new UnificationEntry(TagPrefix.rod, GTMaterials.Gold)
            );

            VanillaRecipeHelper.addShapedRecipe(provider, "detector_rail", new ItemStack(Blocks.DETECTOR_RAIL, 6), "SPS", "IWI", "IdI",
                    'S', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron),
                    'P', new ItemStack(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE),
                    'I', new UnificationEntry(TagPrefix.rod, GTMaterials.Iron),
                    'W', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood)
            );

            VanillaRecipeHelper.addShapedRecipe(provider, "rail", new ItemStack(Blocks.RAIL, 8), "ShS", "IWI", "IdI",
                    'S', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron),
                    'I', new UnificationEntry(TagPrefix.rod, GTMaterials.Iron),
                    'W', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood)
            );

            VanillaRecipeHelper.addShapedRecipe(provider, "activator_rail", new ItemStack(Blocks.ACTIVATOR_RAIL, 6), "SPS", "IWI", "IdI",
                    'S', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron),
                    'P', new ItemStack(Blocks.REDSTONE_TORCH),
                    'I', new UnificationEntry(TagPrefix.rod, GTMaterials.Iron),
                    'W', Tags.Items.RODS_WOODEN
            );

            VanillaRecipeHelper.addShapedRecipe(provider, "§rch", new ItemStack(Blocks.REDSTONE_TORCH), "R", "T",
                    'R', new UnificationEntry(TagPrefix.dust, GTMaterials.Redstone),
                    'T', new ItemStack(Blocks.TORCH)
            );
        } else {
            VanillaRecipeHelper.addShapedRecipe(provider, "piston_bronze", new ItemStack(Blocks.PISTON, 1), "WWW", "CBC", "CRC",
                    'W', ItemTags.PLANKS,
                    'C', ItemTags.STONE_CRAFTING_MATERIALS,
                    'R', new UnificationEntry(dust, Redstone),
                    'B', new UnificationEntry(ingot, Bronze));

            VanillaRecipeHelper.addShapedRecipe(provider, "piston_steel", new ItemStack(Blocks.PISTON, 2), "WWW", "CBC", "CRC",
                    'W', ItemTags.PLANKS,
                    'C', ItemTags.STONE_CRAFTING_MATERIALS,
                    'R', new UnificationEntry(dust, Redstone),
                    'B', new UnificationEntry(ingot, Steel));

            VanillaRecipeHelper.addShapedRecipe(provider, "piston_aluminium", new ItemStack(Blocks.PISTON, 4), "WWW", "CBC", "CRC",
                    'W', ItemTags.PLANKS,
                    'C', ItemTags.STONE_CRAFTING_MATERIALS,
                    'R', new UnificationEntry(dust, Redstone),
                    'B', new UnificationEntry(ingot, Aluminium));

            VanillaRecipeHelper.addShapedRecipe(provider, "piston_titanium", new ItemStack(Blocks.PISTON, 8), "WWW", "CBC", "CRC",
                    'W', ItemTags.PLANKS,
                    'C', ItemTags.STONE_CRAFTING_MATERIALS,
                    'R', new UnificationEntry(dust, Redstone),
                    'B', new UnificationEntry(ingot, Titanium));

            VanillaRecipeHelper.addShapedRecipe(provider, "sticky_piston_resin", new ItemStack(Blocks.STICKY_PISTON), "h", "R", "P",
                    'R', STICKY_RESIN.asStack(),
                    'P', new ItemStack(Blocks.PISTON));

            ASSEMBLER_RECIPES.recipeBuilder("piston_iron").duration(100).EUt(16).inputItems(plate, Iron).inputItems(ItemTags.PLANKS, 3).inputItems(ItemTags.STONE_CRAFTING_MATERIALS, 4).inputItems(dust, Redstone).outputItems(new ItemStack(Blocks.PISTON)).save(provider);
            ASSEMBLER_RECIPES.recipeBuilder("piston_bronze").duration(100).EUt(16).inputItems(plate, Bronze).inputItems(ItemTags.PLANKS, 3).inputItems(ItemTags.STONE_CRAFTING_MATERIALS, 4).inputItems(dust, Redstone).outputItems(new ItemStack(Blocks.PISTON)).save(provider);
            ASSEMBLER_RECIPES.recipeBuilder("piston_steel").duration(100).EUt(16).inputItems(plate, Steel).inputItems(ItemTags.PLANKS, 3).inputItems(ItemTags.STONE_CRAFTING_MATERIALS, 4).inputItems(dust, Redstone).outputItems(new ItemStack(Blocks.PISTON, 2)).save(provider);
            ASSEMBLER_RECIPES.recipeBuilder("piston_aluminium").duration(100).EUt(16).inputItems(plate, Aluminium).inputItems(ItemTags.PLANKS, 3).inputItems(ItemTags.STONE_CRAFTING_MATERIALS, 4).inputItems(dust, Redstone).outputItems(new ItemStack(Blocks.PISTON, 4)).save(provider);
            ASSEMBLER_RECIPES.recipeBuilder("piston_titanium").duration(100).EUt(16).inputItems(plate, Titanium).inputItems(ItemTags.PLANKS, 3).inputItems(ItemTags.STONE_CRAFTING_MATERIALS, 4).inputItems(dust, Redstone).outputItems(new ItemStack(Blocks.PISTON, 8)).save(provider);

            ASSEMBLER_RECIPES.recipeBuilder("light_weighted_pressure_plate")
                    .inputItems(plate, Gold, 2)
                    .outputItems(new ItemStack(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE))
                    .circuitMeta(2).duration(100).EUt(4).save(provider);

            ASSEMBLER_RECIPES.recipeBuilder("heavy_weighted_pressure_plate")
                    .inputItems(plate, Iron, 2)
                    .outputItems(new ItemStack(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE))
                    .circuitMeta(2).duration(100).EUt(4).save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, "comparator_certus", new ItemStack(Items.COMPARATOR), " T ", "TQT", "SSS",
                    'T', new ItemStack(Blocks.REDSTONE_TORCH),
                    'Q', new UnificationEntry(gem, CertusQuartz),
                    'S', new ItemStack(Blocks.STONE)
            );

            VanillaRecipeHelper.addShapedRecipe(provider, "comparator_quartzite", new ItemStack(Items.COMPARATOR), " T ", "TQT", "SSS",
                    'T', new ItemStack(Blocks.REDSTONE_TORCH),
                    'Q', new UnificationEntry(gem, Quartzite),
                    'S', new ItemStack(Blocks.STONE)
            );

            VanillaRecipeHelper.addShapedRecipe(provider, "daylight_detector_certus", new ItemStack(Blocks.DAYLIGHT_DETECTOR), "GGG", "CCC", "PPP",
                    'G', new ItemStack(Blocks.GLASS),
                    'C', new UnificationEntry(gem, CertusQuartz),
                    'P', ItemTags.WOODEN_SLABS
            );

            VanillaRecipeHelper.addShapedRecipe(provider, "daylight_detector_quartzite", new ItemStack(Blocks.DAYLIGHT_DETECTOR), "GGG", "CCC", "PPP",
                    'G', new ItemStack(Blocks.GLASS),
                    'C', new UnificationEntry(gem, Quartzite),
                    'P', ItemTags.WOODEN_SLABS
            );

            ASSEMBLER_RECIPES.recipeBuilder("note_block").duration(100).EUt(16).inputItems(ItemTags.PLANKS, 8).inputItems(dust, Redstone).circuitMeta(1).outputItems(new ItemStack(Blocks.NOTE_BLOCK)).save(provider);
            ASSEMBLER_RECIPES.recipeBuilder("jukebox").duration(100).EUt(16).inputItems(ItemTags.PLANKS, 8).inputItems(gem, Diamond).outputItems(new ItemStack(Blocks.JUKEBOX)).save(provider);
        }
    }

    private static void hardToolArmorRecipes(Consumer<FinishedRecipe> provider) {
        if (ConfigHolder.INSTANCE.recipes.hardToolArmorRecipes) {
            createShovelRecipe(provider, "iron_shovel", new ItemStack(Items.IRON_SHOVEL), GTMaterials.Iron);
            createPickaxeRecipe(provider, "iron_pickaxe", new ItemStack(Items.IRON_PICKAXE), GTMaterials.Iron);
            createAxeRecipe(provider, "iron_axe", new ItemStack(Items.IRON_AXE), GTMaterials.Iron);
            createSwordRecipe(provider, "iron_sword", new ItemStack(Items.IRON_SWORD), GTMaterials.Iron);
            createHoeRecipe(provider, "iron_hoe", new ItemStack(Items.IRON_HOE), GTMaterials.Iron);
            createHelmetRecipe(provider, "iron_helmet", new ItemStack(Items.IRON_HELMET), GTMaterials.Iron);
            createChestplateRecipe(provider, "iron_chestplate", new ItemStack(Items.IRON_CHESTPLATE), GTMaterials.Iron);
            createLeggingsRecipe(provider, "iron_leggings", new ItemStack(Items.IRON_LEGGINGS), GTMaterials.Iron);
            createBootsRecipe(provider, "iron_boots", new ItemStack(Items.IRON_BOOTS), GTMaterials.Iron);

            createShovelRecipe(provider, "golden_shovel", new ItemStack(Items.GOLDEN_SHOVEL), GTMaterials.Gold);
            createPickaxeRecipe(provider, "golden_pickaxe", new ItemStack(Items.GOLDEN_PICKAXE), GTMaterials.Gold);
            createAxeRecipe(provider, "golden_axe", new ItemStack(Items.GOLDEN_AXE), GTMaterials.Gold);
            createSwordRecipe(provider, "golden_sword", new ItemStack(Items.GOLDEN_SWORD), GTMaterials.Gold);
            createHoeRecipe(provider, "golden_hoe", new ItemStack(Items.GOLDEN_HOE), GTMaterials.Gold);
            createHelmetRecipe(provider, "golden_helmet", new ItemStack(Items.GOLDEN_HELMET), GTMaterials.Gold);
            createChestplateRecipe(provider, "golden_chestplate", new ItemStack(Items.GOLDEN_CHESTPLATE), GTMaterials.Gold);
            createLeggingsRecipe(provider, "golden_leggings", new ItemStack(Items.GOLDEN_LEGGINGS), GTMaterials.Gold);
            createBootsRecipe(provider, "golden_boots", new ItemStack(Items.GOLDEN_BOOTS), GTMaterials.Gold);

            createShovelRecipe(provider, "diamond_shovel", new ItemStack(Items.DIAMOND_SHOVEL), GTMaterials.Diamond);
            createPickaxeRecipe(provider, "diamond_pickaxe", new ItemStack(Items.DIAMOND_PICKAXE), GTMaterials.Diamond);
            createAxeRecipe(provider, "diamond_axe", new ItemStack(Items.DIAMOND_AXE), GTMaterials.Diamond);
            createSwordRecipe(provider, "diamond_sword", new ItemStack(Items.DIAMOND_SWORD), GTMaterials.Diamond);
            createHoeRecipe(provider, "diamond_hoe", new ItemStack(Items.DIAMOND_HOE), GTMaterials.Diamond);
            createHelmetRecipe(provider, "diamond_helmet", new ItemStack(Items.DIAMOND_HELMET), GTMaterials.Diamond);
            createChestplateRecipe(provider, "diamond_chestplate", new ItemStack(Items.DIAMOND_CHESTPLATE), GTMaterials.Diamond);
            createLeggingsRecipe(provider, "diamond_leggings", new ItemStack(Items.DIAMOND_LEGGINGS), GTMaterials.Diamond);
            createBootsRecipe(provider, "diamond_boots", new ItemStack(Items.DIAMOND_BOOTS), GTMaterials.Diamond);


            VanillaRecipeHelper.addShapedRecipe(provider, "compass", new ItemStack(Items.COMPASS), "SGB", "RPR", "AdS",
                    'S', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron),
                    'G', new ItemStack(Blocks.GLASS_PANE),
                    'B', new UnificationEntry(TagPrefix.bolt, GTMaterials.IronMagnetic),
                    'R', new UnificationEntry(TagPrefix.ring, GTMaterials.Zinc),
                    'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Iron),
                    'A', new UnificationEntry(TagPrefix.bolt, GTMaterials.RedAlloy)
            );

            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("compass")
                    .inputItems(TagPrefix.plate, GTMaterials.Iron)
                    .inputItems(TagPrefix.ring, GTMaterials.Zinc)
                    .inputItems(TagPrefix.bolt, GTMaterials.RedAlloy)
                    .inputItems(TagPrefix.bolt, GTMaterials.IronMagnetic)
                    .inputItems(TagPrefix.screw, GTMaterials.Iron, 2)
                    .outputItems(new ItemStack(Items.COMPASS))
                    .duration(100).EUt(16).save(provider);


            VanillaRecipeHelper.addShapedRecipe(provider, "fishing_rod", new ItemStack(Items.FISHING_ROD), "  S", " SL", "SxR",
                    'S', new UnificationEntry(TagPrefix.rodLong, GTMaterials.Wood),
                    'L', new ItemStack(Items.STRING),
                    'R', new UnificationEntry(TagPrefix.ring, GTMaterials.Iron)
            );


            VanillaRecipeHelper.addShapedRecipe(provider, "clock", new ItemStack(Items.CLOCK), "RPR", "BCB", "dSw",
                    'R', new UnificationEntry(TagPrefix.ring, GTMaterials.Gold),
                    'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Gold),
                    'B', new UnificationEntry(TagPrefix.bolt, GTMaterials.Gold),
                    'C', new ItemStack(Items.COMPARATOR),
                    'S', new UnificationEntry(TagPrefix.screw, GTMaterials.Gold)
            );

            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("clock")
                    .inputItems(TagPrefix.plate, GTMaterials.Gold)
                    .inputItems(TagPrefix.ring, GTMaterials.Gold)
                    .inputItems(TagPrefix.bolt, GTMaterials.Gold, 2)
                    .inputItems(TagPrefix.screw, GTMaterials.Gold)
                    .inputItems(new ItemStack(Items.COMPARATOR))
                    .outputItems(new ItemStack(Items.CLOCK))
                    .duration(100).EUt(16).save(provider);


            VanillaRecipeHelper.addShapedRecipe(provider, "shears", new ItemStack(Items.SHEARS), "PSP", "hRf", "TdT",
                    'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Iron),
                    'S', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron),
                    'R', new UnificationEntry(TagPrefix.ring, GTMaterials.Iron),
                    'T', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood)
            );


            VanillaRecipeHelper.addShapedRecipe(provider, "shield", new ItemStack(Items.SHIELD), "BRB", "LPL", "BRB",
                    'B', new UnificationEntry(TagPrefix.bolt, GTMaterials.Iron),
                    'R', new UnificationEntry(TagPrefix.rod, GTMaterials.Iron),
                    'L', new UnificationEntry(TagPrefix.rodLong, GTMaterials.Iron),
                    'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Wood)
            );
        } else {
            ASSEMBLER_RECIPES.recipeBuilder("compass")
                    .inputItems(dust, Redstone)
                    .inputItems(plate, Iron, 4)
                    .circuitMeta(1)
                    .outputItems(new ItemStack(Items.COMPASS))
                    .duration(100).EUt(4).save(provider);

            ASSEMBLER_RECIPES.recipeBuilder("clock")
                    .inputItems(dust, Redstone)
                    .inputItems(plate, Gold, 4)
                    .outputItems(new ItemStack(Items.CLOCK))
                    .duration(100).EUt(4).save(provider);
        }
    }

    private static void harderRods(Consumer<FinishedRecipe> provider) {
        if (ConfigHolder.INSTANCE.recipes.harderRods) {
            LATHE_RECIPES.recipeBuilder("stone_rod_from_cobblestone")
                    .inputItems(ItemTags.STONE_CRAFTING_MATERIALS)
                    .outputItems(rod, Stone, 1)
                    .outputItems(dustSmall, Stone, 2)
                    .duration(20).EUt(VA[ULV])
                    .save(provider);

            LATHE_RECIPES.recipeBuilder("stone_rod_from_stone")
                    .inputItems(new ItemStack(Blocks.STONE))
                    .outputItems(rod, Stone, 1)
                    .outputItems(dustSmall, Stone, 2)
                    .duration(20).EUt(VA[ULV])
                    .save(provider);
        } else {
            LATHE_RECIPES.recipeBuilder("stone_rod_from_cobblestone")
                    .inputItems(ItemTags.STONE_CRAFTING_MATERIALS)
                    .outputItems(rod, Stone, 2)
                    .duration(20).EUt(VA[ULV])
                    .save(provider);

            LATHE_RECIPES.recipeBuilder("stone_rod_from_stone")
                    .inputItems(new ItemStack(Blocks.STONE))
                    .outputItems(rod, Stone, 2)
                    .duration(20).EUt(VA[ULV])
                    .save(provider);
        }
    }

    /**
     * Replaces Vanilla Beacon Recipe
     * Replaces Vanilla Jack-o-lantern Recipe
     * Replaces Vanilla Book Recipe
     * Replaces Vanilla Brewing Stand Recipe
     * Replaces Vanilla Enchantment Table recipe
     * Replaces Vanilla Jukebox recipe
     * Replaces Vanilla Note Block recipe
     * Replaces Vanilla Furnace recipe
     * Replaces Vanilla Flower Pot recipe
     * Replaces Vanilla Armor Stand recipe
     * Replaces Vanilla Trapped Chest recipe
     * Replaces Vanilla Ender Chest recipe
     * Replaces Vanilla Bed recipes
     */
    private static void hardMiscRecipes(Consumer<FinishedRecipe> provider) {
        if (ConfigHolder.INSTANCE.recipes.hardMiscRecipes) {
            VanillaRecipeHelper.addShapedRecipe(provider, "beacon", new ItemStack(Blocks.BEACON), "GLG", "GSG", "OOO",
                    'G', new ItemStack(Blocks.GLASS),
                    'L', new UnificationEntry(TagPrefix.lens, GTMaterials.NetherStar),
                    'S', new ItemStack(Items.NETHER_STAR),
                    'O', new UnificationEntry(TagPrefix.plate, GTMaterials.Obsidian)
            );

            VanillaRecipeHelper.addShapedRecipe(provider, "lit_pumpkin", new ItemStack(Blocks.JACK_O_LANTERN), "PT", "k ",
                    'P', new ItemStack(Blocks.PUMPKIN),
                    'T', new ItemStack(Blocks.TORCH)
            );


            VanillaRecipeHelper.addShapedRecipe(provider, "book", new ItemStack(Items.BOOK), "SPL", "SPG", "SPL",
                    'S', new ItemStack(Items.STRING),
                    'P', new ItemStack(Items.PAPER),
                    'L', new ItemStack(Items.LEATHER),
                    'G', GTItems.STICKY_RESIN.asStack()
            );

            VanillaRecipeHelper.addShapedRecipe(provider, "brewing_stand", new ItemStack(Items.BREWING_STAND), "RBR", "ABA", "SCS",
                    'R', new UnificationEntry(TagPrefix.ring, GTMaterials.Aluminium),
                    'B', new UnificationEntry(TagPrefix.rod, GTMaterials.Blaze),
                    'A', new UnificationEntry(TagPrefix.rod, GTMaterials.Aluminium),
                    'S', new UnificationEntry(TagPrefix.screw, GTMaterials.Aluminium),
                    'C', new ItemStack(Items.CAULDRON)
            );

            VanillaRecipeHelper.addShapedRecipe(provider, "enchanting_table", new ItemStack(Blocks.ENCHANTING_TABLE), "DCD", "PBP", "DPD",
                    'D', new UnificationEntry(TagPrefix.gem, GTMaterials.Diamond),
                    'C', new ItemStack(Blocks.RED_CARPET),
                    'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Obsidian),
                    'B', new ItemStack(Blocks.BOOKSHELF)
            );

            VanillaRecipeHelper.addShapedRecipe(provider, "jukebox", new ItemStack(Blocks.JUKEBOX), "LBL", "NRN", "LGL",
                    'L', ItemTags.LOGS,
                    'B', new UnificationEntry(TagPrefix.bolt, GTMaterials.Diamond),
                    'N', new ItemStack(Blocks.NOTE_BLOCK),
                    'R', new UnificationEntry(TagPrefix.ring, GTMaterials.Iron),
                    'G', new UnificationEntry(TagPrefix.gear, GTMaterials.Iron)
            );

            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("jukebox")
                    .inputItems(TagPrefix.bolt, GTMaterials.Diamond)
                    .inputItems(TagPrefix.gear, GTMaterials.Iron)
                    .inputItems(TagPrefix.ring, GTMaterials.Iron)
                    .inputItems(TagPrefix.plate, GTMaterials.Wood, 4)
                    .inputItems(new ItemStack(Blocks.NOTE_BLOCK, 2))
                    .outputItems(new ItemStack(Blocks.JUKEBOX))
                    .duration(100).EUt(16).save(provider);


            VanillaRecipeHelper.addShapedRecipe(provider, "note_block", new ItemStack(Blocks.NOTE_BLOCK), "PPP", "BGB", "PRP",
                    'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Wood),
                    'B', new ItemStack(Blocks.IRON_BARS),
                    'G', new UnificationEntry(TagPrefix.gear, GTMaterials.Wood),
                    'R', new UnificationEntry(TagPrefix.rod, GTMaterials.RedAlloy)
            );

            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("note_block")
                    .inputItems(TagPrefix.plate, GTMaterials.Wood, 4)
                    .inputItems(TagPrefix.gear, GTMaterials.Wood)
                    .inputItems(TagPrefix.rod, GTMaterials.RedAlloy)
                    .inputItems(new ItemStack(Blocks.IRON_BARS, 2))
                    .outputItems(new ItemStack(Blocks.NOTE_BLOCK))
                    .duration(100).EUt(16).save(provider);


            VanillaRecipeHelper.addShapedRecipe(provider, "furnace", new ItemStack(Blocks.FURNACE), "CCC", "FFF", "CCC",
                    'F', new ItemStack(Items.FLINT),
                    'C', ItemTags.STONE_CRAFTING_MATERIALS
            );

            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("furnace")
                    .circuitMeta(8)
                    .inputItems(ItemTags.STONE_CRAFTING_MATERIALS, 8)
                    .inputItems(new ItemStack(Items.FLINT))
                    .outputItems(new ItemStack(Blocks.FURNACE))
                    .duration(100).EUt(VA[ULV]).save(provider);


            VanillaRecipeHelper.addShapedRecipe(provider, "crafting_table", new ItemStack(Blocks.CRAFTING_TABLE), "FF", "WW",
                    'F', new ItemStack(Items.FLINT),
                    'W', ItemTags.LOGS
            );

            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("crafting_table").duration(80).EUt(6)
                    .inputItems(ItemTags.LOGS)
                    .inputItems(new ItemStack(Items.FLINT))
                    .outputItems(new ItemStack(Blocks.CRAFTING_TABLE))
                    .save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, "lead", new ItemStack(Items.LEAD), "SSS", "SBS", "SSS",
                    'S', new ItemStack(Items.STRING),
                    'B', new ItemStack(Items.SLIME_BALL)
            );

            VanillaRecipeHelper.addShapedRecipe(provider, "bow", new ItemStack(Items.BOW), "hLS", "LRS", "fLS",
                    'L', new UnificationEntry(TagPrefix.rodLong, GTMaterials.Wood),
                    'S', new ItemStack(Items.STRING),
                    'R', new UnificationEntry(TagPrefix.ring, GTMaterials.Iron));


            VanillaRecipeHelper.addShapedRecipe(provider, "item_frame", new ItemStack(Items.ITEM_FRAME), "SRS", "TLT", "TTT",
                    'S', new ItemStack(Items.STRING),
                    'R', new UnificationEntry(TagPrefix.ring, GTMaterials.Iron),
                    'T', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood),
                    'L', new ItemStack(Items.LEATHER));


            VanillaRecipeHelper.addShapedRecipe(provider, "painting", new ItemStack(Items.PAINTING), "SRS", "TCT", "TTT",
                    'S', new ItemStack(Items.STRING),
                    'R', new UnificationEntry(TagPrefix.ring, GTMaterials.Iron),
                    'T', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood),
                    'C', ItemTags.WOOL_CARPETS);


            VanillaRecipeHelper.addShapedRecipe(provider, "chest_minecart", new ItemStack(Items.CHEST_MINECART), "hIw", " M ", " d ", 'I', CustomTags.TAG_WOODEN_CHESTS, 'M', new ItemStack(Items.MINECART));
            VanillaRecipeHelper.addShapedRecipe(provider, "furnace_minecart", new ItemStack(Items.FURNACE_MINECART), "hIw", " M ", " d ", 'I', new ItemStack(Blocks.FURNACE), 'M', new ItemStack(Items.MINECART));
            VanillaRecipeHelper.addShapedRecipe(provider, "tnt_minecart", new ItemStack(Items.TNT_MINECART), "hIw", " M ", " d ", 'I', new ItemStack(Blocks.TNT), 'M', new ItemStack(Items.MINECART));
            VanillaRecipeHelper.addShapedRecipe(provider, "hopper_minecart", new ItemStack(Items.HOPPER_MINECART), "hIw", " M ", " d ", 'I', new ItemStack(Blocks.HOPPER), 'M', new ItemStack(Items.MINECART));


            VanillaRecipeHelper.addShapedRecipe(provider, "flower_pot", new ItemStack(Items.FLOWER_POT), "BfB", " B ",
                'B', new ItemStack(Items.BRICK));

            VanillaRecipeHelper.addShapedRecipe(provider, "armor_stand", new ItemStack(Items.ARMOR_STAND), "BSB", "hSs", "IPI",
                'B', new UnificationEntry(TagPrefix.bolt, GTMaterials.Wood),
                'S', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood),
                'I', new UnificationEntry(TagPrefix.plate, GTMaterials.Iron),
                'P', new ItemStack(Blocks.STONE_PRESSURE_PLATE));
            ASSEMBLER_RECIPES.recipeBuilder("armor_stand")
                .inputItems(Blocks.STONE_PRESSURE_PLATE.asItem())
                .inputItems(TagPrefix.plate, GTMaterials.Iron, 2)
                .inputItems(TagPrefix.rod, GTMaterials.Wood, 2)
                .outputItems(Items.ARMOR_STAND)
                .duration(100).EUt(VA[ULV]).save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, "trapped_chest", new ItemStack(Blocks.TRAPPED_CHEST), " H ", "SCS", " d ",
                'H', new ItemStack(Blocks.TRIPWIRE_HOOK),
                'S', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron),
                'C', new ItemStack(Blocks.CHEST));

            ASSEMBLER_RECIPES.recipeBuilder("ender_chest")
                .inputItems(CustomTags.TAG_WOODEN_CHESTS)
                .inputItems(TagPrefix.plateDense, GTMaterials.Obsidian, 6)
                .inputItems(TagPrefix.plate, GTMaterials.EnderEye)
                .outputItems(Blocks.ENDER_CHEST.asItem())
                .duration(200).EUt(VA[MV]).save(provider);

            for (DyeColor color : DyeColor.values()) {
                addBedRecipe(provider, color);
            }

        } else {
            ASSEMBLER_RECIPES.recipeBuilder("crafting_table").duration(80).EUt(6).circuitMeta(4).inputItems(ItemTags.PLANKS, 4).outputItems(new ItemStack(Blocks.CRAFTING_TABLE)).save(provider);
            ASSEMBLER_RECIPES.recipeBuilder("furnace").circuitMeta(8).inputItems(ItemTags.STONE_CRAFTING_MATERIALS, 8).outputItems(new ItemStack(Blocks.FURNACE)).duration(100).EUt(VA[ULV]).save(provider);
            ASSEMBLER_RECIPES.recipeBuilder("enchanting_table").inputItems(new ItemStack(Blocks.OBSIDIAN, 4)).inputItems(gem, Diamond, 2).inputItems(new ItemStack(Items.BOOK)).outputItems(new ItemStack(Blocks.ENCHANTING_TABLE)).duration(100).EUt(VA[ULV]).save(provider);
            ASSEMBLER_RECIPES.recipeBuilder("dispenser").duration(100).EUt(VA[LV]).circuitMeta(1).inputItems(ItemTags.STONE_CRAFTING_MATERIALS, 7).inputItems(new ItemStack(Items.BOW)).inputItems(dust, Redstone).outputItems(new ItemStack(Blocks.DISPENSER)).save(provider);
            ASSEMBLER_RECIPES.recipeBuilder("dropper").duration(100).EUt(VA[LV]).circuitMeta(2).inputItems(ItemTags.STONE_CRAFTING_MATERIALS, 7).inputItems(dust, Redstone).outputItems(new ItemStack(Blocks.DROPPER)).save(provider);
            ASSEMBLER_RECIPES.recipeBuilder("observer_nether_quartz").duration(100).EUt(VA[LV]).inputItems(ItemTags.STONE_CRAFTING_MATERIALS, 6).inputItems(dust, Redstone, 2).inputItems(plate, NetherQuartz).outputItems(new ItemStack(Blocks.OBSERVER)).save(provider);
            ASSEMBLER_RECIPES.recipeBuilder("observer_certus_quartz").duration(100).EUt(VA[LV]).inputItems(ItemTags.STONE_CRAFTING_MATERIALS, 6).inputItems(dust, Redstone, 2).inputItems(plate, CertusQuartz).outputItems(new ItemStack(Blocks.OBSERVER)).save(provider);
            ASSEMBLER_RECIPES.recipeBuilder("observer_quartzite").duration(100).EUt(VA[LV]).inputItems(ItemTags.STONE_CRAFTING_MATERIALS, 6).inputItems(dust, Redstone, 2).inputItems(plate, Quartzite).outputItems(new ItemStack(Blocks.OBSERVER)).save(provider);
        }
    }

    private static void addBedRecipe(Consumer<FinishedRecipe> provider, DyeColor color) {
        String colorName = color.getName();
        VanillaRecipeHelper.addShapedRecipe(provider, colorName + "_bed", new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(colorName + "_bed"))), "WWW", "PPP", "FrF",
            'W', BuiltInRegistries.ITEM.get(new ResourceLocation(colorName + "_carpet")),
            'P', new UnificationEntry(TagPrefix.plank, GTMaterials.Wood),
            'F', ItemTags.WOODEN_FENCES);
    }

    private static void hardGlassRecipes(Consumer<FinishedRecipe> provider) {
    }

    private static void nerfPaperCrafting(Consumer<FinishedRecipe> provider) {
        VanillaRecipeHelper.addShapedRecipe(provider, "paper_dust", ChemicalHelper.get(TagPrefix.dust, GTMaterials.Paper, 2), "SSS", " m ", 'S', new ItemStack(Items.SUGAR_CANE));
        VanillaRecipeHelper.addShapedRecipe(provider, "sugar", ChemicalHelper.get(TagPrefix.dust, GTMaterials.Sugar, 1), "Sm ", 'S', new ItemStack(Items.SUGAR_CANE));
        VanillaRecipeHelper.addShapedRecipe(provider, "paper", new ItemStack(Items.PAPER, 2),
                " r ", "SSS", " B ",
                'S', new UnificationEntry(TagPrefix.dust, GTMaterials.Paper),
                'B', new ItemStack(Items.WATER_BUCKET));
    }

    private static void hardAdvancedIronRecipes(Consumer<FinishedRecipe> provider) {
        VanillaRecipeHelper.addShapedRecipe(provider, "iron_door", new ItemStack(Items.IRON_DOOR), "PTh", "PRS", "PPd",
                'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Iron),
                'T', new ItemStack(Blocks.IRON_BARS),
                'R', new UnificationEntry(TagPrefix.ring, GTMaterials.Steel),
                'S', new UnificationEntry(TagPrefix.screw, GTMaterials.Steel)
        );

        ASSEMBLER_RECIPES.recipeBuilder("iron_door")
                .inputItems(TagPrefix.plate, GTMaterials.Iron, 4)
                .inputItems(new ItemStack(Blocks.IRON_BARS))
                .inputFluids(GTMaterials.Steel.getFluid(L / 9))
                .outputItems(new ItemStack(Items.IRON_DOOR))
                .duration(400).EUt(VA[ULV]).save(provider);

        VanillaRecipeHelper.addShapedRecipe(provider, "anvil", new ItemStack(Blocks.ANVIL), "BBB", "SBS", "PBP",
                'B', new UnificationEntry(TagPrefix.block, GTMaterials.Iron),
                'S', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron),
                'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Iron)
        );

        VanillaRecipeHelper.addShapedRecipe(provider, "iron_trapdoor", new ItemStack(Blocks.IRON_TRAPDOOR), "SPS", "PTP", "sPd",
                'S', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron),
                'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Iron),
                'T', ItemTags.WOODEN_TRAPDOORS
        );

        VanillaRecipeHelper.addShapedRecipe(provider, "minecart_iron", new ItemStack(Items.MINECART), " h ", "PwP", "WPW",
            'W', GTItems.IRON_MINECART_WHEELS.asStack(),
            'P', new UnificationEntry(plate, Iron)
        );
        VanillaRecipeHelper.addShapedRecipe(provider, "minecart_steel", new ItemStack(Items.MINECART), " h ", "PwP", "WPW",
            'W', GTItems.STEEL_MINECART_WHEELS.asStack(),
            'P', new UnificationEntry(plate, Steel)
        );
    }

    private static void hardDyeRecipes(Consumer<FinishedRecipe> provider) {
    }

    private static void harderCharcoalRecipes(Consumer<FinishedRecipe> provider) {
    }

    private static void flintAndSteelRequireSteel(Consumer<FinishedRecipe> provider) {
        VanillaRecipeHelper.addShapedRecipe(provider, "flint_and_steel", new ItemStack(Items.FLINT_AND_STEEL), "G", "F", "S",
                'G', new UnificationEntry(TagPrefix.gearSmall, GTMaterials.Steel),
                'F', new ItemStack(Items.FLINT),
                'S', new UnificationEntry(TagPrefix.springSmall, GTMaterials.Steel)
        );
    }

    private static void removeVanillaBlockRecipes(Consumer<FinishedRecipe> provider) {
        VanillaRecipeHelper.addShapedRecipe(provider, "sandstone_slab_saw", new ItemStack(Blocks.SANDSTONE_SLAB), "sS", 'S', new ItemStack(Blocks.SANDSTONE));
        VanillaRecipeHelper.addShapedRecipe(provider, "smooth_sandstone_slab_saw", new ItemStack(Blocks.SMOOTH_SANDSTONE_SLAB), "sS", 'S', new ItemStack(Blocks.SMOOTH_SANDSTONE));
        VanillaRecipeHelper.addShapedRecipe(provider, "cobblestone_slab_saw", new ItemStack(Blocks.COBBLESTONE_SLAB), "sS", 'S', new ItemStack(Blocks.COBBLESTONE));
        VanillaRecipeHelper.addShapedRecipe(provider, "brick_slab_saw", new ItemStack(Blocks.BRICK_SLAB), "sS", 'S', new ItemStack(Blocks.BRICKS));
        VanillaRecipeHelper.addShapedRecipe(provider, "stone_brick_slab_saw", new ItemStack(Blocks.STONE_BRICK_SLAB), "sS", 'S', ItemTags.STONE_BRICKS);
        VanillaRecipeHelper.addShapedRecipe(provider, "nether_brick_slab_saw", new ItemStack(Blocks.NETHER_BRICK_SLAB), "sS", 'S', new ItemStack(Blocks.NETHER_BRICKS));
        VanillaRecipeHelper.addShapedRecipe(provider, "quartz_slab_saw", new ItemStack(Blocks.QUARTZ_SLAB), "sS", 'S', new ItemStack(Blocks.QUARTZ_BLOCK));
        VanillaRecipeHelper.addShapedRecipe(provider, "smooth_quartz_slab_saw", new ItemStack(Blocks.SMOOTH_QUARTZ_SLAB), "sS", 'S', new ItemStack(Blocks.SMOOTH_QUARTZ));
        VanillaRecipeHelper.addShapedRecipe(provider, "red_sandstone_slab_saw", new ItemStack(Blocks.RED_SANDSTONE_SLAB), "sS", 'S', new ItemStack(Blocks.RED_SANDSTONE));
        VanillaRecipeHelper.addShapedRecipe(provider, "purpur_slab_saw", new ItemStack(Blocks.PURPUR_SLAB), "sS", 'S', new ItemStack(Blocks.PURPUR_BLOCK));
    }

    private static void createShovelRecipe(Consumer<FinishedRecipe> provider, String regName, ItemStack output, Material material) {
        VanillaRecipeHelper.addShapedRecipe(provider, regName, output, "hPf", " S ", " S ",
                'P', new UnificationEntry(TagPrefix.plate, material),
                'S', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood)
        );
    }

    private static void createPickaxeRecipe(Consumer<FinishedRecipe> provider, String regName, ItemStack output, Material material) {
        VanillaRecipeHelper.addShapedRecipe(provider, regName, output, "PII", "hSf", " S ",
                'P', new UnificationEntry(TagPrefix.plate, material),
                'I', new UnificationEntry(material.equals(GTMaterials.Diamond) ? TagPrefix.gem : TagPrefix.ingot, material),
                'S', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood)
        );
    }

    private static void createAxeRecipe(Consumer<FinishedRecipe> provider, String regName, ItemStack output, Material material) {
        VanillaRecipeHelper.addShapedRecipe(provider, regName, output, "PIf", "PS ", "hS ",
                'P', new UnificationEntry(TagPrefix.plate, material),
                'I', new UnificationEntry(material.equals(GTMaterials.Diamond) ? TagPrefix.gem : TagPrefix.ingot, material),
                'S', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood)
        );
    }

    private static void createSwordRecipe(Consumer<FinishedRecipe> provider, String regName, ItemStack output, Material material) {
        VanillaRecipeHelper.addShapedRecipe(provider, regName, output, " P ", "hPf", " S ",
                'P', new UnificationEntry(TagPrefix.plate, material),
                'S', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood)
        );
    }

    private static void createHoeRecipe(Consumer<FinishedRecipe> provider, String regName, ItemStack output, Material material) {
        VanillaRecipeHelper.addShapedRecipe(provider, regName, output, "PIf", "hS ", " S ",
                'P', new UnificationEntry(TagPrefix.plate, material),
                'I', new UnificationEntry(material.equals(GTMaterials.Diamond) ? TagPrefix.gem : TagPrefix.ingot, material),
                'S', new UnificationEntry(TagPrefix.rod, GTMaterials.Wood)
        );
    }

    private static void createHelmetRecipe(Consumer<FinishedRecipe> provider, String regName, ItemStack output, Material material) {
        VanillaRecipeHelper.addShapedRecipe(provider, regName, output, "PPP", "PhP",
                'P', new UnificationEntry(TagPrefix.plate, material)
        );
    }

    private static void createChestplateRecipe(Consumer<FinishedRecipe> provider, String regName, ItemStack output, Material material) {
        VanillaRecipeHelper.addShapedRecipe(provider, regName, output, "PhP", "PPP", "PPP",
                'P', new UnificationEntry(TagPrefix.plate, material)
        );
    }

    private static void createLeggingsRecipe(Consumer<FinishedRecipe> provider, String regName, ItemStack output, Material material) {
        VanillaRecipeHelper.addShapedRecipe(provider, regName, output, "PPP", "PhP", "P P",
                'P', new UnificationEntry(TagPrefix.plate, material)
        );
    }

    private static void createBootsRecipe(Consumer<FinishedRecipe> provider, String regName, ItemStack output, Material material) {
        VanillaRecipeHelper.addShapedRecipe(provider, regName, output, "P P", "PhP",
                'P', new UnificationEntry(TagPrefix.plate, material)
        );
    }
}
