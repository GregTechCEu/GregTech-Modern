package com.gregtechceu.gtceu.utils;

import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GTMath {
    public static long clamp(long value, long min, long max) {
        return Math.max(min, Math.min(max, value));
    }
}
