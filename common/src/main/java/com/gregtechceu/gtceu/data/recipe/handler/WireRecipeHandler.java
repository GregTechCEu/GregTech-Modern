package com.gregtechceu.gtceu.data.recipe.handler;

import com.google.common.collect.ImmutableMap;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.common.libs.GTRecipeTypes;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.WireProperties;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.Map;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.api.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.libs.GTItems.*;
import static com.gregtechceu.gtceu.common.libs.GTMaterials.*;

/**
 * Guide to the new GregTech CE: Unofficial Cable Processing.
 *
 * Cable Covering Fluids:
 * - Rubber: This can be used for any cable EV-tier or lower. After that it is unavailable.
 *
 * - Silicone Rubber: This can be used for any cable tier, saving the amount of fluid needed. However, at IV,
 *                    it will require a Foil of the cable material as well, making it undesirable.
 *
 * - Styrene-Butadiene Rubber (SBR): This can be used for any cable tier, and is the most optimal cable-covering
 *                                   fluid available.
 *
 * Extra Materials for Cable Covering:
 * - Polyphenylene Sulfide (PPS): At LuV, this foil is required to cover cables. Lower tiers will not use it.
 *
 * - Material Foil: At IV, an extra foil of the Material is needed to make the cable with SiR.
 */
public class WireRecipeHandler {

    private static final Map<TagPrefix, Integer> INSULATION_AMOUNT = ImmutableMap.of(
            cableGtSingle, 1,
            cableGtDouble, 1,
            cableGtQuadruple, 2,
            cableGtOctal, 3,
            cableGtHex, 5
    );

    public static void init(Consumer<FinishedRecipe> provider) {

        // Generate 1x Wire creation recipes (Wiremill, Extruder, Wire Cutters)
        wireGtSingle.executeHandler(PropertyKey.WIRE, (tagPrefix, material, property) -> processWireSingle(tagPrefix, material, property, provider));

        // Generate Cable Covering Recipes
        wireGtSingle.executeHandler(PropertyKey.WIRE, (tagPrefix, material, property) -> generateCableCovering(tagPrefix, material, property, provider));
        wireGtDouble.executeHandler(PropertyKey.WIRE, (tagPrefix, material, property) -> generateCableCovering(tagPrefix, material, property, provider));
        wireGtQuadruple.executeHandler(PropertyKey.WIRE, (tagPrefix, material, property) -> generateCableCovering(tagPrefix, material, property, provider));
        wireGtOctal.executeHandler(PropertyKey.WIRE, (tagPrefix, material, property) -> generateCableCovering(tagPrefix, material, property, provider));
        wireGtHex.executeHandler(PropertyKey.WIRE, (tagPrefix, material, property) -> generateCableCovering(tagPrefix, material, property, provider));
    }


    public static void processWireSingle(TagPrefix wirePrefix, Material material, WireProperties property, Consumer<FinishedRecipe> provider) {
        TagPrefix prefix = material.hasProperty(PropertyKey.INGOT) ? ingot : material.hasProperty(PropertyKey.GEM) ? gem : dust;
        String id = "%s_%s_single".formatted(FormattingUtil.toLowerCaseUnder(wirePrefix.name), material.getName());

        GTRecipeTypes.EXTRUDER_RECIPES.recipeBuilder(id)
                .inputItems(prefix, material)
                .notConsumable(SHAPE_EXTRUDER_WIRE.asStack())
                .outputItems(wireGtSingle, material, 2)
                .duration((int) material.getMass() * 2)
                .EUt(6L * getVoltageMultiplier(material))
                .save(provider);

        GTRecipeTypes.WIREMILL_RECIPES.recipeBuilder(id)
                .inputItems(prefix, material)
                .outputItems(wireGtSingle, material, 2)
                .duration((int) material.getMass())
                .EUt(getVoltageMultiplier(material))
                .save(provider);

        if (!material.hasFlag(NO_WORKING) && material.hasFlag(GENERATE_PLATE)) {
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("%s_wire_single", material),
                    ChemicalHelper.get(wireGtSingle, material), "Xx",
                    'X', new UnificationEntry(plate, material));
        }
    }

    public static void generateCableCovering(TagPrefix wirePrefix, Material material, WireProperties property, Consumer<FinishedRecipe> provider) {
        String id = "%s_%s_".formatted(FormattingUtil.toLowerCaseUnder(wirePrefix.name), material.getName());

        // Superconductors have no Cables, so exit early
        if (property.isSuperconductor()) return;

        int cableAmount = (int) (wirePrefix.getMaterialAmount(material) * 2 / GTValues.M);
        TagPrefix cablePrefix = TagPrefix.getPrefix("cable" + wirePrefix.name().substring(4));
        int voltageTier = GTUtil.getTierByVoltage(property.getVoltage());
        int insulationAmount = INSULATION_AMOUNT.get(cablePrefix);

        // Generate hand-crafting recipes for ULV and LV cables
        if (voltageTier <= GTValues.LV) {
            generateManualRecipe(wirePrefix, material, cablePrefix, cableAmount, provider);
        }

        // Rubber Recipe (ULV-EV cables)
        if (voltageTier <= GTValues.EV) {
            var builder = GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(id + "rubber").EUt(GTValues.VA[GTValues.ULV]).duration(100)
                    .inputItems(wirePrefix, material)
                    .outputItems(cablePrefix, material)
                    .inputFluids(Rubber.getFluid(GTValues.L * insulationAmount));

            if (voltageTier == GTValues.EV) {
                builder.inputItems(foil, PolyvinylChloride, insulationAmount);
            }
            builder.save(provider);
        }

        // Silicone Rubber Recipe (all cables)
        var builder = GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(id + "silicone").EUt(GTValues.VA[GTValues.ULV]).duration(100)
                .inputItems(wirePrefix, material)
                .outputItems(cablePrefix, material);

        // Apply a Polyphenylene Sulfate Foil if LuV or above.
        if (voltageTier >= GTValues.LuV) {
            builder.inputItems(foil, PolyphenyleneSulfide, insulationAmount);
        }

        // Apply a PVC Foil if EV or above.
        if (voltageTier >= GTValues.EV) {
            builder.inputItems(foil, PolyvinylChloride, insulationAmount);
        }

        builder.inputFluids(SiliconeRubber.getFluid(GTValues.L * insulationAmount / 2))
                .save(provider);

        // Styrene Butadiene Rubber Recipe (all cables)
        builder = GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(id + "sb_rubber").EUt(GTValues.VA[GTValues.ULV]).duration(100)
                .inputItems(wirePrefix, material)
                .outputItems(cablePrefix, material);

        // Apply a Polyphenylene Sulfate Foil if LuV or above.
        if (voltageTier >= GTValues.LuV) {
            builder.inputItems(foil, PolyphenyleneSulfide, insulationAmount);
        }

        // Apply a PVC Foil if EV or above.
        if (voltageTier >= GTValues.EV) {
            builder.inputItems(foil, PolyvinylChloride, insulationAmount);
        }

        builder.inputFluids(StyreneButadieneRubber.getFluid(GTValues.L * insulationAmount / 4))
                .save(provider);
    }

    private static void generateManualRecipe(TagPrefix wirePrefix, Material material, TagPrefix cablePrefix, int cableAmount, Consumer<FinishedRecipe> provider) {
        String id = "%s_%s_manual".formatted(FormattingUtil.toLowerCaseUnder(wirePrefix.name), material.getName());
        int insulationAmount = INSULATION_AMOUNT.get(cablePrefix);
        Object[] ingredients = new Object[insulationAmount + 1];
        ingredients[0] = new UnificationEntry(wirePrefix, material);
        for (int i = 1; i <= insulationAmount; i++) {
            ingredients[i] = ChemicalHelper.get(plate, Rubber);
        }
        VanillaRecipeHelper.addShapelessRecipe(provider, String.format("%s_cable_%d", material, cableAmount),
                ChemicalHelper.get(cablePrefix, material),
                ingredients
        );

        GTRecipeTypes.PACKER_RECIPES.recipeBuilder(id)
                .inputItems(wirePrefix, material)
                .inputItems(plate, Rubber, insulationAmount)
                .outputItems(cablePrefix, material)
                .duration(100).EUt(GTValues.VA[GTValues.ULV])
                .save(provider);
    }

    private static int getVoltageMultiplier(Material material) {
        return material.getBlastTemperature() >= 2800 ? GTValues.VA[GTValues.LV] : GTValues.VA[GTValues.ULV];
    }
}
