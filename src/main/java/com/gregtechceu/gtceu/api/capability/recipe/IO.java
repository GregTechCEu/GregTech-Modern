package com.gregtechceu.gtceu.api.capability.recipe;

import brachy.modularui.drawable.UITexture;
import lombok.Getter;

/**
 * The capability can be input or output or both
 */
public enum IO {

    IN("import"),
    OUT("export"),
    BOTH("both"),
    NONE("none");

    public final String localeName;
    @Getter
    public final UITexture uiTexture;

    IO(String localeName) {
        this.localeName = localeName;
        this.uiTexture = UITexture.fullImage("gtceu:textures/gui/icon/io_mode/" + localeName + ".png");
    }

    public static String getTitle() {
        return "gtceu.io.title";
    }

    public String getTooltip() {
        return "gtceu.io." + localeName;
    }

    public boolean support(IO io) {
        if (io == this) return true;
        if (io == NONE) return false;
        return this == BOTH;
    }
}
