package com.gregtechceu.gtceu.api.capability;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import net.minecraft.world.effect.MobEffect;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface IMedicalConditionTracker {



    Map<MedicalCondition,Float> getMedicalConditions();

    /**
     * @return the maximum air supply for the entity this is attached to.
     */
    // default maxAirSupply for players is 300.
    int getMaxAirSupply();


    void tick();

    void progressRelatedCondition(@NotNull Material material);

    void heal(MedicalCondition condition, int progression);

    void setMobEffect(MobEffect effect, int amplifier);

}
