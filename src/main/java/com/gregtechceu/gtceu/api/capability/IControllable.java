package com.gregtechceu.gtceu.api.capability;

public interface IControllable {

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

}
