package com.gregtechceu.gtceu.client.bloom;

public enum BloomAlgorithm {

    /**
     * Unity Bloom (rescale)
     */
    UNITY,
    /**
     * Unreal Bloom (gaussian blur)
     */
    UNREAL,
    /**
     * No bloom at all
     */
    DISABLED
}
