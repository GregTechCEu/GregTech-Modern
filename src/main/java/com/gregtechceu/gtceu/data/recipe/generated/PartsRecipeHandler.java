package com.gregtechceu.gtceu.data.recipe.generated;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.*;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.item.TurbineRotorBehaviour;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.utils.GTUtil;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public class PartsRecipeHandler {

    private PartsRecipeHandler() {
    }

    public static void init(Consumer<FinishedRecipe> provider) {
        rod.executeHandler(PropertyKey.DUST, (tagPrefix, material, property) -> processStick(tagPrefix, material, property, provider));
        rodLong.executeHandler(PropertyKey.DUST, (tagPrefix, material, property) -> processLongStick(tagPrefix, material, property, provider));
        plate.executeHandler(PropertyKey.DUST, (tagPrefix, material, property) -> processPlate(tagPrefix, material, property, provider));
        plateDouble.executeHandler(PropertyKey.INGOT, (tagPrefix, material, property) -> processPlateDouble(tagPrefix, material, property, provider));
        plateDense.executeHandler(PropertyKey.INGOT, (tagPrefix, material, property) -> processPlateDense(tagPrefix, material, property, provider));

        turbineBlade.executeHandler(PropertyKey.INGOT, (tagPrefix, material, property) -> processTurbine(tagPrefix, material, property, provider));
        rotor.executeHandler(PropertyKey.INGOT, (tagPrefix, material, property) -> processRotor(tagPrefix, material, property, provider));
        bolt.executeHandler(PropertyKey.DUST, (tagPrefix, material, property) -> processBolt(tagPrefix, material, property, provider));
        screw.executeHandler(PropertyKey.DUST, (tagPrefix, material, property) -> processScrew(tagPrefix, material, property, provider));
        wireFine.executeHandler(PropertyKey.INGOT, (tagPrefix, material, property) -> processFineWire(tagPrefix, material, property, provider));
        foil.executeHandler(PropertyKey.INGOT, (tagPrefix, material, property) -> processFoil(tagPrefix, material, property, provider));
        lens.executeHandler(PropertyKey.GEM, (tagPrefix, material, property) -> processLens(tagPrefix, material, property, provider));

        gear.executeHandler(PropertyKey.DUST, (tagPrefix, material, property) -> processGear(tagPrefix, material, property, provider));
        gearSmall.executeHandler(PropertyKey.DUST, (tagPrefix, material, property) -> processGear(tagPrefix, material, property, provider));
        ring.executeHandler(PropertyKey.INGOT, (tagPrefix, material, property) -> processRing(tagPrefix, material, property, provider));
        springSmall.executeHandler(PropertyKey.INGOT, (tagPrefix, material, property) -> processSpringSmall(tagPrefix, material, property, provider));
        spring.executeHandler(PropertyKey.INGOT, (tagPrefix, material, property) -> processSpring(tagPrefix, material, property, provider));
        round.executeHandler(PropertyKey.INGOT, (tagPrefix, material, property) -> processRound(tagPrefix, material, property, provider));
    }

    public static void processBolt(TagPrefix boltPrefix, Material material, DustProperty property, Consumer<FinishedRecipe> provider) {
        ItemStack boltStack = ChemicalHelper.get(boltPrefix, material);
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
                    .outputItems(GTUtil.copyAmount(8, boltStack))
                    .duration(15)
                    .EUt(VA[MV])
                    .save(provider);

            if (material.hasFlag(NO_SMASHING)) {
                EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_dust_to_bolt")
                        .inputItems(dust, material)
                        .notConsumable(GTItems.SHAPE_EXTRUDER_BOLT)
                        .outputItems(GTUtil.copyAmount(8, boltStack))
                        .duration(15)
                        .EUt(VA[MV])
                        .save(provider);
            }
        }
    }

    public static void processScrew(TagPrefix screwPrefix, Material material, DustProperty property, Consumer<FinishedRecipe> provider) {
        ItemStack screwStack = ChemicalHelper.get(screwPrefix, material);

        LATHE_RECIPES.recipeBuilder("lathe_" + material.getName() + "_bolt_to_screw")
                .inputItems(bolt, material)
                .outputItems(screwStack)
                .duration((int) Math.max(1, material.getMass() / 8L))
                .EUt(4)
                .save(provider);

        VanillaRecipeHelper.addShapedRecipe(provider, String.format("screw_%s", material.getName()),
                screwStack, "fX", "X ",
                'X', new UnificationEntry(bolt, material));
    }

    public static void processFoil(TagPrefix foilPrefix, Material material, IngotProperty property, Consumer<FinishedRecipe> provider) {
        if (!material.hasFlag(NO_SMASHING))
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("foil_%s", material.getName()),
                    ChemicalHelper.get(foilPrefix, material, 2),
                    "hP ", 'P', new UnificationEntry(plate, material));

        BENDER_RECIPES.recipeBuilder("bend_" + material.getName() + "_plate_to_foil")
                .inputItems(plate, material)
                .outputItems(foilPrefix, material, 4)
                .duration((int) material.getMass())
                .EUt(24)
                .circuitMeta(1)
                .save(provider);

        BENDER_RECIPES.recipeBuilder("bend_" + material.getName() + "_ingot_to_foil")
                .inputItems(ingot, material)
                .outputItems(foilPrefix, material, 4)
                .duration((int) material.getMass())
                .EUt(24)
                .circuitMeta(10)
                .save(provider);

        if (material.hasFlag(NO_SMASHING)) {
            EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_ingot_to_foil")
                    .inputItems(ingot, material)
                    .notConsumable(GTItems.SHAPE_EXTRUDER_FOIL)
                    .outputItems(foilPrefix, material, 4)
                    .duration((int) material.getMass())
                    .EUt(24)
                    .save(provider);

            EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_dust_to_foil")
                    .inputItems(dust, material)
                    .notConsumable(GTItems.SHAPE_EXTRUDER_FOIL)
                    .outputItems(foilPrefix, material, 4)
                    .duration((int) material.getMass())
                    .EUt(24)
                    .save(provider);
        }
    }

    public static void processFineWire(TagPrefix fineWirePrefix, Material material, IngotProperty property, Consumer<FinishedRecipe> provider) {
        ItemStack fineWireStack = ChemicalHelper.get(fineWirePrefix, material);

        if (!ChemicalHelper.get(foil, material).isEmpty())
            VanillaRecipeHelper.addShapelessRecipe(provider, String.format("fine_wire_%s", material.getName()),
                    fineWireStack, 'x', new UnificationEntry(foil, material));

        if (material.hasProperty(PropertyKey.WIRE)) {
            WIREMILL_RECIPES.recipeBuilder("mill_" + material.getName() + "_wire_to_fine_wire")
                    .inputItems(wireGtSingle, material)
                    .outputItems(wireFine, material, 4)
                    .duration((int) material.getMass() * 3 / 2)
                    .EUt(VA[ULV])
                    .save(provider);
        } else {
            WIREMILL_RECIPES.recipeBuilder("mill_" + material.getName() + "ingot_to_fine_wire")
                    .inputItems(ingot, material)
                    .outputItems(wireFine, material, 8)
                    .duration((int) material.getMass() * 3)
                    .EUt(VA[ULV])
                    .save(provider);
        }
    }

    public static void processGear(TagPrefix gearPrefix, Material material, DustProperty property, Consumer<FinishedRecipe> provider) {
        ItemStack stack = ChemicalHelper.get(gearPrefix, material);
        if (gearPrefix == gear && material.hasProperty(PropertyKey.INGOT)) {
            int voltageMultiplier = getVoltageMultiplier(material);
            EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_ingot_to_gear")
                    .inputItems(ingot, material, 4)
                    .notConsumable(GTItems.SHAPE_EXTRUDER_GEAR)
                    .outputItems(gearPrefix, material)
                    .duration((int) material.getMass() * 5)
                    .EUt(8L * voltageMultiplier)
                    .save(provider);

            ALLOY_SMELTER_RECIPES.recipeBuilder("alloy_smelt_" + material.getName() + "_ingot_to_gear")
                    .inputItems(ingot, material, 8)
                    .notConsumable(GTItems.SHAPE_MOLD_GEAR)
                    .outputItems(gearPrefix, material)
                    .duration((int) material.getMass() * 10)
                    .EUt(2L * voltageMultiplier)
                    .save(provider);

            if (material.hasFlag(NO_SMASHING)) {
                EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_dust_to_gear")
                        .inputItems(dust, material, 4)
                        .notConsumable(GTItems.SHAPE_EXTRUDER_GEAR)
                        .outputItems(gearPrefix, material)
                        .duration((int) material.getMass() * 5)
                        .EUt(8L * voltageMultiplier)
                        .save(provider);
            }
        }

        if (material.hasFluid()) {
            boolean isSmall = gearPrefix == gearSmall;
            FLUID_SOLIDFICATION_RECIPES.recipeBuilder("solidify_" + material.getName() + "_" + gearPrefix.name)
                    .notConsumable(isSmall ? GTItems.SHAPE_MOLD_GEAR_SMALL : GTItems.SHAPE_MOLD_GEAR)
                    .inputFluids(material.getFluid(L * (isSmall ? 1 : 4)))
                    .outputItems(stack)
                    .duration(isSmall ? 20 : 100)
                    .EUt(VA[ULV])
                    .save(provider);
        }

        if (material.hasFlag(GENERATE_PLATE) && material.hasFlag(GENERATE_ROD)) {
            if (gearPrefix == gearSmall) {
                VanillaRecipeHelper.addShapedRecipe(provider, String.format("small_gear_%s", material.getName()), ChemicalHelper.get(gearSmall, material),
                        " R ", "hPx", " R ", 'R', new UnificationEntry(rod, material), 'P', new UnificationEntry(plate, material));

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
                        .outputItems(gearSmall, material)
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
                        'P', new UnificationEntry(plate, material),
                        'R', new UnificationEntry(rod, material));
            }
        }
    }

    public static void processLens(TagPrefix lensPrefix, Material material, GemProperty property, Consumer<FinishedRecipe> provider) {
        ItemStack stack = ChemicalHelper.get(lensPrefix, material);

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

    public static void processPlate(TagPrefix platePrefix, Material material, DustProperty property, Consumer<FinishedRecipe> provider) {
        if (material.hasFluid()) {
            FLUID_SOLIDFICATION_RECIPES.recipeBuilder("solidify_" + material.getName() + "_to_plate")
                    .notConsumable(GTItems.SHAPE_MOLD_PLATE)
                    .inputFluids(material.getFluid(L))
                    .outputItems(platePrefix, material)
                    .duration(40)
                    .EUt(VA[ULV])
                    .save(provider);
        }
    }

    public static void processPlateDouble(TagPrefix doublePrefix, Material material, IngotProperty property, Consumer<FinishedRecipe> provider) {
        if (material.hasFlag(GENERATE_PLATE)) {
            if (!material.hasFlag(NO_SMASHING)) {
                VanillaRecipeHelper.addShapedRecipe(provider, String.format("plate_double_%s", material.getName()),
                        ChemicalHelper.get(doublePrefix, material),
                        "h", "P", "P", 'P', new UnificationEntry(plate, material));
            }

            BENDER_RECIPES.recipeBuilder("bend_" + material.getName() + "_plate_to_double_plate")
                    .EUt(96).duration((int) material.getMass() * 2)
                    .inputItems(plate, material, 2)
                    .outputItems(doublePrefix, material)
                    .circuitMeta(2)
                    .save(provider);

            BENDER_RECIPES.recipeBuilder("bend_" + material.getName() + "_ingot_to_double_plate")
                    .inputItems(ingot, material, 2)
                    .circuitMeta(2)
                    .outputItems(doublePrefix, material)
                    .duration((int) material.getMass() * 2)
                    .EUt(96)
                    .save(provider);
        }
    }

    public static void processPlateDense(TagPrefix tagPrefix, Material material, IngotProperty property, Consumer<FinishedRecipe> provider) {
        BENDER_RECIPES.recipeBuilder("bend_" + material.getName() + "_plate_to_dense_plate")
                .inputItems(plate, material, 9)
                .circuitMeta(9)
                .outputItems(tagPrefix, material)
                .duration((int) Math.max(material.getMass() * 9L, 1L))
                .EUt(96)
                .save(provider);

        if (material.hasProperty(PropertyKey.INGOT)) {
            BENDER_RECIPES.recipeBuilder("bend_" + material.getName() + "_ingot_to_dense_plate")
                .inputItems(ingot, material, 9)
                .circuitMeta(9)
                .outputItems(tagPrefix, material)
                .duration((int) Math.max(material.getMass() * 9L, 1L))
                .EUt(96)
                .save(provider);
        }
    }

    public static void processRing(TagPrefix ringPrefix, Material material, IngotProperty property, Consumer<FinishedRecipe> provider) {
        EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_ingot_to_ring")
                .inputItems(ingot, material)
                .notConsumable(GTItems.SHAPE_EXTRUDER_RING)
                .outputItems(ringPrefix, material, 4)
                .duration((int) material.getMass() * 2)
                .EUt(6L * getVoltageMultiplier(material))
                .save(provider);

        if (!material.hasFlag(NO_SMASHING)) {
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("ring_%s", material.getName()),
                    ChemicalHelper.get(ringPrefix, material),
                    "h ", " X",
                    'X', new UnificationEntry(rod, material));
        } else {
            EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_dust_to_ring")
                    .inputItems(dust, material)
                    .notConsumable(GTItems.SHAPE_EXTRUDER_RING)
                    .outputItems(ringPrefix, material, 4)
                    .duration((int) material.getMass() * 2)
                    .EUt(6L * getVoltageMultiplier(material))
                    .save(provider);
        }
    }

    public static void processSpringSmall(TagPrefix springPrefix, Material material, IngotProperty property, Consumer<FinishedRecipe> provider) {
        VanillaRecipeHelper.addShapedRecipe(provider, String.format("spring_small_%s", material.getName()),
                ChemicalHelper.get(springSmall, material),
                " s ", "fRx", 'R', new UnificationEntry(rod, material));

        BENDER_RECIPES.recipeBuilder("bend_" + material.getName() + "_rod_to_small_spring")
                .duration((int) (material.getMass() / 2)).EUt(VA[ULV])
                .inputItems(rod, material)
                .outputItems(springSmall, material, 2)
                .circuitMeta(1)
                .save(provider);
    }

    public static void processSpring(TagPrefix springPrefix, Material material, IngotProperty property, Consumer<FinishedRecipe> provider) {
        BENDER_RECIPES.recipeBuilder("bend_" + material.getName() + "_long_rod_to_spring")
                .inputItems(rodLong, material)
                .outputItems(spring, material)
                .circuitMeta(1)
                .duration(200)
                .EUt(16)
                .save(provider);

        VanillaRecipeHelper.addShapedRecipe(provider, String.format("spring_%s", material.getName()),
                ChemicalHelper.get(spring, material),
                " s ", "fRx", " R ", 'R', new UnificationEntry(rodLong, material));
    }

    public static void processRotor(TagPrefix rotorPrefix, Material material, IngotProperty property, Consumer<FinishedRecipe> provider) {
        ItemStack stack = ChemicalHelper.get(rotorPrefix, material);
        VanillaRecipeHelper.addShapedRecipe(provider, String.format("rotor_%s", material.getName()), stack,
                "ChC", "SRf", "CdC",
                'C', new UnificationEntry(plate, material),
                'S', new UnificationEntry(screw, material),
                'R', new UnificationEntry(ring, material));

        if (material.hasFluid()) {
            FLUID_SOLIDFICATION_RECIPES.recipeBuilder("solidify_" + material.getName() + "_to_rotor")
                    .notConsumable(GTItems.SHAPE_MOLD_ROTOR)
                    .inputFluids(material.getFluid(L * 4))
                    .outputItems(GTUtil.copy(stack))
                    .duration(120)
                    .EUt(20)
                    .save(provider);
        }

        EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_ingot_to_rotor")
                .inputItems(ingot, material, 4)
                .notConsumable(GTItems.SHAPE_EXTRUDER_ROTOR)
                .outputItems(GTUtil.copy(stack))
                .duration((int) material.getMass() * 4)
                .EUt(material.getBlastTemperature() >= 2800 ? 256 : 64)
                .save(provider);

        if (material.hasFlag(NO_SMASHING)) {
            EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_dust_to_rotor")
                    .inputItems(dust, material, 4)
                    .notConsumable(GTItems.SHAPE_EXTRUDER_ROTOR)
                    .outputItems(GTUtil.copy(stack))
                    .duration((int) material.getMass() * 4)
                    .EUt(material.getBlastTemperature() >= 2800 ? 256 : 64)
                    .save(provider);
        }
    }

    public static void processStick(TagPrefix stickPrefix, Material material, DustProperty property, Consumer<FinishedRecipe> provider) {
        if (material.hasProperty(PropertyKey.GEM) || material.hasProperty(PropertyKey.INGOT)) {
            GTRecipeBuilder builder = LATHE_RECIPES.recipeBuilder("lathe_" + material.getName() + "_to_rod")
                    .inputItems(material.hasProperty(PropertyKey.GEM) ? gem : ingot, material)
                    .duration((int) Math.max(material.getMass() * 2, 1))
                    .EUt(16);

            if (ConfigHolder.INSTANCE.recipes.harderRods) {
                builder.outputItems(rod, material);
                builder.outputItems(dustSmall, material, 2);
            } else {
                builder.outputItems(rod, material, 2);
            }
            builder.save(provider);
        }

        if (material.hasFlag(GENERATE_BOLT_SCREW)) {
            ItemStack boltStack = ChemicalHelper.get(bolt, material);
            CUTTER_RECIPES.recipeBuilder("cut_" + material.getName() + "_rod_to_bolt")
                    .inputItems(stickPrefix, material)
                    .outputItems(GTUtil.copyAmount(4, boltStack))
                    .duration((int) Math.max(material.getMass() * 2L, 1L))
                    .EUt(4)
                    .save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, String.format("bolt_saw_%s", material.getName()),
                    GTUtil.copyAmount(2, boltStack),
                    "s ", " X",
                    'X', new UnificationEntry(rod, material));
        }
    }

    public static void processLongStick(TagPrefix longStickPrefix, Material material, DustProperty property, Consumer<FinishedRecipe> provider) {
        ItemStack stack = ChemicalHelper.get(longStickPrefix, material);
        ItemStack stickStack = ChemicalHelper.get(rod, material);

        CUTTER_RECIPES.recipeBuilder("cut_" + material.getName() + "_long_rod_to_rod")
                .inputItems(longStickPrefix, material)
                .outputItems(GTUtil.copyAmount(2, stickStack))
                .duration((int) Math.max(material.getMass(), 1L)).EUt(4)
                .save(provider);

        VanillaRecipeHelper.addShapedRecipe(provider, String.format("stick_long_%s", material.getName()),
                GTUtil.copyAmount(2, stickStack),
                "s", "X", 'X', new UnificationEntry(rodLong, material));

        if(material.hasProperty(PropertyKey.GEM)) {
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("stick_long_gem_flawless_%s", material.getName()),
                    stickStack,
                    "sf",
                    "G ",
                    'G', new UnificationEntry(gemFlawless, material));

            VanillaRecipeHelper.addShapedRecipe(provider, String.format("stick_long_gem_exquisite_%s", material.getName()),
                    GTUtil.copyAmount(2, stickStack),
                    "sf", "G ",
                    'G', new UnificationEntry(gemExquisite, material));

        }

        VanillaRecipeHelper.addShapedRecipe(provider, String.format("stick_long_stick_%s", material.getName()), stack,
                "ShS",
                'S', new UnificationEntry(rod, material));

        FORGE_HAMMER_RECIPES.recipeBuilder("hammer_" + material.getName() + "_rod_to_long_rod")
                .inputItems(rod, material, 2)
                .outputItems(stack)
                .duration((int) Math.max(material.getMass(), 1L))
                .EUt(16)
                .save(provider);

        if (material.hasProperty(PropertyKey.INGOT)) {
            EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_ingot_to_long_rod")
                    .inputItems(ingot, material)
                    .notConsumable(GTItems.SHAPE_EXTRUDER_ROD_LONG)
                    .outputItems(stack)
                    .duration((int) Math.max(material.getMass(), 1L))
                    .EUt(64)
                    .save(provider);

            if (material.hasFlag(NO_SMASHING)) {
                EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_dust_to_long_rod")
                        .inputItems(dust, material)
                        .notConsumable(GTItems.SHAPE_EXTRUDER_ROD_LONG)
                        .outputItems(stack)
                        .duration((int) Math.max(material.getMass(), 1L))
                        .EUt(64)
                        .save(provider);
            }
        }
    }

    public static void processTurbine(TagPrefix toolPrefix, Material material, IngotProperty property, Consumer<FinishedRecipe> provider) {
        ItemStack rotorStack = GTItems.TURBINE_ROTOR.asStack();
        //noinspection ConstantConditions
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
                .outputItems(toolPrefix, material)
                .duration(20)
                .EUt(256)
                .save(provider);

        VanillaRecipeHelper.addShapedRecipe(provider, String.format("turbine_blade_%s", material.getName()),
                ChemicalHelper.get(toolPrefix, material),
                "PPP", "SPS", "fPd",
                'P', new UnificationEntry(plateDouble, material),
                'S', new UnificationEntry(screw, material));
    }

    public static void processRound(TagPrefix roundPrefix, Material material, IngotProperty property, Consumer<FinishedRecipe> provider) {
        if (!material.hasFlag(NO_SMASHING)) {

            VanillaRecipeHelper.addShapedRecipe(provider, String.format("round_%s", material.getName()),
                    ChemicalHelper.get(round, material),
                    "fN", "Nh", 'N', new UnificationEntry(nugget, material));

            VanillaRecipeHelper.addShapedRecipe(provider, String.format("round_from_ingot_%s", material.getName()),
                    ChemicalHelper.get(round, material, 4),
                    "fIh", 'I', new UnificationEntry(ingot, material));
        }

        LATHE_RECIPES.recipeBuilder("lathe_" + material.getName() + "_nugget_to_round")
                .EUt(VA[ULV]).duration(100)
                .inputItems(nugget, material)
                .outputItems(round, material)
                .save(provider);
    }

    private static int getVoltageMultiplier(Material material) {
        return material.getBlastTemperature() > 2800 ? VA[LV] : VA[ULV];
    }
}
