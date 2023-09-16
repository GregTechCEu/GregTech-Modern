package com.gregtechceu.gtceu.utils.vec3i;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Vec3i;

import javax.annotation.ParametersAreNonnullByDefault;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class Vec3iUtils {
    public static Vec3i min(Vec3i a, Vec3i b) {
        return new Vec3i(
                Math.min(a.getX(), b.getX()),
                Math.min(a.getY(), b.getY()),
                Math.min(a.getZ(), b.getZ())
        );
    }

    public static Vec3i max(Vec3i a, Vec3i b) {
        return new Vec3i(
                Math.max(a.getX(), b.getX()),
                Math.max(a.getY(), b.getY()),
                Math.max(a.getZ(), b.getZ())
        );
    }
}
