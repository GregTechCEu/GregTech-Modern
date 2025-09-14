package com.gregtechceu.gtceu.api.machine.feature;

import com.gregtechceu.gtceu.api.capability.ICleanroomReceiver;
import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.config.ConfigHolder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A machine can handle recipes.
 */
public interface IRecipeLogicMachine extends IRecipeCapabilityHolder, IMachineFeature, IWorkable, ICleanroomReceiver,
                                     IVoidable {

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
     * Called when recipe logic status changed
     */
    default void notifyStatusChanged(RecipeLogic.Status oldStatus, RecipeLogic.Status newStatus) {}

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
        return self().getDefinition().getRecipeModifier().applyModifier(self(), recipe);
    }

    /**
     * Whether the recipe logic should keep subscribing tick logic when no recipe is available after one cycle.
     * if false. you should call {@link RecipeLogic#updateTickSubscription()} manually later to active recipe logic
     * again.
     */
    default boolean keepSubscribing() {
        return true;
    }

    /**
     * Whether the recipe logic should work or waiting for next {@link RecipeLogic#updateTickSubscription()}.
     */
    default boolean isRecipeLogicAvailable() {
        return true;
    }

    /**
     * Called in {@link RecipeLogic#setupRecipe(GTRecipe)} ()}
     */
    default boolean beforeWorking(@Nullable GTRecipe recipe) {
        return self().getDefinition().getBeforeWorking().test(this, recipe);
    }

    /**
     * Called per tick in {@link RecipeLogic#handleRecipeWorking()}
     */
    default boolean onWorking() {
        return self().getDefinition().getOnWorking().test(this);
    }

    /**
     * Called per tick in {@link RecipeLogic#handleRecipeWorking()}
     */
    default void onWaiting() {
        self().getDefinition().getOnWaiting().accept(this);
    }

    /**
     * Called in {@link RecipeLogic#onRecipeFinish()} before outputs are produced
     */
    default void afterWorking() {
        self().getDefinition().getAfterWorking().accept(this);
    }

    /**
     * Whether progress decrease when machine is waiting for pertick ingredients. (e.g. lack of EU)
     */
    default boolean regressWhenWaiting() {
        return self().getDefinition().isRegressWhenWaiting();
    }

    /**
     * Always try {@link IRecipeLogicMachine#fullModifyRecipe(GTRecipe)} before setting up recipe.
     * 
     * @return true - will map {@link RecipeLogic#lastOriginRecipe} to the latest recipe for next round when finishing.
     *         false - keep using the {@link RecipeLogic#lastRecipe}, which is already modified.
     */
    default boolean alwaysTryModifyRecipe() {
        // make it *always* do overclock and parallel so that the machine doesn't get stuck running a lower-tier recipe
        // in any possible scenario.
        return true;
    }

    default boolean shouldWorkingPlaySound() {
        return ConfigHolder.INSTANCE.machines.machineSounds &&
                (!(self() instanceof IMufflableMachine mufflableMachine) || !mufflableMachine.isMuffled());
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
