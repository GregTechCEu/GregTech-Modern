package com.gregtechceu.gtceu.api.capability;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.jetbrains.annotations.NotNull;

public interface IMedicalConditionTracker {



    Object2IntMap<MedicalCondition> getMedicalConditions();

    /**
     * @return the maximum air supply for the entity this is attached to.
     */
    // default maxAirSupply for players is 300.
    int getMaxAirSupply();


    void tick();

    void progressRelatedCondition(@NotNull Material material);

}
