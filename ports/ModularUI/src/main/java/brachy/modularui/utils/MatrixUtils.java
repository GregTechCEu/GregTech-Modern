package brachy.modularui.utils;

import org.jspecify.annotations.NullMarked;
import net.minecraft.util.Util;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import com.mojang.blaze3d.vertex.PoseStack;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import org.jetbrains.annotations.Contract;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.security.InvalidParameterException;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@NullMarked
public class MatrixUtils {

    private static final Vector3fc UP = new Vector3f(0, 1, 0);

    private static final ImmutableMap<Direction, Vector3fc> directionAxises = Util.make(() -> {
        ImmutableMap.Builder<Direction, Vector3fc> map = ImmutableMap.builderWithExpectedSize(6);
        for (Direction dir : Direction.values()) {
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
        return new Matrix4f().lookAt(eyePos, target, UP);
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
        lookAt(pose.pose(), eyePos, target);
        pose.normal().lookAlong(target.sub(eyePos, new Vector3f()), UP);
    }

    /**
     * Make the pose stack's topmost transformation look at a point
     *
     * @param matrix the pose stack to modify
     * @param eyePos the position of the camera
     * @param target the point to look at
     */
    public static void lookAt(Matrix4f matrix, Vector3fc eyePos, Vector3fc target) {
        matrix.lookAt(eyePos, target, UP);
    }

    /**
     * Projects into a bottom-left-origin pixel viewport using an extraction-time matrix snapshot.
     * The combined matrix must use OpenGL-style [-1, 1] clip depth; the returned depth is [0, 1].
     * GPU projection buffers cannot be read synchronously during GUI extraction.
     */
    public static Vector3f projectWorldToScreen(Vector3fc worldPos, Matrix4fc projectionView, int[] viewport) {
        validateViewport(viewport);
        return projectionView.project(worldPos, viewport, new Vector3f());
    }

    /**
     * Unprojects a pixel and explicit normalized depth using the same matrix used for projection.
     * Picking callers should supply ray endpoints (depth 0 and 1) and intersect the scene,
     * rather than reading the depth of an unrelated, previously rendered framebuffer.
     */
    public static Vector3f projectScreenToWorld(float x, float y, float depth,
                                                Matrix4fc projectionView, int[] viewport) {
        validateViewport(viewport);
        return projectionView.unproject(x, y, depth, viewport, new Vector3f());
    }

    private static void validateViewport(int[] viewport) {
        if (viewport.length != 4 || viewport[2] <= 0 || viewport[3] <= 0) {
            throw new IllegalArgumentException("Viewport must contain x, y and positive width, height");
        }
    }
}
