package com.gregtechceu.gtceu.api.capability.compat;

import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraftforge.energy.IEnergyStorage;

public class FeCompat {

    /**
     * Conversion ratio used by energy converters
     */
    public static int ratio(boolean feToEu) {
        return feToEu ? ConfigHolder.INSTANCE.compat.energy.feToEuRatio :
                ConfigHolder.INSTANCE.compat.energy.euToFeRatio;
    }

    /**
     * Converts eu to fe, using specified ratio
     *
     * @return fe
     */
    public static int toFe(long eu, int ratio) {
        return GTMath.saturatedCast(toFeLong(eu, ratio));
    }

    /**
     * Converts eu to fe, using specified ratio, and returns as a long.
     * Can be used for overflow protection.
     *
     * @return fe
     */
    public static long toFeLong(long eu, int ratio) {
        return eu * ratio;
    }

    /**
     * Converts eu to fe, using a specified ratio, and with a specified upper bound.
     * This can be useful for dealing with int-overflows when converting from a long to an int.
     *
     * @return fe
     */
    public static int toFeBounded(long eu, int ratio, int max) {
        return (int) Math.min(max, toFeLong(eu, ratio));
    }

    /**
     * Converts fe to eu, using specified ratio
     *
     * @return eu
     */
    public static long toEu(long fe, int ratio) {
        return fe / ratio;
    }

    /**
     * Inserts energy to the storage. EU -> FE conversion is performed.
     *
     * @return amount of EU inserted
     */
    public static long insertEu(IEnergyStorage storage, long amountEU, boolean simulate) {
        int euToFeRatio = ratio(false);
        int feSent = storage.receiveEnergy(toFe(amountEU, euToFeRatio), true);
        return toEu(storage.receiveEnergy(feSent - (feSent % euToFeRatio), simulate), euToFeRatio);
    }

    /**
     * Extracts energy from the storage. EU -> FE conversion is performed.
     *
     * @return amount of EU extracted
     */
    public static long extractEu(IEnergyStorage storage, long amountEU, boolean simulate) {
        int euToFeRatio = ratio(false);
        int extract = storage.extractEnergy(toFe(amountEU, euToFeRatio), true);
        return toEu(storage.extractEnergy(extract - (extract % euToFeRatio), simulate), euToFeRatio);
    }
}
