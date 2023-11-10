package com.gregtechceu.gtceu.common.machine.trait;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CleanroomMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class CleanroomLogic extends RecipeLogic implements IWorkable {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(CleanroomLogic.class, RecipeLogic.MANAGED_FIELD_HOLDER);
    public static final int BASE_CLEAN_AMOUNT = 5;
    @Setter @Nullable
    private IMaintenanceMachine maintenanceMachine;
    @Setter @Nullable
    private IEnergyContainer energyContainer;
    /**
     * whether the cleanroom was active and needs an update
     */
    @Getter @Setter @Persisted
    private boolean isActiveAndNeedsUpdate;

    public CleanroomLogic(CleanroomMachine machine) {
        super(machine);
    }

    @Override
    public CleanroomMachine getMachine() {
        return (CleanroomMachine) machine;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    /**
     * Performs the actual cleaning
     * Call this method every tick in update
     */
    public void serverTick() {
        if (!isSuspend() && duration > 0) {
            // all maintenance problems not fixed means the machine does not run
            if (maintenanceMachine == null || maintenanceMachine.getNumMaintenanceProblems() < 6) {
                // drain the energy
                if (!consumeEnergy()) {
                    if (progress > 0 && machine.dampingWhenWaiting()) {
                        if (ConfigHolder.INSTANCE.machines.recipeProgressLowEnergy) {
                            this.progress = 1;
                        } else {
                            this.progress = Math.max(1, progress - 2);
                        }
                    }

                    // the cleanroom does not have enough energy, so it looses cleanliness
                    if (machine.self().getOffsetTimer() % duration == 0) {
                        adjustCleanAmount(true);
                    }

                    setWaiting(Component.translatable("gtceu.recipe_logic.insufficient_in").append(": ").append(EURecipeCapability.CAP.getTraslateComponent()));
                    return;
                }
                // increase progress
                if (progress++ < getMaxProgress()) {
                    machine.onWorking();
                    return;
                }
                progress = 0;
                machine.beforeWorking();
                adjustCleanAmount(false);
                setStatus(Status.WORKING);
            } else {
                // has all maintenance problems
                if (progress > 0) {
                    progress --;
                }
                if (machine.self().getOffsetTimer() % duration == 0) {
                    adjustCleanAmount(true);
                }
                setStatus(Status.IDLE);
                machine.afterWorking();
            }
        }

        if (isSuspend()) {
            // machine isn't working enabled
            if (subscription != null) {
                subscription.unsubscribe();
                subscription = null;
            }
        }
    }

    protected void adjustCleanAmount(boolean declined) {
        int amountToClean = BASE_CLEAN_AMOUNT * (getTierDifference() + 1);
        if (declined) amountToClean *= -1;

        // each maintenance problem lowers gain by 1
        if (maintenanceMachine != null) {
            amountToClean -= maintenanceMachine.getNumMaintenanceProblems();
        }
        getMachine().adjustCleanAmount(amountToClean);
    }

    protected boolean consumeEnergy() {
        var cleanroom = getMachine();
        long energyToDrain = cleanroom.isClean() ? (long) Math.min(4, Math.pow(4, cleanroom.getTier())) : GTValues.VA[cleanroom.getTier()];
        if (energyContainer != null) {
            long resultEnergy = energyContainer.getEnergyStored() - energyToDrain;
            if (resultEnergy >= 0L && resultEnergy <= energyContainer.getEnergyCapacity()) {
                energyContainer.removeEnergy(energyToDrain);
                return true;
            }
        }
        return false;
    }

    protected int getTierDifference() {
        int minEnergyTier = GTValues.LV;
        return getMachine().getTier() - minEnergyTier;
    }

    /** max progress is based on the dimensions of the structure: (x^3)-(x^2)
    /* taller cleanrooms take longer than wider ones
    /*  minimum of 100 is a 5x5x5 cleanroom: 125-25=100 ticks
    **/
    public void setDuration(int max) {
        this.duration = max;
    }
}
