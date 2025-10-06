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
import net.minecraft.tags.FluidTags;
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
                "SGS", "PBP", "PWP", 'S', new MaterialEntry(screw, WroughtIron), 'G', Tags.Items.GLASS_PANES, 'B',
                new ItemStack(Items.BOOK),
                'P', new MaterialEntry(plate, WroughtIron), 'W', new MaterialEntry(wireGtSingle, RedAlloy));
        // Machine Memory Card Recipe
        VanillaRecipeHelper.addShapedRecipe(provider, true, "machine_memory_card", MACHINE_MEMORY_CARD.asStack(),
                "PWP", "SLS", "PPP", 'P', new MaterialEntry(plate, Steel), 'W',
                new MaterialEntry(wireGtSingle, Copper), 'S', new MaterialEntry(screw, RedAlloy), 'L',
                CustomTags.LV_CIRCUITS);
        // Potin Recipe
        VanillaRecipeHelper.addShapelessRecipe(provider, "potin_dust", ChemicalHelper.get(dust, Potin, 8),
                new MaterialEntry(dust, Copper),
                new MaterialEntry(dust, Copper),
                new MaterialEntry(dust, Copper),
                new MaterialEntry(dust, Copper),
                new MaterialEntry(dust, Copper),
                new MaterialEntry(dust, Copper),
                new MaterialEntry(dust, Tin),
                new MaterialEntry(dust, Tin),
                new MaterialEntry(dust, Lead));

        MIXER_RECIPES.recipeBuilder("fermented_spider_eye_brown").duration(100).EUt(VA[ULV])
                .inputItems(dust, Sugar)
                .inputItems(new ItemStack(Blocks.BROWN_MUSHROOM))
                .inputItems(new ItemStack(Items.SPIDER_EYE))
                .outputItems(new ItemStack(Items.FERMENTED_SPIDER_EYE))
                .save(provider);

        MIXER_RECIPES.recipeBuilder("fermented_spider_eye_red").duration(100).EUt(VA[ULV])
                .inputItems(dust, Sugar)
                .inputItems(new ItemStack(Blocks.RED_MUSHROOM))
                .inputItems(new ItemStack(Items.SPIDER_EYE))
                .outputItems(new ItemStack(Items.FERMENTED_SPIDER_EYE))
                .save(provider);

        SIFTER_RECIPES.recipeBuilder("gravel_sifting").duration(100).EUt(16)
                .inputItems(new ItemStack(Blocks.GRAVEL))
                .outputItems(gem, Flint)
                .chancedOutput(gem, Flint, 9000, 0)
                .chancedOutput(gem, Flint, 8000, 0)
                .chancedOutput(gem, Flint, 6000, 0)
                .chancedOutput(gem, Flint, "1/3", 0)
                .chancedOutput(gem, Flint, 2500, 0)
                .save(provider);

        PACKER_RECIPES.recipeBuilder("matchbox")
                .inputItems(TOOL_MATCHES, 16)
                .inputItems(plate, Paper)
                .outputItems(TOOL_MATCHBOX)
                .duration(64)
                .EUt(16)
                .save(provider);

        ROCK_BREAKER_RECIPES.recipeBuilder("cobblestone")
                .notConsumable(Blocks.COBBLESTONE.asItem())
                .outputItems(Blocks.COBBLESTONE.asItem())
                .adjacentFluidTag(FluidTags.LAVA, FluidTags.WATER)
                .duration(16)
                .EUt(VA[ULV])
                .save(provider);

        ROCK_BREAKER_RECIPES.recipeBuilder("stone")
                .notConsumable(Blocks.STONE.asItem())
                .outputItems(Blocks.STONE.asItem())
                .adjacentFluidTag(FluidTags.LAVA, FluidTags.WATER)
                .duration(16)
                .EUt(VA[ULV])
                .save(provider);

        ROCK_BREAKER_RECIPES.recipeBuilder("andesite")
                .notConsumable(Blocks.ANDESITE.asItem())
                .outputItems(Blocks.ANDESITE.asItem())
                .adjacentFluidTag(FluidTags.LAVA, FluidTags.WATER)
                .duration(16)
                .EUt(VHA[MV])
                .save(provider);

        ROCK_BREAKER_RECIPES.recipeBuilder("granite")
                .notConsumable(Blocks.GRANITE.asItem())
                .outputItems(Blocks.GRANITE.asItem())
                .adjacentFluidTag(FluidTags.LAVA, FluidTags.WATER)
                .duration(16)
                .EUt(VHA[MV])
                .save(provider);

        ROCK_BREAKER_RECIPES.recipeBuilder("diorite")
                .notConsumable(Blocks.DIORITE.asItem())
                .outputItems(Blocks.DIORITE.asItem())
                .adjacentFluidTag(FluidTags.LAVA, FluidTags.WATER)
                .duration(16)
                .EUt(VHA[MV])
                .save(provider);

        ROCK_BREAKER_RECIPES.recipeBuilder("obsidian")
                .notConsumable(dust, Redstone)
                .outputItems(Blocks.OBSIDIAN.asItem())
                .adjacentFluidTag(FluidTags.LAVA, FluidTags.WATER)
                .duration(16)
                .EUt(VHA[HV])
                .save(provider);

        ROCK_BREAKER_RECIPES.recipeBuilder("basalt")
                .notConsumable(Blocks.BASALT.asItem())
                .outputItems(Blocks.BASALT.asItem())
                .adjacentFluidTag(FluidTags.LAVA, FluidTags.WATER)
                .duration(16)
                .EUt(VHA[HV])
                .save(provider);

        ROCK_BREAKER_RECIPES.recipeBuilder("blackstone")
                .notConsumable(Blocks.BLACKSTONE.asItem())
                .outputItems(Blocks.BLACKSTONE.asItem())
                .adjacentFluidTag(FluidTags.LAVA, FluidTags.WATER)
                .duration(16)
                .EUt(VHA[HV])
                .save(provider);

        ROCK_BREAKER_RECIPES.recipeBuilder("deepslate")
                .notConsumable(Blocks.DEEPSLATE.asItem())
                .outputItems(Blocks.DEEPSLATE.asItem())
                .adjacentFluidTag(FluidTags.LAVA, FluidTags.WATER)
                .duration(16)
                .EUt(VHA[EV])
                .save(provider);

        ROCK_BREAKER_RECIPES.recipeBuilder("marble")
                .notConsumable(rock, Marble)
                .outputItems(rock, Marble)
                .adjacentFluidTag(FluidTags.LAVA, FluidTags.WATER)
                .duration(16)
                .EUt(VHA[HV])
                .save(provider);

        ROCK_BREAKER_RECIPES.recipeBuilder("basalt")
                .notConsumable(rock, Basalt)
                .outputItems(rock, Basalt)
                .adjacentFluidTag(FluidTags.LAVA, FluidTags.WATER)
                .duration(16)
                .EUt(VHA[HV])
                .save(provider);

        ROCK_BREAKER_RECIPES.recipeBuilder("red_granite")
                .notConsumable(rock, GraniteRed)
                .outputItems(rock, GraniteRed)
                .adjacentFluidTag(FluidTags.LAVA, FluidTags.WATER)
                .duration(16)
                .EUt(VHA[EV])
                .save(provider);

        // Jetpacks
        ASSEMBLER_RECIPES.recipeBuilder("power_thruster").duration(200).EUt(30)
                .inputItems(ELECTRIC_MOTOR_MV)
                .inputItems(ring, Aluminium, 2)
                .inputItems(rod, Aluminium)
                .inputItems(rotor, Steel)
                .inputItems(cableGtSingle, Copper, 2)
                .outputItems(POWER_THRUSTER)
                .addMaterialInfo(true)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("power_thruster_advanced").duration(200).EUt(30)
                .inputItems(ELECTRIC_MOTOR_HV)
                .inputItems(ring, StainlessSteel, 2)
                .inputItems(rod, StainlessSteel)
                .inputItems(rotor, Chromium)
                .inputItems(cableGtSingle, Gold, 2)
                .outputItems(POWER_THRUSTER_ADVANCED)
                .addMaterialInfo(true)
                .save(provider);

        // QuarkTech Suite
        ASSEMBLER_RECIPES.recipeBuilder("quantum_helmet").duration(1500).EUt(VA[IV])
                .inputItems(CustomTags.LuV_CIRCUITS, 2)
                .inputItems(wireGtQuadruple, Tungsten, 5)
                .inputItems(ENERGY_LAPOTRONIC_ORB)
                .inputItems(SENSOR_IV)
                .inputItems(FIELD_GENERATOR_IV)
                .inputItems(screw, TungstenSteel, 4)
                .inputItems(plate, Iridium, 5)
                .inputItems(foil, Ruthenium, 20)
                .inputItems(wireFine, Rhodium, 32)
                .inputFluids(Titanium.getFluid(L * 10))
                .outputItems(QUANTUM_HELMET)
                .addMaterialInfo(true, true)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("quantum_chestplate").duration(1500).EUt(VA[IV])
                .inputItems(CustomTags.LuV_CIRCUITS, 2)
                .inputItems(wireGtQuadruple, Tungsten, 8)
                .inputItems(ENERGY_LAPOTRONIC_ORB)
                .inputItems(EMITTER_IV.asStack(2))
                .inputItems(FIELD_GENERATOR_IV)
                .inputItems(screw, TungstenSteel, 4)
                .inputItems(plate, Iridium, 8)
                .inputItems(foil, Ruthenium, 32)
                .inputItems(wireFine, Rhodium, 48)
                .inputFluids(Titanium.getFluid(L * 16))
                .outputItems(QUANTUM_CHESTPLATE)
                .addMaterialInfo(true, true)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("quantum_leggings").duration(1500).EUt(VA[IV])
                .inputItems(CustomTags.LuV_CIRCUITS, 2)
                .inputItems(wireGtQuadruple, Tungsten, 7)
                .inputItems(ENERGY_LAPOTRONIC_ORB)
                .inputItems(ELECTRIC_MOTOR_IV, 4)
                .inputItems(FIELD_GENERATOR_IV)
                .inputItems(screw, TungstenSteel, 4)
                .inputItems(plate, Iridium, 7)
                .inputItems(foil, Ruthenium, 28)
                .inputItems(wireFine, Rhodium, 40)
                .inputFluids(Titanium.getFluid(L * 14))
                .outputItems(QUANTUM_LEGGINGS)
                .addMaterialInfo(true, true)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("quantum_boots").duration(1500).EUt(VA[IV])
                .inputItems(CustomTags.LuV_CIRCUITS, 2)
                .inputItems(wireGtQuadruple, Tungsten, 4)
                .inputItems(ENERGY_LAPOTRONIC_ORB)
                .inputItems(ELECTRIC_PISTON_IV, 2)
                .inputItems(FIELD_GENERATOR_IV)
                .inputItems(screw, TungstenSteel, 4)
                .inputItems(plate, Iridium, 4)
                .inputItems(foil, Ruthenium, 16)
                .inputItems(wireFine, Rhodium, 16)
                .inputFluids(Titanium.getFluid(L * 8))
                .outputItems(QUANTUM_BOOTS)
                .addMaterialInfo(true, true)
                .save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("quantum_chestplate_advanced").duration(1000).EUt(VA[LuV])
                .inputItems(QUANTUM_CHESTPLATE.asItem())
                .inputItems(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .inputItems(wireFine, NiobiumTitanium, 64)
                .inputItems(wireGtQuadruple, Osmium, 6)
                .inputItems(plateDouble, Iridium, 4)
                .inputItems(GRAVITATION_ENGINE, 2)
                .inputItems(CustomTags.ZPM_CIRCUITS)
                .inputItems(plateDense, RhodiumPlatedPalladium, 2)
                .inputItems(ENERGY_LAPOTRONIC_ORB_CLUSTER)
                .inputItems(FIELD_GENERATOR_LuV, 2)
                .inputItems(ELECTRIC_MOTOR_LuV, 2)
                .inputItems(screw, HSSS, 8)
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
                .inputItems(block, Glass)
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
                    .outputItems(dustSmall, Glass, 3)
                    .category(GTRecipeCategories.MACERATOR_RECYCLING)
                    .save(provider);
        }

        // Glass Plate in Alloy Smelter
        ALLOY_SMELTER_RECIPES.recipeBuilder("glass_plate")
                .inputItems(dust, Glass, 2)
                .notConsumable(SHAPE_MOLD_PLATE)
                .outputItems(plate, Glass)
                .duration(40).EUt(6).save(provider);

        // Dyed Lens Recipes
        GTRecipeBuilder builder = CHEMICAL_BATH_RECIPES.recipeBuilder("").EUt(VA[HV]).duration(200).inputItems(lens,
                Glass).category(GTRecipeCategories.CHEM_DYES);
        final int dyeAmount = 288;

        // skip white lens
        for (int i = 1; i < CHEMICAL_DYES.length; i++) {
            builder.copy(CHEMICAL_DYES[i].getName() + "_lens").inputFluids(CHEMICAL_DYES[i].getFluid(dyeAmount))
                    .outputItems(GLASS_LENSES.get(Color.VALUES[i]))
                    .save(provider);
        }

        builder.copy("colorless_lens").inputFluids(DyeWhite.getFluid(dyeAmount)).outputItems(lens, Glass)
                .save(provider);
        VanillaRecipeHelper.addShapelessRecipe(provider, "brown_dye_from_metal_mixture", new ItemStack(Items.BROWN_DYE),
                new MaterialEntry(dust, MetalMixture));

        // NAN Certificate
        EXTRUDER_RECIPES.recipeBuilder("nan_certificate")
                .inputItems(block, Neutronium, 64)
                .inputItems(block, Neutronium, 64)
                .outputItems(NAN_CERTIFICATE)
                .addMaterialInfo(true)
                .duration(Integer.MAX_VALUE).EUt(VA[ULV]).save(provider);

        // Fertilizer
        MIXER_RECIPES.recipeBuilder("fertilizer")
                .inputItems(new ItemStack(Blocks.DIRT))
                .inputItems(dust, Wood, 2)
                .inputItems(new ItemStack(Blocks.SAND, 4))
                .inputFluids(Water.getFluid(1000))
                .outputItems(FERTILIZER, 4)
                .duration(100).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("fertilizer_c_s").inputItems(dust, Calcite).inputItems(dust, Sulfur)
                .inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 2).duration(200).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer_c_t").inputItems(dust, Calcite).inputItems(dust, TricalciumPhosphate)
                .inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 3).duration(300).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer_c_p").inputItems(dust, Calcite).inputItems(dust, Phosphate)
                .inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 2).duration(200).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer_c_a").inputItems(dust, Calcite).inputItems(dust, Ash, 3)
                .inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 1).duration(100).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer_c_d").inputItems(dust, Calcite).inputItems(dust, DarkAsh)
                .inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 1).duration(100).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer_ca_s").inputItems(dust, Calcium).inputItems(dust, Sulfur)
                .inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 3).duration(300).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer_ca_t").inputItems(dust, Calcium)
                .inputItems(dust, TricalciumPhosphate).inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 4)
                .duration(400).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer_ca_p").inputItems(dust, Calcium).inputItems(dust, Phosphate)
                .inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 3).duration(300).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer_ca_a").inputItems(dust, Calcium).inputItems(dust, Ash, 3)
                .inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 2).duration(200).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer_ca_d").inputItems(dust, Calcium).inputItems(dust, DarkAsh)
                .inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 2).duration(200).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer_a_s").inputItems(dust, Apatite).inputItems(dust, Sulfur)
                .inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 3).duration(300).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer_a_t").inputItems(dust, Apatite).inputItems(dust, TricalciumPhosphate)
                .inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 4).duration(400).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer_a_p").inputItems(dust, Apatite).inputItems(dust, Phosphate)
                .inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 3).duration(300).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer_a_a").inputItems(dust, Apatite).inputItems(dust, Ash, 3)
                .inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 2).duration(200).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer_a_d").inputItems(dust, Apatite).inputItems(dust, DarkAsh)
                .inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 2).duration(200).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer_g_s").inputItems(dust, GlauconiteSand).inputItems(dust, Sulfur)
                .inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 3).duration(300).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer_g_t").inputItems(dust, GlauconiteSand)
                .inputItems(dust, TricalciumPhosphate).inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 4)
                .duration(400).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer_g_p").inputItems(dust, GlauconiteSand).inputItems(dust, Phosphate)
                .inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 3).duration(300).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer_g_a").inputItems(dust, GlauconiteSand).inputItems(dust, Ash, 3)
                .inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 2).duration(200).EUt(VA[LV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("fertilizer_g_d").inputItems(dust, GlauconiteSand).inputItems(dust, DarkAsh)
                .inputFluids(Water.getFluid(1000)).outputItems(FERTILIZER, 2).duration(200).EUt(VA[LV]).save(provider);

        ELECTROLYZER_RECIPES.recipeBuilder("fertilizer_decomposition")
                .inputItems(FERTILIZER)
                .outputItems(dust, Calcite)
                .outputItems(dust, Carbon)
                .outputFluids(Water.getFluid(1000))
                .duration(100).EUt(VA[LV]).save(provider);

        if (!ConfigHolder.INSTANCE.recipes.hardMiscRecipes) {
            VanillaRecipeHelper.addShapedRecipe(provider, "flour_to_dough", new ItemStack(DOUGH, 8),
                    "FFF", "FWF", "FFF",
                    'F', CustomTags.WHEAT_GRAINS,
                    'W', Water.getBucket());

            MIXER_RECIPES.recipeBuilder("flour_to_dough")
                    .inputItems(CustomTags.WHEAT_GRAINS, 2)
                    .inputFluids(Water.getFluid(250))
                    .outputItems(DOUGH, 3)
                    .EUt(VA[ULV])
                    .duration(200)
                    .save(provider);

            VanillaRecipeHelper.addShapelessRecipe(provider, "pumpkin_pie_from_dough", new ItemStack(Items.PUMPKIN_PIE),
                    new ItemStack(Blocks.PUMPKIN), new ItemStack(Items.SUGAR), CustomTags.DOUGHS);

            VanillaRecipeHelper.addShapelessRecipe(provider, "cookie_from_dough", new ItemStack(Items.COOKIE, 8),
                    CustomTags.DOUGHS, new ItemStack(Items.COCOA_BEANS));

            FORMING_PRESS_RECIPES.recipeBuilder("cookie")
                    .notConsumable(SHAPE_MOLD_CYLINDER)
                    .inputItems(CustomTags.DOUGHS)
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
                    'D', CustomTags.DOUGHS);
        } else {
            VanillaRecipeHelper.addShapedRecipe(provider, "flour_to_dough", new ItemStack(DOUGH, 4),
                    "FFF", "FWF", "FFF",
                    'F', CustomTags.WHEAT_GRAINS,
                    'W', Water.getBucket());

            MIXER_RECIPES.recipeBuilder("flour_to_dough")
                    .inputItems(CustomTags.WHEAT_GRAINS, 4)
                    .inputItems(Items.EGG, 2)
                    .inputFluids(Milk.getFluid(250)) // 1 bucket = 1000mB, hence 250mb. Also its infinitely renewable
                    .outputItems(DOUGH, 7)
                    .EUt(VA[ULV])
                    .duration(400)
                    .save(provider);

            VanillaRecipeHelper.addShapelessRecipe(provider, "pumpkin_pie_from_dough", new ItemStack(Items.PUMPKIN_PIE),
                    new ItemStack(Blocks.PUMPKIN), CustomTags.DOUGHS, new ItemStack(Items.SUGAR), 'r', 'k');

            VanillaRecipeHelper.addShapelessRecipe(provider, "cookie", new ItemStack(Items.COOKIE, 4),
                    new ItemStack(Items.COCOA_BEANS), CustomTags.DOUGHS, new ItemStack(Items.SUGAR), 'r');

            FORMING_PRESS_RECIPES.recipeBuilder("cookie")
                    .notConsumable(SHAPE_MOLD_CYLINDER)
                    .inputItems(CustomTags.DOUGHS)
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
                    'D', CustomTags.DOUGHS);
        }

        FORMING_PRESS_RECIPES.recipeBuilder("pumpkin_pie")
                .notConsumable(SHAPE_MOLD_CYLINDER)
                .inputItems(CustomTags.DOUGHS, 2)
                .inputItems(Items.PUMPKIN)
                .inputItems(Items.SUGAR)
                .outputItems(Items.PUMPKIN_PIE, 2)
                .EUt(VA[LV])
                .duration(200)
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
                .inputItems(plate, PolyvinylButyral)
                .outputItems(GTBlocks.CASING_LAMINATED_GLASS.asStack())
                .duration(200).EUt(VA[HV]).save(provider);

        LATHE_RECIPES.recipeBuilder("treated_wood_sticks")
                .inputItems(GTBlocks.TREATED_WOOD_PLANK.asStack())
                .outputItems(rod, TreatedWood, 2)
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
                .inputItems(rod, Iron)
                .inputItems(ring, Iron, 2)
                .outputItems(IRON_MINECART_WHEELS)
                .duration(100).EUt(20)
                .addMaterialInfo(true).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("steel_minecart_wheels")
                .inputItems(rod, Steel)
                .inputItems(ring, Steel, 2)
                .outputItems(STEEL_MINECART_WHEELS)
                .duration(60).EUt(20).save(provider);

        // Bookshelf Decomposition
        MACERATOR_RECIPES.recipeBuilder("chiseled_bookshelf_recycling")
                .inputItems(Blocks.CHISELED_BOOKSHELF.asItem())
                .outputItems(dust, Wood, 6)
                .duration(100).EUt(2).save(provider);
    }
}
