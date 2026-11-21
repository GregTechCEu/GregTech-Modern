package com.gregtechceu.gtceu.client.model;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.ApiStatus;

import java.util.Locale;

@Getter
@Accessors(fluent = true)
public enum FaceLayer {

    // Unclassified is our fallback layer, if you are using this normally, please stop.
    @ApiStatus.Internal
    UNCLASSIFIED(-1),
    BASE(0),
    MACHINE_FACE(1),
    MACHINE_DETAIL(2),
    MACHINE_CONTENT(3),
    EMISSIVE(4),
    COVER(5),
    COVER_EMISSIVE(6);

    public static final String JSON_PROPERTY = "gtceu:face_layer";

    private final int depthRank;

    FaceLayer(int depthRank) {
        this.depthRank = depthRank;
    }

    public static FaceLayer fromSerializedName(String name) {
        FaceLayer layer = valueOf(name.toUpperCase(Locale.ROOT));
        if (layer == UNCLASSIFIED) {
            throw new IllegalArgumentException("UNCLASSIFIED is reserved for internal use");
        }
        return layer;
    }
}
