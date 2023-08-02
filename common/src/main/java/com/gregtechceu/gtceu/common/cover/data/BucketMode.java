package com.gregtechceu.gtceu.common.cover.data;

import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import lombok.Getter;

public enum BucketMode implements EnumSelectorWidget.SelectableEnum {
    BUCKET("cover.bucket.mode.bucket", "minecraft:textures/item/water_bucket", FluidHelper.getBucket()),
    MILLI_BUCKET("cover.bucket.mode.milli_bucket", "gtceu:textures/gui/icon/bucket_mode/water_drop", FluidHelper.getBucket() / 1000L);

    @Getter
    public final String tooltip;
    @Getter
    public final IGuiTexture icon;


    public final long multiplier;

    BucketMode(String tooltip, String textureName, long multiplier) {
        this.tooltip = tooltip;
        this.icon = new ResourceTexture(textureName + ".png").scale(16F / 20F);
        this.multiplier = multiplier;
    }
}
