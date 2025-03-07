package com.gregtechceu.gtceu.data.recipe.generated;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidPipeProperties;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.IMaterialProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ItemPipeProperties;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.pipelike.duct.DuctPipeType;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.NO_SMASHING;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public class PipeRecipeHandler {

    public static void init(Consumer<FinishedRecipe> provider) {
        PIPE_TINY_FLUID.executeHandler(provider, PropertyKey.FLUID_PIPE, PipeRecipeHandler::processPipeTiny);
        PIPE_SMALL_FLUID.executeHandler(provider, PropertyKey.FLUID_PIPE, PipeRecipeHandler::processPipeSmall);
        PIPE_NORMAL_FLUID.executeHandler(provider, PropertyKey.FLUID_PIPE, PipeRecipeHandler::processPipeNormal);
        PIPE_LARGE_FLUID.executeHandler(provider, PropertyKey.FLUID_PIPE, PipeRecipeHandler::processPipeLarge);
        PIPE_HUGE_FLUID.executeHandler(provider, PropertyKey.FLUID_PIPE, PipeRecipeHandler::processPipeHuge);

        PIPE_QUADRUPLE_FLUID.executeHandler(provider, PropertyKey.FLUID_PIPE, PipeRecipeHandler::processPipeQuadruple);
        PIPE_NONUPLE_FLUID.executeHandler(provider, PropertyKey.FLUID_PIPE, PipeRecipeHandler::processPipeNonuple);

        PIPE_SMALL_ITEM.executeHandler(provider, PropertyKey.ITEM_PIPE, PipeRecipeHandler::processPipeSmall);
        PIPE_NORMAL_ITEM.executeHandler(provider, PropertyKey.ITEM_PIPE, PipeRecipeHandler::processPipeNormal);
        PIPE_LARGE_ITEM.executeHandler(provider, PropertyKey.ITEM_PIPE, PipeRecipeHandler::processPipeLarge);
        PIPE_HUGE_ITEM.executeHandler(provider, PropertyKey.ITEM_PIPE, PipeRecipeHandler::processPipeHuge);

        PIPE_SMALL_RESTRICTIVE.executeHandler(provider, PropertyKey.ITEM_PIPE,
                PipeRecipeHandler::processRestrictivePipe);
        PIPE_NORMAL_RESTRICTIVE.executeHandler(provider, PropertyKey.ITEM_PIPE,
                PipeRecipeHandler::processRestrictivePipe);
        PIPE_LARGE_RESTRICTIVE.executeHandler(provider, PropertyKey.ITEM_PIPE,
                PipeRecipeHandler::processRestrictivePipe);
        PIPE_HUGE_RESTRICTIVE.executeHandler(provider, PropertyKey.ITEM_PIPE,
                PipeRecipeHandler::processRestrictivePipe);

        addDuctRecipes(provider, Steel, 2);
        addDuctRecipes(provider, StainlessSteel, 4);
        addDuctRecipes(provider, TungstenSteel, 8);
    }

    private static void processRestrictivePipe(TagPrefix pipePrefix, Material material, ItemPipeProperties property,
                                               Consumer<FinishedRecipe> provider) {
        TagPrefix unrestrictive;
        if (pipePrefix == PIPE_SMALL_RESTRICTIVE) unrestrictive = PIPE_SMALL_ITEM;
        else if (pipePrefix == PIPE_NORMAL_RESTRICTIVE) unrestrictive = PIPE_NORMAL_ITEM;
        else if (pipePrefix == PIPE_LARGE_RESTRICTIVE) unrestrictive = PIPE_LARGE_ITEM;
        else if (pipePrefix == PIPE_HUGE_RESTRICTIVE) unrestrictive = PIPE_HUGE_ITEM;
        else return;

        ASSEMBLER_RECIPES.recipeBuilder("assemble_" + material.getName() + "_" + pipePrefix.name)
                .inputItems(unrestrictive, material)
                .inputItems(RING, Iron, 2)
                .outputItems(pipePrefix, material)
                .duration(20)
                .EUt(VA[ULV])
                .save(provider);

        VanillaRecipeHelper.addShapedRecipe(provider,
                FormattingUtil.toLowerCaseUnder(pipePrefix + "_" + material.getName()),
                ChemicalHelper.get(pipePrefix, material), "PR", "Rh",
                'P', new MaterialEntry(unrestrictive, material), 'R', ChemicalHelper.get(RING, Iron));
    }

    private static void processPipeTiny(TagPrefix pipePrefix, Material material, IMaterialProperty property,
                                        Consumer<FinishedRecipe> provider) {
        if (material.hasProperty(PropertyKey.WOOD)) return;
        ItemStack pipeStack = ChemicalHelper.get(pipePrefix, material);
        EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_tiny_pipe")
                .inputItems(INGOT, material, 1)
                .notConsumable(GTItems.SHAPE_EXTRUDER_PIPE_TINY)
                .outputItems(GTUtil.copyAmount(2, pipeStack))
                .duration((int) (material.getMass()))
                .EUt(6L * getVoltageMultiplier(material))
                .save(provider);

        if (material.hasFlag(NO_SMASHING)) {
            EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_tiny_pipe_dust")
                    .inputItems(DUST, material, 1)
                    .notConsumable(GTItems.SHAPE_EXTRUDER_PIPE_TINY)
                    .outputItems(GTUtil.copyAmount(2, pipeStack))
                    .duration((int) (material.getMass()))
                    .EUt(6L * getVoltageMultiplier(material))
                    .save(provider);
        } else {
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("tiny_%s_pipe", material.getName()),
                    GTUtil.copyAmount(2, pipeStack), " s ", "hXw",
                    'X', new MaterialEntry(PLATE, material));
        }
    }

    private static void processPipeSmall(TagPrefix pipePrefix, Material material, IMaterialProperty property,
                                         Consumer<FinishedRecipe> provider) {
        if (material.hasProperty(PropertyKey.WOOD)) return;
        ItemStack pipeStack = ChemicalHelper.get(pipePrefix, material);
        EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_small_pipe")
                .inputItems(INGOT, material, 1)
                .notConsumable(GTItems.SHAPE_EXTRUDER_PIPE_SMALL)
                .outputItems(pipeStack)
                .duration((int) (material.getMass()))
                .EUt(6L * getVoltageMultiplier(material))
                .save(provider);

        if (material.hasFlag(NO_SMASHING)) {
            EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_small_pipe_dust")
                    .inputItems(DUST, material, 1)
                    .notConsumable(GTItems.SHAPE_EXTRUDER_PIPE_SMALL)
                    .outputItems(pipeStack)
                    .duration((int) (material.getMass()))
                    .EUt(6L * getVoltageMultiplier(material))
                    .save(provider);
        } else {
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("small_%s_pipe", material.getName()),
                    pipeStack, "wXh",
                    'X', new MaterialEntry(PLATE, material));
        }
    }

    private static void processPipeNormal(TagPrefix pipePrefix, Material material, IMaterialProperty property,
                                          Consumer<FinishedRecipe> provider) {
        if (material.hasProperty(PropertyKey.WOOD)) return;
        ItemStack pipeStack = ChemicalHelper.get(pipePrefix, material);
        EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_pipe")
                .inputItems(INGOT, material, 3)
                .notConsumable(GTItems.SHAPE_EXTRUDER_PIPE_NORMAL)
                .outputItems(pipeStack)
                .duration((int) material.getMass() * 3)
                .EUt(6L * getVoltageMultiplier(material))
                .save(provider);

        if (material.hasFlag(NO_SMASHING)) {
            EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_pipe_dust")
                    .inputItems(DUST, material, 3)
                    .notConsumable(GTItems.SHAPE_EXTRUDER_PIPE_NORMAL)
                    .outputItems(pipeStack)
                    .duration((int) material.getMass() * 3)
                    .EUt(6L * getVoltageMultiplier(material))
                    .save(provider);
        } else {
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("medium_%s_pipe", material.getName()),
                    pipeStack, "XXX", "w h",
                    'X', new MaterialEntry(PLATE, material));
        }
    }

    private static void processPipeLarge(TagPrefix pipePrefix, Material material, IMaterialProperty property,
                                         Consumer<FinishedRecipe> provider) {
        if (material.hasProperty(PropertyKey.WOOD)) return;
        ItemStack pipeStack = ChemicalHelper.get(pipePrefix, material);
        EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_large_pipe")
                .inputItems(INGOT, material, 6)
                .notConsumable(GTItems.SHAPE_EXTRUDER_PIPE_LARGE)
                .outputItems(pipeStack)
                .duration((int) material.getMass() * 6)
                .EUt(6L * getVoltageMultiplier(material))
                .save(provider);

        if (material.hasFlag(NO_SMASHING)) {
            EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_large_pipe_dust")
                    .inputItems(DUST, material, 6)
                    .notConsumable(GTItems.SHAPE_EXTRUDER_PIPE_LARGE)
                    .outputItems(pipeStack)
                    .duration((int) material.getMass() * 6)
                    .EUt(6L * getVoltageMultiplier(material))
                    .save(provider);
        } else {
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("large_%s_pipe", material.getName()),
                    pipeStack, "XXX", "w h", "XXX",
                    'X', new MaterialEntry(PLATE, material));
        }
    }

    private static void processPipeHuge(TagPrefix pipePrefix, Material material, IMaterialProperty property,
                                        Consumer<FinishedRecipe> provider) {
        if (material.hasProperty(PropertyKey.WOOD)) return;
        ItemStack pipeStack = ChemicalHelper.get(pipePrefix, material);
        EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_huge_pipe")
                .inputItems(INGOT, material, 12)
                .notConsumable(GTItems.SHAPE_EXTRUDER_PIPE_HUGE)
                .outputItems(pipeStack)
                .duration((int) material.getMass() * 24)
                .EUt(6L * getVoltageMultiplier(material))
                .save(provider);

        if (material.hasFlag(NO_SMASHING)) {
            EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_huge_pipe_dust")
                    .inputItems(DUST, material, 12)
                    .notConsumable(GTItems.SHAPE_EXTRUDER_PIPE_HUGE)
                    .outputItems(pipeStack)
                    .duration((int) material.getMass() * 24)
                    .EUt(6L * getVoltageMultiplier(material))
                    .save(provider);
        } else if (PLATE_DOUBLE.doGenerateItem(material)) {
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("huge_%s_pipe", material.getName()),
                    pipeStack, "XXX", "w h", "XXX",
                    'X', new MaterialEntry(PLATE_DOUBLE, material));
        }
    }

    private static void processPipeQuadruple(TagPrefix pipePrefix, Material material, FluidPipeProperties property,
                                             Consumer<FinishedRecipe> provider) {
        if (material.hasProperty(PropertyKey.WOOD)) return;
        ItemStack smallPipe = ChemicalHelper.get(PIPE_SMALL_FLUID, material);
        ItemStack quadPipe = ChemicalHelper.get(pipePrefix, material);
        VanillaRecipeHelper.addShapedRecipe(provider, String.format("quadruple_%s_pipe", material.getName()),
                quadPipe, "XX", "XX",
                'X', smallPipe);

        PACKER_RECIPES.recipeBuilder("package_" + material.getName() + "_quadruple_pipe")
                .inputItems(GTUtil.copyAmount(4, smallPipe))
                .circuitMeta(4)
                .outputItems(quadPipe)
                .duration(30)
                .EUt(VA[ULV])
                .save(provider);
    }

    private static void processPipeNonuple(TagPrefix pipePrefix, Material material, FluidPipeProperties property,
                                           Consumer<FinishedRecipe> provider) {
        if (material.hasProperty(PropertyKey.WOOD)) return;
        ItemStack smallPipe = ChemicalHelper.get(PIPE_SMALL_FLUID, material);
        ItemStack nonuplePipe = ChemicalHelper.get(pipePrefix, material);
        VanillaRecipeHelper.addShapedRecipe(provider, String.format("nonuple_%s_pipe", material.getName()),
                nonuplePipe, "XXX", "XXX", "XXX",
                'X', smallPipe);

        PACKER_RECIPES.recipeBuilder("package_" + material.getName() + "_nonuple_pipe")
                .inputItems(GTUtil.copyAmount(9, smallPipe))
                .circuitMeta(9)
                .outputItems(nonuplePipe)
                .duration(40)
                .EUt(VA[ULV])
                .save(provider);
    }

    private static void addDuctRecipes(Consumer<FinishedRecipe> provider, Material material, int outputAmount) {
        VanillaRecipeHelper.addShapedRecipe(provider, "small_duct_%s".formatted(material.getName()),
                GTBlocks.DUCT_PIPES[DuctPipeType.SMALL.ordinal()].asStack(outputAmount * 2), "w", "X", "h",
                'X', new MaterialEntry(PLATE, material));
        VanillaRecipeHelper.addShapedRecipe(provider, "medium_duct_%s".formatted(material.getName()),
                GTBlocks.DUCT_PIPES[DuctPipeType.NORMAL.ordinal()].asStack(outputAmount), " X ", "wXh", " X ",
                'X', new MaterialEntry(PLATE, material));
        VanillaRecipeHelper.addShapedRecipe(provider, "large_duct_%s".formatted(material.getName()),
                GTBlocks.DUCT_PIPES[DuctPipeType.LARGE.ordinal()].asStack(outputAmount), "XwX", "X X", "XhX",
                'X', new MaterialEntry(PLATE, material));
        VanillaRecipeHelper.addShapedRecipe(provider, "huge_duct_%s".formatted(material.getName()),
                GTBlocks.DUCT_PIPES[DuctPipeType.HUGE.ordinal()].asStack(outputAmount), "XwX", "X X", "XhX",
                'X', new MaterialEntry(PLATE_DOUBLE, material));
    }

    private static int getVoltageMultiplier(Material material) {
        return material.getBlastTemperature() >= 2800 ? VA[LV] : VA[ULV];
    }
}
