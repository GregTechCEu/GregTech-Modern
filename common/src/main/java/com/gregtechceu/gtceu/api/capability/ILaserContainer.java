package com.gregtechceu.gtceu.api.capability;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;

public interface ILaserContainer {

    /**
     * This method accepts energy, and stores it in the container
     * If passed a {@code seen} context, you must use {@link #changeEnergy(long, Collection)} to prevent
     * infinite recursion
     *
     * @param amount amount of energy to add/remove to the container
     * @return amount of energy actually accepted
     */
    default long changeEnergy(long amount) {
        Collection<ILaserContainer> list = new ArrayList<>();
        list.add(this);
        return changeEnergy(amount, list);
    }

    /**
     * Removes specified amount of energy from this container
     * If passed a {@code seen} context, you must use {@link #removeEnergy(long, Collection)} to prevent
     * infinite recursion
     *
     * @param amount amount of energy to remove
     * @return amount of energy removed
     */
    default long removeEnergy(long amount) {
        return changeEnergy(-amount);
    }

    /**
     * Removes specified amount of energy from this container
     *
     * @param amount amount of energy to remove
     * @param seen   the containers already checked
     * @return amount of energy removed
     */
    default long removeEnergy(long amount, @Nonnull Collection<ILaserContainer> seen) {
        return changeEnergy(-amount, seen);
    }

    /**
     * This method accepts energy, and stores it in the container
     *
     * @param amount amount of energy to add/remove to the container
     * @param seen   the containers already checked
     * @return amount of energy actually accepted
     */
    long changeEnergy(long amount, @Nonnull Collection<ILaserContainer> seen);


    /**
     * If passed a {@code seen} context, you must use {@link #getEnergyStored(Collection)} to prevent
     * infinite recursion
     *
     * @return amount of currently stored energy
     */
    default long getEnergyStored() {
        Collection<ILaserContainer> list = new ArrayList<>();
        list.add(this);
        return getEnergyStored(list);
    }

    /**
     * If passed a {@code seen} context, you must use {@link #getEnergyCapacity(Collection)} to prevent;
     * infinite recursion
     *
     * @return maximum amount of energy that can be stored
     */
    default long getEnergyCapacity() {
        Collection<ILaserContainer> list = new ArrayList<>();
        list.add(this);
        return getEnergyCapacity(list);
    }

    /**
     * @param seen the containers already checked
     * @return amount of currently stored energy
     */
    long getEnergyStored(@Nonnull Collection<ILaserContainer> seen);

    /**
     * @param seen the containers already checked
     * @return maximum amount of energy that can be stored
     */
    long getEnergyCapacity(@Nonnull Collection<ILaserContainer> seen);
}
