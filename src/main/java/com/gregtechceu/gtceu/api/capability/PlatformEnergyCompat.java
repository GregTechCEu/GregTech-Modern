package com.gregtechceu.gtceu.api.capability;

import com.gregtechceu.gtceu.config.ConfigHolder;

public class PlatformEnergyCompat {

    /**
     * Conversion ratio used by energy converters
     */
    public static int ratio(boolean nativeToEu) {
        return nativeToEu ? ConfigHolder.INSTANCE.compat.energy.platformToEuRatio : ConfigHolder.INSTANCE.compat.energy.euToPlatformRatio;
    }

    /**
     * Converts eu to native energy, using specified ratio
     * @return amount of native energy
     */
    public static int toNative(long eu, int ratio) {
        return (int) toNativeLong(eu, ratio);
    }

    /**
     * Converts eu to native energy, using specified ratio, and returns as a long.
     * Can be used for overflow protection.
     * @return amount of native energy
     */
    public static long toNativeLong(long eu, int ratio) {
        return eu * ratio;
    }

    /**
     * Converts eu to native energy, using a specified ratio, and with a specified upper bound.
     * This can be useful for dealing with int-overflows when converting from a long to an int.
     * @return amount of native energy
     */
    public static long toNativeBounded(long eu, int ratio, int max) {
        return Math.min(max, toNativeLong(eu, ratio));
    }

    /**
     * Converts native energy to eu, using specified ratio
     * @return amount of eu
     */
    public static long toEu(long nat, int ratio){
        return nat / ratio;
    }

    /**
     * Inserts energy to the storage. EU -> FE conversion is performed.
     * @return amount of EU inserted
     */
    public static long insertEu(IPlatformEnergyStorage storage, long amountEU){
        int euToNativeRatio = ratio(false);
        long nativeSent = storage.insert(toNativeLong(amountEU, euToNativeRatio), true);
        return toEu(storage.insert(nativeSent - (nativeSent % euToNativeRatio), false), euToNativeRatio);
    }

    /**
     * Extracts energy from the storage. EU -> FE conversion is performed.
     * @return amount of EU extracted
     */
    public static long extractEu(IPlatformEnergyStorage storage, long amountEU){
        int euToNativeRatio = ratio(false);
        long extract = storage.extract(toNativeLong(amountEU, euToNativeRatio), true);
        return toEu(storage.extract(extract - (extract % euToNativeRatio), false), euToNativeRatio);
    }
}
