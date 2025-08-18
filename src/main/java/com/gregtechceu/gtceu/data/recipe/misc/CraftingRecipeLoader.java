package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.recipe.FacadeCoverRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidContainerIngredient;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.pipelike.duct.DuctPipeType;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTItems.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

public class CraftingRecipeLoader {

    public static void init(Consumer<FinishedRecipe> provider) {
        // todo facades
        // registerFacadeRecipe(provider, Iron, 4);

        VanillaRecipeHelper.addShapedRecipe(provider, "small_wooden_pipe", ChemicalHelper.get(pipeSmallFluid, Wood),
                "sWr", 'W', ItemTags.PLANKS);
        VanillaRecipeHelper.addShapedRecipe(provider, "normal_wooden_pipe", ChemicalHelper.get(pipeNormalFluid, Wood),
                "WWW", "s r", 'W', ItemTags.PLANKS);
        VanillaRecipeHelper.addShapedRecipe(provider, "large_wooden_pipe", ChemicalHelper.get(pipeLargeFluid, Wood),
                "WWW", "s r", "WWW", 'W', ItemTags.PLANKS);

        VanillaRecipeHelper.addShapedRecipe(provider, "small_treated_wooden_pipe",
                ChemicalHelper.get(pipeSmallFluid, TreatedWood), "sWr", 'W', GTBlocks.TREATED_WOOD_PLANK.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, "normal_treated_wooden_pipe",
                ChemicalHelper.get(pipeNormalFluid, TreatedWood), "WWW", "s r", 'W',
                GTBlocks.TREATED_WOOD_PLANK.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, "large_treated_wooden_pipe",
                ChemicalHelper.get(pipeLargeFluid, TreatedWood), "WWW", "s r", "WWW", 'W',
                GTBlocks.TREATED_WOOD_PLANK.asStack());

        addDuctRecipes(provider, Steel, 2);
        addDuctRecipes(provider, StainlessSteel, 4);
        addDuctRecipes(provider, TungstenSteel, 8);

        VanillaRecipeHelper.addShapelessRecipe(provider, "programmed_circuit", PROGRAMMED_CIRCUIT.asStack(),
                CustomTags.LV_CIRCUITS);

        VanillaRecipeHelper.addShapedRecipe(provider, "item_filter", ITEM_FILTER.asStack(), "XXX", "XYX", "XXX", 'X',
                new MaterialEntry(foil, Zinc), 'Y', new MaterialEntry(plate, Steel));
        VanillaRecipeHelper.addShapedRecipe(provider, "fluid_filter_lapis", FLUID_FILTER.asStack(), "XXX", "XYX", "XXX",
                'X', new MaterialEntry(foil, Zinc), 'Y', new MaterialEntry(plate, Lapis));
        VanillaRecipeHelper.addShapedRecipe(provider, "fluid_filter_lazurite", FLUID_FILTER.asStack(), "XXX", "XYX",
                "XXX", 'X', new MaterialEntry(foil, Zinc), 'Y', new MaterialEntry(plate, Lazurite));
        VanillaRecipeHelper.addShapedRecipe(provider, "fluid_filter_sodalite", FLUID_FILTER.asStack(), "XXX", "XYX",
                "XXX", 'X', new MaterialEntry(foil, Zinc), 'Y', new MaterialEntry(plate, Sodalite));

        VanillaRecipeHelper.addShapedRecipe(provider, "tag_filter_olivine", TAG_FILTER.asStack(),
                "XXX", "XYX", "XXX", 'X', new MaterialEntry(foil, Zinc), 'Y', new MaterialEntry(plate, Olivine));
        VanillaRecipeHelper.addShapedRecipe(provider, "tag_filter_emerald", TAG_FILTER.asStack(),
                "XXX", "XYX", "XXX", 'X', new MaterialEntry(foil, Zinc), 'Y', new MaterialEntry(plate, Emerald));
        VanillaRecipeHelper.addShapedRecipe(provider, "fluid_tag_filter", TAG_FLUID_FILTER.asStack(),
                "XXX", "XYX", "XXX", 'X', new MaterialEntry(foil, Zinc), 'Y', new MaterialEntry(plate, Amethyst));

        VanillaRecipeHelper.addShapedRecipe(provider, "item_smart_filter_olivine", SMART_ITEM_FILTER.asStack(), "XEX",
                "XCX", "XEX", 'X', new MaterialEntry(foil, Zinc), 'C', CustomTags.LV_CIRCUITS, 'E',
                new MaterialEntry(plate, Ruby));

        VanillaRecipeHelper.addShapedRecipe(provider, "plank_to_wooden_shape", WOODEN_FORM_EMPTY.asStack(), "   ",
                " X ", "s  ", 'X', ItemTags.PLANKS);
        VanillaRecipeHelper.addShapedRecipe(provider, "wooden_shape_brick", WOODEN_FORM_BRICK.asStack(), "k ", " X",
                'X', WOODEN_FORM_EMPTY.asStack());

        VanillaRecipeHelper.addShapedRecipe(provider, "compressed_coke_clay", COMPRESSED_COKE_CLAY.asStack(3), "XXX",
                "SYS", "SSS", 'Y', WOODEN_FORM_BRICK.asStack(), 'X', new ItemStack(Items.CLAY_BALL), 'S',
                ItemTags.SAND);
        VanillaRecipeHelper.addShapelessRecipe(provider, "fireclay_dust", ChemicalHelper.get(dust, Fireclay, 2),
                new MaterialEntry(dust, Brick), new MaterialEntry(dust, Clay));
        VanillaRecipeHelper.addSmeltingRecipe(provider, "coke_oven_brick", COMPRESSED_COKE_CLAY.asStack(),
                COKE_OVEN_BRICK.asStack(), 0.3f);
        VanillaRecipeHelper.addSmeltingRecipe(provider, "fireclay_brick", COMPRESSED_FIRECLAY.asStack(),
                FIRECLAY_BRICK.asStack(), 0.3f);

        VanillaRecipeHelper.addSmeltingRecipe(provider, "wrought_iron_nugget", ChemicalHelper.getTag(nugget, Iron),
                ChemicalHelper.get(nugget, WroughtIron));
        VanillaRecipeHelper.addShapelessRecipe(provider, "nugget_disassembling_iron",
                new ItemStack(Items.IRON_NUGGET, 9), new ItemStack(Items.IRON_INGOT), 's');

        // TODO clipboard
        // VanillaRecipeHelper.addShapedRecipe(provider, "clipboard", CLIPBOARD.asStack(), " Sd", "BWR", "PPP", 'P',
        // Items.PAPER, 'R', new MaterialEntry(springSmall, Iron), 'B', new MaterialEntry(bolt, Iron), 'S', new
        // MaterialEntry(screw, Iron), 'W', new MaterialEntry(plate, Wood));

        VanillaRecipeHelper.addShapedFluidContainerRecipe(provider, "treated_wood_planks",
                GTBlocks.TREATED_WOOD_PLANK.asStack(8),
                "PPP", "PBP", "PPP", 'P', ItemTags.PLANKS, 'B',
                new FluidContainerIngredient(Creosote.getFluidTag(), 1000));

        VanillaRecipeHelper.addShapedRecipe(provider, "rubber_ring", ChemicalHelper.get(ring, Rubber), "k", "X", 'X',
                new MaterialEntry(plate, Rubber));
        VanillaRecipeHelper.addShapedRecipe(provider, "silicone_rubber_ring", ChemicalHelper.get(ring, SiliconeRubber),
                "k", "P", 'P', ChemicalHelper.get(plate, SiliconeRubber));
        VanillaRecipeHelper.addShapedRecipe(provider, "styrene_rubber_ring",
                ChemicalHelper.get(ring, StyreneButadieneRubber), "k", "P", 'P',
                ChemicalHelper.get(plate, StyreneButadieneRubber));

        VanillaRecipeHelper.addShapelessRecipe(provider, "iron_magnetic_stick", ChemicalHelper.get(rod, IronMagnetic),
                new MaterialEntry(rod, Iron), new MaterialEntry(dust, Redstone),
                new MaterialEntry(dust, Redstone), new MaterialEntry(dust, Redstone),
                new MaterialEntry(dust, Redstone));

        VanillaRecipeHelper.addShapedRecipe(provider, "component_grinder_diamond", COMPONENT_GRINDER_DIAMOND.asStack(),
                "XSX", "SDS", "XSX", 'X', new MaterialEntry(dust, Diamond), 'S',
                new MaterialEntry(plateDouble, Steel), 'D', new MaterialEntry(gem, Diamond));
        VanillaRecipeHelper.addShapedRecipe(provider, "component_grinder_tungsten",
                COMPONENT_GRINDER_TUNGSTEN.asStack(), "WSW", "SDS", "WSW", 'W', new MaterialEntry(plate, Tungsten),
                'S', new MaterialEntry(plateDouble, VanadiumSteel), 'D', new MaterialEntry(gem, Diamond));

        VanillaRecipeHelper.addShapedRecipe(provider, "minecart_wheels_iron", IRON_MINECART_WHEELS.asStack(), " h ",
                "RSR", " w ", 'R', new MaterialEntry(ring, Iron), 'S', new MaterialEntry(rod, Iron));
        VanillaRecipeHelper.addShapedRecipe(provider, "minecart_wheels_steel", STEEL_MINECART_WHEELS.asStack(), " h ",
                "RSR", " w ", 'R', new MaterialEntry(ring, Steel), 'S', new MaterialEntry(rod, Steel));

        VanillaRecipeHelper.addShapedRecipe(provider, "nano_saber", NANO_SABER.asStack(), "PIC", "PIC",
                "XEX", 'P', new MaterialEntry(plate, Platinum), 'I', new MaterialEntry(plate, Ruridit), 'C',
                CARBON_FIBER_PLATE.asStack(), 'X', CustomTags.EV_CIRCUITS, 'E', ENERGIUM_CRYSTAL.asStack());

        VanillaRecipeHelper.addShapedRecipe(provider, "solar_panel_basic", COVER_SOLAR_PANEL.asStack(), "WGW", "CPC",
                'W', SILICON_WAFER.asStack(), 'G', new ItemStack(Blocks.GLASS_PANE), 'C', CustomTags.LV_CIRCUITS, 'P',
                CARBON_FIBER_PLATE.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, "solar_panel_ulv", COVER_SOLAR_PANEL_ULV.asStack(), "WGW", "CAC",
                "P P", 'W', PHOSPHORUS_WAFER.asStack(), 'G', Tags.Items.GLASS_PANES, 'C', CustomTags.HV_CIRCUITS, 'P',
                ChemicalHelper.get(plate, GalliumArsenide), 'A', ChemicalHelper.get(wireGtQuadruple, Graphene));
        VanillaRecipeHelper.addShapedRecipe(provider, "solar_panel_lv", COVER_SOLAR_PANEL_LV.asStack(), "WGW", "CAC",
                "P P", 'W', NAQUADAH_WAFER.asStack(), 'G', GTBlocks.CASING_TEMPERED_GLASS.asStack(), 'C',
                CustomTags.LuV_CIRCUITS, 'P', ChemicalHelper.get(plate, IndiumGalliumPhosphide), 'A',
                ChemicalHelper.get(wireGtHex, Graphene));

        VanillaRecipeHelper.addShapedRecipe(provider, "universal_fluid_cell", FLUID_CELL_UNIVERSAL.asStack(), "C ",
                "  ", 'C', FLUID_CELL);
        VanillaRecipeHelper.addShapedRecipe(provider, "universal_fluid_cell_revert", FLUID_CELL.asStack(), "C ", "  ",
                'C', FLUID_CELL_UNIVERSAL);

        VanillaRecipeHelper.addShapedRecipe(provider, "blacklight", BLACKLIGHT.asStack(), "SPS", "GRG", "CPK", 'S',
                new MaterialEntry(screw, TungstenCarbide), 'P', new MaterialEntry(plate, TungstenCarbide), 'G',
                GTBlocks.CASING_LAMINATED_GLASS.asStack(), 'R', new MaterialEntry(spring, Europium), 'C',
                CustomTags.IV_CIRCUITS, 'K', new MaterialEntry(cableGtSingle, Platinum));

        VanillaRecipeHelper.addShapedRecipe(provider, true, "filter_casing", GTBlocks.FILTER_CASING.asStack(), "BBB",
                "III", "MFR", 'B', new ItemStack(Blocks.IRON_BARS), 'I', ITEM_FILTER.asStack(), 'M',
                ELECTRIC_MOTOR_MV.asStack(), 'F', new MaterialEntry(frameGt, Steel), 'R',
                new MaterialEntry(rotor, Steel));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "filter_casing_sterile",
                GTBlocks.FILTER_CASING_STERILE.asStack(), "BEB", "ISI", "MFR", 'B',
                new MaterialEntry(pipeLargeFluid, Polybenzimidazole), 'E', EMITTER_ZPM.asStack(), 'I',
                ITEM_FILTER.asStack(), 'S', BLACKLIGHT.asStack(), 'M', ELECTRIC_MOTOR_ZPM.asStack(), 'F',
                new MaterialEntry(frameGt, Tritanium), 'R', new MaterialEntry(rotor, NaquadahAlloy));

        ///////////////////////////////////////////////////
        // Shapes and Molds //
        ///////////////////////////////////////////////////
        VanillaRecipeHelper.addShapedRecipe(provider, "shape_empty", SHAPE_EMPTY.asStack(), "hf", "PP", "PP", 'P',
                new MaterialEntry(plate, Steel));

        VanillaRecipeHelper.addStrictShapedRecipe(provider, "shape_extruder_bottle", SHAPE_EXTRUDER_BOTTLE.asStack(),
                "  x", " S ", "   ", 'S', SHAPE_EXTRUDER_RING.asStack());
        VanillaRecipeHelper.addStrictShapedRecipe(provider, "shape_extruder_gear", SHAPE_EXTRUDER_GEAR.asStack(), "x  ",
                " S ", "   ", 'S', SHAPE_EXTRUDER_RING.asStack());
        VanillaRecipeHelper.addStrictShapedRecipe(provider, "shape_extruder_block", SHAPE_EXTRUDER_BLOCK.asStack(),
                "x  ", " S ", "   ", 'S', SHAPE_EXTRUDER_INGOT.asStack());
        VanillaRecipeHelper.addStrictShapedRecipe(provider, "shape_extruder_pipe_huge",
                SHAPE_EXTRUDER_PIPE_HUGE.asStack(), "   ", " S ", "  x", 'S', SHAPE_EXTRUDER_BOLT.asStack());
        VanillaRecipeHelper.addStrictShapedRecipe(provider, "shape_extruder_pipe_large",
                SHAPE_EXTRUDER_PIPE_LARGE.asStack(), "   ", " Sx", "   ", 'S', SHAPE_EXTRUDER_BOLT.asStack());
        VanillaRecipeHelper.addStrictShapedRecipe(provider, "shape_extruder_pipe_normal",
                SHAPE_EXTRUDER_PIPE_NORMAL.asStack(), "  x", " S ", "   ", 'S', SHAPE_EXTRUDER_BOLT.asStack());
        VanillaRecipeHelper.addStrictShapedRecipe(provider, "shape_extruder_pipe_small",
                SHAPE_EXTRUDER_PIPE_SMALL.asStack(), " x ", " S ", "   ", 'S', SHAPE_EXTRUDER_BOLT.asStack());
        VanillaRecipeHelper.addStrictShapedRecipe(provider, "shape_extruder_pipe_tiny",
                SHAPE_EXTRUDER_PIPE_TINY.asStack(), "x  ", " S ", "   ", 'S', SHAPE_EXTRUDER_BOLT.asStack());
        VanillaRecipeHelper.addStrictShapedRecipe(provider, "shape_extruder_wire", SHAPE_EXTRUDER_WIRE.asStack(), " x ",
                " S ", "   ", 'S', SHAPE_EXTRUDER_ROD.asStack());
        VanillaRecipeHelper.addStrictShapedRecipe(provider, "shape_extruder_ingot", SHAPE_EXTRUDER_INGOT.asStack(),
                "x  ", " S ", "   ", 'S', SHAPE_EMPTY.asStack());
        VanillaRecipeHelper.addStrictShapedRecipe(provider, "shape_extruder_cell", SHAPE_EXTRUDER_CELL.asStack(), "   ",
                " Sx", "   ", 'S', SHAPE_EXTRUDER_RING.asStack());
        VanillaRecipeHelper.addStrictShapedRecipe(provider, "shape_extruder_ring", SHAPE_EXTRUDER_RING.asStack(), "   ",
                " S ", " x ", 'S', SHAPE_EMPTY.asStack());
        VanillaRecipeHelper.addStrictShapedRecipe(provider, "shape_extruder_bolt", SHAPE_EXTRUDER_BOLT.asStack(), "x  ",
                " S ", "   ", 'S', SHAPE_EXTRUDER_ROD.asStack());
        VanillaRecipeHelper.addStrictShapedRecipe(provider, "shape_extruder_rod", SHAPE_EXTRUDER_ROD.asStack(), "   ",
                " Sx", "   ", 'S', SHAPE_EMPTY.asStack());
        VanillaRecipeHelper.addStrictShapedRecipe(provider, "shape_extruder_plate", SHAPE_EXTRUDER_PLATE.asStack(),
                "x  ", " S ", "   ", 'S', SHAPE_EXTRUDER_FOIL.asStack());
        VanillaRecipeHelper.addStrictShapedRecipe(provider, "shape_extruder_gear_small",
                SHAPE_EXTRUDER_GEAR_SMALL.asStack(), " x ", " S ", "   ", 'S', SHAPE_EXTRUDER_RING.asStack());
        VanillaRecipeHelper.addStrictShapedRecipe(provider, "shape_extruder_foil", SHAPE_EXTRUDER_FOIL.asStack(), "   ",
                " S ", "  x", 'S', SHAPE_EMPTY.asStack());
        VanillaRecipeHelper.addStrictShapedRecipe(provider, "shape_extruder_rotor", SHAPE_EXTRUDER_ROTOR.asStack(),
                "   ", " S ", "x  ", 'S', SHAPE_EMPTY.asStack());

        VanillaRecipeHelper.addStrictShapedRecipe(provider, "shape_mold_pill", SHAPE_MOLD_PILL.asStack(), "  h",
                "  S", "   ", 'S', SHAPE_EMPTY.asStack());
        VanillaRecipeHelper.addStrictShapedRecipe(provider, "shape_mold_rotor", SHAPE_MOLD_ROTOR.asStack(), "  h",
                " S ", "   ", 'S', SHAPE_EMPTY.asStack());
        VanillaRecipeHelper.addStrictShapedRecipe(provider, "shape_mold_gear_small", SHAPE_MOLD_GEAR_SMALL.asStack(),
                "   ", "   ", "h S", 'S', SHAPE_EMPTY.asStack());
        VanillaRecipeHelper.addStrictShapedRecipe(provider, "shape_mold_name", SHAPE_MOLD_NAME.asStack(), "  S", "   ",
                "h  ", 'S', SHAPE_EMPTY.asStack());
        VanillaRecipeHelper.addStrictShapedRecipe(provider, "shape_mold_anvil", SHAPE_MOLD_ANVIL.asStack(), "  S",
                "   ", " h ", 'S', SHAPE_EMPTY.asStack());
        VanillaRecipeHelper.addStrictShapedRecipe(provider, "shape_mold_cylinder", SHAPE_MOLD_CYLINDER.asStack(), "  S",
                "   ", "  h", 'S', SHAPE_EMPTY.asStack());
        VanillaRecipeHelper.addStrictShapedRecipe(provider, "shape_mold_nugget", SHAPE_MOLD_NUGGET.asStack(), "S h",
                "   ", "   ", 'S', SHAPE_EMPTY.asStack());
        VanillaRecipeHelper.addStrictShapedRecipe(provider, "shape_mold_block", SHAPE_MOLD_BLOCK.asStack(), "   ",
                "hS ", "   ", 'S', SHAPE_EMPTY.asStack());
        VanillaRecipeHelper.addStrictShapedRecipe(provider, "shape_mold_ball", SHAPE_MOLD_BALL.asStack(), "   ", " S ",
                "h  ", 'S', SHAPE_EMPTY.asStack());
        VanillaRecipeHelper.addStrictShapedRecipe(provider, "shape_mold_ingot", SHAPE_MOLD_INGOT.asStack(), "   ",
                " S ", " h ", 'S', SHAPE_EMPTY.asStack());
        VanillaRecipeHelper.addStrictShapedRecipe(provider, "shape_mold_bottle", SHAPE_MOLD_BOTTLE.asStack(), "   ",
                " S ", "  h", 'S', SHAPE_EMPTY.asStack());
        VanillaRecipeHelper.addStrictShapedRecipe(provider, "shape_mold_gear", SHAPE_MOLD_GEAR.asStack(), "   ", " Sh",
                "   ", 'S', SHAPE_EMPTY.asStack());
        VanillaRecipeHelper.addStrictShapedRecipe(provider, "shape_mold_plate", SHAPE_MOLD_PLATE.asStack(), " h ",
                " S ", "   ", 'S', SHAPE_EMPTY.asStack());
        VanillaRecipeHelper.addStrictShapedRecipe(provider, "shape_mold_pipe_huge",
                SHAPE_MOLD_HUGE_PIPE.asStack(), "   ", "   ", "hS ", 'S', SHAPE_EMPTY.asStack());
        VanillaRecipeHelper.addStrictShapedRecipe(provider, "shape_mold_pipe_large",
                SHAPE_MOLD_LARGE_PIPE.asStack(), "   ", "h  ", " S ", 'S', SHAPE_EMPTY.asStack());
        VanillaRecipeHelper.addStrictShapedRecipe(provider, "shape_mold_pipe_normal",
                SHAPE_MOLD_NORMAL_PIPE.asStack(), "   ", " h ", " S ", 'S', SHAPE_EMPTY.asStack());
        VanillaRecipeHelper.addStrictShapedRecipe(provider, "shape_mold_pipe_small",
                SHAPE_MOLD_SMALL_PIPE.asStack(), "   ", "  h", " S ", 'S', SHAPE_EMPTY.asStack());
        VanillaRecipeHelper.addStrictShapedRecipe(provider, "shape_mold_pipe_tiny",
                SHAPE_MOLD_TINY_PIPE.asStack(), "   ", "   ", " Sh", 'S', SHAPE_EMPTY.asStack());

        ///////////////////////////////////////////////////
        // Armors //
        ///////////////////////////////////////////////////
        VanillaRecipeHelper.addShapedRecipe(provider, "nightvision_goggles", GTItems.NIGHTVISION_GOGGLES.asStack(),
                "CSC", "RBR", "LdL", 'C', CustomTags.ULV_CIRCUITS, 'S', new MaterialEntry(screw, Steel), 'R',
                new MaterialEntry(ring, Rubber), 'B', GTItems.BATTERY_LV_SODIUM, 'L',
                new MaterialEntry(lens, Glass));
        VanillaRecipeHelper.addShapedRecipe(provider, "fluid_jetpack", GTItems.LIQUID_FUEL_JETPACK.asStack(), "xCw",
                "SUS", "RIR", 'C', CustomTags.LV_CIRCUITS, 'S', GTItems.FLUID_CELL_LARGE_STEEL.asStack(), 'U',
                GTItems.ELECTRIC_PUMP_LV.asStack(), 'R', new MaterialEntry(rotor, Lead), 'I',
                new MaterialEntry(pipeSmallFluid, Potin));
        VanillaRecipeHelper.addShapedRecipe(provider, "electric_jetpack", GTItems.ELECTRIC_JETPACK.asStack(), "xCd",
                "TBT", "I I", 'C', CustomTags.MV_CIRCUITS, 'T', GTItems.POWER_THRUSTER.asStack(), 'B',
                GTItems.BATTERY_MV_LITHIUM.asStack(), 'I', new MaterialEntry(wireGtDouble, AnnealedCopper));
        VanillaRecipeHelper.addShapedRecipe(provider, "electric_jetpack_advanced",
                GTItems.ELECTRIC_JETPACK_ADVANCED.asStack(), "xJd", "TBT", "WCW", 'J',
                GTItems.ELECTRIC_JETPACK.asStack(), 'T', GTItems.POWER_THRUSTER_ADVANCED.asStack(), 'B',
                ENERGIUM_CRYSTAL.asStack(), 'W', new MaterialEntry(wireGtQuadruple, Gold), 'C',
                CustomTags.HV_CIRCUITS);
        VanillaRecipeHelper.addShapedRecipe(provider, "nano_helmet", GTItems.NANO_HELMET.asStack(), "PPP", "PNP", "xEd",
                'P', GTItems.CARBON_FIBER_PLATE.asStack(), 'N', GTItems.NIGHTVISION_GOGGLES.asStack(), 'E',
                GTItems.ENERGIUM_CRYSTAL.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, "nano_chestplate", GTItems.NANO_CHESTPLATE.asStack(), "PEP",
                "PPP", "PPP", 'P', GTItems.CARBON_FIBER_PLATE.asStack(), 'E', GTItems.ENERGIUM_CRYSTAL.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, "nano_leggings", GTItems.NANO_LEGGINGS.asStack(), "PPP", "PEP",
                "PxP", 'P', GTItems.CARBON_FIBER_PLATE.asStack(), 'E', GTItems.ENERGIUM_CRYSTAL.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, "nano_boots", GTItems.NANO_BOOTS.asStack(), "PxP", "PEP", 'P',
                GTItems.CARBON_FIBER_PLATE.asStack(), 'E', GTItems.ENERGIUM_CRYSTAL.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, "nano_chestplate_advanced",
                GTItems.NANO_CHESTPLATE_ADVANCED.asStack(), "xJd", "PNP", "WCW", 'J',
                GTItems.ELECTRIC_JETPACK_ADVANCED.asStack(), 'P', GTItems.LOW_POWER_INTEGRATED_CIRCUIT.asStack(), 'N',
                GTItems.NANO_CHESTPLATE.asStack(), 'W', new MaterialEntry(wireGtQuadruple, Platinum), 'C',
                CustomTags.IV_CIRCUITS);
        VanillaRecipeHelper.addShapedRecipe(provider, "gravitation_engine", GTItems.GRAVITATION_ENGINE.asStack(), "ESE",
                "POP", "ESE", 'E', GTItems.EMITTER_LuV.asStack(), 'S', new MaterialEntry(wireGtQuadruple, Osmium),
                'P', new MaterialEntry(plateDouble, Iridium), 'O', GTItems.ENERGY_LAPOTRONIC_ORB.asStack());

        VanillaRecipeHelper.addShapedRecipe(provider, "face_mask", FACE_MASK.asStack(), "S S", "PPP", 'S', Items.STRING,
                'P', Items.PAPER);
        VanillaRecipeHelper.addShapedRecipe(provider, "rubber_gloves", RUBBER_GLOVES.asStack(), "P P", 'P',
                new MaterialEntry(plate, Rubber));

        VanillaRecipeHelper.addShapedRecipe(provider, "powderbarrel", new ItemStack(GTBlocks.POWDERBARREL), "PSP",
                "GGG", "PGP",
                'P', new MaterialEntry(plate, Wood),
                'S', new ItemStack(Items.STRING),
                'G', new MaterialEntry(dust, Gunpowder));

        ///////////////////////////////////////////////////
        // Special //
        ///////////////////////////////////////////////////
        SpecialRecipeBuilder.special(FacadeCoverRecipe.SERIALIZER).save(provider, "gtceu:crafting/facade_cover");
    }

    private static void addDuctRecipes(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material,
                                       int outputAmount) {
        VanillaRecipeHelper.addShapedRecipe(provider, "small_duct_%s".formatted(material.getName()),
                GTBlocks.DUCT_PIPES[DuctPipeType.SMALL.ordinal()].asStack(outputAmount * 2), "w", "X", "h",
                'X', new MaterialEntry(plate, material));
        VanillaRecipeHelper.addShapedRecipe(provider, "medium_duct_%s".formatted(material.getName()),
                GTBlocks.DUCT_PIPES[DuctPipeType.NORMAL.ordinal()].asStack(outputAmount), " X ", "wXh", " X ",
                'X', new MaterialEntry(plate, material));
        VanillaRecipeHelper.addShapedRecipe(provider, "large_duct_%s".formatted(material.getName()),
                GTBlocks.DUCT_PIPES[DuctPipeType.LARGE.ordinal()].asStack(outputAmount), "XwX", "X X", "XhX",
                'X', new MaterialEntry(plate, material));
        VanillaRecipeHelper.addShapedRecipe(provider, "huge_duct_%s".formatted(material.getName()),
                GTBlocks.DUCT_PIPES[DuctPipeType.HUGE.ordinal()].asStack(outputAmount), "XwX", "X X", "XhX",
                'X', new MaterialEntry(plateDouble, material));
    }

    // private static void registerFacadeRecipe(Consumer<FinishedRecipe> provider, Material material, int facadeAmount)
    // {
    // OreIngredient ingredient = new OreIngredient(new MaterialEntry(plate, material).toString());
    // ForgeRegistries.RECIPES.register(new FacadeRecipe(null, ingredient, facadeAmount).setRegistryName("facade_" +
    // material));
    // }
}
