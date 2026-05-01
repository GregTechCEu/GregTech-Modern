package com.gregtechceu.gtceu.client.model.compat;

public final class QuadTransformers {

    private static final IQuadTransformer EMPTY = quads -> {};

    private QuadTransformers() {}

    public static IQuadTransformer empty() {
        return EMPTY;
    }
}
