package com.gregtechceu.gtceu.common.machine.trait.hazard;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IHazardParticleContainer;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.HazardProperty;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.MachineTraitType;
import com.gregtechceu.gtceu.common.capability.LocalizedHazardSavedData;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.server.level.ServerLevel;

import lombok.Getter;
import lombok.Setter;

/**
 * trait for localized hazard (e.g. radiation) emitters like nuclear reactors.
 */
public class LocalizedHazardEmitterTrait extends MachineTrait {

    public static final MachineTraitType<LocalizedHazardEmitterTrait> TYPE = new MachineTraitType<>(
            LocalizedHazardEmitterTrait.class);

    @Getter
    @Setter
    private MedicalCondition conditionToEmit;
    @Getter
    @Setter
    private int conditionStrength;

    public LocalizedHazardEmitterTrait(MetaMachine machine, MedicalCondition conditionToEmit,
                                       int defaultConditionStrength) {
        super(machine);
        this.conditionToEmit = conditionToEmit;
        this.conditionStrength = defaultConditionStrength;
    }

    @Override
    public MachineTraitType<LocalizedHazardEmitterTrait> getTraitType() {
        return TYPE;
    }

    public void spreadHazard() {
        if (!ConfigHolder.INSTANCE.gameplay.environmentalHazards) {
            return;
        }

        if (getLevel() instanceof ServerLevel serverLevel) {
            IHazardParticleContainer container = GTCapabilityHelper.getHazardContainer(serverLevel,
                    getBlockPos().relative(machine.getFrontFacing()), machine.getFrontFacing().getOpposite());
            if (container != null &&
                    container.getHazardCanBeInserted(getConditionToEmit()) > getConditionStrength()) {
                container.addHazard(getConditionToEmit(), getConditionStrength());
                return;
            }

            var savedData = LocalizedHazardSavedData.getOrCreate(serverLevel);
            savedData.addSphericalZone(getBlockPos(), getConditionStrength(), false,
                    HazardProperty.HazardTrigger.INHALATION, getConditionToEmit());
        }
    }
}
