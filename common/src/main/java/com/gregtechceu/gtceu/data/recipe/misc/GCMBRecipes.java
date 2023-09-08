package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.AlloyBlastProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.ItemTags;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.BLAST_ALLOY_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.MIXER_RECIPES;

public class GCMBRecipes {

    private GCMBRecipes() {
    }

    public static void init(Consumer<FinishedRecipe> provider) {
        registerManualRecipes(provider);
        registerMachineRecipes(provider);
    }

    private static void registerManualRecipes(Consumer<FinishedRecipe> provider) {
        VanillaRecipeHelper.addShapedRecipe(provider, "crushing_wheels", GTBlocks.CRUSHING_WHEELS.asStack(2), "TTT", "UCU","UMU", 'T', ChemicalHelper.get(gearSmall,TungstenCarbide), 'U', ChemicalHelper.get(gear, Ultimet), 'C', GTBlocks.CASING_SECURE_MACERATION.asStack(), 'M', GTItems.ELECTRIC_MOTOR_IV.asStack());
    }

    private static void registerMachineRecipes(Consumer<FinishedRecipe> provider) {
        registerMixerRecipes(provider);
        registerBlastAlloyRecipes(provider);
    }

    private static void registerMixerRecipes(Consumer<FinishedRecipe> provider){
        MIXER_RECIPES.recipeBuilder("tantalum_carbide")
                .inputItems(dust, Tantalum)
                .inputItems(dust, Carbon)
                .outputItems(dust, TantalumCarbide, 2)
                .duration(150).EUt(VA[IV])
                .save(provider);

        MIXER_RECIPES.recipeBuilder("hsla_steel")
                .inputItems(dust, Invar, 2)
                .inputItems(dust, Vanadium)
                .inputItems(dust, Titanium)
                .inputItems(dust, Molybdenum)
                .outputItems(dust, HSLASteel, 5)
                .duration(140).EUt(VA[HV])
                .save(provider);

        MIXER_RECIPES.recipeBuilder("incoloy_ma_956")
                .inputItems(dust, VanadiumSteel, 4)
                .inputItems(dust, Manganese, 2)
                .inputItems(dust, Aluminium, 5)
                .inputItems(dust, Yttrium, 2)
                .outputItems(dust, IncoloyMA956, 13)
                .duration(200).EUt(VA[IV])
                .save(provider);

        MIXER_RECIPES.recipeBuilder("watertight_steel")
                .inputItems(dust, Iron, 7)
                .inputItems(dust, Aluminium, 4)
                .inputItems(dust, Nickel, 2)
                .inputItems(dust, Chromium)
                .inputItems(dust, Sulfur)
                .outputItems(dust, HSLASteel, 15)
                .duration(220).EUt(VA[IV])
                .save(provider);

        MIXER_RECIPES.recipeBuilder("molybdenum_disilicide")
                .inputItems(dust, Molybdenum)
                .inputItems(dust, Silicon, 2)
                .outputItems(dust, MolybdenumDisilicide, 3)
                .duration(180).EUt(VA[EV])
                .save(provider);

        MIXER_RECIPES.recipeBuilder("hastelloy_x")
                .inputItems(dust, Nickel, 8)
                .inputItems(dust, Iron, 3)
                .inputItems(dust, Tungsten, 4)
                .inputItems(dust, Molybdenum, 2)
                .inputItems(dust, Chromium)
                .inputItems(dust, Niobium)
                .outputItems(dust, HastelloyX, 19)
                .duration(210).EUt(VA[IV])
                .save(provider);

        MIXER_RECIPES.recipeBuilder("maraging_steel_300")
                .inputItems(dust, Iron, 16)
                .inputItems(dust, Titanium)
                .inputItems(dust, Aluminium)
                .inputItems(dust, Nickel, 4)
                .inputItems(dust, Cobalt, 2)
                .outputItems(dust, MaragingSteel300, 24)
                .duration(230).EUt(VA[IV])
                .save(provider);

        MIXER_RECIPES.recipeBuilder("stellite_100")
                .inputItems(dust, Iron, 4)
                .inputItems(dust, Chromium, 3)
                .inputItems(dust, Tungsten, 2)
                .inputItems(dust, Molybdenum)
                .outputItems(dust, Stellite100, 10)
                .duration(200).EUt(VA[IV])
                .save(provider);

        MIXER_RECIPES.recipeBuilder("titanium_carbide")
                .inputItems(dust, Titanium)
                .inputItems(dust, Carbon)
                .outputItems(dust, TitaniumCarbide, 2)
                .duration(160).EUt(VA[EV])
                .save(provider);

        MIXER_RECIPES.recipeBuilder("titanium_tungsten_carbide")
                .inputItems(dust, TungstenCarbide)
                .inputItems(dust, TitaniumCarbide, 2)
                .outputItems(dust, TitaniumTungstenCarbide, 3)
                .duration(180).EUt(VA[IV])
                .save(provider);
    }

    private static void registerBlastAlloyRecipes(Consumer<FinishedRecipe> provider) {
        ingot.executeHandler(PropertyKey.ALLOY_BLAST, (tagPrefix, material, property) -> generateAlloyBlastRecipes(tagPrefix, material, property, provider));
    }

    /**
     * Generates alloy blast recipes for a material
     *
     * @param material the material to generate for
     * @param property the blast property of the material
     */
    public static void generateAlloyBlastRecipes(@Nullable TagPrefix unused, @Nonnull Material material,
                                                 @Nonnull AlloyBlastProperty property,
                                                 @Nonnull Consumer<FinishedRecipe> provider) {
        if (material.hasProperty(PropertyKey.BLAST)) {
            property.getRecipeProducer().produce(material, material.getProperty(PropertyKey.BLAST), provider);
        }
    }
}
