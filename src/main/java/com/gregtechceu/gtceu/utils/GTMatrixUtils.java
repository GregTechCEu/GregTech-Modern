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
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.Contract;
import org.joml.*;
import org.lwjgl.opengl.GL11;

import java.lang.Math;
import java.nio.ByteBuffer;
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

    private static final ByteBuffer PIXEL_DEPTH_BUFFER = GlUtil.allocateMemory(4);
    private static final int[] VIEWPORT_COORDS = { 0, 0, 0, 0 };

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
        Vector3f front = frontFace.step();
        rotateMatrix(matrix, Direction.NORTH.step(), getDirectionAxis(frontFace), front);
        return front;
    }

    public static void rotateMatrixToUp(Matrix4f matrix, Vector3fc front, Direction upwardsFace) {
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
     * {@link Matrix4f#lookAt(Vector3fc, Vector3fc, Vector3fc) Matrix4f#lookAt} with an up axis of {@code (0, 1, 0)}
     *
     * @see Matrix4f#lookAt(Vector3fc, Vector3fc, Vector3fc)
     */
    public static Matrix4f lookAt(Vector3fc eyePos, Vector3fc target) {
        return new Matrix4f().lookAt(eyePos, target, GTMath.UNIT_Y);
    }

    /**
     * Make the pose stack's topmost transformation look at a point
     *
     * @param poseStack the pose stack to modify
     * @param eyePos    the position of the camera
     * @param target    the point to look at
     */
    public static void lookAt(PoseStack poseStack, Vector3fc eyePos, Vector3fc target) {
        lookAt(poseStack.last(), eyePos, target);
    }

    /**
     * Make the pose stack's topmost transformation look at a point
     *
     * @param pose   the pose stack layer to modify
     * @param eyePos the position of the camera
     * @param target the point to look at
     */
    public static void lookAt(PoseStack.Pose pose, Vector3fc eyePos, Vector3fc target) {
        pose.pose().lookAt(eyePos, target, GTMath.UNIT_Y);
        pose.normal().lookAlong(target, GTMath.UNIT_Y);
    }

    /**
     * This is in essence the same code as in gluProject, but it returns the resulting transformation matrix instead of
     * applying it to the deprecated OpenGL transformation stack.
     *
     * @param worldPos world space position
     * @apiNote the Z component of the return value is the distance from the screen.
     */
    public static Vector3f projectWorldToScreen(Vector3fc worldPos) {
        Window window = Minecraft.getInstance().getWindow();
        return projectWorldToScreen(worldPos, window.getWidth(), window.getHeight());
    }

    /**
     * This is in essence the same code as in gluProject, but it returns the resulting transformation matrix instead of
     * applying it to the deprecated OpenGL transformation stack.
     *
     * @param worldPos   world space position
     * @param viewWidth  the viewport's width
     * @param viewHeight the viewport's height
     * @apiNote the Z component of the return value is the distance from the screen.
     */
    public static Vector3f projectWorldToScreen(Vector3fc worldPos, int viewWidth, int viewHeight) {
        // read projection and model view matrices
        Matrix4f transform = new Matrix4f(RenderSystem.getProjectionMatrix()).mul(RenderSystem.getModelViewMatrix());
        Vector3f screenPos = new Vector3f(worldPos).mulPosition(transform);
        screenPos.x = viewWidth * (screenPos.x + 1.0f) / 2.0f;
        screenPos.y = viewHeight * (screenPos.y + 1.0f) / 2.0f;
        screenPos.z = (screenPos.z + 1.0f) / 2.0f;

        return screenPos;
    }

    /**
     * This is in essence the same code as in gluProject, but it returns the resulting transformation matrix instead of
     * applying it to the deprecated OpenGL transformation stack.
     *
     * @param x X-coordinate in pixels
     * @param y Y-coordinate in pixels
     * @return world pos
     */
    public static Vector3f projectScreenToWorld(int x, int y) {
        Window window = Minecraft.getInstance().getWindow();
        return projectScreenToWorld(x, y, window.getWidth(), window.getHeight(), true);
    }

    /**
     * This is in essence the same code as in gluProject, but it returns the resulting transformation matrix instead of
     * applying it to the deprecated OpenGL transformation stack.
     *
     * @param x          X-coordinate in pixels
     * @param y          Y-coordinate in pixels
     * @param viewWidth  the viewport's width
     * @param viewHeight the viewport's height
     * @param checkDepth whether to read the depth value of the targeted position
     * @return world pos
     */
    public static Vector3f projectScreenToWorld(int x, int y, int viewWidth, int viewHeight, boolean checkDepth) {
        // update the viewport size array
        VIEWPORT_COORDS[2] = viewWidth;
        VIEWPORT_COORDS[3] = viewHeight;
        return projectScreenToWorld(x, y, VIEWPORT_COORDS, checkDepth);
    }

    /**
     * This is in essence the same code as in gluProject, but it returns the resulting transformation matrixinstead of
     * applying it to the deprecated OpenGL transformation stack.
     *
     * @param x          X-coordinate in pixels
     * @param y          Y-coordinate in pixels
     * @param viewport   the viewport described by {@code [x, y, width, height]}
     * @param checkDepth whether to read the depth value of the targeted position
     * @return world pos
     */
    public static Vector3f projectScreenToWorld(int x, int y, int[] viewport, boolean checkDepth) {
        // read projection and model view matrices
        Matrix4f transform = new Matrix4f(RenderSystem.getProjectionMatrix()).mul(RenderSystem.getModelViewMatrix());

        float depth = 1.0f;
        if (checkDepth) {
            // read depth under mouse
            RenderSystem.readPixels(x, y, 1, 1, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, PIXEL_DEPTH_BUFFER);
            PIXEL_DEPTH_BUFFER.rewind();
            depth = PIXEL_DEPTH_BUFFER.getFloat();
            PIXEL_DEPTH_BUFFER.rewind();
        }

        return transform.unproject(x, y, depth, viewport, new Vector3f());
    }
}
