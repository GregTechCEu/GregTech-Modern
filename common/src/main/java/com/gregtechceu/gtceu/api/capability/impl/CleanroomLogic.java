package com.gregtechceu.gtceu.api.capability.impl;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.ICleanroomProvider;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenance;
import com.gregtechceu.gtceu.api.syncdata.EnhancedFieldManagedStorage;
import com.gregtechceu.gtceu.api.syncdata.IEnhancedManaged;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.lowdragmc.lowdraglib.syncdata.IManagedStorage;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;

public class CleanroomLogic implements IEnhancedManaged {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(CleanroomLogic.class);

    @Getter
    protected final EnhancedFieldManagedStorage fieldManagedStorage = new EnhancedFieldManagedStorage(this);

    public static final int BASE_CLEAN_AMOUNT = 5;

    @Persisted @DescSynced
    private int maxProgress = 0;
    @Persisted @DescSynced
    private int progressTime = 0;

    private final int minEnergyTier;

    private final MetaMachine machine;
    @Persisted @DescSynced
    private final boolean hasMaintenance;

    @Persisted @DescSynced
    private boolean isActive;
    @Persisted @DescSynced
    private boolean isWorkingEnabled = true;
    @Persisted @DescSynced @Getter
    private boolean wasActiveAndNeedsUpdate;
    @Persisted @DescSynced
    private boolean hasNotEnoughEnergy;

    public CleanroomLogic(MetaMachine machine, int minEnergyTier) {
        this.machine = machine;
        this.minEnergyTier = minEnergyTier;
        this.hasMaintenance = ConfigHolder.INSTANCE.machines.enableMaintenance && ((IMaintenance) machine).hasMaintenanceMechanics();
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public IManagedStorage getSyncStorage() {
        return fieldManagedStorage;
    }

    /**
     * Performs the actual cleaning
     * Call this method every tick in update
     */
    public void updateLogic() {
        // cleanrooms which cannot work do nothing
        if (!this.isWorkingEnabled) return;

        // all maintenance problems not fixed means the machine does not run
        if (hasMaintenance && ((IMaintenance) machine).getNumMaintenanceProblems() > 5) return;

        // drain the energy
        if (consumeEnergy(true)) {
            consumeEnergy(false);
        } else {
            if (progressTime >= 2) {
                if (ConfigHolder.INSTANCE.machines.recipeProgressLowEnergy) this.progressTime = 1;
                else this.progressTime = Math.max(1, progressTime - 2);
            }
            hasNotEnoughEnergy = true;

            // the cleanroom does not have enough energy, so it looses cleanliness
            if (machine.getOffsetTimer() % maxProgress == 0) {
                adjustCleanAmount(true);
            }
            return;
        }

        if (!this.isActive) setActive(true);

        // increase progress
        progressTime++;
        if (progressTime % getMaxProgress() != 0) return;
        progressTime = 0;

        adjustCleanAmount(false);
    }

    protected void adjustCleanAmount(boolean shouldRemove) {
        int amountToClean = BASE_CLEAN_AMOUNT * (getTierDifference() + 1);
        if (shouldRemove) amountToClean *= -1;

        // each maintenance problem lowers gain by 1
        if (hasMaintenance) amountToClean -= ((IMaintenance) machine).getNumMaintenanceProblems();
        ((ICleanroomProvider) machine).adjustCleanAmount(amountToClean);
    }

    protected boolean consumeEnergy(boolean simulate) {
        return ((ICleanroomProvider) machine).drainEnergy(simulate);
    }

    public void invalidate() {
        this.progressTime = 0;
        this.maxProgress = 0;
        setActive(false);
    }

    /**
     * @return true if the cleanroom is active
     */
    public boolean isActive() {
        return this.isActive && isWorkingEnabled();
    }

    /**
     * @param active the new state of the cleanroom's activity: true to change to active, else false
     */
    public void setActive(boolean active) {
        if (this.isActive != active) {
            this.isActive = active;
            this.machine.markDirty();
        }
    }

    /**
     * @param workingEnabled the new state of the cleanroom's ability to work: true to change to enabled, else false
     */
    public void setWorkingEnabled(boolean workingEnabled) {
        this.isWorkingEnabled = workingEnabled;
        this.machine.markDirty();
    }

    /**
     * @return whether working is enabled for the logic
     */
    public boolean isWorkingEnabled() {
        return isWorkingEnabled;
    }

    /**
     * @return whether the cleanroom is currently working
     */
    public boolean isWorking() {
        return isActive && !hasNotEnoughEnergy && isWorkingEnabled;
    }

    /**
     * @return the current progress towards completing one cycle of the cleanroom
     */
    public int getProgressTime() {
        return this.progressTime;
    }

    public void setMaxProgress(int maxProgress) {
        this.maxProgress = maxProgress;
    }

    public int getMaxProgress() {
        return this.maxProgress;
    }

    public int getProgressPercent() {
        return (int) ((1.0F * getProgressTime() / getMaxProgress()) * 100);
    }

    protected int getTierDifference() {
        return ((ICleanroomProvider) machine).getEnergyTier() - minEnergyTier;
    }

    @Override
    public void scheduleRenderUpdate() {
        this.machine.scheduleRenderUpdate();
    }

    @Override
    public void onChanged() {
        this.machine.onChanged();
    }


    /**
     * @return whether the cleanroom was active and needs an update
     */
    public boolean wasActiveAndNeedsUpdate() {
        return this.wasActiveAndNeedsUpdate;
    }

    /**
     * set whether the cleanroom was active and needs an update
     *
     * @param wasActiveAndNeedsUpdate the state to set
     */
    public void setWasActiveAndNeedsUpdate(boolean wasActiveAndNeedsUpdate) {
        this.wasActiveAndNeedsUpdate = wasActiveAndNeedsUpdate;
    }
}
