package com.gregtechceu.gtceu.api.capability;

import com.gregtechceu.gtceu.api.machine.feature.IMachineFeature;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;

public interface IDataAccessHatch extends IMachineFeature {

    /**
     * If passed a {@code seen} context, you must use {@link #isRecipeAvailable(GTRecipe, Collection)} to prevent
     * infinite recursion
     *
     * @param recipe the recipe to check
     * @return if the recipe is available for use
     */
    default boolean isRecipeAvailable(@Nonnull GTRecipe recipe) {
        Collection<IDataAccessHatch> list = new ArrayList<>();
        list.add(this);
        return isRecipeAvailable(recipe, list);
    }

    /**
     * @param recipe the recipe to check
     * @param seen   the hatches already checked
     * @return if the recipe is available for use
     */
    boolean isRecipeAvailable(@Nonnull GTRecipe recipe, @Nonnull Collection<IDataAccessHatch> seen);

    /**
     * @return true if this Data Access Hatch is creative or not
     */
    boolean isCreative();
}
