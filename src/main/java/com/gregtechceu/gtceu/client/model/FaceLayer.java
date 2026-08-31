package com.gregtechceu.gtceu.client.model;

import org.jetbrains.annotations.Nullable;

public enum FaceLayer {

    UNCLASSIFIED(false),
    BASE(false),
    MACHINE_FACE(true),
    COVER(true);

    private final boolean aboveBase;

    FaceLayer(boolean aboveBase) {
        this.aboveBase = aboveBase;
    }

    public boolean rendersAboveBase() {
        return aboveBase;
    }

    public static FaceLayer fromTextureKey(@Nullable String textureKey) {
        if (textureKey == null || !textureKey.startsWith("#")) {
            return UNCLASSIFIED;
        }

        String key = textureKey.substring(1);
        if (key.startsWith("overlay")) {
            return MACHINE_FACE;
        }
        return BASE;
    }
}
