package com.gregtechceu.gtceu.api.machine.feature;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IHazardParticleContainer;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.HazardProperty;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.common.capability.EnvironmentalHazardSavedData;
import com.gregtechceu.gtceu.common.data.GTMedicalConditions;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.server.level.ServerLevel;

/**
 * common interface for environmental hazard (e.g. pollution) emitters like mufflers.
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
    float getHazardStrengthPerOperation();

    default void spreadEnvironmentalHazard() {
        if (!ConfigHolder.INSTANCE.gameplay.environmentalHazards) {
            return;
        }

        if (self().getLevel() instanceof ServerLevel serverLevel) {
            IHazardParticleContainer container = GTCapabilityHelper.getHazardContainer(serverLevel,
                    self().getPos().relative(self().getFrontFacing()), self().getFrontFacing().getOpposite());
            if (container != null &&
                    container.getHazardCanBeInserted(getConditionToEmit()) > getHazardStrengthPerOperation()) {
                container.addHazard(getConditionToEmit(), getHazardStrengthPerOperation());
                return;
            }

            var savedData = EnvironmentalHazardSavedData.getOrCreate(serverLevel);
            savedData.addZone(self().getPos(), getHazardStrengthPerOperation(), true,
                    HazardProperty.HazardTrigger.INHALATION, getConditionToEmit());
        }
    }
}
