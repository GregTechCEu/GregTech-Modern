package com.gregtechceu.gtceu.api.capability.recipe;

import brachy.modularui.drawable.UITexture;
import lombok.Getter;

/**
 * The capability can be input or output or both
 */
public enum IO {

    IN("gtceu.io.import", "import"),
    OUT("gtceu.io.export", "export"),
    BOTH("gtceu.io.both", "both"),
    NONE("gtceu.io.none", "none");

    @Getter
    public final String tooltip;
    @Getter
    public final UITexture uiTexture;

    IO(String tooltip, String textureName) {
        this.tooltip = tooltip;
        this.uiTexture = UITexture.fullImage("gtceu:textures/gui/icon/io_mode/" + textureName + ".png");
    }

    public boolean support(IO io) {
        if (io == this) return true;
        if (io == NONE) return false;
        return this == BOTH;
    }
}
