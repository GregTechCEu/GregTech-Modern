package com.gregtechceu.gtceu.api.mui.widgets;

import com.gregtechceu.gtceu.api.mui.base.layout.IViewportStack;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.widget.DelegatingWidget;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.function.Consumer;

public class TransformWidget extends DelegatingWidget {

    private static final Vector3f sharedVec = new Vector3f();

    private final Matrix4f constTransform = new Matrix4f();
    private boolean hasConstTransform = false;
    private Consumer<IViewportStack> transform;

    public TransformWidget(IWidget child) {
        super(child);
    }

    @Override
    public void transform(IViewportStack stack) {
        super.transform(stack);
        if (this.hasConstTransform) stack.multiply(this.constTransform);
        if (this.transform != null) this.transform.accept(stack);
    }

    public TransformWidget transform(Consumer<IViewportStack> transform) {
        this.transform = transform;
        return this;
    }

    public TransformWidget translate(float x, float y) {
        this.hasConstTransform = true;
        this.constTransform.translate(x, y, 0);
        return this;
    }

    public TransformWidget rotate(float angle, float x, float y, float z) {
        this.hasConstTransform = true;
        this.constTransform.rotate(angle, vec(x, y, z));
        return this;
    }

    public TransformWidget scale(float x, float y) {
        this.hasConstTransform = true;
        this.constTransform.scale(vec(x, y, 1));
        return this;
    }

    private static Vector3f vec(float x, float y, float z) {
        sharedVec.set(x, y, z);
        return sharedVec;
    }
}
