package com.gregtechceu.gtceu.common.cover.data;

import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import lombok.Getter;

public enum BucketMode implements EnumSelectorWidget.SelectableEnum {

    BUCKET("cover.bucket.mode.bucket", "minecraft:textures/item/water_bucket", 1000),
    MILLI_BUCKET("cover.bucket.mode.milli_bucket", "gtceu:textures/gui/icon/bucket_mode/water_drop", 1);

    @Getter
    public final String tooltip;
    @Getter
    public final IGuiTexture icon;

    public final int multiplier;

    BucketMode(String tooltip, String textureName, int multiplier) {
        this.tooltip = tooltip;
        this.icon = new ResourceTexture(textureName + ".png").scale(16F / 20F);
        this.multiplier = multiplier;
    }
}
