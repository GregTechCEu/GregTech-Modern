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
                .inputItems(plate, Bronze, 4)
                .inputItems(gear, Bronze, 2)
                .inputItems(frameGt, Bronze)
                .circuitMeta(4)
                .outputItems(CASING_BRONZE_GEARBOX.asStack(ConfigHolder.INSTANCE.recipes.casingsPerCraft))
                .duration(50).EUt(16).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("steel_gearbox_casing")
                .inputItems(plate, Steel, 4)
                .inputItems(gear, Steel, 2)
                .inputItems(frameGt, Steel)
                .circuitMeta(4)
                .outputItems(CASING_STEEL_GEARBOX.asStack(ConfigHolder.INSTANCE.recipes.casingsPerCraft))
                .duration(50).EUt(16).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("stainless_steel_gearbox_casing")
                .inputItems(plate, StainlessSteel, 4)
                .inputItems(gear, StainlessSteel, 2)
                .inputItems(frameGt, StainlessSteel)
                .circuitMeta(4)
                .outputItems(CASING_STAINLESS_STEEL_GEARBOX.asStack(ConfigHolder.INSTANCE.recipes.casingsPerCraft))
                .duration(50).EUt(16).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("titanium_gearbox_casing")
                .inputItems(plate, Titanium, 4)
                .inputItems(gear, Titanium, 2)
                .inputItems(frameGt, Titanium)
                .circuitMeta(4)
                .outputItems(CASING_TITANIUM_GEARBOX.asStack(ConfigHolder.INSTANCE.recipes.casingsPerCraft))
                .duration(50).EUt(16).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("tungstensteel_gearbox_casing")
                .inputItems(plate, TungstenSteel, 4)
                .inputItems(gear, TungstenSteel, 2)
                .inputItems(frameGt, TungstenSteel)
                .circuitMeta(4)
                .outputItems(CASING_TUNGSTENSTEEL_GEARBOX.asStack(ConfigHolder.INSTANCE.recipes.casingsPerCraft))
                .duration(50).EUt(16).save(provider);

        // Other
        ASSEMBLER_RECIPES.recipeBuilder("stable_titanium_casing")
                .inputItems(rotor, Titanium, 2)
                .inputItems(pipeNormalFluid, Titanium, 4)
                .inputItems(CASING_TITANIUM_STABLE.asStack())
                .outputItems(CASING_ENGINE_INTAKE.asStack(ConfigHolder.INSTANCE.recipes.casingsPerCraft))
                .duration(50).EUt(16).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("stable_tungstensteel_casing")
                .inputItems(rotor, TungstenSteel, 2)
                .inputItems(pipeNormalFluid, TungstenSteel, 4)
                .inputItems(CASING_TUNGSTENSTEEL_ROBUST.asStack())
                .outputItems(CASING_EXTREME_ENGINE_INTAKE.asStack(ConfigHolder.INSTANCE.recipes.casingsPerCraft))
                .duration(50).EUt(16).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("spray_can_empty")
                .inputItems(dust, Redstone)
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
                .inputItems(plate, Invar, 2)
                .inputItems(Items.FLINT)
                .outputItems(TOOL_LIGHTER_INVAR)
                .duration(256).EUt(16).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("tool_lighter_platinum")
                .inputItems(plate, Platinum, 2)
                .inputItems(Items.FLINT)
                .outputItems(TOOL_LIGHTER_PLATINUM)
                .duration(256).EUt(256).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("tool_matches_0")
                .inputItems(bolt, Wood)
                .inputItems(dustSmall, Phosphorus)
                .outputItems(TOOL_MATCHES)
                .duration(16).EUt(16).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("tool_matches_1")
                .inputItems(bolt, Wood)
                .inputItems(dustSmall, TricalciumPhosphate)
                .outputItems(TOOL_MATCHES)
                .duration(16).EUt(16).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("tool_matches_2")
                .inputItems(bolt, Wood, 4)
                .inputItems(dust, Phosphorus)
                .outputItems(TOOL_MATCHES, 4)
                .duration(64).EUt(16).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("tool_matches_3")
                .inputItems(bolt, Wood, 4)
                .inputItems(dust, TricalciumPhosphate)
                .outputItems(TOOL_MATCHES, 4)
                .duration(64).EUt(16).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("small_wood_pipe").duration(200).EUt(VA[LV])
                .inputItems(plate, Wood)
                .circuitMeta(12)
                .inputFluids(Glue.getFluid(50))
                .outputItems(pipeSmallFluid, Wood)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("normal_wood_pipe").duration(200).EUt(VA[LV])
                .inputItems(plate, Wood, 3)
                .circuitMeta(6)
                .inputFluids(Glue.getFluid(20))
                .outputItems(pipeNormalFluid, Wood)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("large_wood_pipe").duration(100).EUt(VA[LV])
                .inputItems(plate, Wood, 6)
                .circuitMeta(2)
                .inputFluids(Glue.getFluid(10))
                .outputItems(pipeLargeFluid, Wood)
                .save(provider);

        // Treated Wood Pipes
        ASSEMBLER_RECIPES.recipeBuilder("small_treated_wood_pipe").duration(200).EUt(VA[LV])
                .inputItems(plate, TreatedWood)
                .circuitMeta(12)
                .inputFluids(Glue.getFluid(50))
                .outputItems(pipeSmallFluid, TreatedWood)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("normal_treated_wood_pipe").duration(200).EUt(VA[LV])
                .inputItems(plate, TreatedWood, 3)
                .circuitMeta(6)
                .inputFluids(Glue.getFluid(20))
                .outputItems(pipeNormalFluid, TreatedWood)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("large_treated_wood_pipe").duration(100).EUt(VA[LV])
                .inputItems(plate, TreatedWood, 6)
                .circuitMeta(2)
                .inputFluids(Glue.getFluid(10))
                .outputItems(pipeLargeFluid, TreatedWood)
                .save(provider);

        // Voltage Coils
        ASSEMBLER_RECIPES.recipeBuilder("voltage_coil_ulv").duration(200).EUt(VA[ULV])
                .inputItems(rod, IronMagnetic)
                .inputItems(wireFine, Lead, 16)
                .circuitMeta(1)
                .outputItems(VOLTAGE_COIL_ULV)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("voltage_coil_lv").duration(200).EUt(VA[LV])
                .inputItems(rod, IronMagnetic)
                .inputItems(wireFine, Steel, 16)
                .circuitMeta(1)
                .outputItems(VOLTAGE_COIL_LV)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("voltage_coil_mv").duration(200).EUt(VA[MV])
                .inputItems(rod, SteelMagnetic)
                .inputItems(wireFine, Aluminium, 16)
                .circuitMeta(1)
                .outputItems(VOLTAGE_COIL_MV)
                .addMaterialInfo(true)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("voltage_coil_hv").duration(200).EUt(VA[HV])
                .inputItems(rod, SteelMagnetic)
                .inputItems(wireFine, BlackSteel, 16)
                .circuitMeta(1)
                .outputItems(VOLTAGE_COIL_HV)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("voltage_coil_ev").duration(200).EUt(VA[EV])
                .inputItems(rod, NeodymiumMagnetic)
                .inputItems(wireFine, Platinum, 16)
                .circuitMeta(1)
                .outputItems(VOLTAGE_COIL_EV)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("voltage_coil_iv").duration(200).EUt(VA[IV])
                .inputItems(rod, NeodymiumMagnetic)
                .inputItems(wireFine, Iridium, 16)
                .circuitMeta(1)
                .outputItems(VOLTAGE_COIL_IV)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("voltage_coil_luv").duration(200).EUt(VA[LuV])
                .inputItems(rod, SamariumMagnetic)
                .inputItems(wireFine, Osmiridium, 16)
                .circuitMeta(1)
                .outputItems(VOLTAGE_COIL_LuV)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("voltage_coil_zpm").duration(200).EUt(VA[ZPM])
                .inputItems(rod, SamariumMagnetic)
                .inputItems(wireFine, Europium, 16)
                .circuitMeta(1)
                .outputItems(VOLTAGE_COIL_ZPM)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("voltage_coil_uv").duration(200).EUt(VA[UV])
                .inputItems(rod, SamariumMagnetic)
                .inputItems(wireFine, Tritanium, 16)
                .circuitMeta(1)
                .outputItems(VOLTAGE_COIL_UV)
                .save(provider);

        // Neutron Reflector
        ASSEMBLER_RECIPES.recipeBuilder("neutron_reflector").duration(4000).EUt(VA[MV])
                .inputItems(plate, Ruridit)
                .inputItems(plateDouble, Beryllium, 4)
                .inputItems(plateDouble, TungstenCarbide, 2)
                .inputFluids(TinAlloy.getFluid(L * 32))
                .outputItems(NEUTRON_REFLECTOR)
                .addMaterialInfo(true)
                .save(provider);

        // hazmat pieces
        ASSEMBLER_RECIPES.recipeBuilder("hazmat_boots").duration(200).EUt(VA[LV])
                .inputItems(plate, Rubber, 4)
                .inputItems(foil, Polyethylene, 2)
                .inputItems(plate, PolyvinylChloride, 2)
                .outputItems(HAZMAT_BOOTS)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("hazmat_chestpiece").duration(200).EUt(VA[LV])
                .inputItems(plate, Rubber, 2)
                .inputItems(plate, PolyvinylChloride, 7)
                .inputItems(plate, Lead, 3)
                .outputItems(HAZMAT_CHESTPLATE)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("hazmat_leggings").duration(200).EUt(VA[LV])
                .inputItems(plate, Rubber, 2)
                .inputItems(plate, PolyvinylChloride, 5)
                .inputItems(rod, Iron, 2)
                .inputItems(ring, Steel, 4)
                .outputItems(HAZMAT_LEGGINGS)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("hazmat_headpiece").duration(200).EUt(VA[LV])
                .inputItems(MASK_FILTER)
                .inputItems(plate, PolyvinylChloride, 4)
                .inputItems(plate, Glass, 2)
                .inputItems(ring, PolyvinylChloride, 1)
                .outputItems(HAZMAT_HELMET)
                .save(provider);
    }
}
