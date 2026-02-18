package com.gregtechceu.gtceu.client.mui.screen.viewport;

import com.gregtechceu.gtceu.api.mui.base.layout.IViewport;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Area;

import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * A single matrix in a matrix stack. Also has some other information.
 */
@ApiStatus.Internal
public class TransformationMatrix {

    public static final TransformationMatrix EMPTY = new TransformationMatrix(null);

    @Getter
    private TransformationMatrix wrapped;
    @Getter
    private IViewport viewport;
    @Getter
    private Area area;
    @Getter
    private final Matrix4f matrix = new Matrix4f();
    private final Matrix4f invertedMatrix = new Matrix4f();
    private boolean inUse = false;

    @Getter
    private boolean viewportMatrix;
    @Getter
    private boolean dirty = true;

    public TransformationMatrix() {}

    public TransformationMatrix(@Nullable Matrix4f parent) {
        construct(parent);
    }

    public TransformationMatrix(TransformationMatrix parent, @Nullable Matrix4f parentMatrix) {
        construct(parent, parentMatrix);
    }

    TransformationMatrix construct(TransformationMatrix parent, @Nullable Matrix4f parentMatrix) {
        checkInUse();
        this.wrapped = parent;
        this.viewport = parent.viewport;
        this.area = parent.area;
        if (parentMatrix == null) {
            this.matrix.set(parent.getMatrix());
        } else {
            parentMatrix.mul(parent.getMatrix(), this.matrix);
        }
        this.viewportMatrix = parent.viewportMatrix;
        return this;
    }

    TransformationMatrix construct(@Nullable Matrix4f parent) {
        return construct(null, null, parent, false);
    }

    TransformationMatrix construct(IViewport viewport, Area area, @Nullable Matrix4f parent) {
        return construct(viewport, area, parent, true);
    }

    private TransformationMatrix construct(IViewport viewport, Area area, @Nullable Matrix4f parent,
                                           boolean isViewport) {
        checkInUse();
        this.wrapped = null;
        this.viewport = viewport;
        this.area = area;
        this.viewportMatrix = isViewport;
        if (parent != null) {
            this.matrix.set(parent);
        } else {
            this.matrix.identity();
        }
        return this;
    }

    void dispose() {
        this.wrapped = null;
        this.viewport = null;
        this.area = null;
        this.inUse = false;
        this.viewportMatrix = false;
    }

    private void checkInUse() {
        if (this.inUse) {
            throw new IllegalStateException("Transformation matrix is already in use!");
        }
        this.inUse = true;
        this.dirty = true;
    }

    public Matrix4f getInvertedMatrix() {
        if (this.dirty) {
            this.matrix.invert(this.invertedMatrix);
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
