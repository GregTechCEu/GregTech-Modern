package com.gregtechceu.gtceu.api.machine.feature;

import com.gregtechceu.gtceu.api.capability.ICleanroomReceiver;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.machine.trait.WorkLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerGroup;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.jade.provider.RecipeLogicProvider;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A machine can handle recipes.
 */
public interface IRecipeLogicMachine extends IRecipeCapabilityHolder, IWorkLogicMachine, ICleanroomReceiver,
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
     * Recipe logic
     */
    @NotNull
    RecipeLogic getRecipeLogic();

    @Override
    default WorkLogic getWorkLogic() {
        return getRecipeLogic();
    }

    /**
     * Override it to modify recipe on the fly e.g. applying overclock, change chance, etc
     *
     * @param recipe recipe from detected from GTRecipeType
     * @return modified recipe.
     *         null -- this recipe is unavailable
     */
    @Nullable
    default Component modifyRecipe(GTRecipe recipe, RecipeHandlerGroup group) {
        for(var modifier: self().getDefinition().getRecipeModifiers()) {
            var failReason = modifier.apply(self(), group, recipe);
            if (failReason != null) return failReason;
        }
        return null;
    }

    /**
     * Whether the recipe logic should keep subscribing tick logic when no recipe is available after one cycle.
     * if false. you should call {@link RecipeLogic#updateTickSubscription()} manually later to active recipe logic
     * again.
     */
    @Override
    default boolean keepSubscribing() {
        return false;
    }

    /**
     * Called in {@link RecipeLogic#setupRecipe(GTRecipe)} ()}
     */
    default Component beforeWorking(@Nullable GTRecipe recipe) {
        return self().getDefinition().getBeforeWorking().apply(this, recipe);
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
     * Always try {@link IRecipeLogicMachine#modifyRecipe(GTRecipe, RecipeHandlerGroup)} before setting up recipe.
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

    /**
     * Display recipe voltage used by {@link RecipeLogicProvider}
     */

    default long getDisplayRecipeVoltage() {
        return -1;
    }

    @Override
    default int getProgress() {
        return getRecipeLogic().getProgress();
    }

    @Override
    default int getMaxProgress() {
        return getRecipeLogic().getMaxProgress();
    }
}
