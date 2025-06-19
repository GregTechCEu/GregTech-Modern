package com.gregtechceu.gtceu.api.capability;

import net.minecraft.world.level.block.state.properties.BooleanProperty;

public interface IControllable {

    BooleanProperty WORKING_ENABLED_PROPERTY = BooleanProperty.create("working_enabled");

    /**
     * @return true if the controllable is allowed to work
     */
    boolean isWorkingEnabled();

    /**
     * Set if the controllable can work or not
     *
     * @param isWorkingAllowed true if the workable can work, otherwise false
     */
    void setWorkingEnabled(boolean isWorkingAllowed);

    default void setSuspendAfterFinish(boolean suspendAfterFinish) {}
}
