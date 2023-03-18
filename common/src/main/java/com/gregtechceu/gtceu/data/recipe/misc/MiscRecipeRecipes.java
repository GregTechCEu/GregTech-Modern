package com.gregtechceu.gtceu.data.recipe.misc;


import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.common.block.variant.CasingBlock;
import com.gregtechceu.gtceu.common.block.variant.CoilBlock;
import com.gregtechceu.gtceu.common.block.variant.HullCasingBlock;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import net.minecraft.core.Registry;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.common.libs.GTMachines.*;
import static com.gregtechceu.gtceu.common.libs.GTMaterials.*;
import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.libs.GTRecipeTypes.*;
import static com.gregtechceu.gtceu.common.libs.GTItems.*;
import static com.gregtechceu.gtceu.common.libs.GTBlocks.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterials.*;

public class MiscRecipeRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        registerDecompositionRecipes(provider);
        registerBlastFurnaceRecipes(provider);
        registerAssemblerRecipes(provider);
        registerAlloyRecipes(provider);
        registerBendingCompressingRecipes(provider);
        registerCokeOvenRecipes(provider);
        registerFluidRecipes(provider);
        registerMixingCrystallizationRecipes(provider);
        registerPrimitiveBlastFurnaceRecipes(provider);
        registerRecyclingRecipes(provider);
        registerStoneBricksRecipes(provider);
        registerNBTRemoval(provider);
        ConvertHatchToHatch(provider);
        
        // TODO many recipes
        // Basic Terminal Recipe
//        VanillaRecipeHelper.addShapedRecipe(provider, true, "basic_terminal", TERMINAL.asStack(),
//                "SGS", "PBP", "PWP", 'S', new UnificationEntry(screw, WroughtIron), 'G', ItemTags., 'B', new ItemStack(Items.BOOK),
//                                        'P', new UnificationEntry(plate, WroughtIron), 'W', new UnificationEntry(wireGtSingle, RedAlloy));

        // Potin Recipe
        VanillaRecipeHelper.addShapelessRecipe(provider, "potin_dust", ChemicalHelper.get(dust, Potin, 8),
                new UnificationEntry(dust, Copper),
                new UnificationEntry(dust, Copper),
                new UnificationEntry(dust, Copper),
                new UnificationEntry(dust, Copper),
                new UnificationEntry(dust, Copper),
                new UnificationEntry(dust, Copper),
                new UnificationEntry(dust, Tin),
                new UnificationEntry(dust, Tin),
                new UnificationEntry(dust, Lead));

        MIXER_RECIPES.recipeBuilder("sugar.0").duration(100).EUt(VA[ULV])
                .inputItems(dust, Sugar)
                .inputItems(new ItemStack(Blocks.BROWN_MUSHROOM))
                .inputItems(new ItemStack(Items.SPIDER_EYE))
                .outputItems(new ItemStack(Items.FERMENTED_SPIDER_EYE))
                .save(provider);

        MIXER_RECIPES.recipeBuilder("sugar.1").duration(100).EUt(VA[ULV])
                .inputItems(dust, Sugar)
                .inputItems(new ItemStack(Blocks.RED_MUSHROOM))
                .inputItems(new ItemStack(Items.SPIDER_EYE))
                .outputItems(new ItemStack(Items.FERMENTED_SPIDER_EYE))
                .save(provider);

        SIFTER_RECIPES.recipeBuilder("gravel").duration(100).EUt(16)
                .inputItems(Ingredient.of(Blocks.GRAVEL))
                .outputItems(gem, Flint)
                .chancedOutput(gem, Flint, 9000, 0)
                .chancedOutput(gem, Flint, 8000, 0)
                .chancedOutput(gem, Flint, 6000, 0)
                .chancedOutput(gem, Flint, 3300, 0)
                .chancedOutput(gem, Flint, 2500, 0)
                .save(provider);

//        PACKER_RECIPES.recipeBuilder(TOOL_MATCHBOX.getId())
//                .inputItems(TOOL_MATCHES.asStack(16))
//                .inputItems(plate, Paper)
//                .outputItems(TOOL_MATCHBOX)
//                .duration(64)
//                .EUt(16)
//                .save(provider);

        var rockWaterLava = ROCK_BREAKER_RECIPES.recipeBuilder("water_lava.0")
                .addData("fluidA", Registry.FLUID.getKey(Fluids.WATER).toString())
                .addData("fluidB", Registry.FLUID.getKey(Fluids.LAVA).toString());

        rockWaterLava.copy("water_lava.0")
                .notConsumable(Blocks.COBBLESTONE.asItem())
                .outputItems(Blocks.COBBLESTONE.asItem())
                .duration(16)
                .EUt(VA[ULV])
                .save(provider);

        rockWaterLava.copy("water_lava.1")
                .notConsumable(Blocks.STONE.asItem())
                .outputItems(Blocks.STONE.asItem())
                .duration(16)
                .EUt(VA[ULV])
                .save(provider);

        rockWaterLava.copy("water_lava.2")
                .notConsumable(ChemicalHelper.get(stone, Andesite))
                .outputItems(stone, Andesite)
                .duration(16)
                .EUt(60)
                .save(provider);

        rockWaterLava.copy("water_lava.3")
                .notConsumable(ChemicalHelper.get(stone, Granite))
                .outputItems(stone, Granite)
                .duration(16)
                .EUt(60)
                .save(provider);

        rockWaterLava.copy("water_lava.4")
                .notConsumable(ChemicalHelper.get(stone, Diorite))
                .outputItems(stone, Diorite)
                .duration(16)
                .EUt(60)
                .save(provider);

        rockWaterLava.copy("water_lava.5")
                .notConsumable(dust, Redstone)
                .outputItems(Blocks.OBSIDIAN.asItem())
                .duration(16)
                .EUt(240)
                .save(provider);

//        rockWaterLava.copy("water_lava.6")
//                .notConsumable(stone, Marble)
//                .outputItems(stone, Marble)
//                .duration(16)
//                .EUt(240)
//                .save(provider);
//
//        rockWaterLava.copy("water_lava.7")
//                .notConsumable(stone, Basalt)
//                .outputItems(stone, Basalt)
//                .duration(16)
//                .EUt(240)
//                .save(provider);
//
//        rockWaterLava.copy("water_lava.8")
//                .notConsumable(stone, GraniteRed)
//                .outputItems(stone, GraniteRed)
//                .duration(16)
//                .EUt(960)
//                .save(provider);
//
//        rockWaterLava.copy("water_lava.9")
//                .notConsumable(stone, GraniteBlack)
//                .outputItems(stone, GraniteBlack)
//                .duration(16)
//                .EUt(960)
//                .save(provider);

        // Jetpacks
        ASSEMBLER_RECIPES.recipeBuilder(POWER_THRUSTER.getId()).duration(200).EUt(30)
                .inputItems(ELECTRIC_MOTOR_MV)
                .inputItems(ring, Aluminium, 2)
                .inputItems(stick, Aluminium)
                .inputItems(rotor, Steel)
                .inputItems(cableGtSingle, Copper, 2)
                .outputItems(POWER_THRUSTER)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(POWER_THRUSTER_ADVANCED.getId()).duration(200).EUt(30)
                .inputItems(ELECTRIC_MOTOR_HV)
                .inputItems(ring, StainlessSteel, 2)
                .inputItems(stick, StainlessSteel)
                .inputItems(rotor, Chrome)
                .inputItems(cableGtSingle, Gold, 2)
                .outputItems(POWER_THRUSTER_ADVANCED)
                .save(provider);

        // QuarkTech Suite
//        ASSEMBLER_RECIPES.recipeBuilder().duration(1500).EUt(GTValues.VA[GTValues.IV])
//                .inputItems(circuit, Tier.LuV, 2)
//                .inputItems(wireGtQuadruple, Tungsten, 5)
//                .inputItems(ENERGY_LAPOTRONIC_ORB)
//                .inputItems(SENSOR_IV)
//                .inputItems(FIELD_GENERATOR_IV)
//                .inputItems(screw, TungstenSteel, 4)
//                .inputItems(plate, Iridium, 5)
//                .inputItems(foil, Ruthenium, 20)
//                .inputItems(wireFine, Rhodium, 32)
//                .inputFluids(Titanium.getFluid(L * 10))
//                .outputItems(QUANTUM_HELMET)
//                .save(provider);
//
//        ASSEMBLER_RECIPES.recipeBuilder().duration(1500).EUt(GTValues.VA[GTValues.IV])
//                .inputItems(circuit, Tier.LuV, 2)
//                .inputItems(wireGtQuadruple, Tungsten, 8)
//                .inputItems(ENERGY_LAPOTRONIC_ORB.asStack())
//                .inputItems(EMITTER_IV.asStack(2))
//                .inputItems(FIELD_GENERATOR_IV.asStack())
//                .inputItems(screw, TungstenSteel, 4)
//                .inputItems(plate, Iridium, 8)
//                .inputItems(foil, Ruthenium, 32)
//                .inputItems(wireFine, Rhodium, 48)
//                .inputFluids(Titanium.getFluid(L * 16))
//                .outputItems(QUANTUM_CHESTPLATE.asStack())
//                .save(provider);
//
//        ASSEMBLER_RECIPES.recipeBuilder().duration(1500).EUt(GTValues.VA[GTValues.IV])
//                .inputItems(circuit, Tier.LuV, 2)
//                .inputItems(wireGtQuadruple, Tungsten, 7)
//                .inputItems(ENERGY_LAPOTRONIC_ORB.asStack())
//                .inputItems(ELECTRIC_MOTOR_IV.asStack(4))
//                .inputItems(FIELD_GENERATOR_IV.asStack())
//                .inputItems(screw, TungstenSteel, 4)
//                .inputItems(plate, Iridium, 7)
//                .inputItems(foil, Ruthenium, 28)
//                .inputItems(wireFine, Rhodium, 40)
//                .inputFluids(Titanium.getFluid(L * 14))
//                .outputItems(QUANTUM_LEGGINGS.asStack())
//                .save(provider);
//
//        ASSEMBLER_RECIPES.recipeBuilder().duration(1500).EUt(GTValues.VA[GTValues.IV])
//                .inputItems(circuit, Tier.LuV, 2)
//                .inputItems(wireGtQuadruple, Tungsten, 4)
//                .inputItems(ENERGY_LAPOTRONIC_ORB.asStack())
//                .inputItems(ELECTRIC_PISTON_IV.asStack(2))
//                .inputItems(FIELD_GENERATOR_IV.asStack())
//                .inputItems(screw, TungstenSteel, 4)
//                .inputItems(plate, Iridium, 4)
//                .inputItems(foil, Ruthenium, 16)
//                .inputItems(wireFine, Rhodium, 16)
//                .inputFluids(Titanium.getFluid(L * 8))
//                .outputItems(QUANTUM_BOOTS.asStack())
//                .save(provider);
//
//        ASSEMBLY_LINE_RECIPES.recipeBuilder().duration(1000).EUt(GTValues.VA[GTValues.LuV])
//                .inputItems(QUANTUM_CHESTPLATE.asStack())
//                .inputItems(HIGH_POWER_INTEGRATED_CIRCUIT.asStack(2))
//                .inputItems(wireFine, NiobiumTitanium, 64)
//                .inputItems(wireGtQuadruple, Osmium, 6)
//                .inputItems(plateDouble, Iridium, 4)
//                .inputItems(GRAVITATION_ENGINE.asStack(2))
//                .inputItems(circuit, Tier.ZPM)
//                .inputItems(plateDense, RhodiumPlatedPalladium, 2)
//                .inputItems(ENERGY_LAPOTRONIC_ORB_CLUSTER.asStack())
//                .inputItems(FIELD_GENERATOR_LuV.asStack(2))
//                .inputItems(ELECTRIC_MOTOR_LuV.asStack(2))
//                .inputItems(screw, HSSS, 8)
//                .outputItems(QUANTUM_CHESTPLATE_ADVANCED.asStack())
//                .save(provider);


//        ASSEMBLER_RECIPES.recipeBuilder().duration(80).EUt(VA[HV])
//                .inputItems(COVER_SCREEN.asStack())
//                .inputItems((ItemStack) CraftingComponent.HULL.getIngredient(1))
//                .inputItems(wireFine, AnnealedCopper, 8)
//                .inputFluids(Polyethylene.getFluid(L))
//                .outputItems(MetaTileEntities.MONITOR_SCREEN.asStack())
//                .save(provider);
//
//        ASSEMBLER_RECIPES.recipeBuilder().duration(100).EUt(VA[HV])
//                .inputItems(COVER_SCREEN.asStack())
//                .inputItems((ItemStack) CraftingComponent.HULL.getIngredient(3))
//                .inputItems(circuit, Tier.HV, 2)
//                .inputFluids(Polyethylene.getFluid(L))
//                .outputItems(MetaTileEntities.CENTRAL_MONITOR.asStack())
//                .save(provider);
//
//        ASSEMBLER_RECIPES.recipeBuilder().duration(100).EUt(VA[HV])
//                .inputItems(COVER_SCREEN.asStack())
//                .inputItems(plate, Aluminium)
//                .inputItems(circuit, Tier.MV)
//                .inputItems(screw, StainlessSteel, 4)
//                .inputFluids(Polyethylene.getFluid(L))
//                .outputItems(COVER_DIGITAL_INTERFACE.asStack())
//                .save(provider);
//
//        ASSEMBLER_RECIPES.recipeBuilder().duration(100).EUt(VA[HV])
//                .inputItems(COVER_DIGITAL_INTERFACE.asStack())
//                .inputItems(WIRELESS.asStack())
//                .inputFluids(Polyethylene.getFluid(L))
//                .outputItems(COVER_DIGITAL_INTERFACE_WIRELESS.asStack())
//                .save(provider);
//
//        ASSEMBLER_RECIPES.recipeBuilder().duration(80).EUt(400)
//                .inputItems(COVER_SCREEN.asStack())
//                .inputItems(circuit, Tier.LV)
//                .inputItems(wireFine, Copper, 2)
//                .inputFluids(Polyethylene.getFluid(L))
//                .outputItems(PLUGIN_TEXT.asStack())
//                .save(provider);
//
//        ASSEMBLER_RECIPES.recipeBuilder().duration(80).EUt(400)
//                .inputItems(COVER_SCREEN.asStack())
//                .inputItems(circuit, Tier.LV)
//                .inputItems(wireFine, Silver, 2)
//                .inputFluids(Polyethylene.getFluid(L))
//                .outputItems(PLUGIN_ONLINE_PIC.asStack())
//                .save(provider);
//
//        ASSEMBLER_RECIPES.recipeBuilder().duration(80).EUt(400)
//                .inputItems(COVER_SCREEN.asStack())
//                .inputItems(circuit, Tier.LV)
//                .inputItems(wireFine, Gold, 2)
//                .inputFluids(Polyethylene.getFluid(L))
//                .outputItems(PLUGIN_FAKE_GUI.asStack())
//                .save(provider);
//
//        ASSEMBLER_RECIPES.recipeBuilder().duration(80).EUt(400)
//                .inputItems(COVER_SCREEN.asStack())
//                .inputItems(circuit, Tier.HV)
//                .inputItems(wireFine, Aluminium, 2)
//                .inputFluids(Polyethylene.getFluid(L))
//                .outputItems(PLUGIN_ADVANCED_MONITOR.asStack())
//                .save(provider);

        // terminal
//        ASSEMBLER_RECIPES.recipeBuilder().duration(100).EUt(VA[MV])
//                .inputItems(circuit, Tier.MV, 4)
//                .inputItems(EMITTER_MV, 2)
//                .inputItems(SENSOR_MV, 2)
//                .inputItems(plate, StainlessSteel)
//                .inputFluids(Polyethylene.getFluid(L))
//                .outputItems(WIRELESS.asStack())
//                .save(provider);
//
//        ASSEMBLER_RECIPES.recipeBuilder().duration(100).EUt(VA[LV])
//                .inputItems(ELECTRIC_PISTON_LV, 2)
//                .inputItems(EMITTER_LV)
//                .inputItems(lens, Glass)
//                .inputItems(lens, Diamond)
//                .inputItems(circuit, Tier.LV, 4)
//                .inputFluids(SolderingAlloy.getFluid(L))
//                .outputItems(CAMERA.asStack())
//                .save(provider);

        // Tempered Glass in Arc Furnace
//        ARC_FURNACE_RECIPES.recipeBuilder().duration(60).EUt(VA[LV])
//                .inputItems(block, Glass)
//                .outputItems(CASING.get().getItemVariant(
//                        CasingBlock.CasingType.TEMPERED_GLASS))
//                .save(provider);

        // Dyed Lens Decomposition
        for (var item : GLASS_LENSES.values()) {
            EXTRACTOR_RECIPES.recipeBuilder(item.getId()).EUt(VA[LV]).duration(15)
                    .inputItems(item)
                    .outputFluids(Glass.getFluid(108))
                    .save(provider);

            MACERATOR_RECIPES.recipeBuilder(item.getId()).duration(15)
                    .inputItems(item)
                    .outputItems(dustSmall, Glass, 3)
                    .save(provider);
        }

        // Glass Fluid Extraction
        EXTRACTOR_RECIPES.recipeBuilder("glass_fluid")
                .inputItems(Blocks.GLASS.asItem())
                .outputFluids(Glass.getFluid(L))
                .duration(20).EUt(30).save(provider);

        // Glass Plate in Alloy Smelter
        ALLOY_SMELTER_RECIPES.recipeBuilder("glass_plate")
                .inputItems(dust, Glass, 2)
                .notConsumable(SHAPE_MOLD_PLATE)
                .outputItems(plate, Glass)
                .duration(40).EUt(6).save(provider);

        // Dyed Lens Recipes
        var builder = CHEMICAL_BATH_RECIPES.recipeBuilder("temp").EUt(VA[HV]).duration(200).inputItems(craftingLens, Glass);
        final int dyeAmount = 288;

        builder.copy("lens_glass").inputFluids(DyeWhite.getFluid(dyeAmount)).outputItems(lens, Glass).save(provider);
        builder.copy("lens_glass.orange").inputFluids(DyeOrange.getFluid(dyeAmount)).outputItems(GLASS_LENSES.get(Color.Orange)).save(provider);
        builder.copy("lens_glass.magenta").inputFluids(DyeMagenta.getFluid(dyeAmount)).outputItems(GLASS_LENSES.get(Color.Magenta)).save(provider);
        builder.copy("lens_glass.lightblue").inputFluids(DyeLightBlue.getFluid(dyeAmount)).outputItems(GLASS_LENSES.get(Color.LightBlue)).save(provider);
        builder.copy("lens_glass.yellow").inputFluids(DyeYellow.getFluid(dyeAmount)).outputItems(GLASS_LENSES.get(Color.Yellow)).save(provider);
        builder.copy("lens_glass.lime").inputFluids(DyeLime.getFluid(dyeAmount)).outputItems(GLASS_LENSES.get(Color.Lime)).save(provider);
        builder.copy("lens_glass.pink").inputFluids(DyePink.getFluid(dyeAmount)).outputItems(GLASS_LENSES.get(Color.Pink)).save(provider);
        builder.copy("lens_glass.gray").inputFluids(DyeGray.getFluid(dyeAmount)).outputItems(GLASS_LENSES.get(Color.Gray)).save(provider);
        builder.copy("lens_glass.lightgray").inputFluids(DyeLightGray.getFluid(dyeAmount)).outputItems(GLASS_LENSES.get(Color.LightGray)).save(provider);
        builder.copy("lens_glass.cyan").inputFluids(DyeCyan.getFluid(dyeAmount)).outputItems(GLASS_LENSES.get(Color.Cyan)).save(provider);
        builder.copy("lens_glass.purple").inputFluids(DyePurple.getFluid(dyeAmount)).outputItems(GLASS_LENSES.get(Color.Purple)).save(provider);
        builder.copy("lens_glass.blue").inputFluids(DyeBlue.getFluid(dyeAmount)).outputItems(GLASS_LENSES.get(Color.Blue)).save(provider);
        builder.copy("lens_glass.brown").inputFluids(DyeBrown.getFluid(dyeAmount)).outputItems(GLASS_LENSES.get(Color.Brown)).save(provider);
        builder.copy("lens_glass.green").inputFluids(DyeGreen.getFluid(dyeAmount)).outputItems(GLASS_LENSES.get(Color.Green)).save(provider);
        builder.copy("lens_glass.red").inputFluids(DyeRed.getFluid(dyeAmount)).outputItems(GLASS_LENSES.get(Color.Red)).save(provider);
        builder.copy("lens_glass.black").inputFluids(DyeBlack.getFluid(dyeAmount)).outputItems(GLASS_LENSES.get(Color.Black)).save(provider);

        // NAN Certificate
        EXTRUDER_RECIPES.recipeBuilder(NAN_CERTIFICATE.getId())
                .inputItems(block, Neutronium, 64)
                .inputItems(block, Neutronium, 64)
                .outputItems(NAN_CERTIFICATE)
                .duration(Integer.MAX_VALUE).EUt(VA[ULV]).save(provider);

        // Fertilizer
        MIXER_RECIPES.recipeBuilder(FERTILIZER.getId())
                .inputItems(new ItemStack(Blocks.DIRT))
                .inputItems(dust, Wood, 2)
                .inputItems(new ItemStack(Blocks.SAND, 4))
                .inputFluids(Water.getFluid(1000))
                .outputItems(FERTILIZER, 4)
                .duration(100).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("fertilizer.0").inputItems(dust, Calcite).inputItems(dust, Sulfur).inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 2).duration(200).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer.1").inputItems(dust, Calcite).inputItems(dust, TricalciumPhosphate).inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 3).duration(300).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer.2").inputItems(dust, Calcite).inputItems(dust, Phosphate).inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 2).duration(200).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer.3").inputItems(dust, Calcite).inputItems(dust, Ash, 3).inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 1).duration(100).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer.4").inputItems(dust, Calcite).inputItems(dust, DarkAsh).inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 1).duration(100).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer.5").inputItems(dust, Calcium).inputItems(dust, Sulfur).inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 3).duration(300).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer.6").inputItems(dust, Calcium).inputItems(dust, TricalciumPhosphate).inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 4).duration(400).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer.7").inputItems(dust, Calcium).inputItems(dust, Phosphate).inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 3).duration(300).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer.8").inputItems(dust, Calcium).inputItems(dust, Ash, 3).inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 2).duration(200).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer.9").inputItems(dust, Calcium).inputItems(dust, DarkAsh).inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 2).duration(200).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer.10").inputItems(dust, Apatite).inputItems(dust, Sulfur).inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 3).duration(300).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer.11").inputItems(dust, Apatite).inputItems(dust, TricalciumPhosphate).inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 4).duration(400).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer.12").inputItems(dust, Apatite).inputItems(dust, Phosphate).inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 3).duration(300).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer.13").inputItems(dust, Apatite).inputItems(dust, Ash, 3).inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 2).duration(200).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer.14").inputItems(dust, Apatite).inputItems(dust, DarkAsh).inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 2).duration(200).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer.15").inputItems(dust, GlauconiteSand).inputItems(dust, Sulfur).inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 3).duration(300).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer.16").inputItems(dust, GlauconiteSand).inputItems(dust, TricalciumPhosphate).inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 4).duration(400).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer.17").inputItems(dust, GlauconiteSand).inputItems(dust, Phosphate).inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 3).duration(300).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer.18").inputItems(dust, GlauconiteSand).inputItems(dust, Ash, 3).inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 2).duration(200).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer.19").inputItems(dust, GlauconiteSand).inputItems(dust, DarkAsh).inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 2).duration(200).EUt(VA[LV]).save(provider);

        ELECTROLYZER_RECIPES.recipeBuilder(FERTILIZER.getId())
                .inputItems(FERTILIZER)
                .outputItems(dust, Calcite)
                .outputItems(dust, Carbon)
                .outputFluids(Water.getFluid(1000))
                .duration(100).EUt(VA[LV]).save(provider);

//        FORMING_PRESS_RECIPES.recipeBuilder()
//                .inputItems(MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockGlassCasing.CasingType.TEMPERED_GLASS, 2))
//                .inputItems(plate, PolyvinylButyral)
//                .outputItems(MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockGlassCasing.CasingType.LAMINATED_GLASS))
//                .duration(200).EUt(VA[HV]).save(provider);

        LATHE_RECIPES.recipeBuilder("stick_treated_wood")
                .inputItems(plank, TreatedWood)
                .outputItems(stick, TreatedWood, 2)
                .duration(10).EUt(VA[ULV])
                .save(provider);

        // Coke Brick and Firebrick decomposition
        EXTRACTOR_RECIPES.recipeBuilder(COKE_OVEN_BRICK.getId())
                .inputItems(CASING.get().getItemVariant(CasingBlock.CasingType.COKE_BRICKS))
                .outputItems(COKE_OVEN_BRICK, 4)
                .duration(300).EUt(2)
                .save(provider);

        EXTRACTOR_RECIPES.recipeBuilder(FIRECLAY_BRICK.getId())
                .inputItems(CASING.get().getItemVariant(CasingBlock.CasingType.PRIMITIVE_BRICKS))
                .outputItems(FIRECLAY_BRICK, 4)
                .duration(300).EUt(2)
                .save(provider);
    }

    private static void registerBendingCompressingRecipes(Consumer<FinishedRecipe> provider) {

        COMPRESSOR_RECIPES.recipeBuilder(COMPRESSED_FIRECLAY.getId())
                .inputItems(dust, Fireclay)
                .outputItems(COMPRESSED_FIRECLAY.asStack())
                .duration(80).EUt(4)
                .save(provider);

        FORMING_PRESS_RECIPES.recipeBuilder(CREDIT_CUPRONICKEL.getId())
                .duration(100).EUt(16)
                .notConsumable(SHAPE_MOLD_CREDIT.asStack())
                .inputItems(plate, Cupronickel, 1)
                .outputItems(CREDIT_CUPRONICKEL.asStack(4))
                .save(provider);

        FORMING_PRESS_RECIPES.recipeBuilder(COIN_DOGE.getId())
                .duration(100).EUt(16)
                .notConsumable(SHAPE_MOLD_CREDIT.asStack())
                .inputItems(plate, Brass, 1)
                .outputItems(COIN_DOGE.asStack(4))
                .save(provider);

        for (var shapeMold : SHAPE_MOLDS) {
            FORMING_PRESS_RECIPES.recipeBuilder(shapeMold)
                    .duration(120).EUt(22)
                    .notConsumable(shapeMold.asStack())
                    .inputItems(SHAPE_EMPTY.asStack())
                    .outputItems(shapeMold.asStack())
                    .save(provider);
        }

        for (var shapeExtruder : SHAPE_EXTRUDERS) {
            if (shapeExtruder == null) continue;
            FORMING_PRESS_RECIPES.recipeBuilder(shapeExtruder)
                    .duration(120).EUt(22)
                    .notConsumable(shapeExtruder.asStack())
                    .inputItems(SHAPE_EMPTY.asStack())
                    .outputItems(shapeExtruder.asStack())
                    .save(provider);
        }

        BENDER_RECIPES.recipeBuilder(SHAPE_EMPTY)
                .circuitMeta(4)
                .inputItems(plate, Steel, 4)
                .outputItems(SHAPE_EMPTY.asStack())
                .duration(180).EUt(12)
                .save(provider);

        BENDER_RECIPES.recipeBuilder("fluid_cell.0")
                .circuitMeta(12)
                .inputItems(plate, Tin, 2)
                .outputItems(FLUID_CELL.asStack())
                .duration(200).EUt(VA[ULV])
                .save(provider);

        BENDER_RECIPES.recipeBuilder("fluid_cell.1")
                .circuitMeta(12)
                .inputItems(plate, Steel)
                .outputItems(FLUID_CELL.asStack())
                .duration(100).EUt(VA[ULV])
                .save(provider);

        BENDER_RECIPES.recipeBuilder("fluid_cell.2")
                .circuitMeta(12)
                .inputItems(plate, Polytetrafluoroethylene)
                .outputItems(FLUID_CELL.asStack(4))
                .duration(100).EUt(VA[ULV])
                .save(provider);

        BENDER_RECIPES.recipeBuilder("fluid_cell.3")
                .circuitMeta(12)
                .inputItems(plate, Polybenzimidazole)
                .outputItems(FLUID_CELL.asStack(16))
                .duration(100).EUt(VA[ULV])
                .save(provider);

        EXTRUDER_RECIPES.recipeBuilder("fluid_cell.4")
                .inputItems(ingot, Tin, 2)
                .notConsumable(SHAPE_EXTRUDER_CELL)
                .outputItems(FLUID_CELL.asStack())
                .duration(128).EUt(VA[LV])
                .save(provider);

        EXTRUDER_RECIPES.recipeBuilder("fluid_cell.5")
                .inputItems(ingot, Steel)
                .notConsumable(SHAPE_EXTRUDER_CELL)
                .outputItems(FLUID_CELL.asStack())
                .duration(128).EUt(VA[LV])
                .save(provider);

        EXTRUDER_RECIPES.recipeBuilder("fluid_cell.6")
                .inputItems(ingot, Polytetrafluoroethylene)
                .notConsumable(SHAPE_EXTRUDER_CELL)
                .outputItems(FLUID_CELL.asStack(4))
                .duration(128).EUt(VA[LV])
                .save(provider);

        EXTRUDER_RECIPES.recipeBuilder("fluid_cell.7")
                .inputItems(ingot, Polybenzimidazole)
                .notConsumable(SHAPE_EXTRUDER_CELL)
                .outputItems(FLUID_CELL.asStack(16))
                .duration(128).EUt(VA[LV])
                .save(provider);

        EXTRUDER_RECIPES.recipeBuilder("fluid_cell_glass_vial.0")
                .inputItems(dust, Glass)
                .notConsumable(SHAPE_EXTRUDER_CELL)
                .outputItems(FLUID_CELL_GLASS_VIAL.asStack(4))
                .duration(128).EUt(VA[LV])
                .save(provider);

        COMPRESSOR_RECIPES.recipeBuilder("nether_quartz_plate")
                .inputItems(dust, NetherQuartz)
                .outputItems(plate, NetherQuartz)
                .duration(400).EUt(2).save(provider);

        COMPRESSOR_RECIPES.recipeBuilder("certus_quartz_plate")
                .inputItems(dust, CertusQuartz)
                .outputItems(plate, CertusQuartz)
                .duration(400).EUt(2).save(provider);

        COMPRESSOR_RECIPES.recipeBuilder("quartzite_plate")
                .inputItems(dust, Quartzite)
                .outputItems(plate, Quartzite)
                .duration(400).EUt(2).save(provider);

        COMPRESSOR_RECIPES.recipeBuilder(CasingBlock.CasingType.COKE_BRICKS.getName())
                .inputItems(COKE_OVEN_BRICK, 4)
                .outputItems(CASING.get().getItemVariant(CasingBlock.CasingType.COKE_BRICKS))
                .duration(300).EUt(2).save(provider);
    }

    private static void registerPrimitiveBlastFurnaceRecipes(Consumer<FinishedRecipe> provider) {
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_ingot.0").inputItems(ingot, Iron).inputItems(gem, Coal, 2).outputItems(ingot, Steel).outputItems(dustTiny, DarkAsh, 2).duration(1800).save(provider);
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_ingot.1").inputItems(ingot, Iron).inputItems(dust, Coal, 2).outputItems(ingot, Steel).outputItems(dustTiny, DarkAsh, 2).duration(1800).save(provider);
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_ingot.2").inputItems(ingot, Iron).inputItems(gem, Charcoal, 2).outputItems(ingot, Steel).outputItems(dustTiny, DarkAsh, 2).duration(1800).save(provider);
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_ingot.3").inputItems(ingot, Iron).inputItems(dust, Charcoal, 2).outputItems(ingot, Steel).outputItems(dustTiny, DarkAsh, 2).duration(1800).save(provider);
//        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder().inputItems(ingot, Iron).inputItems(OREDICT_FUEL_COKE).outputItems(ingot, Steel).outputItems(dustTiny, Ash).duration(1500).save(provider);
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_ingot.4").inputItems(ingot, Iron).inputItems(dust, Coke).outputItems(ingot, Steel).outputItems(dustTiny, Ash).duration(1500).save(provider);

        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_block.0").inputItems(block, Iron).inputItems(block, Coal, 2).outputItems(block, Steel).outputItems(dust, DarkAsh, 2).duration(16200).save(provider);
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_block.1").inputItems(block, Iron).inputItems(block, Charcoal, 2).outputItems(block, Steel).outputItems(dust, DarkAsh, 2).duration(16200).save(provider);
//        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder().inputItems(block, Iron).inputItems(OREDICT_BLOCK_FUEL_COKE).outputItems(block, Steel).outputItems(dust, Ash).duration(13500).save(provider);

        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_ingot.5").inputItems(ingot, WroughtIron).inputItems(gem, Coal, 2).outputItems(ingot, Steel).outputItems(dustTiny, DarkAsh, 2).duration(800).save(provider);
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_ingot.6").inputItems(ingot, WroughtIron).inputItems(dust, Coal, 2).outputItems(ingot, Steel).outputItems(dustTiny, DarkAsh, 2).duration(800).save(provider);
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_ingot.7").inputItems(ingot, WroughtIron).inputItems(gem, Charcoal, 2).outputItems(ingot, Steel).outputItems(dustTiny, DarkAsh, 2).duration(800).save(provider);
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_ingot.8").inputItems(ingot, WroughtIron).inputItems(dust, Charcoal, 2).outputItems(ingot, Steel).outputItems(dustTiny, DarkAsh, 2).duration(800).save(provider);
//        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_ingot.5").inputItems(ingot, WroughtIron).inputItems(OREDICT_FUEL_COKE).outputItems(ingot, Steel).outputItems(dustTiny, Ash).duration(600).save(provider);
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_ingot.9").inputItems(ingot, WroughtIron).inputItems(dust, Coke).outputItems(ingot, Steel).outputItems(dustTiny, Ash).duration(600).save(provider);

        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_block.2").inputItems(block, WroughtIron).inputItems(block, Coal, 2).outputItems(block, Steel).outputItems(dust, DarkAsh, 2).duration(7200).save(provider);
        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_block.3").inputItems(block, WroughtIron).inputItems(block, Charcoal, 2).outputItems(block, Steel).outputItems(dust, DarkAsh, 2).duration(7200).save(provider);
//        PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder().inputItems(block, WroughtIron).inputItems(OREDICT_BLOCK_FUEL_COKE).outputItems(block, Steel).outputItems(dust, Ash).duration(5400).save(provider);
    }

    private static void registerCokeOvenRecipes(Consumer<FinishedRecipe> provider) {
        COKE_OVEN_RECIPES.recipeBuilder("gem_charcoal").inputItems(ItemTags.LOGS).outputItems(gem, Charcoal).outputFluids(Creosote.getFluid(250)).duration(900).save(provider);
        COKE_OVEN_RECIPES.recipeBuilder("gem_coke").inputItems(ItemTags.COALS).outputItems(gem, Coke).outputFluids(Creosote.getFluid(500)).duration(900).save(provider);
        COKE_OVEN_RECIPES.recipeBuilder("block_coke").inputItems(block, Coal).outputItems(block, Coke).outputFluids(Creosote.getFluid(4500)).duration(8100).save(provider);
    }

    private static void registerStoneBricksRecipes(Consumer<FinishedRecipe> provider) {
        // normal variant -> cobble variant
//        List<ItemStack> cobbles = Arrays.stream(BlockStoneCobble.BlockType.values()).map(MetaBlocks.STONE_COBBLE::getItemVariant).collect(Collectors.toList());
//        List<ItemStack> mossCobbles = Arrays.stream(BlockStoneCobbleMossy.BlockType.values()).map(MetaBlocks.STONE_COBBLE_MOSSY::getItemVariant).collect(Collectors.toList());
//        List<ItemStack> smooths = Arrays.stream(BlockStoneSmooth.BlockType.values()).map(MetaBlocks.STONE_SMOOTH::getItemVariant).collect(Collectors.toList());
//        List<ItemStack> polisheds = Arrays.stream(BlockStonePolished.BlockType.values()).map(MetaBlocks.STONE_POLISHED::getItemVariant).collect(Collectors.toList());
//        List<ItemStack> bricks = Arrays.stream(BlockStoneBricks.BlockType.values()).map(MetaBlocks.STONE_BRICKS::getItemVariant).collect(Collectors.toList());
//        List<ItemStack> crackedBricks = Arrays.stream(BlockStoneBricksCracked.BlockType.values()).map(MetaBlocks.STONE_BRICKS_CRACKED::getItemVariant).collect(Collectors.toList());
//        List<ItemStack> mossBricks = Arrays.stream(BlockStoneBricksMossy.BlockType.values()).map(MetaBlocks.STONE_BRICKS_MOSSY::getItemVariant).collect(Collectors.toList());
//        List<ItemStack> chiseledBricks = Arrays.stream(BlockStoneChiseled.BlockType.values()).map(MetaBlocks.STONE_CHISELED::getItemVariant).collect(Collectors.toList());
//        List<ItemStack> tiledBricks = Arrays.stream(BlockStoneTiled.BlockType.values()).map(MetaBlocks.STONE_TILED::getItemVariant).collect(Collectors.toList());
//        List<ItemStack> smallTiledBricks = Arrays.stream(BlockStoneTiledSmall.BlockType.values()).map(MetaBlocks.STONE_TILED_SMALL::getItemVariant).collect(Collectors.toList());
//        List<ItemStack> windmillA = Arrays.stream(BlockStoneWindmillA.BlockType.values()).map(MetaBlocks.STONE_WINDMILL_A::getItemVariant).collect(Collectors.toList());
//        List<ItemStack> windmillB = Arrays.stream(BlockStoneWindmillB.BlockType.values()).map(MetaBlocks.STONE_WINDMILL_B::getItemVariant).collect(Collectors.toList());
//        List<ItemStack> squareBricks = Arrays.stream(BlockStoneBricksSquare.BlockType.values()).map(MetaBlocks.STONE_BRICKS_SQUARE::getItemVariant).collect(Collectors.toList());
//        List<ItemStack> smallBricks = Arrays.stream(BlockStoneBricksSmall.BlockType.values()).map(MetaBlocks.STONE_BRICKS_SMALL::getItemVariant).collect(Collectors.toList());
//
//
//        registerSmoothRecipe(cobbles, smooths);
//        registerCobbleRecipe(smooths, cobbles);
//        registerMossRecipe(cobbles, mossCobbles);
//        registerSmoothRecipe(smooths, polisheds);
//        registerBricksRecipe(polisheds, bricks, MarkerColor.LightBlue);
//        registerCobbleRecipe(bricks, crackedBricks);
//        registerMossRecipe(bricks, mossBricks);
//        registerBricksRecipe(polisheds, chiseledBricks, MarkerColor.White);
//        registerBricksRecipe(polisheds, tiledBricks, MarkerColor.Red);
//        registerBricksRecipe(tiledBricks, smallTiledBricks, MarkerColor.Red);
//        registerBricksRecipe(polisheds, windmillA, MarkerColor.Blue);
//        registerBricksRecipe(polisheds, windmillB, MarkerColor.Yellow);
//        registerBricksRecipe(polisheds, squareBricks, MarkerColor.Green);
//        registerBricksRecipe(polisheds, smallBricks, MarkerColor.Pink);
//
//        for (int i = 0; i < smooths.size(); i++) {
//            EXTRUDER_RECIPES.recipeBuilder()
//                    .inputItems(smooths.get(i))
//                    .notConsumable(SHAPE_EXTRUDER_INGOT.asStack())
//                    .outputItems(bricks.get(i))
//                    .duration(24).EUt(8).save(provider);
//        }
    }

    private static void registerMixingCrystallizationRecipes(Consumer<FinishedRecipe> provider) {

        AUTOCLAVE_RECIPES.recipeBuilder("gem_quartzite")
                .inputItems(dust, SiliconDioxide)
                .inputFluids(DistilledWater.getFluid(250))
                .chancedOutput(ChemicalHelper.get(gem, Quartzite), 1000, 1000)
                .duration(1200).EUt(24).save(provider);

        //todo find UU-Matter replacement
//        AUTOCLAVE_RECIPES.recipeBuilder()
//            .inputItems(dust, NetherStar)
//            .inputFluids(UUMatter.getFluid(576))
//            .chancedOutput(new ItemStack(Items.NETHER_STAR), 3333, 3333)
//            .duration(72000).EUt(VA[HV]).save(provider);

        MIXER_RECIPES.recipeBuilder("fluid_" + IndiumConcentrate.getName())
                .inputItems(crushedPurified, Sphalerite)
                .inputItems(crushedPurified, Galena)
                .inputFluids(SulfuricAcid.getFluid(4000))
                .outputFluids(IndiumConcentrate.getFluid(1000))
                .duration(60).EUt(150).save(provider);

//        MIXER_RECIPES.recipeBuilder()
//                .inputItems(dust, Coal)
//                .inputFluids(Concrete.getFluid(L))
//                .outputItems(MetaBlocks.ASPHALT.getItemVariant(BlockAsphalt.BlockType.ASPHALT))
//                .duration(60).EUt(16).save(provider);
//
//        MIXER_RECIPES.recipeBuilder()
//                .inputItems(dust, Charcoal)
//                .inputFluids(Concrete.getFluid(L))
//                .outputItems(MetaBlocks.ASPHALT.getItemVariant(BlockAsphalt.BlockType.ASPHALT))
//                .duration(60).EUt(16).save(provider);
//
//        MIXER_RECIPES.recipeBuilder()
//                .inputItems(dust, Carbon)
//                .inputFluids(Concrete.getFluid(L))
//                .outputItems(MetaBlocks.ASPHALT.getItemVariant(BlockAsphalt.BlockType.ASPHALT))
//                .duration(60).EUt(16).save(provider);

    }

    private static final MaterialStack[][] alloySmelterList = {
            {new MaterialStack(Copper, 3L), new MaterialStack(Tin, 1), new MaterialStack(Bronze, 4L)},
            {new MaterialStack(Copper, 3L), new MaterialStack(Zinc, 1), new MaterialStack(Brass, 4L)},
            {new MaterialStack(Copper, 1), new MaterialStack(Nickel, 1), new MaterialStack(Cupronickel, 2L)},
            {new MaterialStack(Copper, 1), new MaterialStack(Redstone, 4L), new MaterialStack(RedAlloy, 1)},
            {new MaterialStack(AnnealedCopper, 3L), new MaterialStack(Tin, 1), new MaterialStack(Bronze, 4L)},
            {new MaterialStack(AnnealedCopper, 3L), new MaterialStack(Zinc, 1), new MaterialStack(Brass, 4L)},
            {new MaterialStack(AnnealedCopper, 1), new MaterialStack(Nickel, 1), new MaterialStack(Cupronickel, 2L)},
            {new MaterialStack(AnnealedCopper, 1), new MaterialStack(Redstone, 4L), new MaterialStack(RedAlloy, 1)},
            {new MaterialStack(Iron, 1), new MaterialStack(Tin, 1), new MaterialStack(TinAlloy, 2L)},
            {new MaterialStack(WroughtIron, 1), new MaterialStack(Tin, 1), new MaterialStack(TinAlloy, 2L)},
            {new MaterialStack(Iron, 2L), new MaterialStack(Nickel, 1), new MaterialStack(Invar, 3L)},
            {new MaterialStack(WroughtIron, 2L), new MaterialStack(Nickel, 1), new MaterialStack(Invar, 3L)},
            {new MaterialStack(Lead, 4L), new MaterialStack(Antimony, 1), new MaterialStack(BatteryAlloy, 5L)},
            {new MaterialStack(Gold, 1), new MaterialStack(Silver, 1), new MaterialStack(Electrum, 2L)},
            {new MaterialStack(Magnesium, 1), new MaterialStack(Aluminium, 2L), new MaterialStack(Magnalium, 3L)},
            {new MaterialStack(Silver, 1), new MaterialStack(Electrotine, 4), new MaterialStack(BlueAlloy, 1)}};

    private static void registerAlloyRecipes(Consumer<FinishedRecipe> provider) {
        for (MaterialStack[] stack : alloySmelterList) {
            if (stack[0].material().hasProperty(PropertyKey.INGOT)) {
                ALLOY_SMELTER_RECIPES.recipeBuilder("ingot_%s_dust_%s_ingot_%s.0".formatted(stack[0].material().getName(), stack[1].material().getName(), stack[2].material().getName()))
                        .duration((int) stack[2].amount() * 50).EUt(16)
                        .inputItems(ingot, stack[0].material(), (int) stack[0].amount())
                        .inputItems(dust, stack[1].material(), (int) stack[1].amount())
                        .outputItems(ChemicalHelper.get(ingot, stack[2].material(), (int) stack[2].amount()))
                        .save(provider);
            }
            if (stack[1].material().hasProperty(PropertyKey.INGOT)) {
                ALLOY_SMELTER_RECIPES.recipeBuilder("dust_%s_ingot_%s_ingot_%s.1".formatted(stack[0].material().getName(), stack[1].material().getName(), stack[2].material().getName()))
                        .duration((int) stack[2].amount() * 50).EUt(16)
                        .inputItems(dust, stack[0].material(), (int) stack[0].amount())
                        .inputItems(ingot, stack[1].material(), (int) stack[1].amount())
                        .outputItems(ChemicalHelper.get(ingot, stack[2].material(), (int) stack[2].amount()))
                        .save(provider);
            }
            if (stack[0].material().hasProperty(PropertyKey.INGOT)
                    && stack[1].material().hasProperty(PropertyKey.INGOT)) {
                ALLOY_SMELTER_RECIPES.recipeBuilder("ingot_%s_ingot_%s_ingot_%s.2".formatted(stack[0].material().getName(), stack[1].material().getName(), stack[2].material().getName()))
                        .duration((int) stack[2].amount() * 50).EUt(16)
                        .inputItems(ingot, stack[0].material(), (int) stack[0].amount())
                        .inputItems(ingot, stack[1].material(), (int) stack[1].amount())
                        .outputItems(ChemicalHelper.get(ingot, stack[2].material(), (int) stack[2].amount()))
                        .save(provider);
            }
            ALLOY_SMELTER_RECIPES.recipeBuilder("dust_%s_dust_%s_ingot_%s.3".formatted(stack[0].material().getName(), stack[1].material().getName(), stack[2].material().getName()))
                    .duration((int) stack[2].amount() * 50).EUt(16)
                    .inputItems(dust, stack[0].material(), (int) stack[0].amount())
                    .inputItems(dust, stack[1].material(), (int) stack[1].amount())
                    .outputItems(ChemicalHelper.get(ingot, stack[2].material(), (int) stack[2].amount()))
                    .save(provider);
        }

        COMPRESSOR_RECIPES.recipeBuilder(CARBON_MESH).inputItems(CARBON_FIBERS.asStack(2)).outputItems(CARBON_MESH.asStack()).duration(100).save(provider);
        COMPRESSOR_RECIPES.recipeBuilder(CARBON_FIBER_PLATE).inputItems(CARBON_MESH.asStack()).outputItems(CARBON_FIBER_PLATE.asStack()).save(provider);

        ALLOY_SMELTER_RECIPES.recipeBuilder("rubber_plate").duration(10).EUt(VA[ULV]).inputItems(ingot, Rubber, 2).notConsumable(SHAPE_MOLD_PLATE).outputItems(plate, Rubber).save(provider);
        ALLOY_SMELTER_RECIPES.recipeBuilder("rubber_ingot").duration(100).EUt(VA[ULV]).inputItems(dust, Sulfur).inputItems(dust, RawRubber, 3).outputItems(ingot, Rubber).save(provider);

        ALLOY_SMELTER_RECIPES.recipeBuilder(COKE_OVEN_BRICK).duration(150).EUt(VA[ULV]).inputItems(ItemTags.SAND).inputItems(new ItemStack(Items.CLAY_BALL)).outputItems(COKE_OVEN_BRICK.asStack(2)).save(provider);
    }

    private static void registerAssemblerRecipes(Consumer<FinishedRecipe> provider) {
        for (int i = 0; i < CHEMICAL_DYES.length; i++) {
            CANNER_RECIPES.recipeBuilder(SPRAY_CAN_DYES[i])
                    .inputItems(SPRAY_EMPTY)
                    .inputFluids(CHEMICAL_DYES[i].getFluid(GTValues.L * 4))
                    .outputItems(SPRAY_CAN_DYES[i])
                    .EUt(VA[ULV]).duration(200)
                    .save(provider);
        }

        CANNER_RECIPES.recipeBuilder(SPRAY_SOLVENT)
                .inputItems(SPRAY_EMPTY)
                .inputFluids(Acetone.getFluid(1000))
                .outputItems(SPRAY_SOLVENT)
                .EUt(VA[ULV]).duration(200)
                .save(provider);

        Material material = Iron;

        ASSEMBLER_RECIPES.recipeBuilder(COVER_SHUTTER)
                .inputItems(new ItemStack(Items.IRON_DOOR))
                .inputItems(plate, material, 2)
                .outputItems(COVER_SHUTTER.asStack(2))
                .EUt(16).duration(100)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(COVER_CRAFTING)
                .inputItems(Items.CRAFTING_TABLE)
                .inputItems(plate, material)
                .outputItems(COVER_CRAFTING.asStack())
                .EUt(16).duration(100)
                .save(provider);

        for (var solder : new FluidStack[]{Tin.getFluid(L), SolderingAlloy.getFluid(L / 2)}) {
            var fluidName = solder.getFluid() == Tin.getFluid() ? "tin" : "soldering_alloy";
            ASSEMBLER_RECIPES.recipeBuilder(COVER_MACHINE_CONTROLLER, fluidName)
                    .inputItems(new ItemStack(Blocks.LEVER))
                    .inputItems(plate, material)
                    .inputFluids(solder)
                    .outputItems(COVER_MACHINE_CONTROLLER)
                    .EUt(16).duration(100)
                    .save(provider);

            ASSEMBLER_RECIPES.recipeBuilder(COVER_ENERGY_DETECTOR, fluidName)
                    .inputItems(cableGtSingle, Copper, 4)
                    .inputItems(circuit, Tier.LV)
                    .inputItems(plate, material)
                    .inputFluids(solder)
                    .outputItems(COVER_ENERGY_DETECTOR)
                    .EUt(16).duration(100)
                    .save(provider);

            ASSEMBLER_RECIPES.recipeBuilder(COVER_ENERGY_DETECTOR_ADVANCED, fluidName)
                    .inputItems(COVER_ENERGY_DETECTOR)
                    .inputItems(SENSOR_HV)
                    .inputFluids(solder)
                    .outputItems(COVER_ENERGY_DETECTOR_ADVANCED)
                    .EUt(16).duration(100)
                    .save(provider);

            ASSEMBLER_RECIPES.recipeBuilder(COVER_ACTIVITY_DETECTOR, fluidName)
                    .inputItems(new ItemStack(Blocks.REDSTONE_TORCH))
                    .inputItems(plate, material)
                    .inputFluids(solder)
                    .outputItems(COVER_ACTIVITY_DETECTOR)
                    .EUt(16).duration(100)
                    .save(provider);

            ASSEMBLER_RECIPES.recipeBuilder(COVER_ACTIVITY_DETECTOR_ADVANCED, fluidName)
                    .inputItems(wireFine, Gold, 4)
                    .inputItems(circuit, Tier.HV)
                    .inputItems(plate, Aluminium)
                    .inputFluids(solder)
                    .outputItems(COVER_ACTIVITY_DETECTOR_ADVANCED)
                    .EUt(16).duration(100)
                    .save(provider);

            ASSEMBLER_RECIPES.recipeBuilder(COVER_FLUID_DETECTOR, fluidName)
                    .inputItems(new ItemStack(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE))
                    .inputItems(plate, material)
                    .inputFluids(solder)
                    .outputItems(COVER_FLUID_DETECTOR)
                    .EUt(16).duration(100)
                    .save(provider);

            ASSEMBLER_RECIPES.recipeBuilder(COVER_ITEM_DETECTOR, fluidName)
                    .inputItems(new ItemStack(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE))
                    .inputItems(plate, material)
                    .inputFluids(solder)
                    .outputItems(COVER_ITEM_DETECTOR)
                    .EUt(16).duration(100)
                    .save(provider);

            ASSEMBLER_RECIPES.recipeBuilder(COVER_FLUID_DETECTOR_ADVANCED, fluidName)
                    .inputItems(COVER_FLUID_DETECTOR)
                    .inputItems(SENSOR_HV)
                    .inputFluids(solder)
                    .outputItems(COVER_FLUID_DETECTOR_ADVANCED)
                    .EUt(16).duration(100)
                    .save(provider);

            ASSEMBLER_RECIPES.recipeBuilder(COVER_ITEM_DETECTOR_ADVANCED, fluidName)
                    .inputItems(COVER_ITEM_DETECTOR)
                    .inputItems(SENSOR_HV)
                    .inputFluids(solder)
                    .outputItems(COVER_ITEM_DETECTOR_ADVANCED)
                    .EUt(16).duration(100)
                    .save(provider);
        }

        ASSEMBLER_RECIPES.recipeBuilder(COVER_SCREEN)
                .inputItems(plate, Glass)
                .inputItems(foil, Aluminium, 4)
                .inputItems(circuit, Tier.LV)
                .inputItems(wireFine, Copper, 4)
                .outputItems(COVER_SCREEN)
                .EUt(16).duration(50)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(COVER_INFINITE_WATER)
                .inputItems(ELECTRIC_PUMP_HV, 2)
                .inputItems(new ItemStack(Items.CAULDRON))
                .inputItems(circuit, Tier.HV)
                .outputItems(COVER_INFINITE_WATER)
                .EUt(VA[HV]).duration(100)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(COVER_ENDER_FLUID_LINK)
                .inputItems(plate, EnderPearl, 9)
                .inputItems(plateDouble, StainlessSteel)
                .inputItems(SENSOR_HV)
                .inputItems(EMITTER_HV)
                .inputItems(ELECTRIC_PUMP_HV)
                .inputFluids(Polyethylene.getFluid(L * 2))
                .outputItems(COVER_ENDER_FLUID_LINK)
                .EUt(VA[HV]).duration(320)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("hull_casing.0").EUt(16).inputItems(plate, WroughtIron, 8).outputItems(HULL_CASING.get().getItemVariant(HullCasingBlock.CasingType.ULV)).circuitMeta(8).duration(25).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("hull_casing.1").EUt(16).inputItems(plate, Steel, 8).outputItems(HULL_CASING.get().getItemVariant(HullCasingBlock.CasingType.LV)).circuitMeta(8).duration(50).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("hull_casing.2").EUt(16).inputItems(plate, Aluminium, 8).outputItems(HULL_CASING.get().getItemVariant(HullCasingBlock.CasingType.MV)).circuitMeta(8).duration(50).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("hull_casing.3").EUt(16).inputItems(plate, StainlessSteel, 8).outputItems(HULL_CASING.get().getItemVariant(HullCasingBlock.CasingType.HV)).circuitMeta(8).duration(50).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("hull_casing.4").EUt(16).inputItems(plate, Titanium, 8).outputItems(HULL_CASING.get().getItemVariant(HullCasingBlock.CasingType.EV)).circuitMeta(8).duration(50).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("hull_casing.5").EUt(16).inputItems(plate, TungstenSteel, 8).outputItems(HULL_CASING.get().getItemVariant(HullCasingBlock.CasingType.IV)).circuitMeta(8).duration(50).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("hull_casing.6").EUt(16).inputItems(plate, RhodiumPlatedPalladium, 8).outputItems(HULL_CASING.get().getItemVariant(HullCasingBlock.CasingType.LuV)).circuitMeta(8).duration(50).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("hull_casing.7").EUt(16).inputItems(plate, NaquadahAlloy, 8).outputItems(HULL_CASING.get().getItemVariant(HullCasingBlock.CasingType.ZPM)).circuitMeta(8).duration(50).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("hull_casing.8").EUt(16).inputItems(plate, Darmstadtium, 8).outputItems(HULL_CASING.get().getItemVariant(HullCasingBlock.CasingType.UV)).circuitMeta(8).duration(50).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("hull_casing.9").EUt(16).inputItems(plate, Neutronium, 8).outputItems(HULL_CASING.get().getItemVariant(HullCasingBlock.CasingType.UHV)).circuitMeta(8).duration(50).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("wire_coil.0").EUt(VA[LV]).inputItems(wireGtDouble, Cupronickel, 8).inputItems(foil, Bronze, 8).inputFluids(TinAlloy.getFluid(GTValues.L)).outputItems(WIRE_COIL.get().getItemVariant(CoilBlock.CoilType.CUPRONICKEL)).duration(200).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("wire_coil.1").EUt(VA[MV]).inputItems(wireGtDouble, Kanthal, 8).inputItems(foil, Aluminium, 8).inputFluids(Copper.getFluid(GTValues.L)).outputItems(WIRE_COIL.get().getItemVariant(CoilBlock.CoilType.KANTHAL)).duration(300).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("wire_coil.2").EUt(VA[HV]).inputItems(wireGtDouble, Nichrome, 8).inputItems(foil, StainlessSteel, 8).inputFluids(Aluminium.getFluid(GTValues.L)).outputItems(WIRE_COIL.get().getItemVariant(CoilBlock.CoilType.NICHROME)).duration(400).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("wire_coil.3").EUt(VA[EV]).inputItems(wireGtDouble, TungstenSteel, 8).inputItems(foil, VanadiumSteel, 8).inputFluids(Nichrome.getFluid(GTValues.L)).outputItems(WIRE_COIL.get().getItemVariant(CoilBlock.CoilType.TUNGSTENSTEEL)).duration(500).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("wire_coil.4").EUt(VA[IV]).inputItems(wireGtDouble, HSSG, 8).inputItems(foil, TungstenCarbide, 8).inputFluids(Tungsten.getFluid(GTValues.L)).outputItems(WIRE_COIL.get().getItemVariant(CoilBlock.CoilType.HSS_G)).duration(600).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("wire_coil.5").EUt(VA[LuV]).inputItems(wireGtDouble, Naquadah, 8).inputItems(foil, Osmium, 8).inputFluids(TungstenSteel.getFluid(GTValues.L)).outputItems(WIRE_COIL.get().getItemVariant(CoilBlock.CoilType.NAQUADAH)).duration(700).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("wire_coil.6").EUt(VA[ZPM]).inputItems(wireGtDouble, Trinium, 8).inputItems(foil, NaquadahEnriched, 8).inputFluids(Naquadah.getFluid(GTValues.L)).outputItems(WIRE_COIL.get().getItemVariant(CoilBlock.CoilType.TRINIUM)).duration(800).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("wire_coil.7").EUt(VA[UV]).inputItems(wireGtDouble, Tritanium, 8).inputItems(foil, Naquadria, 8).inputFluids(Trinium.getFluid(GTValues.L)).outputItems(WIRE_COIL.get().getItemVariant(CoilBlock.CoilType.TRITANIUM)).duration(900).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("metal_casing.0").EUt(16).inputItems(plate, Bronze, 6).inputItems(Blocks.BRICKS.asItem()).circuitMeta(6).outputItems(CASING.get().getItemVariant(CasingBlock.CasingType.BRONZE_BRICKS, 2)).duration(50).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("metal_casing.1").EUt(16).inputItems(plate, Invar, 6).inputItems(frameGt, Invar, 1).circuitMeta(6).outputItems(CASING.get().getItemVariant(CasingBlock.CasingType.INVAR_HEATPROOF, 2)).duration(50).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("metal_casing.2").EUt(16).inputItems(plate, Steel, 6).inputItems(frameGt, Steel, 1).circuitMeta(6).outputItems(CASING.get().getItemVariant(CasingBlock.CasingType.STEEL_SOLID, 2)).duration(50).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("metal_casing.3").EUt(16).inputItems(plate, Aluminium, 6).inputItems(frameGt, Aluminium, 1).circuitMeta(6).outputItems(CASING.get().getItemVariant(CasingBlock.CasingType.ALUMINIUM_FROSTPROOF, 2)).duration(50).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("metal_casing.4").EUt(16).inputItems(plate, TungstenSteel, 6).inputItems(frameGt, TungstenSteel, 1).circuitMeta(6).outputItems(CASING.get().getItemVariant(CasingBlock.CasingType.TUNGSTENSTEEL_ROBUST, 2)).duration(50).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("metal_casing.5").EUt(16).inputItems(plate, StainlessSteel, 6).inputItems(frameGt, StainlessSteel, 1).circuitMeta(6).outputItems(CASING.get().getItemVariant(CasingBlock.CasingType.STAINLESS_CLEAN, 2)).duration(50).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("metal_casing.6").EUt(16).inputItems(plate, Titanium, 6).inputItems(frameGt, Titanium, 1).circuitMeta(6).outputItems(CASING.get().getItemVariant(CasingBlock.CasingType.TITANIUM_STABLE, 2)).duration(50).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("metal_casing.7").EUt(16).inputItems(plate, HSSE, 6).inputItems(frameGt, Europium).circuitMeta(6).outputItems(CASING.get().getItemVariant(CasingBlock.CasingType.HSSE_STURDY, 2)).duration(50).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("metal_casing.8").EUt(16).inputItems(CASING.get().getItemVariant(CasingBlock.CasingType.STEEL_SOLID)).inputFluids(Polytetrafluoroethylene.getFluid(216)).circuitMeta(6).outputItems(CASING.get().getItemVariant(CasingBlock.CasingType.PTFE_INERT)).duration(50).save(provider);

//        ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[LuV]).inputItems(wireGtDouble, IndiumTinBariumTitaniumCuprate, 32).inputItems(foil, NiobiumTitanium, 32).inputFluids(Trinium.getFluid(GTValues.L * 24)).outputItems(MetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasing.CasingType.SUPERCONDUCTOR_COIL)).duration(100).save(provider);
//        ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[ZPM]).inputItems(wireGtDouble, UraniumRhodiumDinaquadide, 16).inputItems(foil, NiobiumTitanium, 16).inputFluids(Trinium.getFluid(GTValues.L * 16)).outputItems(MetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasing.CasingType.SUPERCONDUCTOR_COIL)).duration(100).save(provider);
//        ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[UV]).inputItems(wireGtDouble, EnrichedNaquadahTriniumEuropiumDuranide, 8).inputItems(foil, NiobiumTitanium, 8).inputFluids(Trinium.getFluid(GTValues.L * 8)).outputItems(MetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasing.CasingType.SUPERCONDUCTOR_COIL)).duration(100).save(provider);

//        ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[ZPM]).inputItems(MetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasing.CasingType.SUPERCONDUCTOR_COIL)).inputItems(FIELD_GENERATOR_IV.asStack(2)).inputItems(ELECTRIC_PUMP_IV.asStack()).inputItems(NEUTRON_REFLECTOR.getStackForm(2)).inputItems(circuit, Tier.LuV, 4).inputItems(pipeSmallFluid, Naquadah, 4).inputItems(plate, Europium, 4).inputFluids(VanadiumGallium.getFluid(GTValues.L * 4)).outputItems(MetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasing.CasingType.FUSION_COIL)).duration(100).cleanroom(CleanroomType.CLEANROOM).save(provider);

//        ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[LuV]).inputItems(MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockGlassCasing.CasingType.LAMINATED_GLASS)).inputItems(plate, Naquadah, 4).inputItems(NEUTRON_REFLECTOR.asStack(4)).outputItems(MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockGlassCasing.CasingType.FUSION_GLASS, 2)).inputFluids(Polybenzimidazole.getFluid(GTValues.L)).duration(50).cleanroom(CleanroomType.CLEANROOM).save(provider);

//        ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[LuV]).inputItems(HULL_CASING.get().getItemVariant(HullCasingBlock.CasingType.LuV)).inputItems(MetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasing.CasingType.SUPERCONDUCTOR_COIL)).inputItems(NEUTRON_REFLECTOR.asStack()).inputItems(ELECTRIC_PUMP_LuV.asStack()).inputItems(plate, TungstenSteel, 6).inputFluids(Polybenzimidazole.getFluid(GTValues.L)).outputItems(MetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasing.CasingType.FUSION_CASING, 2)).duration(100).cleanroom(CleanroomType.CLEANROOM).save(provider);
//        ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[ZPM]).inputItems(HULL_CASING.get().getItemVariant(HullCasingBlock.CasingType.ZPM)).inputItems(MetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasing.CasingType.FUSION_COIL)).inputItems(VOLTAGE_COIL_ZPM.asStack(2)).inputItems(FIELD_GENERATOR_LuV.asStack()).inputItems(plate, Europium, 6).inputFluids(Polybenzimidazole.getFluid(GTValues.L * 2)).outputItems(MetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasing.CasingType.FUSION_CASING_MK2, 2)).duration(100).cleanroom(CleanroomType.CLEANROOM).save(provider);
//        ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[UV]).inputItems(HULL_CASING.get().getItemVariant(HullCasingBlock.CasingType.UV)).inputItems(MetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasing.CasingType.FUSION_COIL)).inputItems(VOLTAGE_COIL_UV.asStack(2)).inputItems(FIELD_GENERATOR_ZPM.asStack()).inputItems(plate, Americium, 6).inputFluids(Polybenzimidazole.getFluid(GTValues.L * 4)).outputItems(MetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasing.CasingType.FUSION_CASING_MK3, 2)).duration(100).cleanroom(CleanroomType.CLEANROOM).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("steel_casing.0").EUt(16).inputItems(plate, Magnalium, 6).inputItems(frameGt, BlueSteel, 1).circuitMeta(6).outputItems(CASING.get().getItemVariant(CasingBlock.CasingType.STEEL_TURBINE_CASING, 2)).duration(50).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("steel_casing.1").EUt(16).inputItems(CASING.get().getItemVariant(CasingBlock.CasingType.STEEL_TURBINE_CASING)).inputItems(plate, StainlessSteel, 6).circuitMeta(6).outputItems(CASING.get().getItemVariant(CasingBlock.CasingType.STAINLESS_TURBINE_CASING, 2)).duration(50).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("steel_casing.2").EUt(16).inputItems(CASING.get().getItemVariant(CasingBlock.CasingType.STEEL_TURBINE_CASING)).inputItems(plate, Titanium, 6).circuitMeta(6).outputItems(CASING.get().getItemVariant(CasingBlock.CasingType.TITANIUM_TURBINE_CASING, 2)).duration(50).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("steel_casing.3").EUt(16).inputItems(CASING.get().getItemVariant(CasingBlock.CasingType.STEEL_TURBINE_CASING)).inputItems(plate, TungstenSteel, 6).circuitMeta(6).outputItems(CASING.get().getItemVariant(CasingBlock.CasingType.TUNGSTENSTEEL_TURBINE_CASING, 2)).duration(50).save(provider);

//        ASSEMBLER_RECIPES.recipeBuilder().EUt(48).inputItems(frameGt, Steel).inputItems(plate, Polyethylene, 6).inputFluids(Concrete.getFluid(L)).outputItems(MetaBlocks.CLEANROOM_CASING.getItemVariant(BlockCleanroomCasing.CasingType.PLASCRETE, 2)).duration(200).save(provider);
//        ASSEMBLER_RECIPES.recipeBuilder().EUt(48).inputItems(frameGt, Steel).inputItems(plate, Polyethylene, 6).inputFluids(Glass.getFluid(L)).outputItems(MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockGlassCasing.CasingType.CLEANROOM_GLASS, 2)).duration(200).save(provider);

        // If these recipes are changed, change the values in MaterialInfoLoader.java

        ASSEMBLER_RECIPES.recipeBuilder("hull.0").duration(25).EUt(16).inputItems(HULL_CASING.get().getItemVariant(HullCasingBlock.CasingType.ULV)).inputItems(cableGtSingle, RedAlloy, 2).inputFluids(Polyethylene.getFluid(L * 2)).outputItems(HULL[0].asStack()).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("hull.1").duration(50).EUt(16).inputItems(HULL_CASING.get().getItemVariant(HullCasingBlock.CasingType.LV)).inputItems(cableGtSingle, Tin, 2).inputFluids(Polyethylene.getFluid(L * 2)).outputItems(HULL[1].asStack()).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("hull.2.0").duration(50).EUt(16).inputItems(HULL_CASING.get().getItemVariant(HullCasingBlock.CasingType.MV)).inputItems(cableGtSingle, Copper, 2).inputFluids(Polyethylene.getFluid(L * 2)).outputItems(HULL[2].asStack()).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("hull.2.1").duration(50).EUt(16).inputItems(HULL_CASING.get().getItemVariant(HullCasingBlock.CasingType.MV)).inputItems(cableGtSingle, AnnealedCopper, 2).inputFluids(Polyethylene.getFluid(L * 2)).outputItems(HULL[2].asStack()).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("hull.3").duration(50).EUt(16).inputItems(HULL_CASING.get().getItemVariant(HullCasingBlock.CasingType.HV)).inputItems(cableGtSingle, Gold, 2).inputFluids(Polyethylene.getFluid(L * 2)).outputItems(HULL[3].asStack()).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("hull.4").duration(50).EUt(16).inputItems(HULL_CASING.get().getItemVariant(HullCasingBlock.CasingType.EV)).inputItems(cableGtSingle, Aluminium, 2).inputFluids(Polyethylene.getFluid(L * 2)).outputItems(HULL[4].asStack()).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("hull.5").duration(50).EUt(16).inputItems(HULL_CASING.get().getItemVariant(HullCasingBlock.CasingType.IV)).inputItems(cableGtSingle, Platinum, 2).inputFluids(Polytetrafluoroethylene.getFluid(L * 2)).outputItems(HULL[5].asStack()).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("hull.6").duration(50).EUt(16).inputItems(HULL_CASING.get().getItemVariant(HullCasingBlock.CasingType.LuV)).inputItems(cableGtSingle, NiobiumTitanium, 2).inputFluids(Polytetrafluoroethylene.getFluid(L * 2)).outputItems(HULL[6].asStack()).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("hull.7").duration(50).EUt(16).inputItems(HULL_CASING.get().getItemVariant(HullCasingBlock.CasingType.ZPM)).inputItems(cableGtSingle, VanadiumGallium, 2).inputFluids(Polybenzimidazole.getFluid(L * 2)).outputItems(HULL[7].asStack()).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("hull.8").duration(50).EUt(16).inputItems(HULL_CASING.get().getItemVariant(HullCasingBlock.CasingType.UV)).inputItems(cableGtSingle, YttriumBariumCuprate, 2).inputFluids(Polybenzimidazole.getFluid(L * 2)).outputItems(HULL[8].asStack()).save(provider);
//        ASSEMBLER_RECIPES.recipeBuilder("hull.9").duration(50).EUt(16).inputItems(HULL_CASING.get().getItemVariant(HullCasingBlock.CasingType.UHV)).inputItems(cableGtSingle, Europium, 2).inputFluids(Polybenzimidazole.getFluid(L * 2)).outputItems(HULL[9].asStack()).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("hopper.0").EUt(2).inputItems(Items.CHEST).inputItems(plate, Iron, 5).outputItems(new ItemStack(Blocks.HOPPER)).duration(800).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("hopper.1").EUt(2).inputItems(Items.CHEST).inputItems(plate, WroughtIron, 5).outputItems(new ItemStack(Blocks.HOPPER)).duration(800).save(provider);

//        ASSEMBLER_RECIPES.recipeBuilder().EUt(16).inputItems(plank, Wood, 4).inputItems(screw, Iron, 4).outputItems(WOODEN_CRATE.asStack()).duration(100).circuitMeta(5).save(provider);
//        ASSEMBLER_RECIPES.recipeBuilder().EUt(16).inputItems(stickLong, Bronze, 4).inputItems(plate, Bronze, 4).outputItems(BRONZE_CRATE.asStack()).duration(200).circuitMeta(1).save(provider);
//        ASSEMBLER_RECIPES.recipeBuilder().EUt(16).inputItems(stickLong, Steel, 4).inputItems(plate, Steel, 4).outputItems(STEEL_CRATE.asStack()).duration(200).circuitMeta(1).save(provider);
//        ASSEMBLER_RECIPES.recipeBuilder().EUt(16).inputItems(stickLong, Aluminium, 4).inputItems(plate, Aluminium, 4).outputItems(ALUMINIUM_CRATE.asStack()).duration(200).circuitMeta(1).save(provider);
//        ASSEMBLER_RECIPES.recipeBuilder().EUt(16).inputItems(stickLong, StainlessSteel, 4).inputItems(plate, StainlessSteel, 4).outputItems(STAINLESS_STEEL_CRATE.asStack()).circuitMeta(1).duration(200).save(provider);
//        ASSEMBLER_RECIPES.recipeBuilder().EUt(16).inputItems(stickLong, Titanium, 4).inputItems(plate, Titanium, 4).outputItems(TITANIUM_CRATE.asStack()).duration(200).circuitMeta(1).save(provider);
//        ASSEMBLER_RECIPES.recipeBuilder().EUt(16).inputItems(stickLong, TungstenSteel, 4).inputItems(plate, TungstenSteel, 4).outputItems(TUNGSTENSTEEL_CRATE.asStack()).duration(200).circuitMeta(1).save(provider);
//
//        ASSEMBLER_RECIPES.recipeBuilder().EUt(16).inputItems(stickLong, Bronze, 2).inputItems(plate, Bronze, 4).outputItems(BRONZE_DRUM.asStack()).duration(200).circuitMeta(2).save(provider);
//        ASSEMBLER_RECIPES.recipeBuilder().EUt(16).inputItems(stickLong, Steel, 2).inputItems(plate, Steel, 4).outputItems(STEEL_DRUM.asStack()).duration(200).circuitMeta(2).save(provider);
//        ASSEMBLER_RECIPES.recipeBuilder().EUt(16).inputItems(stickLong, Aluminium, 2).inputItems(plate, Aluminium, 4).outputItems(ALUMINIUM_DRUM.asStack()).duration(200).circuitMeta(2).save(provider);
//        ASSEMBLER_RECIPES.recipeBuilder().EUt(16).inputItems(stickLong, StainlessSteel, 2).inputItems(plate, StainlessSteel, 4).outputItems(STAINLESS_STEEL_DRUM.asStack()).duration(200).circuitMeta(2).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("duct_tape.0").EUt(VA[LV]).inputItems(foil, Polyethylene, 4).inputItems(CARBON_MESH).inputFluids(Polyethylene.getFluid(288)).outputItems(DUCT_TAPE).duration(100).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("duct_tape.1").EUt(VA[LV]).inputItems(foil, SiliconeRubber, 2).inputItems(CARBON_MESH).inputFluids(Polyethylene.getFluid(288)).outputItems(DUCT_TAPE, 2).duration(100).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("duct_tape.2").EUt(VA[LV]).inputItems(foil, Polycaprolactam, 2).inputItems(CARBON_MESH).inputFluids(Polyethylene.getFluid(144)).outputItems(DUCT_TAPE, 4).duration(100).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("duct_tape.3").EUt(VA[LV]).inputItems(foil, Polybenzimidazole).inputItems(CARBON_MESH).inputFluids(Polyethylene.getFluid(72)).outputItems(DUCT_TAPE, 8).duration(100).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(FLUID_CELL_LARGE_STEEL)
                .inputItems(plateDouble, Steel, 2)
                .inputItems(ring, Bronze, 2)
                .outputItems(FLUID_CELL_LARGE_STEEL)
                .duration(200).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(FLUID_CELL_LARGE_ALUMINIUM)
                .inputItems(plateDouble, Aluminium, 2)
                .inputItems(ring, Silver, 2)
                .outputItems(FLUID_CELL_LARGE_ALUMINIUM)
                .duration(200).EUt(64).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(FLUID_CELL_LARGE_STAINLESS_STEEL)
                .inputItems(plateDouble, StainlessSteel, 3)
                .inputItems(ring, Electrum, 3)
                .outputItems(FLUID_CELL_LARGE_STAINLESS_STEEL)
                .duration(200).EUt(VA[MV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(FLUID_CELL_LARGE_TITANIUM)
                .inputItems(plateDouble, Titanium, 3)
                .inputItems(ring, RoseGold, 3)
                .outputItems(FLUID_CELL_LARGE_TITANIUM)
                .duration(200).EUt(256).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(FLUID_CELL_LARGE_TUNGSTEN_STEEL)
                .inputItems(plateDouble, TungstenSteel, 4)
                .inputItems(ring, Platinum, 4)
                .outputItems(FLUID_CELL_LARGE_TUNGSTEN_STEEL)
                .duration(200).EUt(VA[HV]).save(provider);
    }

    private static void registerBlastFurnaceRecipes(Consumer<FinishedRecipe> provider) {
        BLAST_RECIPES.recipeBuilder("aluminium.0").duration(400).EUt(100).inputItems(dust, Ruby).outputItems(nugget, Aluminium, 3).outputItems(dustTiny, DarkAsh).blastFurnaceTemp(1200).save(provider);
        BLAST_RECIPES.recipeBuilder("aluminium.1").duration(320).EUt(100).inputItems(gem, Ruby).outputItems(nugget, Aluminium, 3).outputItems(dustTiny, DarkAsh).blastFurnaceTemp(1200).save(provider);
        BLAST_RECIPES.recipeBuilder("aluminium.2").duration(400).EUt(100).inputItems(dust, GreenSapphire).outputItems(nugget, Aluminium, 3).outputItems(dustTiny, DarkAsh).blastFurnaceTemp(1200).save(provider);
        BLAST_RECIPES.recipeBuilder("aluminium.3").duration(320).EUt(100).inputItems(gem, GreenSapphire).outputItems(nugget, Aluminium, 3).outputItems(dustTiny, DarkAsh).blastFurnaceTemp(1200).save(provider);
        BLAST_RECIPES.recipeBuilder("aluminium.4").duration(400).EUt(100).inputItems(dust, Sapphire).outputItems(nugget, Aluminium, 3).blastFurnaceTemp(1200).save(provider);
        BLAST_RECIPES.recipeBuilder("aluminium.5").duration(320).EUt(100).inputItems(gem, Sapphire).outputItems(nugget, Aluminium, 3).blastFurnaceTemp(1200).save(provider);
        BLAST_RECIPES.recipeBuilder("titanium.0").duration(800).EUt(VA[HV]).inputItems(dust, Magnesium, 2).inputFluids(TitaniumTetrachloride.getFluid(1000)).outputItems(ChemicalHelper.get(ingotHot, Titanium), ChemicalHelper.get(dust, MagnesiumChloride, 6)).blastFurnaceTemp(Titanium.getBlastTemperature() + 200).save(provider);
        BLAST_RECIPES.recipeBuilder("steel.0").duration(500).EUt(VA[MV]).inputItems(ingot, Iron).inputFluids(Oxygen.getFluid(200)).outputItems(ingot, Steel).outputItems(dustTiny, Ash).blastFurnaceTemp(1000).save(provider);
        BLAST_RECIPES.recipeBuilder("steel.1").duration(300).EUt(VA[MV]).inputItems(ingot, WroughtIron).inputFluids(Oxygen.getFluid(200)).outputItems(ingot, Steel).outputItems(dustTiny, Ash).blastFurnaceTemp(1000).save(provider);

        BLAST_RECIPES.recipeBuilder("fluid_carbon_dioxide")
                .inputItems(dust, Ilmenite, 10)
                .inputItems(dust, Carbon, 4)
                .outputItems(ingot, WroughtIron, 2)
                .outputItems(dust, Rutile, 4)
                .outputFluids(CarbonDioxide.getFluid(2000))
                .blastFurnaceTemp(1700)
                .duration(1600).EUt(VA[HV]).save(provider);

        //Tempered Glass
//        BLAST_RECIPES.recipeBuilder()
//                .inputItems(block, Glass)
//                .inputFluids(Oxygen.getFluid(100))
//                .outputItems(MetaBlocks.TRANSPARENT_CASING.getItemVariant(
//                        BlockGlassCasing.CasingType.TEMPERED_GLASS))
//                .blastFurnaceTemp(1000)
//                .duration(200).EUt(VA[MV]).save(provider);

        registerBlastFurnaceMetallurgyRecipes(provider);
    }

    private static void registerBlastFurnaceMetallurgyRecipes(Consumer<FinishedRecipe> provider) {
        createSulfurDioxideRecipe(Stibnite, AntimonyTrioxide, 1500, provider);
        createSulfurDioxideRecipe(Sphalerite, Zincite, 1000, provider);
        createSulfurDioxideRecipe(Pyrite, BandedIron, 2000, provider);
        createSulfurDioxideRecipe(Pentlandite, Garnierite, 1000, provider);

        BLAST_RECIPES.recipeBuilder("cup_ant_sulf").duration(120).EUt(VA[MV]).blastFurnaceTemp(1200)
                .inputItems(dust, Tetrahedrite)
                .inputFluids(Oxygen.getFluid(3000))
                .outputItems(dust, CupricOxide)
                .outputItems(dustTiny, AntimonyTrioxide, 3)
                .outputFluids(SulfurDioxide.getFluid(2000))
                .save(provider);

        BLAST_RECIPES.recipeBuilder("cup_ars_sulf").duration(120).EUt(VA[MV]).blastFurnaceTemp(1200)
                .inputItems(dust, Cobaltite)
                .inputFluids(Oxygen.getFluid(3000))
                .outputItems(dust, CobaltOxide)
                .outputItems(dust, ArsenicTrioxide)
                .outputFluids(SulfurDioxide.getFluid(1000))
                .save(provider);

        BLAST_RECIPES.recipeBuilder("mas_sil_sulf").duration(120).EUt(VA[MV]).blastFurnaceTemp(1200)
                .inputItems(dust, Galena)
                .inputFluids(Oxygen.getFluid(3000))
                .outputItems(dust, Massicot)
                .outputItems(nugget, Silver, 6)
                .outputFluids(SulfurDioxide.getFluid(1000))
                .save(provider);

        BLAST_RECIPES.recipeBuilder("cup_fer_sulf").duration(120).EUt(VA[MV]).blastFurnaceTemp(1200)
                .inputItems(dust, Chalcopyrite)
                .inputItems(dust, SiliconDioxide)
                .inputFluids(Oxygen.getFluid(3000))
                .outputItems(dust, CupricOxide)
                .outputItems(dust, Ferrosilite)
                .outputFluids(SulfurDioxide.getFluid(2000))
                .save(provider);

        BLAST_RECIPES.recipeBuilder("sil_ash_car").duration(240).EUt(VA[MV]).blastFurnaceTemp(1200)
                .inputItems(dust, SiliconDioxide)
                .inputItems(dust, Carbon, 2)
                .outputItems(ingot, Silicon)
                .outputItems(dustTiny, Ash)
                .outputFluids(CarbonMonoxide.getFluid(2000))
                .save(provider);
    }

    private static void createSulfurDioxideRecipe(Material inputMaterial, Material outputMaterial, int sulfurDioxideAmount, Consumer<FinishedRecipe> provider) {
        BLAST_RECIPES.recipeBuilder(inputMaterial.getName() + "_ash_sulfur_dioxide_" + outputMaterial.getName()).duration(120).EUt(VA[MV]).blastFurnaceTemp(1200)
                .inputItems(dust, inputMaterial)
                .inputFluids(Oxygen.getFluid(3000))
                .outputItems(dust, outputMaterial)
                .outputItems(dustTiny, Ash)
                .outputFluids(SulfurDioxide.getFluid(sulfurDioxideAmount))
                .save(provider);
    }

    private static void registerDecompositionRecipes(Consumer<FinishedRecipe> provider) {


        EXTRACTOR_RECIPES.recipeBuilder("dust_raw_rubber.0")
                .inputItems(STICKY_RESIN.asStack())
                .outputItems(dust, RawRubber, 3)
                .duration(150).EUt(2)
                .save(provider);
//
//        EXTRACTOR_RECIPES.recipeBuilder().duration(300).EUt(2)
//                .inputItems(new ItemStack(RUBBER_LEAVES, 16))
//                .outputItems(dust, RawRubber)
//                .save(provider);
//
//        EXTRACTOR_RECIPES.recipeBuilder().duration(300).EUt(2)
//                .inputItems(new ItemStack(RUBBER_LOG))
//                .outputItems(dust, RawRubber)
//                .save(provider);
//
//        EXTRACTOR_RECIPES.recipeBuilder().duration(300).EUt(2)
//                .inputItems(new ItemStack(RUBBER_SAPLING))
//                .outputItems(dust, RawRubber)
//                .save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("dust_raw_rubber.1").duration(150).EUt(2)
                .inputItems(new ItemStack(Items.SLIME_BALL))
                .outputItems(dust, RawRubber, 2)
                .save(provider);

        COMPRESSOR_RECIPES.recipeBuilder("plant_ball.0").duration(300).EUt(2).inputItems(ItemTags.SAPLINGS, 8).outputItems(PLANT_BALL).save(provider);
        COMPRESSOR_RECIPES.recipeBuilder("plant_ball.1").duration(300).EUt(2).inputItems(new ItemStack(Items.WHEAT, 8)).outputItems(PLANT_BALL).save(provider);
        COMPRESSOR_RECIPES.recipeBuilder("plant_ball.2").duration(300).EUt(2).inputItems(new ItemStack(Items.POTATO, 8)).outputItems(PLANT_BALL).save(provider);
        COMPRESSOR_RECIPES.recipeBuilder("plant_ball.3").duration(300).EUt(2).inputItems(new ItemStack(Items.CARROT, 8)).outputItems(PLANT_BALL).save(provider);
        COMPRESSOR_RECIPES.recipeBuilder("plant_ball.4").duration(300).EUt(2).inputItems(new ItemStack(Blocks.CACTUS, 8)).outputItems(PLANT_BALL).save(provider);
        COMPRESSOR_RECIPES.recipeBuilder("plant_ball.5").duration(300).EUt(2).inputItems(new ItemStack(Items.SUGAR_CANE, 8)).outputItems(PLANT_BALL).save(provider);
        COMPRESSOR_RECIPES.recipeBuilder("plant_ball.6").duration(300).EUt(2).inputItems(new ItemStack(Blocks.BROWN_MUSHROOM, 8)).outputItems(PLANT_BALL).save(provider);
        COMPRESSOR_RECIPES.recipeBuilder("plant_ball.7").duration(300).EUt(2).inputItems(new ItemStack(Blocks.RED_MUSHROOM, 8)).outputItems(PLANT_BALL).save(provider);
        COMPRESSOR_RECIPES.recipeBuilder("plant_ball.8").duration(300).EUt(2).inputItems(new ItemStack(Items.BEETROOT, 8)).outputItems(PLANT_BALL).save(provider);

    }

    private static void registerRecyclingRecipes(Consumer<FinishedRecipe> provider) {

        MACERATOR_RECIPES.recipeBuilder("dust_tungstate")
                .inputItems(stone, Endstone)
                .outputItems(dust, Endstone)
                .chancedOutput(dustTiny, Tungstate, 1200, 280)
                .save(provider);

        MACERATOR_RECIPES.recipeBuilder("nugget_gold")
                .inputItems(stone, Netherrack)
                .outputItems(dust, Netherrack)
                .chancedOutput(nugget, Gold, 500, 120)
                .save(provider);

//        if (!OreDictionary.getOres("stoneSoapstone").isEmpty())
//            MACERATOR_RECIPES.recipeBuilder()
//                    .inputItems(stone, Soapstone)
//                    .outputItems(dustImpure, Talc)
//                    .chancedOutput(dustTiny, Chromite, 1000, 280)
//                    .save(provider);
//
//        if (!OreDictionary.getOres("stoneRedrock").isEmpty())
//            MACERATOR_RECIPES.recipeBuilder()
//                    .inputItems(stone, Redrock)
//                    .outputItems(dust, Redrock)
//                    .chancedOutput(dust, Redrock, 1000, 380)
//                    .save(provider);

        MACERATOR_RECIPES.recipeBuilder(Marble.getName())
                .inputItems(stone, Marble)
                .outputItems(dust, Marble)
                .chancedOutput(dust, Marble, 1000, 380)
                .save(provider);

        MACERATOR_RECIPES.recipeBuilder(Basalt.getName())
                .inputItems(stone, Basalt)
                .outputItems(dust, Basalt)
                .chancedOutput(dust, Basalt, 1000, 380)
                .save(provider);

        MACERATOR_RECIPES.recipeBuilder(GraniteBlack.getName())
                .inputItems(stone, GraniteBlack)
                .outputItems(dust, GraniteBlack)
                .chancedOutput(dust, Thorium, 100, 40)
                .save(provider);

        MACERATOR_RECIPES.recipeBuilder(GraniteRed.getName())
                .inputItems(stone, GraniteRed)
                .outputItems(dust, GraniteRed)
                .chancedOutput(dustSmall, Uranium238, 100, 40)
                .save(provider);

        MACERATOR_RECIPES.recipeBuilder(Andesite.getName())
                .inputItems(stone, Andesite)
                .outputItems(dust, Andesite)
                .chancedOutput(dustSmall, Stone, 100, 40)
                .save(provider);

        MACERATOR_RECIPES.recipeBuilder(Diorite.getName())
                .inputItems(stone, Diorite)
                .outputItems(dust, Diorite)
                .chancedOutput(dustSmall, Stone, 100, 40)
                .save(provider);

        MACERATOR_RECIPES.recipeBuilder(Granite.getName())
                .inputItems(stone, Granite)
                .outputItems(dust, Granite)
                .chancedOutput(dustSmall, Stone, 100, 40)
                .save(provider);

        MACERATOR_RECIPES.recipeBuilder(Items.PORKCHOP)
                .inputItems(Items.PORKCHOP)
                .outputItems(dustSmall, Meat, 6)
                .outputItems(dustTiny, Bone)
                .duration(102).save(provider);

        MACERATOR_RECIPES.recipeBuilder("fish")
                .inputItems(ItemTags.FISHES)
                .outputItems(dustSmall, Meat, 6)
                .outputItems(dustTiny, Bone)
                .duration(102).save(provider);

        MACERATOR_RECIPES.recipeBuilder(Items.CHICKEN)
                .inputItems(Items.CHICKEN)
                .outputItems(dust, Meat)
                .outputItems(dustTiny, Bone)
                .duration(102).save(provider);

        MACERATOR_RECIPES.recipeBuilder(Items.BEEF)
                .inputItems(Items.BEEF)
                .outputItems(dustSmall, Meat, 6)
                .outputItems(dustTiny, Bone)
                .duration(102).save(provider);

        MACERATOR_RECIPES.recipeBuilder(Items.RABBIT)
                .inputItems(Items.RABBIT)
                .outputItems(dustSmall, Meat, 6)
                .outputItems(dustTiny, Bone)
                .duration(102).save(provider);

        MACERATOR_RECIPES.recipeBuilder(Items.MUTTON)
                .inputItems(Items.MUTTON)
                .outputItems(dust, Meat)
                .outputItems(dustTiny, Bone)
                .duration(102).save(provider);


    }

    private static void registerFluidRecipes(Consumer<FinishedRecipe> provider) {

        FLUID_HEATER_RECIPES.recipeBuilder("ice_water").duration(32).EUt(4)
                .inputFluids(Ice.getFluid(L))
                .circuitMeta(1)
                .outputFluids(Water.getFluid(L)).save(provider);

        FLUID_SOLIDFICATION_RECIPES.recipeBuilder("toluene_" + GELLED_TOLUENE.get())
                .inputFluids(Toluene.getFluid(100))
                .notConsumable(SHAPE_MOLD_BALL)
                .outputItems(GELLED_TOLUENE)
                .duration(100).EUt(16).save(provider);

        for (int i = 0; i < CHEMICAL_DYES.length; i++) {
            FLUID_SOLIDFICATION_RECIPES.recipeBuilder(DYE_ONLY_ITEMS[i])
                    .inputFluids(CHEMICAL_DYES[i].getFluid(GTValues.L / 2))
                    .notConsumable(SHAPE_MOLD_BALL.asStack())
                    .outputItems(DYE_ONLY_ITEMS[i].asStack())
                    .duration(100).EUt(16).save(provider);
        }

        FLUID_HEATER_RECIPES.recipeBuilder("water.0").duration(30).EUt(VA[LV]).inputFluids(Water.getFluid(6)).circuitMeta(1).outputFluids(Steam.getFluid(960)).save(provider);
        FLUID_HEATER_RECIPES.recipeBuilder("water.1").duration(30).EUt(VA[LV]).inputFluids(DistilledWater.getFluid(6)).circuitMeta(1).outputFluids(Steam.getFluid(960)).save(provider);
    }

//    private static void registerSmoothRecipe(List<ItemStack> roughStack, List<ItemStack> smoothStack, ) {
//        for (int i = 0; i < roughStack.size(); i++) {
//            VanillaRecipeHelper.addSmeltingRecipe(provider, roughStack.get(i), smoothStack.get(i), 0.1f);
//
//            EXTRUDER_RECIPES.recipeBuilder()
//                    .inputItems(roughStack.get(i))
//                    .notConsumable(SHAPE_EXTRUDER_BLOCK.asStack())
//                    .outputItems(smoothStack.get(i))
//                    .duration(24).EUt(8).save(provider);
//        }
//    }
//
//    private static void registerCobbleRecipe(List<ItemStack> smoothStack, List<ItemStack> cobbleStack) {
//        for (int i = 0; i < smoothStack.size(); i++) {
//            FORGE_HAMMER_RECIPES.recipeBuilder()
//                    .inputItems(smoothStack.get(i))
//                    .outputItems(cobbleStack.get(i))
//                    .duration(12).EUt(4).save(provider);
//        }
//    }
//
//    private static void registerBricksRecipe(List<ItemStack> polishedStack, List<ItemStack> brickStack, MarkerMaterial color) {
//        for (int i = 0; i < polishedStack.size(); i++) {
//            LASER_ENGRAVER_RECIPES.recipeBuilder()
//                    .inputItems(polishedStack.get(i))
//                    .notConsumable(craftingLens, color)
//                    .outputItems(brickStack.get(i))
//                    .duration(50).EUt(16).save(provider);
//        }
//    }
//
//    private static void registerMossRecipe(List<ItemStack> regularStack, List<ItemStack> mossStack) {
//        for (int i = 0; i < regularStack.size(); i++) {
//            CHEMICAL_BATH_RECIPES.recipeBuilder()
//                    .inputItems(regularStack.get(i))
//                    .inputFluids(Water.getFluid(100))
//                    .outputItems(mossStack.get(i))
//                    .duration(50).EUt(16).save(provider);
//        }
//    }

    private static void registerNBTRemoval(Consumer<FinishedRecipe> provider) {
//        for (MetaTileEntityQuantumChest chest : MetaTileEntities.QUANTUM_CHEST)
//            if (chest != null) {
//                VanillaRecipeHelper.addShapelessNBTClearingRecipe("quantum_chest_nbt_" + chest.getTier() + chest.getMetaName(), chest.asStack(), chest.asStack());
//            }
//
//        for (MetaTileEntityQuantumTank tank : MetaTileEntities.QUANTUM_TANK)
//            if (tank != null) {
//                VanillaRecipeHelper.addShapelessNBTClearingRecipe("quantum_tank_nbt_" + tank.getTier() + tank.getMetaName(), tank.asStack(), tank.asStack());
//            }
//
//        //Drums
//        VanillaRecipeHelper.addShapelessNBTClearingRecipe("drum_nbt_wood", MetaTileEntities.WOODEN_DRUM.asStack(), MetaTileEntities.WOODEN_DRUM.asStack());
//        VanillaRecipeHelper.addShapelessNBTClearingRecipe("drum_nbt_bronze", MetaTileEntities.BRONZE_DRUM.asStack(), MetaTileEntities.BRONZE_DRUM.asStack());
//        VanillaRecipeHelper.addShapelessNBTClearingRecipe("drum_nbt_steel", MetaTileEntities.STEEL_DRUM.asStack(), MetaTileEntities.STEEL_DRUM.asStack());
//        VanillaRecipeHelper.addShapelessNBTClearingRecipe("drum_nbt_aluminium", MetaTileEntities.ALUMINIUM_DRUM.asStack(), MetaTileEntities.ALUMINIUM_DRUM.asStack());
//        VanillaRecipeHelper.addShapelessNBTClearingRecipe("drum_nbt_stainless_steel", MetaTileEntities.STAINLESS_STEEL_DRUM.asStack(), MetaTileEntities.STAINLESS_STEEL_DRUM.asStack());
//
//        // Cells
//        VanillaRecipeHelper.addShapedNBTClearingRecipe("cell_nbt_regular", FLUID_CELL.asStack(), " C", "  ", 'C', FLUID_CELL.asStack());
//        VanillaRecipeHelper.addShapedNBTClearingRecipe("cell_nbt_universal", FLUID_CELL_UNIVERSAL.asStack(), " C", "  ", 'C', FLUID_CELL_UNIVERSAL.asStack());
//        VanillaRecipeHelper.addShapelessNBTClearingRecipe("cell_nbt_steel", FLUID_CELL_LARGE_STEEL.asStack(), FLUID_CELL_LARGE_STEEL.asStack());
//        VanillaRecipeHelper.addShapelessNBTClearingRecipe("cell_nbt_aluminium", FLUID_CELL_LARGE_ALUMINIUM.asStack(), FLUID_CELL_LARGE_ALUMINIUM.asStack());
//        VanillaRecipeHelper.addShapelessNBTClearingRecipe("cell_nbt_stainless_steel", FLUID_CELL_LARGE_STAINLESS_STEEL.asStack(), FLUID_CELL_LARGE_STAINLESS_STEEL.asStack());
//        VanillaRecipeHelper.addShapelessNBTClearingRecipe("cell_nbt_titanium", FLUID_CELL_LARGE_TITANIUM.asStack(), FLUID_CELL_LARGE_TITANIUM.asStack());
//        VanillaRecipeHelper.addShapelessNBTClearingRecipe("cell_nbt_tungstensteel", FLUID_CELL_LARGE_TUNGSTEN_STEEL.asStack(), FLUID_CELL_LARGE_TUNGSTEN_STEEL.asStack());
//        VanillaRecipeHelper.addShapelessNBTClearingRecipe("cell_vial_nbt", FLUID_CELL_GLASS_VIAL.asStack(), FLUID_CELL_GLASS_VIAL.asStack());
//
//
//        //Jetpacks
//        VanillaRecipeHelper.addShapelessRecipe("fluid_jetpack_clear", SEMIFLUID_JETPACK.asStack(), SEMIFLUID_JETPACK.asStack());

    }

    private static void ConvertHatchToHatch(Consumer<FinishedRecipe> provider) {
        for (int i = 0; i < FLUID_IMPORT_HATCH.length; i++) {
            if (FLUID_IMPORT_HATCH[i] != null && FLUID_EXPORT_HATCH[i] != null) {

                VanillaRecipeHelper.addShapedRecipe(provider, "fluid_hatch_output_to_input_" + FLUID_IMPORT_HATCH[i].getTier(), FLUID_IMPORT_HATCH[i].asStack(),
                        "d", "B", 'B', FLUID_EXPORT_HATCH[i].asStack());
                VanillaRecipeHelper.addShapedRecipe(provider, "fluid_hatch_input_to_output_" + FLUID_EXPORT_HATCH[i].getTier(), FLUID_EXPORT_HATCH[i].asStack(),
                        "d", "B", 'B', FLUID_IMPORT_HATCH[i].asStack());
            }
        }
        for (int i = 0; i < ITEM_IMPORT_BUS.length; i++) {
            if (ITEM_IMPORT_BUS[i] != null && ITEM_EXPORT_BUS[i] != null) {

                VanillaRecipeHelper.addShapedRecipe(provider, "item_bus_output_to_input_" + ITEM_IMPORT_BUS[i].getTier(), ITEM_IMPORT_BUS[i].asStack(),
                        "d", "B", 'B', ITEM_EXPORT_BUS[i].asStack());
                VanillaRecipeHelper.addShapedRecipe(provider, "item_bus_input_to_output_" + ITEM_EXPORT_BUS[i].getTier(), ITEM_EXPORT_BUS[i].asStack(),
                        "d", "B", 'B', ITEM_IMPORT_BUS[i].asStack());
            }
        }

//        for (int i = 0; i < MULTI_FLUID_IMPORT_HATCH.length; i++) {
//            if (MULTI_FLUID_IMPORT_HATCH[i] != null && MULTI_FLUID_EXPORT_HATCH[i] != null) {
//
//                VanillaRecipeHelper.addShapedRecipe(provider, "multi_fluid_hatch_output_to_input_" + MULTI_FLUID_IMPORT_HATCH[i].getTier(), MULTI_FLUID_IMPORT_HATCH[i].asStack(),
//                        "d", "B", 'B', MULTI_FLUID_EXPORT_HATCH[i].asStack());
//                VanillaRecipeHelper.addShapedRecipe(provider, "multi_fluid_hatch_input_to_output_" + MULTI_FLUID_EXPORT_HATCH[i].getTier(), MULTI_FLUID_EXPORT_HATCH[i].asStack(),
//                        "d", "B", 'B', MULTI_FLUID_IMPORT_HATCH[i].asStack());
//            }
//        }
//
//        if (STEAM_EXPORT_BUS != null && STEAM_IMPORT_BUS != null) {
//            //Steam
//            VanillaRecipeHelper.addShapedRecipe(provider, "steam_bus_output_to_input_" + STEAM_EXPORT_BUS.getTier(), STEAM_EXPORT_BUS.asStack(),
//                    "d", "B", 'B', STEAM_IMPORT_BUS.asStack());
//            VanillaRecipeHelper.addShapedRecipe(provider, "steam_bus_input_to_output_" + STEAM_IMPORT_BUS.getTier(), STEAM_IMPORT_BUS.asStack(),
//                    "d", "B", 'B', STEAM_EXPORT_BUS.asStack());
//        }
    }
}
