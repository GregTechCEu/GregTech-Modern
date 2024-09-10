package com.gregtechceu.gtceu.api.machine.feature;

import com.gregtechceu.gtceu.api.medicalcondition.MedicalCondition;

public interface IEnvironmentalHazardCleaner extends IMachineFeature {

    float getRemovedLastSecond();

    void cleanHazard(MedicalCondition condition, float amount);
}
