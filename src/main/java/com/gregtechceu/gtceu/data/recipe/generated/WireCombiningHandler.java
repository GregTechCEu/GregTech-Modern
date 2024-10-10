package com.gregtechceu.gtceu.data.recipe.generated;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.pipelike.handlers.properties.MaterialEnergyProperties;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;

import net.minecraft.data.recipes.FinishedRecipe;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Map;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.PACKER_RECIPES;

public class WireCombiningHandler {

    private static final TagPrefix[] WIRE_DOUBLING_ORDER = new TagPrefix[] {
            wireGtSingle, wireGtDouble, wireGtQuadruple, wireGtOctal, wireGtHex
    };

    private static final Map<TagPrefix, TagPrefix> cableToWireMap = ImmutableMap.of(
            cableGtSingle, wireGtSingle,
            cableGtDouble, wireGtDouble,
            cableGtQuadruple, wireGtQuadruple,
            cableGtOctal, wireGtOctal,
            cableGtHex, wireGtHex);

    public static void init(Consumer<FinishedRecipe> provider) {
        // Generate Wire Packer/Unpacker recipes
        wireGtSingle.executeHandler(provider,
                MaterialEnergyProperties.registrationHandler(WireCombiningHandler::processWireCompression));

        // Generate manual recipes for combining Wires/Cables
        for (TagPrefix wirePrefix : WIRE_DOUBLING_ORDER) {
            wirePrefix.executeHandler(provider,
                    MaterialEnergyProperties.registrationHandler(WireCombiningHandler::generateWireCombiningRecipe));
        }

        // Generate Cable -> Wire recipes in the unpacker
        for (TagPrefix cablePrefix : cableToWireMap.keySet()) {
            cablePrefix.executeHandler(provider,
                    MaterialEnergyProperties.registrationHandler(WireCombiningHandler::processCableStripping));
        }
    }

    private static void generateWireCombiningRecipe(TagPrefix wirePrefix, Material material,
                                                    MaterialEnergyProperties property,
                                                    Consumer<FinishedRecipe> provider) {
        int wireIndex = ArrayUtils.indexOf(WIRE_DOUBLING_ORDER, wirePrefix);

        if (wireIndex < WIRE_DOUBLING_ORDER.length - 1) {
            VanillaRecipeHelper.addShapelessRecipe(provider,
                    String.format("%s_wire_%s_doubling", material.getName(), wirePrefix),
                    ChemicalHelper.get(WIRE_DOUBLING_ORDER[wireIndex + 1], material),
                    new UnificationEntry(wirePrefix, material),
                    new UnificationEntry(wirePrefix, material));
        }

        if (wireIndex > 0) {
            VanillaRecipeHelper.addShapelessRecipe(provider,
                    String.format("%s_wire_%s_splitting", material.getName(), wirePrefix),
                    ChemicalHelper.get(WIRE_DOUBLING_ORDER[wireIndex - 1], material, 2),
                    new UnificationEntry(wirePrefix, material));
        }

        if (wireIndex < 3) {
            VanillaRecipeHelper.addShapelessRecipe(provider,
                    String.format("%s_wire_%s_quadrupling", material.getName(), wirePrefix),
                    ChemicalHelper.get(WIRE_DOUBLING_ORDER[wireIndex + 2], material),
                    new UnificationEntry(wirePrefix, material),
                    new UnificationEntry(wirePrefix, material),
                    new UnificationEntry(wirePrefix, material),
                    new UnificationEntry(wirePrefix, material));
        }
    }

    private static void processWireCompression(TagPrefix prefix, Material material, MaterialEnergyProperties property,
                                               Consumer<FinishedRecipe> provider) {
        for (int startTier = 0; startTier < 4; startTier++) {
            for (int i = 1; i < 5 - startTier; i++) {
                PACKER_RECIPES.recipeBuilder("pack_" + material.getName() + "_wires_" + i + "_" + startTier)
                        .inputItems(WIRE_DOUBLING_ORDER[startTier], material, 1 << i)
                        .circuitMeta((int) Math.pow(2, i))
                        .outputItems(WIRE_DOUBLING_ORDER[startTier + i], material, 1)
                        .save(provider);
            }
        }

        for (int i = 1; i < 5; i++) {
            PACKER_RECIPES.recipeBuilder("pack_" + material.getName() + "_wires_" + i + "_single")
                    .inputItems(WIRE_DOUBLING_ORDER[i], material, 1)
                    .circuitMeta(1)
                    .outputItems(WIRE_DOUBLING_ORDER[0], material, (int) Math.pow(2, i))
                    .save(provider);
        }
    }

    private static void processCableStripping(TagPrefix prefix, Material material, MaterialEnergyProperties property,
                                              Consumer<FinishedRecipe> provider) {
        PACKER_RECIPES.recipeBuilder("strip_" + material.getName() + "_" + prefix.name)
                .inputItems(prefix, material)
                .outputItems(cableToWireMap.get(prefix), material)
                .outputItems(plate, GTMaterials.Rubber,
                        (int) (prefix.secondaryMaterials().get(0).amount() / GTValues.M))
                .duration(100).EUt(GTValues.VA[GTValues.ULV])
                .save(provider);
    }
}
