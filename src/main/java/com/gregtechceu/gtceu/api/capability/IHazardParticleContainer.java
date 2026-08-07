package com.gregtechceu.gtceu.api.capability;

import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;

import net.minecraft.core.Direction;

public interface IHazardParticleContainer {

    /**
     * @return if this container accepts particles from the given side
     */
    boolean inputsHazard(Direction side, MedicalCondition condition);

    /**
     * @return if this container can output particles to the given side
     */
    default boolean outputsHazard(Direction side, MedicalCondition condition) {
        return false;
    }

    /**
     * This changes the amount stored.
     *
     * @param differenceAmount amount of particles to add (>0) or remove (<0)
     * @return amount of particles added or removed
     */
    float changeHazard(MedicalCondition condition, float differenceAmount);

    /**
     * Adds specified amount of particles to this particles container
     *
     * @param particlesToAdd amount of particles to add
     * @return amount of particles added
     */
    default float addHazard(MedicalCondition condition, float particlesToAdd) {
        return changeHazard(condition, particlesToAdd);
    }

    /**
     * Removes specified amount of particles from this particles container
     *
     * @param particlesToRemove amount of particles to remove
     * @return amount of particles removed
     */
    default float removeHazard(MedicalCondition condition, float particlesToRemove) {
        return -changeHazard(condition, -particlesToRemove);
    }

    /**
     * @return the maximum amount of particles that can be inserted
     */
    default float getHazardCanBeInserted(MedicalCondition condition) {
        return getHazardCapacity(condition) - getHazardStored(condition);
    }

    /**
     * @return amount of currently stored particles
     */
    float getHazardStored(MedicalCondition condition);

    /**
     * @return maximum amount of storable particles
     */
    float getHazardCapacity(MedicalCondition condition);

    IHazardParticleContainer DEFAULT = new IHazardParticleContainer() {

        @Override
        public boolean inputsHazard(Direction side, MedicalCondition condition) {
            return false;
        }

        @Override
        public float changeHazard(MedicalCondition condition, float differenceAmount) {
            return 0;
        }

        @Override
        public float getHazardStored(MedicalCondition condition) {
            return 0;
        }

        @Override
        public float getHazardCapacity(MedicalCondition condition) {
            return 0;
        }
    };
}
