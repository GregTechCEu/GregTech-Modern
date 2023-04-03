package com.gregtechceu.gtceu.data.recipe.generated;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.IngotProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.POLARIZER_RECIPES;

public class PolarizingRecipeHandler {

    private static final TagPrefix[] POLARIZING_PREFIXES = new TagPrefix[]{
            rod, rodLong, plate, ingot, plateDense, rotor,
            bolt, screw, wireFine, foil, ring};

    public static void init(Consumer<FinishedRecipe> provider) {
        for (TagPrefix orePrefix : POLARIZING_PREFIXES) {
            orePrefix.executeHandler(PropertyKey.INGOT, (tagPrefix, material, property) -> processPolarizing(tagPrefix, material, property, provider));
        }
    }

    public static void processPolarizing(TagPrefix polarizingPrefix, Material material, IngotProperty property, Consumer<FinishedRecipe> provider) {
        Material magneticMaterial = property.getMagneticMaterial();

        if (magneticMaterial != null && polarizingPrefix.doGenerateItem(magneticMaterial)) {
            ItemStack magneticStack = ChemicalHelper.get(polarizingPrefix, magneticMaterial);
            POLARIZER_RECIPES.recipeBuilder("polarize_" + material.getName() + "_" + polarizingPrefix.name) //polarizing
                    .inputItems(polarizingPrefix, material)
                    .outputItems(magneticStack)
                    .duration((int) ((int) material.getMass() * polarizingPrefix.getMaterialAmount(material) / M))
                    .EUt(8L * getVoltageMultiplier(material))
                    .save(provider);

            VanillaRecipeHelper.addSmeltingRecipe(provider, "demagnetize_" + magneticMaterial.getName() + "_" + polarizingPrefix,
                    ChemicalHelper.getTag(polarizingPrefix, magneticMaterial),
                    ChemicalHelper.get(polarizingPrefix, material)); //de-magnetizing
        }
    }

    private static int getVoltageMultiplier(Material material) {
        return material.getBlastTemperature() >= 1200 ? VA[LV] : 2;
    }
}
