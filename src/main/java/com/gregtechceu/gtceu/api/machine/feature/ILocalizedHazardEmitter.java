package com.gregtechceu.gtceu.api.machine.feature;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IHazardParticleContainer;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.HazardProperty;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.common.capability.LocalizedHazardSavedData;
import com.gregtechceu.gtceu.common.data.GTMedicalConditions;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.server.level.ServerLevel;

/**
 * common interface for localized hazard (e.g. radiation) emitters like nuclear reactors.
 */
public interface ILocalizedHazardEmitter extends IMachineFeature {

    /**
     * @return the medical condition this hazard emitter creates.
     */
    default MedicalCondition getConditionToEmit() {
        return GTMedicalConditions.CARCINOGEN;
    }

    /**
     * @return the starting strength of the hazard zone. recommended values are in the range [1,5)
     */
    int getHazardSizePerOperation();

    default void spreadLocalizedHazard() {
        if (!ConfigHolder.INSTANCE.gameplay.environmentalHazards) {
            return;
        }

        if (self().getLevel() instanceof ServerLevel serverLevel) {
            IHazardParticleContainer container = GTCapabilityHelper.getHazardContainer(serverLevel,
                    self().getPos().relative(self().getFrontFacing()), self().getFrontFacing().getOpposite());
            if (container != null &&
                    container.getHazardCanBeInserted(getConditionToEmit()) > getHazardSizePerOperation()) {
                container.addHazard(getConditionToEmit(), getHazardSizePerOperation());
                return;
            }

            var savedData = LocalizedHazardSavedData.getOrCreate(serverLevel);
            savedData.addSphericalZone(self().getPos(), getHazardSizePerOperation(), false,
                    HazardProperty.HazardTrigger.INHALATION, getConditionToEmit());
        }
    }
}
