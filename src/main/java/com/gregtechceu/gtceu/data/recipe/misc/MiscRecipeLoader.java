package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterials.Color;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidContainerIngredient;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeCategories;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;

import com.tterrag.registrate.util.entry.ItemEntry;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTItems.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public class MiscRecipeLoader {

    public static void init(Consumer<FinishedRecipe> provider) {
        // Basic Terminal Recipe
        VanillaRecipeHelper.addShapedRecipe(provider, true, "basic_terminal", TERMINAL.asStack(),
                "SGS", "PBP", "PWP", 'S', new MaterialEntry(SCREW, WroughtIron), 'G', Tags.Items.GLASS_PANES, 'B',
                new ItemStack(Items.BOOK),
                'P', new MaterialEntry(PLATE, WroughtIron), 'W', new MaterialEntry(WIRE_GT_SINGLE, RedAlloy));
        // Machine Memory Card Recipe
        VanillaRecipeHelper.addShapedRecipe(provider, true, "machine_memory_card", MACHINE_MEMORY_CARD.asStack(),
                "PWP", "SLS", "PPP", 'P', new MaterialEntry(PLATE, Steel), 'W',
                new MaterialEntry(WIRE_GT_SINGLE, Copper), 'S', new MaterialEntry(SCREW, RedAlloy), 'L',
                CustomTags.LV_CIRCUITS);
        // Potin Recipe
        VanillaRecipeHelper.addShapelessRecipe(provider, "potin_dust", ChemicalHelper.get(DUST, Potin, 8),
                new MaterialEntry(DUST, Copper),
                new MaterialEntry(DUST, Copper),
                new MaterialEntry(DUST, Copper),
                new MaterialEntry(DUST, Copper),
                new MaterialEntry(DUST, Copper),
                new MaterialEntry(DUST, Copper),
                new MaterialEntry(DUST, Tin),
                new MaterialEntry(DUST, Tin),
                new MaterialEntry(DUST, Lead));

        MIXER_RECIPES.recipeBuilder("fermented_spider_eye_brown").duration(100).EUt(VA[ULV])
                .inputItems(DUST, Sugar)
                .inputItems(new ItemStack(Blocks.BROWN_MUSHROOM))
                .inputItems(new ItemStack(Items.SPIDER_EYE))
                .outputItems(new ItemStack(Items.FERMENTED_SPIDER_EYE))
                .save(provider);

        MIXER_RECIPES.recipeBuilder("fermented_spider_eye_red").duration(100).EUt(VA[ULV])
                .inputItems(DUST, Sugar)
                .inputItems(new ItemStack(Blocks.RED_MUSHROOM))
                .inputItems(new ItemStack(Items.SPIDER_EYE))
                .outputItems(new ItemStack(Items.FERMENTED_SPIDER_EYE))
                .save(provider);

        SIFTER_RECIPES.recipeBuilder("gravel_sifting").duration(100).EUt(16)
                .inputItems(new ItemStack(Blocks.GRAVEL))
                .outputItems(GEM, Flint)
                .chancedOutput(GEM, Flint, 9000, 0)
                .chancedOutput(GEM, Flint, 8000, 0)
                .chancedOutput(GEM, Flint, 6000, 0)
                .chancedOutput(GEM, Flint, "1/3", 0)
                .chancedOutput(GEM, Flint, 2500, 0)
                .save(provider);

        PACKER_RECIPES.recipeBuilder("matchbox")
                .inputItems(TOOL_MATCHES, 16)
                .inputItems(PLATE, Paper)
                .outputItems(TOOL_MATCHBOX)
                .duration(64)
                .EUt(16)
                .save(provider);

        ROCK_BREAKER_RECIPES.recipeBuilder("cobblestone")
                .notConsumable(Blocks.COBBLESTONE.asItem())
                .outputItems(Blocks.COBBLESTONE.asItem())
                .duration(16)
                .EUt(VA[ULV])
                .addData("fluidA", "minecraft:lava")
                .addData("fluidB", "minecraft:water")
                .save(provider);

        ROCK_BREAKER_RECIPES.recipeBuilder("stone")
                .notConsumable(Blocks.STONE.asItem())
                .outputItems(Blocks.STONE.asItem())
                .duration(16)
                .EUt(VA[ULV])
                .addData("fluidA", "minecraft:lava")
                .addData("fluidB", "minecraft:water")
                .save(provider);

        ROCK_BREAKER_RECIPES.recipeBuilder("andesite")
                .notConsumable(Blocks.ANDESITE.asItem())
                .outputItems(Blocks.ANDESITE.asItem())
                .duration(16)
                .EUt(VHA[MV])
                .addData("fluidA", "minecraft:lava")
                .addData("fluidB", "minecraft:water")
                .save(provider);

        ROCK_BREAKER_RECIPES.recipeBuilder("granite")
                .notConsumable(Blocks.GRANITE.asItem())
                .outputItems(Blocks.GRANITE.asItem())
                .duration(16)
                .EUt(VHA[MV])
                .addData("fluidA", "minecraft:lava")
                .addData("fluidB", "minecraft:water")
                .save(provider);

        ROCK_BREAKER_RECIPES.recipeBuilder("diorite")
                .notConsumable(Blocks.DIORITE.asItem())
                .outputItems(Blocks.DIORITE.asItem())
                .duration(16)
                .EUt(VHA[MV])
                .addData("fluidA", "minecraft:lava")
                .addData("fluidB", "minecraft:water")
                .save(provider);

        ROCK_BREAKER_RECIPES.recipeBuilder("obsidian")
                .notConsumable(DUST, Redstone)
                .outputItems(Blocks.OBSIDIAN.asItem())
                .duration(16)
                .EUt(VHA[HV])
                .addData("fluidA", "minecraft:lava")
                .addData("fluidB", "minecraft:water")
                .save(provider);

        ROCK_BREAKER_RECIPES.recipeBuilder("basalt")
                .notConsumable(Blocks.BASALT.asItem())
                .outputItems(Blocks.BASALT.asItem())
                .duration(16)
                .EUt(VHA[HV])
                .addData("fluidA", "minecraft:lava")
                .addData("fluidB", "minecraft:water")
                .save(provider);

        ROCK_BREAKER_RECIPES.recipeBuilder("blackstone")
                .notConsumable(Blocks.BLACKSTONE.asItem())
                .outputItems(Blocks.BLACKSTONE.asItem())
                .duration(16)
                .EUt(VHA[HV])
                .addData("fluidA", "minecraft:lava")
                .addData("fluidB", "minecraft:water")
                .save(provider);

        ROCK_BREAKER_RECIPES.recipeBuilder("deepslate")
                .notConsumable(Blocks.DEEPSLATE.asItem())
                .outputItems(Blocks.DEEPSLATE.asItem())
                .duration(16)
                .EUt(VHA[EV])
                .addData("fluidA", "minecraft:lava")
                .addData("fluidB", "minecraft:water")
                .save(provider);

        ROCK_BREAKER_RECIPES.recipeBuilder("marble")
                .notConsumable(ROCK, Marble)
                .outputItems(ROCK, Marble)
                .duration(16)
                .EUt(VHA[HV])
                .addData("fluidA", "minecraft:lava")
                .addData("fluidB", "minecraft:water")
                .save(provider);

        ROCK_BREAKER_RECIPES.recipeBuilder("basalt")
                .notConsumable(ROCK, Basalt)
                .outputItems(ROCK, Basalt)
                .duration(16)
                .EUt(VHA[HV])
                .addData("fluidA", "minecraft:lava")
                .addData("fluidB", "minecraft:water")
                .save(provider);

        ROCK_BREAKER_RECIPES.recipeBuilder("red_granite")
                .notConsumable(ROCK, GraniteRed)
                .outputItems(ROCK, GraniteRed)
                .duration(16)
                .EUt(VHA[EV])
                .addData("fluidA", "minecraft:lava")
                .addData("fluidB", "minecraft:water")
                .save(provider);

        // Jetpacks
        ASSEMBLER_RECIPES.recipeBuilder("power_thruster").duration(200).EUt(30)
                .inputItems(ELECTRIC_MOTOR_MV)
                .inputItems(RING, Aluminium, 2)
                .inputItems(ROD, Aluminium)
                .inputItems(ROTOR, Steel)
                .inputItems(CABLE_GT_SINGLE, Copper, 2)
                .outputItems(POWER_THRUSTER)
                .addMaterialInfo(true)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("power_thruster_advanced").duration(200).EUt(30)
                .inputItems(ELECTRIC_MOTOR_HV)
                .inputItems(RING, StainlessSteel, 2)
                .inputItems(ROD, StainlessSteel)
                .inputItems(ROTOR, Chromium)
                .inputItems(CABLE_GT_SINGLE, Gold, 2)
                .outputItems(POWER_THRUSTER_ADVANCED)
                .addMaterialInfo(true)
                .save(provider);

        // QuarkTech Suite
        ASSEMBLER_RECIPES.recipeBuilder("quantum_helmet").duration(1500).EUt(VA[IV])
                .inputItems(CustomTags.LuV_CIRCUITS, 2)
                .inputItems(WIRE_GT_QUADRUPLE, Tungsten, 5)
                .inputItems(ENERGY_LAPOTRONIC_ORB)
                .inputItems(SENSOR_IV)
                .inputItems(FIELD_GENERATOR_IV)
                .inputItems(SCREW, TungstenSteel, 4)
                .inputItems(PLATE, Iridium, 5)
                .inputItems(FOIL, Ruthenium, 20)
                .inputItems(WIRE_FINE, Rhodium, 32)
                .inputFluids(Titanium.getFluid(L * 10))
                .outputItems(QUANTUM_HELMET)
                .addMaterialInfo(true, true)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("quantum_chestplate").duration(1500).EUt(VA[IV])
                .inputItems(CustomTags.LuV_CIRCUITS, 2)
                .inputItems(WIRE_GT_QUADRUPLE, Tungsten, 8)
                .inputItems(ENERGY_LAPOTRONIC_ORB)
                .inputItems(EMITTER_IV.asStack(2))
                .inputItems(FIELD_GENERATOR_IV)
                .inputItems(SCREW, TungstenSteel, 4)
                .inputItems(PLATE, Iridium, 8)
                .inputItems(FOIL, Ruthenium, 32)
                .inputItems(WIRE_FINE, Rhodium, 48)
                .inputFluids(Titanium.getFluid(L * 16))
                .outputItems(QUANTUM_CHESTPLATE)
                .addMaterialInfo(true, true)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("quantum_leggings").duration(1500).EUt(VA[IV])
                .inputItems(CustomTags.LuV_CIRCUITS, 2)
                .inputItems(WIRE_GT_QUADRUPLE, Tungsten, 7)
                .inputItems(ENERGY_LAPOTRONIC_ORB)
                .inputItems(ELECTRIC_MOTOR_IV, 4)
                .inputItems(FIELD_GENERATOR_IV)
                .inputItems(SCREW, TungstenSteel, 4)
                .inputItems(PLATE, Iridium, 7)
                .inputItems(FOIL, Ruthenium, 28)
                .inputItems(WIRE_FINE, Rhodium, 40)
                .inputFluids(Titanium.getFluid(L * 14))
                .outputItems(QUANTUM_LEGGINGS)
                .addMaterialInfo(true, true)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("quantum_boots").duration(1500).EUt(VA[IV])
                .inputItems(CustomTags.LuV_CIRCUITS, 2)
                .inputItems(WIRE_GT_QUADRUPLE, Tungsten, 4)
                .inputItems(ENERGY_LAPOTRONIC_ORB)
                .inputItems(ELECTRIC_PISTON_IV, 2)
                .inputItems(FIELD_GENERATOR_IV)
                .inputItems(SCREW, TungstenSteel, 4)
                .inputItems(PLATE, Iridium, 4)
                .inputItems(FOIL, Ruthenium, 16)
                .inputItems(WIRE_FINE, Rhodium, 16)
                .inputFluids(Titanium.getFluid(L * 8))
                .outputItems(QUANTUM_BOOTS)
                .addMaterialInfo(true, true)
                .save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("quantum_chestplate_advanced").duration(1000).EUt(VA[LuV])
                .inputItems(QUANTUM_CHESTPLATE.asItem())
                .inputItems(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .inputItems(WIRE_FINE, NiobiumTitanium, 64)
                .inputItems(WIRE_GT_QUADRUPLE, Osmium, 6)
                .inputItems(PLATE_DOUBLE, Iridium, 4)
                .inputItems(GRAVITATION_ENGINE, 2)
                .inputItems(CustomTags.ZPM_CIRCUITS)
                .inputItems(PLATE_DENSE, RhodiumPlatedPalladium, 2)
                .inputItems(ENERGY_LAPOTRONIC_ORB_CLUSTER)
                .inputItems(FIELD_GENERATOR_LuV, 2)
                .inputItems(ELECTRIC_MOTOR_LuV, 2)
                .inputItems(SCREW, HSSS, 8)
                .outputItems(QUANTUM_CHESTPLATE_ADVANCED)
                .addMaterialInfo(true, true)
                .save(provider);

        // TODO Central monitor
        /*
         * ASSEMBLER_RECIPES.recipeBuilder("monitor_screen").duration(80).EUt(VA[HV])
         * .inputItems(COVER_SCREEN)
         * .inputItems(CraftingComponent.HULL.getIngredient(1))
         * .inputItems(wireFine, AnnealedCopper, 8)
         * .inputFluids(Polyethylene.getFluid(L))
         * .outputItems(GTMachines.MONITOR_SCREEN)
         * .save(provider);
         * 
         * ASSEMBLER_RECIPES.recipeBuilder("central_monitor").duration(100).EUt(VA[HV])
         * .inputItems(COVER_SCREEN)
         * .inputItems(CraftingComponent.HULL.getIngredient(3))
         * .inputItems(circuit, Tier.HV, 2)
         * .inputFluids(Polyethylene.getFluid(L))
         * .outputItems(GTMachines.CENTRAL_MONITOR)
         * .save(provider);
         */
        /*
         * ASSEMBLER_RECIPES.recipeBuilder("cover_digital_interface").duration(100).EUt(VA[HV])
         * .inputItems(COVER_SCREEN)
         * .inputItems(plate, Aluminium)
         * .inputItems(CustomTags.MV_CIRCUITS)
         * .inputItems(screw, StainlessSteel, 4)
         * .inputFluids(Polyethylene.getFluid(L))
         * .outputItems(COVER_DIGITAL_INTERFACE)
         * .save(provider);
         * \
         * 
         */
        // todo digital interface cover
        /*
         * ASSEMBLER_RECIPES.recipeBuilder("cover_wireless_digital_interface").duration(100).EUt(VA[HV])
         * .inputItems(COVER_DIGITAL_INTERFACE)
         * .inputItems(WIRELESS)
         * .inputFluids(Polyethylene.getFluid(L))
         * .outputItems(COVER_DIGITAL_INTERFACE_WIRELESS)
         * .save(provider);
         * 
         * ASSEMBLER_RECIPES.recipeBuilder("plugin_text").duration(80).EUt(400)
         * .inputItems(COVER_SCREEN)
         * .inputItems(circuit, Tier.LV)
         * .inputItems(wireFine, Copper, 2)
         * .inputFluids(Polyethylene.getFluid(L))
         * .outputItems(PLUGIN_TEXT)
         * .save(provider);
         * 
         * ASSEMBLER_RECIPES.recipeBuilder("plugin_online_pic").duration(80).EUt(400)
         * .inputItems(COVER_SCREEN)
         * .inputItems(circuit, Tier.LV)
         * .inputItems(wireFine, Silver, 2)
         * .inputFluids(Polyethylene.getFluid(L))
         * .outputItems(PLUGIN_ONLINE_PIC)
         * .save(provider);
         * 
         * ASSEMBLER_RECIPES.recipeBuilder("plugin_fake_gui").duration(80).EUt(400)
         * .inputItems(COVER_SCREEN)
         * .inputItems(circuit, Tier.LV)
         * .inputItems(wireFine, Gold, 2)
         * .inputFluids(Polyethylene.getFluid(L))
         * .outputItems(PLUGIN_FAKE_GUI)
         * .save(provider);
         * 
         * ASSEMBLER_RECIPES.recipeBuilder("plugin_advanced_monitor").duration(80).EUt(400)
         * .inputItems(COVER_SCREEN)
         * .inputItems(circuit, Tier.HV)
         * .inputItems(wireFine, Aluminium, 2)
         * .inputFluids(Polyethylene.getFluid(L))
         * .outputItems(PLUGIN_ADVANCED_MONITOR)
         * .save(provider);
         */

        // todo terminal
        /*
         * ASSEMBLER_RECIPES.recipeBuilder("wireless_upgrade").duration(100).EUt(VA[MV])
         * .inputItems(circuit, Tier.MV, 4)
         * .inputItems(EMITTER_MV, 2)
         * .inputItems(SENSOR_MV, 2)
         * .inputItems(plate, StainlessSteel)
         * .inputFluids(Polyethylene.getFluid(L))
         * .outputItems(WIRELESS)
         * .save(provider);
         * 
         * ASSEMBLER_RECIPES.recipeBuilder("camera_upgrade").duration(100).EUt(VA[LV])
         * .inputItems(ELECTRIC_PISTON_LV, 2)
         * .inputItems(EMITTER_LV)
         * .inputItems(lens, Glass)
         * .inputItems(lens, Diamond)
         * .inputItems(circuit, Tier.LV, 4)
         * .inputFluids(SolderingAlloy.getFluid(L))
         * .outputItems(CAMERA)
         * .save(provider);
         */

        // Tempered Glass in Arc Furnace
        ARC_FURNACE_RECIPES.recipeBuilder("tempered_glass").duration(60).EUt(VA[LV])
                .inputItems(BLOCK, Glass)
                .outputItems(GTBlocks.CASING_TEMPERED_GLASS.asStack())
                .save(provider);

        // Dyed Lens Decomposition
        for (ItemEntry<Item> item : GLASS_LENSES.values()) {
            EXTRACTOR_RECIPES.recipeBuilder("extract_" + item.get()).EUt(VA[LV]).duration(15)
                    .inputItems(item)
                    .outputFluids(Glass.getFluid(108))
                    .category(GTRecipeCategories.EXTRACTOR_RECYCLING)
                    .save(provider);

            MACERATOR_RECIPES.recipeBuilder("macerate_" + item.get()).EUt(VA[LV]).duration(15)
                    .inputItems(item)
                    .outputItems(DUST_SMALL, Glass, 3)
                    .category(GTRecipeCategories.MACERATOR_RECYCLING)
                    .save(provider);
        }

        // Glass Plate in Alloy Smelter
        ALLOY_SMELTER_RECIPES.recipeBuilder("glass_plate")
                .inputItems(DUST, Glass, 2)
                .notConsumable(SHAPE_MOLD_PLATE)
                .outputItems(PLATE, Glass)
                .duration(40).EUt(6).save(provider);

        // Dyed Lens Recipes
        GTRecipeBuilder builder = CHEMICAL_BATH_RECIPES.recipeBuilder("").EUt(VA[HV]).duration(200).inputItems(LENS,
                Glass).category(GTRecipeCategories.CHEM_DYES);
        final int dyeAmount = 288;

        // skip white lens
        for (int i = 1; i < CHEMICAL_DYES.length; i++) {
            builder.copy(CHEMICAL_DYES[i].getName() + "_lens").inputFluids(CHEMICAL_DYES[i].getFluid(dyeAmount))
                    .outputItems(GLASS_LENSES.get(Color.VALUES[i]))
                    .save(provider);
        }

        builder.copy("colorless_lens").inputFluids(DyeWhite.getFluid(dyeAmount)).outputItems(LENS, Glass)
                .save(provider);

        // NAN Certificate
        EXTRUDER_RECIPES.recipeBuilder("nan_certificate")
                .inputItems(BLOCK, Neutronium, 64)
                .inputItems(BLOCK, Neutronium, 64)
                .outputItems(NAN_CERTIFICATE)
                .addMaterialInfo(true)
                .duration(Integer.MAX_VALUE).EUt(VA[ULV]).save(provider);

        // Fertilizer
        MIXER_RECIPES.recipeBuilder("fertilizer")
                .inputItems(new ItemStack(Blocks.DIRT))
                .inputItems(DUST, Wood, 2)
                .inputItems(new ItemStack(Blocks.SAND, 4))
                .inputFluids(Water.getFluid(1000))
                .outputItems(FERTILIZER, 4)
                .duration(100).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("fertilizer_c_s").inputItems(DUST, Calcite).inputItems(DUST, Sulfur)
                .inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 2).duration(200).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer_c_t").inputItems(DUST, Calcite).inputItems(DUST, TricalciumPhosphate)
                .inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 3).duration(300).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer_c_p").inputItems(DUST, Calcite).inputItems(DUST, Phosphate)
                .inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 2).duration(200).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer_c_a").inputItems(DUST, Calcite).inputItems(DUST, Ash, 3)
                .inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 1).duration(100).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer_c_d").inputItems(DUST, Calcite).inputItems(DUST, DarkAsh)
                .inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 1).duration(100).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer_ca_s").inputItems(DUST, Calcium).inputItems(DUST, Sulfur)
                .inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 3).duration(300).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer_ca_t").inputItems(DUST, Calcium)
                .inputItems(DUST, TricalciumPhosphate).inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 4)
                .duration(400).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer_ca_p").inputItems(DUST, Calcium).inputItems(DUST, Phosphate)
                .inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 3).duration(300).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer_ca_a").inputItems(DUST, Calcium).inputItems(DUST, Ash, 3)
                .inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 2).duration(200).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer_ca_d").inputItems(DUST, Calcium).inputItems(DUST, DarkAsh)
                .inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 2).duration(200).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer_a_s").inputItems(DUST, Apatite).inputItems(DUST, Sulfur)
                .inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 3).duration(300).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer_a_t").inputItems(DUST, Apatite).inputItems(DUST, TricalciumPhosphate)
                .inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 4).duration(400).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer_a_p").inputItems(DUST, Apatite).inputItems(DUST, Phosphate)
                .inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 3).duration(300).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer_a_a").inputItems(DUST, Apatite).inputItems(DUST, Ash, 3)
                .inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 2).duration(200).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer_a_d").inputItems(DUST, Apatite).inputItems(DUST, DarkAsh)
                .inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 2).duration(200).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer_g_s").inputItems(DUST, GlauconiteSand).inputItems(DUST, Sulfur)
                .inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 3).duration(300).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer_g_t").inputItems(DUST, GlauconiteSand)
                .inputItems(DUST, TricalciumPhosphate).inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 4)
                .duration(400).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer_g_p").inputItems(DUST, GlauconiteSand).inputItems(DUST, Phosphate)
                .inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 3).duration(300).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer_g_a").inputItems(DUST, GlauconiteSand).inputItems(DUST, Ash, 3)
                .inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 2).duration(200).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer_g_d").inputItems(DUST, GlauconiteSand).inputItems(DUST, DarkAsh)
                .inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 2).duration(200).EUt(VA[LV]).save(provider);

        ELECTROLYZER_RECIPES.recipeBuilder("fertilizer_decomposition")
                .inputItems(FERTILIZER)
                .outputItems(DUST, Calcite)
                .outputItems(DUST, Carbon)
                .outputFluids(Water.getFluid(1000))
                .duration(100).EUt(VA[LV]).save(provider);

        if (!ConfigHolder.INSTANCE.recipes.hardMiscRecipes) {
            VanillaRecipeHelper.addShapedRecipe(provider, "flour_to_dough", new ItemStack(DOUGH, 8),
                    "FFF", "FWF", "FFF",
                    'F', ChemicalHelper.get(DUST, Wheat),
                    'W', Water.getBucket());

            MIXER_RECIPES.recipeBuilder("flour_to_dough")
                    .inputItems(DUST, Wheat, 2)
                    .inputFluids(Water.getFluid(250))
                    .outputItems(DOUGH, 3)
                    .EUt(VA[ULV])
                    .duration(200)
                    .save(provider);

            VanillaRecipeHelper.addShapelessRecipe(provider, "pumpkin_pie_from_dough", new ItemStack(Items.PUMPKIN_PIE),
                    new ItemStack(Blocks.PUMPKIN), new ItemStack(Items.SUGAR), new ItemStack(DOUGH));

            VanillaRecipeHelper.addShapelessRecipe(provider, "cookie_from_dough", new ItemStack(Items.COOKIE, 8),
                    new ItemStack(DOUGH), new ItemStack(Items.COCOA_BEANS));

            FORMING_PRESS_RECIPES.recipeBuilder("cookie")
                    .notConsumable(SHAPE_MOLD_CYLINDER)
                    .inputItems(DOUGH)
                    .inputItems(Items.COCOA_BEANS, 2)
                    .outputItems(Items.COOKIE, 12)
                    .EUt(VA[LV])
                    .duration(200)
                    .save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, "cake_from_dough", new ItemStack(Items.CAKE),
                    "MMM", "SES", " D ",
                    'E', Items.EGG,
                    'S', Items.SUGAR,
                    'M', new FluidContainerIngredient(Milk.getFluidTag(), 1000),
                    'D', DOUGH);
        } else {
            VanillaRecipeHelper.addShapedRecipe(provider, "flour_to_dough", new ItemStack(DOUGH, 4),
                    "FFF", "FWF", "FFF",
                    'F', ChemicalHelper.get(DUST, Wheat),
                    'W', Water.getBucket());

            MIXER_RECIPES.recipeBuilder("flour_to_dough")
                    .inputItems(DUST, Wheat, 4)
                    .inputItems(Items.EGG, 2)
                    .inputFluids(Milk.getFluid(250)) // 1 bucket = 1000mB, hence 250mb. Also its infinitely renewable
                    .outputItems(DOUGH, 7)
                    .EUt(VA[ULV])
                    .duration(400)
                    .save(provider);

            VanillaRecipeHelper.addShapelessRecipe(provider, "pumpkin_pie_from_dough", new ItemStack(Items.PUMPKIN_PIE),
                    new ItemStack(Blocks.PUMPKIN), new ItemStack(DOUGH), new ItemStack(Items.SUGAR), 'r', 'k');

            VanillaRecipeHelper.addShapelessRecipe(provider, "cookie", new ItemStack(Items.COOKIE, 4),
                    new ItemStack(Items.COCOA_BEANS), new ItemStack(DOUGH), new ItemStack(Items.SUGAR), 'r');

            FORMING_PRESS_RECIPES.recipeBuilder("cookie")
                    .notConsumable(SHAPE_MOLD_CYLINDER)
                    .inputItems(DOUGH)
                    .inputItems(Items.COCOA_BEANS, 2)
                    .inputItems(Items.SUGAR)
                    .outputItems(Items.COOKIE, 8)
                    .EUt(VA[LV])
                    .duration(200)
                    .save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, "cake", new ItemStack(Items.CAKE),
                    "BBB", "SMS", "DDD",
                    'B', Items.SWEET_BERRIES,
                    'S', Items.SUGAR,
                    'M', new FluidContainerIngredient(Milk.getFluidTag(), 1000),
                    'D', DOUGH);
        }

        FORMING_PRESS_RECIPES.recipeBuilder("pumpkin_pie")
                .notConsumable(SHAPE_MOLD_CYLINDER)
                .inputItems(DOUGH, 2)
                .inputItems(Items.PUMPKIN)
                .inputItems(Items.SUGAR)
                .outputItems(Items.PUMPKIN_PIE, 2)
                .EUt(VA[LV])
                .duration(200)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("chocolate_coin")
                .inputItems(DUST, Cocoa)
                .inputItems(FOIL, Gold)
                .inputItems(DUST, Sugar)
                .inputFluids(Milk.getFluid(500))
                .outputItems(COIN_CHOCOLATE)
                .duration(60).EUt(15)
                .save(provider);

        // XP set to 0.35, similar to vanilla food smelting
        VanillaRecipeHelper.addSmeltingRecipe(provider, "dough_to_bread", CustomTags.DOUGHS, new ItemStack(Items.BREAD),
                0.35f);
        VanillaRecipeHelper.addCampfireRecipe(provider, "dough_to_bread", CustomTags.DOUGHS, new ItemStack(Items.BREAD),
                0.35f);
        VanillaRecipeHelper.addSmokingRecipe(provider, "dough_to_bread", CustomTags.DOUGHS, new ItemStack(Items.BREAD),
                0.35f);

        FORMING_PRESS_RECIPES.recipeBuilder("laminated_glass")
                .inputItems(GTBlocks.CASING_TEMPERED_GLASS.asStack(2))
                .inputItems(PLATE, PolyvinylButyral)
                .outputItems(GTBlocks.CASING_LAMINATED_GLASS.asStack())
                .duration(200).EUt(VA[HV]).save(provider);

        LATHE_RECIPES.recipeBuilder("treated_wood_sticks")
                .inputItems(GTBlocks.TREATED_WOOD_PLANK.asStack())
                .outputItems(ROD, TreatedWood, 2)
                .duration(10).EUt(VA[ULV])
                .save(provider);

        // Coke Brick and Firebrick decomposition
        EXTRACTOR_RECIPES.recipeBuilder("extract_coke_oven_bricks")
                .inputItems(GTBlocks.CASING_COKE_BRICKS.asStack())
                .outputItems(COKE_OVEN_BRICK, 4)
                .duration(300).EUt(2)
                .save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("extract_primitive_bricks")
                .inputItems(GTBlocks.CASING_PRIMITIVE_BRICKS.asStack())
                .outputItems(FIRECLAY_BRICK, 4)
                .duration(300).EUt(2)
                .save(provider);

        // Minecart wheels
        ASSEMBLER_RECIPES.recipeBuilder("iron_minecart_wheels")
                .inputItems(ROD, Iron)
                .inputItems(RING, Iron, 2)
                .outputItems(IRON_MINECART_WHEELS)
                .duration(100).EUt(20)
                .addMaterialInfo(true).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("steel_minecart_wheels")
                .inputItems(ROD, Steel)
                .inputItems(RING, Steel, 2)
                .outputItems(STEEL_MINECART_WHEELS)
                .duration(60).EUt(20).save(provider);

        // Bookshelf Decomposition
        MACERATOR_RECIPES.recipeBuilder("chiseled_bookshelf_recycling")
                .inputItems(Blocks.CHISELED_BOOKSHELF.asItem())
                .outputItems(DUST, Wood, 6)
                .duration(100).EUt(2).save(provider);
    }
}
