package com.gregtechceu.gtceu.client.model;

import org.jetbrains.annotations.Nullable;

public enum FaceLayer {

    UNCLASSIFIED(0),
    BASE(0),
    MACHINE_FACE(1),
    EMISSIVE(2),
    COVER(3),
    COVER_EMISSIVE(4);

    private final int depthRank;

    FaceLayer(int depthRank) {
        this.depthRank = depthRank;
    }

    public int depthRank() {
        return depthRank;
    }

    public static FaceLayer fromTextureKey(@Nullable String textureKey) {
        if (textureKey == null || !textureKey.startsWith("#")) {
            return UNCLASSIFIED;
        }

        String key = textureKey.substring(1);
        if (key.startsWith("overlay")) {
            return key.contains("emissive") ? EMISSIVE : MACHINE_FACE;
        }
        return BASE;
    }
}
