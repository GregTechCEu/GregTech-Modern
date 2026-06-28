package com.gregtechceu.gtceu.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import org.joml.Vector3f;

public class BlockPosUtil {

    public static final BlockPos MAX = new BlockPos(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    public static final BlockPos MIN = new BlockPos(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);

    public static int getBlockCountInside(BlockPos p1, BlockPos p2) {
        return getXDist(p1, p2) * getYDist(p1, p2) * getZDist(p1, p2);
    }

    public static int getXDist(BlockPos p1, BlockPos p2) {
        return Math.abs(p1.getX() - p2.getX());
    }

    public static int getYDist(BlockPos p1, BlockPos p2) {
        return Math.abs(p1.getY() - p2.getY());
    }

    public static int getZDist(BlockPos p1, BlockPos p2) {
        return Math.abs(p1.getZ() - p2.getZ());
    }

    public static BlockPos getMin(BlockPos p1, BlockPos p2) {
        return new BlockPos(Math.min(p1.getX(), p2.getX()),
                Math.min(p1.getY(), p2.getY()),
                Math.min(p1.getZ(), p2.getZ()));
    }

    public static BlockPos getMax(BlockPos p1, BlockPos p2) {
        return new BlockPos(Math.max(p1.getX(), p2.getX()), Math.max(p1.getY(), p2.getY()),
                Math.max(p1.getZ(), p2.getZ()));
    }

    public static void setMin(BlockPos.MutableBlockPos p1, BlockPos p2) {
        p1.set(Math.min(p1.getX(), p2.getX()), Math.min(p1.getY(), p2.getY()), Math.min(p1.getZ(), p2.getZ()));
    }

    public static void setMax(BlockPos.MutableBlockPos p1, BlockPos p2) {
        p1.set(Math.max(p1.getX(), p2.getX()), Math.max(p1.getY(), p2.getY()), Math.max(p1.getZ(), p2.getZ()));
    }

    public static BlockPos getCenter(BlockPos p1, BlockPos p2) {
        BlockPos min = getMin(p1, p2);
        return new BlockPos(getXDist(p1, p2) / 2 + min.getX(), getYDist(p1, p2) / 2 + min.getY(),
                getYDist(p1, p2) / 2 + min.getY());
    }

    public static Vector3f getCenterF(BlockPos p1, BlockPos p2) {
        BlockPos min = getMin(p1, p2);
        return getCenterF(min.getX(), min.getY(), min.getZ(), getXDist(p1, p2), getYDist(p1, p2), getZDist(p1, p2));
    }

    public static Vector3f getCenterF(int oX, int oY, int oZ, int dX, int dY, int dZ) {
        return new Vector3f(dX / 2.0f + oX, dY / 2.0f + oY, dZ / 2.0f + oZ);
    }

    public static Vec3 getCenterD(BlockPos p1, BlockPos p2) {
        BlockPos min = getMin(p1, p2);
        return getCenterD(min.getX(), min.getY(), min.getZ(), getXDist(p1, p2), getYDist(p1, p2), getZDist(p1, p2));
    }

    public static Vec3 getCenterD(int oX, int oY, int oZ, int dX, int dY, int dZ) {
        return new Vec3(dX / 2.0 + oX, dY / 2.0 + oY, dZ / 2.0 + oZ);
    }

    public static Iterable<BlockPos> getAllInside(BlockPos p1, BlockPos p2, boolean includeBorder) {
        int x0 = Math.min(p1.getX(), p2.getX());
        int y0 = Math.min(p1.getY(), p2.getY());
        int z0 = Math.min(p1.getZ(), p2.getZ());
        int x1 = Math.max(p1.getX(), p2.getX());
        int y1 = Math.max(p1.getY(), p2.getY());
        int z1 = Math.max(p1.getZ(), p2.getZ());

        if (includeBorder) {
            x0--;
            y0--;
            z0--;
        } else {
            x1--;
            y1--;
            z1--;
        }
        return BlockPos.betweenClosed(x0, y0, z0, x1, y1, z1);
    }

    public static boolean isOnBorder(BlockPos boxMin, BlockPos boxMax, BlockPos p) {
        return p.getX() == boxMin.getX() || p.getX() == boxMax.getX() ||
                p.getY() == boxMin.getY() || p.getY() == boxMax.getY() ||
                p.getZ() == boxMin.getZ() || p.getZ() == boxMax.getZ();
    }
}
