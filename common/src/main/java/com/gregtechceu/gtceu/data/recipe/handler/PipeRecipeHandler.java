package com.gregtechceu.gtceu.data.recipe.handler;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidPipeProperties;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.IMaterialProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ItemPipeProperties;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.common.libs.GTMaterials.*;
import static com.gregtechceu.gtceu.api.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.libs.GTRecipeTypes.*;
import static com.gregtechceu.gtceu.common.libs.GTItems.*;

public class PipeRecipeHandler {

    public static void init(Consumer<FinishedRecipe> provider) {
        pipeTinyFluid.executeHandler(PropertyKey.FLUID_PIPE, (tagPrefix, material, property) -> processPipeTiny(tagPrefix, material, property, provider));
        pipeSmallFluid.executeHandler(PropertyKey.FLUID_PIPE, (tagPrefix, material, property) -> processPipeSmall(tagPrefix, material, property, provider));
        pipeNormalFluid.executeHandler(PropertyKey.FLUID_PIPE, (tagPrefix, material, property) -> processPipeNormal(tagPrefix, material, property, provider));
        pipeLargeFluid.executeHandler(PropertyKey.FLUID_PIPE, (tagPrefix, material, property) -> processPipeLarge(tagPrefix, material, property, provider));
        pipeHugeFluid.executeHandler(PropertyKey.FLUID_PIPE, (tagPrefix, material, property) -> processPipeHuge(tagPrefix, material, property, provider));

        pipeQuadrupleFluid.executeHandler(PropertyKey.FLUID_PIPE, (tagPrefix, material, property) -> processPipeQuadruple(tagPrefix, material, property, provider));
        pipeNonupleFluid.executeHandler(PropertyKey.FLUID_PIPE, (tagPrefix, material, property) -> processPipeNonuple(tagPrefix, material, property, provider));
        
        // TODO Item Pipe?
//        pipeTinyItem.executeHandler(PropertyKey.ITEM_PIPE, (tagPrefix, material, property) -> processPipeTiny(tagPrefix, material, property, provider));
//        pipeSmallItem.executeHandler(PropertyKey.ITEM_PIPE, (tagPrefix, material, property) -> processPipeSmall(tagPrefix, material, property, provider));
//        pipeNormalItem.executeHandler(PropertyKey.ITEM_PIPE, (tagPrefix, material, property) -> processPipeNormal(tagPrefix, material, property, provider));
//        pipeLargeItem.executeHandler(PropertyKey.ITEM_PIPE, (tagPrefix, material, property) -> processPipeLarge(tagPrefix, material, property, provider));
//        pipeHugeItem.executeHandler(PropertyKey.ITEM_PIPE, (tagPrefix, material, property) -> processPipeHuge(tagPrefix, material, property, provider));
//
//        pipeSmallRestrictive.executeHandler(PropertyKey.ITEM_PIPE, (tagPrefix, material, property) -> processRestrictivePipe(tagPrefix, material, property, provider));
//        pipeNormalRestrictive.executeHandler(PropertyKey.ITEM_PIPE, (tagPrefix, material, property) -> processRestrictivePipe(tagPrefix, material, property, provider));
//        pipeLargeRestrictive.executeHandler(PropertyKey.ITEM_PIPE, (tagPrefix, material, property) -> processRestrictivePipe(tagPrefix, material, property, provider));
//        pipeHugeRestrictive.executeHandler(PropertyKey.ITEM_PIPE, (tagPrefix, material, property) -> processRestrictivePipe(tagPrefix, material, property, provider));
    }

    private static void processRestrictivePipe(TagPrefix pipePrefix, Material material, ItemPipeProperties property, Consumer<FinishedRecipe> provider) {
        String id = "%s_%s".formatted(FormattingUtil.toLowerCaseUnder(pipePrefix.name), material.getName().toLowerCase());
        TagPrefix unrestrictive;
        if (pipePrefix == pipeSmallRestrictive) unrestrictive = pipeSmallItem;
        else if (pipePrefix == pipeNormalRestrictive) unrestrictive = pipeNormalItem;
        else if (pipePrefix == pipeLargeRestrictive) unrestrictive = pipeLargeItem;
        else if (pipePrefix == pipeHugeRestrictive) unrestrictive = pipeHugeItem;
        else return;

        ASSEMBLER_RECIPES.recipeBuilder(id)
                .inputItems(unrestrictive, material)
                .inputItems(ring, Iron, 2)
                .outputItems(pipePrefix, material)
                .duration(20)
                .EUt(GTValues.VA[GTValues.ULV])
                .save(provider);

        VanillaRecipeHelper.addShapedRecipe(provider, id,
                ChemicalHelper.get(pipePrefix, material), "PR", "Rh",
                'P', new UnificationEntry(unrestrictive, material), 'R', ChemicalHelper.get(ring, Iron));
    }

    private static void processPipeTiny(TagPrefix pipePrefix, Material material, IMaterialProperty<?> property, Consumer<FinishedRecipe> provider) {
        String id = "%s_%s.".formatted(FormattingUtil.toLowerCaseUnder(pipePrefix.name), material.getName().toLowerCase());
        ItemStack pipeStack = ChemicalHelper.get(pipePrefix, material);
        EXTRUDER_RECIPES.recipeBuilder(id + 0)
                .inputItems(ingot, material, 1)
                .notConsumable(SHAPE_EXTRUDER_PIPE_TINY.asStack())
                .outputItems(GTUtil.copyAmount(2, pipeStack))
                .duration((int) (material.getMass()))
                .EUt(6L * getVoltageMultiplier(material))
                .save(provider);

        if (material.hasFlag(NO_SMASHING)) {
            EXTRUDER_RECIPES.recipeBuilder(id + 1)
                    .inputItems(dust, material, 1)
                    .notConsumable(SHAPE_EXTRUDER_PIPE_TINY.asStack())
                    .outputItems(GTUtil.copyAmount(2, pipeStack))
                    .duration((int) (material.getMass()))
                    .EUt(6L * getVoltageMultiplier(material))
                    .save(provider);
        } else {
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("tiny_%s_pipe", material),
                    GTUtil.copyAmount(2, pipeStack), " s ", "hXw",
                    'X', new UnificationEntry(plate, material));
        }
    }

    private static void processPipeSmall(TagPrefix pipePrefix, Material material, IMaterialProperty<?> property, Consumer<FinishedRecipe> provider) {
        String id = "%s_%s.".formatted(FormattingUtil.toLowerCaseUnder(pipePrefix.name), material.getName().toLowerCase());
        ItemStack pipeStack = ChemicalHelper.get(pipePrefix, material);
        EXTRUDER_RECIPES.recipeBuilder(id + 0)
                .inputItems(ingot, material, 1)
                .notConsumable(SHAPE_EXTRUDER_PIPE_SMALL.asStack())
                .outputItems(pipeStack)
                .duration((int) (material.getMass()))
                .EUt(6L * getVoltageMultiplier(material))
                .save(provider);

        if (material.hasFlag(NO_SMASHING)) {
            EXTRUDER_RECIPES.recipeBuilder(id + 1)
                    .inputItems(dust, material, 1)
                    .notConsumable(SHAPE_EXTRUDER_PIPE_SMALL.asStack())
                    .outputItems(pipeStack)
                    .duration((int) (material.getMass()))
                    .EUt(6L * getVoltageMultiplier(material))
                    .save(provider);
        } else {
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("small_%s_pipe", material),
                    pipeStack, "wXh",
                    'X', new UnificationEntry(plate, material));
        }
    }

    private static void processPipeNormal(TagPrefix pipePrefix, Material material, IMaterialProperty<?> property, Consumer<FinishedRecipe> provider) {
        String id = "%s_%s.".formatted(FormattingUtil.toLowerCaseUnder(pipePrefix.name), material.getName().toLowerCase());
        ItemStack pipeStack = ChemicalHelper.get(pipePrefix, material);
        EXTRUDER_RECIPES.recipeBuilder(id + 0)
                .inputItems(ingot, material, 3)
                .notConsumable(SHAPE_EXTRUDER_PIPE_NORMAL.asStack())
                .outputItems(pipeStack)
                .duration((int) material.getMass() * 3)
                .EUt(6L * getVoltageMultiplier(material))
                .save(provider);

        if (material.hasFlag(NO_SMASHING)) {
            EXTRUDER_RECIPES.recipeBuilder(id + 1)
                    .inputItems(dust, material, 3)
                    .notConsumable(SHAPE_EXTRUDER_PIPE_NORMAL.asStack())
                    .outputItems(pipeStack)
                    .duration((int) material.getMass() * 3)
                    .EUt(6L * getVoltageMultiplier(material))
                    .save(provider);
        } else {
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("medium_%s_pipe", material),
                    pipeStack, "XXX", "w h",
                    'X', new UnificationEntry(plate, material));
        }
    }

    private static void processPipeLarge(TagPrefix pipePrefix, Material material, IMaterialProperty<?> property, Consumer<FinishedRecipe> provider) {
        String id = "%s_%s.".formatted(FormattingUtil.toLowerCaseUnder(pipePrefix.name), material.getName().toLowerCase());
        ItemStack pipeStack = ChemicalHelper.get(pipePrefix, material);
        EXTRUDER_RECIPES.recipeBuilder(id + 0)
                .inputItems(ingot, material, 6)
                .notConsumable(SHAPE_EXTRUDER_PIPE_LARGE.asStack())
                .outputItems(pipeStack)
                .duration((int) material.getMass() * 6)
                .EUt(6L * getVoltageMultiplier(material))
                .save(provider);

        if (material.hasFlag(NO_SMASHING)) {
            EXTRUDER_RECIPES.recipeBuilder(id + 1)
                    .inputItems(dust, material, 6)
                    .notConsumable(SHAPE_EXTRUDER_PIPE_LARGE.asStack())
                    .outputItems(pipeStack)
                    .duration((int) material.getMass() * 6)
                    .EUt(6L * getVoltageMultiplier(material))
                    .save(provider);
        } else {
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("large_%s_pipe", material),
                    pipeStack, "XXX", "w h", "XXX",
                    'X', new UnificationEntry(plate, material));
        }
    }

    private static void processPipeHuge(TagPrefix pipePrefix, Material material, IMaterialProperty<?> property, Consumer<FinishedRecipe> provider) {
        String id = "%s_%s.".formatted(FormattingUtil.toLowerCaseUnder(pipePrefix.name), material.getName().toLowerCase());
        ItemStack pipeStack = ChemicalHelper.get(pipePrefix, material);
        EXTRUDER_RECIPES.recipeBuilder(id + 0)
                .inputItems(ingot, material, 12)
                .notConsumable(SHAPE_EXTRUDER_PIPE_HUGE.asStack())
                .outputItems(pipeStack)
                .duration((int) material.getMass() * 24)
                .EUt(6L * getVoltageMultiplier(material))
                .save(provider);

        if (material.hasFlag(NO_SMASHING)) {
            EXTRUDER_RECIPES.recipeBuilder(id + 1)
                    .inputItems(dust, material, 12)
                    .notConsumable(SHAPE_EXTRUDER_PIPE_HUGE.asStack())
                    .outputItems(pipeStack)
                    .duration((int) material.getMass() * 24)
                    .EUt(6L * getVoltageMultiplier(material))
                    .save(provider);
        } else if (plateDouble.doGenerateItem(material)) {
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("huge_%s_pipe", material),
                    pipeStack, "XXX", "w h", "XXX",
                    'X', new UnificationEntry(plateDouble, material));
        }
    }

    private static void processPipeQuadruple(TagPrefix pipePrefix, Material material, FluidPipeProperties property, Consumer<FinishedRecipe> provider) {
        String id = "%s_%s".formatted(FormattingUtil.toLowerCaseUnder(pipePrefix.name), material.getName().toLowerCase());
        ItemStack smallPipe = ChemicalHelper.get(pipeSmallFluid, material);
        ItemStack quadPipe = ChemicalHelper.get(pipePrefix, material);
        VanillaRecipeHelper.addShapedRecipe(provider, String.format("quadruple_%s_pipe", material.toString()),
                quadPipe, "XX", "XX",
                'X', smallPipe);

        PACKER_RECIPES.recipeBuilder(id)
                .inputItems(GTUtil.copyAmount(4, smallPipe))
                .circuitMeta(4)
                .outputItems(quadPipe)
                .duration(30)
                .EUt(GTValues.VA[GTValues.ULV])
                .save(provider);
    }

    private static void processPipeNonuple(TagPrefix pipePrefix, Material material, FluidPipeProperties property, Consumer<FinishedRecipe> provider) {
        String id = "%s_%s".formatted(FormattingUtil.toLowerCaseUnder(pipePrefix.name), material.getName().toLowerCase());
        ItemStack smallPipe = ChemicalHelper.get(pipeSmallFluid, material);
        ItemStack nonuplePipe = ChemicalHelper.get(pipePrefix, material);
        VanillaRecipeHelper.addShapedRecipe(provider, String.format("nonuple_%s_pipe", material.toString()),
                nonuplePipe, "XXX", "XXX", "XXX",
                'X', smallPipe);

        PACKER_RECIPES.recipeBuilder(id)
                .inputItems(GTUtil.copyAmount(9, smallPipe))
                .circuitMeta(9)
                .outputItems(nonuplePipe)
                .duration(40)
                .EUt(GTValues.VA[GTValues.ULV])
                .save(provider);
    }

    private static int getVoltageMultiplier(Material material) {
        return material.getBlastTemperature() >= 2800 ? GTValues.VA[GTValues.LV] : GTValues.VA[GTValues.ULV];
    }
}
