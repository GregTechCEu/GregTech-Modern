package com.lowdragmc.lowdraglib.utils;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.joml.Vector3f;

public class ShapeUtils {

    public static AABB rotate(AABB aabb, Direction direction) {
        return com.lowdragmc.lowdraglib2.utils.ShapeUtils.rotate(aabb, direction);
    }

    public static AABB rotate(AABB aabb, Vector3f axis, double degree) {
        return com.lowdragmc.lowdraglib2.utils.ShapeUtils.rotate(aabb, axis, degree);
    }

    public static VoxelShape rotate(VoxelShape shape, Direction direction) {
        return com.lowdragmc.lowdraglib2.utils.ShapeUtils.rotate(shape, direction);
    }

    public static VoxelShape rotate(VoxelShape shape, Vector3f axis, double degree) {
        return com.lowdragmc.lowdraglib2.utils.ShapeUtils.rotate(shape, axis, degree);
    }
}
