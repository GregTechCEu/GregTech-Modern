package com.gregtechceu.gtceu.api.capability.recipe;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

/**
 * The capability can be input or output or both
 */
public enum IO {
    IN("gtceu.io.import", "import"),
    OUT("gtceu.io.export", "export"),
    BOTH("gtceu.io.both", "both"),
    NONE("gtceu.io.none", "none");

    public final String localeName;
    public final IGuiTexture icon;

    IO(String localeName, String textureName) {
        this.localeName = localeName;
        this.icon = new ResourceTexture("gtceu:textures/gui/icon/io_mode/" + textureName + ".png");
    }

    public boolean support(IO io) {
        if (io == this) return true;
        if (io == NONE) return false;
        return this == BOTH;
    }

}
