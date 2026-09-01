package com.gregtechceu.gtceu.client.model;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Locale;

@Accessors(fluent = true)
public enum FaceLayer {

    UNCLASSIFIED(0),
    BASE(0),
    MACHINE_FACE(1),
    EMISSIVE(2),
    COVER(3),
    COVER_EMISSIVE(4);

    @Getter
    private final int depthRank;

    FaceLayer(int depthRank) {
        this.depthRank = depthRank;
    }

    public static FaceLayer fromSerializedName(String name) {
        return valueOf(name.toUpperCase(Locale.ROOT));
    }
}
