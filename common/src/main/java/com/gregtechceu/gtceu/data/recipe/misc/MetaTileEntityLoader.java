package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.CraftingComponent;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
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
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_hsse_sturdy", GTBlocks.CASING_HSSE_STURDY.asStack(2), "PhP", "PFP", "PwP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.HSSE), 'F', new UnificationEntry(TagPrefix.frameGt, GTMaterials.Europium));

        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_steel_turbine_casing", GTBlocks.CASING_STEEL_TURBINE.asStack(2), "PhP", "PFP", "PwP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Magnalium), 'F', new UnificationEntry(TagPrefix.frameGt, GTMaterials.BlueSteel));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_stainless_turbine_casing", GTBlocks.CASING_STAINLESS_TURBINE.asStack(2), "PhP", "PFP", "PwP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.StainlessSteel), 'F', GTBlocks.CASING_STEEL_TURBINE.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_titanium_turbine_casing", GTBlocks.CASING_TITANIUM_TURBINE.asStack(2), "PhP", "PFP", "PwP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Titanium), 'F', GTBlocks.CASING_STEEL_TURBINE.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_tungstensteel_turbine_casing", GTBlocks.CASING_TUNGSTENSTEEL_TURBINE.asStack(2), "PhP", "PFP", "PwP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.TungstenSteel), 'F', GTBlocks.CASING_STEEL_TURBINE.asStack());


        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_bronze_pipe", GTBlocks.CASING_BRONZE_PIPE.asStack(2), "PIP", "IFI", "PIP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Bronze), 'F', new UnificationEntry(TagPrefix.frameGt, GTMaterials.Bronze), 'I', new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.Bronze));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_steel_pipe", GTBlocks.CASING_STEEL_PIPE.asStack(2), "PIP", "IFI", "PIP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Steel), 'F', new UnificationEntry(TagPrefix.frameGt, GTMaterials.Steel), 'I', new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.Steel));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_titanium_pipe", GTBlocks.CASING_TITANIUM_PIPE.asStack(2), "PIP", "IFI", "PIP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Titanium), 'F', new UnificationEntry(TagPrefix.frameGt, GTMaterials.Titanium), 'I', new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.Titanium));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_tungstensteel_pipe", GTBlocks.CASING_TUNGSTENSTEEL_PIPE.asStack(2), "PIP", "IFI", "PIP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.TungstenSteel), 'F', new UnificationEntry(TagPrefix.frameGt, GTMaterials.TungstenSteel), 'I', new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.TungstenSteel));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_ptfe_pipe", GTBlocks.CASING_POLYTETRAFLUOROETHYLENE_PIPE.asStack(2), "PIP", "IFI", "PIP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Polytetrafluoroethylene), 'F', new UnificationEntry(TagPrefix.frameGt, GTMaterials.Polytetrafluoroethylene), 'I', new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.Polytetrafluoroethylene));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_bronze_firebox", GTBlocks.FIREBOX_BRONZE.asStack(2), "PSP", "SFS", "PSP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Bronze), 'F', new UnificationEntry(TagPrefix.frameGt, GTMaterials.Bronze), 'S', new UnificationEntry(TagPrefix.rod, GTMaterials.Bronze));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_steel_firebox", GTBlocks.FIREBOX_STEEL.asStack(2), "PSP", "SFS", "PSP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Steel), 'F', new UnificationEntry(TagPrefix.frameGt, GTMaterials.Steel), 'S', new UnificationEntry(TagPrefix.rod, GTMaterials.Steel));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_titanium_firebox", GTBlocks.FIREBOX_TITANIUM.asStack(2), "PSP", "SFS", "PSP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Titanium), 'F', new UnificationEntry(TagPrefix.frameGt, GTMaterials.Titanium), 'S', new UnificationEntry(TagPrefix.rod, GTMaterials.Titanium));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_tungstensteel_firebox", GTBlocks.FIREBOX_TUNGSTENSTEEL.asStack(2), "PSP", "SFS", "PSP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.TungstenSteel), 'F', new UnificationEntry(TagPrefix.frameGt, GTMaterials.TungstenSteel), 'S', new UnificationEntry(TagPrefix.rod, GTMaterials.TungstenSteel));

        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_bronze_gearbox", GTBlocks.CASING_BRONZE_GEARBOX.asStack(2), "PhP", "GFG", "PwP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Bronze), 'F', new UnificationEntry(TagPrefix.frameGt, GTMaterials.Bronze), 'G', new UnificationEntry(TagPrefix.gear, GTMaterials.Bronze));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_steel_gearbox", GTBlocks.CASING_STEEL_GEARBOX.asStack(2), "PhP", "GFG", "PwP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Steel), 'F', new UnificationEntry(TagPrefix.frameGt, GTMaterials.Steel), 'G', new UnificationEntry(TagPrefix.gear, GTMaterials.Steel));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_stainless_steel_gearbox", GTBlocks.CASING_STAINLESS_STEEL_GEARBOX.asStack(2), "PhP", "GFG", "PwP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.StainlessSteel), 'F', new UnificationEntry(TagPrefix.frameGt, GTMaterials.StainlessSteel), 'G', new UnificationEntry(TagPrefix.gear, GTMaterials.StainlessSteel));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_titanium_gearbox", GTBlocks.CASING_TITANIUM_GEARBOX.asStack(2), "PhP", "GFG", "PwP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Titanium), 'F', new UnificationEntry(TagPrefix.frameGt, GTMaterials.Titanium), 'G', new UnificationEntry(TagPrefix.gear, GTMaterials.Titanium));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_tungstensteel_gearbox", GTBlocks.CASING_TUNGSTENSTEEL_GEARBOX.asStack(2), "PhP", "GFG", "PwP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.TungstenSteel), 'F', new UnificationEntry(TagPrefix.frameGt, GTMaterials.TungstenSteel), 'G', new UnificationEntry(TagPrefix.gear, GTMaterials.TungstenSteel));

        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_grate_casing", GTBlocks.CASING_GRATE.asStack(2), "PVP", "PFP", "PMP", 'P', new ItemStack(Blocks.IRON_BARS, 1), 'F', new UnificationEntry(TagPrefix.frameGt, GTMaterials.Steel), 'M', GTItems.ELECTRIC_MOTOR_MV, 'V', new UnificationEntry(TagPrefix.rotor, GTMaterials.Steel));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_assembly_control", GTBlocks.CASING_ASSEMBLY_CONTROL.asStack(2), "CPC", "SFE", "CMC", 'C', CustomTags.EV_CIRCUITS, 'P', GTItems.HIGH_POWER_INTEGRATED_CIRCUIT, 'S', GTItems.SENSOR_IV.asStack(), 'F', new UnificationEntry(TagPrefix.frameGt, GTMaterials.TungstenSteel), 'E', GTItems.EMITTER_IV.asStack(), 'M', GTItems.ELECTRIC_MOTOR_IV);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_assembly_line", GTBlocks.CASING_ASSEMBLY_LINE.asStack(2), "PGP", "AFA", "PGP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Steel), 'G', new UnificationEntry(TagPrefix.gear, GTMaterials.Ruridit), 'A', GTItems.ROBOT_ARM_IV.asStack(), 'F', ChemicalHelper.get(TagPrefix.frameGt, GTMaterials.TungstenSteel));

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

        VanillaRecipeHelper.addShapedRecipe(provider, true, "rotor_holder_hv", GTMachines.ROTOR_HOLDER[HV].asStack(), "SGS", "GHG", "SGS", 'H', GTMachines.HULL[HV].asStack(), 'G', new UnificationEntry(TagPrefix.gear, GTMaterials.BlackSteel), 'S', new UnificationEntry(TagPrefix.gearSmall, GTMaterials.StainlessSteel));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "rotor_holder_ev", GTMachines.ROTOR_HOLDER[EV].asStack(), "SGS", "GHG", "SGS", 'H', GTMachines.HULL[GTValues.EV].asStack(), 'G', new UnificationEntry(TagPrefix.gear, GTMaterials.Ultimet), 'S', new UnificationEntry(TagPrefix.gearSmall, GTMaterials.Titanium));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "rotor_holder_iv", GTMachines.ROTOR_HOLDER[IV].asStack(), "SGS", "GHG", "SGS", 'H', GTMachines.HULL[GTValues.IV].asStack(), 'G', new UnificationEntry(TagPrefix.gear, GTMaterials.HSSG), 'S', new UnificationEntry(TagPrefix.gearSmall, GTMaterials.TungstenSteel));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "rotor_holder_luv", GTMachines.ROTOR_HOLDER[LuV].asStack(), "SGS", "GHG", "SGS", 'H', GTMachines.HULL[GTValues.LuV].asStack(), 'G', new UnificationEntry(TagPrefix.gear, GTMaterials.Ruthenium), 'S', new UnificationEntry(TagPrefix.gearSmall, GTMaterials.RhodiumPlatedPalladium));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "rotor_holder_zpm", GTMachines.ROTOR_HOLDER[ZPM].asStack(), "SGS", "GHG", "SGS", 'H', GTMachines.HULL[GTValues.ZPM].asStack(), 'G', new UnificationEntry(TagPrefix.gear, GTMaterials.Trinium), 'S', new UnificationEntry(TagPrefix.gearSmall, GTMaterials.NaquadahAlloy));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "rotor_holder_uv", GTMachines.ROTOR_HOLDER[UV].asStack(), "SGS", "GHG", "SGS", 'H', GTMachines.HULL[GTValues.UV].asStack(), 'G', new UnificationEntry(TagPrefix.gear, GTMaterials.Tritanium), 'S', new UnificationEntry(TagPrefix.gearSmall, GTMaterials.Darmstadtium));

        VanillaRecipeHelper.addShapedRecipe(provider, true, "maintenance_hatch", GTMachines.MAINTENANCE_HATCH.asStack(), "dwx", "hHc", "fsr", 'H', GTMachines.HULL[GTValues.LV].asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "maintenance_hatch_configurable", GTMachines.CONFIGURABLE_MAINTENANCE_HATCH.asStack(), "   ", "CMC", "VHV", 'C', CIRCUIT.getIngredient(HV), 'M', GTMachines.MAINTENANCE_HATCH.asStack(), 'V', CONVEYOR.getIngredient(HV), 'H', GTMachines.HULL[HV].asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "maintenance_hatch_automatic", GTMachines.AUTO_MAINTENANCE_HATCH.asStack(), "CMC", "RHR", "CMC", 'C', CIRCUIT.getIngredient(HV), 'M', GTMachines.MAINTENANCE_HATCH.asStack(), 'R', ROBOT_ARM.getIngredient(HV), 'H', GTMachines.HULL[HV].asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "maintenance_hatch_cleaning", GTMachines.CLEANING_MAINTENANCE_HATCH.asStack(), "CMC", "RHR", "WCW", 'C', CIRCUIT.getIngredient(GTValues.UV), 'M', GTMachines.AUTO_MAINTENANCE_HATCH.asStack(), 'R', ROBOT_ARM.getIngredient(GTValues.UV), 'H', GTMachines.HULL[GTValues.UV].asStack(), 'W', new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.YttriumBariumCuprate));

        // TODO Access Interface
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "machine_access_interface", GTMachines.MACHINE_HATCH.getStackForm(), "CHS", 'C', CustomTags.IV), 'H', GTMachines.HULL[GTValues.IV].getStackForm(), 'S', MetaItems.SENSOR_IV.getStackForm());

        VanillaRecipeHelper.addShapedRecipe(provider, true, "passthrough_hatch_item", GTMachines.ITEM_PASSTHROUGH_HATCH[HV].asStack(), " C ", "GHG", " S ", 'C', GTItems.CONVEYOR_MODULE_HV.asStack(), 'G', new UnificationEntry(TagPrefix.gearSmall, GTMaterials.Steel), 'H', GTMachines.HULL[HV].asStack(), 'S', CustomTags.TAG_WOODEN_CHESTS);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "passthrough_hatch_fluid", GTMachines.FLUID_PASSTHROUGH_HATCH[HV].asStack(), " C ", "GHG", " S ", 'C', GTItems.ELECTRIC_PUMP_HV.asStack(), 'G', new UnificationEntry(TagPrefix.pipeSmallFluid, GTMaterials.Steel), 'H', GTMachines.HULL[HV].asStack(), 'S', GTBlocks.CASING_TEMPERED_GLASS);

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
        VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_macerator_bronze", GTMachines.STEAM_MACERATOR.left().asStack(), "DXD", "XMX", "PXP", 'M', GTBlocks.BRONZE_HULL.asStack(), 'X', new UnificationEntry(TagPrefix.pipeSmallFluid, GTMaterials.Bronze), 'P', CustomTags.TAG_PISTONS, 'D', new UnificationEntry(TagPrefix.gem, GTMaterials.Diamond));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_macerator_steel", GTMachines.STEAM_MACERATOR.right().asStack(), "WSW", "PMP", "WWW", 'M', GTMachines.STEAM_MACERATOR.left().asStack(), 'W', new UnificationEntry(TagPrefix.plate, GTMaterials.WroughtIron), 'S', new UnificationEntry(TagPrefix.plate, GTMaterials.Steel), 'P', new UnificationEntry(TagPrefix.pipeSmallFluid, GTMaterials.TinAlloy));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_extractor_bronze", GTMachines.STEAM_EXTRACTOR.left().asStack(), "XXX", "PMG", "XXX", 'M', GTBlocks.BRONZE_HULL.asStack(), 'X', new UnificationEntry(TagPrefix.pipeSmallFluid, GTMaterials.Bronze), 'P', CustomTags.TAG_PISTONS, 'G', new ItemStack(Blocks.GLASS));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_extractor_steel", GTMachines.STEAM_EXTRACTOR.right().asStack(), "PSP", "WMW", "PPP", 'M', GTMachines.STEAM_EXTRACTOR.left().asStack(), 'P', new UnificationEntry(TagPrefix.pipeSmallFluid, GTMaterials.TinAlloy), 'S', new UnificationEntry(TagPrefix.plate, GTMaterials.Steel), 'W', new UnificationEntry(TagPrefix.plate, GTMaterials.WroughtIron));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_hammer_bronze", GTMachines.STEAM_HAMMER.left().asStack(), "XPX", "XMX", "XAX", 'M', GTBlocks.BRONZE_HULL.asStack(), 'X', new UnificationEntry(TagPrefix.pipeSmallFluid, GTMaterials.Bronze), 'P', CustomTags.TAG_PISTONS, 'A', Blocks.ANVIL);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_hammer_steel", GTMachines.STEAM_HAMMER.right().asStack(), "WSW", "PMP", "WWW", 'M', GTMachines.STEAM_HAMMER.left().asStack(), 'S', new UnificationEntry(TagPrefix.plate, GTMaterials.Steel), 'W', new UnificationEntry(TagPrefix.plate, GTMaterials.WroughtIron), 'P', new UnificationEntry(TagPrefix.pipeSmallFluid, GTMaterials.TinAlloy));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_compressor_bronze", GTMachines.STEAM_COMPRESSOR.left().asStack(), "XXX", "PMP", "XXX", 'M', GTBlocks.BRONZE_HULL.asStack(), 'X', new UnificationEntry(TagPrefix.pipeSmallFluid, GTMaterials.Bronze), 'P', CustomTags.TAG_PISTONS);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_compressor_steel", GTMachines.STEAM_COMPRESSOR.right().asStack(), "PSP", "WMW", "PPP", 'M', GTMachines.STEAM_COMPRESSOR.left().asStack(), 'S', new UnificationEntry(TagPrefix.plate, GTMaterials.Steel), 'W', new UnificationEntry(TagPrefix.plate, GTMaterials.WroughtIron), 'P', new UnificationEntry(TagPrefix.pipeSmallFluid, GTMaterials.TinAlloy));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_alloy_smelter_bronze", GTMachines.STEAM_ALLOY_SMELTER.left().asStack(), "XXX", "FMF", "XXX", 'M', GTBlocks.BRONZE_BRICKS_HULL.asStack(), 'X', new UnificationEntry(TagPrefix.pipeSmallFluid, GTMaterials.Bronze), 'F', Blocks.FURNACE);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_alloy_smelter_steel", GTMachines.STEAM_ALLOY_SMELTER.right().asStack(), "WSW", "WMW", "WPW", 'M', GTMachines.STEAM_ALLOY_SMELTER.left().asStack(), 'S', new UnificationEntry(TagPrefix.plate, GTMaterials.Steel), 'W', new UnificationEntry(TagPrefix.plate, GTMaterials.WroughtIron), 'P', new UnificationEntry(TagPrefix.pipeSmallFluid, GTMaterials.TinAlloy));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_rock_breaker_bronze", GTMachines.STEAM_ROCK_CRUSHER.left().asStack(), "PXP", "XMX", "DXD", 'M', GTBlocks.BRONZE_HULL.asStack(), 'X', new UnificationEntry(TagPrefix.pipeSmallFluid, GTMaterials.Bronze), 'P', CustomTags.TAG_PISTONS, 'D', new UnificationEntry(TagPrefix.gem, GTMaterials.Diamond));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_rock_breaker_steel", GTMachines.STEAM_ROCK_CRUSHER.right().asStack(), "WSW", "PMP", "WWW", 'M', GTMachines.STEAM_ROCK_CRUSHER.left().asStack(), 'W', new UnificationEntry(TagPrefix.plate, GTMaterials.WroughtIron), 'S', new UnificationEntry(TagPrefix.plate, GTMaterials.Steel), 'P', new UnificationEntry(TagPrefix.pipeSmallFluid, GTMaterials.TinAlloy));
        // TODO steam miner
        VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_miner", GTMachines.STEAM_MINER.asStack(), "DSD", "SMS", "GSG", 'M', GTBlocks.BRONZE_BRICKS_HULL.asStack(), 'S', new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.Bronze), 'D', new UnificationEntry(TagPrefix.gem, GTMaterials.Diamond), 'G', new UnificationEntry(TagPrefix.gearSmall, GTMaterials.Bronze));

        // MULTI BLOCK CONTROLLERS
        VanillaRecipeHelper.addShapedRecipe(provider, true, "bronze_primitive_blast_furnace", GTMachines.PRIMITIVE_BLAST_FURNACE.asStack(), "hRS", "PBR", "dRS", 'R', new UnificationEntry(TagPrefix.rod, GTMaterials.Iron), 'S', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron), 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Iron), 'B', GTBlocks.CASING_PRIMITIVE_BRICKS.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "coke_oven", GTMachines.COKE_OVEN.asStack(), "PIP", "IwI", "PIP", 'P', GTBlocks.CASING_COKE_BRICKS.asStack(), 'I', new UnificationEntry(TagPrefix.plate, GTMaterials.Iron));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "coke_oven_hatch", GTMachines.COKE_OVEN_HATCH.asStack(), "CD", 'C', GTBlocks.CASING_COKE_BRICKS.asStack(), 'D', GTMachines.WOODEN_DRUM.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "electric_blast_furnace", GTMachines.ELECTRIC_BLAST_FURNACE.asStack(), "FFF", "CMC", "WCW", 'M', GTBlocks.CASING_INVAR_HEATPROOF.asStack(), 'F', Blocks.FURNACE.asItem(), 'C', CustomTags.LV_CIRCUITS, 'W', new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Tin));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "vacuum_freezer", GTMachines.VACUUM_FREEZER.asStack(), "PPP", "CMC", "WCW", 'M', GTBlocks.CASING_ALUMINIUM_FROSTPROOF.asStack(), 'P', GTItems.ELECTRIC_PUMP_HV, 'C', CustomTags.EV_CIRCUITS, 'W', new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Gold));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "implosion_compressor", GTMachines.IMPLOSION_COMPRESSOR.asStack(), "OOO", "CMC", "WCW", 'M', GTBlocks.CASING_STEEL_SOLID.asStack(), 'O', new UnificationEntry(TagPrefix.block, GTMaterials.Obsidian), 'C', CustomTags.HV_CIRCUITS, 'W', new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Gold));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "distillation_tower", GTMachines.DISTILLATION_TOWER.asStack(), "CBC", "FMF", "CBC", 'M', GTMachines.HULL[HV].asStack(), 'B', new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.StainlessSteel), 'C', CustomTags.EV_CIRCUITS, 'F', GTItems.ELECTRIC_PUMP_HV);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "cracking_unit", GTMachines.CRACKER.asStack(), "CEC", "PHP", "CEC", 'C', GTBlocks.COIL_CUPRONICKEL.asStack(), 'E', GTItems.ELECTRIC_PUMP_HV.asStack(), 'P', CustomTags.HV_CIRCUITS, 'H', GTMachines.HULL[HV].asStack());

        VanillaRecipeHelper.addShapedRecipe(provider, true, "pyrolyse_oven", GTMachines.PYROLYSE_OVEN.asStack(), "WEP", "EME", "WCP", 'M', GTMachines.HULL[GTValues.MV].asStack(), 'W', GTItems.ELECTRIC_PISTON_MV.asStack(), 'P', new UnificationEntry(TagPrefix.wireGtQuadruple, GTMaterials.Cupronickel), 'E', CustomTags.MV_CIRCUITS, 'C', GTItems.ELECTRIC_PUMP_MV);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "large_combustion_engine", GTMachines.LARGE_COMBUSTION_ENGINE.asStack(), "PCP", "EME", "GWG", 'M', GTMachines.HULL[GTValues.EV].asStack(), 'P', GTItems.ELECTRIC_PISTON_EV.asStack(), 'E', GTItems.ELECTRIC_MOTOR_EV.asStack(), 'C', CustomTags.IV_CIRCUITS, 'W', new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Aluminium), 'G', new UnificationEntry(TagPrefix.gear, GTMaterials.Titanium));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "extreme_combustion_engine", GTMachines.EXTREME_COMBUSTION_ENGINE.asStack(), "PCP", "EME", "GWG", 'M', GTMachines.HULL[GTValues.IV].asStack(), 'P', GTItems.ELECTRIC_PISTON_IV.asStack(), 'E', GTItems.ELECTRIC_MOTOR_IV.asStack(), 'C', CustomTags.LuV_CIRCUITS, 'W', new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.HSSG), 'G', new UnificationEntry(TagPrefix.gear, GTMaterials.TungstenSteel));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "engine_intake_casing", GTBlocks.CASING_ENGINE_INTAKE.asStack(2), "PhP", "RFR", "PwP", 'R', new UnificationEntry(TagPrefix.rotor, GTMaterials.Titanium), 'F', GTBlocks.CASING_TITANIUM_STABLE.asStack(), 'P', new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.Titanium));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "extreme_engine_intake_casing", GTBlocks.CASING_EXTREME_ENGINE_INTAKE.asStack(2), "PhP", "RFR", "PwP", 'R', new UnificationEntry(TagPrefix.rotor, GTMaterials.TungstenSteel), 'F',GTBlocks.CASING_TUNGSTENSTEEL_ROBUST.asStack(), 'P', new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.TungstenSteel));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "multi_furnace", GTMachines.MULTI_SMELTER.asStack(), "PPP", "ASA", "CAC", 'P', Blocks.FURNACE, 'A', CustomTags.HV_CIRCUITS, 'S', GTBlocks.CASING_INVAR_HEATPROOF.asStack(), 'C', new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Copper));

        VanillaRecipeHelper.addShapedRecipe(provider, true, "large_steam_turbine", GTMachines.LARGE_STEAM_TURBINE.asStack(), "PSP", "SAS", "CSC", 'S', new UnificationEntry(TagPrefix.gear, GTMaterials.Steel), 'P', CustomTags.HV_CIRCUITS, 'A', GTMachines.HULL[HV].asStack(), 'C', new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.Steel));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "large_gas_turbine", GTMachines.LARGE_GAS_TURBINE.asStack(), "PSP", "SAS", "CSC", 'S', new UnificationEntry(TagPrefix.gear, GTMaterials.StainlessSteel), 'P', CustomTags.EV_CIRCUITS, 'A', GTMachines.HULL[GTValues.EV].asStack(), 'C', new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.StainlessSteel));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "large_plasma_turbine", GTMachines.LARGE_PLASMA_TURBINE.asStack(), "PSP", "SAS", "CSC", 'S', new UnificationEntry(TagPrefix.gear, GTMaterials.TungstenSteel), 'P', CustomTags.LuV_CIRCUITS, 'A', GTMachines.HULL[GTValues.LuV].asStack(), 'C', new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.TungstenSteel));

        VanillaRecipeHelper.addShapedRecipe(provider, true, "large_bronze_boiler", GTMachines.LARGE_BOILER_BRONZE.asStack(), "PSP", "SAS", "PSP", 'P', new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Tin), 'S', CustomTags.LV_CIRCUITS, 'A', GTBlocks.FIREBOX_BRONZE.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "large_steel_boiler", GTMachines.LARGE_BOILER_STEEL.asStack(), "PSP", "SAS", "PSP", 'P', new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Copper), 'S', CustomTags.HV_CIRCUITS, 'A', GTBlocks.FIREBOX_STEEL.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "large_titanium_boiler", GTMachines.LARGE_BOILER_TITANIUM.asStack(), "PSP", "SAS", "PSP", 'P', new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Gold), 'S', CustomTags.EV_CIRCUITS, 'A', GTBlocks.FIREBOX_TITANIUM.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "large_tungstensteel_boiler", GTMachines.LARGE_BOILER_TUNGSTENSTEEL.asStack(), "PSP", "SAS", "PSP", 'P', new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Aluminium), 'S', CustomTags.IV_CIRCUITS, 'A', GTBlocks.FIREBOX_TUNGSTENSTEEL.asStack());

        VanillaRecipeHelper.addShapedRecipe(provider, true, "assembly_line", GTMachines.ASSEMBLY_LINE.asStack(), "CRC", "SAS", "CRC", 'A', GTMachines.HULL[GTValues.IV].asStack(), 'R', GTItems.ROBOT_ARM_IV, 'C', GTBlocks.CASING_ASSEMBLY_CONTROL.asStack(), 'S', CustomTags.IV_CIRCUITS);

        VanillaRecipeHelper.addShapedRecipe(provider, true, "large_chemical_reactor", GTMachines.LARGE_CHEMICAL_REACTOR.asStack(), "CRC", "PMP", "CHC", 'C', CustomTags.HV_CIRCUITS, 'R', ChemicalHelper.get(TagPrefix.rotor, GTMaterials.StainlessSteel), 'P', ChemicalHelper.get(TagPrefix.pipeLargeFluid, GTMaterials.Polytetrafluoroethylene), 'M', GTItems.ELECTRIC_MOTOR_HV.asStack(), 'H', GTMachines.HULL[HV].asStack());

        if (ConfigHolder.INSTANCE.machines.doProcessingArray) {
            VanillaRecipeHelper.addShapedRecipe(provider, true, "processing_array", GTMachines.PROCESSING_ARRAY[GTValues.IV].asStack(), "COC", "RHR", "CPC", 'C', CustomTags.IV_CIRCUITS, 'O', GTItems.TOOL_DATA_ORB.asStack(), 'R', GTItems.ROBOT_ARM_EV.asStack(), 'P', GTBlocks.FLUID_PIPE_BLOCKS.get(TagPrefix.pipeLargeFluid, GTMaterials.StainlessSteel), 'H', GTMachines.HULL[GTValues.EV].asStack());
            VanillaRecipeHelper.addShapedRecipe(provider, true, "advanced_processing_array", GTMachines.PROCESSING_ARRAY[GTValues.LuV].asStack(), "RCR", "SPE", "HNH", 'R', GTItems.ROBOT_ARM_LuV.asStack(), 'C', CustomTags.ZPM_CIRCUITS, 'S', GTItems.SENSOR_LuV, 'P', GTMachines.PROCESSING_ARRAY[IV].asStack(), 'E', GTItems.EMITTER_LuV.asStack(), 'H', new UnificationEntry(TagPrefix.plate, GTMaterials.HSSE), 'N', new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.Naquadah));
        }

        // GENERATORS
        VanillaRecipeHelper.addShapedRecipe(provider, true, "diesel_generator_lv", GTMachines.COMBUSTION[LV].asStack(), "PCP", "EME", "GWG", 'M', GTMachines.HULL[GTValues.LV].asStack(), 'P', GTItems.ELECTRIC_PISTON_LV, 'E', GTItems.ELECTRIC_MOTOR_LV, 'C', CustomTags.LV_CIRCUITS, 'W', new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Tin), 'G', new UnificationEntry(TagPrefix.gear, GTMaterials.Steel));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "diesel_generator_mv", GTMachines.COMBUSTION[MV].asStack(), "PCP", "EME", "GWG", 'M', GTMachines.HULL[GTValues.MV].asStack(), 'P', GTItems.ELECTRIC_PISTON_MV, 'E', GTItems.ELECTRIC_MOTOR_MV, 'C', CustomTags.MV_CIRCUITS, 'W', new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Copper), 'G', new UnificationEntry(TagPrefix.gear, GTMaterials.Aluminium));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "diesel_generator_hv", GTMachines.COMBUSTION[HV].asStack(), "PCP", "EME", "GWG", 'M', GTMachines.HULL[HV].asStack(), 'P', GTItems.ELECTRIC_PISTON_HV, 'E', GTItems.ELECTRIC_MOTOR_HV, 'C', CustomTags.HV_CIRCUITS, 'W', new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Gold), 'G', new UnificationEntry(TagPrefix.gear, GTMaterials.StainlessSteel));

        VanillaRecipeHelper.addShapedRecipe(provider, true, "gas_turbine_lv", GTMachines.GAS_TURBINE[LV].asStack(), "CRC", "RMR", "EWE", 'M', GTMachines.HULL[GTValues.LV].asStack(), 'E', GTItems.ELECTRIC_MOTOR_LV, 'R', new UnificationEntry(TagPrefix.rotor, GTMaterials.Tin), 'C', CustomTags.LV_CIRCUITS, 'W', new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Tin));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "gas_turbine_mv", GTMachines.GAS_TURBINE[MV].asStack(), "CRC", "RMR", "EWE", 'M', GTMachines.HULL[GTValues.MV].asStack(), 'E', GTItems.ELECTRIC_MOTOR_MV, 'R', new UnificationEntry(TagPrefix.rotor, GTMaterials.Bronze), 'C', CustomTags.MV_CIRCUITS, 'W', new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Copper));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "gas_turbine_hv", GTMachines.GAS_TURBINE[HV].asStack(), "CRC", "RMR", "EWE", 'M', GTMachines.HULL[HV].asStack(), 'E', GTItems.ELECTRIC_MOTOR_HV, 'R', new UnificationEntry(TagPrefix.rotor, GTMaterials.Steel), 'C', CustomTags.HV_CIRCUITS, 'W', new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Gold));

        VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_turbine_lv", GTMachines.STEAM_TURBINE[LV].asStack(), "PCP", "RMR", "EWE", 'M', GTMachines.HULL[GTValues.LV].asStack(), 'E', GTItems.ELECTRIC_MOTOR_LV, 'R', new UnificationEntry(TagPrefix.rotor, GTMaterials.Tin), 'C', CustomTags.LV_CIRCUITS, 'W', new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Tin), 'P', new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.Bronze));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_turbine_mv", GTMachines.STEAM_TURBINE[MV].asStack(), "PCP", "RMR", "EWE", 'M', GTMachines.HULL[GTValues.MV].asStack(), 'E', GTItems.ELECTRIC_MOTOR_MV, 'R', new UnificationEntry(TagPrefix.rotor, GTMaterials.Bronze), 'C', CustomTags.MV_CIRCUITS, 'W', new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Copper), 'P', new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.Steel));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "steam_turbine_hv", GTMachines.STEAM_TURBINE[HV].asStack(), "PCP", "RMR", "EWE", 'M', GTMachines.HULL[HV].asStack(), 'E', GTItems.ELECTRIC_MOTOR_HV, 'R', new UnificationEntry(TagPrefix.rotor, GTMaterials.Steel), 'C', CustomTags.HV_CIRCUITS, 'W', new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.Gold), 'P', new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.StainlessSteel));

        // TODO Crafting station
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "workbench_bronze", GTMachines.WORKBENCH.getStackForm(), "CSC", "PWP", "PsP", 'C', OreDictNames.chestWood, 'W', new ItemStack(Blocks.CRAFTING_TABLE), 'S', OreDictUnifier.get("slabWood"), 'P', new UnificationEntry(TagPrefix.plank, GTMaterials.Wood));

        VanillaRecipeHelper.addShapedRecipe(provider, true, "primitive_pump", GTMachines.PRIMITIVE_PUMP.asStack(), "RGS", "OWd", "CLC", 'R', new UnificationEntry(TagPrefix.ring, GTMaterials.Iron), 'G', new UnificationEntry(TagPrefix.pipeNormalFluid, GTMaterials.Wood), 'S', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron), 'O', new UnificationEntry(TagPrefix.rotor, GTMaterials.Iron), 'W', GTBlocks.TREATED_WOOD_PLANK.asStack(), 'C', new ItemStack(Items.COBBLESTONE_SLAB), 'L', new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.Wood));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "pump_deck", GTBlocks.CASING_PUMP_DECK.asStack(2), "SWS", "dCh", 'S', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron), 'W', GTBlocks.TREATED_WOOD_PLANK.asStack(), 'C', new ItemStack(Items.COBBLESTONE_SLAB));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "pump_hatch", GTMachines.PUMP_HATCH.asStack(), "SRd", "PLP", "CRC", 'S', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron), 'R', new UnificationEntry(TagPrefix.ring, GTMaterials.Iron), 'P', GTBlocks.TREATED_WOOD_PLANK.asStack(), 'L', new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.Wood), 'C', new ItemStack(Items.COBBLESTONE_SLAB));

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
        registerMachineRecipe(provider, GTMachines.PACKER, "BCB", "RMV", "WCW", 'M', HULL, 'R', ROBOT_ARM, 'V', CONVEYOR, 'C', CIRCUIT, 'W', CABLE, 'B', CustomTags.TAG_WOODEN_CHESTS);
        registerMachineRecipe(provider, GTMachines.CHEMICAL_REACTOR, "GRG", "WEW", "CMC", 'M', HULL, 'R', ROTOR, 'E', MOTOR, 'C', CIRCUIT, 'W', CABLE, 'G', PIPE_REACTOR);
        registerMachineRecipe(provider, GTMachines.BREWERY, "GPG", "WMW", "CBC", 'M', HULL, 'P', PUMP, 'B', STICK_DISTILLATION, 'C', CIRCUIT, 'W', CABLE, 'G', GLASS);
        registerMachineRecipe(provider, GTMachines.FERMENTER, "WPW", "GMG", "WCW", 'M', HULL, 'P', PUMP, 'C', CIRCUIT, 'W', CABLE, 'G', GLASS);
        registerMachineRecipe(provider, GTMachines.FLUID_SOLIDIFIER, "PGP", "WMW", "CBC", 'M', HULL, 'P', PUMP, 'C', CIRCUIT, 'W', CABLE, 'G', GLASS, 'B', CustomTags.TAG_WOODEN_CHESTS);
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
        registerMachineRecipe(provider, GTMachines.FISHER, "WTW", "PMP", "TGT", 'M', HULL, 'W', CIRCUIT, 'G', PUMP, 'T', MOTOR, 'P', PISTON);
        //registerMachineRecipe(provider, GTMachines.ITEM_COLLECTOR, "MRM", "RHR", "CWC", 'M', MOTOR, 'R', ROTOR, 'H', HULL, 'C', CIRCUIT, 'W', CABLE);
        registerMachineRecipe(provider, GTMachines.BLOCK_BREAKER, "MGM", "CHC", "WSW", 'M', MOTOR, 'H', HULL, 'C', CIRCUIT, 'W', CABLE, 'S', CustomTags.TAG_WOODEN_CHESTS, 'G', GRINDER);
        //registerMachineRecipe(provider, GTMachines.WORLD_ACCELERATOR, "IGI", "FHF", "IGI", 'H', HULL, 'F', EMITTER, 'G', SENSOR, 'I', FIELD_GENERATOR);
        registerMachineRecipe(provider, GTMachines.MINER, "MMM", "WHW", "CSC", 'M', MOTOR, 'W', CABLE, 'H', HULL, 'C', CIRCUIT, 'S', SENSOR);

        registerMachineRecipe(provider, GTMachines.MUFFLER_HATCH, "HM", "PR", 'H', HULL, 'M', MOTOR, 'P', PIPE_NORMAL, 'R', ROTOR);

        registerMachineRecipe(provider, ArrayUtils.subarray(GTMachines.DIODE, GTValues.ULV, HV), "CDC", "DHD", "PDP", 'H', HULL, 'D', GTItems.DIODE, 'P', PLATE, 'C', CABLE_QUAD);
        registerMachineRecipe(provider, ArrayUtils.subarray(GTMachines.DIODE, HV, GTValues.LuV), "CDC", "DHD", "PDP", 'H', HULL, 'D', GTItems.SMD_DIODE, 'P', PLATE, 'C', CABLE_QUAD);
        registerMachineRecipe(provider, ArrayUtils.subarray(GTMachines.DIODE, GTValues.LuV, GTMachines.DIODE.length), "CDC", "DHD", "PDP", 'H', HULL, 'D', GTItems.ADVANCED_SMD_DIODE, 'P', PLATE, 'C', CABLE_QUAD);

        registerMachineRecipe(provider, ArrayUtils.subarray(GTMachines.TRANSFORMER, GTValues.ULV, GTValues.MV), " CC", "TH ", " CC", 'C', CABLE, 'T', CABLE_TIER_UP, 'H', HULL);
        registerMachineRecipe(provider, ArrayUtils.subarray(GTMachines.TRANSFORMER, GTValues.MV, GTValues.UHV), "WCC", "TH ", "WCC", 'W', POWER_COMPONENT, 'C', CABLE, 'T', CABLE_TIER_UP, 'H', HULL);
        registerMachineRecipe(provider, ArrayUtils.subarray(GTMachines.HI_AMP_TRANSFORMER_2A, GTValues.ULV, GTValues.MV), " CC", "TH ", " CC", 'C', CABLE_DOUBLE, 'T', CABLE_TIER_UP_DOUBLE, 'H', HULL);
        registerMachineRecipe(provider, ArrayUtils.subarray(GTMachines.HI_AMP_TRANSFORMER_2A, GTValues.MV, GTValues.UHV), "WCC", "TH ", "WCC", 'W', POWER_COMPONENT, 'C', CABLE_DOUBLE, 'T', CABLE_TIER_UP_DOUBLE, 'H', HULL);
        registerMachineRecipe(provider, ArrayUtils.subarray(GTMachines.HI_AMP_TRANSFORMER_4A, GTValues.ULV, GTValues.MV), " CC", "TH ", " CC", 'C', CABLE_QUAD, 'T', CABLE_TIER_UP_QUAD, 'H', HULL);
        registerMachineRecipe(provider, ArrayUtils.subarray(GTMachines.HI_AMP_TRANSFORMER_4A, GTValues.MV, GTValues.UHV), "WCC", "TH ", "WCC", 'W', POWER_COMPONENT, 'C', CABLE_QUAD, 'T', CABLE_TIER_UP_QUAD, 'H', HULL);
        registerMachineRecipe(provider, ArrayUtils.subarray(GTMachines.POWER_TRANSFORMER, GTValues.ULV, GTValues.MV), " CC", "TH ", " CC", 'C', CABLE_HEX, 'T', CABLE_TIER_UP_HEX, 'H', HULL);
        registerMachineRecipe(provider, ArrayUtils.subarray(GTMachines.POWER_TRANSFORMER, GTValues.MV, GTValues.UHV), "WCC", "TH ", "WCC", 'W', POWER_COMPONENT, 'C', CABLE_HEX, 'T', CABLE_TIER_UP_HEX, 'H', HULL);

        registerMachineRecipe(provider, GTMachines.BATTERY_BUFFER_4, "WTW", "WMW", 'M', HULL, 'W', WIRE_QUAD, 'T', CustomTags.TAG_WOODEN_CHESTS);
        registerMachineRecipe(provider, GTMachines.BATTERY_BUFFER_8, "WTW", "WMW", 'M', HULL, 'W', WIRE_OCT, 'T', CustomTags.TAG_WOODEN_CHESTS);
        registerMachineRecipe(provider, GTMachines.BATTERY_BUFFER_16, "WTW", "WMW", 'M', HULL, 'W', WIRE_HEX, 'T', CustomTags.TAG_WOODEN_CHESTS);

        registerMachineRecipe(provider, GTMachines.CHARGER_4, "WTW", "WMW", "BCB", 'M', HULL, 'W', WIRE_QUAD, 'T', CustomTags.TAG_WOODEN_CHESTS, 'B', CABLE, 'C', CIRCUIT);

        registerMachineRecipe(provider, GTMachines.FLUID_IMPORT_HATCH, " G", " M", 'M', HULL, 'G', GLASS);
        registerMachineRecipe(provider, GTMachines.FLUID_EXPORT_HATCH, " M", " G", 'M', HULL, 'G', GLASS);

        registerMachineRecipe(provider, GTMachines.ITEM_IMPORT_BUS, " C", " M", 'M', HULL, 'C', CustomTags.TAG_WOODEN_CHESTS);
        registerMachineRecipe(provider, GTMachines.ITEM_EXPORT_BUS, " M", " C", 'M', HULL, 'C', CustomTags.TAG_WOODEN_CHESTS);

        VanillaRecipeHelper.addShapedRecipe(provider, true, "wooden_crate", GTMachines.WOODEN_CRATE.asStack(), "RPR", "PsP", "RPR", 'P', ItemTags.PLANKS, 'R', new UnificationEntry(TagPrefix.screw, GTMaterials.Iron));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "bronze_crate", GTMachines.BRONZE_CRATE.asStack(), "RPR", "PhP", "RPR", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Bronze), 'R', new UnificationEntry(TagPrefix.rodLong, GTMaterials.Bronze));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "steel_crate", GTMachines.STEEL_CRATE.asStack(), "RPR", "PhP", "RPR", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Steel), 'R', new UnificationEntry(TagPrefix.rodLong, GTMaterials.Steel));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "aluminium_crate", GTMachines.ALUMINIUM_CRATE.asStack(), "RPR", "PhP", "RPR", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Aluminium), 'R', new UnificationEntry(TagPrefix.rodLong, GTMaterials.Aluminium));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "stainless_steel_crate", GTMachines.STAINLESS_STEEL_CRATE.asStack(), "RPR", "PhP", "RPR", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.StainlessSteel), 'R', new UnificationEntry(TagPrefix.rodLong, GTMaterials.StainlessSteel));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "titanium_crate", GTMachines.TITANIUM_CRATE.asStack(), "RPR", "PhP", "RPR", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Titanium), 'R', new UnificationEntry(TagPrefix.rodLong, GTMaterials.Titanium));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "tungstensteel_crate", GTMachines.TUNGSTENSTEEL_CRATE.asStack(), "RPR", "PhP", "RPR", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.TungstenSteel), 'R', new UnificationEntry(TagPrefix.rodLong, GTMaterials.TungstenSteel));

        VanillaRecipeHelper.addShapedRecipe(provider, true, "wooden_barrel", GTMachines.WOODEN_DRUM.asStack(), "rSs", "PRP", "PRP", 'S', GTItems.STICKY_RESIN.asStack(), 'P', ItemTags.PLANKS, 'R', new UnificationEntry(TagPrefix.rodLong, GTMaterials.Iron));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "bronze_drum", GTMachines.BRONZE_DRUM.asStack(), " h ", "PRP", "PRP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Bronze), 'R', new UnificationEntry(TagPrefix.rodLong, GTMaterials.Bronze));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "steel_drum", GTMachines.STEEL_DRUM.asStack(), " h ", "PRP", "PRP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Steel), 'R', new UnificationEntry(TagPrefix.rodLong, GTMaterials.Steel));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "aluminium_drum", GTMachines.ALUMINIUM_DRUM.asStack(), " h ", "PRP", "PRP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Aluminium), 'R', new UnificationEntry(TagPrefix.rodLong, GTMaterials.Aluminium));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "stainless_steel_drum", GTMachines.STAINLESS_STEEL_DRUM.asStack(), " h ", "PRP", "PRP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.StainlessSteel), 'R', new UnificationEntry(TagPrefix.rodLong, GTMaterials.StainlessSteel));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "gold_drum", GTMachines.GOLD_DRUM.asStack(), " h ", "PRP", "PRP", 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Gold), 'R', new UnificationEntry(TagPrefix.rodLong, GTMaterials.Gold));

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
        VanillaRecipeHelper.addShapedRecipe(provider, true, "super_chest_lv", GTMachines.SUPER_CHEST[LV].asStack(), "CPC", "PFP", "CPC", 'C', CustomTags.LV_CIRCUITS, 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Steel), 'F', GTMachines.STEEL_CRATE.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "super_chest_mv", GTMachines.SUPER_CHEST[MV].asStack(), "CPC", "PFP", "CPC", 'C', CustomTags.MV_CIRCUITS, 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Aluminium), 'F', GTMachines.ALUMINIUM_CRATE.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "super_chest_hv", GTMachines.SUPER_CHEST[HV].asStack(), "CPC", "PFP", "CGC", 'C', CustomTags.HV_CIRCUITS, 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.StainlessSteel), 'F', GTMachines.STAINLESS_STEEL_CRATE.asStack(), 'G', GTItems.FIELD_GENERATOR_LV.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "super_chest_ev", GTMachines.SUPER_CHEST[EV].asStack(), "CPC", "PFP", "CGC", 'C', CustomTags.EV_CIRCUITS, 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Titanium), 'F', GTMachines.TITANIUM_CRATE.asStack(), 'G', GTItems.FIELD_GENERATOR_MV.asStack());

        VanillaRecipeHelper.addShapedRecipe(provider, true, "quantum_chest_iv", GTMachines.QUANTUM_CHEST[IV].asStack(), "CPC", "PHP", "CFC", 'C', CustomTags.IV_CIRCUITS, 'P', new UnificationEntry(TagPrefix.plateDense, GTMaterials.TungstenSteel), 'F', GTItems.FIELD_GENERATOR_HV.asStack(), 'H', GTMachines.HULL[5].asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "quantum_chest_luv", GTMachines.QUANTUM_CHEST[LuV].asStack(), "CPC", "PHP", "CFC", 'C', CustomTags.LuV_CIRCUITS, 'P', new UnificationEntry(TagPrefix.plateDense, GTMaterials.RhodiumPlatedPalladium), 'F', GTItems.FIELD_GENERATOR_EV.asStack(), 'H', GTMachines.HULL[6].asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "quantum_chest_zpm", GTMachines.QUANTUM_CHEST[ZPM].asStack(), "CPC", "PHP", "CFC", 'C', CustomTags.ZPM_CIRCUITS, 'P', new UnificationEntry(TagPrefix.plateDense, GTMaterials.NaquadahAlloy), 'F', GTItems.FIELD_GENERATOR_IV.asStack(), 'H', GTMachines.HULL[7].asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "quantum_chest_uv", GTMachines.QUANTUM_CHEST[UV].asStack(), "CPC", "PHP", "CFC", 'C', CustomTags.UV_CIRCUITS, 'P', new UnificationEntry(TagPrefix.plateDense, GTMaterials.Darmstadtium), 'F', GTItems.FIELD_GENERATOR_LuV.asStack(), 'H', GTMachines.HULL[8].asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "quantum_chest_uhv", GTMachines.QUANTUM_CHEST[UHV].asStack(), "CPC", "PHP", "CFC", 'C', CustomTags.UHV_CIRCUITS, 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Neutronium), 'F', GTItems.FIELD_GENERATOR_ZPM.asStack(), 'H', GTMachines.HULL[9].asStack());

        // Super / Quantum Tanks
        VanillaRecipeHelper.addShapedRecipe(provider, true, "super_tank_lv", GTMachines.SUPER_TANK[LV].asStack(), "CPC", "PHP", "CFC", 'C', CustomTags.LV_CIRCUITS, 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Steel), 'F', GTItems.ELECTRIC_PUMP_LV.asStack(), 'H', GTBlocks.HERMETIC_CASING_LV.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "super_tank_mv", GTMachines.SUPER_TANK[MV].asStack(), "CPC", "PHP", "CFC", 'C', CustomTags.MV_CIRCUITS, 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Aluminium), 'F', GTItems.ELECTRIC_PUMP_MV.asStack(), 'H', GTBlocks.HERMETIC_CASING_MV.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "super_tank_hv", GTMachines.SUPER_TANK[HV].asStack(), "CGC", "PHP", "CFC", 'C', CustomTags.HV_CIRCUITS, 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.StainlessSteel), 'F', GTItems.ELECTRIC_PUMP_HV.asStack(), 'H', GTBlocks.HERMETIC_CASING_HV.asStack(), 'G', GTItems.FIELD_GENERATOR_LV.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "super_tank_ev", GTMachines.SUPER_TANK[EV].asStack(), "CGC", "PHP", "CFC", 'C', CustomTags.EV_CIRCUITS, 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Titanium), 'F', GTItems.ELECTRIC_PUMP_EV.asStack(), 'H', GTBlocks.HERMETIC_CASING_EV.asStack(), 'G', GTItems.FIELD_GENERATOR_MV.asStack());

        VanillaRecipeHelper.addShapedRecipe(provider, true, "quantum_tank_iv", GTMachines.QUANTUM_TANK[IV].asStack(), "CGC", "PHP", "CUC", 'C', CustomTags.IV_CIRCUITS, 'P', new UnificationEntry(TagPrefix.plateDense, GTMaterials.TungstenSteel), 'U', GTItems.ELECTRIC_PUMP_IV.asStack(), 'G', GTItems.FIELD_GENERATOR_HV.asStack(), 'H', GTBlocks.HERMETIC_CASING_IV.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "quantum_tank_luv", GTMachines.QUANTUM_TANK[LuV].asStack(), "CGC", "PHP", "CUC", 'C', CustomTags.LuV_CIRCUITS, 'P', new UnificationEntry(TagPrefix.plateDense, GTMaterials.RhodiumPlatedPalladium), 'U', GTItems.ELECTRIC_PUMP_LuV.asStack(), 'G', GTItems.FIELD_GENERATOR_EV.asStack(), 'H', GTBlocks.HERMETIC_CASING_LuV.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "quantum_tank_zpm", GTMachines.QUANTUM_TANK[ZPM].asStack(), "CGC", "PHP", "CUC", 'C', CustomTags.ZPM_CIRCUITS, 'P', new UnificationEntry(TagPrefix.plateDense, GTMaterials.NaquadahAlloy), 'U', GTItems.ELECTRIC_PUMP_ZPM.asStack(), 'G', GTItems.FIELD_GENERATOR_IV.asStack(), 'H', GTBlocks.HERMETIC_CASING_ZPM.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "quantum_tank_uv", GTMachines.QUANTUM_TANK[UV].asStack(), "CGC", "PHP", "CUC", 'C', CustomTags.UV_CIRCUITS, 'P', new UnificationEntry(TagPrefix.plateDense, GTMaterials.Darmstadtium), 'U', GTItems.ELECTRIC_PUMP_UV.asStack(), 'G', GTItems.FIELD_GENERATOR_LuV.asStack(), 'H', GTBlocks.HERMETIC_CASING_UV.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "quantum_tank_uhv", GTMachines.QUANTUM_TANK[UHV].asStack(), "CGC", "PHP", "CUC", 'C', CustomTags.UHV_CIRCUITS, 'P', new UnificationEntry(TagPrefix.plate, GTMaterials.Neutronium), 'U', GTItems.ELECTRIC_PUMP_UV.asStack(), 'G', GTItems.FIELD_GENERATOR_ZPM.asStack(), 'H', GTBlocks.HERMETIC_CASING_UHV.asStack());

        // TODO Buffers
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "buffer_lv", GTMachines.BUFFER[0].getStackForm(), "HP", "CV", 'H', GTMachines.HULL[GTValues.LV].getStackForm(), 'P', MetaItems.ELECTRIC_PUMP_LV.getStackForm(), 'V', MetaItems.CONVEYOR_MODULE_LV.getStackForm(), 'C', new UnificationEntry(TagPrefix.circuit, MarkerGTMaterials.Tier.LV));
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "buffer_mv", GTMachines.BUFFER[1].getStackForm(), "HP", "CV", 'H', GTMachines.HULL[GTValues.MV].getStackForm(), 'P', MetaItems.ELECTRIC_PUMP_MV.getStackForm(), 'V', MetaItems.CONVEYOR_MODULE_MV.getStackForm(), 'C', new UnificationEntry(TagPrefix.circuit, MarkerGTMaterials.Tier.LV));
        //VanillaRecipeHelper.addShapedRecipe(provider, true, "buffer_hv", GTMachines.BUFFER[2].getStackForm(), "HP", "CV", 'H', GTMachines.HULL[GTValues.HV].getStackForm(), 'P', MetaItems.ELECTRIC_PUMP_HV.getStackForm(), 'V', MetaItems.CONVEYOR_MODULE_HV.getStackForm(), 'C', new UnificationEntry(TagPrefix.circuit, MarkerGTMaterials.Tier.LV));

        VanillaRecipeHelper.addShapedRecipe(provider, true, "cleanroom", GTMachines.CLEANROOM.asStack(), "FFF", "RHR", "MCM", 'F', GTItems.ITEM_FILTER.asStack(), 'R', new UnificationEntry(TagPrefix.rotor, GTMaterials.StainlessSteel), 'H', HULL.getIngredient(HV), 'M', GTItems.ELECTRIC_MOTOR_HV.asStack(), 'C', CustomTags.HV_CIRCUITS);

        if (ConfigHolder.INSTANCE.compat.energy.enablePlatformConverters && GTCEu.isRebornEnergyLoaded()) {
            registerMachineRecipe(provider, true, GTMachines.ENERGY_CONVERTER_1A, " WW", "RMC", " WW", 'C', CIRCUIT, 'M', HULL, 'W', CABLE, 'R', new UnificationEntry(TagPrefix.cableGtSingle, GTMaterials.RedAlloy));
            registerMachineRecipe(provider, true, GTMachines.ENERGY_CONVERTER_4A, " WW", "RMC", " WW", 'C', CIRCUIT, 'M', HULL, 'W', CABLE_QUAD, 'R', new UnificationEntry(TagPrefix.cableGtQuadruple, GTMaterials.RedAlloy));
            registerMachineRecipe(provider, true, GTMachines.ENERGY_CONVERTER_8A, " WW", "RMC", " WW", 'C', CIRCUIT, 'M', HULL, 'W', CABLE_OCT, 'R', new UnificationEntry(TagPrefix.cableGtOctal, GTMaterials.RedAlloy));
            registerMachineRecipe(provider, true, GTMachines.ENERGY_CONVERTER_16A, " WW", "RMC", " WW", 'C', CIRCUIT, 'M', HULL, 'W', CABLE_HEX, 'R', new UnificationEntry(TagPrefix.cableGtHex, GTMaterials.RedAlloy));
        }
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
            } else if (recipe[i] instanceof ItemProviderEntry<?> itemEntry) {
                recipe[i] = itemEntry.asStack();
            }
        }
        return recipe;
    }
}
