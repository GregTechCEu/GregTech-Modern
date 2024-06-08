package com.gregtechceu.gtceu.api.machine.feature;

import com.gregtechceu.gtceu.api.data.chemical.material.properties.HazardProperty;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.common.capability.EnvironmentalHazardSavedData;
import com.gregtechceu.gtceu.common.data.GTMedicalConditions;

import net.minecraft.server.level.ServerLevel;

/**
 * @author screret
 * @date 2024/6/8
 * @apiNote common interface for environmental hazard emitters like mufflers.
 */
public interface IEnvironmentalHazardEmitter extends IMachineFeature {

    /**
     * @return the medical condition this hazard emitter creates.
     */
    default MedicalCondition getConditionToEmit() {
        return GTMedicalConditions.CARBON_MONOXIDE_POISONING;
    }

    /**
     * @return the starting strength of the hazard zone.
     */
    int hazardStrengthPerOperation();

    default void spreadEnvironmentalHazard() {
        if (self().getLevel() instanceof ServerLevel serverLevel) {
            var savedData = EnvironmentalHazardSavedData.getOrCreate(serverLevel);
            savedData.addZone(self().getPos(), hazardStrengthPerOperation(), true,
                    HazardProperty.HazardTrigger.INHALATION, getConditionToEmit());
        }
    }
}
