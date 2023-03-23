package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterials.Tier;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.CommonTags;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTItems.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

public class CraftingRecipeLoader {

    public static void init(Consumer<FinishedRecipe> provider) {
        // todo facades
        //registerFacadeRecipe(provider, Iron, 4);

        // todo wood pipes
        //VanillaRecipeHelper.addShapedRecipe(provider, "small_wooden_pipe", ChemicalHelper.get(pipeSmallFluid, Wood), "sWr", 'W', CommonTags.TAG_PLANKS);
        //VanillaRecipeHelper.addShapedRecipe(provider, "normal_wooden_pipe", ChemicalHelper.get(pipeNormalFluid, Wood), "WWW", "s r", 'W', CommonTags.TAG_PLANKS);
        //VanillaRecipeHelper.addShapedRecipe(provider, "large_wooden_pipe", ChemicalHelper.get(pipeLargeFluid, Wood), "WWW", "s r", "WWW", 'W', CommonTags.TAG_PLANKS);

        // TODO Treated wood
        //VanillaRecipeHelper.addShapedRecipe(provider, "small_treated_wooden_pipe", ChemicalHelper.get(pipeSmallFluid, TreatedWood), "sWr", 'W', new UnificationEntry(plank, TreatedWood));
        //VanillaRecipeHelper.addShapedRecipe(provider, "normal_treated_wooden_pipe", ChemicalHelper.get(pipeNormalFluid, TreatedWood), "WWW", "s r", 'W', new UnificationEntry(plank, TreatedWood));
        //VanillaRecipeHelper.addShapedRecipe(provider, "large_treated_wooden_pipe", ChemicalHelper.get(pipeLargeFluid, TreatedWood), "WWW", "s r", "WWW", 'W', new UnificationEntry(plank, TreatedWood));

        VanillaRecipeHelper.addShapelessRecipe(provider, "integrated_circuit", INTEGRATED_CIRCUIT.asStack(), new UnificationEntry(circuit, Tier.LV));

        VanillaRecipeHelper.addShapedRecipe(provider, "item_filter", ITEM_FILTER.asStack(), "XXX", "XYX", "XXX", 'X', new UnificationEntry(foil, Zinc), 'Y', new UnificationEntry(plate, Steel));
        VanillaRecipeHelper.addShapedRecipe(provider, "fluid_filter_lapis", FLUID_FILTER.asStack(), "XXX", "XYX", "XXX", 'X', new UnificationEntry(foil, Zinc), 'Y', new UnificationEntry(plate, Lapis));
        VanillaRecipeHelper.addShapedRecipe(provider, "fluid_filter_lazurite", FLUID_FILTER.asStack(), "XXX", "XYX", "XXX", 'X', new UnificationEntry(foil, Zinc), 'Y', new UnificationEntry(plate, Lazurite));
        VanillaRecipeHelper.addShapedRecipe(provider, "fluid_filter_sodalite", FLUID_FILTER.asStack(), "XXX", "XYX", "XXX", 'X', new UnificationEntry(foil, Zinc), 'Y', new UnificationEntry(plate, Sodalite));

        VanillaRecipeHelper.addShapedRecipe(provider, "ore_dictionary_filter_olivine", ORE_DICTIONARY_FILTER.asStack(), "XXX", "XYX", "XXX", 'X', new UnificationEntry(foil, Zinc), 'Y', new UnificationEntry(plate, Olivine));
        VanillaRecipeHelper.addShapedRecipe(provider, "ore_dictionary_filter_emerald", ORE_DICTIONARY_FILTER.asStack(), "XXX", "XYX", "XXX", 'X', new UnificationEntry(foil, Zinc), 'Y', new UnificationEntry(plate, Emerald));

        // TODO smart filter
        //VanillaRecipeHelper.addShapedRecipe(provider, "smart_item_filter_olivine", SMART_FILTER.asStack(), "XEX", "XCX", "XEX", 'X', new UnificationEntry(foil, Zinc), 'C', new UnificationEntry(circuit, Tier.LV), 'E', new UnificationEntry(plate, Olivine));
        //VanillaRecipeHelper.addShapedRecipe(provider, "smart_item_filter_emerald", SMART_FILTER.asStack(), "XEX", "XCX", "XEX", 'X', new UnificationEntry(foil, Zinc), 'C', new UnificationEntry(circuit, Tier.LV), 'E', new UnificationEntry(plate, Emerald));

        VanillaRecipeHelper.addShapedRecipe(provider, "plank_to_wooden_shape", WOODEN_FORM_EMPTY.asStack(), "   ", " X ", "s  ", 'X', CommonTags.TAG_PLANKS);
        VanillaRecipeHelper.addShapedRecipe(provider, "wooden_shape_brick", WOODEN_FORM_BRICK.asStack(), "k ", " X", 'X', WOODEN_FORM_EMPTY.asStack());

        if (ConfigHolder.recipes.harderBrickRecipes) {
            VanillaRecipeHelper.addShapelessRecipe(provider, "compressed_clay", COMPRESSED_CLAY.asStack(), WOODEN_FORM_BRICK.asStack(), new ItemStack(Items.CLAY_BALL));
            VanillaRecipeHelper.addSmeltingRecipe(provider, "brick_from_compressed_clay", COMPRESSED_CLAY.asStack(), new ItemStack(Items.BRICK), 0.3f);
        }

        VanillaRecipeHelper.addShapedRecipe(provider, "compressed_coke_clay", COMPRESSED_COKE_CLAY.asStack(3), "XXX", "SYS", "SSS", 'Y', WOODEN_FORM_BRICK.asStack(), 'X', new ItemStack(Items.CLAY_BALL), 'S', CommonTags.TAG_SAND);
        VanillaRecipeHelper.addShapelessRecipe(provider, "fireclay_dust", ChemicalHelper.get(dust, Fireclay, 2), new UnificationEntry(dust, Brick), new UnificationEntry(dust, Clay));
        VanillaRecipeHelper.addSmeltingRecipe(provider, "coke_oven_brick", COMPRESSED_COKE_CLAY.asStack(), COKE_OVEN_BRICK.asStack(), 0.3f);
        VanillaRecipeHelper.addSmeltingRecipe(provider, "fireclay_brick", COMPRESSED_FIRECLAY.asStack(), FIRECLAY_BRICK.asStack(), 0.3f);

        VanillaRecipeHelper.addSmeltingRecipe(provider, "wrought_iron_nugget", ChemicalHelper.getTag(nugget, Iron), ChemicalHelper.get(nugget, WroughtIron));

        // TODO clipboard
        //VanillaRecipeHelper.addShapedRecipe(provider, "clipboard", CLIPBOARD.asStack(), " Sd", "BWR", "PPP", 'P', Items.PAPER, 'R', new UnificationEntry(springSmall, Iron), 'B', new UnificationEntry(bolt, Iron), 'S', new UnificationEntry(screw, Iron), 'W', new UnificationEntry(plate, Wood));

        // TODO rubber wood, treated wood
        //VanillaRecipeHelper.addShapelessRecipe("rubber_wood_planks", MetaBlocks.PLANKS.getItemVariant(BlockGregPlanks.BlockType.RUBBER_PLANK, 4), new ItemStack(MetaBlocks.RUBBER_LOG));
        //VanillaRecipeHelper.addShapedRecipe(provider, "treated_wood_planks", MetaBlocks.PLANKS.getItemVariant(BlockGregPlanks.BlockType.TREATED_PLANK, 8), "PPP", "PBP", "PPP", 'P', "plankWood", 'B', FluidUtil.getFilledBucket(Creosote.getFluid(1000)));
        //VanillaRecipeHelper.addShapedRecipe(provider, "treated_wood_stick_saw", ChemicalHelper.get(stick, TreatedWood, ConfigHolder.recipes.nerfWoodCrafting ? 1 : 2), "s", "L", 'L', MetaBlocks.PLANKS.getItemVariant(BlockGregPlanks.BlockType.TREATED_PLANK));
        //if (!ConfigHolder.recipes.nerfWoodCrafting)
        //    VanillaRecipeHelper.addShapedRecipe(provider, "treated_wood_stick", ChemicalHelper.get(stick, TreatedWood, 4), "L", "L", 'L', MetaBlocks.PLANKS.getItemVariant(BlockGregPlanks.BlockType.TREATED_PLANK));

        VanillaRecipeHelper.addShapedRecipe(provider, "rubber_ring", ChemicalHelper.get(ring, Rubber), "k", "X", 'X', new UnificationEntry(plate, Rubber));
        VanillaRecipeHelper.addShapedRecipe(provider, "silicone_rubber_ring", ChemicalHelper.get(ring, SiliconeRubber), "k", "P", 'P', ChemicalHelper.get(plate, SiliconeRubber));
        VanillaRecipeHelper.addShapedRecipe(provider, "styrene_rubber_ring", ChemicalHelper.get(ring, StyreneButadieneRubber), "k", "P", 'P', ChemicalHelper.get(plate, StyreneButadieneRubber));

        VanillaRecipeHelper.addShapelessRecipe(provider, "iron_magnetic_stick", ChemicalHelper.get(stick, IronMagnetic), new UnificationEntry(stick, Iron), new UnificationEntry(dust, Redstone), new UnificationEntry(dust, Redstone), new UnificationEntry(dust, Redstone), new UnificationEntry(dust, Redstone));

        VanillaRecipeHelper.addShapedRecipe(provider, "component_grinder_diamond", COMPONENT_GRINDER_DIAMOND.asStack(), "XSX", "SDS", "XSX", 'X', new UnificationEntry(dust, Diamond), 'S', new UnificationEntry(plateDouble, Steel), 'D', new UnificationEntry(gem, Diamond));
        VanillaRecipeHelper.addShapedRecipe(provider, "component_grinder_tungsten", COMPONENT_GRINDER_TUNGSTEN.asStack(), "WSW", "SDS", "WSW", 'W', new UnificationEntry(plate, Tungsten), 'S', new UnificationEntry(plateDouble, VanadiumSteel), 'D', new UnificationEntry(gem, Diamond));

        // todo nanosaber
        //VanillaRecipeHelper.addShapedRecipe(provider, "nano_saber", NANO_SABER.asStack(), "PIC", "PIC", "XEX", 'P', new UnificationEntry(plate, Platinum), 'I', new UnificationEntry(plate, Ruridit), 'C', CARBON_FIBER_PLATE.asStack(), 'X', new UnificationEntry(circuit, Tier.EV), 'E', ENERGIUM_CRYSTAL.asStack());

        VanillaRecipeHelper.addShapedRecipe(provider, "solar_panel_basic", COVER_SOLAR_PANEL.asStack(), "WGW", "CPC", 'W', SILICON_WAFER.asStack(), 'G', new ItemStack(Blocks.GLASS_PANE), 'C', new UnificationEntry(circuit, Tier.LV), 'P', CARBON_FIBER_PLATE.asStack());
        // TODO glass tiers
        //VanillaRecipeHelper.addShapedRecipe(provider, "solar_panel_ulv", COVER_SOLAR_PANEL_ULV.asStack(), "WGW", "CAC", "P P", 'W', GLOWSTONE_WAFER.asStack(), 'G', "paneGlass", 'C', new UnificationEntry(circuit, Tier.HV), 'P', ChemicalHelper.get(plate, GalliumArsenide), 'A', ChemicalHelper.get(wireGtQuadruple, Graphene));
        //VanillaRecipeHelper.addShapedRecipe(provider, "solar_panel_lv", COVER_SOLAR_PANEL_LV.asStack(), "WGW", "CAC", "P P", 'W', NAQUADAH_WAFER.asStack(), 'G', MetaBlocks.TRANSPARENT_CASING.getItemVariant(
        //        BlockGlassCasing.CasingType.TEMPERED_GLASS), 'C', new UnificationEntry(circuit, Tier.LuV), 'P', ChemicalHelper.get(plate, IndiumGalliumPhosphide), 'A', ChemicalHelper.get(wireGtHex, Graphene));

        VanillaRecipeHelper.addShapedRecipe(provider, "universal_fluid_cell", FLUID_CELL_UNIVERSAL.asStack(), "C ", "  ", 'C', FLUID_CELL);
        VanillaRecipeHelper.addShapedRecipe(provider, "universal_fluid_cell_revert", FLUID_CELL.asStack(), "C ", "  ", 'C', FLUID_CELL_UNIVERSAL);

        // todo glass tiers
        //VanillaRecipeHelper.addShapedRecipe(provider, "blacklight", BLACKLIGHT.asStack(), "SPS", "GRG", "CPK", 'S', new UnificationEntry(screw, TungstenCarbide), 'P', new UnificationEntry(plate, TungstenCarbide), 'G', MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockGlassCasing.CasingType.LAMINATED_GLASS), 'R', new UnificationEntry(spring, Europium), 'C', new UnificationEntry(circuit, Tier.IV), 'K', new UnificationEntry(cableGtSingle, Platinum));

        // TODO cleanroom
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "filter_casing", MetaBlocks.CLEANROOM_CASING.getItemVariant(BlockCleanroomCasing.CasingType.FILTER_CASING, 2), "BBB", "III", "MFR", 'B', new ItemStack(Blocks.IRON_BARS), 'I', ITEM_FILTER.asStack(), 'M', ELECTRIC_MOTOR_MV.asStack(), 'F', new UnificationEntry(frameGt, Steel), 'R', new UnificationEntry(rotor, Steel));
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "filter_casing_sterile", MetaBlocks.CLEANROOM_CASING.getItemVariant(BlockCleanroomCasing.CasingType.FILTER_CASING_STERILE, 2), "BEB", "ISI", "MFR", 'B', new UnificationEntry(pipeLargeFluid, Polybenzimidazole), 'E', EMITTER_ZPM.asStack(), 'I', ITEM_FILTER.asStack(), 'S', BLACKLIGHT.asStack(), 'M', ELECTRIC_MOTOR_ZPM.asStack(), 'F', new UnificationEntry(frameGt, Tritanium), 'R', new UnificationEntry(rotor, NaquadahAlloy));

        ///////////////////////////////////////////////////
        //               Shapes and Molds                //
        ///////////////////////////////////////////////////
        VanillaRecipeHelper.addShapedRecipe(provider, "shape_empty", SHAPE_EMPTY.asStack(), "hf", "PP", "PP", 'P', new UnificationEntry(plate, Steel));

        VanillaRecipeHelper.addShapedRecipe(provider, "shape_extruder_bottle", SHAPE_EXTRUDER_BOTTLE.asStack(), "  x", " S ", "   ", 'S', SHAPE_EXTRUDER_RING.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, "shape_extruder_gear", SHAPE_EXTRUDER_GEAR.asStack(), "x  ", " S ", "   ", 'S', SHAPE_EXTRUDER_RING.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, "shape_extruder_block", SHAPE_EXTRUDER_BLOCK.asStack(), "x  ", " S ", "   ", 'S', SHAPE_EXTRUDER_INGOT.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, "shape_extruder_pipe_huge", SHAPE_EXTRUDER_PIPE_HUGE.asStack(), "   ", " S ", "  x", 'S', SHAPE_EXTRUDER_BOLT.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, "shape_extruder_pipe_large", SHAPE_EXTRUDER_PIPE_LARGE.asStack(), "   ", " Sx", "   ", 'S', SHAPE_EXTRUDER_BOLT.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, "shape_extruder_pipe_normal", SHAPE_EXTRUDER_PIPE_NORMAL.asStack(), "  x", " S ", "   ", 'S', SHAPE_EXTRUDER_BOLT.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, "shape_extruder_pipe_small", SHAPE_EXTRUDER_PIPE_SMALL.asStack(), " x ", " S ", "   ", 'S', SHAPE_EXTRUDER_BOLT.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, "shape_extruder_pipe_tiny", SHAPE_EXTRUDER_PIPE_TINY.asStack(), "x  ", " S ", "   ", 'S', SHAPE_EXTRUDER_BOLT.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, "shape_extruder_wire", SHAPE_EXTRUDER_WIRE.asStack(), " x ", " S ", "   ", 'S', SHAPE_EXTRUDER_ROD.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, "shape_extruder_ingot", SHAPE_EXTRUDER_INGOT.asStack(), "x  ", " S ", "   ", 'S', SHAPE_EMPTY.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, "shape_extruder_cell", SHAPE_EXTRUDER_CELL.asStack(), "   ", " Sx", "   ", 'S', SHAPE_EXTRUDER_RING.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, "shape_extruder_ring", SHAPE_EXTRUDER_RING.asStack(), "   ", " S ", " x ", 'S', SHAPE_EMPTY.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, "shape_extruder_bolt", SHAPE_EXTRUDER_BOLT.asStack(), "x  ", " S ", "   ", 'S', SHAPE_EXTRUDER_ROD.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, "shape_extruder_rod", SHAPE_EXTRUDER_ROD.asStack(), "   ", " Sx", "   ", 'S', SHAPE_EMPTY.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, "shape_extruder_rod_long", SHAPE_EXTRUDER_ROD_LONG.asStack(), "  x", " S ", "   ", 'S', SHAPE_EXTRUDER_ROD.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, "shape_extruder_plate", SHAPE_EXTRUDER_PLATE.asStack(), "x  ", " S ", "   ", 'S', SHAPE_EXTRUDER_FOIL.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, "shape_extruder_gear_small", SHAPE_EXTRUDER_GEAR_SMALL.asStack(), " x ", " S ", "   ", 'S', SHAPE_EXTRUDER_RING.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, "shape_extruder_foil", SHAPE_EXTRUDER_FOIL.asStack(), "   ", " S ", "  x", 'S', SHAPE_EMPTY.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, "shape_extruder_rotor", SHAPE_EXTRUDER_ROTOR.asStack(), "   ", " S ", "x  ", 'S', SHAPE_EMPTY.asStack());

        VanillaRecipeHelper.addShapedRecipe(provider, "shape_mold_rotor", SHAPE_MOLD_ROTOR.asStack(), "  h", " S ", "   ", 'S', SHAPE_EMPTY.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, "shape_mold_gear_small", SHAPE_MOLD_GEAR_SMALL.asStack(), "   ", "   ", "h S", 'S', SHAPE_EMPTY.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, "shape_mold_name", SHAPE_MOLD_NAME.asStack(), "  S", "   ", "h  ", 'S', SHAPE_EMPTY.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, "shape_mold_anvil", SHAPE_MOLD_ANVIL.asStack(), "  S", "   ", " h ", 'S', SHAPE_EMPTY.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, "shape_mold_cylinder", SHAPE_MOLD_CYLINDER.asStack(), "  S", "   ", "  h", 'S', SHAPE_EMPTY.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, "shape_mold_nugget", SHAPE_MOLD_NUGGET.asStack(), "S h", "   ", "   ", 'S', SHAPE_EMPTY.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, "shape_mold_block", SHAPE_MOLD_BLOCK.asStack(), "   ", "hS ", "   ", 'S', SHAPE_EMPTY.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, "shape_mold_ball", SHAPE_MOLD_BALL.asStack(), "   ", " S ", "h  ", 'S', SHAPE_EMPTY.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, "shape_mold_ingot", SHAPE_MOLD_INGOT.asStack(), "   ", " S ", " h ", 'S', SHAPE_EMPTY.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, "shape_mold_bottle", SHAPE_MOLD_BOTTLE.asStack(), "   ", " S ", "  h", 'S', SHAPE_EMPTY.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, "shape_mold_credit", SHAPE_MOLD_CREDIT.asStack(), "h  ", " S ", "   ", 'S', SHAPE_EMPTY.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, "shape_mold_gear", SHAPE_MOLD_GEAR.asStack(), "   ", " Sh", "   ", 'S', SHAPE_EMPTY.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, "shape_mold_plate", SHAPE_MOLD_PLATE.asStack(), " h ", " S ", "   ", 'S', SHAPE_EMPTY.asStack());

        ///////////////////////////////////////////////////
        //                   Credits                     //
        ///////////////////////////////////////////////////
        VanillaRecipeHelper.addShapelessRecipe(provider, "coin_chocolate", COIN_CHOCOLATE.asStack(), new UnificationEntry(dust, Cocoa), new UnificationEntry(foil, Gold), new ItemStack(Items.MILK_BUCKET), new UnificationEntry(dust, Sugar));

        VanillaRecipeHelper.addShapelessRecipe(provider, "credit_copper", CREDIT_COPPER.asStack(8), CREDIT_CUPRONICKEL.asStack());
        VanillaRecipeHelper.addShapelessRecipe(provider, "credit_cupronickel_alt", CREDIT_CUPRONICKEL.asStack(), CREDIT_COPPER.asStack(), CREDIT_COPPER.asStack(), CREDIT_COPPER.asStack(), CREDIT_COPPER.asStack(), CREDIT_COPPER.asStack(), CREDIT_COPPER.asStack(), CREDIT_COPPER.asStack(), CREDIT_COPPER.asStack());
        VanillaRecipeHelper.addShapelessRecipe(provider, "credit_cupronickel", CREDIT_CUPRONICKEL.asStack(8), CREDIT_SILVER.asStack());
        VanillaRecipeHelper.addShapelessRecipe(provider, "credit_silver_alt", CREDIT_SILVER.asStack(), CREDIT_CUPRONICKEL.asStack(), CREDIT_CUPRONICKEL.asStack(), CREDIT_CUPRONICKEL.asStack(), CREDIT_CUPRONICKEL.asStack(), CREDIT_CUPRONICKEL.asStack(), CREDIT_CUPRONICKEL.asStack(), CREDIT_CUPRONICKEL.asStack(), CREDIT_CUPRONICKEL.asStack());
        VanillaRecipeHelper.addShapelessRecipe(provider, "credit_silver", CREDIT_SILVER.asStack(8), CREDIT_GOLD.asStack());
        VanillaRecipeHelper.addShapelessRecipe(provider, "credit_gold_alt", CREDIT_GOLD.asStack(), CREDIT_SILVER.asStack(), CREDIT_SILVER.asStack(), CREDIT_SILVER.asStack(), CREDIT_SILVER.asStack(), CREDIT_SILVER.asStack(), CREDIT_SILVER.asStack(), CREDIT_SILVER.asStack(), CREDIT_SILVER.asStack());
        VanillaRecipeHelper.addShapelessRecipe(provider, "credit_gold", CREDIT_GOLD.asStack(8), CREDIT_PLATINUM.asStack());
        VanillaRecipeHelper.addShapelessRecipe(provider, "credit_platinum_alt", CREDIT_PLATINUM.asStack(), CREDIT_GOLD.asStack(), CREDIT_GOLD.asStack(), CREDIT_GOLD.asStack(), CREDIT_GOLD.asStack(), CREDIT_GOLD.asStack(), CREDIT_GOLD.asStack(), CREDIT_GOLD.asStack(), CREDIT_GOLD.asStack());
        VanillaRecipeHelper.addShapelessRecipe(provider, "credit_platinum", CREDIT_PLATINUM.asStack(8), CREDIT_OSMIUM.asStack());
        VanillaRecipeHelper.addShapelessRecipe(provider, "credit_osmium_alt", CREDIT_OSMIUM.asStack(), CREDIT_PLATINUM.asStack(), CREDIT_PLATINUM.asStack(), CREDIT_PLATINUM.asStack(), CREDIT_PLATINUM.asStack(), CREDIT_PLATINUM.asStack(), CREDIT_PLATINUM.asStack(), CREDIT_PLATINUM.asStack(), CREDIT_PLATINUM.asStack());
        VanillaRecipeHelper.addShapelessRecipe(provider, "credit_osmium", CREDIT_OSMIUM.asStack(8), CREDIT_NAQUADAH.asStack());
        VanillaRecipeHelper.addShapelessRecipe(provider, "credit_naquadah_alt", CREDIT_NAQUADAH.asStack(), CREDIT_OSMIUM.asStack(), CREDIT_OSMIUM.asStack(), CREDIT_OSMIUM.asStack(), CREDIT_OSMIUM.asStack(), CREDIT_OSMIUM.asStack(), CREDIT_OSMIUM.asStack(), CREDIT_OSMIUM.asStack(), CREDIT_OSMIUM.asStack());
        VanillaRecipeHelper.addShapelessRecipe(provider, "credit_naquadah", CREDIT_NAQUADAH.asStack(8), CREDIT_NEUTRONIUM.asStack());
        VanillaRecipeHelper.addShapelessRecipe(provider, "credit_darmstadtium", CREDIT_NEUTRONIUM.asStack(), CREDIT_NAQUADAH.asStack(), CREDIT_NAQUADAH.asStack(), CREDIT_NAQUADAH.asStack(), CREDIT_NAQUADAH.asStack(), CREDIT_NAQUADAH.asStack(), CREDIT_NAQUADAH.asStack(), CREDIT_NAQUADAH.asStack(), CREDIT_NAQUADAH.asStack());
        ///////////////////////////////////////////////////
        //                   Armors                      //
        ///////////////////////////////////////////////////
        // TODO armor
        //VanillaRecipeHelper.addShapedRecipe(provider, "nightvision_goggles", MetaItems.NIGHTVISION_GOGGLES.asStack(), "CSC", "RBR", "LdL", 'C', new UnificationEntry(circuit, Tier.ULV), 'S', new UnificationEntry(screw, Steel), 'R', new UnificationEntry(ring, Rubber), 'B', MetaItems.BATTERY_LV_SODIUM, 'L', new UnificationEntry(lens, Glass));
        //VanillaRecipeHelper.addShapedRecipe(provider, "fluid_jetpack", MetaItems.SEMIFLUID_JETPACK.asStack(), "xCw", "SUS", "RIR", 'C', new UnificationEntry(circuit, Tier.LV), 'S', MetaItems.FLUID_CELL_LARGE_STEEL.asStack(), 'U', MetaItems.ELECTRIC_PUMP_LV.asStack(), 'R', new UnificationEntry(rotor, Lead), 'I', new UnificationEntry(pipeSmallFluid, Potin));
        //VanillaRecipeHelper.addShapedRecipe(provider, "electric_jetpack", MetaItems.ELECTRIC_JETPACK.asStack(), "xCd", "TBT", "I I", 'C', new UnificationEntry(circuit, Tier.MV), 'T', MetaItems.POWER_THRUSTER.asStack(), 'B', MetaItems.BATTERY_MV_LITHIUM.asStack(), 'I', new UnificationEntry(wireGtDouble, AnnealedCopper));
        //VanillaRecipeHelper.addShapedRecipe(provider, "electric_jetpack_advanced", MetaItems.ELECTRIC_JETPACK_ADVANCED.asStack(), "xJd", "TBT", "WCW", 'J', MetaItems.ELECTRIC_JETPACK.asStack(), 'T', MetaItems.POWER_THRUSTER_ADVANCED.asStack(), 'B', ENERGIUM_CRYSTAL.asStack(), 'W', new UnificationEntry(wireGtQuadruple, Gold), 'C', new UnificationEntry(circuit, Tier.HV));
        //VanillaRecipeHelper.addShapedRecipe(provider, "nano_helmet", MetaItems.NANO_HELMET.asStack(), "PPP", "PNP", "xEd", 'P', MetaItems.CARBON_FIBER_PLATE.asStack(), 'N', MetaItems.NIGHTVISION_GOGGLES.asStack(), 'E', MetaItems.ENERGIUM_CRYSTAL.asStack());
        //VanillaRecipeHelper.addShapedRecipe(provider, "nano_chestplate", MetaItems.NANO_CHESTPLATE.asStack(), "PEP", "PPP", "PPP", 'P', MetaItems.CARBON_FIBER_PLATE.asStack(), 'E', MetaItems.ENERGIUM_CRYSTAL.asStack());
        //VanillaRecipeHelper.addShapedRecipe(provider, "nano_leggings", MetaItems.NANO_LEGGINGS.asStack(), "PPP", "PEP", "PxP", 'P', MetaItems.CARBON_FIBER_PLATE.asStack(), 'E', MetaItems.ENERGIUM_CRYSTAL.asStack());
        //VanillaRecipeHelper.addShapedRecipe(provider, "nano_boots", MetaItems.NANO_BOOTS.asStack(), "PxP", "PEP", 'P', MetaItems.CARBON_FIBER_PLATE.asStack(), 'E', MetaItems.ENERGIUM_CRYSTAL.asStack());
        //VanillaRecipeHelper.addShapedRecipe(provider, "nano_chestplate_advanced", MetaItems.NANO_CHESTPLATE_ADVANCED.asStack(), "xJd", "PNP", "WCW", 'J', MetaItems.ELECTRIC_JETPACK_ADVANCED.asStack(), 'P', MetaItems.LOW_POWER_INTEGRATED_CIRCUIT.asStack(), 'N', MetaItems.NANO_CHESTPLATE.asStack(), 'W', new UnificationEntry(wireGtQuadruple, Platinum), 'C', new UnificationEntry(circuit, Tier.IV));
        //VanillaRecipeHelper.addShapedRecipe(provider, "gravitation_engine", MetaItems.GRAVITATION_ENGINE.asStack(), "ESE", "POP", "ESE", 'E', MetaItems.EMITTER_LuV.asStack(), 'S', new UnificationEntry(wireGtQuadruple, Osmium), 'P', new UnificationEntry(plateDouble, Iridium), 'O', MetaItems.ENERGY_LAPOTRONIC_ORB.asStack());
    }

    // TODO Facades
    //private static void registerFacadeRecipe(Consumer<FinishedRecipe> provider, Material material, int facadeAmount) {
    //    OreIngredient ingredient = new OreIngredient(new UnificationEntry(plate, material).toString());
    //    ForgeRegistries.RECIPES.register(new FacadeRecipe(null, ingredient, facadeAmount).setRegistryName("facade_" + material));
    //}
}
