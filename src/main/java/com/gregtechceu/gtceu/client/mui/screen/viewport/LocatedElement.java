package com.gregtechceu.gtceu.client.mui.screen.viewport;

import lombok.Getter;

public class LocatedElement<T> {

    @Getter
    private final T element;
    @Getter
    private final TransformationMatrix transformationMatrix;

    public LocatedElement(T element, TransformationMatrix transformationMatrix) {
        this.element = element;
        this.transformationMatrix = new TransformationMatrix(transformationMatrix, null);
    }

    public void applyMatrix(GuiContext context) {
        context.push(this.transformationMatrix);
    }

    public void unapplyMatrix(GuiContext context) {
        context.pop(this.transformationMatrix);
    }
}
