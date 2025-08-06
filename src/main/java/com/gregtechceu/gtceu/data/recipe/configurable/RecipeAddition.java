package com.gregtechceu.gtceu.data.recipe.configurable;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidContainerIngredient;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.config.ConfigHolder;
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

public class RecipeAddition {

    public static void init(Consumer<FinishedRecipe> provider) {
        hardMiscRecipes(provider);
        hardRedstoneRecipes(provider);
        disableManualCompression(provider);
        hardToolArmorRecipes(provider);
        harderRods(provider);
        harderBrickRecipes(provider);
        steelSteamMultiblocks(provider);
        if (ConfigHolder.INSTANCE.recipes.hardWoodRecipes) hardWoodRecipes(provider);
        if (ConfigHolder.INSTANCE.recipes.hardIronRecipes) hardIronRecipes(provider);
        if (ConfigHolder.INSTANCE.recipes.hardGlassRecipes) hardGlassRecipes(provider);
        if (ConfigHolder.INSTANCE.recipes.nerfPaperCrafting) nerfPaperCrafting(provider);
        if (ConfigHolder.INSTANCE.recipes.hardAdvancedIronRecipes) hardAdvancedIronRecipes(provider);
        if (ConfigHolder.INSTANCE.recipes.flintAndSteelRequireSteel) flintAndSteelRequireSteel(provider);
        if (ConfigHolder.INSTANCE.recipes.removeVanillaBlockRecipes) vanillaBlockRecipes(provider);
    }

    private static void steelSteamMultiblocks(Consumer<FinishedRecipe> provider) {
        if (ConfigHolder.INSTANCE.machines.steelSteamMultiblocks) {
            VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_oven",
                    GTMultiMachines.STEAM_OVEN.asStack(),
                    "CGC", "FMF", "CGC",
                    'F', GTBlocks.FIREBOX_STEEL.asStack(),
                    'C', GTBlocks.CASING_STEEL_SOLID.asStack(),
                    'M', GTMachines.STEAM_FURNACE.right().asStack(),
                    'G', new MaterialEntry(TagPrefix.gear, GTMaterials.Invar));

            VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_grinder",
                    GTMultiMachines.STEAM_GRINDER.asStack(),
                    "CGC", "CFC", "CGC",
                    'G', new MaterialEntry(TagPrefix.gear, GTMaterials.Potin),
                    'F', GTMachines.STEAM_MACERATOR.right().asStack(),
                    'C', GTBlocks.CASING_STEEL_SOLID.asStack());

            VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_hatch",
                    GTMachines.STEAM_HATCH.asStack(),
                    "BPB", "BTB", "BPB",
                    'B', new MaterialEntry(TagPrefix.plate, GTMaterials.Steel),
                    'P', new MaterialEntry(TagPrefix.pipeNormalFluid, GTMaterials.Steel),
                    'T', GTMachines.STEEL_DRUM.asStack());

            VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_input_bus",
                    GTMachines.STEAM_IMPORT_BUS.asStack(),
                    "C", "H",
                    'H', GTBlocks.STEEL_HULL.asStack(),
                    'C', Tags.Items.CHESTS_WOODEN);

            VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_output_bus",
                    GTMachines.STEAM_EXPORT_BUS.asStack(),
                    "H", "C",
                    'H', GTBlocks.STEEL_HULL.asStack(),
                    'C', Tags.Items.CHESTS_WOODEN);
        } else {
            // Doubled-up recipes to allow for either Low or High Pressure singleblock
            VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_oven_from_lp",
                    GTMultiMachines.STEAM_OVEN.asStack(),
                    "CGC", "FMF", "CGC",
                    'F', GTBlocks.FIREBOX_BRONZE.asStack(),
                    'C', GTBlocks.CASING_BRONZE_BRICKS.asStack(),
                    'M', GTMachines.STEAM_FURNACE.left().asStack(),
                    'G', new MaterialEntry(TagPrefix.gear, GTMaterials.Invar));

            VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_oven_from_hp",
                    GTMultiMachines.STEAM_OVEN.asStack(),
                    "CGC", "FMF", "CGC",
                    'F', GTBlocks.FIREBOX_BRONZE.asStack(),
                    'C', GTBlocks.CASING_BRONZE_BRICKS.asStack(),
                    'M', GTMachines.STEAM_FURNACE.right().asStack(),
                    'G', new MaterialEntry(TagPrefix.gear, GTMaterials.Invar));

            VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_grinder_from_lp",
                    GTMultiMachines.STEAM_GRINDER.asStack(),
                    "CGC", "CFC", "CGC",
                    'G', new MaterialEntry(TagPrefix.gear, GTMaterials.Potin),
                    'F', GTMachines.STEAM_MACERATOR.left().asStack(),
                    'C', GTBlocks.CASING_BRONZE_BRICKS.asStack());

            VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_grinder_from_hp",
                    GTMultiMachines.STEAM_GRINDER.asStack(),
                    "CGC", "CFC", "CGC",
                    'G', new MaterialEntry(TagPrefix.gear, GTMaterials.Potin),
                    'F', GTMachines.STEAM_MACERATOR.right().asStack(),
                    'C', GTBlocks.CASING_BRONZE_BRICKS.asStack());

            VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_hatch",
                    GTMachines.STEAM_HATCH.asStack(),
                    "BPB", "BTB", "BPB",
                    'B', new MaterialEntry(TagPrefix.plate, GTMaterials.Bronze),
                    'P', new MaterialEntry(TagPrefix.pipeNormalFluid, GTMaterials.Bronze),
                    'T', GTMachines.BRONZE_DRUM.asStack());

            VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_input_bus",
                    GTMachines.STEAM_IMPORT_BUS.asStack(),
                    "C", "H",
                    'H', GTBlocks.BRONZE_HULL.asStack(),
                    'C', Tags.Items.CHESTS_WOODEN);

            VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_output_bus",
                    GTMachines.STEAM_EXPORT_BUS.asStack(),
                    "H", "C",
                    'H', GTBlocks.BRONZE_HULL.asStack(),
                    'C', Tags.Items.CHESTS_WOODEN);
        }
    }

    private static void disableManualCompression(Consumer<FinishedRecipe> provider) {
        if (!ConfigHolder.INSTANCE.recipes.disableManualCompression) {
            VanillaRecipeHelper.addShapelessRecipe(provider, "nether_quartz_block_to_nether_quartz",
                    new ItemStack(Items.QUARTZ, 4), Blocks.QUARTZ_BLOCK);
        }
    }

    private static void harderBrickRecipes(Consumer<FinishedRecipe> provider) {
        if (ConfigHolder.INSTANCE.recipes.harderBrickRecipes) {
            VanillaRecipeHelper.addShapedFluidContainerRecipe(provider, "brick_from_water",
                    new ItemStack(Blocks.BRICKS, 2), "BBB",
                    "BWB", "BBB",
                    'B', new ItemStack(Items.BRICK),
                    'W', new FluidContainerIngredient(Water.getFluidTag(), 1000));

            VanillaRecipeHelper.addShapedRecipe(provider, "bucket_of_concrete", new ItemStack(Concrete.getBucket()),
                    "CBS", "CWQ", " L ",
                    'C', new MaterialEntry(dust, Calcite),
                    'S', new MaterialEntry(dust, Stone),
                    'W', new ItemStack(Items.WATER_BUCKET),
                    'Q', new MaterialEntry(dust, QuartzSand),
                    'L', new MaterialEntry(dust, Clay),
                    'B', new ItemStack(Items.BUCKET));

            VanillaRecipeHelper.addShapedFluidContainerRecipe(provider, true, "casing_primitive_bricks",
                    GTBlocks.CASING_PRIMITIVE_BRICKS.asStack(),
                    "BGB", "BCB", "BGB",
                    'B', GTItems.FIRECLAY_BRICK.asStack(),
                    'G', new MaterialEntry(dust, Gypsum),
                    'C', new FluidContainerIngredient(Concrete.getFluidTag(), 1000));

            VanillaRecipeHelper.addShapelessRecipe(provider, "compressed_clay", COMPRESSED_CLAY.asStack(),
                    WOODEN_FORM_BRICK.asStack(), new ItemStack(Items.CLAY_BALL));
            VanillaRecipeHelper.addSmeltingRecipe(provider, "brick_from_compressed_clay", COMPRESSED_CLAY.asStack(),
                    new ItemStack(Items.BRICK), 0.3f);
        } else {
            VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_primitive_bricks",
                    GTBlocks.CASING_PRIMITIVE_BRICKS.asStack(),
                    "XX", "XX",
                    'X', GTItems.FIRECLAY_BRICK);
        }
    }

    private static void hardWoodRecipes(Consumer<FinishedRecipe> provider) {
        VanillaRecipeHelper.addShapedRecipe(provider, "ladder", new ItemStack(Blocks.LADDER, 2), "SrS", "SRS", "ShS",
                'S', new MaterialEntry(TagPrefix.rod, GTMaterials.Wood), 'R',
                new MaterialEntry(TagPrefix.bolt, GTMaterials.Wood));

        VanillaRecipeHelper.addShapedRecipe(provider, "bowl", new ItemStack(Items.BOWL), "k", "X", 'X',
                ItemTags.PLANKS);

        VanillaRecipeHelper.addShapedRecipe(provider, "chest", new ItemStack(Blocks.CHEST), "LPL", "PFP", "LPL",
                'L', ItemTags.LOGS,
                'P', ItemTags.PLANKS,
                'F', new ItemStack(Items.FLINT));

        VanillaRecipeHelper.addShapedRecipe(provider, "barrel", new ItemStack(Blocks.BARREL), "PSP",
                "PsP", "PSP",
                'P', ItemTags.PLANKS,
                'S', ItemTags.WOODEN_SLABS);
    }

    private static void hardIronRecipes(Consumer<FinishedRecipe> provider) {
        VanillaRecipeHelper.addShapedRecipe(provider, "cauldron", new ItemStack(Items.CAULDRON), "X X", "XhX", "XXX",
                'X', new MaterialEntry(TagPrefix.plate, GTMaterials.Iron));

        VanillaRecipeHelper.addShapedRecipe(provider, "hopper", new ItemStack(Blocks.HOPPER), "XCX", "XGX", "wXh",
                'X', new MaterialEntry(TagPrefix.plate, GTMaterials.Iron),
                'C', Tags.Items.CHESTS_WOODEN,
                'G', new MaterialEntry(TagPrefix.gearSmall, GTMaterials.Iron));

        VanillaRecipeHelper.addShapedRecipe(provider, "iron_bars", new ItemStack(Blocks.IRON_BARS, 8), " h ", "XXX",
                "XXX",
                'X', new MaterialEntry(TagPrefix.rod, GTMaterials.Iron));

        VanillaRecipeHelper.addShapedRecipe(provider, "iron_bucket", new ItemStack(Items.BUCKET), "XhX", " X ", 'X',
                new MaterialEntry(TagPrefix.plate, GTMaterials.Iron));

        VanillaRecipeHelper.addShapedRecipe(provider, "chain_iron", new ItemStack(Items.CHAIN), " R ",
                "wR ", " R ",
                'R', new MaterialEntry(ring, Iron));

        ASSEMBLER_RECIPES.recipeBuilder("chain_iron")
                .inputItems(ring, Iron, 3)
                .outputItems(new ItemStack(Items.CHAIN, 2))
                .circuitMeta(1)
                .duration(40).EUt(10).save(provider);

        VanillaRecipeHelper.addShapedRecipe(provider, "chain_wrought_iron", new ItemStack(Items.CHAIN, 2), " R ",
                "wR ", " R ",
                'R', new MaterialEntry(ring, WroughtIron));

        ASSEMBLER_RECIPES.recipeBuilder("chain_wrought_iron")
                .inputItems(ring, WroughtIron, 3)
                .outputItems(new ItemStack(Items.CHAIN, 3))
                .circuitMeta(1)
                .duration(40).EUt(10).save(provider);

        VanillaRecipeHelper.addShapedRecipe(provider, "chain_steel", new ItemStack(Items.CHAIN, 3), " R ",
                "wR ", " R ",
                'R', new MaterialEntry(ring, Steel));

        ASSEMBLER_RECIPES.recipeBuilder("chain_steel")
                .inputItems(ring, Steel, 3)
                .outputItems(new ItemStack(Items.CHAIN, 6))
                .circuitMeta(1)
                .duration(40).EUt(10).save(provider);
    }

    private static void hardRedstoneRecipes(Consumer<FinishedRecipe> provider) {
        if (ConfigHolder.INSTANCE.recipes.hardRedstoneRecipes) {
            VanillaRecipeHelper.addShapedRecipe(provider, "dispenser", new ItemStack(Blocks.DISPENSER), "CRC", "STS",
                    "GAG",
                    'C', ItemTags.STONE_CRAFTING_MATERIALS,
                    'R', new MaterialEntry(TagPrefix.ring, GTMaterials.Iron),
                    'S', new MaterialEntry(TagPrefix.spring, GTMaterials.Iron),
                    'T', new ItemStack(Items.STRING),
                    'G', new MaterialEntry(TagPrefix.gearSmall, GTMaterials.Iron),
                    'A', new MaterialEntry(TagPrefix.rod, GTMaterials.RedAlloy));

            ASSEMBLER_RECIPES.recipeBuilder("dispenser").duration(100).EUt(VA[LV])
                    .inputItems(ItemTags.STONE_CRAFTING_MATERIALS, 2)
                    .inputItems(TagPrefix.ring, GTMaterials.Iron)
                    .inputItems(TagPrefix.spring, GTMaterials.Iron, 2)
                    .inputItems(TagPrefix.gearSmall, GTMaterials.Iron, 2)
                    .inputItems(TagPrefix.rod, GTMaterials.RedAlloy)
                    .inputItems(new ItemStack(Items.STRING))
                    .outputItems(new ItemStack(Blocks.DISPENSER))
                    .addMaterialInfo(true)
                    .save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, "sticky_piston", new ItemStack(Blocks.STICKY_PISTON), "h",
                    "R", "P",
                    'R', new ItemStack(Items.SLIME_BALL),
                    'P', new ItemStack(Blocks.PISTON));

            VanillaRecipeHelper.addShapedRecipe(provider, "piston_iron", new ItemStack(Blocks.PISTON), "WWW", "GFG",
                    "CRC",
                    'W', ItemTags.PLANKS,
                    'C', ItemTags.STONE_CRAFTING_MATERIALS,
                    'R', new MaterialEntry(TagPrefix.plate, GTMaterials.RedAlloy),
                    'G', new MaterialEntry(TagPrefix.gearSmall, GTMaterials.Iron),
                    'F', ItemTags.WOODEN_FENCES);

            ASSEMBLER_RECIPES.recipeBuilder("piston_iron")
                    .inputItems(TagPrefix.rod, GTMaterials.Iron)
                    .inputItems(TagPrefix.gearSmall, GTMaterials.Iron)
                    .inputItems(ItemTags.WOODEN_SLABS)
                    .inputItems(ItemTags.STONE_CRAFTING_MATERIALS)
                    .inputFluids(GTMaterials.RedAlloy.getFluid(GTValues.L))
                    .outputItems(new ItemStack(Blocks.PISTON))
                    .duration(240).EUt(VA[ULV]).save(provider);

            ASSEMBLER_RECIPES.recipeBuilder("piston_steel")
                    .inputItems(TagPrefix.rod, GTMaterials.Steel)
                    .inputItems(TagPrefix.gearSmall, GTMaterials.Steel)
                    .inputItems(ItemTags.WOODEN_SLABS, 2)
                    .inputItems(ItemTags.STONE_CRAFTING_MATERIALS, 2)
                    .inputFluids(GTMaterials.RedAlloy.getFluid(GTValues.L * 2))
                    .outputItems(new ItemStack(Blocks.PISTON, 2))
                    .duration(240).EUt(16).save(provider);

            ASSEMBLER_RECIPES.recipeBuilder("piston_aluminium")
                    .inputItems(TagPrefix.rod, GTMaterials.Aluminium)
                    .inputItems(TagPrefix.gearSmall, GTMaterials.Aluminium)
                    .inputItems(ItemTags.WOODEN_SLABS, 4)
                    .inputItems(ItemTags.STONE_CRAFTING_MATERIALS, 4)
                    .inputFluids(GTMaterials.RedAlloy.getFluid(GTValues.L * 3))
                    .outputItems(new ItemStack(Blocks.PISTON, 4))
                    .duration(240).EUt(VA[LV]).save(provider);

            ASSEMBLER_RECIPES.recipeBuilder("piston_stainless_steel")
                    .inputItems(TagPrefix.rod, GTMaterials.StainlessSteel)
                    .inputItems(TagPrefix.gearSmall, GTMaterials.StainlessSteel)
                    .inputItems(ItemTags.WOODEN_SLABS, 8)
                    .inputItems(ItemTags.STONE_CRAFTING_MATERIALS, 8)
                    .inputFluids(GTMaterials.RedAlloy.getFluid(GTValues.L * 4))
                    .outputItems(new ItemStack(Blocks.PISTON, 8))
                    .duration(600).EUt(VA[LV]).save(provider);

            ASSEMBLER_RECIPES.recipeBuilder("piston_titanium")
                    .inputItems(TagPrefix.rod, GTMaterials.Titanium)
                    .inputItems(TagPrefix.gearSmall, GTMaterials.Titanium)
                    .inputItems(ItemTags.WOODEN_SLABS, 16)
                    .inputItems(ItemTags.STONE_CRAFTING_MATERIALS, 16)
                    .inputFluids(GTMaterials.RedAlloy.getFluid(GTValues.L * 8))
                    .outputItems(new ItemStack(Blocks.PISTON, 16))
                    .duration(800).EUt(VA[LV]).save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, "stone_pressure_plate",
                    new ItemStack(Blocks.STONE_PRESSURE_PLATE, 2), "ShS", "LCL", "SdS",
                    'S', new MaterialEntry(TagPrefix.screw, GTMaterials.Iron),
                    'L', new ItemStack(Blocks.STONE_SLAB),
                    'C', new MaterialEntry(TagPrefix.spring, GTMaterials.Iron));

            VanillaRecipeHelper.addShapedRecipe(provider, "polished_blackstone_pressure_plate",
                    new ItemStack(Blocks.POLISHED_BLACKSTONE_PRESSURE_PLATE, 2), "ShS", "LCL", "SdS",
                    'S', new MaterialEntry(TagPrefix.screw, GTMaterials.Iron),
                    'L', new ItemStack(Blocks.POLISHED_BLACKSTONE_SLAB),
                    'C', new MaterialEntry(TagPrefix.spring, GTMaterials.Iron));

            VanillaRecipeHelper.addShapedRecipe(provider, "heavy_weighted_pressure_plate",
                    new ItemStack(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE), "ShS", "LCL", "SdS",
                    'S', new MaterialEntry(TagPrefix.screw, GTMaterials.Steel),
                    'L', new MaterialEntry(TagPrefix.plate, GTMaterials.Gold),
                    'C', new MaterialEntry(TagPrefix.spring, GTMaterials.Steel));

            VanillaRecipeHelper.addShapedRecipe(provider, "light_weighted_pressure_plate",
                    new ItemStack(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE), "ShS", "LCL", "SdS",
                    'S', new MaterialEntry(TagPrefix.screw, GTMaterials.Steel),
                    'L', new MaterialEntry(TagPrefix.plate, GTMaterials.Iron),
                    'C', new MaterialEntry(TagPrefix.spring, GTMaterials.Steel));

            ASSEMBLER_RECIPES.recipeBuilder("light_weighted_pressure_plate")
                    .inputItems(TagPrefix.spring, GTMaterials.Steel)
                    .inputItems(TagPrefix.plate, GTMaterials.Gold)
                    .outputItems(new ItemStack(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE))
                    .duration(200).EUt(16).save(provider);

            ASSEMBLER_RECIPES.recipeBuilder("heavy_weighted_pressure_plate")
                    .inputItems(TagPrefix.spring, GTMaterials.Steel)
                    .inputItems(TagPrefix.plate, GTMaterials.Iron)
                    .outputItems(new ItemStack(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE))
                    .duration(200).EUt(16).save(provider);

            CUTTER_RECIPES.recipeBuilder("stone_button")
                    .inputItems(new ItemStack(Blocks.STONE_PRESSURE_PLATE))
                    .outputItems(new ItemStack(Blocks.STONE_BUTTON, 12))
                    .duration(25).EUt(VA[ULV]).save(provider);

            CUTTER_RECIPES.recipeBuilder("blackstone_button")
                    .inputItems(new ItemStack(Blocks.POLISHED_BLACKSTONE_PRESSURE_PLATE))
                    .outputItems(new ItemStack(Blocks.POLISHED_BLACKSTONE_BUTTON, 12))
                    .duration(25).EUt(VA[ULV]).save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, "lever", new ItemStack(Blocks.LEVER), "B", "S",
                    'B', new ItemStack(Blocks.STONE_BUTTON),
                    'S', new MaterialEntry(TagPrefix.rod, GTMaterials.Wood));

            VanillaRecipeHelper.addShapedRecipe(provider, "daylight_detector", new ItemStack(Blocks.DAYLIGHT_DETECTOR),
                    "GGG", "PPP", "SRS",
                    'G', new ItemStack(Blocks.GLASS),
                    'P', new MaterialEntry(TagPrefix.plate, GTMaterials.NetherQuartz),
                    'S', ItemTags.WOODEN_SLABS,
                    'R', new MaterialEntry(TagPrefix.rod, GTMaterials.RedAlloy));

            VanillaRecipeHelper.addShapedRecipe(provider, "daylight_detector_certus",
                    new ItemStack(Blocks.DAYLIGHT_DETECTOR), "GGG", "PPP", "SRS",
                    'G', new ItemStack(Blocks.GLASS),
                    'P', new MaterialEntry(TagPrefix.plate, GTMaterials.CertusQuartz),
                    'S', ItemTags.WOODEN_SLABS,
                    'R', new MaterialEntry(TagPrefix.rod, GTMaterials.RedAlloy));

            VanillaRecipeHelper.addShapedRecipe(provider, "daylight_detector_quartzite",
                    new ItemStack(Blocks.DAYLIGHT_DETECTOR), "GGG", "PPP", "SRS",
                    'G', new ItemStack(Blocks.GLASS, 1),
                    'P', new MaterialEntry(TagPrefix.plate, GTMaterials.Quartzite),
                    'S', ItemTags.WOODEN_SLABS,
                    'R', new MaterialEntry(TagPrefix.rod, GTMaterials.RedAlloy));

            ASSEMBLER_RECIPES.recipeBuilder("daylight_detector")
                    .inputItems(rod, RedAlloy)
                    .inputItems(new ItemStack(Blocks.GLASS, 3))
                    .inputItems(gem, NetherQuartz, 3)
                    .inputItems(ItemTags.PLANKS)
                    .outputItems(new ItemStack(Blocks.DAYLIGHT_DETECTOR))
                    .duration(200).EUt(16).save(provider);

            ASSEMBLER_RECIPES.recipeBuilder("daylight_detector_certus")
                    .inputItems(rod, RedAlloy)
                    .inputItems(new ItemStack(Blocks.GLASS, 3))
                    .inputItems(gem, CertusQuartz, 3)
                    .inputItems(ItemTags.PLANKS)
                    .outputItems(new ItemStack(Blocks.DAYLIGHT_DETECTOR))
                    .duration(200).EUt(16).save(provider);

            ASSEMBLER_RECIPES.recipeBuilder("daylight_detector_quartzite")
                    .inputItems(rod, RedAlloy)
                    .inputItems(new ItemStack(Blocks.GLASS, 3))
                    .inputItems(gem, Quartzite, 3)
                    .inputItems(ItemTags.PLANKS)
                    .outputItems(new ItemStack(Blocks.DAYLIGHT_DETECTOR))
                    .duration(200).EUt(16).save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, true, "redstone_lamp", new ItemStack(Blocks.REDSTONE_LAMP),
                    "PPP",
                    "PGP", "PRP",
                    'P', new ItemStack(Blocks.GLASS_PANE),
                    'G', new ItemStack(Blocks.GLOWSTONE),
                    'R', new MaterialEntry(TagPrefix.rod, GTMaterials.RedAlloy));

            VanillaRecipeHelper.addShapedRecipe(provider, true, "tripwire_hook", new ItemStack(Blocks.TRIPWIRE_HOOK),
                    "IRI",
                    "SRS", " S ",
                    'I', new MaterialEntry(TagPrefix.ring, GTMaterials.Iron),
                    'R', new MaterialEntry(TagPrefix.rod, GTMaterials.Wood),
                    'S', new ItemStack(Items.STRING));

            VanillaRecipeHelper.addShapedRecipe(provider, true, "dropper", new ItemStack(Blocks.DROPPER), "CRC", "STS",
                    "GAG",
                    'C', ItemTags.STONE_CRAFTING_MATERIALS,
                    'R', new MaterialEntry(TagPrefix.ring, GTMaterials.Iron),
                    'S', new MaterialEntry(TagPrefix.springSmall, GTMaterials.Iron),
                    'T', new ItemStack(Items.STRING),
                    'G', new MaterialEntry(TagPrefix.gearSmall, GTMaterials.Iron),
                    'A', new MaterialEntry(TagPrefix.rod, GTMaterials.RedAlloy));

            VanillaRecipeHelper.addShapedRecipe(provider, true, "observer", new ItemStack(Blocks.OBSERVER), "RCR",
                    "CQC",
                    "GSG",
                    'R', new MaterialEntry(TagPrefix.ring, GTMaterials.Iron),
                    'C', ItemTags.STONE_CRAFTING_MATERIALS,
                    'Q', new MaterialEntry(TagPrefix.plate, GTMaterials.NetherQuartz),
                    'G', new MaterialEntry(TagPrefix.gearSmall, GTMaterials.Iron),
                    'S', new MaterialEntry(TagPrefix.rod, GTMaterials.RedAlloy));

            VanillaRecipeHelper.addShapedRecipe(provider, "observer_certus", new ItemStack(Blocks.OBSERVER), "RCR",
                    "CQC", "GSG",
                    'R', new MaterialEntry(TagPrefix.ring, GTMaterials.Iron),
                    'C', ItemTags.STONE_CRAFTING_MATERIALS,
                    'Q', new MaterialEntry(TagPrefix.plate, GTMaterials.CertusQuartz),
                    'G', new MaterialEntry(TagPrefix.gearSmall, GTMaterials.Iron),
                    'S', new MaterialEntry(TagPrefix.rod, GTMaterials.RedAlloy));

            VanillaRecipeHelper.addShapedRecipe(provider, "observer_quartzite", new ItemStack(Blocks.OBSERVER), "RCR",
                    "CQC", "GSG",
                    'R', new MaterialEntry(TagPrefix.ring, GTMaterials.Iron),
                    'C', ItemTags.STONE_CRAFTING_MATERIALS,
                    'Q', new MaterialEntry(TagPrefix.plate, GTMaterials.Quartzite),
                    'G', new MaterialEntry(TagPrefix.gearSmall, GTMaterials.Iron),
                    'S', new MaterialEntry(TagPrefix.rod, GTMaterials.RedAlloy));

            VanillaRecipeHelper.addShapedRecipe(provider, "repeater", new ItemStack(Items.REPEATER), "S S", "TdT",
                    "PRP",
                    'S', new MaterialEntry(TagPrefix.screw, GTMaterials.Iron),
                    'T', new ItemStack(Blocks.REDSTONE_TORCH),
                    'P', new ItemStack(Blocks.STONE_PRESSURE_PLATE),
                    'R', new MaterialEntry(TagPrefix.rod, GTMaterials.RedAlloy));

            VanillaRecipeHelper.addShapedRecipe(provider, "comparator", new ItemStack(Items.COMPARATOR), "STS", "TQT",
                    "PdP",
                    'S', new MaterialEntry(TagPrefix.screw, GTMaterials.Iron),
                    'T', new ItemStack(Blocks.REDSTONE_TORCH),
                    'Q', new MaterialEntry(TagPrefix.plate, GTMaterials.NetherQuartz),
                    'P', new ItemStack(Blocks.STONE_PRESSURE_PLATE));

            VanillaRecipeHelper.addShapedRecipe(provider, "comparator_certus", new ItemStack(Items.COMPARATOR), "STS",
                    "TQT", "PdP",
                    'S', new MaterialEntry(TagPrefix.screw, GTMaterials.Iron),
                    'T', new ItemStack(Blocks.REDSTONE_TORCH),
                    'Q', new MaterialEntry(TagPrefix.plate, GTMaterials.CertusQuartz),
                    'P', new ItemStack(Blocks.STONE_PRESSURE_PLATE));

            VanillaRecipeHelper.addShapedRecipe(provider, "comparator_quartzite", new ItemStack(Items.COMPARATOR),
                    "STS", "TQT", "PdP",
                    'S', new MaterialEntry(TagPrefix.screw, GTMaterials.Iron),
                    'T', new ItemStack(Blocks.REDSTONE_TORCH),
                    'Q', new MaterialEntry(TagPrefix.plate, GTMaterials.Quartzite),
                    'P', new ItemStack(Blocks.STONE_PRESSURE_PLATE));

            VanillaRecipeHelper.addShapedRecipe(provider, "powered_rail", new ItemStack(Blocks.POWERED_RAIL, 6), "SPS",
                    "IWI", "GdG",
                    'S', new MaterialEntry(TagPrefix.screw, GTMaterials.Steel),
                    'P', new MaterialEntry(TagPrefix.plate, GTMaterials.RedAlloy),
                    'I', new MaterialEntry(TagPrefix.rod, GTMaterials.Iron),
                    'W', new MaterialEntry(TagPrefix.rod, GTMaterials.Wood),
                    'G', new MaterialEntry(TagPrefix.rod, GTMaterials.Gold));

            VanillaRecipeHelper.addShapedRecipe(provider, "detector_rail", new ItemStack(Blocks.DETECTOR_RAIL, 6),
                    "SPS", "IWI", "IdI",
                    'S', new MaterialEntry(TagPrefix.screw, GTMaterials.Iron),
                    'P', new ItemStack(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE),
                    'I', new MaterialEntry(TagPrefix.rod, GTMaterials.Iron),
                    'W', new MaterialEntry(TagPrefix.rod, GTMaterials.Wood));

            VanillaRecipeHelper.addShapedRecipe(provider, "rail", new ItemStack(Blocks.RAIL, 8), "ShS", "IWI", "IdI",
                    'S', new MaterialEntry(TagPrefix.screw, GTMaterials.Iron),
                    'I', new MaterialEntry(TagPrefix.rod, GTMaterials.Iron),
                    'W', new MaterialEntry(TagPrefix.rod, GTMaterials.Wood));

            VanillaRecipeHelper.addShapedRecipe(provider, "activator_rail", new ItemStack(Blocks.ACTIVATOR_RAIL, 6),
                    "SPS", "IWI", "IdI",
                    'S', new MaterialEntry(TagPrefix.screw, GTMaterials.Iron),
                    'P', new ItemStack(Blocks.REDSTONE_TORCH),
                    'I', new MaterialEntry(TagPrefix.rod, GTMaterials.Iron),
                    'W', Tags.Items.RODS_WOODEN);

            VanillaRecipeHelper.addShapedRecipe(provider, "redstone_torch", new ItemStack(Blocks.REDSTONE_TORCH), "R",
                    "T",
                    'R', new MaterialEntry(TagPrefix.dust, GTMaterials.Redstone),
                    'T', new ItemStack(Blocks.TORCH));

            ASSEMBLER_RECIPES.recipeBuilder("calibrated_sculk_sensor")
                    .inputItems(new ItemStack(Blocks.SCULK_SENSOR))
                    .inputItems(gem, Amethyst)
                    .inputItems(plate, Amethyst)
                    .outputItems(new ItemStack(Blocks.CALIBRATED_SCULK_SENSOR))
                    .duration(200).EUt(16).save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, "target", new ItemStack(Items.TARGET), "RBR",
                    "PHP", "RPR",
                    'R', new MaterialEntry(dust, Redstone),
                    'P', new ItemStack(Items.PAPER),
                    'H', new ItemStack(Items.HAY_BLOCK),
                    'B', ItemTags.WOODEN_BUTTONS); // wooden buttons because ONLY WOODEN BUTTONS CAN BE TRIGGERED WITH
                                                   // PROJECTILES. NO STONE!!

            ASSEMBLER_RECIPES.recipeBuilder("target")
                    .inputItems(dust, Redstone, 4)
                    .inputItems(ItemTags.WOODEN_BUTTONS)
                    .inputItems(new ItemStack(Items.PAPER), 3)
                    .inputItems(new ItemStack(Items.HAY_BLOCK))
                    .outputItems(new ItemStack(Items.TARGET))
                    .duration(200).EUt(120).save(provider);

        } else {
            VanillaRecipeHelper.addShapedRecipe(provider, "piston_bronze", new ItemStack(Blocks.PISTON, 1), "WWW",
                    "CBC", "CRC",
                    'W', ItemTags.PLANKS,
                    'C', ItemTags.STONE_CRAFTING_MATERIALS,
                    'R', new MaterialEntry(dust, Redstone),
                    'B', new MaterialEntry(ingot, Bronze));

            VanillaRecipeHelper.addShapedRecipe(provider, "piston_steel", new ItemStack(Blocks.PISTON, 2), "WWW", "CBC",
                    "CRC",
                    'W', ItemTags.PLANKS,
                    'C', ItemTags.STONE_CRAFTING_MATERIALS,
                    'R', new MaterialEntry(dust, Redstone),
                    'B', new MaterialEntry(ingot, Steel));

            VanillaRecipeHelper.addShapedRecipe(provider, "piston_aluminium", new ItemStack(Blocks.PISTON, 4), "WWW",
                    "CBC", "CRC",
                    'W', ItemTags.PLANKS,
                    'C', ItemTags.STONE_CRAFTING_MATERIALS,
                    'R', new MaterialEntry(dust, Redstone),
                    'B', new MaterialEntry(ingot, Aluminium));

            VanillaRecipeHelper.addShapedRecipe(provider, "piston_titanium", new ItemStack(Blocks.PISTON, 8), "WWW",
                    "CBC", "CRC",
                    'W', ItemTags.PLANKS,
                    'C', ItemTags.STONE_CRAFTING_MATERIALS,
                    'R', new MaterialEntry(dust, Redstone),
                    'B', new MaterialEntry(ingot, Titanium));

            VanillaRecipeHelper.addShapedRecipe(provider, "sticky_piston_resin", new ItemStack(Blocks.STICKY_PISTON),
                    "h", "R", "P",
                    'R', STICKY_RESIN.asStack(),
                    'P', new ItemStack(Blocks.PISTON));

            ASSEMBLER_RECIPES.recipeBuilder("piston_iron").duration(100).EUt(16).inputItems(plate, Iron)
                    .inputItems(ItemTags.PLANKS, 3).inputItems(ItemTags.STONE_CRAFTING_MATERIALS, 4)
                    .inputItems(dust, Redstone).outputItems(new ItemStack(Blocks.PISTON)).save(provider);
            ASSEMBLER_RECIPES.recipeBuilder("piston_bronze").duration(100).EUt(16).inputItems(plate, Bronze)
                    .inputItems(ItemTags.PLANKS, 3).inputItems(ItemTags.STONE_CRAFTING_MATERIALS, 4)
                    .inputItems(dust, Redstone).outputItems(new ItemStack(Blocks.PISTON)).save(provider);
            ASSEMBLER_RECIPES.recipeBuilder("piston_steel").duration(100).EUt(16).inputItems(plate, Steel)
                    .inputItems(ItemTags.PLANKS, 3).inputItems(ItemTags.STONE_CRAFTING_MATERIALS, 4)
                    .inputItems(dust, Redstone).outputItems(new ItemStack(Blocks.PISTON, 2)).save(provider);
            ASSEMBLER_RECIPES.recipeBuilder("piston_aluminium").duration(100).EUt(16).inputItems(plate, Aluminium)
                    .inputItems(ItemTags.PLANKS, 3).inputItems(ItemTags.STONE_CRAFTING_MATERIALS, 4)
                    .inputItems(dust, Redstone).outputItems(new ItemStack(Blocks.PISTON, 4)).save(provider);
            ASSEMBLER_RECIPES.recipeBuilder("piston_titanium").duration(100).EUt(16).inputItems(plate, Titanium)
                    .inputItems(ItemTags.PLANKS, 3).inputItems(ItemTags.STONE_CRAFTING_MATERIALS, 4)
                    .inputItems(dust, Redstone).outputItems(new ItemStack(Blocks.PISTON, 8)).save(provider);

            ASSEMBLER_RECIPES.recipeBuilder("light_weighted_pressure_plate")
                    .inputItems(plate, Gold, 2)
                    .outputItems(new ItemStack(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE))
                    .circuitMeta(3).duration(100).EUt(4).save(provider);

            ASSEMBLER_RECIPES.recipeBuilder("heavy_weighted_pressure_plate")
                    .inputItems(plate, Iron, 2)
                    .outputItems(new ItemStack(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE))
                    .circuitMeta(2).duration(100).EUt(4).save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, "comparator_certus", new ItemStack(Items.COMPARATOR), " T ",
                    "TQT", "SSS",
                    'T', new ItemStack(Blocks.REDSTONE_TORCH),
                    'Q', new MaterialEntry(gem, CertusQuartz),
                    'S', new ItemStack(Blocks.STONE));

            VanillaRecipeHelper.addShapedRecipe(provider, "comparator_quartzite", new ItemStack(Items.COMPARATOR),
                    " T ", "TQT", "SSS",
                    'T', new ItemStack(Blocks.REDSTONE_TORCH),
                    'Q', new MaterialEntry(gem, Quartzite),
                    'S', new ItemStack(Blocks.STONE));

            VanillaRecipeHelper.addShapedRecipe(provider, "daylight_detector_certus",
                    new ItemStack(Blocks.DAYLIGHT_DETECTOR), "GGG", "CCC", "PPP",
                    'G', new ItemStack(Blocks.GLASS),
                    'C', new MaterialEntry(gem, CertusQuartz),
                    'P', ItemTags.WOODEN_SLABS);

            VanillaRecipeHelper.addShapedRecipe(provider, "daylight_detector_quartzite",
                    new ItemStack(Blocks.DAYLIGHT_DETECTOR), "GGG", "CCC", "PPP",
                    'G', new ItemStack(Blocks.GLASS),
                    'C', new MaterialEntry(gem, Quartzite),
                    'P', ItemTags.WOODEN_SLABS);

            ASSEMBLER_RECIPES.recipeBuilder("note_block").duration(100).EUt(16).inputItems(ItemTags.PLANKS, 8)
                    .inputItems(dust, Redstone).circuitMeta(1).outputItems(new ItemStack(Blocks.NOTE_BLOCK))
                    .save(provider);
            ASSEMBLER_RECIPES.recipeBuilder("jukebox").duration(100).EUt(16).inputItems(ItemTags.PLANKS, 8)
                    .inputItems(gem, Diamond).outputItems(new ItemStack(Blocks.JUKEBOX)).save(provider);
            ASSEMBLER_RECIPES.recipeBuilder("target").duration(100).EUt(16).inputItems(Items.REDSTONE, 4)
                    .inputItems(Items.HAY_BLOCK).outputItems(new ItemStack(Blocks.TARGET)).save(provider);
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
            createChestplateRecipe(provider, "golden_chestplate", new ItemStack(Items.GOLDEN_CHESTPLATE),
                    GTMaterials.Gold);
            createLeggingsRecipe(provider, "golden_leggings", new ItemStack(Items.GOLDEN_LEGGINGS), GTMaterials.Gold);
            createBootsRecipe(provider, "golden_boots", new ItemStack(Items.GOLDEN_BOOTS), GTMaterials.Gold);

            createShovelRecipe(provider, "diamond_shovel", new ItemStack(Items.DIAMOND_SHOVEL), GTMaterials.Diamond);
            createPickaxeRecipe(provider, "diamond_pickaxe", new ItemStack(Items.DIAMOND_PICKAXE), GTMaterials.Diamond);
            createAxeRecipe(provider, "diamond_axe", new ItemStack(Items.DIAMOND_AXE), GTMaterials.Diamond);
            createSwordRecipe(provider, "diamond_sword", new ItemStack(Items.DIAMOND_SWORD), GTMaterials.Diamond);
            createHoeRecipe(provider, "diamond_hoe", new ItemStack(Items.DIAMOND_HOE), GTMaterials.Diamond);
            createHelmetRecipe(provider, "diamond_helmet", new ItemStack(Items.DIAMOND_HELMET), GTMaterials.Diamond);
            createChestplateRecipe(provider, "diamond_chestplate", new ItemStack(Items.DIAMOND_CHESTPLATE),
                    GTMaterials.Diamond);
            createLeggingsRecipe(provider, "diamond_leggings", new ItemStack(Items.DIAMOND_LEGGINGS),
                    GTMaterials.Diamond);
            createBootsRecipe(provider, "diamond_boots", new ItemStack(Items.DIAMOND_BOOTS), GTMaterials.Diamond);

            VanillaRecipeHelper.addShapedRecipe(provider, "compass", new ItemStack(Items.COMPASS), "SGB", "RPR", "AdS",
                    'S', new MaterialEntry(TagPrefix.screw, GTMaterials.Iron),
                    'G', new ItemStack(Blocks.GLASS_PANE),
                    'B', new MaterialEntry(TagPrefix.bolt, GTMaterials.IronMagnetic),
                    'R', new MaterialEntry(TagPrefix.ring, GTMaterials.Zinc),
                    'P', new MaterialEntry(TagPrefix.plate, GTMaterials.Iron),
                    'A', new MaterialEntry(TagPrefix.bolt, GTMaterials.RedAlloy));

            ASSEMBLER_RECIPES.recipeBuilder("compass")
                    .inputItems(TagPrefix.plate, GTMaterials.Iron)
                    .inputItems(TagPrefix.ring, GTMaterials.Zinc)
                    .inputItems(TagPrefix.bolt, GTMaterials.RedAlloy)
                    .inputItems(TagPrefix.bolt, GTMaterials.IronMagnetic)
                    .inputItems(TagPrefix.screw, GTMaterials.Iron, 2)
                    .outputItems(new ItemStack(Items.COMPASS))
                    .duration(100).EUt(16)
                    .addMaterialInfo(true).save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, "fishing_rod", new ItemStack(Items.FISHING_ROD), "  S", " SL",
                    "SxR",
                    'S', new MaterialEntry(TagPrefix.rodLong, GTMaterials.Wood),
                    'L', new ItemStack(Items.STRING),
                    'R', new MaterialEntry(TagPrefix.ring, GTMaterials.Iron));

            VanillaRecipeHelper.addShapedRecipe(provider, "clock", new ItemStack(Items.CLOCK), "RPR", "BCB", "dSw",
                    'R', new MaterialEntry(TagPrefix.ring, GTMaterials.Gold),
                    'P', new MaterialEntry(TagPrefix.plate, GTMaterials.Gold),
                    'B', new MaterialEntry(TagPrefix.bolt, GTMaterials.Gold),
                    'C', new ItemStack(Items.COMPARATOR),
                    'S', new MaterialEntry(TagPrefix.screw, GTMaterials.Gold));

            ASSEMBLER_RECIPES.recipeBuilder("clock")
                    .inputItems(TagPrefix.plate, GTMaterials.Gold)
                    .inputItems(TagPrefix.ring, GTMaterials.Gold)
                    .inputItems(TagPrefix.bolt, GTMaterials.Gold, 2)
                    .inputItems(TagPrefix.screw, GTMaterials.Gold)
                    .inputItems(new ItemStack(Items.COMPARATOR))
                    .outputItems(new ItemStack(Items.CLOCK))
                    .duration(100).EUt(16)
                    .addMaterialInfo(true).save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, "shears", new ItemStack(Items.SHEARS), "PSP", "hRf", "TdT",
                    'P', new MaterialEntry(TagPrefix.plate, GTMaterials.Iron),
                    'S', new MaterialEntry(TagPrefix.screw, GTMaterials.Iron),
                    'R', new MaterialEntry(TagPrefix.ring, GTMaterials.Iron),
                    'T', new MaterialEntry(TagPrefix.rod, GTMaterials.Wood));

            VanillaRecipeHelper.addShapedRecipe(provider, "shield", new ItemStack(Items.SHIELD), "BRB", "LPL", "BRB",
                    'B', new MaterialEntry(TagPrefix.bolt, GTMaterials.Iron),
                    'R', new MaterialEntry(TagPrefix.rod, GTMaterials.Iron),
                    'L', new MaterialEntry(TagPrefix.rodLong, GTMaterials.Iron),
                    'P', new MaterialEntry(TagPrefix.plate, GTMaterials.Wood));

            VanillaRecipeHelper.addShapedRecipe(provider, "bow", new ItemStack(Items.BOW), "hLS", "LRS", "fLS",
                    'L', new MaterialEntry(TagPrefix.rodLong, GTMaterials.Wood),
                    'S', new ItemStack(Items.STRING),
                    'R', new MaterialEntry(TagPrefix.ring, GTMaterials.Iron));

            VanillaRecipeHelper.addShapedRecipe(provider, "crossbow", new ItemStack(Items.CROSSBOW), "RIR", "STS",
                    "sRf",
                    'R', new MaterialEntry(TagPrefix.rodLong, GTMaterials.Wood),
                    'S', new ItemStack(Items.STRING),
                    'T', new ItemStack(Items.TRIPWIRE_HOOK),
                    'I', new MaterialEntry(ring, Iron));
        } else {
            ASSEMBLER_RECIPES.recipeBuilder("compass")
                    .inputItems(dust, Redstone)
                    .inputItems(plate, Iron, 4)
                    .circuitMeta(1)
                    .outputItems(new ItemStack(Items.COMPASS))
                    .duration(100).EUt(4)
                    .addMaterialInfo(true).save(provider);

            ASSEMBLER_RECIPES.recipeBuilder("clock")
                    .inputItems(dust, Redstone)
                    .inputItems(plate, Gold, 4)
                    .outputItems(new ItemStack(Items.CLOCK))
                    .duration(100).EUt(4)
                    .addMaterialInfo(true).save(provider);
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
     * Replaces recipes for items that don't fit in any other config option.
     * Vanilla items go here only if they not fit the criteria for removeVanillaBlockRecipes,
     * disableManualCompression, or any of the other config options
     */
    private static void hardMiscRecipes(Consumer<FinishedRecipe> provider) {
        if (ConfigHolder.INSTANCE.recipes.hardMiscRecipes) {
            VanillaRecipeHelper.addShapedRecipe(provider, true, "beacon", new ItemStack(Blocks.BEACON), "GLG", "GSG",
                    "OOO",
                    'G', new ItemStack(Blocks.GLASS),
                    'L', new MaterialEntry(TagPrefix.lens, GTMaterials.NetherStar),
                    'S', new ItemStack(Items.NETHER_STAR),
                    'O', new MaterialEntry(TagPrefix.plate, GTMaterials.Obsidian));

            VanillaRecipeHelper.addShapedRecipe(provider, "jack_o_lantern", new ItemStack(Blocks.JACK_O_LANTERN), "PT",
                    "k ",
                    'P', new ItemStack(Blocks.PUMPKIN),
                    'T', new ItemStack(Blocks.TORCH));

            VanillaRecipeHelper.addShapedRecipe(provider, "book", new ItemStack(Items.BOOK), "SPL", "SPG", "SPL",
                    'S', new ItemStack(Items.STRING),
                    'P', new ItemStack(Items.PAPER),
                    'L', new ItemStack(Items.LEATHER),
                    'G', GTItems.STICKY_RESIN.asStack());

            VanillaRecipeHelper.addShapedRecipe(provider, "brewing_stand", new ItemStack(Items.BREWING_STAND), "RBR",
                    "ABA", "SCS",
                    'R', new MaterialEntry(TagPrefix.ring, GTMaterials.Aluminium),
                    'B', new MaterialEntry(TagPrefix.rod, GTMaterials.Blaze),
                    'A', new MaterialEntry(TagPrefix.rod, GTMaterials.Aluminium),
                    'S', new MaterialEntry(TagPrefix.screw, GTMaterials.Aluminium),
                    'C', new ItemStack(Items.CAULDRON));

            VanillaRecipeHelper.addShapedRecipe(provider, true, "enchanting_table",
                    new ItemStack(Blocks.ENCHANTING_TABLE),
                    "DCD", "PBP", "DPD",
                    'D', new MaterialEntry(TagPrefix.gem, GTMaterials.Diamond),
                    'C', new ItemStack(Blocks.RED_CARPET),
                    'P', new MaterialEntry(TagPrefix.plate, GTMaterials.Obsidian),
                    'B', new ItemStack(Blocks.BOOKSHELF));

            VanillaRecipeHelper.addShapedRecipe(provider, "jukebox", new ItemStack(Blocks.JUKEBOX), "LBL", "NRN", "LGL",
                    'L', ItemTags.LOGS,
                    'B', new MaterialEntry(TagPrefix.bolt, GTMaterials.Diamond),
                    'N', new ItemStack(Blocks.NOTE_BLOCK),
                    'R', new MaterialEntry(TagPrefix.ring, GTMaterials.Iron),
                    'G', new MaterialEntry(TagPrefix.gear, GTMaterials.Iron));

            ASSEMBLER_RECIPES.recipeBuilder("jukebox")
                    .inputItems(TagPrefix.bolt, GTMaterials.Diamond)
                    .inputItems(TagPrefix.gear, GTMaterials.Iron)
                    .inputItems(TagPrefix.ring, GTMaterials.Iron)
                    .inputItems(TagPrefix.plate, GTMaterials.Wood, 4)
                    .inputItems(new ItemStack(Blocks.NOTE_BLOCK, 2))
                    .outputItems(new ItemStack(Blocks.JUKEBOX))
                    .duration(100).EUt(16)
                    .addMaterialInfo(true).save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, "note_block", new ItemStack(Blocks.NOTE_BLOCK), "PPP", "BGB",
                    "PRP",
                    'P', new MaterialEntry(TagPrefix.plate, GTMaterials.Wood),
                    'B', new ItemStack(Blocks.IRON_BARS),
                    'G', new MaterialEntry(TagPrefix.gear, GTMaterials.Wood),
                    'R', new MaterialEntry(TagPrefix.rod, GTMaterials.RedAlloy));

            ASSEMBLER_RECIPES.recipeBuilder("note_block")
                    .inputItems(TagPrefix.plate, GTMaterials.Wood, 4)
                    .inputItems(TagPrefix.gear, GTMaterials.Wood)
                    .inputItems(TagPrefix.rod, GTMaterials.RedAlloy)
                    .inputItems(new ItemStack(Blocks.IRON_BARS, 2))
                    .outputItems(new ItemStack(Blocks.NOTE_BLOCK))
                    .duration(100).EUt(16)
                    .addMaterialInfo(true).save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, "furnace", new ItemStack(Blocks.FURNACE), "CCC", "FFF", "CCC",
                    'F', new ItemStack(Items.FLINT),
                    'C', ItemTags.STONE_CRAFTING_MATERIALS);

            ASSEMBLER_RECIPES.recipeBuilder("furnace")
                    .circuitMeta(8)
                    .inputItems(ItemTags.STONE_CRAFTING_MATERIALS, 8)
                    .inputItems(new ItemStack(Items.FLINT))
                    .outputItems(new ItemStack(Blocks.FURNACE))
                    .duration(100).EUt(VA[ULV])
                    .addMaterialInfo(true).save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, "crafting_table", new ItemStack(Blocks.CRAFTING_TABLE), "FF",
                    "WW",
                    'F', new ItemStack(Items.FLINT),
                    'W', ItemTags.LOGS);

            ASSEMBLER_RECIPES.recipeBuilder("crafting_table").duration(80).EUt(6)
                    .inputItems(ItemTags.LOGS)
                    .inputItems(new ItemStack(Items.FLINT))
                    .outputItems(new ItemStack(Blocks.CRAFTING_TABLE))
                    .addMaterialInfo(true)
                    .save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, "lead", new ItemStack(Items.LEAD), "SSS", "SBS", "SSS",
                    'S', new ItemStack(Items.STRING),
                    'B', new ItemStack(Items.SLIME_BALL));

            VanillaRecipeHelper.addShapedRecipe(provider, "item_frame", new ItemStack(Items.ITEM_FRAME), "SRS", "TLT",
                    "TTT",
                    'S', new ItemStack(Items.STRING),
                    'R', new MaterialEntry(TagPrefix.ring, GTMaterials.Iron),
                    'T', new MaterialEntry(TagPrefix.rod, GTMaterials.Wood),
                    'L', new ItemStack(Items.LEATHER));

            VanillaRecipeHelper.addShapedRecipe(provider, "painting", new ItemStack(Items.PAINTING), "SRS", "TCT",
                    "TTT",
                    'S', new ItemStack(Items.STRING),
                    'R', new MaterialEntry(TagPrefix.ring, GTMaterials.Iron),
                    'T', new MaterialEntry(TagPrefix.rod, GTMaterials.Wood),
                    'C', ItemTags.WOOL_CARPETS);

            VanillaRecipeHelper.addShapedRecipe(provider, "chest_minecart", new ItemStack(Items.CHEST_MINECART), "hIw",
                    " M ", " d ", 'I', Tags.Items.CHESTS_WOODEN, 'M', new ItemStack(Items.MINECART));
            VanillaRecipeHelper.addShapedRecipe(provider, "furnace_minecart", new ItemStack(Items.FURNACE_MINECART),
                    "hIw", " M ", " d ", 'I', new ItemStack(Blocks.FURNACE), 'M', new ItemStack(Items.MINECART));
            VanillaRecipeHelper.addShapedRecipe(provider, "tnt_minecart", new ItemStack(Items.TNT_MINECART), "hIw",
                    " M ", " d ", 'I', new ItemStack(Blocks.TNT), 'M', new ItemStack(Items.MINECART));
            VanillaRecipeHelper.addShapedRecipe(provider, "hopper_minecart", new ItemStack(Items.HOPPER_MINECART),
                    "hIw", " M ", " d ", 'I', new ItemStack(Blocks.HOPPER), 'M', new ItemStack(Items.MINECART));

            VanillaRecipeHelper.addShapedRecipe(provider, "flower_pot", new ItemStack(Items.FLOWER_POT), "BfB", " B ",
                    'B', new ItemStack(Items.BRICK));

            VanillaRecipeHelper.addShapedRecipe(provider, "armor_stand", new ItemStack(Items.ARMOR_STAND), "BSB", "hSs",
                    "IPI",
                    'B', new MaterialEntry(TagPrefix.bolt, GTMaterials.Wood),
                    'S', new MaterialEntry(TagPrefix.rod, GTMaterials.Wood),
                    'I', new MaterialEntry(TagPrefix.plate, GTMaterials.Iron),
                    'P', new ItemStack(Blocks.STONE_PRESSURE_PLATE));
            ASSEMBLER_RECIPES.recipeBuilder("armor_stand")
                    .inputItems(Blocks.STONE_PRESSURE_PLATE.asItem())
                    .inputItems(TagPrefix.plate, GTMaterials.Iron, 2)
                    .inputItems(TagPrefix.rod, GTMaterials.Wood, 2)
                    .outputItems(Items.ARMOR_STAND)
                    .duration(100).EUt(VA[ULV]).save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, true, "trapped_chest", new ItemStack(Blocks.TRAPPED_CHEST),
                    " H ",
                    "SCS", " d ",
                    'H', new ItemStack(Blocks.TRIPWIRE_HOOK),
                    'S', new MaterialEntry(TagPrefix.screw, GTMaterials.Iron),
                    'C', new ItemStack(Blocks.CHEST));

            ASSEMBLER_RECIPES.recipeBuilder("ender_chest")
                    .inputItems(Tags.Items.CHESTS_WOODEN)
                    .inputItems(TagPrefix.plateDense, GTMaterials.Obsidian, 6)
                    .inputItems(TagPrefix.plate, GTMaterials.EnderEye)
                    .outputItems(Blocks.ENDER_CHEST.asItem())
                    .duration(200).EUt(VA[MV])
                    .addMaterialInfo(true).save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, "lantern", new ItemStack(Blocks.LANTERN), "hRf",
                    "RFR", " R ",
                    'F', new ItemStack(Blocks.TORCH),
                    'R', new MaterialEntry(ring, Iron));

            ASSEMBLER_RECIPES.recipeBuilder("lantern")
                    .inputItems(new ItemStack(Blocks.TORCH))
                    .inputItems(ring, Iron, 4)
                    .outputItems(new ItemStack(Blocks.LANTERN))
                    .duration(100).EUt(1).save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, "soul_lantern", new ItemStack(Blocks.SOUL_LANTERN), "hRf",
                    "RFR", " R ",
                    'F', new ItemStack(Blocks.SOUL_TORCH),
                    'R', new MaterialEntry(ring, Iron));

            ASSEMBLER_RECIPES.recipeBuilder("soul_lantern")
                    .inputItems(new ItemStack(Blocks.SOUL_TORCH))
                    .inputItems(ring, Iron, 4)
                    .outputItems(new ItemStack(Blocks.SOUL_LANTERN))
                    .duration(100).EUt(1).save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, "stonecutter", new ItemStack(Blocks.STONECUTTER), "f d",
                    "SBS", "XXX",
                    'X', new ItemStack(Blocks.STONE_SLAB),
                    'S', new MaterialEntry(screw, Wood),
                    'B', new MaterialEntry(toolHeadBuzzSaw, Iron));

            ASSEMBLER_RECIPES.recipeBuilder("stonecutter")
                    .inputItems(toolHeadBuzzSaw, Iron)
                    .inputItems(new ItemStack(Blocks.STONE_SLAB))
                    .outputItems(new ItemStack(Blocks.STONECUTTER))
                    .duration(80).EUt(6).save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, "cartography_table", new ItemStack(Blocks.CARTOGRAPHY_TABLE),
                    "sPd",
                    "WWW", "SLS",
                    'P', new ItemStack(Items.PAPER),
                    'S', new MaterialEntry(screw, Iron),
                    'W', new MaterialEntry(plate, TreatedWood),
                    'L', ItemTags.LOGS);

            ASSEMBLER_RECIPES.recipeBuilder("cartography_table")
                    .inputItems(new ItemStack(Items.PAPER))
                    .inputItems(ItemTags.LOGS)
                    .outputItems(new ItemStack(Blocks.CARTOGRAPHY_TABLE))
                    .duration(80).EUt(6).save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, "fletching_table", new ItemStack(Blocks.FLETCHING_TABLE),
                    "sPd",
                    "WWW", "SLS",
                    'P', new ItemStack(Items.FEATHER),
                    'S', new MaterialEntry(screw, Iron),
                    'W', new MaterialEntry(plate, Wood),
                    'L', ItemTags.LOGS);

            ASSEMBLER_RECIPES.recipeBuilder("fletching_table")
                    .inputItems(new ItemStack(Items.FEATHER))
                    .inputItems(ItemTags.LOGS)
                    .outputItems(new ItemStack(Blocks.FLETCHING_TABLE))
                    .duration(80).EUt(6).save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, "smithing_table", new ItemStack(Blocks.SMITHING_TABLE), "h d",
                    "WWW", "SLS",
                    'S', new MaterialEntry(screw, WroughtIron),
                    'W', new MaterialEntry(plate, Steel),
                    'L', ItemTags.LOGS);

            ASSEMBLER_RECIPES.recipeBuilder("smithing_table")
                    .inputItems(screw, WroughtIron, 2)
                    .inputItems(ItemTags.LOGS)
                    .inputItems(plate, Steel, 3)
                    .outputItems(new ItemStack(Blocks.SMITHING_TABLE))
                    .duration(80).EUt(6).save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, "grindstone", new ItemStack(Blocks.GRINDSTONE), "hBd",
                    "IXI", "WfW",
                    'X', new ItemStack(Blocks.STONE_SLAB),
                    'B', new MaterialEntry(toolHeadBuzzSaw, Iron),
                    'I', new MaterialEntry(bolt, Iron),
                    'W', new MaterialEntry(rodLong, Wood));

            ASSEMBLER_RECIPES.recipeBuilder("grindstone")
                    .inputItems(new ItemStack(Blocks.STONE_SLAB))
                    .inputItems(toolHeadBuzzSaw, Iron)
                    .inputItems(rodLong, Wood, 2)
                    .outputItems(new ItemStack(Blocks.GRINDSTONE))
                    .duration(80).EUt(6).save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, "loom", new ItemStack(Blocks.LOOM), "SSS",
                    "PdP", "XYX",
                    'S', new ItemStack(Items.STRING),
                    'P', new MaterialEntry(plate, Wood),
                    'X', new MaterialEntry(screw, Wood),
                    'Y', ItemTags.PLANKS);

            ASSEMBLER_RECIPES.recipeBuilder("loom")
                    .inputItems(new ItemStack(Items.STRING, 3))
                    .inputItems(plate, Wood, 2)
                    .inputItems(ItemTags.PLANKS)
                    .outputItems(new ItemStack(Blocks.LOOM))
                    .duration(80).EUt(6).save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, "smoker", new ItemStack(Blocks.SMOKER), "wId",
                    "PFP", "XPX",
                    'I', new ItemStack(Blocks.IRON_BARS),
                    'P', new MaterialEntry(plate, TreatedWood),
                    'X', new MaterialEntry(bolt, Iron),
                    'F', new ItemStack(Blocks.FURNACE));

            ASSEMBLER_RECIPES.recipeBuilder("smoker")
                    .inputItems(new ItemStack(Blocks.IRON_BARS))
                    .inputItems(ItemTags.PLANKS, 3)
                    .inputItems(new ItemStack(Blocks.FURNACE))
                    .outputItems(new ItemStack(Blocks.SMOKER))
                    .duration(80).EUt(6).save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, "blast_furnace", new ItemStack(Blocks.BLAST_FURNACE), "wfd",
                    "PFP", "XPX",
                    'P', new MaterialEntry(plate, Iron),
                    'X', new MaterialEntry(screw, Iron),
                    'F', new ItemStack(Blocks.FURNACE));

            ASSEMBLER_RECIPES.recipeBuilder("blast_furnace")
                    .inputItems(plate, Iron, 3)
                    .inputItems(new ItemStack(Blocks.FURNACE))
                    .outputItems(new ItemStack(Blocks.BLAST_FURNACE))
                    .duration(80).EUt(6).save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, "composter", new ItemStack(Blocks.COMPOSTER), "PsP",
                    "P P", "XPX",
                    'P', ItemTags.PLANKS,
                    'X', new MaterialEntry(screw, Wood));

            ASSEMBLER_RECIPES.recipeBuilder("composter")
                    .inputItems(ItemTags.PLANKS, 4)
                    .circuitMeta(23)
                    .outputItems(new ItemStack(Blocks.COMPOSTER))
                    .duration(80).EUt(6).save(provider);

            ASSEMBLER_RECIPES.recipeBuilder("bell")
                    .inputItems(new ItemStack(Items.STICK))
                    .inputItems(rod, Stone, 2)
                    .inputItems(bolt, Iron, 2)
                    .inputItems(plate, Bronze, 4)
                    .outputItems(new ItemStack(Blocks.BELL))
                    .duration(200).EUt(16).save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, "candle", new ItemStack(Blocks.CANDLE), "r",
                    "S", "W",
                    'S', new ItemStack(Items.STRING),
                    'W', new ItemStack(Items.HONEYCOMB));

            ASSEMBLER_RECIPES.recipeBuilder("lodestone")
                    .inputItems(plateDense, SteelMagnetic)
                    .inputItems(screw, Iron, 4)
                    .inputItems(plate, Stone, 4)
                    .outputItems(new ItemStack(Blocks.LODESTONE))
                    .duration(200).EUt(16).save(provider);

            ASSEMBLER_RECIPES.recipeBuilder("music_disc_5")
                    .inputItems(new ItemStack(Items.DISC_FRAGMENT_5, 9))
                    .outputItems(new ItemStack(Items.MUSIC_DISC_5))
                    .duration(100).EUt(6).save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, "turtle_helmet", new ItemStack(Items.TURTLE_HELMET), "SSS",
                    "SrS",
                    'S', new ItemStack(Items.SCUTE));

            VanillaRecipeHelper.addShapedRecipe(provider, "scaffolding", new ItemStack(Blocks.SCAFFOLDING, 3), "BSB",
                    "BWB", "BrB",
                    'B', new ItemStack(Items.BAMBOO),
                    'S', new ItemStack(Items.STRING),
                    'W', new MaterialEntry(bolt, Wood));

            ASSEMBLER_RECIPES.recipeBuilder("scaffolding")
                    .inputItems(new ItemStack(Items.BAMBOO, 6))
                    .inputItems(new ItemStack(Items.STRING))
                    .outputItems(new ItemStack(Blocks.SCAFFOLDING, 4))
                    .duration(100).EUt(4).save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, "beehive", new ItemStack(Blocks.BEEHIVE, 1), "PsP",
                    "WXW", "PdP",
                    'P', ItemTags.PLANKS,
                    'W', new MaterialEntry(plate, Wood),
                    'X', new MaterialEntry(bolt, Wood));

            ASSEMBLER_RECIPES.recipeBuilder("beehive")
                    .inputItems(screw, Wood, 2)
                    .inputItems(ItemTags.PLANKS, 5)
                    .circuitMeta(2)
                    .outputItems(new ItemStack(Blocks.BEEHIVE))
                    .duration(100).EUt(4).save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, "lightning_rod", new ItemStack(Blocks.LIGHTNING_ROD), " B ",
                    "fRh", " R ",
                    'R', new MaterialEntry(rod, Copper),
                    'B', new MaterialEntry(plateDouble, Copper));

            ASSEMBLER_RECIPES.recipeBuilder("lightning_rod")
                    .inputItems(rod, Copper, 2)
                    .inputItems(plateDouble, Copper)
                    .outputItems(new ItemStack(Blocks.LIGHTNING_ROD))
                    .duration(100).EUt(4).save(provider);

            ASSEMBLER_RECIPES.recipeBuilder("chiseled_bookshelf")
                    .inputItems(ItemTags.PLANKS, 6)
                    .circuitMeta(4)
                    .outputItems(new ItemStack(Blocks.CHISELED_BOOKSHELF))
                    .duration(100).EUt(4).save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, "lectern", new ItemStack(Blocks.LECTERN), "SSS",
                    "WBW", "dSs",
                    'S', ItemTags.WOODEN_SLABS,
                    'W', new MaterialEntry(screw, Wood),
                    'B', new ItemStack(Blocks.BOOKSHELF));

            ASSEMBLER_RECIPES.recipeBuilder("lectern")
                    .inputItems(ItemTags.PLANKS, 2)
                    .inputItems(screw, Wood, 2)
                    .inputItems(new ItemStack(Blocks.BOOKSHELF))
                    .outputItems(new ItemStack(Blocks.LECTERN))
                    .duration(100).EUt(4).save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, "brush", new ItemStack(Items.BRUSH), " F ",
                    "fRr", " S ",
                    'S', new MaterialEntry(rod, Wood),
                    'R', new MaterialEntry(ring, Copper),
                    'F', new ItemStack(Items.FEATHER));

            ASSEMBLER_RECIPES.recipeBuilder("brush")
                    .inputItems(rodLong, Wood)
                    .inputItems(ring, Copper)
                    .inputItems(new ItemStack(Items.FEATHER))
                    .outputItems(new ItemStack(Items.BRUSH))
                    .duration(100).EUt(4).save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, "spyglass", new ItemStack(Items.SPYGLASS), "hGd",
                    "LRS", "fP ",
                    'L', new ItemStack(Items.LEATHER),
                    'G', new MaterialEntry(lens, Glass),
                    'R', new MaterialEntry(ring, Gold),
                    'S', new MaterialEntry(screw, Iron),
                    'P', new MaterialEntry(plate, Copper));

            ASSEMBLER_RECIPES.recipeBuilder("spyglass")
                    .inputItems(lens, Glass)
                    .inputItems(ring, Gold)
                    .inputItems(plate, Copper)
                    .inputItems(new ItemStack(Items.LEATHER))
                    .outputItems(new ItemStack(Items.SPYGLASS))
                    .duration(100).EUt(4).save(provider);

            ASSEMBLER_RECIPES.recipeBuilder("recovery_compass")
                    .inputItems(new ItemStack(Items.COMPASS))
                    .inputItems(plate, EchoShard, 8)
                    .inputItems(rod, EchoShard)
                    .outputItems(new ItemStack(Items.RECOVERY_COMPASS))
                    .duration(400).EUt(30).save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, "respawn_anchor", new ItemStack(Items.RESPAWN_ANCHOR), "CCC",
                    "GGG", "CCC",
                    'C', new ItemStack(Items.CRYING_OBSIDIAN),
                    'G', new MaterialEntry(plate, Glowstone));

            ASSEMBLER_RECIPES.recipeBuilder("respawn_anchor")
                    .inputItems(new ItemStack(Items.CRYING_OBSIDIAN, 6))
                    .inputItems(plate, Glowstone, 3)
                    .outputItems(new ItemStack(Items.RESPAWN_ANCHOR))
                    .duration(200).EUt(120).save(provider);

            for (DyeColor color : DyeColor.values()) {
                addBedRecipe(provider, color);
                addCarpetRecipe(provider, color);
            }

        } else {
            ASSEMBLER_RECIPES.recipeBuilder("crafting_table").duration(80).EUt(6).circuitMeta(4)
                    .inputItems(ItemTags.PLANKS, 4).outputItems(new ItemStack(Blocks.CRAFTING_TABLE)).save(provider);
            ASSEMBLER_RECIPES.recipeBuilder("furnace").circuitMeta(8).inputItems(ItemTags.STONE_CRAFTING_MATERIALS, 8)
                    .outputItems(new ItemStack(Blocks.FURNACE)).duration(100).EUt(VA[ULV])
                    .addMaterialInfo(true).save(provider);
            ASSEMBLER_RECIPES.recipeBuilder("enchanting_table").inputItems(new ItemStack(Blocks.OBSIDIAN, 4))
                    .inputItems(gem, Diamond, 2).inputItems(new ItemStack(Items.BOOK))
                    .outputItems(new ItemStack(Blocks.ENCHANTING_TABLE)).duration(100).EUt(VA[ULV])
                    .addMaterialInfo(true).save(provider);
            ASSEMBLER_RECIPES.recipeBuilder("dispenser").duration(100).EUt(VA[LV]).circuitMeta(1)
                    .inputItems(ItemTags.STONE_CRAFTING_MATERIALS, 7).inputItems(new ItemStack(Items.BOW))
                    .inputItems(dust, Redstone).outputItems(new ItemStack(Blocks.DISPENSER)).save(provider);
            ASSEMBLER_RECIPES.recipeBuilder("dropper").duration(100).EUt(VA[LV]).circuitMeta(2)
                    .inputItems(ItemTags.STONE_CRAFTING_MATERIALS, 7).inputItems(dust, Redstone)
                    .outputItems(new ItemStack(Blocks.DROPPER)).save(provider);
            ASSEMBLER_RECIPES.recipeBuilder("observer_nether_quartz").duration(100).EUt(VA[LV])
                    .inputItems(ItemTags.STONE_CRAFTING_MATERIALS, 6).inputItems(dust, Redstone, 2)
                    .inputItems(plate, NetherQuartz).outputItems(new ItemStack(Blocks.OBSERVER)).save(provider);
            ASSEMBLER_RECIPES.recipeBuilder("observer_certus_quartz").duration(100).EUt(VA[LV])
                    .inputItems(ItemTags.STONE_CRAFTING_MATERIALS, 6).inputItems(dust, Redstone, 2)
                    .inputItems(plate, CertusQuartz).outputItems(new ItemStack(Blocks.OBSERVER)).save(provider);
            ASSEMBLER_RECIPES.recipeBuilder("observer_quartzite").duration(100).EUt(VA[LV])
                    .inputItems(ItemTags.STONE_CRAFTING_MATERIALS, 6).inputItems(dust, Redstone, 2)
                    .inputItems(plate, Quartzite).outputItems(new ItemStack(Blocks.OBSERVER)).save(provider);
            ASSEMBLER_RECIPES.recipeBuilder("lantern").duration(100).EUt(VA[LV])
                    .inputItems(Items.TORCH).inputFluids(Iron.getFluid(GTValues.L / 9 * 8))
                    .outputItems(new ItemStack(Blocks.LANTERN)).save(provider);
            ASSEMBLER_RECIPES.recipeBuilder("tinted_glass").duration(100).EUt(VA[LV])
                    .inputItems(Items.AMETHYST_SHARD, 2).inputItems(Items.GLASS)
                    .outputItems(new ItemStack(Blocks.TINTED_GLASS)).save(provider);
            ASSEMBLER_RECIPES.recipeBuilder("stonecutter").duration(100).EUt(VA[LV])
                    .inputItems(Items.STONE, 3).inputFluids(Iron.getFluid(GTValues.L))
                    .outputItems(new ItemStack(Blocks.STONECUTTER)).save(provider);
            ASSEMBLER_RECIPES.recipeBuilder("cartography_table").duration(100).EUt(VA[LV])
                    .inputItems(ItemTags.PLANKS, 4).inputItems(Items.PAPER, 2)
                    .outputItems(new ItemStack(Blocks.CARTOGRAPHY_TABLE)).circuitMeta(7).save(provider);
            ASSEMBLER_RECIPES.recipeBuilder("fletching_table").duration(100).EUt(VA[LV])
                    .inputItems(ItemTags.PLANKS, 4).inputItems(Items.FLINT, 2)
                    .outputItems(new ItemStack(Blocks.FLETCHING_TABLE)).circuitMeta(7).save(provider);
            ASSEMBLER_RECIPES.recipeBuilder("smithing_table").duration(100).EUt(VA[LV])
                    .inputItems(ItemTags.PLANKS, 4).inputFluids(Iron.getFluid(GTValues.L * 2))
                    .outputItems(new ItemStack(Blocks.SMITHING_TABLE)).circuitMeta(7).save(provider);
            ASSEMBLER_RECIPES.recipeBuilder("grindstone").duration(100).EUt(VA[LV])
                    .inputItems(Tags.Items.RODS_WOODEN, 2).inputItems(Items.STONE_SLAB).inputItems(ItemTags.PLANKS, 2)
                    .outputItems(new ItemStack(Blocks.GRINDSTONE)).circuitMeta(7).save(provider);
            ASSEMBLER_RECIPES.recipeBuilder("loom").duration(100).EUt(VA[LV])
                    .inputItems(ItemTags.PLANKS, 2).inputItems(Items.STRING, 2).outputItems(new ItemStack(Blocks.LOOM))
                    .circuitMeta(7).save(provider);
            ASSEMBLER_RECIPES.recipeBuilder("smoker").duration(100).EUt(VA[LV])
                    .inputItems(ItemTags.LOGS, 4).inputItems(Items.FURNACE).outputItems(new ItemStack(Blocks.SMOKER))
                    .circuitMeta(7).save(provider);
            ASSEMBLER_RECIPES.recipeBuilder("blast_furnace").duration(100).EUt(VA[LV])
                    .inputItems(Items.SMOOTH_STONE, 3).inputItems(Items.FURNACE)
                    .inputFluids(Iron.getFluid(GTValues.L * 5)).outputItems(new ItemStack(Blocks.BLAST_FURNACE))
                    .save(provider);
            ASSEMBLER_RECIPES.recipeBuilder("composter").duration(100).EUt(VA[LV])
                    .inputItems(ItemTags.WOODEN_SLABS, 7).outputItems(new ItemStack(Blocks.COMPOSTER)).circuitMeta(7)
                    .save(provider);
            ASSEMBLER_RECIPES.recipeBuilder("lodestone").duration(100).EUt(VA[LV])
                    .inputItems(Items.CHISELED_STONE_BRICKS, 8).inputItems(Items.NETHERITE_INGOT)
                    .outputItems(new ItemStack(Blocks.LODESTONE)).save(provider);
            ASSEMBLER_RECIPES.recipeBuilder("scaffolding").duration(100).EUt(VA[LV])
                    .inputItems(Items.BAMBOO, 6).inputItems(Items.STRING)
                    .outputItems(new ItemStack(Blocks.SCAFFOLDING, 6)).circuitMeta(7).save(provider);
            ASSEMBLER_RECIPES.recipeBuilder("beehive").duration(100).EUt(VA[LV])
                    .inputItems(ItemTags.PLANKS, 6).inputItems(Items.HONEYCOMB, 3)
                    .outputItems(new ItemStack(Blocks.BEEHIVE)).circuitMeta(7).save(provider);
            ASSEMBLER_RECIPES.recipeBuilder("chiseled_bookshelf").duration(100).EUt(VA[LV])
                    .inputItems(ItemTags.PLANKS, 6).inputItems(ItemTags.WOODEN_SLABS, 3)
                    .outputItems(new ItemStack(Blocks.CHISELED_BOOKSHELF)).circuitMeta(9).save(provider);
            ASSEMBLER_RECIPES.recipeBuilder("lectern").duration(100).EUt(VA[LV])
                    .inputItems(ItemTags.WOODEN_SLABS, 4).inputItems(Items.BOOKSHELF)
                    .outputItems(new ItemStack(Blocks.LECTERN)).circuitMeta(10).save(provider);
            ASSEMBLER_RECIPES.recipeBuilder("respawn_anchor").duration(100).EUt(VA[LV])
                    .inputItems(Items.CRYING_OBSIDIAN, 6).inputItems(Items.GLOWSTONE, 3)
                    .outputItems(new ItemStack(Blocks.RESPAWN_ANCHOR)).save(provider);
        }
    }

    private static void vanillaBlockRecipes(Consumer<FinishedRecipe> provider) {
        COMPRESSOR_RECIPES.recipeBuilder("mud_bricks")
                .inputItems(Items.PACKED_MUD, 1)
                .outputItems(Items.MUD_BRICKS, 1)
                .duration(200)
                .EUt(4)
                .save(provider);
    }

    private static void addBedRecipe(Consumer<FinishedRecipe> provider, DyeColor color) {
        String colorName = color.getName();
        VanillaRecipeHelper.addShapedRecipe(provider, colorName + "_bed",
                new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(colorName + "_bed"))), "WWW", "PPP",
                "FrF",
                'W', BuiltInRegistries.ITEM.get(new ResourceLocation(colorName + "_carpet")),
                'P', ItemTags.PLANKS,
                'F', ItemTags.WOODEN_FENCES);
    }

    private static void addCarpetRecipe(Consumer<FinishedRecipe> provider, DyeColor color) {
        String colorName = color.getName();
        VanillaRecipeHelper.addShapedRecipe(provider, colorName + "_carpet",
                new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(colorName + "_carpet"))), "WW",
                'W', BuiltInRegistries.ITEM.get(new ResourceLocation(colorName + "_wool")));
    }

    private static void hardGlassRecipes(Consumer<FinishedRecipe> provider) {
        VanillaRecipeHelper.addShapedRecipe(provider, "glass_pane", new ItemStack(Blocks.GLASS_PANE, 2), "sG", 'G',
                new ItemStack(Blocks.GLASS));

        for (DyeColor color : DyeColor.values()) {
            String dyeName = color.getName();
            VanillaRecipeHelper.addShapedRecipe(provider, dyeName + "_glass_pane",
                    new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(dyeName + "_stained_glass_pane")), 2),
                    "sG",
                    'G', BuiltInRegistries.ITEM.get(new ResourceLocation(dyeName + "_stained_glass")));
        }

        ALLOY_SMELTER_RECIPES.recipeBuilder("tinted_glass")
                .inputItems(new ItemStack(Blocks.GLASS))
                .inputItems(new ItemStack(Items.AMETHYST_SHARD, 4))
                .outputItems(new ItemStack(Blocks.TINTED_GLASS, 2))
                .duration(80).EUt(6).save(provider);
    }

    private static void nerfPaperCrafting(Consumer<FinishedRecipe> provider) {
        VanillaRecipeHelper.addShapedRecipe(provider, "paper_dust",
                ChemicalHelper.get(TagPrefix.dust, GTMaterials.Paper, 2), "SSS", " m ", 'S',
                new ItemStack(Items.SUGAR_CANE));
        VanillaRecipeHelper.addShapedRecipe(provider, "sugar", ChemicalHelper.get(TagPrefix.dust, GTMaterials.Sugar, 1),
                "Sm ", 'S', new ItemStack(Items.SUGAR_CANE));
        VanillaRecipeHelper.addShapedFluidContainerRecipe(provider, "paper", new ItemStack(Items.PAPER, 2),
                " r ", "SSS", " B ",
                'S', new MaterialEntry(TagPrefix.dust, GTMaterials.Paper),
                'B', new FluidContainerIngredient(Water.getFluidTag(), 1000));
    }

    private static void hardAdvancedIronRecipes(Consumer<FinishedRecipe> provider) {
        VanillaRecipeHelper.addShapedRecipe(provider, "iron_door", new ItemStack(Items.IRON_DOOR), "PTh", "PRS", "PPd",
                'P', new MaterialEntry(TagPrefix.plate, GTMaterials.Iron),
                'T', new ItemStack(Blocks.IRON_BARS),
                'R', new MaterialEntry(TagPrefix.ring, GTMaterials.Steel),
                'S', new MaterialEntry(TagPrefix.screw, GTMaterials.Steel));

        ASSEMBLER_RECIPES.recipeBuilder("iron_door")
                .inputItems(TagPrefix.plate, GTMaterials.Iron, 4)
                .inputItems(new ItemStack(Blocks.IRON_BARS))
                .inputFluids(GTMaterials.Steel.getFluid(L / 9))
                .outputItems(new ItemStack(Items.IRON_DOOR))
                .duration(400).EUt(VA[ULV])
                .addMaterialInfo(true).save(provider);

        VanillaRecipeHelper.addShapedRecipe(provider, "anvil", new ItemStack(Blocks.ANVIL), "BBB", "SBS", "PBP",
                'B', new MaterialEntry(TagPrefix.block, GTMaterials.Iron),
                'S', new MaterialEntry(TagPrefix.screw, GTMaterials.Iron),
                'P', new MaterialEntry(TagPrefix.plate, GTMaterials.Iron));

        VanillaRecipeHelper.addShapedRecipe(provider, "iron_trapdoor", new ItemStack(Blocks.IRON_TRAPDOOR), "SPS",
                "PTP", "sPd",
                'S', new MaterialEntry(TagPrefix.screw, GTMaterials.Iron),
                'P', new MaterialEntry(TagPrefix.plate, GTMaterials.Iron),
                'T', ItemTags.WOODEN_TRAPDOORS);

        VanillaRecipeHelper.addShapedRecipe(provider, "minecart_iron", new ItemStack(Items.MINECART), " h ", "PwP",
                "WPW",
                'W', GTItems.IRON_MINECART_WHEELS.asStack(),
                'P', new MaterialEntry(plate, Iron));
        VanillaRecipeHelper.addShapedRecipe(provider, "minecart_steel", new ItemStack(Items.MINECART), " h ", "PwP",
                "WPW",
                'W', GTItems.STEEL_MINECART_WHEELS.asStack(),
                'P', new MaterialEntry(plate, Steel));
    }

    private static void flintAndSteelRequireSteel(Consumer<FinishedRecipe> provider) {
        VanillaRecipeHelper.addShapedRecipe(provider, true, "flint_and_steel", new ItemStack(Items.FLINT_AND_STEEL),
                "G", "F",
                "S",
                'G', new MaterialEntry(TagPrefix.gearSmall, GTMaterials.Steel),
                'F', new ItemStack(Items.FLINT),
                'S', new MaterialEntry(TagPrefix.springSmall, GTMaterials.Steel));
    }

    private static void createShovelRecipe(Consumer<FinishedRecipe> provider, String regName, ItemStack output,
                                           Material material) {
        VanillaRecipeHelper.addShapedRecipe(provider, true, regName, output, "hPf", " S ", " S ",
                'P', new MaterialEntry(TagPrefix.plate, material),
                'S', new MaterialEntry(TagPrefix.rod, GTMaterials.Wood));
    }

    private static void createPickaxeRecipe(Consumer<FinishedRecipe> provider, String regName, ItemStack output,
                                            Material material) {
        VanillaRecipeHelper.addShapedRecipe(provider, true, regName, output, "PII", "hSf", " S ",
                'P', new MaterialEntry(TagPrefix.plate, material),
                'I',
                new MaterialEntry(material.hasProperty(PropertyKey.GEM) ? TagPrefix.gem : TagPrefix.ingot, material),
                'S', new MaterialEntry(TagPrefix.rod, GTMaterials.Wood));
    }

    private static void createAxeRecipe(Consumer<FinishedRecipe> provider, String regName, ItemStack output,
                                        Material material) {
        VanillaRecipeHelper.addShapedRecipe(provider, true, regName, output, "PIf", "PS ", "hS ",
                'P', new MaterialEntry(TagPrefix.plate, material),
                'I',
                new MaterialEntry(material.hasProperty(PropertyKey.GEM) ? TagPrefix.gem : TagPrefix.ingot, material),
                'S', new MaterialEntry(TagPrefix.rod, GTMaterials.Wood));
    }

    private static void createSwordRecipe(Consumer<FinishedRecipe> provider, String regName, ItemStack output,
                                          Material material) {
        VanillaRecipeHelper.addShapedRecipe(provider, true, regName, output, " P ", "hPf", " S ",
                'P', new MaterialEntry(TagPrefix.plate, material),
                'S', new MaterialEntry(TagPrefix.rod, GTMaterials.Wood));
    }

    private static void createHoeRecipe(Consumer<FinishedRecipe> provider, String regName, ItemStack output,
                                        Material material) {
        VanillaRecipeHelper.addShapedRecipe(provider, true, regName, output, "PIf", "hS ", " S ",
                'P', new MaterialEntry(TagPrefix.plate, material),
                'I',
                new MaterialEntry(material.hasProperty(PropertyKey.GEM) ? TagPrefix.gem : TagPrefix.ingot, material),
                'S', new MaterialEntry(TagPrefix.rod, GTMaterials.Wood));
    }

    private static void createHelmetRecipe(Consumer<FinishedRecipe> provider, String regName, ItemStack output,
                                           Material material) {
        VanillaRecipeHelper.addShapedRecipe(provider, true, regName, output, "PPP", "PhP",
                'P', new MaterialEntry(TagPrefix.plate, material));
    }

    private static void createChestplateRecipe(Consumer<FinishedRecipe> provider, String regName, ItemStack output,
                                               Material material) {
        VanillaRecipeHelper.addShapedRecipe(provider, true, regName, output, "PhP", "PPP", "PPP",
                'P', new MaterialEntry(TagPrefix.plate, material));
    }

    private static void createLeggingsRecipe(Consumer<FinishedRecipe> provider, String regName, ItemStack output,
                                             Material material) {
        VanillaRecipeHelper.addShapedRecipe(provider, true, regName, output, "PPP", "PhP", "P P",
                'P', new MaterialEntry(TagPrefix.plate, material));
    }

    private static void createBootsRecipe(Consumer<FinishedRecipe> provider, String regName, ItemStack output,
                                          Material material) {
        VanillaRecipeHelper.addShapedRecipe(provider, true, regName, output, "P P", "PhP",
                'P', new MaterialEntry(TagPrefix.plate, material));
    }
}
