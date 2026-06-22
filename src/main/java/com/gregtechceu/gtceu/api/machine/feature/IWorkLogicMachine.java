package com.gregtechceu.gtceu.api.machine.feature;

import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.WorkLogic;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IWorkLogicMachine extends IMachineFeature, IWorkable {

    @NotNull
    WorkLogic getWorkLogic();

    default void notifyWorkStatusChanged(WorkLogic.Status oldStatus, WorkLogic.Status newStatus) {
        if (this instanceof MetaMachine metaMachine &&
                metaMachine.getRenderState().hasProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS)) {
            metaMachine.setRenderState(metaMachine.getRenderState()
                    .setValue(GTMachineModelProperties.RECIPE_LOGIC_STATUS, newStatus));
        }
    }

    default void notifyWorkingEnabledChanged(boolean oldValue, boolean newValue) {
        if (this instanceof MetaMachine metaMachine &&
                metaMachine.getRenderState().hasProperty(GTMachineModelProperties.IS_WORKING_ENABLED)) {
            metaMachine.setRenderState(metaMachine.getRenderState()
                    .setValue(GTMachineModelProperties.IS_WORKING_ENABLED, newValue));
        }
    }

    default boolean isWorkLogicAvailable() {
        return true;
    }

    default boolean keepSubscribing() {
        return true;
    }

    default void setStatus(WorkLogic.Status status) {
        getWorkLogic().setStatus(status);
    }

    default void setWaiting(@Nullable Component reason) {
        getWorkLogic().setWaiting(reason);
    }

    @Override
    default boolean isWorkingEnabled() {
        return getWorkLogic().isWorkingEnabled();
    }

    @Override
    default void setWorkingEnabled(boolean isWorkingAllowed) {
        getWorkLogic().setWorkingEnabled(isWorkingAllowed);
    }

    @Override
    default void setSuspendAfterFinish(boolean suspendAfterFinish) {
        getWorkLogic().setSuspendAfterFinish(suspendAfterFinish);
    }

    @Override
    default boolean isSuspendAfterFinish() {
        return getWorkLogic().isSuspendAfterFinish();
    }

    @Override
    default boolean isActive() {
        return getWorkLogic().isActive();
    }

    @Override
    default int getProgress() {
        return 0;
    };

    @Override
    default int getMaxProgress() {
        return 0;
    };
}
