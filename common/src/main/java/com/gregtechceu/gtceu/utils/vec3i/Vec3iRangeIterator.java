package com.gregtechceu.gtceu.utils.vec3i;

import net.minecraft.core.Vec3i;

import java.util.Iterator;


/**
 * Iterates over a cubic area, denoted by its minimum and maximum points,
 * located on diagonally opposite corners.
 */
public class Vec3iRangeIterator implements Iterator<Vec3i> {
    private final Vec3i min;
    private final Vec3i max;

    private int x;
    private int y;
    private int z;

    public Vec3iRangeIterator(Vec3i min, Vec3i max) {
        this.min = min;
        this.max = max;

        this.x = min.getX() - 1;
        this.y = min.getY();
        this.z = min.getZ();
    }

    @Override
    public boolean hasNext() {
        return x < max.getX() || y < max.getY() || z < max.getZ();
    }

    @Override
    public Vec3i next() {
        if (x++ < max.getX()) {
            return new Vec3i(x, y, z);
        }

        if (y++ < max.getY()) {
            x = min.getX();
            return new Vec3i(x, y, z);
        }

        if (z++ < max.getZ()) {
            x = min.getX();
            y = min.getY();
            return new Vec3i(x, y, z);
        }

        throw new IndexOutOfBoundsException("Cannot iterate past the specified range");
    }
}
