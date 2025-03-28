package com.gregtechceu.gtceu.data.recipe.generated;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.WireProperties;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.Util;
import net.minecraft.data.recipes.FinishedRecipe;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import org.jetbrains.annotations.NotNull;

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
public final class WireRecipeHandler {

    private static final Reference2IntMap<TagPrefix> INSULATION_AMOUNT = Util.make(new Reference2IntOpenHashMap<>(),
            map -> {
                map.put(cableGtSingle, 1);
                map.put(cableGtDouble, 1);
                map.put(cableGtQuadruple, 2);
                map.put(cableGtOctal, 3);
                map.put(cableGtHex, 5);
            });

    private static final TagPrefix[] wireSizes = { wireGtDouble, wireGtQuadruple, wireGtOctal, wireGtHex };

    private WireRecipeHandler() {}

    public static void run(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        WireProperties property = material.getProperty(PropertyKey.WIRE);
        if (property == null) {
            return;
        }

        // Generate Wire creation recipes (Wiremill, Extruder, Wire Cutters)
        // Wiremill: Ingot -> 1x, 2x, 4x, 8x, 16x, Fine
        // Wiremill: 1x Wire -> Fine
        // Extruder: Ingot -> 1x Wire
        // Wire Cutter: Plate -> 1x Wire
        processWires(provider, material);

        // Generate Cable Covering Recipes
        generateCableCovering(provider, property, wireGtSingle, material);
        generateCableCovering(provider, property, wireGtDouble, material);
        generateCableCovering(provider, property, wireGtQuadruple, material);
        generateCableCovering(provider, property, wireGtOctal, material);
        generateCableCovering(provider, property, wireGtHex, material);
    }

    private static void processWires(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(wireGtSingle)) {
            return;
        }

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
                    'X', new MaterialEntry(plate, material));
        }
    }

    private static void generateCableCovering(@NotNull Consumer<FinishedRecipe> provider,
                                              @NotNull WireProperties property,
                                              @NotNull TagPrefix prefix, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(prefix) || property.isSuperconductor()) {
            // Superconductors have no Cables, so exit early
            return;
        }

        int cableAmount = (int) (prefix.getMaterialAmount(material) * 2 / M);
        TagPrefix cablePrefix = TagPrefix.get("cable" + prefix.name().substring(4));
        int voltageTier = GTUtil.getTierByVoltage(property.getVoltage());
        int insulationAmount = INSULATION_AMOUNT.getInt(cablePrefix);

        // Generate hand-crafting recipes for ULV and LV cables
        if (voltageTier <= LV) {
            generateManualRecipe(provider, prefix, cablePrefix, cableAmount, material);
        }

        // Rubber Recipe (ULV-EV cables)
        if (voltageTier <= EV) {
            GTRecipeBuilder builder = ASSEMBLER_RECIPES
                    .recipeBuilder("cover_" + material.getName() + "_" + prefix + "_rubber")
                    .EUt(VA[ULV]).duration(100)
                    .inputItems(prefix, material)
                    .outputItems(cablePrefix, material)
                    .inputFluids(Rubber, L * insulationAmount);

            if (voltageTier == EV) {
                builder.inputItems(foil, PolyvinylChloride, insulationAmount);
            }
            builder.save(provider);
        }

        // Silicone Rubber Recipe (all cables)
        GTRecipeBuilder builder = ASSEMBLER_RECIPES
                .recipeBuilder("cover_" + material.getName() + "_" + prefix + "_silicone")
                .EUt(VA[ULV]).duration(100)
                .inputItems(prefix, material)
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
                .recipeBuilder("cover_" + material.getName() + "_" + prefix + "_styrene_butadiene")
                .EUt(VA[ULV]).duration(100)
                .inputItems(prefix, material)
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

    private static void generateManualRecipe(@NotNull Consumer<FinishedRecipe> provider, @NotNull TagPrefix wirePrefix,
                                             @NotNull TagPrefix cablePrefix, int cableAmount,
                                             @NotNull Material material) {
        int insulationAmount = INSULATION_AMOUNT.getInt(cablePrefix);
        Object[] ingredients = new Object[insulationAmount + 1];
        ingredients[0] = new MaterialEntry(wirePrefix, material);
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

    private static int getVoltageMultiplier(@NotNull Material material) {
        return material.getBlastTemperature() >= 2800 ? VA[LV] : VA[ULV];
    }
}
