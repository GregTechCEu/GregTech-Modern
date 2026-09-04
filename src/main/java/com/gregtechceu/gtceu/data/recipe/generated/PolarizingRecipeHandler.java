package com.gregtechceu.gtceu.data.recipe.generated;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.IngotProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.POLARIZER_RECIPES;

public final class PolarizingRecipeHandler {

    private static final HolderSet<TagPrefix> POLARIZING_PREFIXES = HolderSet.direct(
            rod, rodLong, plate, ingot, plateDense, plateDouble, rotor,
            bolt, screw, wireFine, foil, ring, dust, nugget, block,
            dustTiny, dustSmall);

    private PolarizingRecipeHandler() {}

    public static void run(@NotNull RecipeOutput provider, @NotNull Material material) {
        IngotProperty property = material.getProperty(PropertyKey.INGOT);
        if (property == null) {
            return;
        }

        for (Holder<TagPrefix> prefix : POLARIZING_PREFIXES) {
            processPolarizing(provider, property, prefix, material);
        }
    }

    private static void processPolarizing(@NotNull RecipeOutput provider, @NotNull IngotProperty property,
                                          @NotNull Holder<TagPrefix> prefix, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(prefix) || !material.hasProperty(PropertyKey.INGOT)) {
            return;
        }

        Material magneticMaterial = property.getMagneticMaterial();

        if (magneticMaterial != null && (prefix.value().doGenerateBlock(magneticMaterial) ||
                prefix.value().doGenerateItem(magneticMaterial))) {
            ItemStack magneticStack = ChemicalHelper.get(prefix, magneticMaterial);
            POLARIZER_RECIPES.recipeBuilder("polarize_" + material.getName() + "_" + prefix.value().name) // polarizing
                    .inputItems(prefix, material)
                    .outputItems(magneticStack)
                    .duration((int) ((int) material.getMass() * prefix.value().getMaterialAmount(material) / M))
                    .EUt(getVoltageMultiplier(material))
                    .save(provider);

            VanillaRecipeHelper.addSmeltingRecipe(provider,
                    "demagnetize_" + magneticMaterial.getName() + "_" + prefix,
                    ChemicalHelper.getTag(prefix, magneticMaterial),
                    ChemicalHelper.get(prefix, material)); // de-magnetizing
        }
    }

    private static int getVoltageMultiplier(@NotNull Material material) {
        if (material == GTMaterials.Steel.value() || material == GTMaterials.Iron.value()) return VH[LV];
        if (material == GTMaterials.Neodymium.value()) return VH[HV];
        if (material == GTMaterials.Samarium.value()) return VH[IV];
        return material.getBlastTemperature() >= 1200 ? VA[LV] : 2;
    }
}
