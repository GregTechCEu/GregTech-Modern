package com.gregtechceu.gtceu.client.mui.screen.viewport;

import com.gregtechceu.gtceu.api.mui.base.layout.IViewport;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Area;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * A single matrix in a matrix stack. Also has some other information.
 */
public class TransformationMatrix {

    public static final TransformationMatrix EMPTY = new TransformationMatrix(null);

    @Getter
    private final TransformationMatrix wrapped;
    @Getter
    private final IViewport viewport;
    @Getter
    private final Area area;
    @Getter
    private final Matrix4f matrix;
    private final Matrix4f invertedMatrix = new Matrix4f();

    @Getter
    private final boolean viewportMatrix;
    @Getter
    private boolean dirty = true;

    public TransformationMatrix(TransformationMatrix parent, @Nullable Matrix4f parentMatrix) {
        this.wrapped = parent;
        this.viewport = parent.viewport;
        this.area = parent.area;
        this.matrix = parentMatrix == null ? new Matrix4f(parent.getMatrix()) :
                parentMatrix.mul(parent.getMatrix(), new Matrix4f());
        this.viewportMatrix = parent.viewportMatrix;
    }

    public TransformationMatrix(@Nullable Matrix4f parent) {
        this.wrapped = null;
        this.viewport = null;
        this.area = null;
        this.matrix = new Matrix4f();
        this.viewportMatrix = false;
        if (parent != null) {
            this.matrix.set(parent);
        }
    }

    public TransformationMatrix(IViewport viewport, Area area, @Nullable Matrix4f parent) {
        this.wrapped = null;
        this.viewport = viewport;
        this.area = area;
        this.matrix = new Matrix4f();
        this.viewportMatrix = true;
        if (parent != null) {
            this.matrix.set(parent);
        }
    }

    public Matrix4f getInvertedMatrix() {
        if (this.dirty) {
            if (this.matrix.invert(this.invertedMatrix) == null) {
                this.invertedMatrix.set(this.matrix);
            }
            this.dirty = false;
        }
        return this.invertedMatrix;
    }

    public void markDirty() {
        this.dirty = true;
    }

    public int transformX(float x, float y) {
        Matrix4f m = getMatrix();
        return (int) (x * m.m00() + y * m.m10() + m.m30());
    }

    public int transformY(float x, float y) {
        Matrix4f m = getMatrix();
        return (int) (x * m.m01() + y * m.m11() + m.m31());
    }

    public int unTransformX(float x, float y) {
        Matrix4f m = getInvertedMatrix();
        return (int) (x * m.m00() + y * m.m10() + m.m30());
    }

    public int unTransformY(float x, float y) {
        Matrix4f m = getInvertedMatrix();
        return (int) (x * m.m01() + y * m.m11() + m.m31());
    }

    public Vector3f transform(Vector3f vec, Vector3f dest) {
        return transform(getMatrix(), vec, dest);
    }

    public Vector3f unTransform(Vector3f vec, Vector3f dest) {
        return transform(getInvertedMatrix(), vec, dest);
    }

    public static Vector3f transform(Matrix4f m, Vector3f vec, Vector3f dest) {
        float x = m.m00() * vec.x + m.m10() * vec.y + m.m20() * vec.z + m.m30();
        float y = m.m01() * vec.x + m.m11() * vec.y + m.m21() * vec.z + m.m31();
        float z = m.m02() * vec.x + m.m12() * vec.y + m.m22() * vec.z + m.m32();
        dest.set(x, y, z);
        return dest;
    }
}
