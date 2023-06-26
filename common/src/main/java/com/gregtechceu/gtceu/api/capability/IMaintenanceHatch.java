package com.gregtechceu.gtceu.api.capability;

import net.minecraft.util.Tuple;

public interface IMaintenanceHatch {

    /**
     * @return true if this is a Full-Auto Maintenance Hatch, false otherwise.
     */
    boolean isFullAuto();

    /**
     * Sets this Maintenance Hatch as being duct taped
     * @param isTaped is the state of the hatch being taped or not
     */
    void setTaped(boolean isTaped);

    boolean isTaped();

    /**
     * Stores maintenance data to this MetaTileEntity
     * @param maintenanceProblems is the byte value representing the problems
     * @param timeActive is the int value representing the total time the parent multiblock has been active
     */
    void storeMaintenanceData(byte maintenanceProblems, int timeActive);

    /**
     *
     * @return whether this maintenance hatch has maintenance data
     */
    boolean hasMaintenanceData();

    /**
     * reads this MetaTileEntity's maintenance data
     * @return Tuple of Byte, Integer corresponding to the maintenance problems, and total time active
     */
    Tuple<Byte, Integer> readMaintenanceData();

    double getDurationMultiplier();

    double getTimeMultiplier();

    boolean startWithoutProblems();
}