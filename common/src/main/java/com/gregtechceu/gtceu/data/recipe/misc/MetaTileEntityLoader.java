package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterials.Tier;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.LoaderTags;
import com.gregtechceu.gtceu.data.recipe.CraftingComponent;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.data.recipe.CraftingComponent.*;

public class MetaTileEntityLoader {

    public static void init(Consumer<FinishedRecipe> provider) {
        CraftingComponent.initializeComponents();

        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_ulv", GTBlocks.MACHINE_CASING_ULV.asStack(), "PPP", "PwP", "PPP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.WroughtIron));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_lv", GTBlocks.MACHINE_CASING_LV.asStack(), "PPP", "PwP", "PPP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Steel));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_mv", GTBlocks.MACHINE_CASING_MV.asStack(), "PPP", "PwP", "PPP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Aluminium));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_hv", GTBlocks.MACHINE_CASING_HV.asStack(), "PPP", "PwP", "PPP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.StainlessSteel));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_ev", GTBlocks.MACHINE_CASING_EV.asStack(), "PPP", "PwP", "PPP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Titanium));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_iv", GTBlocks.MACHINE_CASING_IV.asStack(), "PPP", "PwP", "PPP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.TungstenSteel));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_luv", GTBlocks.MACHINE_CASING_LuV.asStack(), "PPP", "PwP", "PPP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.RhodiumPlatedPalladium));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_zpm", GTBlocks.MACHINE_CASING_ZPM.asStack(), "PPP", "PwP", "PPP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.NaquadahAlloy));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_uv", GTBlocks.MACHINE_CASING_UV.asStack(), "PPP", "PwP", "PPP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Darmstadtium));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_uhv", GTBlocks.MACHINE_CASING_UHV.asStack(), "PPP", "PwP", "PPP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Neutronium));

        // If these recipes are changed, change the values in MaterialInfoLoader.java
        registerMachineRecipe(provider, false, GTMachines.HULL, "PLP", "CHC", 'P', HULL_PLATE, 'L', PLATE, 'C', CABLE, 'H', CASING);

        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_coke_bricks", GTBlocks.CASING_COKE_BRICKS.asStack(), "XX", "XX", 'X', GTItems.COKE_OVEN_BRICK);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_bronze_bricks", GTBlocks.CASING_BRONZE_BRICKS.asStack(2), "PhP", "PBP", "PwP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Bronze), 'B', new ItemStack(Blocks.BRICKS));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_steel_solid", GTBlocks.CASING_STEEL_SOLID.asStack(2), "PhP", "PFP", "PwP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Steel), 'F', new UnificationEntry(TagPrefix.frameGt, GTMaterials.Steel));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_titanium_stable", GTBlocks.CASING_TITANIUM_STABLE.asStack(2), "PhP", "PFP", "PwP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Titanium), 'F', new UnificationEntry(TagPrefix.frameGt, GTMaterials.Titanium));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_invar_heatproof", GTBlocks.CASING_INVAR_HEATPROOF.asStack(2), "PhP", "PFP", "PwP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Invar), 'F', new UnificationEntry(TagPrefix.frameGt, GTMaterials.Invar));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_aluminium_frostproof", GTBlocks.CASING_ALUMINIUM_FROSTPROOF.asStack(2), "PhP", "PFP", "PwP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Aluminium), 'F', new UnificationEntry(TagPrefix.frameGt, GTMaterials.Aluminium));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_stainless_clean", GTBlocks.CASING_STAINLESS_CLEAN.asStack(2), "PhP", "PFP", "PwP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.StainlessSteel), 'F', new UnificationEntry(TagPrefix.frameGt, GTMaterials.StainlessSteel));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_tungstensteel_robust", GTBlocks.CASING_TUNGSTENSTEEL_ROBUST.asStack(2), "PhP", "PFP", "PwP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.TungstenSteel), 'F', new UnificationEntry(TagPrefix.frameGt, GTMaterials.TungstenSteel));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_hssg_robust", GTBlocks.CASING_HSSE_STURDY.asStack(2), "PhP", "PFP", "PwP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.HSSE), 'F', new UnificationEntry(TagPrefix.frameGt, GTMaterials.Europium));

        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_steel_turbine_casing", GTBlocks.CASING_STEEL_TURBINE.asStack(2), "PhP", "PFP", "PwP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Magnalium), 'F', new UnificationEntry(TagPrefix.frameGt, GTMaterials.BlueSteel));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_stainless_turbine_casing", GTBlocks.CASING_STAINLESS_TURBINE.asStack(2), "PhP", "PFP", "PwP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.StainlessSteel), 'F', GTBlocks.CASING_STEEL_TURBINE.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_titanium_turbine_casing", GTBlocks.CASING_TITANIUM_TURBINE.asStack(2), "PhP", "PFP", "PwP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Titanium), 'F', GTBlocks.CASING_STEEL_TURBINE.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_tungstensteel_turbine_casing", GTBlocks.CASING_TUNGSTENSTEEL_TURBINE.asStack(2), "PhP", "PFP", "PwP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.TungstenSteel), 'F', GTBlocks.CASING_STEEL_TURBINE.asStack());


        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_bronze_pipe", GTBlocks.CASING_BRONZE_PIPE.asStack(2), "PIP", "IFI", "PIP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Bronze), 'F', new UnificationEntry(TagPrefix.frameGt, GTMaterials.Bronze), 'I', new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.Bronze));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_steel_pipe", GTBlocks.CASING_STEEL_PIPE.asStack(2), "PIP", "IFI", "PIP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Steel), 'F', new UnificationEntry(TagPrefix.frameGt, GTMaterials.Steel), 'I', new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.Steel));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_titanium_pipe", GTBlocks.CASING_TITANIUM_PIPE.asStack(2), "PIP", "IFI", "PIP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Titanium), 'F', new UnificationEntry(TagPrefix.frameGt, GTMaterials.Titanium), 'I', new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.Titanium));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_tungstensteel_pipe", GTBlocks.CASING_TUNGSTENSTEEL_PIPE.asStack(2), "PIP", "IFI", "PIP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.TungstenSteel), 'F', new UnificationEntry(TagPrefix.frameGt, GTMaterials.TungstenSteel), 'I', new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.TungstenSteel));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_ptfe_pipe", GTBlocks.CASING_POLYTETRAFLUOROETHYLENE_PIPE.asStack(2), "PIP", "IFI", "PIP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Polytetrafluoroethylene), 'F', new UnificationEntry(TagPrefix.frameGt, GTMaterials.Polytetrafluoroethylene), 'I', new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.Polytetrafluoroethylene));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_bronze_firebox", GTBlocks.FIREBOX_BRONZE.asStack(2), "PSP", "SFS", "PSP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Bronze), 'F', new UnificationEntry(TagPrefix.frameGt, GTMaterials.Bronze), 'S', new UnificationEntry(TagPrefix.stick, GTMaterials.Bronze));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_steel_firebox", GTBlocks.FIREBOX_STEEL.asStack(2), "PSP", "SFS", "PSP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Steel), 'F', new UnificationEntry(TagPrefix.frameGt, GTMaterials.Steel), 'S', new UnificationEntry(TagPrefix.stick, GTMaterials.Steel));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_titanium_firebox", GTBlocks.FIREBOX_TITANIUM.asStack(2), "PSP", "SFS", "PSP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Titanium), 'F', new UnificationEntry(TagPrefix.frameGt, GTMaterials.Titanium), 'S', new UnificationEntry(TagPrefix.stick, GTMaterials.Titanium));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_tungstensteel_firebox", GTBlocks.FIREBOX_TUNGSTENSTEEL.asStack(2), "PSP", "SFS", "PSP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.TungstenSteel), 'F', new UnificationEntry(TagPrefix.frameGt, GTMaterials.TungstenSteel), 'S', new UnificationEntry(TagPrefix.stick, GTMaterials.TungstenSteel));

        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_bronze_gearbox", GTBlocks.CASING_BRONZE_GEARBOX.asStack(2), "PhP", "GFG", "PwP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Bronze), 'F', new UnificationEntry(TagPrefix.frameGt, GTMaterials.Bronze), 'G', new UnificationEntry(TagPrefix.gear, GTMaterials.Bronze));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_steel_gearbox", GTBlocks.CASING_STEEL_GEARBOX.asStack(2), "PhP", "GFG", "PwP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Steel), 'F', new UnificationEntry(TagPrefix.frameGt, GTMaterials.Steel), 'G', new UnificationEntry(TagPrefix.gear, GTMaterials.Steel));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_stainless_steel_gearbox", GTBlocks.CASING_STAINLESS_STEEL_GEARBOX.asStack(2), "PhP", "GFG", "PwP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.StainlessSteel), 'F', new UnificationEntry(TagPrefix.frameGt, GTMaterials.StainlessSteel), 'G', new UnificationEntry(TagPrefix.gear, GTMaterials.StainlessSteel));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_titanium_gearbox", GTBlocks.CASING_TITANIUM_GEARBOX.asStack(2), "PhP", "GFG", "PwP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Titanium), 'F', new UnificationEntry(TagPrefix.frameGt, GTMaterials.Titanium), 'G', new UnificationEntry(TagPrefix.gear, GTMaterials.Titanium));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_tungstensteel_gearbox", GTBlocks.CASING_TUNGSTENSTEEL_GEARBOX.asStack(2), "PhP", "GFG", "PwP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.TungstenSteel), 'F', new UnificationEntry(TagPrefix.frameGt, GTMaterials.TungstenSteel), 'G', new UnificationEntry(TagPrefix.gear, GTMaterials.TungstenSteel));

        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_grate_casing", GTBlocks.CASING_GRATE.asStack(2), "PVP", "PFP", "PMP", 'P', new ItemStack(Blocks.IRON_BARS, 1), 'F', new UnificationEntry(TagPrefix.frameGt, GTMaterials.Steel), 'M', GTItems.ELECTRIC_MOTOR_MV, 'V', new UnificationEntry(TagPrefix.rotor, GTMaterials.Steel));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_assembly_control", GTBlocks.CASING_ASSEMBLY_CONTROL.asStack(2), "CPC", "SFE", "CMC", 'C', new UnificationEntry(TagPrefix.circuit, Tier.EV), 'P', GTItems.HIGH_POWER_INTEGRATED_CIRCUIT, 'S', GTItems.SENSOR_IV.asStack(), 'F', new UnificationEntry(TagPrefix.frameGt, GTMaterials.TungstenSteel), 'E', GTItems.EMITTER_IV.asStack(), 'M', GTItems.ELECTRIC_MOTOR_IV);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_assembly_line", GTBlocks.CASING_ASSEMBLY_LINE_GRATE.asStack(2), "PGP", "AFA", "PGP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Steel), 'G', new UnificationEntry(TagPrefix.gear, GTMaterials.Ruridit), 'A', GTItems.ROBOT_ARM_IV.asStack(), 'F', ChemicalHelper.get(TagPrefix.frameGt, GTMaterials.TungstenSteel));

        // TODO Hazard sign blocks
        /*
        VanillaRecipeHelper.addShapedRecipe(provider, true, "warning_sign_yellow_stripes", MetaBlocks.WARNING_SIGN.getItemVariant(YELLOW_STRIPES), "Y  ", " M ", "  B", 'M', MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");
        VanillaRecipeHelper.addShapedRecipe(provider, true, "warning_sign_small_yellow_stripes", MetaBlocks.WARNING_SIGN.getItemVariant(SMALL_YELLOW_STRIPES), "  Y", " M ", "B  ", 'M', MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");
        VanillaRecipeHelper.addShapedRecipe(provider, true, "warning_sign_radioactive_hazard", MetaBlocks.WARNING_SIGN.getItemVariant(RADIOACTIVE_HAZARD), " YB", " M ", "   ", 'M', MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");
        VanillaRecipeHelper.addShapedRecipe(provider, true, "warning_sign_bio_hazard", MetaBlocks.WARNING_SIGN.getItemVariant(BIO_HAZARD), " Y ", " MB", "   ", 'M', MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");
        VanillaRecipeHelper.addShapedRecipe(provider, true, "warning_sign_explosion_hazard", MetaBlocks.WARNING_SIGN.getItemVariant(EXPLOSION_HAZARD), " Y ", " M ", "  B", 'M', MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");
        VanillaRecipeHelper.addShapedRecipe(provider, true, "warning_sign_fire_hazard", MetaBlocks.WARNING_SIGN.getItemVariant(FIRE_HAZARD), " Y ", " M ", " B ", 'M', MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");
        VanillaRecipeHelper.addShapedRecipe(provider, true, "warning_sign_acid_hazard", MetaBlocks.WARNING_SIGN.getItemVariant(ACID_HAZARD), " Y ", " M ", "B  ", 'M', MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");
        VanillaRecipeHelper.addShapedRecipe(provider, true, "warning_sign_magic_hazard", MetaBlocks.WARNING_SIGN.getItemVariant(MAGIC_HAZARD), " Y ", "BM ", "   ", 'M', MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");
        VanillaRecipeHelper.addShapedRecipe(provider, true, "warning_sign_frost_hazard", MetaBlocks.WARNING_SIGN.getItemVariant(FROST_HAZARD), "BY ", " M ", "   ", 'M', MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");
        VanillaRecipeHelper.addShapedRecipe(provider, true, "warning_sign_noise_hazard", MetaBlocks.WARNING_SIGN.getItemVariant(NOISE_HAZARD), "   ", " M ", "BY ", 'M', MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");
        VanillaRecipeHelper.addShapedRecipe(provider, true, "warning_sign_generic_hazard", MetaBlocks.WARNING_SIGN.getItemVariant(GENERIC_HAZARD), "   ", "BM ", " Y ", 'M', MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");
        VanillaRecipeHelper.addShapedRecipe(provider, true, "warning_sign_high_voltage_hazard", MetaBlocks.WARNING_SIGN.getItemVariant(HIGH_VOLTAGE_HAZARD), "B  ", " M ", " Y ", 'M', MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");
        VanillaRecipeHelper.addShapedRecipe(provider, true, "warning_sign_magnetic_hazard", MetaBlocks.WARNING_SIGN.getItemVariant(MAGNETIC_HAZARD), " B ", " M ", " Y ", 'M', MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");
        VanillaRecipeHelper.addShapedRecipe(provider, true, "warning_sign_antimatter_hazard", MetaBlocks.WARNING_SIGN.getItemVariant(ANTIMATTER_HAZARD), "  B", " M ", " Y ", 'M', MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");
        VanillaRecipeHelper.addShapedRecipe(provider, true, "warning_sign_high_temperature_hazard", MetaBlocks.WARNING_SIGN.getItemVariant(HIGH_TEMPERATURE_HAZARD), "   ", " MB", " Y ", 'M', MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");
        VanillaRecipeHelper.addShapedRecipe(provider, true, "warning_sign_void_hazard", MetaBlocks.WARNING_SIGN.getItemVariant(VOID_HAZARD), "   ", " M ", " YB", 'M', MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");

        VanillaRecipeHelper.addShapedRecipe(provider, true, "warning_mob_spawner_hazard", MetaBlocks.WARNING_SIGN_1.getItemVariant(MOB_SPAWNER_HAZARD), "B  ", "YM ", "   ", 'M', MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");
        VanillaRecipeHelper.addShapedRecipe(provider, true, "warning_spatial_storage_hazard", MetaBlocks.WARNING_SIGN_1.getItemVariant(SPATIAL_STORAGE_HAZARD), " B ", "YM ", "   ", 'M', MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");
        VanillaRecipeHelper.addShapedRecipe(provider, true, "warning_laser_hazard", MetaBlocks.WARNING_SIGN_1.getItemVariant(LASER_HAZARD), "  B", "YM ", "   ", 'M', MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");
        VanillaRecipeHelper.addShapedRecipe(provider, true, "warning_mob_hazard", MetaBlocks.WARNING_SIGN_1.getItemVariant(MOB_HAZARD), "   ", "YMB", "   ", 'M', MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");
        VanillaRecipeHelper.addShapedRecipe(provider, true, "warning_boss_hazard", MetaBlocks.WARNING_SIGN_1.getItemVariant(BOSS_HAZARD), "   ", "YM ", "  B", 'M', MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");
        VanillaRecipeHelper.addShapedRecipe(provider, true, "warning_gregification_hazard", MetaBlocks.WARNING_SIGN_1.getItemVariant(GREGIFICATION_HAZARD), "   ", "YM ", " B ", 'M', MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");
        VanillaRecipeHelper.addShapedRecipe(provider, true, "warning_causality_hazard", MetaBlocks.WARNING_SIGN_1.getItemVariant(CAUSALITY_HAZARD), "   ", "YM ", "B  ", 'M', MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");
        VanillaRecipeHelper.addShapedRecipe(provider, true, "warning_automated_defenses_hazard", MetaBlocks.WARNING_SIGN_1.getItemVariant(AUTOMATED_DEFENSES_HAZARD), "   ", " MY", "  B", 'M', MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");
        VanillaRecipeHelper.addShapedRecipe(provider, true, "warning_high_pressure_hazard", MetaBlocks.WARNING_SIGN_1.getItemVariant(HIGH_PRESSURE_HAZARD), "   ", " MY", " B ", 'M', MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'Y', "dyeYellow", 'B', "dyeBlack");

        VanillaRecipeHelper.addShapelessRecipe("yellow_stripes_to_steel_solid_casing", MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), MetaBlocks.WARNING_SIGN.getItemVariant(YELLOW_STRIPES));
        VanillaRecipeHelper.addShapelessRecipe("small_yellow_stripes_to_steel_solid_casing", MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), MetaBlocks.WARNING_SIGN.getItemVariant(SMALL_YELLOW_STRIPES));
        VanillaRecipeHelper.addShapelessRecipe("radioactive_hazard_to_steel_solid_casing", MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), MetaBlocks.WARNING_SIGN.getItemVariant(RADIOACTIVE_HAZARD));
        VanillaRecipeHelper.addShapelessRecipe("bio_hazard_to_steel_solid_casing", MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), MetaBlocks.WARNING_SIGN.getItemVariant(BIO_HAZARD));
        VanillaRecipeHelper.addShapelessRecipe("explosion_hazard_to_steel_solid_casing", MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), MetaBlocks.WARNING_SIGN.getItemVariant(EXPLOSION_HAZARD));
        VanillaRecipeHelper.addShapelessRecipe("fire_hazard_to_steel_solid_casing", MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), MetaBlocks.WARNING_SIGN.getItemVariant(FIRE_HAZARD));
        VanillaRecipeHelper.addShapelessRecipe("acid_hazard_to_steel_solid_casing", MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), MetaBlocks.WARNING_SIGN.getItemVariant(ACID_HAZARD));
        VanillaRecipeHelper.addShapelessRecipe("magic_hazard_to_steel_solid_casing", MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), MetaBlocks.WARNING_SIGN.getItemVariant(MAGIC_HAZARD));
        VanillaRecipeHelper.addShapelessRecipe("frost_hazard_to_steel_solid_casing", MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), MetaBlocks.WARNING_SIGN.getItemVariant(FROST_HAZARD));
        VanillaRecipeHelper.addShapelessRecipe("noise_hazard_to_steel_solid_casing", MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), MetaBlocks.WARNING_SIGN.getItemVariant(NOISE_HAZARD));
        */

        // TODO Multi-fluid hatches
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "fluid_import_hatch_4x", GTMachines.MULTI_FLUID_IMPORT_HATCH[0].getStackForm(), "P", "M", 'M', GTMachines.HULL[GTValues.HV].getStackForm(), 'P', new UnificationEntry(TagPrefix.pipeQuadrupleFluid, GTMaterials.Titanium));
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "fluid_import_hatch_9x", GTMachines.MULTI_FLUID_IMPORT_HATCH[1].getStackForm(), "P", "M", 'M', GTMachines.HULL[GTValues.IV].getStackForm(), 'P', new UnificationEntry(TagPrefix.pipeNonupleFluid, GTMaterials.TungstenSteel));
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "fluid_export_hatch_4x", GTMachines.MULTI_FLUID_EXPORT_HATCH[0].getStackForm(), "M", "P", 'M', GTMachines.HULL[GTValues.HV].getStackForm(), 'P', new UnificationEntry(TagPrefix.pipeQuadrupleFluid, GTMaterials.Titanium));
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "fluid_export_hatch_9x", GTMachines.MULTI_FLUID_EXPORT_HATCH[1].getStackForm(), "M", "P", 'M', GTMachines.HULL[GTValues.IV].getStackForm(), 'P', new UnificationEntry(TagPrefix.pipeNonupleFluid, GTMaterials.TungstenSteel));

        // TODO rotor holders
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "rotor_holder_hv", GTMachines.ROTOR_HOLDER[0].getStackForm(), "SGS", "GHG", "SGS", 'H', GTMachines.HULL[GTValues.HV].getStackForm(), 'G', new UnificationEntry(TagPrefix.gear, GTMaterials.BlackSteel), 'S', new UnificationEntry(TagPrefix.gearSmall, GTMaterials.StainlessSteel));
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "rotor_holder_ev", GTMachines.ROTOR_HOLDER[1].getStackForm(), "SGS", "GHG", "SGS", 'H', GTMachines.HULL[GTValues.EV].getStackForm(), 'G', new UnificationEntry(TagPrefix.gear, GTMaterials.Ultimet), 'S', new UnificationEntry(TagPrefix.gearSmall, GTMaterials.Titanium));
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "rotor_holder_iv", GTMachines.ROTOR_HOLDER[2].getStackForm(), "SGS", "GHG", "SGS", 'H', GTMachines.HULL[GTValues.IV].getStackForm(), 'G', new UnificationEntry(TagPrefix.gear, GTMaterials.HSSG), 'S', new UnificationEntry(TagPrefix.gearSmall, GTMaterials.TungstenSteel));
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "rotor_holder_luv", GTMachines.ROTOR_HOLDER[3].getStackForm(), "SGS", "GHG", "SGS", 'H', GTMachines.HULL[GTValues.LuV].getStackForm(), 'G', new UnificationEntry(TagPrefix.gear, GTMaterials.Ruthenium), 'S', new UnificationEntry(TagPrefix.gearSmall, GTMaterials.RhodiumPlatedPalladium));
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "rotor_holder_zpm", GTMachines.ROTOR_HOLDER[4].getStackForm(), "SGS", "GHG", "SGS", 'H', GTMachines.HULL[GTValues.ZPM].getStackForm(), 'G', new UnificationEntry(TagPrefix.gear, GTMaterials.Trinium), 'S', new UnificationEntry(TagPrefix.gearSmall, GTMaterials.NaquadahAlloy));
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "rotor_holder_uv", GTMachines.ROTOR_HOLDER[5].getStackForm(), "SGS", "GHG", "SGS", 'H', GTMachines.HULL[GTValues.UV].getStackForm(), 'G', new UnificationEntry(TagPrefix.gear, GTMaterials.Tritanium), 'S', new UnificationEntry(TagPrefix.gearSmall, GTMaterials.Darmstadtium));

        // TODO Maintenance hatches
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "maintenance_hatch", GTMachines.MAINTENANCE_HATCH.getStackForm(), "dwx", "hHc", "fsr", 'H', GTMachines.HULL[GTValues.LV].getStackForm());
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "maintenance_hatch_configurable", GTMachines.CONFIGURABLE_MAINTENANCE_HATCH.getStackForm(), "   ", "CMC", "VHV", 'C', CIRCUIT.getIngredient(GTValues.HV), 'M', GTMachines.MAINTENANCE_HATCH.getStackForm(), 'V', CONVEYOR.getIngredient(GTValues.HV), 'H', GTMachines.HULL[GTValues.HV].getStackForm());
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "maintenance_hatch_automatic", GTMachines.AUTO_MAINTENANCE_HATCH.getStackForm(), "CMC", "RHR", "CMC", 'C', CIRCUIT.getIngredient(GTValues.HV), 'M', GTMachines.MAINTENANCE_HATCH.getStackForm(), 'R', ROBOT_ARM.getIngredient(GTValues.HV), 'H', GTMachines.HULL[GTValues.HV].getStackForm());
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "maintenance_hatch_cleaning", GTMachines.CLEANING_MAINTENANCE_HATCH.getStackForm(), "CMC", "RHR", "WCW", 'C', CIRCUIT.getIngredient(GTValues.UV), 'M', GTMachines.AUTO_MAINTENANCE_HATCH.getStackForm(), 'R', ROBOT_ARM.getIngredient(GTValues.UV), 'H', GTMachines.HULL[GTValues.UV].getStackForm(), 'W', new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.YttriumBariumCuprate));

        // TODO Processing array
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "machine_access_interface", GTMachines.MACHINE_HATCH.getStackForm(), "CHS", 'C', new UnificationEntry(TagPrefix.circuit, Tier.IV), 'H', GTMachines.HULL[GTValues.IV].getStackForm(), 'S', MetaItems.SENSOR_IV.getStackForm());

        // TODO Cleanroom
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "passthrough_hatch_item", GTMachines.PASSTHROUGH_HATCH_ITEM.getStackForm(), " C ", "GHG", " S ", 'C', MetaItems.CONVEYOR_MODULE_HV.getStackForm(), 'G', new UnificationEntry(TagPrefix.gearSmall, GTMaterials.Steel), 'H', GTMachines.HULL[GTValues.HV].getStackForm(), 'S', OreDictNames.chestWood);
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "passthrough_hatch_fluid", GTMachines.PASSTHROUGH_HATCH_FLUID.getStackForm(), " C ", "GHG", " S ", 'C', MetaItems.ELECTRIC_PUMP_HV.getStackForm(), 'G', new UnificationEntry(TagPrefix.pipeSmallFluid, GTMaterials.Steel), 'H', GTMachines.HULL[GTValues.HV].getStackForm(), 'S', MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockGlassCasing.CasingType.TEMPERED_GLASS));

        // TODO Charcoal pile igniter
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "charcoal_pile_igniter", GTMachines.CHARCOAL_PILE_IGNITER.getStackForm(), "ERE", "EHE", "FFF", 'E', new UnificationEntry(TagPrefix.plate, GTMaterials.Bronze), 'R', new UnificationEntry(TagPrefix.rotor, GTMaterials.Iron), 'H', MetaBlocks.STEAM_CASING.getItemVariant(BRONZE_BRICKS_HULL), 'F', new ItemStack(Items.FLINT));

        // STEAM MACHINES
        VanillaRecipeHelper.addShapedRecipe(provider, true, "bronze_hull", GTBlocks.BRONZE_HULL.asStack(), "PPP", "PhP", "PPP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Bronze));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "bronze_bricks_hull", GTBlocks.BRONZE_BRICKS_HULL.asStack(), "PPP", "PhP", "BBB", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Bronze), 'B', new ItemStack(Blocks.BRICKS));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "steel_hull", GTBlocks.STEEL_HULL.asStack(), "PPP", "PhP", "PPP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Steel));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "steel_bricks_hull", GTBlocks.STEEL_BRICKS_HULL.asStack(), "PPP", "PhP", "BBB", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.WroughtIron), 'B', new ItemStack(Blocks.BRICKS));

        VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_boiler_coal_bronze", GTMachines.STEAM_SOLID_BOILER.left().asStack(), "PPP", "PwP", "BFB", 'F', Blocks.FURNACE, 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Bronze), 'B', new ItemStack(Blocks.BRICKS));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_boiler_coal_steel", GTMachines.STEAM_SOLID_BOILER.right().asStack(), "PPP", "PwP", "BFB", 'F', Blocks.FURNACE, 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Steel), 'B', new ItemStack(Blocks.BRICKS));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_boiler_lava_bronze", GTMachines.STEAM_LIQUID_BOILER.left().asStack(), "PPP", "PGP", "PMP", 'M', GTBlocks.BRONZE_BRICKS_HULL.asStack(), 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Bronze), 'G', new ItemStack(Blocks.GLASS, 1));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_boiler_lava_steel", GTMachines.STEAM_LIQUID_BOILER.right().asStack(), "PPP", "PGP", "PMP", 'M', GTBlocks.STEEL_BRICKS_HULL.asStack(), 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Steel), 'G', new ItemStack(Blocks.GLASS, 1));
        // TODO Steam solar boiler
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_boiler_solar_bronze", GTMachines.STEAM_BOILER_SOLAR_BRONZE.getStackForm(), "GGG", "SSS", "PMP", 'M', MetaBlocks.STEAM_CASING.getItemVariant(BRONZE_BRICKS_HULL), 'P', new UnificationEntry(TagPrefix.pipeSmallFluid, GTMaterials.Bronze), 'S', new UnificationEntry(TagPrefix.plate, GTMaterials.Silver), 'G', new ItemStack(Blocks.GLASS));
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_boiler_solar_steel", GTMachines.STEAM_BOILER_SOLAR_STEEL.getStackForm(), "GGG", "SSS", "PMP", 'M', MetaBlocks.STEAM_CASING.getItemVariant(STEEL_BRICKS_HULL), 'P', new UnificationEntry(TagPrefix.pipeSmallFluid, GTMaterials.Steel), 'S', new UnificationEntry(TagPrefix.plateDouble, GTMaterials.Silver), 'G', new ItemStack(Blocks.GLASS));

        VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_furnace_bronze", GTMachines.STEAM_FURNACE.left().asStack(), "XXX", "XMX", "XFX", 'M', GTBlocks.BRONZE_BRICKS_HULL.asStack(), 'X', new UnificationEntry(TagPrefix.pipeSmallFluid, GTMaterials.Bronze), 'F', Blocks.FURNACE);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_furnace_steel", GTMachines.STEAM_FURNACE.right().asStack(), "XSX", "PMP", "XXX", 'M', GTMachines.STEAM_FURNACE.left().asStack(), 'X', new UnificationEntry(TagPrefix.pipeSmallFluid, GTMaterials.TinAlloy), 'S', new UnificationEntry(TagPrefix.plate, GTMaterials.Steel), 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.WroughtIron));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_macerator_bronze", GTMachines.STEAM_MACERATOR.left().asStack(), "DXD", "XMX", "PXP", 'M', GTBlocks.BRONZE_HULL.asStack(), 'X', new UnificationEntry(TagPrefix.pipeSmallFluid, GTMaterials.Bronze), 'P', LoaderTags.TAG_PISTONS, 'D', new UnificationEntry(TagPrefix.gem, GTMaterials.Diamond));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_macerator_steel", GTMachines.STEAM_MACERATOR.right().asStack(), "WSW", "PMP", "WWW", 'M', GTMachines.STEAM_MACERATOR.left().asStack(), 'W', new UnificationEntry(TagPrefix.plate, GTMaterials.WroughtIron), 'S', new UnificationEntry(TagPrefix.plate, GTMaterials.Steel), 'P', new UnificationEntry(TagPrefix.pipeSmallFluid, GTMaterials.TinAlloy));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_extractor_bronze", GTMachines.STEAM_EXTRACTOR.left().asStack(), "XXX", "PMG", "XXX", 'M', GTBlocks.BRONZE_HULL.asStack(), 'X', new UnificationEntry(TagPrefix.pipeSmallFluid, GTMaterials.Bronze), 'P', LoaderTags.TAG_PISTONS, 'G', new ItemStack(Blocks.GLASS));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_extractor_steel", GTMachines.STEAM_EXTRACTOR.right().asStack(), "PSP", "WMW", "PPP", 'M', GTMachines.STEAM_EXTRACTOR.left().asStack(), 'P', new UnificationEntry(TagPrefix.pipeSmallFluid, GTMaterials.TinAlloy), 'S', new UnificationEntry(TagPrefix.plate, GTMaterials.Steel), 'W', new UnificationEntry(TagPrefix.plate, GTMaterials.WroughtIron));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_hammer_bronze", GTMachines.STEAM_HAMMER.left().asStack(), "XPX", "XMX", "XAX", 'M', GTBlocks.BRONZE_HULL.asStack(), 'X', new UnificationEntry(TagPrefix.pipeSmallFluid, GTMaterials.Bronze), 'P', LoaderTags.TAG_PISTONS, 'A', Blocks.ANVIL);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_hammer_steel", GTMachines.STEAM_HAMMER.right().asStack(), "WSW", "PMP", "WWW", 'M', GTMachines.STEAM_HAMMER.left().asStack(), 'S', new UnificationEntry(TagPrefix.plate, GTMaterials.Steel), 'W', new UnificationEntry(TagPrefix.plate, GTMaterials.WroughtIron), 'P', new UnificationEntry(TagPrefix.pipeSmallFluid, GTMaterials.TinAlloy));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_compressor_bronze", GTMachines.STEAM_COMPRESSOR.left().asStack(), "XXX", "PMP", "XXX", 'M', GTBlocks.BRONZE_HULL.asStack(), 'X', new UnificationEntry(TagPrefix.pipeSmallFluid, GTMaterials.Bronze), 'P', LoaderTags.TAG_PISTONS);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_compressor_steel", GTMachines.STEAM_COMPRESSOR.right().asStack(), "PSP", "WMW", "PPP", 'M', GTMachines.STEAM_COMPRESSOR.left().asStack(), 'S', new UnificationEntry(TagPrefix.plate, GTMaterials.Steel), 'W', new UnificationEntry(TagPrefix.plate, GTMaterials.WroughtIron), 'P', new UnificationEntry(TagPrefix.pipeSmallFluid, GTMaterials.TinAlloy));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_alloy_smelter_bronze", GTMachines.STEAM_ALLOY_SMELTER.left().asStack(), "XXX", "FMF", "XXX", 'M', GTBlocks.BRONZE_BRICKS_HULL.asStack(), 'X', new UnificationEntry(TagPrefix.pipeSmallFluid, GTMaterials.Bronze), 'F', Blocks.FURNACE);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_alloy_smelter_steel", GTMachines.STEAM_ALLOY_SMELTER.right().asStack(), "WSW", "WMW", "WPW", 'M', GTMachines.STEAM_ALLOY_SMELTER.left().asStack(), 'S', new UnificationEntry(TagPrefix.plate, GTMaterials.Steel), 'W', new UnificationEntry(TagPrefix.plate, GTMaterials.WroughtIron), 'P', new UnificationEntry(TagPrefix.pipeSmallFluid, GTMaterials.TinAlloy));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_rock_breaker_bronze", GTMachines.STEAM_ROCK_CRUSHER.left().asStack(), "PXP", "XMX", "DXD", 'M', GTBlocks.BRONZE_HULL.asStack(), 'X', new UnificationEntry(TagPrefix.pipeSmallFluid, GTMaterials.Bronze), 'P', LoaderTags.TAG_PISTONS, 'D', new UnificationEntry(TagPrefix.gem, GTMaterials.Diamond));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_rock_breaker_steel", GTMachines.STEAM_ROCK_CRUSHER.right().asStack(), "WSW", "PMP", "WWW", 'M', GTMachines.STEAM_ROCK_CRUSHER.left().asStack(), 'W', new UnificationEntry(TagPrefix.plate, GTMaterials.WroughtIron), 'S', new UnificationEntry(TagPrefix.plate, GTMaterials.Steel), 'P', new UnificationEntry(TagPrefix.pipeSmallFluid, GTMaterials.TinAlloy));
        // TODO steam miner
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_miner", GTMachines.STEAM_MINER.getStackForm(), "DSD", "SMS", "GSG", 'M', MetaBlocks.STEAM_CASING.getItemVariant(BRONZE_HULL), 'S', new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.Bronze), 'D', new UnificationEntry(TagPrefix.gem, GTMaterials.Diamond), 'G', new UnificationEntry(TagPrefix.gearSmall, GTMaterials.Bronze));

        // MULTI BLOCK CONTROLLERS
        VanillaRecipeHelper.addShapedRecipe(provider, true, "bronze_primitive_blast_furnace", GTMachines.PRIMITIVE_BLAST_FURNACE.asStack(), "hRS", "PBR", "dRS", 'R', new UnificationEntry(TagPrefix.stick, GTMaterials.Iron), 'S', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron), 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Iron), 'B', GTBlocks.CASING_PRIMITIVE_BRICKS.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "coke_oven", GTMachines.COKE_OVEN.asStack(), "PIP", "IwI", "PIP", 'P', GTBlocks.CASING_COKE_BRICKS.asStack(), 'I', new UnificationEntry(TagPrefix.plate, GTMaterials.Iron));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "coke_oven_hatch", GTMachines.COKE_OVEN_HATCH.asStack(), "CD", 'C', GTBlocks.CASING_COKE_BRICKS.asStack(), 'D', GTMachines.WOODEN_DRUM.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "electric_blast_furnace", GTMachines.ELECTRIC_BLAST_FURNACE.asStack(), "FFF", "CMC", "WCW", 'M', GTBlocks.CASING_INVAR_HEATPROOF.asStack(), 'F', Blocks.FURNACE.asItem(), 'C', new UnificationEntry(TagPrefix.circuit, Tier.LV), 'W', new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Tin));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "vacuum_freezer", GTMachines.VACUUM_FREEZER.asStack(), "PPP", "CMC", "WCW", 'M', GTBlocks.CASING_ALUMINIUM_FROSTPROOF.asStack(), 'P', GTItems.ELECTRIC_PUMP_HV, 'C', new UnificationEntry(TagPrefix.circuit, Tier.EV), 'W', new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Gold));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "implosion_compressor", GTMachines.IMPLOSION_COMPRESSOR.asStack(), "OOO", "CMC", "WCW", 'M', GTBlocks.CASING_STEEL_SOLID.asStack(), 'O', new UnificationEntry(TagPrefix.block, GTMaterials.Obsidian), 'C', new UnificationEntry(TagPrefix.circuit, Tier.HV), 'W', new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Gold));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "distillation_tower", GTMachines.DISTILLATION_TOWER.asStack(), "CBC", "FMF", "CBC", 'M', GTMachines.HULL[GTValues.HV].asStack(), 'B', new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.StainlessSteel), 'C', new UnificationEntry(TagPrefix.circuit, Tier.EV), 'F', GTItems.ELECTRIC_PUMP_HV);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "cracking_unit", GTMachines.CRACKER.asStack(), "CEC", "PHP", "CEC", 'C', GTBlocks.COIL_CUPRONICKEL.asStack(), 'E', GTItems.ELECTRIC_PUMP_HV.asStack(), 'P', new UnificationEntry(TagPrefix.circuit, Tier.HV), 'H', GTMachines.HULL[GTValues.HV].asStack());

        VanillaRecipeHelper.addShapedRecipe(provider, true, "pyrolyse_oven", GTMachines.PYROLYSE_OVEN.asStack(), "WEP", "EME", "WCP", 'M', GTMachines.HULL[GTValues.MV].asStack(), 'W', GTItems.ELECTRIC_PISTON_MV.asStack(), 'P', new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.Cupronickel), 'E', new UnificationEntry(TagPrefix.circuit, Tier.MV), 'C', GTItems.ELECTRIC_PUMP_MV);
        // TODO Large combustion engines
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "large_combustion_engine", GTMachines.LARGE_COMBUSTION_ENGINE.asStack(), "PCP", "EME", "GWG", 'M', GTMachines.HULL[GTValues.EV].getStackForm(), 'P', MetaItems.ELECTRIC_PISTON_EV.getStackForm(), 'E', GTItems.ELECTRIC_MOTOR_EV.asStack(), 'C', new UnificationEntry(TagPrefix.circuit, MarkerMaterials.Tier.IV), 'W', new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Aluminium), 'G', new UnificationEntry(TagPrefix.gear, GTMaterials.Titanium));
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "extreme_combustion_engine", GTMachines.EXTREME_COMBUSTION_ENGINE.getStackForm(), "PCP", "EME", "GWG", 'M', GTMachines.HULL[GTValues.IV].getStackForm(), 'P', MetaItems.ELECTRIC_PISTON_IV.getStackForm(), 'E', GTItems.ELECTRIC_MOTOR_IV.asStack(), 'C', new UnificationEntry(TagPrefix.circuit, MarkerMaterials.Tier.LuV), 'W', new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.HSSG), 'G', new UnificationEntry(TagPrefix.gear, GTMaterials.TungstenSteel));
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "engine_intake_casing", GTBlocks.CASING.get().getItemVariant(ENGINE_INTAKE_CASING, 2), "PhP", "RFR", "PwP", 'R', new UnificationEntry(TagPrefix.rotor, GTMaterials.Titanium), 'F', GTBlocks.CASING.get().getItemVariant(TITANIUM_STABLE), 'P', new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.Titanium));
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "extreme_engine_intake_casing", GTBlocks.CASING.get().getItemVariant(EXTREME_ENGINE_INTAKE_CASING, 2), "PhP", "RFR", "PwP", 'R', new UnificationEntry(TagPrefix.rotor, GTMaterials.TungstenSteel), 'F', GTBlocks.CASING.get().getItemVariant(TUNGSTENSTEEL_ROBUST), 'P', new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.TungstenSteel));
        // TODO Multi-smelter
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "multi_furnace", GTMachines.MULTI_FURNACE.getStackForm(), "PPP", "ASA", "CAC", 'P', Blocks.FURNACE, 'A', new UnificationEntry(TagPrefix.circuit, MarkerGTMaterials.Tier.HV), 'S', MetaBlocks.METAL_CASING.getItemVariant(INVAR_HEATPROOF), 'C', new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Copper));

        // TODO Multiblock turbines
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "large_steam_turbine", GTMachines.LARGE_STEAM_TURBINE.getStackForm(), "PSP", "SAS", "CSC", 'S', new UnificationEntry(TagPrefix.gear, GTMaterials.Steel), 'P', new UnificationEntry(TagPrefix.circuit, MarkerGTMaterials.Tier.HV), 'A', GTMachines.HULL[GTValues.HV].getStackForm(), 'C', new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.Steel));
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "large_gas_turbine", GTMachines.LARGE_GAS_TURBINE.getStackForm(), "PSP", "SAS", "CSC", 'S', new UnificationEntry(TagPrefix.gear, GTMaterials.StainlessSteel), 'P', new UnificationEntry(TagPrefix.circuit, MarkerGTMaterials.Tier.EV), 'A', GTMachines.HULL[GTValues.EV].getStackForm(), 'C', new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.StainlessSteel));
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "large_plasma_turbine", GTMachines.LARGE_PLASMA_TURBINE.getStackForm(), "PSP", "SAS", "CSC", 'S', new UnificationEntry(TagPrefix.gear, GTMaterials.TungstenSteel), 'P', new UnificationEntry(TagPrefix.circuit, MarkerGTMaterials.Tier.LuV), 'A', GTMachines.HULL[GTValues.LuV].getStackForm(), 'C', new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.TungstenSteel));

        VanillaRecipeHelper.addShapedRecipe(provider, true, "large_bronze_boiler", GTMachines.LARGE_BOILER_BRONZE.asStack(), "PSP", "SAS", "PSP", 'P', new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Tin), 'S', new UnificationEntry(TagPrefix.circuit, Tier.LV), 'A', GTBlocks.FIREBOX_BRONZE.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "large_steel_boiler", GTMachines.LARGE_BOILER_STEEL.asStack(), "PSP", "SAS", "PSP", 'P', new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Copper), 'S', new UnificationEntry(TagPrefix.circuit, Tier.HV), 'A', GTBlocks.FIREBOX_STEEL.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "large_titanium_boiler", GTMachines.LARGE_BOILER_TITANIUM.asStack(), "PSP", "SAS", "PSP", 'P', new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Gold), 'S', new UnificationEntry(TagPrefix.circuit, Tier.EV), 'A', GTBlocks.FIREBOX_TITANIUM.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "large_tungstensteel_boiler", GTMachines.LARGE_BOILER_TUNGSTENSTEEL.asStack(), "PSP", "SAS", "PSP", 'P', new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Aluminium), 'S', new UnificationEntry(TagPrefix.circuit, Tier.IV), 'A', GTBlocks.FIREBOX_TUNGSTENSTEEL.asStack());

        VanillaRecipeHelper.addShapedRecipe(provider, true, "assembly_line", GTMachines.ASSEMBLY_LINE.asStack(), "CRC", "SAS", "CRC", 'A', GTMachines.HULL[GTValues.IV].asStack(), 'R', GTItems.ROBOT_ARM_IV, 'C', GTBlocks.CASING_ASSEMBLY_CONTROL.asStack(), 'S', new UnificationEntry(TagPrefix.circuit, Tier.IV));

        VanillaRecipeHelper.addShapedRecipe(provider, true, "large_chemical_reactor", GTMachines.LARGE_CHEMICAL_REACTOR.asStack(), "CRC", "PMP", "CHC", 'C', new UnificationEntry(TagPrefix.circuit, Tier.HV), 'R', ChemicalHelper.get(TagPrefix.rotor, GTMaterials.StainlessSteel), 'P', ChemicalHelper.get(TagPrefix.pipeLargeFluid, GTMaterials.Polytetrafluoroethylene), 'M', GTItems.ELECTRIC_MOTOR_HV.asStack(), 'H', GTMachines.HULL[GTValues.HV].asStack());

        // TODO Steam multiblocks
        //if (ConfigHolder.machines.steelSteamMultiblocks) {
            //VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_oven", GTMachines.STEAM_OVEN.getStackForm(), "CGC", "FMF", "CGC", 'F', MetaBlocks.BOILER_FIREBOX_CASING.getItemVariant(STEEL_FIREBOX), 'C', MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID), 'M', GTMachines.STEAM_FURNACE_STEEL.getStackForm(), 'G', new UnificationEntry(TagPrefix.gear, GTMaterials.Invar));
            //VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_grinder", GTMachines.STEAM_GRINDER.getStackForm(), "CGC", "CFC", "CGC", 'G', new UnificationEntry(TagPrefix.gear, GTMaterials.Potin), 'F', GTMachines.STEAM_MACERATOR_STEEL.getStackForm(), 'C', MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID));
            //VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_hatch", GTMachines.STEAM_HATCH.asStack(), "BPB", "BTB", "BPB", 'B', new UnificationEntry(TagPrefix.plate, GTMaterials.Steel), 'P', new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.Steel), 'T', GTMachines.STEEL_DRUM.getStackForm());
            //VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_input_bus", GTMachines.STEAM_IMPORT_BUS.getStackForm(), "C", "H", 'H', MetaBlocks.STEAM_CASING.getItemVariant(STEEL_HULL), 'C', OreDictNames.chestWood);
            //VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_output_bus", GTMachines.STEAM_EXPORT_BUS.getStackForm(), "H", "C", 'H', MetaBlocks.STEAM_CASING.getItemVariant(STEEL_HULL), 'C', OreDictNames.chestWood);
        //} else {
            //VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_oven", GTMachines.STEAM_OVEN.getStackForm(), "CGC", "FMF", "CGC", 'F', MetaBlocks.BOILER_FIREBOX_CASING.getItemVariant(BRONZE_FIREBOX), 'C', MetaBlocks.METAL_CASING.getItemVariant(BRONZE_BRICKS), 'M', GTMachines.STEAM_FURNACE_BRONZE.getStackForm(), 'G', new UnificationEntry(TagPrefix.gear, GTMaterials.Invar));
            //VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_grinder", GTMachines.STEAM_GRINDER.getStackForm(), "CGC", "CFC", "CGC", 'G', new UnificationEntry(TagPrefix.gear, GTMaterials.Potin), 'F', GTMachines.STEAM_MACERATOR_BRONZE.getStackForm(), 'C', MetaBlocks.METAL_CASING.getItemVariant(BRONZE_BRICKS));
            //VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_hatch", GTMachines.STEAM_HATCH.asStack(), "BPB", "BTB", "BPB", 'B', new UnificationEntry(TagPrefix.plate, GTMaterials.Bronze), 'P', new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.Bronze), 'T', GTMachines.BRONZE_DRUM.getStackForm());
            //VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_input_bus", GTMachines.STEAM_IMPORT_BUS.getStackForm(), "C", "H", 'H', MetaBlocks.STEAM_CASING.getItemVariant(BRONZE_HULL), 'C', OreDictNames.chestWood);
            //VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_output_bus", GTMachines.STEAM_EXPORT_BUS.getStackForm(), "H", "C", 'H', MetaBlocks.STEAM_CASING.getItemVariant(BRONZE_HULL), 'C', OreDictNames.chestWood);
        //}

        // TODO Processing array
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "processing_array", GTMachines.PROCESSING_ARRAY.getStackForm(), "COC", "RHR", "CPC", 'C', new UnificationEntry(TagPrefix.circuit, Tier.IV), 'O', MetaItems.TOOL_DATA_ORB.getStackForm(), 'R', MetaItems.ROBOT_ARM_EV.getStackForm(), 'P', OreDictUnifier.get(TagPrefix.pipeLargeFluid, GTMaterials.StainlessSteel), 'H', GTMachines.HULL[GTValues.EV].getStackForm());
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "advanced_processing_array", GTMachines.ADVANCED_PROCESSING_ARRAY.getStackForm(), "RCR", "SPE", "HNH", 'R', MetaItems.ROBOT_ARM_LuV, 'C', new UnificationEntry(TagPrefix.circuit, Tier.ZPM), 'S', MetaItems.SENSOR_LuV, 'P', GTMachines.PROCESSING_ARRAY.getStackForm(), 'E', MetaItems.EMITTER_LuV, 'H', new UnificationEntry(TagPrefix.plate, GTMaterials.HSSE), 'N', new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.Naquadah));

        // GENERATORS
        VanillaRecipeHelper.addShapedRecipe(provider, true, "diesel_generator_lv", GTMachines.COMBUSTION[0].asStack(), "PCP", "EME", "GWG", 'M', GTMachines.HULL[GTValues.LV].asStack(), 'P', GTItems.ELECTRIC_PISTON_LV, 'E', GTItems.ELECTRIC_MOTOR_LV, 'C', new UnificationEntry(TagPrefix.circuit, Tier.LV), 'W', new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Tin), 'G', new UnificationEntry(TagPrefix.gear, GTMaterials.Steel));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "diesel_generator_mv", GTMachines.COMBUSTION[1].asStack(), "PCP", "EME", "GWG", 'M', GTMachines.HULL[GTValues.MV].asStack(), 'P', GTItems.ELECTRIC_PISTON_MV, 'E', GTItems.ELECTRIC_MOTOR_MV, 'C', new UnificationEntry(TagPrefix.circuit, Tier.MV), 'W', new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Copper), 'G', new UnificationEntry(TagPrefix.gear, GTMaterials.Aluminium));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "diesel_generator_hv", GTMachines.COMBUSTION[2].asStack(), "PCP", "EME", "GWG", 'M', GTMachines.HULL[GTValues.HV].asStack(), 'P', GTItems.ELECTRIC_PISTON_HV, 'E', GTItems.ELECTRIC_MOTOR_HV, 'C', new UnificationEntry(TagPrefix.circuit, Tier.HV), 'W', new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Gold), 'G', new UnificationEntry(TagPrefix.gear, GTMaterials.StainlessSteel));

        VanillaRecipeHelper.addShapedRecipe(provider, true, "gas_turbine_lv", GTMachines.GAS_TURBINE[0].asStack(), "CRC", "RMR", "EWE", 'M', GTMachines.HULL[GTValues.LV].asStack(), 'E', GTItems.ELECTRIC_MOTOR_LV, 'R', new UnificationEntry(TagPrefix.rotor, GTMaterials.Tin), 'C', new UnificationEntry(TagPrefix.circuit, Tier.LV), 'W', new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Tin));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "gas_turbine_mv", GTMachines.GAS_TURBINE[1].asStack(), "CRC", "RMR", "EWE", 'M', GTMachines.HULL[GTValues.MV].asStack(), 'E', GTItems.ELECTRIC_MOTOR_MV, 'R', new UnificationEntry(TagPrefix.rotor, GTMaterials.Bronze), 'C', new UnificationEntry(TagPrefix.circuit, Tier.MV), 'W', new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Copper));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "gas_turbine_hv", GTMachines.GAS_TURBINE[2].asStack(), "CRC", "RMR", "EWE", 'M', GTMachines.HULL[GTValues.HV].asStack(), 'E', GTItems.ELECTRIC_MOTOR_HV, 'R', new UnificationEntry(TagPrefix.rotor, GTMaterials.Steel), 'C', new UnificationEntry(TagPrefix.circuit, Tier.HV), 'W', new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Gold));

        VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_turbine_lv", GTMachines.STEAM_TURBINE[0].asStack(), "PCP", "RMR", "EWE", 'M', GTMachines.HULL[GTValues.LV].asStack(), 'E', GTItems.ELECTRIC_MOTOR_LV, 'R', new UnificationEntry(TagPrefix.rotor, GTMaterials.Tin), 'C', new UnificationEntry(TagPrefix.circuit, Tier.LV), 'W', new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Tin), 'P', new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.Bronze));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_turbine_mv", GTMachines.STEAM_TURBINE[1].asStack(), "PCP", "RMR", "EWE", 'M', GTMachines.HULL[GTValues.MV].asStack(), 'E', GTItems.ELECTRIC_MOTOR_MV, 'R', new UnificationEntry(TagPrefix.rotor, GTMaterials.Bronze), 'C', new UnificationEntry(TagPrefix.circuit, Tier.MV), 'W', new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Copper), 'P', new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.Steel));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_turbine_hv", GTMachines.STEAM_TURBINE[2].asStack(), "PCP", "RMR", "EWE", 'M', GTMachines.HULL[GTValues.HV].asStack(), 'E', GTItems.ELECTRIC_MOTOR_HV, 'R', new UnificationEntry(TagPrefix.rotor, GTMaterials.Steel), 'C', new UnificationEntry(TagPrefix.circuit, Tier.HV), 'W', new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Gold), 'P', new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.StainlessSteel));

        // TODO Crafting station
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "workbench_bronze", GTMachines.WORKBENCH.getStackForm(), "CSC", "PWP", "PsP", 'C', OreDictNames.chestWood, 'W', new ItemStack(Blocks.CRAFTING_TABLE), 'S', OreDictUnifier.get("slabWood"), 'P', new UnificationEntry(TagPrefix.plank, GTMaterials.Wood));

        // TODO Primitive Pump
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "primitive_pump", GTMachines.PRIMITIVE_WATER_PUMP.getStackForm(), "RGS", "OWd", "CLC", 'R', new UnificationEntry(TagPrefix.ring, GTMaterials.Iron), 'G', new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.Wood), 'S', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron), 'O', new UnificationEntry(TagPrefix.rotor, GTMaterials.Iron), 'W', MetaBlocks.PLANKS.getItemVariant(BlockGregPlanks.BlockType.TREATED_PLANK), 'C', new ItemStack(Blocks.STONE_SLAB, 1, 3), 'L', new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.Wood));
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "pump_deck", MetaBlocks.STEAM_CASING.getItemVariant(PUMP_DECK, 2), "SWS", "dCh", 'S', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron), 'W', MetaBlocks.PLANKS.getItemVariant(BlockGregPlanks.BlockType.TREATED_PLANK), 'C', new ItemStack(Blocks.STONE_SLAB, 1, 3));
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "pump_hatch", GTMachines.PUMP_OUTPUT_HATCH.getStackForm(), "SRd", "PLP", "CRC", 'S', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron), 'R', new UnificationEntry(TagPrefix.ring, GTMaterials.Iron), 'P', MetaBlocks.PLANKS.getItemVariant(BlockGregPlanks.BlockType.TREATED_PLANK), 'L', new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.Wood), 'C', new ItemStack(Blocks.STONE_SLAB, 1, 3));

        // TODO Multiblock tanks
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "wood_multiblock_tank", GTMachines.WOODEN_TANK.getStackForm(), " R ", "rCs", " R ", 'R', new UnificationEntry(TagPrefix.ring, GTMaterials.Lead), 'C', MetaBlocks.STEAM_CASING.getItemVariant(WOOD_WALL));
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "steel_multiblock_tank", GTMachines.STEEL_TANK.getStackForm(), " R ", "hCw", " R ", 'R', new UnificationEntry(TagPrefix.ring, GTMaterials.Steel), 'C', MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID));
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "wood_tank_valve", GTMachines.WOODEN_TANK_VALVE.getStackForm(), " R ", "rCs", " O ", 'O', new UnificationEntry(TagPrefix.rotor, GTMaterials.Lead), 'R', new UnificationEntry(TagPrefix.ring, GTMaterials.Lead), 'C', MetaBlocks.STEAM_CASING.getItemVariant(WOOD_WALL));
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "steel_tank_valve", GTMachines.STEEL_TANK_VALVE.getStackForm(), " R ", "hCw", " O ", 'O', new UnificationEntry(TagPrefix.rotor, GTMaterials.Steel), 'R', new UnificationEntry(TagPrefix.ring, GTMaterials.Steel), 'C', MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID));
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "wood_wall", MetaBlocks.STEAM_CASING.getItemVariant(WOOD_WALL), "W W", "sPh", "W W", 'W', MetaBlocks.PLANKS.getItemVariant(BlockGregPlanks.BlockType.TREATED_PLANK), 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Lead));

        // MACHINES
        registerMachineRecipe(provider, GTMachines.ALLOY_SMELTER, "ECE", "CMC", "WCW", 'M', HULL, 'E', CIRCUIT, 'W', CABLE, 'C', COIL_HEATING_DOUBLE);
        registerMachineRecipe(provider, GTMachines.ASSEMBLER, "ACA", "VMV", "WCW", 'M', HULL, 'V', CONVEYOR, 'A', ROBOT_ARM, 'C', CIRCUIT, 'W', CABLE);
        registerMachineRecipe(provider, GTMachines.BENDER, "PwP", "CMC", "EWE", 'M', HULL, 'E', MOTOR, 'P', PISTON, 'C', CIRCUIT, 'W', CABLE);
        registerMachineRecipe(provider, GTMachines.CANNER, "WPW", "CMC", "GGG", 'M', HULL, 'P', PUMP, 'C', CIRCUIT, 'W', CABLE, 'G', GLASS);
        registerMachineRecipe(provider, GTMachines.COMPRESSOR, " C ", "PMP", "WCW", 'M', HULL, 'P', PISTON, 'C', CIRCUIT, 'W', CABLE);
        registerMachineRecipe(provider, GTMachines.CUTTER, "WCG", "VMB", "CWE", 'M', HULL, 'E', MOTOR, 'V', CONVEYOR, 'C', CIRCUIT, 'W', CABLE, 'G', GLASS, 'B', SAWBLADE);
        registerMachineRecipe(provider, GTMachines.ELECTRIC_FURNACE, "ECE", "CMC", "WCW", 'M', HULL, 'E', CIRCUIT, 'W', CABLE, 'C', COIL_HEATING);
        registerMachineRecipe(provider, GTMachines.EXTRACTOR, "GCG", "EMP", "WCW", 'M', HULL, 'E', PISTON, 'P', PUMP, 'C', CIRCUIT, 'W', CABLE, 'G', GLASS);
        registerMachineRecipe(provider, GTMachines.EXTRUDER, "CCE", "XMP", "CCE", 'M', HULL, 'X', PISTON, 'E', CIRCUIT, 'P', PIPE_NORMAL, 'C', COIL_HEATING_DOUBLE);
        registerMachineRecipe(provider, GTMachines.LATHE, "WCW", "EMD", "CWP", 'M', HULL, 'E', MOTOR, 'P', PISTON, 'C', CIRCUIT, 'W', CABLE, 'D', GRINDER);
        registerMachineRecipe(provider, GTMachines.MACERATOR, "PEG", "WWM", "CCW", 'M', HULL, 'E', MOTOR, 'P', PISTON, 'C', CIRCUIT, 'W', CABLE, 'G', GRINDER);
        registerMachineRecipe(provider, GTMachines.WIREMILL, "EWE", "CMC", "EWE", 'M', HULL, 'E', MOTOR, 'C', CIRCUIT, 'W', CABLE);
        registerMachineRecipe(provider, GTMachines.CENTRIFUGE, "CEC", "WMW", "CEC", 'M', HULL, 'E', MOTOR, 'C', CIRCUIT, 'W', CABLE);
        registerMachineRecipe(provider, GTMachines.ELECTROLYZER, "IGI", "IMI", "CWC", 'M', HULL, 'C', CIRCUIT, 'W', CABLE, 'I', WIRE_ELECTRIC, 'G', GLASS);
        registerMachineRecipe(provider, GTMachines.THERMAL_CENTRIFUGE, "CEC", "OMO", "WEW", 'M', HULL, 'E', MOTOR, 'C', CIRCUIT, 'W', CABLE, 'O', COIL_HEATING_DOUBLE);
        registerMachineRecipe(provider, GTMachines.ORE_WASHER, "RGR", "CEC", "WMW", 'M', HULL, 'R', ROTOR, 'E', MOTOR, 'C', CIRCUIT, 'W', CABLE, 'G', GLASS);
        registerMachineRecipe(provider, GTMachines.PACKER, "BCB", "RMV", "WCW", 'M', HULL, 'R', ROBOT_ARM, 'V', CONVEYOR, 'C', CIRCUIT, 'W', CABLE, 'B', LoaderTags.TAG_WOODEN_CHESTS);
        registerMachineRecipe(provider, GTMachines.CHEMICAL_REACTOR, "GRG", "WEW", "CMC", 'M', HULL, 'R', ROTOR, 'E', MOTOR, 'C', CIRCUIT, 'W', CABLE, 'G', PIPE_REACTOR);
        registerMachineRecipe(provider, GTMachines.BREWERY, "GPG", "WMW", "CBC", 'M', HULL, 'P', PUMP, 'B', STICK_DISTILLATION, 'C', CIRCUIT, 'W', CABLE, 'G', GLASS);
        registerMachineRecipe(provider, GTMachines.FERMENTER, "WPW", "GMG", "WCW", 'M', HULL, 'P', PUMP, 'C', CIRCUIT, 'W', CABLE, 'G', GLASS);
        registerMachineRecipe(provider, GTMachines.FLUID_SOLIDIFIER, "PGP", "WMW", "CBC", 'M', HULL, 'P', PUMP, 'C', CIRCUIT, 'W', CABLE, 'G', GLASS, 'B', LoaderTags.TAG_WOODEN_CHESTS);
        registerMachineRecipe(provider, GTMachines.DISTILLERY, "GBG", "CMC", "WPW", 'M', HULL, 'P', PUMP, 'B', STICK_DISTILLATION, 'C', CIRCUIT, 'W', CABLE, 'G', GLASS);
        registerMachineRecipe(provider, GTMachines.CHEMICAL_BATH, "VGW", "PGV", "CMC", 'M', HULL, 'P', PUMP, 'V', CONVEYOR, 'C', CIRCUIT, 'W', CABLE, 'G', GLASS);
        registerMachineRecipe(provider, GTMachines.POLARIZER, "ZSZ", "WMW", "ZSZ", 'M', HULL, 'S', STICK_ELECTROMAGNETIC, 'Z', COIL_ELECTRIC, 'W', CABLE);
        registerMachineRecipe(provider, GTMachines.ELECTROMAGNETIC_SEPARATOR, "VWZ", "WMS", "CWZ", 'M', HULL, 'S', STICK_ELECTROMAGNETIC, 'Z', COIL_ELECTRIC, 'V', CONVEYOR, 'C', CIRCUIT, 'W', CABLE);
        registerMachineRecipe(provider, GTMachines.AUTOCLAVE, "IGI", "IMI", "CPC", 'M', HULL, 'P', PUMP, 'C', CIRCUIT, 'I', PLATE, 'G', GLASS);
        registerMachineRecipe(provider, GTMachines.MIXER, "GRG", "GEG", "CMC", 'M', HULL, 'E', MOTOR, 'R', ROTOR, 'C', CIRCUIT, 'G', GLASS);
        registerMachineRecipe(provider, GTMachines.LASER_ENGRAVER, "PEP", "CMC", "WCW", 'M', HULL, 'E', EMITTER, 'P', PISTON, 'C', CIRCUIT, 'W', CABLE);
        registerMachineRecipe(provider, GTMachines.FORMING_PRESS, "WPW", "CMC", "WPW", 'M', HULL, 'P', PISTON, 'C', CIRCUIT, 'W', CABLE);
        registerMachineRecipe(provider, GTMachines.FORGE_HAMMER, "WPW", "CMC", "WAW", 'M', HULL, 'P', PISTON, 'C', CIRCUIT, 'W', CABLE, 'A', Blocks.ANVIL);
        registerMachineRecipe(provider, GTMachines.FLUID_HEATER, "OGO", "PMP", "WCW", 'M', HULL, 'P', PUMP, 'O', COIL_HEATING_DOUBLE, 'C', CIRCUIT, 'W', CABLE, 'G', GLASS);
        registerMachineRecipe(provider, GTMachines.SIFTER, "WFW", "PMP", "CFC", 'M', HULL, 'P', PISTON, 'F', GTItems.ITEM_FILTER, 'C', CIRCUIT, 'W', CABLE);
        registerMachineRecipe(provider, GTMachines.ARC_FURNACE, "WGW", "CMC", "PPP", 'M', HULL, 'P', PLATE, 'C', CIRCUIT, 'W', CABLE_QUAD, 'G', new UnificationEntry(TagPrefix.dust, GTMaterials.Graphite));
        registerMachineRecipe(provider, GTMachines.CIRCUIT_ASSEMBLER, "RIE", "CHC", "WIW", 'R', ROBOT_ARM, 'I', BETTER_CIRCUIT, 'E', EMITTER, 'C', CONVEYOR, 'H', HULL, 'W', CABLE);
        // TODO Replication system
        //registerMachineRecipe(GTMachines.MASS_FABRICATOR, "CFC", "QMQ", "CFC", 'M', HULL, 'Q', CABLE_QUAD, 'C', BETTER_CIRCUIT, 'F', FIELD_GENERATOR);
        //registerMachineRecipe(GTMachines.REPLICATOR, "EFE", "CMC", "EQE", 'M', HULL, 'Q', CABLE_QUAD, 'C', BETTER_CIRCUIT, 'F', FIELD_GENERATOR, 'E', EMITTER);
        // TODO Assembly Line Research System
        //registerMachineRecipe(GTMachines.SCANNER, "CEC", "WHW", "CSC", 'C', BETTER_CIRCUIT, 'E', EMITTER, 'W', CABLE, 'H', HULL, 'S', SENSOR);
        registerMachineRecipe(provider, GTMachines.GAS_COLLECTOR, "WFW", "PHP", "WCW", 'W', Blocks.IRON_BARS, 'F', GTItems.FLUID_FILTER, 'P', PUMP, 'H', HULL, 'C', CIRCUIT);
        registerMachineRecipe(provider, GTMachines.ROCK_CRUSHER, "PMW", "CHC", "GGG", 'P', PISTON, 'M', MOTOR, 'W', GRINDER, 'C', CABLE, 'H', HULL, 'G', GLASS);
        registerMachineRecipe(provider, GTMachines.PUMP, "WGW", "GMG", "TGT", 'M', HULL, 'W', CIRCUIT, 'G', PUMP, 'T', PIPE_LARGE);
        // TODO Misc machines
        //registerMachineRecipe(provider, GTMachines.FISHER, "WTW", "PMP", "TGT", 'M', HULL, 'W', CIRCUIT, 'G', PUMP, 'T', MOTOR, 'P', PISTON);
        //registerMachineRecipe(provider, GTMachines.ITEM_COLLECTOR, "MRM", "RHR", "CWC", 'M', MOTOR, 'R', ROTOR, 'H', HULL, 'C', CIRCUIT, 'W', CABLE);
        //registerMachineRecipe(provider, GTMachines.BLOCK_BREAKER, "MGM", "CHC", "WSW", 'M', MOTOR, 'H', HULL, 'C', CIRCUIT, 'W', CABLE, 'S', OreDictNames.chestWood, 'G', GRINDER);
        //registerMachineRecipe(provider, GTMachines.WORLD_ACCELERATOR, "IGI", "FHF", "IGI", 'H', HULL, 'F', EMITTER, 'G', SENSOR, 'I', FIELD_GENERATOR);
        //registerMachineRecipe(provider, GTMachines.MINER, "MMM", "WHW", "CSC", 'M', MOTOR, 'W', CABLE, 'H', HULL, 'C', CIRCUIT, 'S', SENSOR);

        registerMachineRecipe(provider, GTMachines.MUFFLER_HATCH, "HM", "PR", 'H', HULL, 'M', MOTOR, 'P', PIPE_NORMAL, 'R', ROTOR);

        // TODO Diodes
        //registerMachineRecipe(provider, ArrayUtils.subarray(provider, GTMachines.DIODES, GTValues.ULV, GTValues.HV), "CDC", "DHD", "PDP", 'H', HULL, 'D', GTItems.DIODE, 'P', PLATE, 'C', CABLE_QUAD);
        //registerMachineRecipe(provider, ArrayUtils.subarray(provider, GTMachines.DIODES, GTValues.HV, GTValues.LuV), "CDC", "DHD", "PDP", 'H', HULL, 'D', GTItems.SMD_DIODE, 'P', PLATE, 'C', CABLE_QUAD);
        //registerMachineRecipe(provider, ArrayUtils.subarray(provider, GTMachines.DIODES, GTValues.LuV, GTMachines.DIODES.length), "CDC", "DHD", "PDP", 'H', HULL, 'D', GTItems.ADVANCED_SMD_DIODE, 'P', PLATE, 'C', CABLE_QUAD);

        registerMachineRecipe(provider, ArrayUtils.subarray(GTMachines.TRANSFORMER, GTValues.ULV, GTValues.MV), " CC", "TH ", " CC", 'C', CABLE, 'T', CABLE_TIER_UP, 'H', HULL);
        registerMachineRecipe(provider, ArrayUtils.subarray(GTMachines.TRANSFORMER, GTValues.MV, GTValues.UHV), "WCC", "TH ", "WCC", 'W', POWER_COMPONENT, 'C', CABLE, 'T', CABLE_TIER_UP, 'H', HULL);

        registerMachineRecipe(provider, GTMachines.BATTERY_BUFFER_4, "WTW", "WMW", 'M', HULL, 'W', WIRE_QUAD, 'T', LoaderTags.TAG_WOODEN_CHESTS);
        registerMachineRecipe(provider, GTMachines.BATTERY_BUFFER_8, "WTW", "WMW", 'M', HULL, 'W', WIRE_OCT, 'T', LoaderTags.TAG_WOODEN_CHESTS);
        registerMachineRecipe(provider, GTMachines.BATTERY_BUFFER_16, "WTW", "WMW", 'M', HULL, 'W', WIRE_HEX, 'T', LoaderTags.TAG_WOODEN_CHESTS);

        // TODO Charger
        //registerMachineRecipe(provider, GTMachines.CHARGER, "WTW", "WMW", "BCB", 'M', HULL, 'W', WIRE_QUAD, 'T', OreDictNames.chestWood, 'B', CABLE, 'C', CIRCUIT);

        registerMachineRecipe(provider, GTMachines.FLUID_IMPORT_HATCH, " G", " M", 'M', HULL, 'G', GLASS);
        registerMachineRecipe(provider, GTMachines.FLUID_EXPORT_HATCH, " M", " G", 'M', HULL, 'G', GLASS);

        registerMachineRecipe(provider, GTMachines.ITEM_IMPORT_BUS, " C", " M", 'M', HULL, 'C', LoaderTags.TAG_WOODEN_CHESTS);
        registerMachineRecipe(provider, GTMachines.ITEM_EXPORT_BUS, " M", " C", 'M', HULL, 'C', LoaderTags.TAG_WOODEN_CHESTS);

        VanillaRecipeHelper.addShapedRecipe(provider, true, "wooden_crate", GTMachines.WOODEN_CRATE.asStack(), "RPR", "PsP", "RPR", 'P', ItemTags.PLANKS, 'R', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "bronze_crate", GTMachines.BRONZE_CRATE.asStack(), "RPR", "PhP", "RPR", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Bronze), 'R', new UnificationEntry(TagPrefix.stickLong, GTMaterials.Bronze));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "steel_crate", GTMachines.STEEL_CRATE.asStack(), "RPR", "PhP", "RPR", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Steel), 'R', new UnificationEntry(TagPrefix.stickLong, GTMaterials.Steel));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "aluminium_crate", GTMachines.ALUMINIUM_CRATE.asStack(), "RPR", "PhP", "RPR", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Aluminium), 'R', new UnificationEntry(TagPrefix.stickLong, GTMaterials.Aluminium));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "stainless_steel_crate", GTMachines.STAINLESS_STEEL_CRATE.asStack(), "RPR", "PhP", "RPR", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.StainlessSteel), 'R', new UnificationEntry(TagPrefix.stickLong, GTMaterials.StainlessSteel));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "titanium_crate", GTMachines.TITANIUM_CRATE.asStack(), "RPR", "PhP", "RPR", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Titanium), 'R', new UnificationEntry(TagPrefix.stickLong, GTMaterials.Titanium));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "tungstensteel_crate", GTMachines.TUNGSTENSTEEL_CRATE.asStack(), "RPR", "PhP", "RPR", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.TungstenSteel), 'R', new UnificationEntry(TagPrefix.stickLong, GTMaterials.TungstenSteel));

        VanillaRecipeHelper.addShapedRecipe(provider, true, "wooden_barrel", GTMachines.WOODEN_DRUM.asStack(), "rSs", "PRP", "PRP", 'S', GTItems.STICKY_RESIN.asStack(), 'P', ItemTags.PLANKS, 'R', new UnificationEntry(TagPrefix.stickLong, GTMaterials.Iron));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "bronze_drum", GTMachines.BRONZE_DRUM.asStack(), " h ", "PRP", "PRP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Bronze), 'R', new UnificationEntry(TagPrefix.stickLong, GTMaterials.Bronze));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "steel_drum", GTMachines.STEEL_DRUM.asStack(), " h ", "PRP", "PRP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Steel), 'R', new UnificationEntry(TagPrefix.stickLong, GTMaterials.Steel));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "aluminium_drum", GTMachines.ALUMINIUM_DRUM.asStack(), " h ", "PRP", "PRP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Aluminium), 'R', new UnificationEntry(TagPrefix.stickLong, GTMaterials.Aluminium));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "stainless_steel_drum", GTMachines.STAINLESS_STEEL_DRUM.asStack(), " h ", "PRP", "PRP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.StainlessSteel), 'R', new UnificationEntry(TagPrefix.stickLong, GTMaterials.StainlessSteel));

        // Hermetic Casings
        VanillaRecipeHelper.addShapedRecipe(provider, true, "hermetic_casing_lv", GTBlocks.HERMETIC_CASING_LV.asStack(), "PPP", "PFP", "PPP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Steel), 'F', new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.Polyethylene));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "hermetic_casing_mv", GTBlocks.HERMETIC_CASING_MV.asStack(), "PPP", "PFP", "PPP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Aluminium), 'F', new UnificationEntry(TagPrefix.pipeLargeItem, GTMaterials.PolyvinylChloride));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "hermetic_casing_hv", GTBlocks.HERMETIC_CASING_HV.asStack(), "PPP", "PFP", "PPP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.StainlessSteel), 'F', new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.Polytetrafluoroethylene));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "hermetic_casing_ev", GTBlocks.HERMETIC_CASING_EV.asStack(), "PPP", "PFP", "PPP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Titanium), 'F', new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.StainlessSteel));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "hermetic_casing_iv", GTBlocks.HERMETIC_CASING_IV.asStack(), "PPP", "PFP", "PPP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.TungstenSteel), 'F', new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.Titanium));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "hermetic_casing_luv", GTBlocks.HERMETIC_CASING_LuV.asStack(), "PPP", "PFP", "PPP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.RhodiumPlatedPalladium), 'F', new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.TungstenSteel));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "hermetic_casing_zpm", GTBlocks.HERMETIC_CASING_ZPM.asStack(), "PPP", "PFP", "PPP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.NaquadahAlloy), 'F', new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.NiobiumTitanium));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "hermetic_casing_uv", GTBlocks.HERMETIC_CASING_UV.asStack(), "PPP", "PFP", "PPP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Darmstadtium), 'F', new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.Naquadah));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "hermetic_casing_max", GTBlocks.HERMETIC_CASING_UHV.asStack(), "PPP", "PFP", "PPP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Neutronium), 'F', new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.Duranium));

        // Super / Quantum Chests
        VanillaRecipeHelper.addShapedRecipe(provider, true, "super_chest_lv", GTMachines.SUPER_CHEST[0].asStack(), "CPC", "PFP", "CPC", 'C', new UnificationEntry(TagPrefix.circuit, Tier.LV), 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Steel), 'F', GTMachines.STEEL_CRATE.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "super_chest_mv", GTMachines.SUPER_CHEST[1].asStack(), "CPC", "PFP", "CPC", 'C', new UnificationEntry(TagPrefix.circuit, Tier.MV), 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Aluminium), 'F', GTMachines.ALUMINIUM_CRATE.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "super_chest_hv", GTMachines.SUPER_CHEST[2].asStack(), "CPC", "PFP", "CGC", 'C', new UnificationEntry(TagPrefix.circuit, Tier.HV), 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.StainlessSteel), 'F', GTMachines.STAINLESS_STEEL_CRATE.asStack(), 'G', GTItems.FIELD_GENERATOR_LV.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "super_chest_ev", GTMachines.SUPER_CHEST[3].asStack(), "CPC", "PFP", "CGC", 'C', new UnificationEntry(TagPrefix.circuit, Tier.EV), 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Titanium), 'F', GTMachines.TITANIUM_CRATE.asStack(), 'G', GTItems.FIELD_GENERATOR_MV.asStack());

        VanillaRecipeHelper.addShapedRecipe(provider, true, "quantum_chest_iv", GTMachines.QUANTUM_CHEST[0].asStack(), "CPC", "PHP", "CFC", 'C', new UnificationEntry(TagPrefix.circuit, Tier.IV), 'P', new UnificationEntry(TagPrefix.plateDense, GTMaterials.TungstenSteel), 'F', GTItems.FIELD_GENERATOR_HV.asStack(), 'H', GTMachines.HULL[5].asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "quantum_chest_luv", GTMachines.QUANTUM_CHEST[1].asStack(), "CPC", "PHP", "CFC", 'C', new UnificationEntry(TagPrefix.circuit, Tier.LuV), 'P', new UnificationEntry(TagPrefix.plateDense, GTMaterials.RhodiumPlatedPalladium), 'F', GTItems.FIELD_GENERATOR_EV.asStack(), 'H', GTMachines.HULL[6].asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "quantum_chest_zpm", GTMachines.QUANTUM_CHEST[2].asStack(), "CPC", "PHP", "CFC", 'C', new UnificationEntry(TagPrefix.circuit, Tier.ZPM), 'P', new UnificationEntry(TagPrefix.plateDense, GTMaterials.NaquadahAlloy), 'F', GTItems.FIELD_GENERATOR_IV.asStack(), 'H', GTMachines.HULL[7].asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "quantum_chest_uv", GTMachines.QUANTUM_CHEST[3].asStack(), "CPC", "PHP", "CFC", 'C', new UnificationEntry(TagPrefix.circuit, Tier.UV), 'P', new UnificationEntry(TagPrefix.plateDense, GTMaterials.Darmstadtium), 'F', GTItems.FIELD_GENERATOR_LuV.asStack(), 'H', GTMachines.HULL[8].asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "quantum_chest_uhv", GTMachines.QUANTUM_CHEST[4].asStack(), "CPC", "PHP", "CFC", 'C', new UnificationEntry(TagPrefix.circuit, Tier.UHV), 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Neutronium), 'F', GTItems.FIELD_GENERATOR_ZPM.asStack(), 'H', GTMachines.HULL[9].asStack());

        // Super / Quantum Tanks
        VanillaRecipeHelper.addShapedRecipe(provider, true, "super_tank_lv", GTMachines.SUPER_TANK[0].asStack(), "CPC", "PHP", "CFC", 'C', new UnificationEntry(TagPrefix.circuit, Tier.LV), 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Steel), 'F', GTItems.ELECTRIC_PUMP_LV.asStack(), 'H', GTBlocks.HERMETIC_CASING_LV.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "super_tank_mv", GTMachines.SUPER_TANK[1].asStack(), "CPC", "PHP", "CFC", 'C', new UnificationEntry(TagPrefix.circuit, Tier.MV), 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Aluminium), 'F', GTItems.ELECTRIC_PUMP_MV.asStack(), 'H', GTBlocks.HERMETIC_CASING_MV.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "super_tank_hv", GTMachines.SUPER_TANK[2].asStack(), "CGC", "PHP", "CFC", 'C', new UnificationEntry(TagPrefix.circuit, Tier.HV), 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.StainlessSteel), 'F', GTItems.ELECTRIC_PUMP_HV.asStack(), 'H', GTBlocks.HERMETIC_CASING_HV.asStack(), 'G', GTItems.FIELD_GENERATOR_LV.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "super_tank_ev", GTMachines.SUPER_TANK[3].asStack(), "CGC", "PHP", "CFC", 'C', new UnificationEntry(TagPrefix.circuit, Tier.EV), 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Titanium), 'F', GTItems.ELECTRIC_PUMP_EV.asStack(), 'H', GTBlocks.HERMETIC_CASING_EV.asStack(), 'G', GTItems.FIELD_GENERATOR_MV.asStack());

        VanillaRecipeHelper.addShapedRecipe(provider, true, "quantum_tank_iv", GTMachines.QUANTUM_TANK[0].asStack(), "CGC", "PHP", "CUC", 'C', new UnificationEntry(TagPrefix.circuit, Tier.IV), 'P', new UnificationEntry(TagPrefix.plateDense, GTMaterials.TungstenSteel), 'U', GTItems.ELECTRIC_PUMP_IV.asStack(), 'G', GTItems.FIELD_GENERATOR_HV.asStack(), 'H', GTBlocks.HERMETIC_CASING_IV.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "quantum_tank_luv", GTMachines.QUANTUM_TANK[1].asStack(), "CGC", "PHP", "CUC", 'C', new UnificationEntry(TagPrefix.circuit, Tier.LuV), 'P', new UnificationEntry(TagPrefix.plateDense, GTMaterials.RhodiumPlatedPalladium), 'U', GTItems.ELECTRIC_PUMP_LuV.asStack(), 'G', GTItems.FIELD_GENERATOR_EV.asStack(), 'H', GTBlocks.HERMETIC_CASING_LuV.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "quantum_tank_zpm", GTMachines.QUANTUM_TANK[2].asStack(), "CGC", "PHP", "CUC", 'C', new UnificationEntry(TagPrefix.circuit, Tier.ZPM), 'P', new UnificationEntry(TagPrefix.plateDense, GTMaterials.NaquadahAlloy), 'U', GTItems.ELECTRIC_PUMP_ZPM.asStack(), 'G', GTItems.FIELD_GENERATOR_IV.asStack(), 'H', GTBlocks.HERMETIC_CASING_ZPM.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "quantum_tank_uv", GTMachines.QUANTUM_TANK[3].asStack(), "CGC", "PHP", "CUC", 'C', new UnificationEntry(TagPrefix.circuit, Tier.UV), 'P', new UnificationEntry(TagPrefix.plateDense, GTMaterials.Darmstadtium), 'U', GTItems.ELECTRIC_PUMP_UV.asStack(), 'G', GTItems.FIELD_GENERATOR_LuV.asStack(), 'H', GTBlocks.HERMETIC_CASING_UV.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "quantum_tank_uhv", GTMachines.QUANTUM_TANK[4].asStack(), "CGC", "PHP", "CUC", 'C', new UnificationEntry(TagPrefix.circuit, Tier.UHV), 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Neutronium), 'U', GTItems.ELECTRIC_PUMP_UV.asStack(), 'G', GTItems.FIELD_GENERATOR_ZPM.asStack(), 'H', GTBlocks.HERMETIC_CASING_UHV.asStack());

        // TODO Buffers
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "buffer_lv", GTMachines.BUFFER[0].getStackForm(), "HP", "CV", 'H', GTMachines.HULL[GTValues.LV].getStackForm(), 'P', MetaItems.ELECTRIC_PUMP_LV.getStackForm(), 'V', MetaItems.CONVEYOR_MODULE_LV.getStackForm(), 'C', new UnificationEntry(TagPrefix.circuit, MarkerGTMaterials.Tier.LV));
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "buffer_mv", GTMachines.BUFFER[1].getStackForm(), "HP", "CV", 'H', GTMachines.HULL[GTValues.MV].getStackForm(), 'P', MetaItems.ELECTRIC_PUMP_MV.getStackForm(), 'V', MetaItems.CONVEYOR_MODULE_MV.getStackForm(), 'C', new UnificationEntry(TagPrefix.circuit, MarkerGTMaterials.Tier.LV));
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "buffer_hv", GTMachines.BUFFER[2].getStackForm(), "HP", "CV", 'H', GTMachines.HULL[GTValues.HV].getStackForm(), 'P', MetaItems.ELECTRIC_PUMP_HV.getStackForm(), 'V', MetaItems.CONVEYOR_MODULE_HV.getStackForm(), 'C', new UnificationEntry(TagPrefix.circuit, MarkerGTMaterials.Tier.LV));

        // TODO Cleanroom
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "cleanroom", GTMachines.CLEANROOM.getStackForm(), "FFF", "RHR", "MCM", 'F', MetaItems.ITEM_FILTER.getStackForm(), 'R', new UnificationEntry(TagPrefix.rotor, GTMaterials.StainlessSteel), 'H', HULL.getIngredient(GTValues.HV), 'M', MetaItems.ELECTRIC_MOTOR_HV.getStackForm(), 'C', new UnificationEntry(TagPrefix.circuit, Tier.HV));

        // TODO Converters
        //if (ConfigHolder.compat.energy.enableFEConverters) {
        //    registerMachineRecipe(GTMachines.ENERGY_CONVERTER[0], " WW", "RMC", " WW", 'C', CIRCUIT, 'M', HULL, 'W', CABLE, 'R', new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.RedAlloy));
        //    registerMachineRecipe(GTMachines.ENERGY_CONVERTER[1], " WW", "RMC", " WW", 'C', CIRCUIT, 'M', HULL, 'W', CABLE_QUAD, 'R', new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.RedAlloy));
        //    registerMachineRecipe(GTMachines.ENERGY_CONVERTER[2], " WW", "RMC", " WW", 'C', CIRCUIT, 'M', HULL, 'W', CABLE_OCT, 'R', new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.RedAlloy));
        //    registerMachineRecipe(GTMachines.ENERGY_CONVERTER[3], " WW", "RMC", " WW", 'C', CIRCUIT, 'M', HULL, 'W', CABLE_HEX, 'R', new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.RedAlloy));
        //}
    }

    // Can only accept a subset of "Item" types:
    // - ItemStack
    // - Item
    // - Block
    // - ItemEntry<?> (like GTItems)
    // - CraftingComponent.Component
    // - UnificationEntry
    // - TagKey<?>
    public static void registerMachineRecipe(Consumer<FinishedRecipe> provider, boolean withUnificationData, MachineDefinition[] machines, Object... recipe) {
        for (MachineDefinition machine : machines) {

            // Needed to skip certain tiers if not enabled.
            // Leaves UHV+ machine recipes to be implemented by addons.
            if (machine != null) {
                Object[] prepRecipe = prepareRecipe(machine.getTier(), Arrays.copyOf(recipe, recipe.length));
                if (prepRecipe == null) {
                    return;
                }
                VanillaRecipeHelper.addShapedRecipe(provider, withUnificationData, machine.getName(), machine.asStack(), prepRecipe);
            }
        }
    }

    public static void registerMachineRecipe(Consumer<FinishedRecipe> provider, MachineDefinition[] machines, Object... recipe) {
        registerMachineRecipe(provider, true, machines, recipe);
    }

    private static Object[] prepareRecipe(int tier, Object... recipe) {
        for (int i = 3; i < recipe.length; i++) {
            if (recipe[i] instanceof Component) {
                Object component = ((Component) recipe[i]).getIngredient(tier);
                if (component == null) {
                    return null;
                }
                recipe[i] = component;
            } else if (recipe[i] instanceof Item item) {
                recipe[i] = new ItemStack(item);
            } else if (recipe[i] instanceof Block block) {
                recipe[i] = new ItemStack(block);
            } else if (recipe[i] instanceof ItemEntry<?> itemEntry) {
                recipe[i] = itemEntry.asStack();
            }
        }
        return recipe;
    }
}
