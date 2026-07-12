package com.gregtechceu.gtceu.common.cover.data;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.resources.ResourceLocation;

import brachy.modularui.drawable.UITexture;
import lombok.Getter;

public enum BucketMode {

    BUCKET("cover.bucket.mode.bucket", new ResourceLocation("minecraft", "textures/item/water_bucket"), 1000),
    MILLI_BUCKET("cover.bucket.mode.milli_bucket", GTCEu.id("textures/gui/icon/bucket_mode/water_drop"), 1);

    @Getter
    public final String tooltip;
    @Getter
    public final UITexture icon;

    public final int multiplier;

    BucketMode(String tooltip, ResourceLocation texture, int multiplier) {
        this.tooltip = tooltip;
        this.icon = UITexture.fullImage(texture);
        this.multiplier = multiplier;
    }
}
