package com.gregtechceu.gtceu.api.machine.feature;

import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.machine.trait.feature.IRecipeLogicModifierTrait;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.jade.provider.RecipeLogicProvider;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A machine can handle recipes.
 */
public interface IRecipeLogicMachine extends IRecipeCapabilityHolder, IMachineFeature, IWorkable, IVoidable {

    /**
     * RecipeType held
     */
    @NotNull
    GTRecipeType[] getRecipeTypes();

    @NotNull
    GTRecipeType getRecipeType();

    int getActiveRecipeType();

    void setActiveRecipeType(int type);

    /**
     * Recipe logic
     */
    @NotNull
    RecipeLogic getRecipeLogic();

    default GTRecipe fullModifyRecipe(GTRecipe recipe) {
        return doModifyRecipe(RecipeHelper.trimRecipeOutputs(recipe, this.getOutputLimits()));
    }

    /**
     * Override it to modify recipe on the fly e.g. applying overclock, change chance, etc
     *
     * @param recipe recipe from detected from GTRecipeType
     * @return modified recipe.
     *         null -- this recipe is unavailable
     */
    @Nullable
    default GTRecipe doModifyRecipe(GTRecipe recipe) {
        recipe = self().getDefinition().getRecipeModifier().applyModifier(self(), recipe);
        if (recipe == null) return null;

        for (var rlTrait : self().getTraitHolder().getTraitsByInterface(IRecipeLogicModifierTrait.class)) {
            recipe = rlTrait.modifyRecipe(recipe);
            if (recipe == null) return null;
        }
        return recipe;
    }

    /**
     * Whether the recipe logic should work or waiting for next {@link RecipeLogic#updateTickSubscription()}.
     */
    default boolean isRecipeLogicAvailable() {
        return true;
    }

    /**
     * Called when the recipe logic status changes
     * 
     * @param oldStatus Old recipe logic status
     * @param newStatus New recipe logic status
     */
    default void recipeLogicStatusChanged(RecipeLogic.Status oldStatus, RecipeLogic.Status newStatus) {
        for (var rlTrait : self().getTraitHolder().getTraitsByInterface(IRecipeLogicModifierTrait.class)) {
            rlTrait.recipeLogicStatusChanged(oldStatus, newStatus);
        }
    }

    /**
     * Called when a recipe is about to be run, just before inputs are consumed.
     *
     * @return false to cancel the recipe, true to continue
     *
     * @see RecipeLogic#setupRecipe(GTRecipe)
     */
    default boolean beforeWorking(@Nullable GTRecipe recipe) {
        if (!self().getDefinition().getBeforeWorking().test(this, recipe)) return false;

        for (var rlTrait : self().getTraitHolder().getTraitsByInterface(IRecipeLogicModifierTrait.class)) {
            if (!rlTrait.beforeWorking(recipe)) return false;
        }
        return true;
    }

    /**
     * Called every tick while the recipe is working.
     *
     * @return false to interrupt and suspend the recipe, true to continue working
     *
     * @see RecipeLogic#handleRecipeWorking()
     */
    default boolean onWorking() {
        if (!self().getDefinition().getOnWorking().test(this)) return false;

        for (var rlTrait : self().getTraitHolder().getTraitsByInterface(IRecipeLogicModifierTrait.class)) {
            if (!rlTrait.onWorking()) return false;
        }
        return true;
    }

    /**
     * Called per tick in {@link RecipeLogic#handleRecipeWorking()}
     */
    default void onWaiting() {
        self().getDefinition().getOnWaiting().accept(this);
    }

    /**
     * Called when the recipe finishes, before outputs are produced.
     *
     * @see RecipeLogic#onRecipeFinish()
     */
    default void afterWorking() {
        self().getDefinition().getAfterWorking().accept(this);
        for (var rlTrait : self().getTraitHolder().getTraitsByInterface(IRecipeLogicModifierTrait.class)) {
            rlTrait.afterWorking();
        }
    }

    default boolean shouldWorkingPlaySound() {
        return ConfigHolder.INSTANCE.machines.machineSounds &&
                (!(self() instanceof IMufflableMachine mufflableMachine) || !mufflableMachine.isMuffled());
    }

    /**
     * Display recipe voltage used by {@link RecipeLogicProvider}
     */

    default long getDisplayRecipeVoltage() {
        return -1;
    }

    /**
     * Max output EU/t of a generator
     * is negative when it is not a generator
     */
    default long getDisplayGeneratorPower() {
        return -1;
    }

    //////////////////////////////////////
    // ******* IWorkable ********//
    //////////////////////////////////////
    @Override
    default boolean isWorkingEnabled() {
        return getRecipeLogic().isWorkingEnabled();
    }

    @Override
    default void setWorkingEnabled(boolean isWorkingAllowed) {
        getRecipeLogic().setWorkingEnabled(isWorkingAllowed);
    }

    @Override
    default void setSuspendAfterFinish(boolean suspendAfterFinish) {
        getRecipeLogic().setSuspendAfterFinish(suspendAfterFinish);
    }

    @Override
    default boolean isSuspendAfterFinish() {
        return getRecipeLogic().isSuspendAfterFinish();
    }

    @Override
    default int getProgress() {
        return getRecipeLogic().getProgress();
    }

    @Override
    default int getMaxProgress() {
        return getRecipeLogic().getMaxProgress();
    }

    @Override
    default boolean isActive() {
        return getRecipeLogic().isActive();
    }
}
