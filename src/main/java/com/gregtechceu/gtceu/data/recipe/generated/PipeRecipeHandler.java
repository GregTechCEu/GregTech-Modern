package com.gregtechceu.gtceu.data.recipe.generated;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.pipelike.duct.DuctPipeType;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.NO_SMASHING;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public final class PipeRecipeHandler {

    private PipeRecipeHandler() {}

    public static void run(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        processPipeTiny(provider, PropertyKey.FLUID_PIPE, pipeTinyFluid, material);
        processPipeSmall(provider, PropertyKey.FLUID_PIPE, pipeSmallFluid, material);
        processPipeNormal(provider, PropertyKey.FLUID_PIPE, pipeNormalFluid, material);
        processPipeLarge(provider, PropertyKey.FLUID_PIPE, pipeLargeFluid, material);
        processPipeHuge(provider, PropertyKey.FLUID_PIPE, pipeHugeFluid, material);
        processPipeQuadruple(provider, PropertyKey.FLUID_PIPE, pipeQuadrupleFluid, material);
        processPipeNonuple(provider, PropertyKey.FLUID_PIPE, pipeNonupleFluid, material);

        processPipeSmall(provider, PropertyKey.ITEM_PIPE, pipeSmallItem, material);
        processPipeNormal(provider, PropertyKey.ITEM_PIPE, pipeNormalItem, material);
        processPipeLarge(provider, PropertyKey.ITEM_PIPE, pipeLargeItem, material);
        processPipeHuge(provider, PropertyKey.ITEM_PIPE, pipeHugeItem, material);
        processRestrictivePipe(provider, PropertyKey.ITEM_PIPE, pipeSmallRestrictive, pipeSmallItem, material);
        processRestrictivePipe(provider, PropertyKey.ITEM_PIPE, pipeNormalRestrictive, pipeNormalItem, material);
        processRestrictivePipe(provider, PropertyKey.ITEM_PIPE, pipeLargeRestrictive, pipeLargeItem, material);
        processRestrictivePipe(provider, PropertyKey.ITEM_PIPE, pipeHugeRestrictive, pipeHugeItem, material);

        addDuctRecipes(provider, Steel, 2);
        addDuctRecipes(provider, StainlessSteel, 4);
        addDuctRecipes(provider, TungstenSteel, 8);
    }

    private static void processRestrictivePipe(@NotNull Consumer<FinishedRecipe> provider,
                                               @NotNull PropertyKey<?> propertyKey,
                                               @NotNull TagPrefix prefix, @NotNull TagPrefix unrestrictive,
                                               @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(prefix) || !material.hasProperty(propertyKey)) {
            return;
        }

        ASSEMBLER_RECIPES.recipeBuilder("assemble_" + material.getName() + "_" + prefix.name)
                .inputItems(unrestrictive, material)
                .inputItems(ring, Iron, 2)
                .outputItems(prefix, material)
                .duration(20)
                .EUt(VA[ULV])
                .save(provider);

        VanillaRecipeHelper.addShapedRecipe(provider,
                FormattingUtil.toLowerCaseUnderscore(prefix + "_" + material.getName()),
                ChemicalHelper.get(prefix, material), "PR", "Rh",
                'P', new MaterialEntry(unrestrictive, material), 'R', ChemicalHelper.get(ring, Iron));
    }

    private static void processPipeTiny(@NotNull Consumer<FinishedRecipe> provider, @NotNull PropertyKey<?> propertyKey,
                                        @NotNull TagPrefix prefix, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(prefix) || !material.hasProperty(propertyKey)) {
            return;
        }

        if (material.hasProperty(PropertyKey.WOOD)) return;
        ItemStack pipeStack = ChemicalHelper.get(prefix, material);
        EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_tiny_pipe")
                .inputItems(ingot, material, 1)
                .notConsumable(GTItems.SHAPE_EXTRUDER_PIPE_TINY)
                .outputItems(pipeStack.copyWithCount(2))
                .duration((int) (material.getMass()))
                .EUt(6L * getVoltageMultiplier(material))
                .save(provider);

        if (material.hasFluid()) {
            FLUID_SOLIDFICATION_RECIPES.recipeBuilder("solidify_" + material.getName() + "_to_tiny_pipe")
                    .notConsumable(GTItems.SHAPE_MOLD_TINY_PIPE)
                    .inputFluids(material.getFluid(L / 2))
                    .outputItems(pipeStack)
                    .duration((int) (material.getMass()) / 2)
                    .EUt(6L * getVoltageMultiplier(material))
                    .save(provider);
        }
        if (material.hasFlag(NO_SMASHING)) {
            EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_tiny_pipe_dust")
                    .inputItems(dust, material, 1)
                    .notConsumable(GTItems.SHAPE_EXTRUDER_PIPE_TINY)
                    .outputItems(pipeStack.copyWithCount(2))
                    .duration((int) (material.getMass()))
                    .EUt(6L * getVoltageMultiplier(material))
                    .save(provider);
        } else if (plate.doGenerateItem(material)) {
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("tiny_%s_pipe", material.getName()),
                    pipeStack.copyWithCount(2), " s ", "hXw",
                    'X', new MaterialEntry(plate, material));
        }
    }

    private static void processPipeSmall(@NotNull Consumer<FinishedRecipe> provider,
                                         @NotNull PropertyKey<?> propertyKey,
                                         @NotNull TagPrefix prefix, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(prefix) || !material.hasProperty(propertyKey)) {
            return;
        }

        if (material.hasProperty(PropertyKey.WOOD)) return;
        ItemStack pipeStack = ChemicalHelper.get(prefix, material);
        EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_small_pipe")
                .inputItems(ingot, material, 1)
                .notConsumable(GTItems.SHAPE_EXTRUDER_PIPE_SMALL)
                .outputItems(pipeStack)
                .duration((int) (material.getMass()))
                .EUt(6L * getVoltageMultiplier(material))
                .save(provider);

        if (material.hasFluid()) {
            FLUID_SOLIDFICATION_RECIPES.recipeBuilder("solidify_" + material.getName() + "_to_small_pipe")
                    .notConsumable(GTItems.SHAPE_MOLD_SMALL_PIPE)
                    .inputFluids(material.getFluid(L))
                    .outputItems(pipeStack)
                    .duration((int) (material.getMass()))
                    .EUt(6L * getVoltageMultiplier(material))
                    .save(provider);
        }
        if (material.hasFlag(NO_SMASHING)) {
            EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_small_pipe_dust")
                    .inputItems(dust, material, 1)
                    .notConsumable(GTItems.SHAPE_EXTRUDER_PIPE_SMALL)
                    .outputItems(pipeStack)
                    .duration((int) (material.getMass()))
                    .EUt(6L * getVoltageMultiplier(material))
                    .save(provider);
        } else if (plate.doGenerateItem(material)) {
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("small_%s_pipe", material.getName()),
                    pipeStack, "wXh",
                    'X', new MaterialEntry(plate, material));
        }
    }

    private static void processPipeNormal(@NotNull Consumer<FinishedRecipe> provider,
                                          @NotNull PropertyKey<?> propertyKey,
                                          @NotNull TagPrefix prefix, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(prefix) || !material.hasProperty(propertyKey)) {
            return;
        }

        if (material.hasProperty(PropertyKey.WOOD)) return;
        ItemStack pipeStack = ChemicalHelper.get(prefix, material);
        EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_pipe")
                .inputItems(ingot, material, 3)
                .notConsumable(GTItems.SHAPE_EXTRUDER_PIPE_NORMAL)
                .outputItems(pipeStack)
                .duration((int) material.getMass() * 3)
                .EUt(6L * getVoltageMultiplier(material))
                .save(provider);

        if (material.hasFluid()) {
            FLUID_SOLIDFICATION_RECIPES.recipeBuilder("solidify_" + material.getName() + "_to_normal_pipe")
                    .notConsumable(GTItems.SHAPE_MOLD_NORMAL_PIPE)
                    .inputFluids(material.getFluid(L * 3))
                    .outputItems(pipeStack)
                    .duration((int) (material.getMass()) * 3)
                    .EUt(6L * getVoltageMultiplier(material))
                    .save(provider);
        }
        if (material.hasFlag(NO_SMASHING)) {
            EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_pipe_dust")
                    .inputItems(dust, material, 3)
                    .notConsumable(GTItems.SHAPE_EXTRUDER_PIPE_NORMAL)
                    .outputItems(pipeStack)
                    .duration((int) material.getMass() * 3)
                    .EUt(6L * getVoltageMultiplier(material))
                    .save(provider);
        } else if (plate.doGenerateItem(material)) {
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("medium_%s_pipe", material.getName()),
                    pipeStack, "XXX", "w h",
                    'X', new MaterialEntry(plate, material));
        }
    }

    private static void processPipeLarge(@NotNull Consumer<FinishedRecipe> provider,
                                         @NotNull PropertyKey<?> propertyKey,
                                         @NotNull TagPrefix prefix, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(prefix) || !material.hasProperty(propertyKey)) {
            return;
        }

        if (material.hasProperty(PropertyKey.WOOD)) return;
        ItemStack pipeStack = ChemicalHelper.get(prefix, material);
        EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_large_pipe")
                .inputItems(ingot, material, 6)
                .notConsumable(GTItems.SHAPE_EXTRUDER_PIPE_LARGE)
                .outputItems(pipeStack)
                .duration((int) material.getMass() * 6)
                .EUt(6L * getVoltageMultiplier(material))
                .save(provider);

        if (material.hasFluid()) {
            FLUID_SOLIDFICATION_RECIPES.recipeBuilder("solidify_" + material.getName() + "_to_large_pipe")
                    .notConsumable(GTItems.SHAPE_MOLD_LARGE_PIPE)
                    .inputFluids(material.getFluid(L * 6))
                    .outputItems(pipeStack)
                    .duration((int) (material.getMass()) * 6)
                    .EUt(6L * getVoltageMultiplier(material))
                    .save(provider);
        }
        if (material.hasFlag(NO_SMASHING)) {
            EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_large_pipe_dust")
                    .inputItems(dust, material, 6)
                    .notConsumable(GTItems.SHAPE_EXTRUDER_PIPE_LARGE)
                    .outputItems(pipeStack)
                    .duration((int) material.getMass() * 6)
                    .EUt(6L * getVoltageMultiplier(material))
                    .save(provider);
        } else if (plate.doGenerateItem(material)) {
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("large_%s_pipe", material.getName()),
                    pipeStack, "XXX", "w h", "XXX",
                    'X', new MaterialEntry(plate, material));
        }
    }

    private static void processPipeHuge(@NotNull Consumer<FinishedRecipe> provider, @NotNull PropertyKey<?> propertyKey,
                                        @NotNull TagPrefix prefix, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(prefix) || !material.hasProperty(propertyKey)) {
            return;
        }

        if (material.hasProperty(PropertyKey.WOOD)) return;
        ItemStack pipeStack = ChemicalHelper.get(prefix, material);
        EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_huge_pipe")
                .inputItems(ingot, material, 12)
                .notConsumable(GTItems.SHAPE_EXTRUDER_PIPE_HUGE)
                .outputItems(pipeStack)
                .duration((int) material.getMass() * 24)
                .EUt(6L * getVoltageMultiplier(material))
                .save(provider);

        if (material.hasFluid()) {
            FLUID_SOLIDFICATION_RECIPES.recipeBuilder("solidify_" + material.getName() + "_to_huge_pipe")
                    .notConsumable(GTItems.SHAPE_MOLD_HUGE_PIPE)
                    .inputFluids(material.getFluid(L * 12))
                    .outputItems(pipeStack)
                    .duration((int) (material.getMass()) * 24)
                    .EUt(6L * getVoltageMultiplier(material))
                    .save(provider);
        }
        if (material.hasFlag(NO_SMASHING)) {
            EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_huge_pipe_dust")
                    .inputItems(dust, material, 12)
                    .notConsumable(GTItems.SHAPE_EXTRUDER_PIPE_HUGE)
                    .outputItems(pipeStack)
                    .duration((int) material.getMass() * 24)
                    .EUt(6L * getVoltageMultiplier(material))
                    .save(provider);
        } else if (plateDouble.doGenerateItem(material)) {
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("huge_%s_pipe", material.getName()),
                    pipeStack, "XXX", "w h", "XXX",
                    'X', new MaterialEntry(plateDouble, material));
        }
    }

    private static void processPipeQuadruple(@NotNull Consumer<FinishedRecipe> provider,
                                             @NotNull PropertyKey<?> propertyKey,
                                             @NotNull TagPrefix prefix, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(prefix) || !material.hasProperty(propertyKey)) {
            return;
        }

        if (material.hasProperty(PropertyKey.WOOD)) return;
        ItemStack smallPipe = ChemicalHelper.get(pipeSmallFluid, material);
        ItemStack quadPipe = ChemicalHelper.get(prefix, material);
        VanillaRecipeHelper.addShapedRecipe(provider, String.format("quadruple_%s_pipe", material.getName()),
                quadPipe, "XX", "XX",
                'X', smallPipe);

        PACKER_RECIPES.recipeBuilder("package_" + material.getName() + "_quadruple_pipe")
                .inputItems(smallPipe.copyWithCount(4))
                .circuitMeta(4)
                .outputItems(quadPipe)
                .duration(30)
                .EUt(VA[ULV])
                .save(provider);
    }

    private static void processPipeNonuple(@NotNull Consumer<FinishedRecipe> provider,
                                           @NotNull PropertyKey<?> propertyKey,
                                           @NotNull TagPrefix prefix, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(prefix) || !material.hasProperty(propertyKey)) {
            return;
        }

        if (material.hasProperty(PropertyKey.WOOD)) return;
        ItemStack smallPipe = ChemicalHelper.get(pipeSmallFluid, material);
        ItemStack nonuplePipe = ChemicalHelper.get(prefix, material);
        VanillaRecipeHelper.addShapedRecipe(provider, String.format("nonuple_%s_pipe", material.getName()),
                nonuplePipe, "XXX", "XXX", "XXX",
                'X', smallPipe);

        PACKER_RECIPES.recipeBuilder("package_" + material.getName() + "_nonuple_pipe")
                .inputItems(smallPipe.copyWithCount(9))
                .circuitMeta(9)
                .outputItems(nonuplePipe)
                .duration(40)
                .EUt(VA[ULV])
                .save(provider);
    }

    private static void addDuctRecipes(Consumer<FinishedRecipe> provider, Material material, int outputAmount) {
        if (plate.doGenerateItem(material)) {
            VanillaRecipeHelper.addShapedRecipe(provider, "small_duct_%s".formatted(material.getName()),
                    GTBlocks.DUCT_PIPES[DuctPipeType.SMALL.ordinal()].asStack(outputAmount * 2), "w", "X", "h",
                    'X', new MaterialEntry(plate, material));
            VanillaRecipeHelper.addShapedRecipe(provider, "medium_duct_%s".formatted(material.getName()),
                    GTBlocks.DUCT_PIPES[DuctPipeType.NORMAL.ordinal()].asStack(outputAmount), " X ", "wXh", " X ",
                    'X', new MaterialEntry(plate, material));
            VanillaRecipeHelper.addShapedRecipe(provider, "large_duct_%s".formatted(material.getName()),
                    GTBlocks.DUCT_PIPES[DuctPipeType.LARGE.ordinal()].asStack(outputAmount), "XwX", "X X", "XhX",
                    'X', new MaterialEntry(plate, material));
        }
        if (plateDouble.doGenerateItem(material)) {
            VanillaRecipeHelper.addShapedRecipe(provider, "huge_duct_%s".formatted(material.getName()),
                    GTBlocks.DUCT_PIPES[DuctPipeType.HUGE.ordinal()].asStack(outputAmount), "XwX", "X X", "XhX",
                    'X', new MaterialEntry(plateDouble, material));
        }
    }

    private static int getVoltageMultiplier(Material material) {
        if (material.hasProperty(PropertyKey.POLYMER)) return 4;
        return material.getBlastTemperature() >= 2800 ? VA[LV] : VA[ULV];
    }
}
