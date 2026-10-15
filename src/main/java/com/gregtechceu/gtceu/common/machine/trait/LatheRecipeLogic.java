package com.gregtechceu.gtceu.common.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class LatheRecipeLogic extends RecipeLogic {

    // Non-Rod recipes retain normal lathe behavior.
    public static final String LUBRICATED_ROD = "lubricated_rod";
    public static final int LUBRICANT_AMOUNT = 25;

    public static final GTRecipeType.ICustomRecipeLogic REPRESENTATIVE_RECIPES = new GTRecipeType.ICustomRecipeLogic() {

        @Override
        public @Nullable GTRecipe createCustomRecipe(IRecipeCapabilityHolder holder) {
            // Recipe selection remains in the machine logic
            return null;
        }

        @Override
        public void buildRepresentativeRecipes() {
            var type = GTRecipeTypes.LATHE_RECIPES.value();
            for (var category : type.getCategories()) {
                for (var recipe : List.copyOf(type.getRecipesInCategory(category))) {
                    // Both viewers may use this hook, don't actually generate recipeDB entries for synth representative
                    // recipes.
                    if (recipe.id.getPath().startsWith("/")) continue;
                    var lubricated = createLubricatedRecipe(recipe);
                    if (lubricated == null) continue;
                    lubricated.id = recipe.id.withPrefix("/").withSuffix("_lubricated");
                    type.addToCategoryMap(category, lubricated);
                }
            }
        }
    };

    private static boolean isLubricatedRod(GTRecipe recipe) {
        return recipe != null && recipe.data.contains(LUBRICATED_ROD);
    }

    @Override
    public boolean checkMatchedRecipeAvailable(GTRecipe match) {
        var prepared = createLubricatedRecipe(match);
        if (prepared == null || !RecipeHelper.handleRecipe(getRLMachine(), match, IO.IN,
                Collections.singletonMap(FluidRecipeCapability.CAP, prepared.inputs.get(FluidRecipeCapability.CAP)),
                Collections.emptyMap(), false, true).isSuccess()) {
            return super.checkMatchedRecipeAvailable(match);
        }
        return super.checkMatchedRecipeAvailable(prepared);
    }

    /** Creates the lubricated variant shared by XEI's and logic. */
    public static @Nullable GTRecipe createLubricatedRecipe(GTRecipe match) {
        if (!isLubricatedRod(match)) return null;

        var rodId = ResourceLocation.tryParse(match.data.getString(LUBRICATED_ROD));
        if (rodId == null || !BuiltInRegistries.ITEM.containsKey(rodId)) {
            return null;
        }
        var wet = match.recipeType.recipeBuilder(match.id)
                .inputFluids(GTMaterials.Lubricant, LUBRICANT_AMOUNT)
                .outputItems(BuiltInRegistries.ITEM.get(rodId), 2).build();
        // Handle before modifiers so lube is handled properly, eg. 2 parallel = 50mb lube.
        var prepared = match.copy();
        prepared.inputs.putAll(wet.inputs);
        prepared.outputs.put(ItemRecipeCapability.CAP, wet.outputs.get(ItemRecipeCapability.CAP));
        return prepared;
    }

    @Override
    public void findAndHandleRecipe() {
        // Recheck lubricant before reusing a cached rod recipe.
        if (isLubricatedRod(lastUnrolledRecipe)) markLastRecipeDirty();
        super.findAndHandleRecipe();
    }

    @Override
    public void onRecipeFinish() {
        // Finish the current operation unchanged, then recheck it.
        if (isLubricatedRod(lastRecipe)) markLastRecipeDirty();
        super.onRecipeFinish();
    }
}
