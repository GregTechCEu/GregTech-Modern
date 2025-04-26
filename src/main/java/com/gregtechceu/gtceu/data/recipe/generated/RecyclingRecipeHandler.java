package com.gregtechceu.gtceu.data.recipe.generated;

import com.gregtechceu.gtceu.api.material.ChemicalHelper;
import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.material.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.material.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.gregtechceu.gtceu.data.recipe.misc.RecyclingRecipes;

import net.minecraft.data.recipes.RecipeOutput;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.gregtechceu.gtceu.api.tag.TagPrefix.*;

public final class RecyclingRecipeHandler {

    private static final List<TagPrefix> IGNORE_ARC_SMELTING = Arrays.asList(ingot, gem, nugget);

    private RecyclingRecipeHandler() {}

    public static void run(@NotNull RecipeOutput provider, @NotNull Material material) {
        // registers universal maceration recipes for specified ore prefixes
        for (TagPrefix prefix : GTRegistries.TAG_PREFIXES) {
            if (prefix.generateRecycling()) {
                processCrushing(provider, prefix, material);
            }
        }
    }

    private static void processCrushing(@NotNull RecipeOutput provider, @NotNull TagPrefix prefix,
                                        @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(prefix) || !material.hasProperty(PropertyKey.DUST)) {
            return;
        }

        ArrayList<MaterialStack> materialStacks = new ArrayList<>();
        materialStacks.add(new MaterialStack(material, prefix.getMaterialAmount(material)));
        materialStacks.addAll(prefix.secondaryMaterials());
        // only ignore arc smelting for blacklisted prefixes if yielded material is the same as input material
        // if arc smelting gives different material, allow it
        boolean ignoreArcSmelting = IGNORE_ARC_SMELTING.contains(prefix) &&
                !(material.hasProperty(PropertyKey.INGOT) &&
                        material.getProperty(PropertyKey.INGOT).getArcSmeltingInto() != material);
        RecyclingRecipes.registerRecyclingRecipes(provider, ChemicalHelper.get(prefix, material), materialStacks,
                ignoreArcSmelting, prefix);
    }
}
