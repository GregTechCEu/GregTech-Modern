package com.gregtechceu.gtceu.client.shader.post;

import org.jetbrains.annotations.NotNull;

public enum BloomType {

    /**
     * Simple Gaussian Blur
     */
    GAUSSIAN,
    /**
     * Unity Bloom
     */
    UNITY,
    /**
     * Unreal Bloom
     */
    UNREAL,
    /**
     * No bloom at all :O
     */
    DISABLED;

    public int getValue() {
        return switch (this) {
            case GAUSSIAN -> 0;
            case UNITY -> 1;
            case UNREAL -> 2;
            case DISABLED -> -1;
        };
    }

    @NotNull
    public static BloomType fromValue(int value) {
        return switch (value) {
            case 0 -> GAUSSIAN;
            case 1 -> UNITY;
            case 2 -> UNREAL;
            default -> DISABLED;
        };
    }
}
