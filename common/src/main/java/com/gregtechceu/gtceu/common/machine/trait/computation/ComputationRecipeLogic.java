package com.gregtechceu.gtceu.common.machine.trait.computation;

import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.IOpticalComputationProvider;
import com.gregtechceu.gtceu.api.capability.IOpticalComputationReceiver;
import com.gregtechceu.gtceu.api.capability.recipe.CWURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class ComputationRecipeLogic extends RecipeLogic {
    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(ComputationRecipeLogic.class,  RecipeLogic.MANAGED_FIELD_HOLDER);

    private final ComputationType type;
    /*
     * Whether recipe duration should be treated as a total CWU value (so, incremented by the CWU/t used each tick),
     * or normally (increase by 1 for each successful draw of CWU/t). If this value is true, the logic will attempt
     * to draw as much CWU/t as possible to try and accelerate the computation process, and CWU/t is treated as a
     * minimum value instead of a static cost.
     */
    @Persisted
    @Getter
    protected boolean isDurationTotalCWU;
    @Persisted
    @Getter
    protected int recipeCWUt;
    @Getter
    private boolean hasNotEnoughComputation;
    private int currentDrawnCWUt;

    public ComputationRecipeLogic(IRecipeLogicMachine machine, ComputationType type) {
        super(machine);
        this.type = type;
    }

    @NotNull
    public IOpticalComputationProvider getComputationProvider() {
        IOpticalComputationReceiver controller = (IOpticalComputationReceiver) this.machine;
        return controller.getComputationProvider();
    }

    @Override
    protected boolean checkMatchedRecipeAvailable(GTRecipe match) {
        if (!super.checkMatchedRecipeAvailable(match)) {
            return false;
        }
        if (!match.inputs.containsKey(CWURecipeCapability.CAP) && !match.tickInputs.containsKey(CWURecipeCapability.CAP)) {
            return true;
        }
        IOpticalComputationProvider provider = getComputationProvider();
        int recipeCWUt = CWURecipeCapability.CAP.of(match.inputs.containsKey(CWURecipeCapability.CAP) ? match.getInputContents(CWURecipeCapability.CAP).get(0).content : match.getTickInputContents(CWURecipeCapability.CAP).get(0).content);
        return provider.requestCWUt(recipeCWUt, true) >= recipeCWUt;
    }

    @Override
    public void setupRecipe(GTRecipe recipe) {
        super.setupRecipe(recipe);
        this.recipeCWUt = recipe.getTickInputContents(CWURecipeCapability.CAP).stream().map(Content::getContent).map(CWURecipeCapability.CAP::of).reduce(0, Integer::sum);
        this.isDurationTotalCWU = !recipe.getInputContents(CWURecipeCapability.CAP).isEmpty();
    }

    @Override
    public void handleRecipeWorking() {
        if (recipeCWUt == 0) {
            super.handleRecipeWorking();
            return;
        }

        if (machine.getCapabilitiesProxy().get(IO.IN, EURecipeCapability.CAP) == null) return;
        IEnergyContainer container = new EnergyContainerList(machine.getCapabilitiesProxy().get(IO.IN, EURecipeCapability.CAP).stream().filter(IEnergyContainer.class::isInstance).map(IEnergyContainer.class::cast).toList());

        long recipeEUt = this.lastRecipe.getTickInputContents(EURecipeCapability.CAP).stream().map(Content::getContent).map(EURecipeCapability.CAP::of).reduce(0L, Long::sum);
        long pulled = container.changeEnergy(-recipeEUt);
        if (getStatus() != Status.WAITING && pulled > 0) {

            IOpticalComputationProvider provider = getComputationProvider();
            int availableCWUt = provider.requestCWUt(Integer.MAX_VALUE, true);
            if (availableCWUt >= recipeCWUt) {
                // carry on as normal
                this.hasNotEnoughComputation = false;
                if (isDurationTotalCWU) {
                    // draw as much CWU as possible, and increase progress by this amount
                    currentDrawnCWUt = provider.requestCWUt(availableCWUt, false);
                    progress += currentDrawnCWUt;
                } else {
                    // draw only the recipe CWU/t, and increase progress by 1
                    provider.requestCWUt(recipeCWUt, false);
                    progress++;
                }
                if (progress >= duration) {
                    onRecipeFinish();
                }
            } else {
                currentDrawnCWUt = 0;
                this.hasNotEnoughComputation = true;
                // only decrement progress for low CWU/t if we need a steady supply
                if (type == ComputationType.STEADY) {
                    if (ConfigHolder.INSTANCE.machines.recipeProgressLowEnergy) {
                        this.progress = 1;
                    } else {
                        this.progress = Math.max(1, progress - 2);
                    }
                }
            }
            if (this.isWaiting()/* && getEnergyInputPerSecond() > 19L * recipeEUt*/) {
                this.setStatus(Status.WORKING);
            }
        } else if (recipeEUt > 0) {
            this.setStatus(Status.WAITING);
            if (ConfigHolder.INSTANCE.machines.recipeProgressLowEnergy) {
                this.progress = 1;
            } else {
                this.progress = Math.max(1, progress - 2);
            }
        }
        if (pulled <= 0) {
            container.addEnergy(recipeEUt);
        }
    }

    @Override
    public void onRecipeFinish() {
        super.onRecipeFinish();
        this.recipeCWUt = 0;
        this.isDurationTotalCWU = false;
        this.hasNotEnoughComputation = false;
        this.currentDrawnCWUt = 0;
    }

    public int getCurrentDrawnCWUt() {
        return isDurationTotalCWU ? currentDrawnCWUt : recipeCWUt;
    }

    /**
     * @return Whether TOP / WAILA should show the recipe progress as duration or as total computation.
     */
    public boolean shouldShowDuration() {
        return !isDurationTotalCWU;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public enum ComputationType {
        /**
         * CWU/t works like EU/t. If there is not enough, recipe reverts progress/halts
         */
        STEADY,
        /**
         * CWU/t works like a total input. If there is not enough, recipe halts at current progress time.
         * Progress only increases on ticks where enough computation is present. Energy will always be drawn.
         */
        SPORADIC
    }
}
