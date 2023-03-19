package com.gregtechceu.gtceu.data.recipe.handler;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.DustProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.GemProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.IngotProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.utils.GTUtil;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.api.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTItems.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public class PartsRecipeHandler {

    private PartsRecipeHandler() {
    }

    public static void init(Consumer<FinishedRecipe> provider) {
        stick.executeHandler(PropertyKey.DUST,  (tagPrefix, material, property) -> processStick(tagPrefix, material, property, provider));
        stickLong.executeHandler(PropertyKey.DUST,  (tagPrefix, material, property) -> processLongStick(tagPrefix, material, property, provider));
        plate.executeHandler(PropertyKey.DUST,  (tagPrefix, material, property) -> processPlate(tagPrefix, material, property, provider));
        plateDouble.executeHandler(PropertyKey.INGOT,  (tagPrefix, material, property) -> processPlateDouble(tagPrefix, material, property, provider));
        plateDense.executeHandler(PropertyKey.INGOT,  (tagPrefix, material, property) -> processPlateDense(tagPrefix, material, property, provider));

        turbineBlade.executeHandler(PropertyKey.INGOT,  (tagPrefix, material, property) -> processTurbine(tagPrefix, material, property, provider));
        rotor.executeHandler(PropertyKey.INGOT,  (tagPrefix, material, property) -> processRotor(tagPrefix, material, property, provider));
        bolt.executeHandler(PropertyKey.DUST,  (tagPrefix, material, property) -> processBolt(tagPrefix, material, property, provider));
        screw.executeHandler(PropertyKey.DUST,  (tagPrefix, material, property) -> processScrew(tagPrefix, material, property, provider));
        wireFine.executeHandler(PropertyKey.INGOT,  (tagPrefix, material, property) -> processFineWire(tagPrefix, material, property, provider));
        foil.executeHandler(PropertyKey.INGOT,  (tagPrefix, material, property) -> processFoil(tagPrefix, material, property, provider));
        lens.executeHandler(PropertyKey.GEM,  (tagPrefix, material, property) -> processLens(tagPrefix, material, property, provider));

        gear.executeHandler(PropertyKey.DUST,  (tagPrefix, material, property) -> processGear(tagPrefix, material, property, provider));
        gearSmall.executeHandler(PropertyKey.DUST,  (tagPrefix, material, property) -> processGear(tagPrefix, material, property, provider));
        ring.executeHandler(PropertyKey.INGOT,  (tagPrefix, material, property) -> processRing(tagPrefix, material, property, provider));
        springSmall.executeHandler(PropertyKey.INGOT,  (tagPrefix, material, property) -> processSpringSmall(tagPrefix, material, property, provider));
        spring.executeHandler(PropertyKey.INGOT,  (tagPrefix, material, property) -> processSpring(tagPrefix, material, property, provider));
        round.executeHandler(PropertyKey.INGOT,  (tagPrefix, material, property) -> processRound(tagPrefix, material, property, provider));
    }

    public static void processBolt(TagPrefix boltPrefix, Material material, DustProperty property, Consumer<FinishedRecipe> provider) {
        ItemStack boltStack = ChemicalHelper.get(boltPrefix, material);
        ItemStack ingotStack = ChemicalHelper.get(ingot, material);

        CUTTER_RECIPES.recipeBuilder(boltPrefix.name, material)
                .inputItems(screw, material)
                .outputItems(boltStack)
                .duration(20)
                .EUt(24)
                .save(provider);

        if (!boltStack.isEmpty() && !ingotStack.isEmpty()) {
            EXTRUDER_RECIPES.recipeBuilder(boltPrefix.name, material, "ingot")
                    .inputItems(ingot, material)
                    .notConsumable(SHAPE_EXTRUDER_BOLT)
                    .outputItems(GTUtil.copyAmount(8, boltStack))
                    .duration(15)
                    .EUt(VA[MV])
                    .save(provider);

            if (material.hasFlag(NO_SMASHING)) {
                EXTRUDER_RECIPES.recipeBuilder(boltPrefix.name, material, "dust")
                        .inputItems(dust, material)
                        .notConsumable(SHAPE_EXTRUDER_BOLT)
                        .outputItems(GTUtil.copyAmount(8, boltStack))
                        .duration(15)
                        .EUt(VA[MV])
                        .save(provider);
            }
        }
    }

    public static void processScrew(TagPrefix screwPrefix, Material material, DustProperty property, Consumer<FinishedRecipe> provider) {
        ItemStack screwStack = ChemicalHelper.get(screwPrefix, material);

        LATHE_RECIPES.recipeBuilder(screwPrefix.name, material)
                .inputItems(bolt, material)
                .outputItems(screwStack)
                .duration((int) Math.max(1, material.getMass() / 8L))
                .EUt(4)
                .save(provider);

        VanillaRecipeHelper.addShapedRecipe(provider, String.format("screw_%s", material),
                screwStack, "fX", "X ",
                'X', new UnificationEntry(bolt, material));
    }

    public static void processFoil(TagPrefix foilPrefix, Material material, IngotProperty property, Consumer<FinishedRecipe> provider) {
        if (!material.hasFlag(NO_SMASHING))
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("foil_%s", material),
                    ChemicalHelper.get(foilPrefix, material, 2),
                    "hP ", 'P', new UnificationEntry(plate, material));

        BENDER_RECIPES.recipeBuilder(foilPrefix.name, material, "plate")
                .inputItems(plate, material)
                .outputItems(foilPrefix, material, 4)
                .duration((int) material.getMass())
                .EUt(24)
                .circuitMeta(1)
                .save(provider);

        if (material.hasFlag(NO_SMASHING)) {
            EXTRUDER_RECIPES.recipeBuilder(foilPrefix.name, material, "ingot")
                    .inputItems(ingot, material)
                    .notConsumable(SHAPE_EXTRUDER_FOIL)
                    .outputItems(foilPrefix, material, 4)
                    .duration((int) material.getMass())
                    .EUt(24)
                    .save(provider);

            EXTRUDER_RECIPES.recipeBuilder(foilPrefix.name, material, "dust")
                    .inputItems(dust, material)
                    .notConsumable(SHAPE_EXTRUDER_FOIL)
                    .outputItems(foilPrefix, material, 4)
                    .duration((int) material.getMass())
                    .EUt(24)
                    .save(provider);
        }
    }

    public static void processFineWire(TagPrefix fineWirePrefix, Material material, IngotProperty property, Consumer<FinishedRecipe> provider) {
        ItemStack fineWireStack = ChemicalHelper.get(fineWirePrefix, material);

        if (!ChemicalHelper.get(foil, material).isEmpty())
            VanillaRecipeHelper.addShapelessRecipe(provider, String.format("fine_wire_%s", material.toString()),
                    fineWireStack, 'x', new UnificationEntry(foil, material));

        if (material.hasProperty(PropertyKey.WIRE)) {
            WIREMILL_RECIPES.recipeBuilder(fineWirePrefix.name, material)
                    .inputItems(wireGtSingle, material)
                    .outputItems(ChemicalHelper.get(wireFine, material, 4))
                    .duration((int) material.getMass() * 3 / 2)
                    .EUt(VA[ULV])
                    .save(provider);
        } else {
            WIREMILL_RECIPES.recipeBuilder(fineWirePrefix.name, material, "ingot")
                    .inputItems(ingot, material)
                    .outputItems(ChemicalHelper.get(wireFine, material, 8))
                    .duration((int) material.getMass() * 3)
                    .EUt(VA[ULV])
                    .save(provider);
        }
    }

    public static void processGear(TagPrefix gearPrefix, Material material, DustProperty property, Consumer<FinishedRecipe> provider) {
        ItemStack stack = ChemicalHelper.get(gearPrefix, material);
        if (gearPrefix == gear && material.hasProperty(PropertyKey.INGOT)) {
            int voltageMultiplier = getVoltageMultiplier(material);
            EXTRUDER_RECIPES.recipeBuilder(gearPrefix.name, material, "ingot")
                    .inputItems(ingot, material, 4)
                    .notConsumable(SHAPE_EXTRUDER_GEAR)
                    .outputItems(ChemicalHelper.get(gearPrefix, material))
                    .duration((int) material.getMass() * 5)
                    .EUt(8L * voltageMultiplier)
                    .save(provider);

            ALLOY_SMELTER_RECIPES.recipeBuilder(gearPrefix.name, material, "ingot")
                    .inputItems(ingot, material, 8)
                    .notConsumable(SHAPE_MOLD_GEAR)
                    .outputItems(ChemicalHelper.get(gearPrefix, material))
                    .duration((int) material.getMass() * 10)
                    .EUt(2L * voltageMultiplier)
                    .save(provider);

            if (material.hasFlag(NO_SMASHING)) {
                EXTRUDER_RECIPES.recipeBuilder(gearPrefix.name, material, "dust")
                        .inputItems(dust, material, 4)
                        .notConsumable(SHAPE_EXTRUDER_GEAR)
                        .outputItems(ChemicalHelper.get(gearPrefix, material))
                        .duration((int) material.getMass() * 5)
                        .EUt(8L * voltageMultiplier)
                        .save(provider);
            }
        }

        if (material.hasFluid()) {
            boolean isSmall = gearPrefix == gearSmall;
            FLUID_SOLIDFICATION_RECIPES.recipeBuilder(gearPrefix.name, material)
                    .notConsumable(isSmall ? SHAPE_MOLD_GEAR_SMALL : SHAPE_MOLD_GEAR)
                    .inputFluids(material.getFluid(L * (isSmall ? 1 : 4)))
                    .outputItems(stack)
                    .duration(isSmall ? 20 : 100)
                    .EUt(VA[ULV])
                    .save(provider);
        }

        if (material.hasFlag(GENERATE_PLATE) && material.hasFlag(GENERATE_ROD)) {
            if (gearPrefix == gearSmall) {
                VanillaRecipeHelper.addShapedRecipe(provider, String.format("small_gear_%s", material), ChemicalHelper.get(gearSmall, material),
                        " R ", "hPx", " R ", 'R', new UnificationEntry(stick, material), 'P', new UnificationEntry(plate, material));

                EXTRUDER_RECIPES.recipeBuilder(gearPrefix.name, material, ingot)
                        .inputItems(ingot, material)
                        .notConsumable(SHAPE_EXTRUDER_GEAR_SMALL)
                        .outputItems(stack)
                        .duration((int) material.getMass())
                        .EUt(material.getBlastTemperature() >= 2800 ? 256 : 64)
                        .save(provider);

                ALLOY_SMELTER_RECIPES.recipeBuilder(gearPrefix.name, material, ingot).duration((int) material.getMass()).EUt(VA[LV])
                        .inputItems(ingot, material, 2)
                        .notConsumable(SHAPE_MOLD_GEAR_SMALL)
                        .outputItems(gearSmall, material)
                        .save(provider);

                if (material.hasFlag(NO_SMASHING)) {
                    EXTRUDER_RECIPES.recipeBuilder(gearPrefix.name, material, dust)
                            .inputItems(dust, material)
                            .notConsumable(SHAPE_EXTRUDER_GEAR_SMALL)
                            .outputItems(stack)
                            .duration((int) material.getMass())
                            .EUt(material.getBlastTemperature() >= 2800 ? 256 : 64)
                            .save(provider);
                }
            } else {
                VanillaRecipeHelper.addShapedRecipe(provider, String.format("gear_%s", material), stack,
                        "RPR", "PwP", "RPR",
                        'P', new UnificationEntry(plate, material),
                        'R', new UnificationEntry(stick, material));
            }
        }
    }

    public static void processLens(TagPrefix lensPrefix, Material material, GemProperty property, Consumer<FinishedRecipe> provider) {
        ItemStack stack = ChemicalHelper.get(lensPrefix, material);

        LATHE_RECIPES.recipeBuilder(lensPrefix.name, material, plate)
                .inputItems(plate, material)
                .outputItems(lens, material)
                .outputItems(dustSmall, material)
                .duration(1200).EUt(120).save(provider);

        if (!ChemicalHelper.get(gemExquisite, material).isEmpty()) {
            LATHE_RECIPES.recipeBuilder(lensPrefix.name, material, gemExquisite)
                    .inputItems(gemExquisite, material)
                    .outputItems(lens, material)
                    .outputItems(dust, material, 2)
                    .duration(2400).EUt(30).save(provider);
        }

//        if (material == Diamond) { // override Diamond Lens to be LightBlue
//            OreDictUnifier.registerOre(stack, craftingLens, MarkerMaterials.Color.LightBlue);
//        } else if (material == Materials.Ruby) { // override Ruby Lens to be Red
//            OreDictUnifier.registerOre(stack, craftingLens, MarkerMaterials.Color.Red);
//        } else if (material == Materials.Emerald) { // override Emerald Lens to be Green
//            OreDictUnifier.registerOre(stack, craftingLens, MarkerMaterials.Color.Green);
//        } else if (material == Materials.Glass) { // the overriding is done in OreDictionaryLoader to prevent log spam
//            OreDictUnifier.registerOre(stack, craftingLens.name() + material.toCamelCaseString());
//        } else { // add more custom lenses here if needed
//
//            // Default behavior for determining lens color, left for addons and CraftTweaker
//            EnumDyeColor dyeColor = determineDyeColor(material.getMaterialRGB());
//            MarkerMaterial colorMaterial = MarkerMaterials.Color.COLORS.get(dyeColor);
//            OreDictUnifier.registerOre(stack, craftingLens, colorMaterial);
//        }
    }

    public static void processPlate(TagPrefix platePrefix, Material material, DustProperty property, Consumer<FinishedRecipe> provider) {
        var output = ChemicalHelper.get(platePrefix, material);
        if (material.hasFluid() && !output.isEmpty()) {
            FLUID_SOLIDFICATION_RECIPES.recipeBuilder(platePrefix.name, material)
                    .notConsumable(SHAPE_MOLD_PLATE)
                    .inputFluids(material.getFluid(L))
                    .outputItems(output)
                    .duration(40)
                    .EUt(VA[ULV])
                    .save(provider);
        }
    }

    public static void processPlateDouble(TagPrefix doublePrefix, Material material, IngotProperty property, Consumer<FinishedRecipe> provider) {
        if (material.hasFlag(GENERATE_PLATE)) {
            if (!material.hasFlag(NO_SMASHING)) {
                VanillaRecipeHelper.addShapedRecipe(provider, String.format("plate_double_%s", material),
                        ChemicalHelper.get(doublePrefix, material),
                        "h", "P", "P", 'P', new UnificationEntry(plate, material));
            }

            BENDER_RECIPES.recipeBuilder(doublePrefix.name, material, plate).EUt(96).duration((int) material.getMass() * 2)
                    .inputItems(plate, material, 2)
                    .outputItems(doublePrefix, material)
                    .circuitMeta(2)
                    .save(provider);

            BENDER_RECIPES.recipeBuilder(doublePrefix.name, material, ingot)
                    .inputItems(ingot, material, 2)
                    .circuitMeta(2)
                    .outputItems(doublePrefix, material)
                    .duration((int) material.getMass() * 2)
                    .EUt(96)
                    .save(provider);
        }
    }

    public static void processPlateDense(TagPrefix tagPrefix, Material material, IngotProperty property, Consumer<FinishedRecipe> provider) {
        BENDER_RECIPES.recipeBuilder(tagPrefix.name, material, plate)
                .inputItems(plate, material, 9)
                .circuitMeta(9)
                .outputItems(tagPrefix, material)
                .duration((int) Math.max(material.getMass() * 9L, 1L))
                .EUt(96)
                .save(provider);

        BENDER_RECIPES.recipeBuilder(tagPrefix.name, material, ingot)
                .inputItems(ingot, material, 9)
                .circuitMeta(9)
                .outputItems(tagPrefix, material)
                .duration((int) Math.max(material.getMass() * 9L, 1L))
                .EUt(96)
                .save(provider);
    }

    public static void processRing(TagPrefix ringPrefix, Material material, IngotProperty property, Consumer<FinishedRecipe> provider) {
        EXTRUDER_RECIPES.recipeBuilder(ringPrefix.name, material, ingot)
                .inputItems(ingot, material)
                .notConsumable(SHAPE_EXTRUDER_RING)
                .outputItems(ChemicalHelper.get(ringPrefix, material, 4))
                .duration((int) material.getMass() * 2)
                .EUt(6L * getVoltageMultiplier(material))
                .save(provider);

        if (!material.hasFlag(NO_SMASHING)) {
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("ring_%s", material),
                    ChemicalHelper.get(ringPrefix, material),
                    "h ", " X",
                    'X', new UnificationEntry(stick, material));
        } else {
            EXTRUDER_RECIPES.recipeBuilder(ringPrefix.name, material, dust)
                    .inputItems(dust, material)
                    .notConsumable(SHAPE_EXTRUDER_RING)
                    .outputItems(ChemicalHelper.get(ringPrefix, material, 4))
                    .duration((int) material.getMass() * 2)
                    .EUt(6L * getVoltageMultiplier(material))
                    .save(provider);
        }
    }

    public static void processSpringSmall(TagPrefix springPrefix, Material material, IngotProperty property, Consumer<FinishedRecipe> provider) {
        VanillaRecipeHelper.addShapedRecipe(provider, String.format("spring_small_%s", material.toString()),
                ChemicalHelper.get(springSmall, material),
                " s ", "fRx", 'R', new UnificationEntry(stick, material));

        BENDER_RECIPES.recipeBuilder(springPrefix.name, material, stick).duration((int) (material.getMass() / 2)).EUt(VA[ULV])
                .inputItems(stick, material)
                .outputItems(springSmall, material, 2)
                .circuitMeta(1)
                .save(provider);
    }

    public static void processSpring(TagPrefix springPrefix, Material material, IngotProperty property, Consumer<FinishedRecipe> provider) {
        BENDER_RECIPES.recipeBuilder(springPrefix.name, material, stickLong)
                .inputItems(stickLong, material)
                .outputItems(ChemicalHelper.get(spring, material))
                .circuitMeta(1)
                .duration(200)
                .EUt(16)
                .save(provider);

        VanillaRecipeHelper.addShapedRecipe(provider, String.format("spring_%s", material.toString()),
                ChemicalHelper.get(spring, material),
                " s ", "fRx", " R ", 'R', new UnificationEntry(stickLong, material));
    }

    public static void processRotor(TagPrefix rotorPrefix, Material material, IngotProperty property, Consumer<FinishedRecipe> provider) {
        ItemStack stack = ChemicalHelper.get(rotorPrefix, material);
        VanillaRecipeHelper.addShapedRecipe(provider, String.format("rotor_%s", material.toString()), stack,
                "ChC", "SRf", "CdC",
                'C', new UnificationEntry(plate, material),
                'S', new UnificationEntry(screw, material),
                'R', new UnificationEntry(ring, material));

        if (material.hasFluid()) {
            FLUID_SOLIDFICATION_RECIPES.recipeBuilder(rotorPrefix.name, material)
                    .notConsumable(SHAPE_MOLD_ROTOR)
                    .inputFluids(material.getFluid(L * 4))
                    .outputItems(GTUtil.copy(stack))
                    .duration(120)
                    .EUt(20)
                    .save(provider);
        }

        EXTRUDER_RECIPES.recipeBuilder(rotorPrefix.name, material, ingot)
                .inputItems(ingot, material, 4)
                .notConsumable(SHAPE_EXTRUDER_ROTOR)
                .outputItems(GTUtil.copy(stack))
                .duration((int) material.getMass() * 4)
                .EUt(material.getBlastTemperature() >= 2800 ? 256 : 64)
                .save(provider);

        if (material.hasFlag(NO_SMASHING)) {
            EXTRUDER_RECIPES.recipeBuilder(rotorPrefix.name, material, dust)
                    .inputItems(dust, material, 4)
                    .notConsumable(SHAPE_EXTRUDER_ROTOR)
                    .outputItems(GTUtil.copy(stack))
                    .duration((int) material.getMass() * 4)
                    .EUt(material.getBlastTemperature() >= 2800 ? 256 : 64)
                    .save(provider);
        }
    }

    public static void processStick(TagPrefix stickPrefix, Material material, DustProperty property, Consumer<FinishedRecipe> provider) {
        if (!material.hasFlag(GENERATE_ROD)) {
            return;
        }
        if (material.hasProperty(PropertyKey.GEM) || material.hasProperty(PropertyKey.INGOT)) {
            GTRecipeBuilder builder = LATHE_RECIPES.recipeBuilder(stickPrefix.name, material)
                    .inputItems(material.hasProperty(PropertyKey.GEM) ? gem : ingot, material)
                    .duration((int) Math.max(material.getMass() * 2, 1))
                    .EUt(16);

            if (ConfigHolder.recipes.harderRods) {
                builder.outputItems(stick, material);
                builder.outputItems(dustSmall, material, 2);
            } else {
                builder.outputItems(stick, material, 2);
            }
            builder.save(provider);
        }

        if (material.hasFlag(GENERATE_BOLT_SCREW)) {
            ItemStack boltStack = ChemicalHelper.get(bolt, material);
            CUTTER_RECIPES.recipeBuilder(stickPrefix.name, material)
                    .inputItems(stickPrefix, material)
                    .outputItems(GTUtil.copyAmount(4, boltStack))
                    .duration((int) Math.max(material.getMass() * 2L, 1L))
                    .EUt(4)
                    .save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, String.format("bolt_saw_%s", material),
                    GTUtil.copyAmount(2, boltStack),
                    "s ", " X",
                    'X', new UnificationEntry(stick, material));
        }
    }

    public static void processLongStick(TagPrefix longStickPrefix, Material material, DustProperty property, Consumer<FinishedRecipe> provider) {
        ItemStack stack = ChemicalHelper.get(longStickPrefix, material);
        ItemStack stickStack = ChemicalHelper.get(stick, material);

        if (!stickStack.isEmpty()) {
            CUTTER_RECIPES.recipeBuilder(longStickPrefix.name, material)
                    .inputItems(longStickPrefix, material)
                    .outputItems(GTUtil.copyAmount(2, stickStack))
                    .duration((int) Math.max(material.getMass(), 1L)).EUt(4)
                    .save(provider);

            VanillaRecipeHelper.addShapedRecipe(provider, String.format("stick_long_%s", material),
                    GTUtil.copyAmount(2, stickStack),
                    "s", "X", 'X', new UnificationEntry(stickLong, material));

            if(material.hasProperty(PropertyKey.GEM)) {
                VanillaRecipeHelper.addShapedRecipe(provider, String.format("stick_long_gem_flawless_%s", material),
                        stickStack,
                        "sf",
                        "G ",
                        'G', new UnificationEntry(gemFlawless, material));

                VanillaRecipeHelper.addShapedRecipe(provider, String.format("stick_long_gem_exquisite_%s", material),
                        GTUtil.copyAmount(2, stickStack),
                        "sf", "G ",
                        'G', new UnificationEntry(gemExquisite, material));

            }
        }

        if (!stack.isEmpty()) {
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("stick_long_stick_%s", material), stack,
                    "ShS",
                    'S', new UnificationEntry(stick, material));

            FORGE_HAMMER_RECIPES.recipeBuilder(longStickPrefix.name, material, stick)
                    .inputItems(stick, material, 2)
                    .outputItems(stack)
                    .duration((int) Math.max(material.getMass(), 1L))
                    .EUt(16)
                    .save(provider);

            if (material.hasProperty(PropertyKey.INGOT)) {
                EXTRUDER_RECIPES.recipeBuilder(longStickPrefix.name, material, ingot)
                        .inputItems(ingot, material)
                        .notConsumable(SHAPE_EXTRUDER_ROD_LONG)
                        .outputItems(stack)
                        .duration((int) Math.max(material.getMass(), 1L))
                        .EUt(64)
                        .save(provider);

                if (material.hasFlag(NO_SMASHING)) {
                    EXTRUDER_RECIPES.recipeBuilder(longStickPrefix.name, material, dust)
                            .inputItems(dust, material)
                            .notConsumable(SHAPE_EXTRUDER_ROD_LONG)
                            .outputItems(stack)
                            .duration((int) Math.max(material.getMass(), 1L))
                            .EUt(64)
                            .save(provider);
                }
            }
        }
    }

    //todo
    public static void processTurbine(TagPrefix toolPrefix, Material material, IngotProperty property, Consumer<FinishedRecipe> provider) {
//        ItemStack rotorStack = TURBINE_ROTOR.asStack();
//        //noinspection ConstantConditions
//        TurbineRotorBehavior.getInstanceFor(rotorStack).setPartMaterial(rotorStack, material);
//
//        ASSEMBLER_RECIPES.recipeBuilder()
//                .inputItems(turbineBlade, material, 8)
//                .inputItems(stickLong, Materials.Magnalium)
//                .outputItems(rotorStack)
//                .duration(200)
//                .EUt(400)
//                .save(provider);
//
//        FORMING_PRESS_RECIPES.recipeBuilder()
//                .inputItems(plateDouble, material, 5)
//                .inputItems(screw, material, 2)
//                .outputItems(ChemicalHelper.get(toolPrefix, material))
//                .duration(20)
//                .EUt(256)
//                .save(provider);
//
//        ModHandler.addShapedRecipe(String.format("turbine_blade_%s", material),
//                ChemicalHelper.get(toolPrefix, material),
//                "PPP", "SPS", "fPd",
//                'P', new UnificationEntry(plateDouble, material),
//                'S', new UnificationEntry(screw, material));
    }

    public static void processRound(TagPrefix roundPrefix, Material material, IngotProperty property, Consumer<FinishedRecipe> provider) {
        if (!material.hasFlag(NO_SMASHING)) {

            VanillaRecipeHelper.addShapedRecipe(provider, String.format("round_%s", material),
                    ChemicalHelper.get(round, material),
                    "fN", "Nh", 'N', new UnificationEntry(nugget, material));

            VanillaRecipeHelper.addShapedRecipe(provider, String.format("round_from_ingot_%s", material),
                    ChemicalHelper.get(round, material, 4),
                    "fIh", 'I', new UnificationEntry(ingot, material));
        }

        LATHE_RECIPES.recipeBuilder(roundPrefix.name, material, nugget).EUt(VA[ULV]).duration(100)
                .inputItems(nugget, material)
                .outputItems(round, material)
                .save(provider);
    }

    private static int getVoltageMultiplier(Material material) {
        return material.getBlastTemperature() > 2800 ? VA[LV] : VA[ULV];
    }
}
