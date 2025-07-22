package com.gregtechceu.gtceu.data.recipe.generated;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterials;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.*;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public final class MaterialRecipeHandler {

    private MaterialRecipeHandler() {}

    public static void run(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        processIngot(provider, material);
        processNugget(provider, material);
        processBlock(provider, material);
        processFrame(provider, material);
        processDust(provider, material);
        processSmallDust(provider, material);
        processTinyDust(provider, material);
        if (ConfigHolder.INSTANCE.recipes.generateLowQualityGems) {
            processGemConversion(provider, gemChipped, null, material);
            processGemConversion(provider, gemFlawed, gemChipped, material);
            processGemConversion(provider, gem, gemFlawed, material);
        } else {
            processGemConversion(provider, gem, null, material);
        }
        processGemConversion(provider, gemFlawless, gem, material);
        processGemConversion(provider, gemExquisite, gemFlawless, material);

        generateSurfaceRockRecipe(provider, material);
    }

    private static void processDust(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(dust) || !material.hasProperty(PropertyKey.DUST)) {
            return;
        }

        String id = "dust_%s".formatted(material.getName().toLowerCase(Locale.ROOT));
        ItemStack dustStack = ChemicalHelper.get(dust, material);
        OreProperty oreProperty = material.hasProperty(PropertyKey.ORE) ? material.getProperty(PropertyKey.ORE) : null;
        if (material.hasProperty(PropertyKey.GEM)) {
            ItemStack gemStack = ChemicalHelper.get(gem, material);

            if (material.hasFlag(CRYSTALLIZABLE)) {
                AUTOCLAVE_RECIPES.recipeBuilder("autoclave_" + id + "_water")
                        .inputItems(dustStack)
                        .inputFluids(Water.getFluid(250))
                        .chancedOutput(gemStack, 7500, 0)
                        .duration(1200).EUt(24)
                        .save(provider);

                AUTOCLAVE_RECIPES.recipeBuilder("autoclave_" + id + "_distilled")
                        .inputItems(dustStack)
                        .inputFluids(DistilledWater.getFluid(50))
                        .outputItems(gemStack)
                        .duration(600).EUt(24)
                        .save(provider);
            }

            if (!material.hasFlag(EXPLOSIVE) && !material.hasFlag(FLAMMABLE)) {
                IMPLOSION_RECIPES.recipeBuilder("implode_" + id + "_powderbarrel")
                        .inputItems(dustStack.copyWithCount(4))
                        .outputItems(gemStack.copyWithCount(3))
                        .chancedOutput(dust, GTMaterials.DarkAsh, 2500, 0)
                        .explosivesType(new ItemStack(GTBlocks.POWDERBARREL, 8))
                        .save(provider);

                IMPLOSION_RECIPES.recipeBuilder("implode_" + id + "_tnt")
                        .inputItems(dustStack.copyWithCount(4))
                        .outputItems(gemStack.copyWithCount(3))
                        .chancedOutput(dust, GTMaterials.DarkAsh, 2500, 0)
                        .explosivesAmount(4)
                        .save(provider);

                IMPLOSION_RECIPES.recipeBuilder("implode_" + id + "_dynamite")
                        .inputItems(dustStack.copyWithCount(4))
                        .outputItems(gemStack.copyWithCount(3))
                        .chancedOutput(dust, GTMaterials.DarkAsh, 2500, 0)
                        .explosivesType(GTItems.DYNAMITE.asStack(2))
                        .save(provider);

                IMPLOSION_RECIPES.recipeBuilder("implode_" + id + "_itnt")
                        .inputItems(dustStack.copyWithCount(4))
                        .outputItems(gemStack.copyWithCount(3))
                        .chancedOutput(dust, GTMaterials.DarkAsh, 2500, 0)
                        .explosivesType(new ItemStack(GTBlocks.INDUSTRIAL_TNT))
                        .save(provider);
            }

            if (oreProperty != null) {
                Material smeltingResult = oreProperty.getDirectSmeltResult();
                if (!smeltingResult.isNull()) {
                    VanillaRecipeHelper.addSmeltingRecipe(provider, id + "_ingot",
                            ChemicalHelper.getTag(dust, material), ChemicalHelper.get(ingot, smeltingResult));
                }
            }

        } else if (material.hasProperty(PropertyKey.INGOT)) {
            if (!material.hasAnyOfFlags(FLAMMABLE, NO_SMELTING)) {

                boolean hasHotIngot = ingotHot.doGenerateItem(material);
                ItemStack ingotStack = ChemicalHelper.get(hasHotIngot ? ingotHot : ingot, material);
                if (ingotStack.isEmpty() && oreProperty != null) {
                    Material smeltingResult = oreProperty.getDirectSmeltResult();
                    if (!smeltingResult.isNull()) {
                        ingotStack = ChemicalHelper.get(ingot, smeltingResult);
                    }
                }
                int blastTemp = material.getBlastTemperature();

                if (blastTemp <= 0) {
                    // smelting magnetic dusts is handled elsewhere
                    if (!material.hasFlag(IS_MAGNETIC)) {
                        // do not register inputs by tag prefix here. Let other mods register their own dust -> ingots
                        VanillaRecipeHelper.addSmeltingRecipe(provider, "smelt_" + id + "_to_ingot",
                                ChemicalHelper.getTag(dust, material), ingotStack);
                    }
                } else {
                    IngotProperty ingotProperty = material.getProperty(PropertyKey.INGOT);
                    BlastProperty blastProperty = material.getProperty(PropertyKey.BLAST);

                    processEBFRecipe(material, blastProperty, ingotStack, provider);

                    if (!ingotProperty.getMagneticMaterial().isNull()) {
                        processEBFRecipe(ingotProperty.getMagneticMaterial(), blastProperty, ingotStack, provider);
                    }
                }
            }
        } else {
            if (material.hasFlag(GENERATE_PLATE) && !material.hasFlag(EXCLUDE_PLATE_COMPRESSOR_RECIPE)) {
                COMPRESSOR_RECIPES.recipeBuilder("compress_plate_" + id)
                        .inputItems(dustStack)
                        .outputItems(plate, material)
                        .save(provider);
            }

            // Some Ores with Direct Smelting Results have neither ingot nor gem properties
            if (oreProperty != null) {
                Material smeltingResult = oreProperty.getDirectSmeltResult();
                if (!smeltingResult.isNull()) {
                    ItemStack ingotStack = ChemicalHelper.get(ingot, smeltingResult);
                    if (!ingotStack.isEmpty()) {
                        VanillaRecipeHelper.addSmeltingRecipe(provider, "smelt_" + id + "_to_ingot",
                                ChemicalHelper.getTag(dust, material), ingotStack);
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
            FluidIngredient gas = gasTier.getFluid();

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

        AlloyBlastProperty alloyBlastProperty = material.getProperty(PropertyKey.ALLOY_BLAST);
        if (alloyBlastProperty != null) {
            alloyBlastProperty.getRecipeProducer().produce(material, property, provider);
        }
    }

    private static void processSmallDust(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(dustSmall) || !material.hasProperty(PropertyKey.DUST)) {
            return;
        }

        // small dust retains magnetism
        ItemStack smallDustStack = ChemicalHelper.get(dustSmall, material);
        ItemStack dustStack = ChemicalHelper.get(dust, material);

        VanillaRecipeHelper.addStrictShapedRecipe(provider,
                String.format("small_dust_disassembling_%s", material.getName()),
                smallDustStack.copyWithCount(4), " X", "  ", 'X', new MaterialEntry(dust, material));
        VanillaRecipeHelper.addShapedRecipe(provider, String.format("small_dust_assembling_%s", material.getName()),
                dustStack, "XX", "XX", 'X', new MaterialEntry(dustSmall, material));

        PACKER_RECIPES.recipeBuilder("package_" + material.getName() + "_small_dust")
                .inputItems(dustSmall, material, 4)
                .circuitMeta(1)
                .outputItems(dustStack)
                .save(provider);

        PACKER_RECIPES.recipeBuilder("unpackage_" + material.getName() + "_small_dust")
                .inputItems(dust, material)
                .circuitMeta(2)
                .outputItems(smallDustStack.copyWithCount(4))
                .save(provider);
    }

    private static void processTinyDust(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(dustTiny) || !material.hasProperty(PropertyKey.DUST)) {
            return;
        }

        // tiny dust retains magnetism
        ItemStack tinyDustStack = ChemicalHelper.get(dustTiny, material);
        ItemStack dustStack = ChemicalHelper.get(dust, material);

        VanillaRecipeHelper.addStrictShapedRecipe(provider,
                String.format("tiny_dust_disassembling_%s", material.getName()),
                tinyDustStack.copyWithCount(9), "X ", "  ", 'X', new MaterialEntry(dust, material));
        VanillaRecipeHelper.addShapedRecipe(provider, String.format("tiny_dust_assembling_%s", material.getName()),
                dustStack, "XXX", "XXX", "XXX", 'X', new MaterialEntry(dustTiny, material));

        PACKER_RECIPES.recipeBuilder("package_" + material.getName() + "_tiny_dust")
                .inputItems(dustTiny, material, 9)
                .circuitMeta(1)
                .outputItems(dustStack)
                .save(provider);

        PACKER_RECIPES.recipeBuilder("unpackage_" + material.getName() + "_tiny_dust")
                .inputItems(dust, material)
                .circuitMeta(1)
                .outputItems(tinyDustStack.copyWithCount(9))
                .save(provider);
    }

    private static void processIngot(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(ingot) || !material.hasProperty(PropertyKey.INGOT)) {
            return;
        }

        if (material.hasFlag(MORTAR_GRINDABLE)) {
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("mortar_grind_%s", material.getName()),
                    ChemicalHelper.get(dust, material), "X", "m", 'X', new MaterialEntry(ingot, material));
        }

        var magMaterial = material.hasFlag(IS_MAGNETIC) ?
                material.getProperty(PropertyKey.INGOT).getMacerateInto() : material;

        if (material.hasFlag(GENERATE_ROD)) {
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("stick_%s", material.getName()),
                    ChemicalHelper.get(rod, magMaterial),
                    "f ", " X",
                    'X', new MaterialEntry(ingot, material));
            if (!material.hasFlag(NO_WORKING)) {
                EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_to_rod")
                        .inputItems(ingot, material)
                        .notConsumable(GTItems.SHAPE_EXTRUDER_ROD)
                        .outputItems(rod, magMaterial, 2)
                        .duration((int) material.getMass() * 2)
                        .EUt(6L * getVoltageMultiplier(material))
                        .save(provider);
            }
        }

        if (material.hasFluid()) {
            FluidStack stack = material.getProperty(PropertyKey.FLUID).solidifiesFrom(L);
            if (!stack.isEmpty()) {
                FLUID_SOLIDFICATION_RECIPES.recipeBuilder("solidify_" + material.getName() + "_to_ingot")
                        .notConsumable(GTItems.SHAPE_MOLD_INGOT)
                        .inputFluids(stack)
                        .outputItems(ingot, material)
                        .duration(20).EUt(VA[ULV])
                        .save(provider);
            }
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
                            .inputItems(ingot, material)
                            .outputItems(plateStack)
                            .EUt(24).duration((int) (material.getMass()))
                            .save(provider);

                    FORGE_HAMMER_RECIPES.recipeBuilder("hammer_" + material.getName() + "_to_plate")
                            .inputItems(ingot, material, 3)
                            .outputItems(plateStack.copyWithCount(2))
                            .EUt(16).duration((int) material.getMass())
                            .save(provider);

                    VanillaRecipeHelper.addShapedRecipe(provider, String.format("plate_%s", material.getName()),
                            plateStack, "h", "I", "I", 'I', new MaterialEntry(ingot, material));
                }
            }

            int voltageMultiplier = getVoltageMultiplier(material);
            if (!ChemicalHelper.get(plate, material).isEmpty()) {
                EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_to_plate")
                        .inputItems(ingot, material)
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

    private static void processGemConversion(@NotNull Consumer<FinishedRecipe> provider, @NotNull TagPrefix prefix,
                                             @Nullable TagPrefix lowerPrefix, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(prefix) || !material.hasProperty(PropertyKey.GEM)) {
            return;
        }

        long materialAmount = prefix.getMaterialAmount(material);
        ItemStack crushedStack = ChemicalHelper.getDust(material, materialAmount);

        if (material.hasFlag(MORTAR_GRINDABLE)) {
            VanillaRecipeHelper.addShapedRecipe(provider,
                    String.format("gem_to_dust_%s_%s", material.getName(),
                            FormattingUtil.toLowerCaseUnderscore(prefix.name)),
                    crushedStack,
                    "X", "m", 'X', new MaterialEntry(prefix, material));
        }

        if (lowerPrefix == null) {
            return;
        }

        ItemStack prevStack = ChemicalHelper.get(lowerPrefix, material, 2);
        if (prevStack.isEmpty()) {
            return;
        }

        VanillaRecipeHelper.addShapelessRecipe(provider,
                String.format("gem_to_gem_%s_%s", FormattingUtil.toLowerCaseUnderscore(lowerPrefix.name),
                        material.getName()),
                prevStack,
                'h', new MaterialEntry(prefix, material));

        CUTTER_RECIPES
                .recipeBuilder("cut_" + material.getName() + "_" + FormattingUtil.toLowerCaseUnderscore(prefix.name) +
                        "_to_" + FormattingUtil.toLowerCaseUnderscore(lowerPrefix.name))
                .inputItems(prefix, material)
                .outputItems(prevStack)
                .duration(20)
                .EUt(16)
                .save(provider);

        LASER_ENGRAVER_RECIPES
                .recipeBuilder(
                        "engrave_" + material.getName() + "_" + FormattingUtil.toLowerCaseUnderscore(prefix.name) +
                                "_to_" + FormattingUtil.toLowerCaseUnderscore(lowerPrefix.name))
                .inputItems(prevStack)
                .notConsumable(lens, MarkerMaterials.Color.White)
                .outputItems(prefix, material)
                .duration(300)
                .EUt(240)
                .save(provider);
    }

    private static void processNugget(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(nugget) || !material.hasProperty(PropertyKey.DUST)) {
            return;
        }

        ItemStack nuggetStack = ChemicalHelper.get(nugget, material);
        if (material.hasProperty(PropertyKey.INGOT)) {
            ItemStack ingotStack = ChemicalHelper.get(ingot, material.hasFlag(IS_MAGNETIC) ?
                    material.getProperty(PropertyKey.INGOT).getMacerateInto() : material);

            if (!ConfigHolder.INSTANCE.recipes.disableManualCompression) {
                if (!ingot.isIgnored(material)) {
                    VanillaRecipeHelper.addShapelessRecipe(provider,
                            String.format("nugget_disassembling_%s", material.getName()),
                            nuggetStack.copyWithCount(9), new MaterialEntry(ingot, material));
                }
                if (!nugget.isIgnored(material)) {
                    VanillaRecipeHelper.addShapedRecipe(provider,
                            String.format("nugget_assembling_%s", material.getName()),
                            ingotStack, "XXX", "XXX", "XXX", 'X', new MaterialEntry(nugget, material));
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
                FluidStack stack = material.getProperty(PropertyKey.FLUID).solidifiesFrom(L);
                if (!stack.isEmpty()) {
                    FLUID_SOLIDFICATION_RECIPES.recipeBuilder("solidify_" + material.getName() + "_to_nugget")
                            .notConsumable(GTItems.SHAPE_MOLD_NUGGET)
                            .inputFluids(stack)
                            .outputItems(nugget, material, 9)
                            .duration((int) material.getMass())
                            .EUt(VA[ULV])
                            .save(provider);
                }
            }
        } else if (material.hasProperty(PropertyKey.GEM)) {
            ItemStack gemStack = ChemicalHelper.get(gem, material);

            if (!ConfigHolder.INSTANCE.recipes.disableManualCompression) {
                if (!gem.isIgnored(material)) {
                    VanillaRecipeHelper.addShapelessRecipe(provider,
                            String.format("nugget_disassembling_%s", material.getName()),
                            nuggetStack.copyWithCount(9), new MaterialEntry(gem, material));
                }
                if (!nugget.isIgnored(material)) {
                    VanillaRecipeHelper.addShapedRecipe(provider,
                            String.format("nugget_assembling_%s", material.getName()),
                            gemStack, "XXX", "XXX", "XXX", 'X', new MaterialEntry(nugget, material));
                }
            }
        }
    }

    private static void processFrame(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(frameGt) || !material.hasProperty(PropertyKey.DUST)) {
            return;
        }

        if (material.hasFlag(GENERATE_FRAME)) {
            boolean isWoodenFrame = material.hasProperty(PropertyKey.WOOD);
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("frame_%s", material.getName()),
                    ChemicalHelper.get(frameGt, material, 2),
                    "SSS", isWoodenFrame ? "SsS" : "SwS", "SSS",
                    'S', new MaterialEntry(rod, material));

            ASSEMBLER_RECIPES.recipeBuilder("assemble_" + material.getName() + "_frame")
                    .inputItems(rod, material, 4)
                    .circuitMeta(4)
                    .outputItems(frameGt, material)
                    .EUt(VA[ULV]).duration(64)
                    .save(provider);
        }
    }

    private static void processBlock(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(block) || !material.hasProperty(PropertyKey.DUST)) {
            return;
        }

        ItemStack blockStack = ChemicalHelper.get(block, material.hasFlag(IS_MAGNETIC) ?
                material.getProperty(PropertyKey.INGOT).getMacerateInto() : material);
        long materialAmount = block.getMaterialAmount(material);
        if (material.hasFluid()) {
            FluidStack stack = material.getProperty(PropertyKey.FLUID).solidifiesFrom((int) (materialAmount * L / M));
            if (!stack.isEmpty()) {
                FLUID_SOLIDFICATION_RECIPES.recipeBuilder("solidify_" + material.getName() + "_block")
                        .notConsumable(GTItems.SHAPE_MOLD_BLOCK)
                        .inputFluids(stack)
                        .outputItems(blockStack)
                        .duration((int) material.getMass()).EUt(VA[ULV])
                        .save(provider);
            }
        }

        if (material.hasFlag(GENERATE_PLATE)) {
            ItemStack plateStack = ChemicalHelper.get(plate, material.hasFlag(IS_MAGNETIC) ?
                    material.getProperty(PropertyKey.INGOT).getMacerateInto() : material);
            if (!plateStack.isEmpty()) {
                CUTTER_RECIPES.recipeBuilder("cut_" + material.getName() + "_block_to_plate")
                        .inputItems(block, material)
                        .outputItems(plateStack.copyWithCount((int) (materialAmount / M)))
                        .duration((int) (material.getMass() * 8L)).EUt(VA[LV])
                        .save(provider);
            }
        }

        MaterialEntry blockEntry;
        if (material.hasProperty(PropertyKey.GEM)) {
            blockEntry = new MaterialEntry(gem, material);
        } else if (material.hasProperty(PropertyKey.INGOT)) {
            blockEntry = new MaterialEntry(ingot, material);
        } else {
            blockEntry = new MaterialEntry(dust, material);
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
                        ChemicalHelper.get(blockEntry.tagPrefix(), blockEntry.material()).copyWithCount(size),
                        new MaterialEntry(block, material));
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

                Material nonMagneticMaterial = material.hasFlag(IS_MAGNETIC) ?
                        material.getProperty(PropertyKey.INGOT).getSmeltingInto() : material;
                if (!nonMagneticMaterial.hasProperty(PropertyKey.BLAST)) {
                    ALLOY_SMELTER_RECIPES.recipeBuilder("alloy_smelt_" + material.getName() + "_dust_to_block")
                            .inputItems(dust, material, (int) (materialAmount / M))
                            .notConsumable(GTItems.SHAPE_MOLD_BLOCK)
                            .outputItems(blockStack)
                            .duration(20).EUt(4L * voltageMultiplier)
                            .category(GTRecipeCategories.INGOT_MOLDING)
                            .save(provider);
                }
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

    private static void generateSurfaceRockRecipe(@NotNull Consumer<FinishedRecipe> provider,
                                                  @NotNull Material material) {
        if (material.hasProperty(PropertyKey.ORE)) {
            VanillaRecipeHelper.addShapedRecipe(provider, "%s_surface_indicator".formatted(material.getName()),
                    GTMaterialBlocks.SURFACE_ROCK_BLOCKS.get(material).asStack(2),
                    "DDD", "DGD", "DDD",
                    'D', ChemicalHelper.get(dustSmall, material),
                    'G', Items.GRAVEL);
        }
    }

    private static int getVoltageMultiplier(Material material) {
        return material.getBlastTemperature() >= 2800 ? VA[LV] : VA[ULV];
    }
}
