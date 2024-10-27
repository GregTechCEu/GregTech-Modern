package com.gregtechceu.gtceu.api.capability;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.HazardProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;

import net.minecraft.world.effect.MobEffect;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import org.jetbrains.annotations.NotNull;

public interface IMedicalConditionTracker {

    /**
     * @return Map of medical condition to its progression.
     */
    Object2FloatMap<MedicalCondition> getMedicalConditions();

    /**
     * @return the maximum air supply for the entity this is attached to. -1 for default (300).
     */
    // default maxAirSupply for players is 300.
    int getMaxAirSupply();

    void tick();

    default void progressRelatedCondition(@NotNull Material material) {
        HazardProperty materialHazard = material.getProperty(PropertyKey.HAZARD);
        progressCondition(materialHazard.condition, materialHazard.progressionMultiplier);
    }

    void progressCondition(@NotNull MedicalCondition condition, float strength);

    void heal(MedicalCondition condition, int progression);

    void setMobEffect(MobEffect effect, int amplifier);

    void removeMedicalCondition(MedicalCondition condition);
}
