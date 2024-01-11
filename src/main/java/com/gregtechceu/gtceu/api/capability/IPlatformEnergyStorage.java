package com.gregtechceu.gtceu.api.capability;

public interface IPlatformEnergyStorage {

    /**
     * Return false if calling {@link #insert} will absolutely always return 0, or true otherwise or in doubt.
     *
     * <p>Note: This function is meant to be used by cables or other devices that can transfer energy to know if
     * they should interact with this storage at all.
     */
    boolean supportsInsertion();

    /**
     * Try to insert up to some amount of energy into this storage.
     *
     * @param maxAmount The maximum amount of energy to insert. May not be negative.
     * @param simulate Is this a test?
     * @return A nonnegative integer not greater than maxAmount: the amount that was inserted.
     */
    long insert(long maxAmount, boolean simulate);

    /**
     * Return false if calling {@link #extract} will absolutely always return 0, or true otherwise or in doubt.
     *
     * <p>Note: This function is meant to be used by cables or other devices that can transfer energy to know if
     * they should interact with this storage at all.
     */
    boolean supportsExtraction();

    /**
     * Try to extract up to some amount of energy from this storage.
     *
     * @param maxAmount The maximum amount of energy to extract. May not be negative.
     * @param simulate Is this a test?
     * @return A nonnegative integer not greater than maxAmount: the amount that was extracted.
     */
    long extract(long maxAmount, boolean simulate);

    /**
     * Return the current amount of energy that is stored.
     */
    long getAmount();

    /**
     * Return the maximum amount of energy that could be stored.
     */
    long getCapacity();
}
