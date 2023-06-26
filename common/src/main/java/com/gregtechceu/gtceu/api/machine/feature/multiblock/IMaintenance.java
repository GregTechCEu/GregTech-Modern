package com.gregtechceu.gtceu.api.machine.feature.multiblock;

/**
 * @author KilaBash
 * @date 2023/3/6
 * @implNote IMaintenance
 */
public interface IMaintenance {
    byte getMaintenanceProblems();

    int getNumMaintenanceProblems();

    boolean hasMaintenanceProblems();

    void setMaintenanceFixed(int index);

    void causeMaintenanceProblems();

    void storeTaped(boolean isTaped);

    boolean hasMaintenanceMechanics();
}
