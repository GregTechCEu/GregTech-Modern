package com.gregtechceu.gtceu.api.mui.base.layout;

import com.gregtechceu.gtceu.api.mui.widget.sizer.Area;
import com.gregtechceu.gtceu.client.mui.screen.viewport.TransformationMatrix;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * This handles all viewports in a GUI. Also keeps track of a matrix stack used for rendering and
 * user interaction.
 */
public interface IViewportStack {

    /**
     * Reset all viewports and the matrix stack.
     */
    void reset();

    /**
     * @return current viewport
     */
    Area getViewport();

    /**
     * Pushes a viewport to the top. Also pushes a new matrix.
     *
     * @param viewport viewport to push
     * @param area     area of the viewport
     */
    void pushViewport(IViewport viewport, Area area);

    /**
     * Only pushes a matrix without a viewport.
     */
    void pushMatrix();

    /**
     * Removes the top viewport and its matrix from the stack.
     *
     * @param viewport viewport to remove from the top.
     * @throws IllegalStateException if the given viewport doesn't match the viewport at the top.
     */
    void popViewport(IViewport viewport);

    /**
     * Removes the top matrix from the stack.
     *
     * @throws IllegalStateException if the top matrix is a viewport.
     */
    void popMatrix();

    /**
     * @return the matrix stack size
     */
    int getStackSize();

    /**
     * Removes all matrices ABOVE the given index.
     *
     * @param index matrices are removed above this index.
     */
    void popUntilIndex(int index);

    /**
     * Removes all matrices ABOVE the given viewport.
     *
     * @param viewport matrices are removed above this viewport.
     */
    void popUntilViewport(IViewport viewport);

    /**
     * Applies translation transformation to the current top matrix.
     *
     * @param x translation in x
     * @param y translation in y
     */
    void translate(float x, float y);

    /**
     * Applies translation transformation to the current top matrix.
     *
     * @param x translation in x
     * @param y translation in y
     * @param z translation in z
     */
    void translate(float x, float y, float z);

    /**
     * Applies rotation transformation to the current top matrix.
     *
     * @param angle clockwise rotation angle in radians
     * @param x     x-axis rotation. 1 for yes, 0 for no
     * @param y     y-axis rotation. 1 for yes, 0 for no
     * @param z     z-axis rotation. 1 for yes, 0 for no
     */
    void rotate(float angle, float x, float y, float z);

    /**
     * Applies rotation transformation to the current top matrix around z.
     *
     * @param angle clockwise rotation angle in radians
     */
    void rotateZ(float angle);

    /**
     * Applies scaling transformation to the current top matrix around.
     *
     * @param x x scale factor
     * @param y y scale factor
     */
    void scale(float x, float y);

    void multiply(Matrix4f matrix);

    /**
     * Resets the top matrix to the matrix below.
     */
    void resetCurrent();

    /**
     * Transforms the x component of a position with the current matrix transformations.
     *
     * @param x x component of position
     * @param y y component of position
     * @return transformed x component
     */
    int transformX(float x, float y);

    /**
     * Transforms the y component of a position with the current matrix transformations.
     *
     * @param x x component of position
     * @param y y component of position
     * @return transformed y component
     */
    int transformY(float x, float y);

    /**
     * Transforms the x component of a position with the current inverted matrix transformations.
     *
     * @param x x component of position
     * @param y y component of position
     * @return un-transformed x component
     */
    int unTransformX(float x, float y);

    /**
     * Transforms the y component of a position with the current inverted matrix transformations.
     *
     * @param x x component of position
     * @param y y component of position
     * @return un-transformed y component
     */
    int unTransformY(float x, float y);

    /**
     * Transforms a vector with the current matrix transformations.
     * This modifies the given vector.
     *
     * @param vec vector to transform
     * @return transformed vector
     */
    default Vector3f transform(Vector3f vec) {
        return transform(vec, vec);
    }

    /**
     * Transforms a vector with the current matrix transformations.
     *
     * @param vec  vector to transform
     * @param dest vector to write the result to
     * @return transformed vector
     */
    Vector3f transform(Vector3f vec, Vector3f dest);

    /**
     * Transforms a vector with the current inverted matrix transformations.
     * This modifies the given vector.
     *
     * @param vec vector to un-transform
     * @return un-transformed vector
     */
    default Vector3f unTransform(Vector3f vec) {
        return unTransform(vec, vec);
    }

    /**
     * Transforms a vector with the current inverted matrix transformations.
     * This modifies the given vector.
     *
     * @param vec  vector to un-transform
     * @param dest vector to write the result to
     * @return un-transformed vector
     */
    Vector3f unTransform(Vector3f vec, Vector3f dest);

    /**
     * Applies the current matrix transformations the current OpenGL matrix.
     */
    void applyTo(PoseStack poseStack);

    /**
     * @return the top matrix or null if stack is empty
     */
    @Nullable
    TransformationMatrix peek();
}
