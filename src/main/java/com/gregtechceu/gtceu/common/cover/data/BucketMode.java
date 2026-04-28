package com.gregtechceu.gtceu.common.cover.data;

import lombok.Getter;

public enum BucketMode {

    BUCKET("cover.bucket.mode.bucket", "textures/item/water_bucket", 1000),
    MILLI_BUCKET("cover.bucket.mode.milli_bucket", "gtceu:textures/gui/icon/bucket_mode/water_drop", 1);

    @Getter
    public final String tooltip;
    private final String textureName;

    public final int multiplier;

    BucketMode(String tooltip, String textureName, int multiplier) {
        this.tooltip = tooltip;
        this.textureName = textureName;
        this.multiplier = multiplier;
    }

    String getTextureName() {
        return textureName;
    }
}
