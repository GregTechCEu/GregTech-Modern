package com.gregtechceu.gtceu.data.recipe.chemistry;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.common.block.variant.ActiveCasingBlock;
import com.gregtechceu.gtceu.common.block.variant.CasingBlock;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.common.libs.GTMaterials.*;
import static com.gregtechceu.gtceu.api.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.libs.GTRecipeTypes.*;
import static com.gregtechceu.gtceu.common.libs.GTItems.*;
import static com.gregtechceu.gtceu.common.libs.GTBlocks.*;

public class AssemblerRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {

        // Gearbox-like
        ASSEMBLER_RECIPES.recipeBuilder(CasingBlock.CasingType.BRONZE_BRICKS.getName())
                .inputItems(plate, Bronze, 4)
                .inputItems(gear, Bronze, 2)
                .inputItems(frameGt, Bronze)
                .circuitMeta(4)
                .outputItems(CASING.get().getItemVariant(CasingBlock.CasingType.BRONZE_BRICKS, 2))
                .duration(50).EUt(16).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(CasingBlock.CasingType.STEEL_GEARBOX.getName())
                .inputItems(plate, Steel, 4)
                .inputItems(gear, Steel, 2)
                .inputItems(frameGt, Steel)
                .circuitMeta(4)
                .outputItems(CASING.get().getItemVariant(CasingBlock.CasingType.STEEL_GEARBOX, 2))
                .duration(50).EUt(16).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(CasingBlock.CasingType.STAINLESS_STEEL_GEARBOX.getName())
                .inputItems(plate, StainlessSteel, 4)
                .inputItems(gear, StainlessSteel, 2)
                .inputItems(frameGt, StainlessSteel)
                .circuitMeta(4)
                .outputItems(CASING.get().getItemVariant(CasingBlock.CasingType.STAINLESS_STEEL_GEARBOX, 2))
                .duration(50).EUt(16).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(CasingBlock.CasingType.TITANIUM_GEARBOX.getName())
                .inputItems(plate, Titanium, 4)
                .inputItems(gear, Titanium, 2)
                .inputItems(frameGt, Titanium)
                .circuitMeta(4)
                .outputItems(CASING.get().getItemVariant(CasingBlock.CasingType.TITANIUM_GEARBOX, 2))
                .duration(50).EUt(16).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(CasingBlock.CasingType.TUNGSTENSTEEL_GEARBOX.getName())
                .inputItems(plate, TungstenSteel, 4)
                .inputItems(gear, TungstenSteel, 2)
                .inputItems(frameGt, TungstenSteel)
                .circuitMeta(4)
                .outputItems(CASING.get().getItemVariant(CasingBlock.CasingType.TUNGSTENSTEEL_GEARBOX, 2))
                .duration(50).EUt(16).save(provider);

        // Other
        ASSEMBLER_RECIPES.recipeBuilder(ActiveCasingBlock.CasingType.ENGINE_INTAKE_CASING.getName())
                .inputItems(rotor, Titanium, 2)
                .inputItems(pipeNormalFluid, Titanium, 4)
                .inputItems(CASING.get().getItemVariant(CasingBlock.CasingType.TITANIUM_STABLE))
                .outputItems(ACTIVE_CASING.get().getItemVariant(ActiveCasingBlock.CasingType.ENGINE_INTAKE_CASING, 2))
                .duration(50).EUt(16).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(ActiveCasingBlock.CasingType.EXTREME_ENGINE_INTAKE_CASING.getName())
                .inputItems(rotor, TungstenSteel, 2)
                .inputItems(pipeNormalFluid, TungstenSteel, 4)
                .inputItems(CASING.get().getItemVariant(CasingBlock.CasingType.TUNGSTENSTEEL_ROBUST))
                .outputItems(ACTIVE_CASING.get().getItemVariant(ActiveCasingBlock.CasingType.EXTREME_ENGINE_INTAKE_CASING, 2))
                .duration(50).EUt(16).save(provider);

        // TODO lighter
//        ASSEMBLER_RECIPES.recipeBuilder()
//                .inputItems(plate, Invar, 2)
//                .inputItems(new ItemStack(Items.FLINT))
//                .outputItems(TOOL_LIGHTER_INVAR)
//                .duration(256).EUt(16).save(provider);
//
//        ASSEMBLER_RECIPES.recipeBuilder()
//                .inputItems(plate, Platinum, 2)
//                .inputItems(new ItemStack(Items.FLINT))
//                .outputItems(TOOL_LIGHTER_PLATINUM)
//                .duration(256).EUt(256).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(SPRAY_EMPTY.getId())
                .inputItems(dust, Redstone)
                .inputItems(FLUID_CELL.asStack())
                .outputItems(SPRAY_EMPTY.asStack())
                .duration(200).EUt(GTValues.VA[GTValues.ULV]).save(provider);

        // TODO spray
//        ASSEMBLER_RECIPES.recipeBuilder()
//                .inputItems(plate, Tin, 6)
//                .inputItems(SPRAY_EMPTY.asStack())
//                .inputItems(paneGlass.name(), 1)
//                .outputItems(FOAM_SPRAYER)
//                .duration(200).EUt(VA[ULV]).save(provider);

        // Matches/lighters recipes
//        ASSEMBLER_RECIPES.recipeBuilder()
//                .inputItems(bolt, Wood)
//                .inputItems(dustSmall, Phosphorus)
//                .outputItems(TOOL_MATCHES)
//                .duration(16).EUt(16).save(provider);

//        ASSEMBLER_RECIPES.recipeBuilder()
//                .inputItems(bolt, Wood)
//                .inputItems(dustSmall, TricalciumPhosphate)
//                .outputItems(TOOL_MATCHES)
//                .duration(16).EUt(16).save(provider);
//
//        ASSEMBLER_RECIPES.recipeBuilder()
//                .inputItems(bolt, Wood, 4)
//                .inputItems(dust, Phosphorus)
//                .outputItems(TOOL_MATCHES, 4)
//                .duration(64).EUt(16).save(provider);
//
//        ASSEMBLER_RECIPES.recipeBuilder()
//                .inputItems(bolt, Wood, 4)
//                .inputItems(dust, TricalciumPhosphate)
//                .outputItems(TOOL_MATCHES, 4)
//                .duration(64).EUt(16).save(provider);

        // Wood Pipes todo wood fluid pipe
//        ASSEMBLER_RECIPES.recipeBuilder("wood_pipe_small_fluid").duration(200).EUt(VA[LV])
//                .inputItems(plate, Wood)
//                .circuitMeta(12)
//                .inputFluids(Glue.getFluid(50))
//                .outputItems(pipeSmallFluid, Wood)
//                .save(provider);
//
//        ASSEMBLER_RECIPES.recipeBuilder("wood_pipe_normal_fluid").duration(200).EUt(VA[LV])
//                .inputItems(plate, Wood, 3)
//                .circuitMeta(6)
//                .inputFluids(Glue.getFluid(20))
//                .outputItems(pipeNormalFluid, Wood)
//                .save(provider);
//
//        ASSEMBLER_RECIPES.recipeBuilder("wood_pipe_large_fluid").duration(100).EUt(VA[LV])
//                .inputItems(plate, Wood, 6)
//                .circuitMeta(2)
//                .inputFluids(Glue.getFluid(10))
//                .outputItems(pipeLargeFluid, Wood)
//                .save(provider);
//
//        // Treated Wood Pipes
//        ASSEMBLER_RECIPES.recipeBuilder("treated_wood_pipe_small_fluid").duration(200).EUt(VA[LV])
//                .inputItems(plate, TreatedWood)
//                .circuitMeta(12)
//                .inputFluids(Glue.getFluid(50))
//                .outputItems(pipeSmallFluid, TreatedWood)
//                .save(provider);
//
//        ASSEMBLER_RECIPES.recipeBuilder("treated_wood_pipe_normal_fluid").duration(200).EUt(VA[LV])
//                .inputItems(plate, TreatedWood, 3)
//                .circuitMeta(6)
//                .inputFluids(Glue.getFluid(20))
//                .outputItems(pipeNormalFluid, TreatedWood)
//                .save(provider);
//
//        ASSEMBLER_RECIPES.recipeBuilder("treated_wood_pipe_large_fluid").duration(100).EUt(VA[LV])
//                .inputItems(plate, TreatedWood, 6)
//                .circuitMeta(2)
//                .inputFluids(Glue.getFluid(10))
//                .outputItems(pipeLargeFluid, TreatedWood)
//                .save(provider);

        // Voltage Coils
        ASSEMBLER_RECIPES.recipeBuilder(VOLTAGE_COIL_ULV.getId()).duration(200).EUt(GTValues.VA[GTValues.ULV])
                .inputItems(stick, IronMagnetic)
                .inputItems(wireFine, Lead, 16)
                .circuitMeta(1)
                .outputItems(VOLTAGE_COIL_ULV.asStack())
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(VOLTAGE_COIL_LV.getId()).duration(200).EUt(GTValues.VA[GTValues.LV])
                .inputItems(stick, IronMagnetic)
                .inputItems(wireFine, Steel, 16)
                .circuitMeta(1)
                .outputItems(VOLTAGE_COIL_LV.asStack())
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(VOLTAGE_COIL_MV.getId()).duration(200).EUt(GTValues.VA[GTValues.MV])
                .inputItems(stick, SteelMagnetic)
                .inputItems(wireFine, Aluminium, 16)
                .circuitMeta(1)
                .outputItems(VOLTAGE_COIL_MV.asStack())
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(VOLTAGE_COIL_HV.getId()).duration(200).EUt(GTValues.VA[GTValues.HV])
                .inputItems(stick, SteelMagnetic)
                .inputItems(wireFine, BlackSteel, 16)
                .circuitMeta(1)
                .outputItems(VOLTAGE_COIL_HV.asStack())
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(VOLTAGE_COIL_EV.getId()).duration(200).EUt(GTValues.VA[GTValues.EV])
                .inputItems(stick, NeodymiumMagnetic)
                .inputItems(wireFine, TungstenSteel, 16)
                .circuitMeta(1)
                .outputItems(VOLTAGE_COIL_EV.asStack())
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(VOLTAGE_COIL_IV.getId()).duration(200).EUt(GTValues.VA[GTValues.IV])
                .inputItems(stick, NeodymiumMagnetic)
                .inputItems(wireFine, Iridium, 16)
                .circuitMeta(1)
                .outputItems(VOLTAGE_COIL_IV.asStack())
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(VOLTAGE_COIL_LuV.getId()).duration(200).EUt(GTValues.VA[GTValues.LuV])
                .inputItems(stick, SamariumMagnetic)
                .inputItems(wireFine, Osmiridium, 16)
                .circuitMeta(1)
                .outputItems(VOLTAGE_COIL_LuV.asStack())
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(VOLTAGE_COIL_ZPM.getId()).duration(200).EUt(GTValues.VA[GTValues.ZPM])
                .inputItems(stick, SamariumMagnetic)
                .inputItems(wireFine, Europium, 16)
                .circuitMeta(1)
                .outputItems(VOLTAGE_COIL_ZPM.asStack())
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(VOLTAGE_COIL_UV.getId()).duration(200).EUt(GTValues.VA[GTValues.UV])
                .inputItems(stick, SamariumMagnetic)
                .inputItems(wireFine, Tritanium, 16)
                .circuitMeta(1)
                .outputItems(VOLTAGE_COIL_UV.asStack())
                .save(provider);

        // Neutron Reflector
        ASSEMBLER_RECIPES.recipeBuilder(NEUTRON_REFLECTOR.getId()).duration(4000).EUt(GTValues.VA[GTValues.MV])
                .inputItems(plate, Ruridit)
                .inputItems(plateDouble, Beryllium, 4)
                .inputItems(plateDouble, TungstenCarbide, 2)
                .inputFluids(TinAlloy.getFluid(GTValues.L * 32))
                .outputItems(NEUTRON_REFLECTOR.asStack())
                .save(provider);
    }
}
