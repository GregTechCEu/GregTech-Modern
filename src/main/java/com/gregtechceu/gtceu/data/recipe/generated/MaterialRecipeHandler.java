package com.gregtechceu.gtceu.data.recipe.generated;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterials;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.*;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.registry.MaterialRegistry;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeCategories;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.CraftingComponent;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public class MaterialRecipeHandler {

    private static final List<TagPrefix> GEM_ORDER = ConfigHolder.INSTANCE.recipes.generateLowQualityGems ?
            Arrays.asList(
                    gemChipped, gemFlawed, gem, gemFlawless, gemExquisite) :
            Arrays.asList(gem, gemFlawless, gemExquisite);

    public static void init(Consumer<FinishedRecipe> provider) {
        ingot.executeHandler(provider, PropertyKey.INGOT, MaterialRecipeHandler::processIngot);
        nugget.executeHandler(provider, PropertyKey.DUST, MaterialRecipeHandler::processNugget);

        block.executeHandler(provider, PropertyKey.DUST, MaterialRecipeHandler::processBlock);
        frameGt.executeHandler(provider, PropertyKey.DUST, MaterialRecipeHandler::processFrame);

        dust.executeHandler(provider, PropertyKey.DUST, MaterialRecipeHandler::processDust);
        dustSmall.executeHandler(provider, PropertyKey.DUST, MaterialRecipeHandler::processSmallDust);
        dustTiny.executeHandler(provider, PropertyKey.DUST, MaterialRecipeHandler::processTinyDust);

        for (TagPrefix orePrefix : GEM_ORDER) {
            orePrefix.executeHandler(provider, PropertyKey.GEM, MaterialRecipeHandler::processGemConversion);
        }

        generateSurfaceRockRecipe(provider);
    }

    public static void processDust(TagPrefix dustPrefix, Material mat, DustProperty property,
                                   Consumer<FinishedRecipe> provider) {
        String id = "%s_%s".formatted(FormattingUtil.toLowerCaseUnder(dustPrefix.name),
                mat.getName().toLowerCase(Locale.ROOT));
        ItemStack dustStack = ChemicalHelper.get(dustPrefix, mat);
        OreProperty oreProperty = mat.hasProperty(PropertyKey.ORE) ? mat.getProperty(PropertyKey.ORE) : null;
        if (mat.hasProperty(PropertyKey.GEM)) {
            ItemStack gemStack = ChemicalHelper.get(gem, mat);

            if (mat.hasFlag(CRYSTALLIZABLE)) {
                AUTOCLAVE_RECIPES.recipeBuilder("autoclave_" + id + "_water")
                        .inputItems(dustStack)
                        .inputFluids(Water.getFluid(250))
                        .chancedOutput(gemStack, 7000, 1000)
                        .duration(1200).EUt(24)
                        .save(provider);

                AUTOCLAVE_RECIPES.recipeBuilder("autoclave_" + id + "_distilled")
                        .inputItems(dustStack)
                        .inputFluids(DistilledWater.getFluid(50))
                        .outputItems(gemStack)
                        .duration(600).EUt(24)
                        .save(provider);
            }

            if (!mat.hasFlag(EXPLOSIVE) && !mat.hasFlag(FLAMMABLE)) {
                IMPLOSION_RECIPES.recipeBuilder("implode_" + id + "_powderbarrel")
                        .inputItems(GTUtil.copyAmount(4, dustStack))
                        .outputItems(GTUtil.copyAmount(3, gemStack))
                        .chancedOutput(dust, GTMaterials.DarkAsh, 2500, 0)
                        .explosivesType(new ItemStack(GTBlocks.POWDERBARREL, 8))
                        .save(provider);

                IMPLOSION_RECIPES.recipeBuilder("implode_" + id + "_tnt")
                        .inputItems(GTUtil.copyAmount(4, dustStack))
                        .outputItems(GTUtil.copyAmount(3, gemStack))
                        .chancedOutput(dust, GTMaterials.DarkAsh, 2500, 0)
                        .explosivesAmount(4)
                        .save(provider);

                IMPLOSION_RECIPES.recipeBuilder("implode_" + id + "_dynamite")
                        .inputItems(GTUtil.copyAmount(4, dustStack))
                        .outputItems(GTUtil.copyAmount(3, gemStack))
                        .chancedOutput(dust, GTMaterials.DarkAsh, 2500, 0)
                        .explosivesType(GTItems.DYNAMITE.asStack(2))
                        .save(provider);

                IMPLOSION_RECIPES.recipeBuilder("implode_" + id + "_itnt")
                        .inputItems(GTUtil.copyAmount(4, dustStack))
                        .outputItems(GTUtil.copyAmount(3, gemStack))
                        .chancedOutput(dust, GTMaterials.DarkAsh, 2500, 0)
                        .explosivesType(new ItemStack(GTBlocks.INDUSTRIAL_TNT))
                        .save(provider);
            }

            if (oreProperty != null) {
                Material smeltingResult = oreProperty.getDirectSmeltResult();
                if (smeltingResult != null) {
                    VanillaRecipeHelper.addSmeltingRecipe(provider, id + "_ingot",
                            ChemicalHelper.getTag(dustPrefix, mat), ChemicalHelper.get(ingot, smeltingResult));
                }
            }

        } else if (mat.hasProperty(PropertyKey.INGOT)) {
            if (!mat.hasAnyOfFlags(FLAMMABLE, NO_SMELTING)) {

                boolean hasHotIngot = ingotHot.doGenerateItem(mat);
                ItemStack ingotStack = ChemicalHelper.get(hasHotIngot ? ingotHot : ingot, mat);
                if (ingotStack.isEmpty() && oreProperty != null) {
                    Material smeltingResult = oreProperty.getDirectSmeltResult();
                    if (smeltingResult != null) {
                        ingotStack = ChemicalHelper.get(ingot, smeltingResult);
                    }
                }
                int blastTemp = mat.getBlastTemperature();

                if (blastTemp <= 0) {
                    // smelting magnetic dusts is handled elsewhere
                    if (!mat.hasFlag(IS_MAGNETIC)) {
                        // do not register inputs by tag prefix here. Let other mods register their own dust -> ingots
                        VanillaRecipeHelper.addSmeltingRecipe(provider, "smelt_" + id + "_to_ingot",
                                ChemicalHelper.getTag(dustPrefix, mat), ingotStack);
                    }
                } else {
                    IngotProperty ingotProperty = mat.getProperty(PropertyKey.INGOT);
                    BlastProperty blastProperty = mat.getProperty(PropertyKey.BLAST);

                    processEBFRecipe(mat, blastProperty, ingotStack, provider);

                    if (ingotProperty.getMagneticMaterial() != null) {
                        processEBFRecipe(ingotProperty.getMagneticMaterial(), blastProperty, ingotStack, provider);
                    }
                }
            }
        } else {
            if (mat.hasFlag(GENERATE_PLATE) && !mat.hasFlag(EXCLUDE_PLATE_COMPRESSOR_RECIPE)) {
                COMPRESSOR_RECIPES.recipeBuilder("compress_plate_" + id)
                        .inputItems(dustStack)
                        .outputItems(plate, mat)
                        .save(provider);
            }

            // Some Ores with Direct Smelting Results have neither ingot nor gem properties
            if (oreProperty != null) {
                Material smeltingResult = oreProperty.getDirectSmeltResult();
                if (smeltingResult != null) {
                    ItemStack ingotStack = ChemicalHelper.get(ingot, smeltingResult);
                    if (!ingotStack.isEmpty()) {
                        VanillaRecipeHelper.addSmeltingRecipe(provider, "smelt_" + id + "_to_ingot",
                                ChemicalHelper.getTag(dustPrefix, mat), ingotStack);
                    }
                }
            }
        }
    }

    private static void processEBFRecipe(Material material, BlastProperty property, ItemStack output,
                                         Consumer<FinishedRecipe> provider) {
        int blastTemp = property.getBlastTemperature();
        BlastProperty.GasTier gasTier = property.getGasTier();
        int duration = property.getDurationOverride();
        if (duration <= 0) {
            duration = Math.max(1, (int) (material.getMass() * blastTemp / 50L));
        }
        int EUt = property.getEUtOverride();
        if (EUt <= 0) EUt = VA[MV];

        GTRecipeBuilder blastBuilder = BLAST_RECIPES.recipeBuilder("blast_" + material.getName())
                .inputItems(dust, material)
                .outputItems(output)
                .blastFurnaceTemp(blastTemp)
                .EUt(EUt);

        if (gasTier != null) {
            FluidIngredient gas = CraftingComponent.EBF_GASES.get(gasTier).copy();

            blastBuilder.copy("blast_" + material.getName())
                    .circuitMeta(1)
                    .duration(duration)
                    .save(provider);

            blastBuilder.copy("blast_" + material.getName() + "_gas")
                    .circuitMeta(2)
                    .inputFluids(gas)
                    .duration((int) (duration * 0.67))
                    .save(provider);
        } else {
            blastBuilder.duration(duration);
            if (material == Silicon) {
                blastBuilder.circuitMeta(1);
            }
            blastBuilder.save(provider);
        }

        // Add Vacuum Freezer recipe if required.
        if (ingotHot.doGenerateItem(material)) {
            int vacuumEUt = property.getVacuumEUtOverride() != -1 ? property.getVacuumEUtOverride() : VA[MV];
            int vacuumDuration = property.getVacuumDurationOverride() != -1 ? property.getVacuumDurationOverride() :
                    (int) material.getMass() * 3;
            if (blastTemp < 5000) {
                VACUUM_RECIPES.recipeBuilder("cool_hot_" + material.getName() + "_ingot")
                        .inputItems(ingotHot, material)
                        .outputItems(ingot, material)
                        .duration(vacuumDuration)
                        .EUt(vacuumEUt)
                        .save(provider);
            } else {
                VACUUM_RECIPES.recipeBuilder("cool_hot_" + material.getName() + "_ingot")
                        .inputItems(ingotHot, material)
                        .inputFluids(Helium.getFluid(FluidStorageKeys.LIQUID, 500))
                        .outputItems(ingot, material)
                        .outputFluids(Helium.getFluid(250))
                        .duration(vacuumDuration)
                        .EUt(vacuumEUt)
                        .save(provider);
            }
        }
    }

    public static void processSmallDust(TagPrefix orePrefix, Material material, DustProperty property,
                                        Consumer<FinishedRecipe> provider) {
        // small dust retains magnetism
        ItemStack smallDustStack = ChemicalHelper.get(orePrefix, material);
        ItemStack dustStack = ChemicalHelper.get(dust, material);

        VanillaRecipeHelper.addStrictShapedRecipe(provider,
                String.format("small_dust_disassembling_%s", material.getName()),
                GTUtil.copyAmount(4, smallDustStack), " X ", "   ", "   ", 'X', new UnificationEntry(dust, material));
        VanillaRecipeHelper.addShapedRecipe(provider, String.format("small_dust_assembling_%s", material.getName()),
                dustStack, "XX", "XX", 'X', new UnificationEntry(orePrefix, material));

        PACKER_RECIPES.recipeBuilder("package_" + material.getName() + "_small_dust")
                .inputItems(orePrefix, material, 4)
                .circuitMeta(1)
                .outputItems(dustStack)
                .save(provider);

        PACKER_RECIPES.recipeBuilder("unpackage_" + material.getName() + "_small_dust")
                .inputItems(dust, material)
                .circuitMeta(2)
                .outputItems(GTUtil.copyAmount(4, smallDustStack))
                .save(provider);
    }

    public static void processTinyDust(TagPrefix orePrefix, Material material, DustProperty property,
                                       Consumer<FinishedRecipe> provider) {
        // tiny dust retains magnetism
        ItemStack tinyDustStack = ChemicalHelper.get(orePrefix, material);
        ItemStack dustStack = ChemicalHelper.get(dust, material);

        VanillaRecipeHelper.addStrictShapedRecipe(provider,
                String.format("tiny_dust_disassembling_%s", material.getName()),
                GTUtil.copyAmount(9, tinyDustStack), "X  ", "   ", "   ", 'X', new UnificationEntry(dust, material));
        VanillaRecipeHelper.addShapedRecipe(provider, String.format("tiny_dust_assembling_%s", material.getName()),
                dustStack, "XXX", "XXX", "XXX", 'X', new UnificationEntry(orePrefix, material));

        PACKER_RECIPES.recipeBuilder("package_" + material.getName() + "_tiny_dust")
                .inputItems(orePrefix, material, 9)
                .circuitMeta(1)
                .outputItems(dustStack)
                .save(provider);

        PACKER_RECIPES.recipeBuilder("unpackage_" + material.getName() + "_tiny_dust")
                .inputItems(dust, material)
                .circuitMeta(1)
                .outputItems(GTUtil.copyAmount(9, tinyDustStack))
                .save(provider);
    }

    public static void processIngot(TagPrefix ingotPrefix, Material material, IngotProperty property,
                                    Consumer<FinishedRecipe> provider) {
        if (material.hasFlag(MORTAR_GRINDABLE)) {
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("mortar_grind_%s", material.getName()),
                    ChemicalHelper.get(dust, material), "X", "m", 'X', new UnificationEntry(ingotPrefix, material));
        }

        var magMaterial = material.hasFlag(IS_MAGNETIC) ?
                material.getProperty(PropertyKey.INGOT).getMacerateInto() : material;

        if (material.hasFlag(GENERATE_ROD)) {
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("stick_%s", material.getName()),
                    ChemicalHelper.get(rod, magMaterial),
                    "f ", " X",
                    'X', new UnificationEntry(ingotPrefix, material));
            if (!material.hasFlag(NO_WORKING)) {
                EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_to_rod")
                        .inputItems(ingotPrefix, material)
                        .notConsumable(GTItems.SHAPE_EXTRUDER_ROD)
                        .outputItems(rod, magMaterial, 2)
                        .duration((int) material.getMass() * 2)
                        .EUt(6L * getVoltageMultiplier(material))
                        .save(provider);
            }
        }

        if (material.hasFluid()) {
            FLUID_SOLIDFICATION_RECIPES.recipeBuilder("solidify_" + material.getName() + "_to_ingot")
                    .notConsumable(GTItems.SHAPE_MOLD_INGOT)
                    .inputFluids(material.getFluid(L))
                    .outputItems(ingotPrefix, material)
                    .duration(20).EUt(VA[ULV])
                    .save(provider);
        }

        if (material.hasFlag(NO_SMASHING)) {
            EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_to_ingot")
                    .inputItems(dust, material)
                    .notConsumable(GTItems.SHAPE_EXTRUDER_INGOT)
                    .outputItems(ingot, magMaterial)
                    .duration(10)
                    .EUt(4L * getVoltageMultiplier(material))
                    .save(provider);
        }

        ALLOY_SMELTER_RECIPES.recipeBuilder("alloy_smelt_" + material.getName() + "_to_nugget")
                .EUt(VA[ULV]).duration((int) material.getMass())
                .inputItems(ingot, material)
                .notConsumable(GTItems.SHAPE_MOLD_NUGGET)
                .outputItems(nugget, magMaterial, 9)
                .category(GTRecipeCategories.INGOT_MOLDING)
                .save(provider);

        if (!ChemicalHelper.get(block, material).isEmpty()) {
            ALLOY_SMELTER_RECIPES.recipeBuilder("alloy_smelt_" + material.getName() + "_to_ingot")
                    .EUt(VA[ULV]).duration((int) material.getMass() * (int) (block.getMaterialAmount(material) / M))
                    .inputItems(block, material)
                    .notConsumable(GTItems.SHAPE_MOLD_INGOT)
                    .outputItems(ingot, magMaterial, (int) (block.getMaterialAmount(material) / M))
                    .category(GTRecipeCategories.INGOT_MOLDING)
                    .save(provider);

            COMPRESSOR_RECIPES.recipeBuilder("compress_" + material.getName() + "_to_block")
                    .EUt(2).duration(300)
                    .inputItems(ingot, material, (int) (block.getMaterialAmount(material) / M))
                    .outputItems(block, magMaterial)
                    .save(provider);
        }

        if (material.hasFlag(GENERATE_PLATE) && !material.hasFlag(NO_WORKING)) {

            if (!material.hasFlag(NO_SMASHING)) {
                ItemStack plateStack = ChemicalHelper.get(plate, material.hasFlag(IS_MAGNETIC) ?
                        material.getProperty(PropertyKey.INGOT).getMacerateInto() : material);
                if (!plateStack.isEmpty()) {
                    BENDER_RECIPES.recipeBuilder("bend_" + material.getName() + "_to_plate")
                            .circuitMeta(1)
                            .inputItems(ingotPrefix, material)
                            .outputItems(plateStack)
                            .EUt(24).duration((int) (material.getMass()))
                            .save(provider);

                    FORGE_HAMMER_RECIPES.recipeBuilder("hammer_" + material.getName() + "_to_plate")
                            .inputItems(ingotPrefix, material, 3)
                            .outputItems(GTUtil.copyAmount(2, plateStack))
                            .EUt(16).duration((int) material.getMass())
                            .save(provider);

                    VanillaRecipeHelper.addShapedRecipe(provider, String.format("plate_%s", material.getName()),
                            plateStack, "h", "I", "I", 'I', new UnificationEntry(ingotPrefix, material));
                }
            }

            int voltageMultiplier = getVoltageMultiplier(material);
            if (!ChemicalHelper.get(plate, material).isEmpty()) {
                EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_to_plate")
                        .inputItems(ingotPrefix, material)
                        .notConsumable(GTItems.SHAPE_EXTRUDER_PLATE)
                        .outputItems(plate, magMaterial)
                        .duration((int) material.getMass())
                        .EUt(8L * voltageMultiplier)
                        .save(provider);

                if (material.hasFlag(NO_SMASHING)) {
                    EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_dust_to_plate")
                            .inputItems(dust, material)
                            .notConsumable(GTItems.SHAPE_EXTRUDER_PLATE)
                            .outputItems(plate, magMaterial)
                            .duration((int) material.getMass())
                            .EUt(8L * voltageMultiplier)
                            .save(provider);
                }
            }
        }
    }

    public static void processGemConversion(TagPrefix gemPrefix, Material material, GemProperty property,
                                            Consumer<FinishedRecipe> provider) {
        long materialAmount = gemPrefix.getMaterialAmount(material);
        ItemStack crushedStack = ChemicalHelper.getDust(material, materialAmount);

        if (material.hasFlag(MORTAR_GRINDABLE)) {
            VanillaRecipeHelper.addShapedRecipe(provider,
                    String.format("gem_to_dust_%s_%s", material.getName(),
                            FormattingUtil.toLowerCaseUnder(gemPrefix.name)),
                    crushedStack,
                    "X", "m", 'X', new UnificationEntry(gemPrefix, material));
        }

        TagPrefix prevPrefix = GTUtil.getItem(GEM_ORDER, GEM_ORDER.indexOf(gemPrefix) - 1, null);
        ItemStack prevStack = prevPrefix == null ? ItemStack.EMPTY : ChemicalHelper.get(prevPrefix, material, 2);
        if (!prevStack.isEmpty() && prevPrefix != null) {
            VanillaRecipeHelper.addShapelessRecipe(provider,
                    String.format("gem_to_gem_%s_%s", FormattingUtil.toLowerCaseUnder(prevPrefix.name),
                            material.getName()),
                    prevStack,
                    'h', new UnificationEntry(gemPrefix, material));

            CUTTER_RECIPES
                    .recipeBuilder("cut_" + material.getName() + "_" + FormattingUtil.toLowerCaseUnder(gemPrefix.name) +
                            "_to_" + FormattingUtil.toLowerCaseUnder(prevPrefix.name))
                    .inputItems(gemPrefix, material)
                    .outputItems(prevStack)
                    .duration(20)
                    .EUt(16)
                    .save(provider);

            LASER_ENGRAVER_RECIPES
                    .recipeBuilder(
                            "engrave_" + material.getName() + "_" + FormattingUtil.toLowerCaseUnder(gemPrefix.name) +
                                    "_to_" + FormattingUtil.toLowerCaseUnder(prevPrefix.name))
                    .inputItems(prevStack)
                    .notConsumable(lens, MarkerMaterials.Color.White)
                    .outputItems(gemPrefix, material)
                    .duration(300)
                    .EUt(240)
                    .save(provider);
        }
    }

    public static void processNugget(TagPrefix orePrefix, Material material, DustProperty property,
                                     Consumer<FinishedRecipe> provider) {
        ItemStack nuggetStack = ChemicalHelper.get(orePrefix, material);
        if (material.hasProperty(PropertyKey.INGOT)) {
            ItemStack ingotStack = ChemicalHelper.get(ingot, material.hasFlag(IS_MAGNETIC) ?
                    material.getProperty(PropertyKey.INGOT).getMacerateInto() : material);

            if (!ConfigHolder.INSTANCE.recipes.disableManualCompression) {
                if (!ingot.isIgnored(material)) {
                    VanillaRecipeHelper.addShapelessRecipe(provider,
                            String.format("nugget_disassembling_%s", material.getName()),
                            GTUtil.copyAmount(9, nuggetStack), new UnificationEntry(ingot, material));
                }
                if (!orePrefix.isIgnored(material)) {
                    VanillaRecipeHelper.addShapedRecipe(provider,
                            String.format("nugget_assembling_%s", material.getName()),
                            ingotStack, "XXX", "XXX", "XXX", 'X', new UnificationEntry(orePrefix, material));
                }
            }

            COMPRESSOR_RECIPES.recipeBuilder("compress_" + material.getName() + "_nugget_to_ingot")
                    .inputItems(nugget, material, 9)
                    .outputItems(ingotStack)
                    .EUt(2).duration(300).save(provider);

            ALLOY_SMELTER_RECIPES.recipeBuilder("alloy_smelt_" + material.getName() + "_nugget_to_ingot")
                    .EUt(VA[ULV]).duration((int) material.getMass())
                    .inputItems(nugget, material, 9)
                    .notConsumable(GTItems.SHAPE_MOLD_INGOT)
                    .outputItems(ingotStack)
                    .category(GTRecipeCategories.INGOT_MOLDING)
                    .save(provider);

            if (material.hasFluid()) {
                FLUID_SOLIDFICATION_RECIPES.recipeBuilder("solidify_" + material.getName() + "_to_nugget")
                        .notConsumable(GTItems.SHAPE_MOLD_NUGGET)
                        .inputFluids(material.getFluid(L))
                        .outputItems(orePrefix, material, 9)
                        .duration((int) material.getMass())
                        .EUt(VA[ULV])
                        .save(provider);
            }
        } else if (material.hasProperty(PropertyKey.GEM)) {
            ItemStack gemStack = ChemicalHelper.get(gem, material);

            if (!ConfigHolder.INSTANCE.recipes.disableManualCompression) {
                if (!gem.isIgnored(material)) {
                    VanillaRecipeHelper.addShapelessRecipe(provider,
                            String.format("nugget_disassembling_%s", material.getName()),
                            GTUtil.copyAmount(9, nuggetStack), new UnificationEntry(gem, material));
                }
                if (!orePrefix.isIgnored(material)) {
                    VanillaRecipeHelper.addShapedRecipe(provider,
                            String.format("nugget_assembling_%s", material.getName()),
                            gemStack, "XXX", "XXX", "XXX", 'X', new UnificationEntry(orePrefix, material));
                }
            }
        }
    }

    public static void processFrame(TagPrefix framePrefix, Material material, DustProperty property,
                                    Consumer<FinishedRecipe> provider) {
        if (material.hasFlag(GENERATE_FRAME)) {
            boolean isWoodenFrame = material.hasProperty(PropertyKey.WOOD);
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("frame_%s", material.getName()),
                    ChemicalHelper.get(framePrefix, material, 2),
                    "SSS", isWoodenFrame ? "SsS" : "SwS", "SSS",
                    'S', new UnificationEntry(rod, material));

            ASSEMBLER_RECIPES.recipeBuilder("assemble_" + material.getName() + "_frame")
                    .inputItems(rod, material, 4)
                    .circuitMeta(4)
                    .outputItems(framePrefix, material)
                    .EUt(VA[ULV]).duration(64)
                    .save(provider);
        }
    }

    public static void processBlock(TagPrefix blockPrefix, Material material, DustProperty property,
                                    Consumer<FinishedRecipe> provider) {
        ItemStack blockStack = ChemicalHelper.get(blockPrefix, material.hasFlag(IS_MAGNETIC) ?
                material.getProperty(PropertyKey.INGOT).getMacerateInto() : material);
        long materialAmount = blockPrefix.getMaterialAmount(material);
        if (material.hasFluid()) {
            FLUID_SOLIDFICATION_RECIPES.recipeBuilder("solidify_" + material.getName() + "_block")
                    .notConsumable(GTItems.SHAPE_MOLD_BLOCK)
                    .inputFluids(material.getFluid((int) (materialAmount * L / M)))
                    .outputItems(blockStack)
                    .duration((int) material.getMass()).EUt(VA[ULV])
                    .save(provider);
        }

        if (material.hasFlag(GENERATE_PLATE)) {
            ItemStack plateStack = ChemicalHelper.get(plate, material.hasFlag(IS_MAGNETIC) ?
                    material.getProperty(PropertyKey.INGOT).getMacerateInto() : material);
            if (!plateStack.isEmpty()) {
                CUTTER_RECIPES.recipeBuilder("cut_" + material.getName() + "_block_to_plate")
                        .inputItems(blockPrefix, material)
                        .outputItems(GTUtil.copyAmount((int) (materialAmount / M), plateStack))
                        .duration((int) (material.getMass() * 8L)).EUt(VA[LV])
                        .save(provider);
            }
        }

        UnificationEntry blockEntry;
        if (material.hasProperty(PropertyKey.GEM)) {
            blockEntry = new UnificationEntry(gem, material);
        } else if (material.hasProperty(PropertyKey.INGOT)) {
            blockEntry = new UnificationEntry(ingot, material);
        } else {
            blockEntry = new UnificationEntry(dust, material);
        }

        // do not allow handcrafting or uncrafting, extruding or alloy smelting of blacklisted blocks
        if (!material.hasFlag(EXCLUDE_BLOCK_CRAFTING_RECIPES)) {

            // do not allow non-perfect square root material amounts
            int size = (int) (materialAmount / M);
            int sizeSqrt = Math.round(Mth.sqrt(size));
            // do not allow handcrafting or uncrafting of blacklisted blocks
            if (!material.hasFlag(EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES) &&
                    !ConfigHolder.INSTANCE.recipes.disableManualCompression && sizeSqrt * sizeSqrt == size &&
                    !block.isIgnored(material)) {
                String patternString = "B".repeat(Math.max(0, sizeSqrt));
                String[] pattern = new String[sizeSqrt];
                Arrays.fill(pattern, patternString);
                VanillaRecipeHelper.addShapedRecipe(provider, String.format("block_compress_%s", material.getName()),
                        blockStack, pattern, 'B', blockEntry);

                VanillaRecipeHelper.addShapelessRecipe(provider,
                        String.format("block_decompress_%s", material.getName()),
                        GTUtil.copyAmount(size, ChemicalHelper.get(blockEntry.tagPrefix, blockEntry.material)),
                        new UnificationEntry(blockPrefix, material));
            }

            if (material.hasProperty(PropertyKey.INGOT)) {
                int voltageMultiplier = getVoltageMultiplier(material);
                EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_ingot_to_block")
                        .inputItems(ingot, material, (int) (materialAmount / M))
                        .notConsumable(GTItems.SHAPE_EXTRUDER_BLOCK)
                        .outputItems(blockStack)
                        .duration(10).EUt(8L * voltageMultiplier)
                        .save(provider);

                ALLOY_SMELTER_RECIPES.recipeBuilder("alloy_smelt_" + material.getName() + "_ingot_to_block")
                        .inputItems(ingot, material, (int) (materialAmount / M))
                        .notConsumable(GTItems.SHAPE_MOLD_BLOCK)
                        .outputItems(blockStack)
                        .duration(5).EUt(4L * voltageMultiplier)
                        .category(GTRecipeCategories.INGOT_MOLDING)
                        .save(provider);
            } else if (material.hasProperty(PropertyKey.GEM)) {
                COMPRESSOR_RECIPES.recipeBuilder("compress_" + material.getName() + "_gem_to_block")
                        .inputItems(gem, material, (int) (block.getMaterialAmount(material) / M))
                        .outputItems(block, material)
                        .duration(300).EUt(2).save(provider);

                FORGE_HAMMER_RECIPES.recipeBuilder("hammer_" + material.getName() + "_block_to_gem")
                        .inputItems(block, material)
                        .outputItems(gem, material, (int) (block.getMaterialAmount(material) / M))
                        .duration(100).EUt(24).save(provider);
            }
        }
    }

    private static void generateSurfaceRockRecipe(Consumer<FinishedRecipe> provider) {
        for (MaterialRegistry registry : GTCEuAPI.materialManager.getRegistries()) {
            for (Material material : registry.getAllMaterials()) {
                if (material.hasProperty(PropertyKey.ORE)) {
                    VanillaRecipeHelper.addShapedRecipe(provider, "%s_surface_indicator".formatted(material.getName()),
                            GTBlocks.SURFACE_ROCK_BLOCKS.get(material).asStack(2),
                            "DDD", "DGD", "DDD",
                            'D', ChemicalHelper.get(dustSmall, material),
                            'G', Items.GRAVEL);
                }
            }
        }
    }

    private static int getVoltageMultiplier(Material material) {
        return material.getBlastTemperature() >= 2800 ? VA[LV] : VA[ULV];
    }
}
