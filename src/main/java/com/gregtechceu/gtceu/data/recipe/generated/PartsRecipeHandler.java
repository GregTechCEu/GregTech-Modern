package com.gregtechceu.gtceu.data.recipe.generated;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.*;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeCategories;
import com.gregtechceu.gtceu.common.item.TurbineRotorBehaviour;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public final class PartsRecipeHandler {

    private PartsRecipeHandler() {}

    public static void run(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        processRod(provider, material);
        processLongRod(provider, material);
        processPlate(provider, material);
        processPlateDouble(provider, material);
        processPlateDense(provider, material);
        processTurbine(provider, material);
        processRotor(provider, material);
        processBolt(provider, material);
        processScrew(provider, material);
        processFineWire(provider, material);
        processFoil(provider, material);
        processLens(provider, material);
        processGear(provider, gear, material);
        processGear(provider, gearSmall, material);
        processRing(provider, material);
        processSpring(provider, spring, material);
        processSpring(provider, springSmall, material);
        processRound(provider, material);
    }

    private static void processBolt(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(bolt) || !material.hasProperty(PropertyKey.DUST)) {
            return;
        }

        ItemStack boltStack = ChemicalHelper.get(bolt,
                material.hasFlag(IS_MAGNETIC) && material.hasProperty(PropertyKey.INGOT) ?
                        material.getProperty(PropertyKey.INGOT).getMacerateInto() : material);
        ItemStack ingotStack = ChemicalHelper.get(ingot, material);

        CUTTER_RECIPES.recipeBuilder("cut_" + material.getName() + "_screw_to_bolt")
                .inputItems(screw, material)
                .outputItems(boltStack)
                .duration(20)
                .EUt(24)
                .save(provider);

        if (!boltStack.isEmpty() && !ingotStack.isEmpty()) {
            EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_ingot_to_bolt")
                    .inputItems(ingot, material)
                    .notConsumable(GTItems.SHAPE_EXTRUDER_BOLT)
                    .outputItems(boltStack.copyWithCount(8))
                    .duration(15)
                    .EUt(VA[MV])
                    .save(provider);

            if (material.hasFlag(NO_SMASHING)) {
                EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_dust_to_bolt")
                        .inputItems(dust, material)
                        .notConsumable(GTItems.SHAPE_EXTRUDER_BOLT)
                        .outputItems(boltStack.copyWithCount(8))
                        .duration(15)
                        .EUt(VA[MV])
                        .save(provider);
            }
        }
    }

    private static void processScrew(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(screw) || !material.hasProperty(PropertyKey.DUST)) {
            return;
        }

        ItemStack screwStack = ChemicalHelper.get(screw,
                material.hasFlag(IS_MAGNETIC) && material.hasProperty(PropertyKey.INGOT) ?
                        material.getProperty(PropertyKey.INGOT).getMacerateInto() : material);

        LATHE_RECIPES.recipeBuilder("lathe_" + material.getName() + "_bolt_to_screw")
                .inputItems(bolt, material)
                .outputItems(screwStack)
                .duration((int) Math.max(1, material.getMass() / 8L))
                .EUt(4)
                .save(provider);

        VanillaRecipeHelper.addShapedRecipe(provider, String.format("screw_%s", material.getName()),
                screwStack, "fX", "X ",
                'X', new MaterialEntry(bolt, material));
    }

    private static void processFoil(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(foil) || !material.hasProperty(PropertyKey.INGOT)) {
            return;
        }

        var magMaterial = material.hasFlag(IS_MAGNETIC) ?
                material.getProperty(PropertyKey.INGOT).getMacerateInto() : material;
        if (!material.hasFlag(NO_SMASHING))
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("foil_%s", material.getName()),
                    ChemicalHelper.get(foil, material, 2),
                    "hP ", 'P', new MaterialEntry(plate, magMaterial));

        BENDER_RECIPES.recipeBuilder("bend_" + material.getName() + "_plate_to_foil")
                .inputItems(plate, material)
                .outputItems(foil, magMaterial, 4)
                .duration((int) material.getMass())
                .EUt(24)
                .circuitMeta(1)
                .save(provider);

        BENDER_RECIPES.recipeBuilder("bend_" + material.getName() + "_ingot_to_foil")
                .inputItems(ingot, material)
                .outputItems(foil, magMaterial, 4)
                .duration((int) material.getMass())
                .EUt(24)
                .circuitMeta(10)
                .save(provider);

        if (material.hasFlag(NO_SMASHING)) {
            EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_ingot_to_foil")
                    .inputItems(ingot, material)
                    .notConsumable(GTItems.SHAPE_EXTRUDER_FOIL)
                    .outputItems(foil, magMaterial, 4)
                    .duration((int) material.getMass())
                    .EUt(24)
                    .save(provider);

            EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_dust_to_foil")
                    .inputItems(dust, material)
                    .notConsumable(GTItems.SHAPE_EXTRUDER_FOIL)
                    .outputItems(foil, magMaterial, 4)
                    .duration((int) material.getMass())
                    .EUt(24)
                    .save(provider);
        }
    }

    private static void processFineWire(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(wireFine) || !material.hasProperty(PropertyKey.INGOT)) {
            return;
        }

        ItemStack fineWireStack = ChemicalHelper.get(wireFine, material.hasFlag(IS_MAGNETIC) ?
                material.getProperty(PropertyKey.INGOT).getMacerateInto() : material);

        if (!ChemicalHelper.get(foil, material).isEmpty())
            VanillaRecipeHelper.addShapelessRecipe(provider, String.format("fine_wire_%s", material.getName()),
                    fineWireStack, 'x', new MaterialEntry(foil, material));

        if (material.hasProperty(PropertyKey.WIRE)) {
            WIREMILL_RECIPES.recipeBuilder("mill_" + material.getName() + "_wire_to_fine_wire")
                    .inputItems(wireGtSingle, material)
                    .outputItems(fineWireStack.copyWithCount(4))
                    .duration((int) material.getMass() * 3 / 2)
                    .EUt(VA[ULV])
                    .save(provider);
        } else {
            WIREMILL_RECIPES.recipeBuilder("mill_" + material.getName() + "ingot_to_fine_wire")
                    .inputItems(ingot, material)
                    .outputItems(fineWireStack.copyWithCount(8))
                    .duration((int) material.getMass() * 3)
                    .EUt(VA[ULV])
                    .save(provider);
        }
    }

    private static void processGear(@NotNull Consumer<FinishedRecipe> provider, @NotNull TagPrefix prefix,
                                    @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(prefix) || !material.hasProperty(PropertyKey.DUST)) {
            return;
        }

        boolean isSmall = prefix == gearSmall;
        ItemStack stack = ChemicalHelper.get(prefix,
                material.hasFlag(IS_MAGNETIC) && material.hasProperty(PropertyKey.INGOT) ?
                        material.getProperty(PropertyKey.INGOT).getMacerateInto() : material);
        if (!isSmall && material.hasProperty(PropertyKey.INGOT)) {
            int voltageMultiplier = getVoltageMultiplier(material);
            EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_ingot_to_gear")
                    .inputItems(ingot, material, 4)
                    .notConsumable(GTItems.SHAPE_EXTRUDER_GEAR)
                    .outputItems(stack)
                    .duration((int) material.getMass() * 5)
                    .EUt(8L * voltageMultiplier)
                    .save(provider);

            ALLOY_SMELTER_RECIPES.recipeBuilder("alloy_smelt_" + material.getName() + "_ingot_to_gear")
                    .inputItems(ingot, material, 8)
                    .notConsumable(GTItems.SHAPE_MOLD_GEAR)
                    .outputItems(stack)
                    .duration((int) material.getMass() * 10)
                    .EUt(2L * voltageMultiplier)
                    .category(GTRecipeCategories.INGOT_MOLDING)
                    .save(provider);

            if (material.hasFlag(NO_SMASHING)) {
                EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_dust_to_gear")
                        .inputItems(dust, material, 4)
                        .notConsumable(GTItems.SHAPE_EXTRUDER_GEAR)
                        .outputItems(stack)
                        .duration((int) material.getMass() * 5)
                        .EUt(8L * voltageMultiplier)
                        .save(provider);
            }
        }

        if (material.hasFluid()) {
            FluidStack fluidStack = material.getProperty(PropertyKey.FLUID).solidifiesFrom(L * (isSmall ? 1 : 4));
            if (!fluidStack.isEmpty()) {
                FLUID_SOLIDFICATION_RECIPES.recipeBuilder("solidify_" + material.getName() + "_" + prefix.name)
                        .notConsumable(isSmall ? GTItems.SHAPE_MOLD_GEAR_SMALL : GTItems.SHAPE_MOLD_GEAR)
                        .inputFluids(fluidStack)
                        .outputItems(stack)
                        .duration(isSmall ? 20 : 100)
                        .EUt(VA[ULV])
                        .save(provider);
            }
        }

        if (material.hasFlag(GENERATE_PLATE) && material.hasFlag(GENERATE_ROD)) {
            if (isSmall) {
                VanillaRecipeHelper.addShapedRecipe(provider, String.format("small_gear_%s", material.getName()),
                        stack,
                        " R ", "hPx", " R ", 'R', new MaterialEntry(rod, material), 'P',
                        new MaterialEntry(plate, material));

                EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_ingot_to_small_gear")
                        .inputItems(ingot, material)
                        .notConsumable(GTItems.SHAPE_EXTRUDER_GEAR_SMALL)
                        .outputItems(stack)
                        .duration((int) material.getMass())
                        .EUt(material.getBlastTemperature() >= 2800 ? 256 : 64)
                        .save(provider);

                ALLOY_SMELTER_RECIPES.recipeBuilder("alloy_smelt_" + material.getName() + "_ingot_to_small_gear")
                        .duration((int) material.getMass()).EUt(VA[LV])
                        .inputItems(ingot, material, 2)
                        .notConsumable(GTItems.SHAPE_MOLD_GEAR_SMALL)
                        .outputItems(stack)
                        .category(GTRecipeCategories.INGOT_MOLDING)
                        .save(provider);

                if (material.hasFlag(NO_SMASHING)) {
                    EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_dust_to_small_gear")
                            .inputItems(dust, material)
                            .notConsumable(GTItems.SHAPE_EXTRUDER_GEAR_SMALL)
                            .outputItems(stack)
                            .duration((int) material.getMass())
                            .EUt(material.getBlastTemperature() >= 2800 ? 256 : 64)
                            .save(provider);
                }
            } else {
                VanillaRecipeHelper.addShapedRecipe(provider, String.format("gear_%s", material.getName()), stack,
                        "RPR", "PwP", "RPR",
                        'P', new MaterialEntry(plate, material),
                        'R', new MaterialEntry(rod, material));
            }
        }
    }

    private static void processLens(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(lens) || !material.hasProperty(PropertyKey.GEM)) {
            return;
        }

        LATHE_RECIPES.recipeBuilder("lathe_" + material.getName() + "_plate_to_lens")
                .inputItems(plate, material)
                .outputItems(lens, material)
                .outputItems(dustSmall, material)
                .duration(1200).EUt(120).save(provider);

        if (!ChemicalHelper.get(gemExquisite, material).isEmpty()) {
            LATHE_RECIPES.recipeBuilder("lathe_" + material.getName() + "_gem_to_lens")
                    .inputItems(gemExquisite, material)
                    .outputItems(lens, material)
                    .outputItems(dust, material, 2)
                    .duration(2400).EUt(30).save(provider);
        }
    }

    private static void processPlate(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(plate) || !material.hasProperty(PropertyKey.DUST)) {
            return;
        }

        if (material.hasFluid()) {
            FluidStack stack = material.getProperty(PropertyKey.FLUID).solidifiesFrom(L);
            if (!stack.isEmpty()) {
                FLUID_SOLIDFICATION_RECIPES.recipeBuilder("solidify_" + material.getName() + "_to_plate")
                        .notConsumable(GTItems.SHAPE_MOLD_PLATE)
                        .inputFluids(stack)
                        .outputItems(plate, material)
                        .duration(40)
                        .EUt(VA[ULV])
                        .save(provider);
            }
        }
    }

    private static void processPlateDouble(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(plateDouble) || !material.hasProperty(PropertyKey.INGOT)) {
            return;
        }

        var magMaterial = material.hasFlag(IS_MAGNETIC) ?
                material.getProperty(PropertyKey.INGOT).getMacerateInto() : material;
        if (material.hasFlag(GENERATE_PLATE)) {
            if (!material.hasFlag(NO_SMASHING)) {
                VanillaRecipeHelper.addShapedRecipe(provider, String.format("plate_double_%s", material.getName()),
                        ChemicalHelper.get(plateDouble, magMaterial),
                        "h", "P", "P", 'P', new MaterialEntry(plate, material));
            }

            BENDER_RECIPES.recipeBuilder("bend_" + material.getName() + "_plate_to_double_plate")
                    .EUt(96).duration((int) material.getMass() * 2)
                    .inputItems(plate, material, 2)
                    .outputItems(plateDouble, magMaterial)
                    .circuitMeta(2)
                    .save(provider);

            BENDER_RECIPES.recipeBuilder("bend_" + material.getName() + "_ingot_to_double_plate")
                    .inputItems(ingot, material, 2)
                    .circuitMeta(2)
                    .outputItems(plateDouble, magMaterial)
                    .duration((int) material.getMass() * 2)
                    .EUt(96)
                    .save(provider);
        }
    }

    private static void processPlateDense(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(plateDense) || !material.hasProperty(PropertyKey.DUST)) {
            return;
        }

        var magMaterial = material.hasFlag(IS_MAGNETIC) && material.hasProperty(PropertyKey.INGOT) ?
                material.getProperty(PropertyKey.INGOT).getMacerateInto() : material;
        BENDER_RECIPES.recipeBuilder("bend_" + material.getName() + "_plate_to_dense_plate")
                .inputItems(plate, material, 9)
                .circuitMeta(9)
                .outputItems(plateDense, magMaterial)
                .duration((int) Math.max(material.getMass() * 9L, 1L))
                .EUt(96)
                .save(provider);

        if (material.hasProperty(PropertyKey.INGOT)) {
            BENDER_RECIPES.recipeBuilder("bend_" + material.getName() + "_ingot_to_dense_plate")
                    .inputItems(ingot, material, 9)
                    .circuitMeta(9)
                    .outputItems(plateDense, magMaterial)
                    .duration((int) Math.max(material.getMass() * 9L, 1L))
                    .EUt(96)
                    .save(provider);
        }
    }

    private static void processRing(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(ring) || !material.hasProperty(PropertyKey.INGOT)) {
            return;
        }

        EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_ingot_to_ring")
                .inputItems(ingot, material)
                .notConsumable(GTItems.SHAPE_EXTRUDER_RING)
                .outputItems(ring, material, 4)
                .duration((int) material.getMass() * 2)
                .EUt(6L * getVoltageMultiplier(material))
                .save(provider);

        if (!material.hasFlag(NO_SMASHING)) {
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("ring_%s", material.getName()),
                    ChemicalHelper.get(ring, material),
                    "h ", "fX",
                    'X', new MaterialEntry(rod, material));
        } else {
            EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_dust_to_ring")
                    .inputItems(dust, material)
                    .notConsumable(GTItems.SHAPE_EXTRUDER_RING)
                    .outputItems(ring, material, 4)
                    .duration((int) material.getMass() * 2)
                    .EUt(6L * getVoltageMultiplier(material))
                    .save(provider);
        }
    }

    private static void processSpring(@NotNull Consumer<FinishedRecipe> provider, @NotNull TagPrefix prefix,
                                      @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(prefix) || !material.hasProperty(PropertyKey.INGOT)) {
            return;
        }

        boolean isSmall = prefix == springSmall;
        if (isSmall) {
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("spring_small_%s", material.getName()),
                    ChemicalHelper.get(springSmall, material),
                    " s ", "fRx", 'R', new MaterialEntry(rod, material));

            BENDER_RECIPES.recipeBuilder("bend_" + material.getName() + "_rod_to_small_spring")
                    .duration((int) (material.getMass() / 2)).EUt(VA[ULV])
                    .inputItems(rod, material)
                    .outputItems(springSmall, material, 2)
                    .circuitMeta(1)
                    .save(provider);
        } else {
            BENDER_RECIPES.recipeBuilder("bend_" + material.getName() + "_long_rod_to_spring")
                    .inputItems(rodLong, material)
                    .outputItems(spring, material)
                    .circuitMeta(1)
                    .duration(200)
                    .EUt(16)
                    .save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, String.format("spring_%s", material.getName()),
                    ChemicalHelper.get(spring, material),
                    " s ", "fRx", " R ", 'R', new MaterialEntry(rodLong, material));
        }
    }

    private static void processRotor(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(rotor) || !material.hasProperty(PropertyKey.INGOT)) {
            return;
        }

        ItemStack stack = ChemicalHelper.get(rotor, material);
        VanillaRecipeHelper.addShapedRecipe(provider, String.format("rotor_%s", material.getName()), stack,
                "ChC", "SRf", "CdC",
                'C', new MaterialEntry(plate, material),
                'S', new MaterialEntry(screw, material),
                'R', new MaterialEntry(ring, material));

        if (material.hasFluid()) {
            FluidStack fluidStack = material.getProperty(PropertyKey.FLUID).solidifiesFrom(L * 4);
            if (!fluidStack.isEmpty()) {
                FLUID_SOLIDFICATION_RECIPES.recipeBuilder("solidify_" + material.getName() + "_to_rotor")
                        .notConsumable(GTItems.SHAPE_MOLD_ROTOR)
                        .inputFluids(fluidStack)
                        .outputItems(stack.copy())
                        .duration(120)
                        .EUt(20)
                        .save(provider);
            }
        }

        EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_ingot_to_rotor")
                .inputItems(ingot, material, 4)
                .notConsumable(GTItems.SHAPE_EXTRUDER_ROTOR)
                .outputItems(stack.copy())
                .duration((int) material.getMass() * 4)
                .EUt(material.getBlastTemperature() >= 2800 ? 256 : 64)
                .save(provider);

        if (material.hasFlag(NO_SMASHING)) {
            EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_dust_to_rotor")
                    .inputItems(dust, material, 4)
                    .notConsumable(GTItems.SHAPE_EXTRUDER_ROTOR)
                    .outputItems(stack.copy())
                    .duration((int) material.getMass() * 4)
                    .EUt(material.getBlastTemperature() >= 2800 ? 256 : 64)
                    .save(provider);
        }
    }

    private static void processRod(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(rod) || !material.hasProperty(PropertyKey.DUST)) {
            return;
        }

        if (material.hasProperty(PropertyKey.GEM) || material.hasProperty(PropertyKey.INGOT)) {
            GTRecipeBuilder builder = LATHE_RECIPES.recipeBuilder("lathe_" + material.getName() + "_to_rod")
                    .inputItems(material.hasProperty(PropertyKey.GEM) ? gem : ingot, material)
                    .duration((int) Math.max(material.getMass() * 2, 1))
                    .EUt(16);

            var materialOutput = material.hasFlag(IS_MAGNETIC) && material.hasProperty(PropertyKey.INGOT) ?
                    material.getProperty(PropertyKey.INGOT).getMacerateInto() : material;
            if (ConfigHolder.INSTANCE.recipes.harderRods) {
                builder.outputItems(rod, materialOutput);
                builder.outputItems(dustSmall, materialOutput, 2);
            } else {
                builder.outputItems(rod, materialOutput, 2);
            }
            builder.save(provider);
        }

        if (material.hasFlag(GENERATE_BOLT_SCREW)) {
            ItemStack boltStack = ChemicalHelper.get(bolt, material.hasFlag(IS_MAGNETIC) ?
                    material.getProperty(PropertyKey.INGOT).getMacerateInto() : material);
            CUTTER_RECIPES.recipeBuilder("cut_" + material.getName() + "_rod_to_bolt")
                    .inputItems(rod, material)
                    .outputItems(boltStack.copyWithCount(4))
                    .duration((int) Math.max(material.getMass() * 2L, 1L))
                    .EUt(4)
                    .save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, String.format("bolt_saw_%s", material.getName()),
                    boltStack.copyWithCount(2),
                    "s ", " X",
                    'X', new MaterialEntry(rod, material));
        }
    }

    private static void processLongRod(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(rodLong) || !material.hasProperty(PropertyKey.DUST)) {
            return;
        }

        ItemStack stack = ChemicalHelper.get(rodLong,
                material.hasFlag(IS_MAGNETIC) && material.hasProperty(PropertyKey.INGOT) ?
                        material.getProperty(PropertyKey.INGOT).getMacerateInto() : material);
        ItemStack stickStack = ChemicalHelper.get(rod,
                material.hasFlag(IS_MAGNETIC) && material.hasProperty(PropertyKey.INGOT) ?
                        material.getProperty(PropertyKey.INGOT).getMacerateInto() : material);

        CUTTER_RECIPES.recipeBuilder("cut_" + material.getName() + "_long_rod_to_rod")
                .inputItems(rodLong, material)
                .outputItems(stickStack.copyWithCount(2))
                .duration((int) Math.max(material.getMass(), 1L)).EUt(4)
                .save(provider);

        VanillaRecipeHelper.addShapedRecipe(provider, String.format("stick_long_%s", material.getName()),
                stickStack.copyWithCount(2),
                "s", "X", 'X', new MaterialEntry(rodLong, material));

        if (material.hasProperty(PropertyKey.GEM)) {
            VanillaRecipeHelper.addShapedRecipe(provider,
                    String.format("stick_long_gem_flawless_%s", material.getName()),
                    stickStack,
                    "sf",
                    "G ",
                    'G', new MaterialEntry(gemFlawless, material));

            VanillaRecipeHelper.addShapedRecipe(provider,
                    String.format("stick_long_gem_exquisite_%s", material.getName()),
                    stickStack.copyWithCount(2),
                    "sf", "G ",
                    'G', new MaterialEntry(gemExquisite, material));

        }

        VanillaRecipeHelper.addShapedRecipe(provider, String.format("stick_long_stick_%s", material.getName()), stack,
                "ShS",
                'S', new MaterialEntry(rod, material));

        FORGE_HAMMER_RECIPES.recipeBuilder("hammer_" + material.getName() + "_rod_to_long_rod")
                .inputItems(rod, material, 2)
                .outputItems(stack)
                .duration((int) Math.max(material.getMass(), 1L))
                .EUt(16)
                .save(provider);
    }

    private static void processTurbine(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(turbineBlade) || !material.hasProperty(PropertyKey.INGOT)) {
            return;
        }

        ItemStack rotorStack = GTItems.TURBINE_ROTOR.asStack();
        // noinspection ConstantConditions
        TurbineRotorBehaviour.getBehaviour(rotorStack).setPartMaterial(rotorStack, material);

        ASSEMBLER_RECIPES.recipeBuilder("assemble_" + material.getName() + "_turbine_blade")
                .inputItems(turbineBlade, material, 8)
                .inputItems(rodLong, GTMaterials.Magnalium)
                .outputItems(rotorStack)
                .duration(200)
                .EUt(400)
                .save(provider);

        FORMING_PRESS_RECIPES.recipeBuilder("press_" + material.getName() + "_turbine_rotor")
                .inputItems(plateDouble, material, 5)
                .inputItems(screw, material, 2)
                .outputItems(turbineBlade, material)
                .duration(20)
                .EUt(256)
                .save(provider);

        VanillaRecipeHelper.addShapedRecipe(provider, String.format("turbine_blade_%s", material.getName()),
                ChemicalHelper.get(turbineBlade, material),
                "PPP", "SPS", "fPd",
                'P', new MaterialEntry(plateDouble, material),
                'S', new MaterialEntry(screw, material));
    }

    private static void processRound(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(round) || !material.hasProperty(PropertyKey.INGOT)) {
            return;
        }

        var outputMaterial = material.hasFlag(IS_MAGNETIC) ? material.getProperty(PropertyKey.INGOT).getMacerateInto() :
                material;
        if (!material.hasFlag(NO_SMASHING)) {
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("round_%s", material.getName()),
                    ChemicalHelper.get(round, outputMaterial),
                    "fN", "Nh", 'N', new MaterialEntry(nugget, material));

            VanillaRecipeHelper.addShapedRecipe(provider, String.format("round_from_ingot_%s", material.getName()),
                    ChemicalHelper.get(round, outputMaterial, 4),
                    "fIh", 'I', new MaterialEntry(ingot, material));
        }

        LATHE_RECIPES.recipeBuilder("lathe_" + material.getName() + "_nugget_to_round")
                .EUt(VA[ULV]).duration(100)
                .inputItems(nugget, material)
                .outputItems(round, outputMaterial)
                .save(provider);
    }

    private static int getVoltageMultiplier(@NotNull Material material) {
        if (material.hasProperty(PropertyKey.POLYMER)) return 4;
        return material.getBlastTemperature() > 2800 ? VA[LV] : VA[ULV];
    }
}
