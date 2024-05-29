package com.gregtechceu.gtceu.api.capability;

public interface IElectricItem {

    /**
     * Determines if item can provide external discharging capability "in general"
     * it ensures it can be inserted into battery discharger slots & so
     *
     * @return true if item can be discharged externally
     */
    boolean canProvideChargeExternally();

    boolean chargeable();

    /**
     * Charge an item with a specified amount of energy.
     *
     * @param amount              max amount of energy to charge in EU
     * @param chargerTier         tier of the charging device, has to be at least as high as the item to charge
     * @param ignoreTransferLimit ignore any transfer limits, infinite charge rate
     * @param simulate            don't actually change the item, just determine the return value
     * @return Energy transferred into the electric item
     */
    long charge(long amount, int chargerTier, boolean ignoreTransferLimit, boolean simulate);

    /**
     * Discharge an item by a specified amount of energy
     * <p>
     * The externally parameter is used to prevent non-battery-like items from providing power. For
     * example discharge slots set externally to true, but items using energy for themselves don't.
     * Special cases like the nano saber hitting armor will discharge with externally = false.
     *
     * @param amount              max amount of energy to discharge in EU
     * @param dischargerTier      tier of the discharging device, has to be at least as high as the item to discharge
     * @param ignoreTransferLimit ignore any transfer limits, infinite discharge rate
     * @param externally          use the supplied item externally, i.e. to power something else as if it was a battery
     * @param simulate            don't actually discharge the item, just determine the return value
     * @return Energy retrieved from the electric item
     */
    long discharge(long amount, int dischargerTier, boolean ignoreTransferLimit, boolean externally, boolean simulate);

    /**
     * Determine the transfer limit for the specified item
     *
     * @return maximum transfer rate item can handle in EU/t
     */
    long getTransferLimit();

    /**
     * Determine the charge level for the specified item.
     * <p>
     * The item may not actually be chargeable to the returned level, e.g. if it is a
     * non-rechargeable single use battery.
     *
     * @return maximum charge level in EU
     */
    long getMaxCharge();

    /**
     * Determine the current charge for the specified item
     *
     * @return current charge level in EU
     */
    long getCharge();

    /**
     * Determine if the specified electric item has at least a specific amount of EU.
     *
     * @param amount minimum amount of energy required
     * @return true if there's enough energy
     */
    default boolean canUse(long amount) {
        return discharge(amount, Integer.MAX_VALUE, true, false, true) == amount;
    }

    /**
     * Get the tier of the specified item.
     *
     * @return The tier of the item.
     */
    int getTier();
}
