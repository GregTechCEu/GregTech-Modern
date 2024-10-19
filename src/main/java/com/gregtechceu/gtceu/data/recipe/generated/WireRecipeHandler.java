package com.gregtechceu.gtceu.data.recipe.generated;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.WireProperties;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.data.recipes.FinishedRecipe;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_FINE_WIRE;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_PLATE;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.NO_WORKING;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

/**
 * Guide to the GregTech CE: Unofficial Cable Processing.
 * <br>
 * Cable Covering Fluids:
 * - Rubber: This can be used for any cable EV-tier or lower. After that it is unavailable.
 * <br>
 * - Silicone Rubber: This can be used for any cable tier, saving the amount of fluid needed. However, at IV,
 * it will require a Foil of the cable material as well, making it undesirable.
 * <br>
 * - Styrene-Butadiene Rubber (SBR): This can be used for any cable tier, and is the most optimal cable-covering
 * fluid available.
 * <br>
 * Extra Materials for Cable Covering:
 * - Polyphenylene Sulfide (PPS): At LuV, this foil is required to cover cables. Lower tiers will not use it.
 * <br>
 * - Material Foil: At IV, an extra foil of the Material is needed to make the cable with SiR.
 */
public class WireRecipeHandler {

    private static final Map<TagPrefix, Integer> INSULATION_AMOUNT = ImmutableMap.of(
            cableGtSingle, 1,
            cableGtDouble, 1,
            cableGtQuadruple, 2,
            cableGtOctal, 3,
            cableGtHex, 5);

    private static final TagPrefix[] wireSizes = { wireGtDouble, wireGtQuadruple, wireGtOctal, wireGtHex };

    public static void init(Consumer<FinishedRecipe> provider) {
        // Generate Wire creation recipes (Wiremill, Extruder, Wire Cutters)
        // Wiremill: Ingot -> 1x, 2x, 4x, 8x, 16x, Fine
        // Wiremill: 1x Wire -> Fine
        // Extruder: Ingot -> 1x Wire
        // Wire Cutter: Plate -> 1x Wire
        wireGtSingle.executeHandler(provider, PropertyKey.WIRE, WireRecipeHandler::processWires);

        // Generate Cable Covering Recipes
        wireGtSingle.executeHandler(provider, PropertyKey.WIRE, WireRecipeHandler::generateCableCovering);
        wireGtDouble.executeHandler(provider, PropertyKey.WIRE, WireRecipeHandler::generateCableCovering);
        wireGtQuadruple.executeHandler(provider, PropertyKey.WIRE, WireRecipeHandler::generateCableCovering);
        wireGtOctal.executeHandler(provider, PropertyKey.WIRE, WireRecipeHandler::generateCableCovering);
        wireGtHex.executeHandler(provider, PropertyKey.WIRE, WireRecipeHandler::generateCableCovering);
    }

    public static void processWires(TagPrefix wirePrefix, Material material, WireProperties property,
                                    Consumer<FinishedRecipe> provider) {
        TagPrefix prefix = material.hasProperty(PropertyKey.INGOT) ? ingot :
                material.hasProperty(PropertyKey.GEM) ? gem : dust;

        EXTRUDER_RECIPES.recipeBuilder("extrude_" + material.getName() + "_wire")
                .inputItems(prefix, material)
                .notConsumable(GTItems.SHAPE_EXTRUDER_WIRE)
                .outputItems(wireGtSingle, material, 2)
                .duration((int) material.getMass() * 2)
                .EUt(6L * getVoltageMultiplier(material))
                .save(provider);

        WIREMILL_RECIPES.recipeBuilder("mill_" + material.getName() + "_wire")
                .inputItems(prefix, material)
                .circuitMeta(1)
                .outputItems(wireGtSingle, material, 2)
                .duration((int) material.getMass())
                .EUt(getVoltageMultiplier(material))
                .save(provider);

        for (TagPrefix wireSize : wireSizes) {
            final int multiplier = (int) (wireSize.getMaterialAmount(material) / GTValues.M);
            WIREMILL_RECIPES.recipeBuilder("mill_" + material.getName() + "_wire_" + (multiplier * 2))
                    .inputItems(prefix, material, multiplier)
                    .circuitMeta(multiplier * 2)
                    .outputItems(wireSize, material)
                    .duration((int) material.getMass() * multiplier)
                    .EUt(getVoltageMultiplier(material))
                    .save(provider);
        }

        if (material.hasFlag(GENERATE_FINE_WIRE)) {
            WIREMILL_RECIPES.recipeBuilder("mill_" + material.getName() + "_wire_fine")
                    .inputItems(prefix, material, 1)
                    .circuitMeta(3)
                    .outputItems(wireFine, material, 8)
                    .duration((int) material.getMass() * 3)
                    .EUt(getVoltageMultiplier(material))
                    .save(provider);
        }

        if (!material.hasFlag(NO_WORKING) && material.hasFlag(GENERATE_PLATE)) {
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("%s_wire_single", material.getName()),
                    ChemicalHelper.get(wireGtSingle, material), "Xx",
                    'X', new UnificationEntry(plate, material));
        }
    }

    public static void generateCableCovering(TagPrefix wirePrefix, Material material, WireProperties property,
                                             Consumer<FinishedRecipe> provider) {
        // Superconductors have no Cables, so exit early
        if (property.isSuperconductor()) return;

        int cableAmount = (int) (wirePrefix.getMaterialAmount(material) * 2 / M);
        TagPrefix cablePrefix = TagPrefix.get("cable" + wirePrefix.name().substring(4));
        int voltageTier = GTUtil.getTierByVoltage(property.getVoltage());
        int insulationAmount = INSULATION_AMOUNT.get(cablePrefix);

        // Generate hand-crafting recipes for ULV and LV cables
        if (voltageTier <= LV) {
            generateManualRecipe(wirePrefix, material, cablePrefix, cableAmount, provider);
        }

        // Rubber Recipe (ULV-EV cables)
        if (voltageTier <= EV) {
            GTRecipeBuilder builder = ASSEMBLER_RECIPES
                    .recipeBuilder("cover_" + material.getName() + "_" + wirePrefix + "_rubber")
                    .EUt(VA[ULV]).duration(100)
                    .inputItems(wirePrefix, material)
                    .outputItems(cablePrefix, material)
                    .inputFluids(Rubber.getFluid(L * insulationAmount));

            if (voltageTier == EV) {
                builder.inputItems(foil, PolyvinylChloride, insulationAmount);
            }
            builder.save(provider);
        }

        // Silicone Rubber Recipe (all cables)
        GTRecipeBuilder builder = ASSEMBLER_RECIPES
                .recipeBuilder("cover_" + material.getName() + "_" + wirePrefix + "_silicone")
                .EUt(VA[ULV]).duration(100)
                .inputItems(wirePrefix, material)
                .outputItems(cablePrefix, material);

        // Apply a Polyphenylene Sulfate Foil if LuV or above.
        if (voltageTier >= LuV) {
            builder.inputItems(foil, PolyphenyleneSulfide, insulationAmount);
        }

        // Apply a PVC Foil if EV or above.
        if (voltageTier >= EV) {
            builder.inputItems(foil, PolyvinylChloride, insulationAmount);
        }

        builder.inputFluids(SiliconeRubber.getFluid(L * insulationAmount / 2))
                .save(provider);

        // Styrene Butadiene Rubber Recipe (all cables)
        builder = ASSEMBLER_RECIPES
                .recipeBuilder("cover_" + material.getName() + "_" + wirePrefix + "_styrene_butadiene")
                .EUt(VA[ULV]).duration(100)
                .inputItems(wirePrefix, material)
                .outputItems(cablePrefix, material);

        // Apply a Polyphenylene Sulfate Foil if LuV or above.
        if (voltageTier >= LuV) {
            builder.inputItems(foil, PolyphenyleneSulfide, insulationAmount);
        }

        // Apply a PVC Foil if EV or above.
        if (voltageTier >= EV) {
            builder.inputItems(foil, PolyvinylChloride, insulationAmount);
        }

        builder.inputFluids(StyreneButadieneRubber.getFluid(L * insulationAmount / 4))
                .save(provider);
    }

    private static void generateManualRecipe(TagPrefix wirePrefix, Material material, TagPrefix cablePrefix,
                                             int cableAmount, Consumer<FinishedRecipe> provider) {
        int insulationAmount = INSULATION_AMOUNT.get(cablePrefix);
        Object[] ingredients = new Object[insulationAmount + 1];
        ingredients[0] = new UnificationEntry(wirePrefix, material);
        for (int i = 1; i <= insulationAmount; i++) {
            ingredients[i] = ChemicalHelper.get(plate, Rubber);
        }
        VanillaRecipeHelper.addShapelessRecipe(provider, String.format("%s_cable_%d", material.getName(), cableAmount),
                ChemicalHelper.get(cablePrefix, material),
                ingredients);

        PACKER_RECIPES.recipeBuilder("cover_" + material.getName() + "_" + wirePrefix)
                .inputItems(wirePrefix, material)
                .inputItems(plate, Rubber, insulationAmount)
                .outputItems(cablePrefix, material)
                .duration(100).EUt(VA[ULV])
                .save(provider);
    }

    private static int getVoltageMultiplier(Material material) {
        return material.getBlastTemperature() >= 2800 ? VA[LV] : VA[ULV];
    }
}
