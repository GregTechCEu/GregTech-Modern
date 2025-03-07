package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.GTItems.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ASSEMBLER_RECIPES;

public class AssemblerRecipeLoader {

    public static void init(Consumer<FinishedRecipe> provider) {
        // Gearbox-like
        ASSEMBLER_RECIPES.recipeBuilder("bronze_gearbox_casing")
                .inputItems(PLATE, Bronze, 4)
                .inputItems(GEAR, Bronze, 2)
                .inputItems(FRAME_GT, Bronze)
                .circuitMeta(4)
                .outputItems(CASING_BRONZE_GEARBOX.asStack(ConfigHolder.INSTANCE.recipes.casingsPerCraft))
                .duration(50).EUt(16).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("steel_gearbox_casing")
                .inputItems(PLATE, Steel, 4)
                .inputItems(GEAR, Steel, 2)
                .inputItems(FRAME_GT, Steel)
                .circuitMeta(4)
                .outputItems(CASING_STEEL_GEARBOX.asStack(ConfigHolder.INSTANCE.recipes.casingsPerCraft))
                .duration(50).EUt(16).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("stainless_steel_gearbox_casing")
                .inputItems(PLATE, StainlessSteel, 4)
                .inputItems(GEAR, StainlessSteel, 2)
                .inputItems(FRAME_GT, StainlessSteel)
                .circuitMeta(4)
                .outputItems(CASING_STAINLESS_STEEL_GEARBOX.asStack(ConfigHolder.INSTANCE.recipes.casingsPerCraft))
                .duration(50).EUt(16).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("titanium_gearbox_casing")
                .inputItems(PLATE, Titanium, 4)
                .inputItems(GEAR, Titanium, 2)
                .inputItems(FRAME_GT, Titanium)
                .circuitMeta(4)
                .outputItems(CASING_TITANIUM_GEARBOX.asStack(ConfigHolder.INSTANCE.recipes.casingsPerCraft))
                .duration(50).EUt(16).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("tungstensteel_gearbox_casing")
                .inputItems(PLATE, TungstenSteel, 4)
                .inputItems(GEAR, TungstenSteel, 2)
                .inputItems(FRAME_GT, TungstenSteel)
                .circuitMeta(4)
                .outputItems(CASING_TUNGSTENSTEEL_GEARBOX.asStack(ConfigHolder.INSTANCE.recipes.casingsPerCraft))
                .duration(50).EUt(16).save(provider);

        // Other
        ASSEMBLER_RECIPES.recipeBuilder("stable_titanium_casing")
                .inputItems(ROTOR, Titanium, 2)
                .inputItems(PIPE_NORMAL_FLUID, Titanium, 4)
                .inputItems(CASING_TITANIUM_STABLE.asStack())
                .outputItems(CASING_ENGINE_INTAKE.asStack(ConfigHolder.INSTANCE.recipes.casingsPerCraft))
                .duration(50).EUt(16).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("stable_tungstensteel_casing")
                .inputItems(ROTOR, TungstenSteel, 2)
                .inputItems(PIPE_NORMAL_FLUID, TungstenSteel, 4)
                .inputItems(CASING_TUNGSTENSTEEL_ROBUST.asStack())
                .outputItems(CASING_EXTREME_ENGINE_INTAKE.asStack(ConfigHolder.INSTANCE.recipes.casingsPerCraft))
                .duration(50).EUt(16).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("spray_can_empty")
                .inputItems(DUST, Redstone)
                .inputItems(FLUID_CELL)
                .outputItems(SPRAY_EMPTY)
                .duration(200).EUt(VA[ULV]).save(provider);

        // TODO Foam Sprayer
        // ASSEMBLER_RECIPES.recipeBuilder("foam_sprayer")
        // .inputItems(plate, Tin, 6)
        // .inputItems(SPRAY_EMPTY)
        // .inputItems(paneGlass.name(), 1)
        // .outputItems(FOAM_SPRAYER)
        // .duration(200).EUt(VA[ULV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("tool_lighter_invar")
                .inputItems(PLATE, Invar, 2)
                .inputItems(Items.FLINT)
                .outputItems(TOOL_LIGHTER_INVAR)
                .duration(256).EUt(16).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("tool_lighter_platinum")
                .inputItems(PLATE, Platinum, 2)
                .inputItems(Items.FLINT)
                .outputItems(TOOL_LIGHTER_PLATINUM)
                .duration(256).EUt(256).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("tool_matches_0")
                .inputItems(BOLT, Wood)
                .inputItems(DUST_SMALL, Phosphorus)
                .outputItems(TOOL_MATCHES)
                .duration(16).EUt(16).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("tool_matches_1")
                .inputItems(BOLT, Wood)
                .inputItems(DUST_SMALL, TricalciumPhosphate)
                .outputItems(TOOL_MATCHES)
                .duration(16).EUt(16).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("tool_matches_2")
                .inputItems(BOLT, Wood, 4)
                .inputItems(DUST, Phosphorus)
                .outputItems(TOOL_MATCHES, 4)
                .duration(64).EUt(16).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("tool_matches_3")
                .inputItems(BOLT, Wood, 4)
                .inputItems(DUST, TricalciumPhosphate)
                .outputItems(TOOL_MATCHES, 4)
                .duration(64).EUt(16).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("small_wood_pipe").duration(200).EUt(VA[LV])
                .inputItems(PLATE, Wood)
                .circuitMeta(12)
                .inputFluids(Glue.getFluid(50))
                .outputItems(PIPE_SMALL_FLUID, Wood)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("normal_wood_pipe").duration(200).EUt(VA[LV])
                .inputItems(PLATE, Wood, 3)
                .circuitMeta(6)
                .inputFluids(Glue.getFluid(20))
                .outputItems(PIPE_NORMAL_FLUID, Wood)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("large_wood_pipe").duration(100).EUt(VA[LV])
                .inputItems(PLATE, Wood, 6)
                .circuitMeta(2)
                .inputFluids(Glue.getFluid(10))
                .outputItems(PIPE_LARGE_FLUID, Wood)
                .save(provider);

        // Treated Wood Pipes
        ASSEMBLER_RECIPES.recipeBuilder("small_treated_wood_pipe").duration(200).EUt(VA[LV])
                .inputItems(PLATE, TreatedWood)
                .circuitMeta(12)
                .inputFluids(Glue.getFluid(50))
                .outputItems(PIPE_SMALL_FLUID, TreatedWood)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("normal_treated_wood_pipe").duration(200).EUt(VA[LV])
                .inputItems(PLATE, TreatedWood, 3)
                .circuitMeta(6)
                .inputFluids(Glue.getFluid(20))
                .outputItems(PIPE_NORMAL_FLUID, TreatedWood)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("large_treated_wood_pipe").duration(100).EUt(VA[LV])
                .inputItems(PLATE, TreatedWood, 6)
                .circuitMeta(2)
                .inputFluids(Glue.getFluid(10))
                .outputItems(PIPE_LARGE_FLUID, TreatedWood)
                .save(provider);

        // Voltage Coils
        ASSEMBLER_RECIPES.recipeBuilder("voltage_coil_ulv").duration(200).EUt(VA[ULV])
                .inputItems(ROD, IronMagnetic)
                .inputItems(WIRE_FINE, Lead, 16)
                .circuitMeta(1)
                .outputItems(VOLTAGE_COIL_ULV)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("voltage_coil_lv").duration(200).EUt(VA[LV])
                .inputItems(ROD, IronMagnetic)
                .inputItems(WIRE_FINE, Steel, 16)
                .circuitMeta(1)
                .outputItems(VOLTAGE_COIL_LV)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("voltage_coil_mv").duration(200).EUt(VA[MV])
                .inputItems(ROD, SteelMagnetic)
                .inputItems(WIRE_FINE, Aluminium, 16)
                .circuitMeta(1)
                .outputItems(VOLTAGE_COIL_MV)
                .addMaterialInfo(true)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("voltage_coil_hv").duration(200).EUt(VA[HV])
                .inputItems(ROD, SteelMagnetic)
                .inputItems(WIRE_FINE, BlackSteel, 16)
                .circuitMeta(1)
                .outputItems(VOLTAGE_COIL_HV)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("voltage_coil_ev").duration(200).EUt(VA[EV])
                .inputItems(ROD, NeodymiumMagnetic)
                .inputItems(WIRE_FINE, Platinum, 16)
                .circuitMeta(1)
                .outputItems(VOLTAGE_COIL_EV)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("voltage_coil_iv").duration(200).EUt(VA[IV])
                .inputItems(ROD, NeodymiumMagnetic)
                .inputItems(WIRE_FINE, Iridium, 16)
                .circuitMeta(1)
                .outputItems(VOLTAGE_COIL_IV)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("voltage_coil_luv").duration(200).EUt(VA[LuV])
                .inputItems(ROD, SamariumMagnetic)
                .inputItems(WIRE_FINE, Osmiridium, 16)
                .circuitMeta(1)
                .outputItems(VOLTAGE_COIL_LuV)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("voltage_coil_zpm").duration(200).EUt(VA[ZPM])
                .inputItems(ROD, SamariumMagnetic)
                .inputItems(WIRE_FINE, Europium, 16)
                .circuitMeta(1)
                .outputItems(VOLTAGE_COIL_ZPM)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("voltage_coil_uv").duration(200).EUt(VA[UV])
                .inputItems(ROD, SamariumMagnetic)
                .inputItems(WIRE_FINE, Tritanium, 16)
                .circuitMeta(1)
                .outputItems(VOLTAGE_COIL_UV)
                .save(provider);

        // Neutron Reflector
        ASSEMBLER_RECIPES.recipeBuilder("neutron_reflector").duration(4000).EUt(VA[MV])
                .inputItems(PLATE, Ruridit)
                .inputItems(PLATE_DOUBLE, Beryllium, 4)
                .inputItems(PLATE_DOUBLE, TungstenCarbide, 2)
                .inputFluids(TinAlloy.getFluid(L * 32))
                .outputItems(NEUTRON_REFLECTOR)
                .addMaterialInfo(true)
                .save(provider);

        // hazmat pieces
        ASSEMBLER_RECIPES.recipeBuilder("hazmat_boots").duration(200).EUt(VA[LV])
                .inputItems(PLATE, Rubber, 4)
                .inputItems(FOIL, Polyethylene, 2)
                .inputItems(PLATE, PolyvinylChloride, 2)
                .outputItems(HAZMAT_BOOTS)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("hazmat_chestpiece").duration(200).EUt(VA[LV])
                .inputItems(PLATE, Rubber, 2)
                .inputItems(PLATE, PolyvinylChloride, 7)
                .inputItems(PLATE, Lead, 3)
                .outputItems(HAZMAT_CHESTPLATE)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("hazmat_leggings").duration(200).EUt(VA[LV])
                .inputItems(PLATE, Rubber, 2)
                .inputItems(PLATE, PolyvinylChloride, 5)
                .inputItems(ROD, Iron, 2)
                .inputItems(RING, Steel, 4)
                .outputItems(HAZMAT_LEGGINGS)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("hazmat_headpiece").duration(200).EUt(VA[LV])
                .inputItems(MASK_FILTER)
                .inputItems(PLATE, PolyvinylChloride, 4)
                .inputItems(PLATE, Glass, 2)
                .inputItems(RING, PolyvinylChloride, 1)
                .outputItems(HAZMAT_HELMET)
                .save(provider);
    }
}
