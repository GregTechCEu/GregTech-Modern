package com.gregtechceu.gtceu.utils;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.mojang.blaze3d.platform.GlUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import org.jetbrains.annotations.Contract;
import org.joml.*;
import org.lwjgl.opengl.GL11;

import java.lang.Math;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.security.InvalidParameterException;
import java.util.Objects;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GTMatrixUtils {

    @SuppressWarnings("UnstableApiUsage")
    private static final ImmutableMap<Direction, Vector3fc> directionAxises = Util.make(() -> {
        ImmutableMap.Builder<Direction, Vector3fc> map = ImmutableMap.builderWithExpectedSize(6);
        for (Direction dir : GTUtil.DIRECTIONS) {
            map.put(dir, dir.step());
        }
        return map.build();
    });
    private static final Table<Direction, Direction, Matrix4fc> rotations = Tables
            .synchronizedTable(HashBasedTable.create());

    /**
     * @param from the original vector
     * @param to   the wanted vector
     * @return the angle of rotation to make {@code from} point in the direction of {@code to}
     */
    @Contract(pure = true)
    public static float getRotationAngle(final Vector3fc from, final Vector3fc to) {
        return (float) Math.acos(from.dot(to));
    }

    /**
     * This method isn't pure, {@code from} will be modified!
     * 
     * @param from the original vector
     * @param to   the wanted vector
     * @return the axis of rotation to make {@code from} point in the direction of {@code to}
     */
    public static Vector3f getRotationAxis(Vector3f from, final Vector3fc to) {
        return getRotationAxis(from, to, from);
    }

    /**
     * @param from the original vector
     * @param to   the wanted vector
     * @param dest the vector to save the result to
     * @return {@code dest}
     */
    public static Vector3f getRotationAxis(final Vector3fc from, final Vector3fc to, Vector3f dest) {
        return from.cross(to, dest).normalize();
    }

    /**
     * @param from the original vector
     * @param to   the wanted vector
     * @return the quaternion to make {@code from} point in the direction of {@code to}
     */
    @Contract(pure = true)
    public static Quaternionf getRotation(final Vector3fc from, final Vector3fc to) {
        return from.rotationTo(to, new Quaternionf());
    }

    /**
     * @param from the original direction
     * @param to   the wanted direction
     * @return the quaternion to make a vector based on {@code from} point towards {@code to}
     */
    @Contract(pure = true)
    public static Quaternionf getRotation(final Direction from, final Direction to) {
        return getRotation(getDirectionAxis(from), getDirectionAxis(to));
    }

    /**
     * Transforms the {@code matrix} and all {@code additional} vectors such that the {@code from} vector will be on the
     * {@code to} vector's axis
     * 
     * @param matrix     the matrix to transform
     * @param from       the original vector
     * @param to         the destination vector
     * @param additional additional vectors to transform
     */
    public static void rotateMatrix(Matrix4f matrix, Vector3f from, Vector3fc to, Vector3f... additional) {
        if (from.equals(to)) {
            return;
        }
        if (-from.x() == to.x() && -from.y() == to.y() && -from.z() == to.z()) {
            rotateMatrix(matrix, Mth.PI, getDirectionAxis(Direction.UP), additional);
        } else {
            var angle = getRotationAngle(from, to);
            getRotationAxis(from, to);
            rotateMatrix(matrix, angle, from, additional);
        }
    }

    /**
     * @param matrix     the matrix to transform
     * @param angle      the angle of rotation (radians)
     * @param axis       axis of rotation
     * @param additional additional vectors to transform
     */
    public static void rotateMatrix(Matrix4f matrix, float angle, Vector3fc axis, Vector3f... additional) {
        matrix.rotate(angle, axis);
        for (var vec : additional) {
            vec.rotateAxis(angle, axis.x(), axis.y(), axis.z());
        }
    }

    /**
     * @param upward the {@code upwardFacing} of the machine
     * @return the angle of rotation (in radians) along the front face axis to get the correct orientation
     */
    public static float upwardFacingAngle(Direction upward) {
        return switch (upward) {
            case NORTH -> 0;
            case SOUTH -> 2;
            case WEST -> 3;
            case EAST -> 1;
            default -> throw new InvalidParameterException("Upward facing can't be up/down");
        } * Mth.HALF_PI;
    }

    public static Vector3f rotateMatrixToFront(Matrix4f matrix, Direction frontFace) {
        // rotate frontFacing to correct cardinal direction
        var front = frontFace.step();
        rotateMatrix(matrix, Direction.NORTH.step(), getDirectionAxis(frontFace), front);
        return front;
    }

    public static void rotateMatrixToUp(Matrix4f matrix, Vector3f front, Direction upwardsFace) {
        // rotate upwards face to the correct orientation
        rotateMatrix(matrix, upwardFacingAngle(upwardsFace), front);
    }

    public static Matrix4fc createRotationState(Direction frontFace, Direction upwardFace) {
        if (rotations.contains(frontFace, upwardFace)) {
            var rotation = rotations.get(frontFace, upwardFace);
            assert rotation != null;
            return rotation;
        }
        var matrix = new Matrix4f();
        var front = rotateMatrixToFront(matrix, frontFace);
        front.absolute();
        rotateMatrixToUp(matrix, front, upwardFace);
        rotations.put(frontFace, upwardFace, matrix);
        return matrix;
    }

    public static Vector3fc getDirectionAxis(Direction dir) {
        return Objects.requireNonNull(directionAxises.get(dir));
    }

    /**
     * This is in essence the same code as in gluLookAt, but it returns the resulting transformation matrix instead of
     * applying it to the deprecated OpenGL transformation stack.
     */
    public static Matrix4f lookAt(Vector3f eyePos, Vector3f lookAt) {
        Vector3f dir = new Vector3f(lookAt);
        dir.sub(eyePos);

        Vector3f up = new Vector3f(0, 1f, 0);
        dir.normalize();

        var right = new Vector3f(dir);
        right.cross(up);
        right.normalize();

        up = new Vector3f(right);
        up.cross(dir);
        up.normalize();

        var viewMatrix = new Matrix4f();
        viewMatrix.setTransposed(FloatBuffer.wrap(new float[] {
                right.x(),
                right.y(),
                right.z(),
                0.0f,

                up.x(),
                up.y(),
                up.z(),
                0.0f,

                -dir.x(),
                -dir.y(),
                -dir.z(),
                0.0f,

                0.0f,
                0.0f,
                0.0f,
                1.0f,
        }));

        viewMatrix.translate(-eyePos.x(), -eyePos.y(), -eyePos.z());
        return viewMatrix;
    }

    /**
     * This is in essence the same code as in gluProject, but it returns the resulting transformation matrix instead of
     * applying it to the deprecated OpenGL transformation stack.
     *
     * @apiNote the Z component of the return value is the distance from the screen.
     */
    public static Vector3f projectWorldToScreen(Vector3f worldPos) {
        Matrix4f modelViewMatrix = RenderSystem.getModelViewMatrix();
        Matrix4f projectionMatrix = RenderSystem.getProjectionMatrix();
        Vector4f v = new Matrix4f(projectionMatrix).mul(modelViewMatrix).transform(new Vector4f(worldPos, 1.0f));

        float windowX = Minecraft.getInstance().getWindow().getWidth() * (v.x() + 1) / 2;
        float windowY = Minecraft.getInstance().getWindow().getHeight() * (v.y() + 1) / 2;
        float windowZ = (v.z() + 1) / 2;
        return new Vector3f(windowX, windowY, windowZ);
    }

    /**
     * This is in essence the same code as in gluProject, but it returns the resulting transformation matrix instead of
     * applying it to the deprecated OpenGL transformation stack.
     */
    public static Vector3f projectScreenToWorld(int viewX, int viewY) {
        return projectScreenToWorld(viewX, viewY, true);
    }

    /**
     * This is in essence the same code as in gluProject, but it returns the resulting transformation matrix instead of
     * applying it to the deprecated OpenGL transformation stack.
     */
    public static Vector3f projectScreenToWorld(int viewX, int viewY, boolean checkDepth) {
        float depth = 1.0f;
        if (checkDepth) {
            ByteBuffer depthBuffer = GlUtil.allocateMemory(4);
            RenderSystem.readPixels(viewX, viewY, 1, 1,
                    GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, depthBuffer);
            depth = depthBuffer.asFloatBuffer().get();
            GlUtil.freeMemory(depthBuffer);
        }

        int viewW = Minecraft.getInstance().getWindow().getWidth();
        int viewH = Minecraft.getInstance().getWindow().getHeight();
        Vector4f coords = new Vector4f(2.0f * viewX / viewW - 1, 2.0f * viewY / viewH - 1, 2.0f * depth - 1, 1);

        Matrix4f modelViewMatrix = RenderSystem.getModelViewMatrix();
        Matrix4f projectionMatrix = RenderSystem.getProjectionMatrix();
        Matrix4f inverse = new Matrix4f(projectionMatrix).mul(modelViewMatrix).invert();
        inverse.transform(coords);

        return new Vector3f(coords.x(), coords.y(), coords.z());
    }
}
