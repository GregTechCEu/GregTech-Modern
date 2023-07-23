package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public interface IResearchRecipeLogic {

    /**
     * Add a recipe to the data stick registry for the {@link com.gregtechceu.gtceu.common.machine.trait.ResearchRecipeLogic}
     *
     * @param researchId the ID to match recipes to, typically derived from the recipe output
     * @param recipe the recipe to add to the registry
     */
    void addDataStickEntry(@Nonnull String researchId, @Nonnull GTRecipe recipe);

    /**
     * @param researchId the ID to match recipes to, typically derived from the recipe output
     * @return the set of recipes assigned to the ID
     */
    @Nullable
    Collection<GTRecipe> getDataStickEntry(@Nonnull String researchId);

    /**
     * Remove a recipe from the data stick registry for the {@link com.gregtechceu.gtceu.common.machine.trait.ResearchRecipeLogic}
     *
     * @param researchId the ID to match recipes to, typically derived from the recipe output
     * @param recipe the recipe to remove from the registry
     * @return true if the recipe was successfully removed, otherwise false
     */
    boolean removeDataStickEntry(@Nonnull String researchId, @Nonnull GTRecipe recipe);
}
