package com.gregtechceu.gtceu.data.recipe.handler;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.IngotProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.LV;
import static com.gregtechceu.gtceu.api.GTValues.VA;
import static com.gregtechceu.gtceu.common.libs.GTRecipeTypes.POLARIZER_RECIPES;

public class PolarizingRecipeHandler {

    private static final TagPrefix[] POLARIZING_PREFIXES = new TagPrefix[]{
            TagPrefix.stick, TagPrefix.stickLong, TagPrefix.plate, TagPrefix.ingot, TagPrefix.plateDense, TagPrefix.rotor,
            TagPrefix.bolt, TagPrefix.screw, TagPrefix.wireFine, TagPrefix.foil, TagPrefix.ring};

    public static void init(Consumer<FinishedRecipe> provider) {
        for (TagPrefix TagPrefix : POLARIZING_PREFIXES) {
            TagPrefix.executeHandler(PropertyKey.INGOT, (tagPrefix, material, property) -> processPolarizing(tagPrefix, material, property, provider));
        }
    }

    public static void processPolarizing(TagPrefix polarizingPrefix, Material material, IngotProperty property, Consumer<FinishedRecipe> provider) {
        Material magneticMaterial = property.getMagneticMaterial();

        if (magneticMaterial != null && polarizingPrefix.doGenerateItem(magneticMaterial)) {
            ItemStack magneticStack = ChemicalHelper.get(polarizingPrefix, magneticMaterial);
            POLARIZER_RECIPES.recipeBuilder(polarizingPrefix.name, material) //polarizing
                    .inputItems(polarizingPrefix, material)
                    .outputItems(magneticStack)
                    .duration((int) (material.getMass() * polarizingPrefix.getMaterialAmount(material) / GTValues.M))
                    .EUt(8L * getVoltageMultiplier(material))
                    .save(provider);

            VanillaRecipeHelper.addSmeltingRecipe(provider, "%s_%s".formatted(polarizingPrefix, material), ChemicalHelper.getTag(polarizingPrefix, magneticMaterial),
                    ChemicalHelper.get(polarizingPrefix, material)); //de-magnetizing
        }
    }

    private static int getVoltageMultiplier(Material material) {
        return material.getBlastTemperature() >= 1200 ? VA[LV] : 2;
    }

}
