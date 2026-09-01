package com.gregtechceu.gtceu.client.model;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Locale;

@Getter
@Accessors(fluent = true)
public enum FaceLayer {

    UNCLASSIFIED(0),
    BASE(0),
    MACHINE_FACE(1),
    MACHINE_DETAIL(2),
    MACHINE_CONTENT(3),
    EMISSIVE(4),
    COVER(5),
    COVER_EMISSIVE(6);

    private final int depthRank;

    FaceLayer(int depthRank) {
        this.depthRank = depthRank;
    }

    public static FaceLayer fromSerializedName(String name) {
        return valueOf(name.toUpperCase(Locale.ROOT));
    }
}
