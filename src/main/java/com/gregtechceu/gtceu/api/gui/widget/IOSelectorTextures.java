package com.gregtechceu.gtceu.api.gui.widget;

import com.gregtechceu.gtceu.api.capability.recipe.IO;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

public final class IOSelectorTextures {

    private IOSelectorTextures() {}

    public static Object getIcon(IO io) {
        return new ResourceTexture("gtceu:textures/gui/icon/io_mode/" + io.getTextureName() + ".png");
    }
}
