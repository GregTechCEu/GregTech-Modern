package com.gregtechceu.gtceu.common.cover.data;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

public enum VoidingMode {
    VOID_ANY("cover.voiding.voiding_mode.void_any", "void_any", 1),
    VOID_OVERFLOW("cover.voiding.voiding_mode.void_overflow", "void_overflow", 1024);

    public final String localeName;
    public final IGuiTexture icon;
    public final int maxStackSize;

    VoidingMode(String localeName, String textureName, int maxStackSize) {
        this.localeName = localeName;
        this.maxStackSize = maxStackSize;
        this.icon = new ResourceTexture("gtceu:textures/gui/icon/voiding_mode/" + textureName + ".png");
    }
}
