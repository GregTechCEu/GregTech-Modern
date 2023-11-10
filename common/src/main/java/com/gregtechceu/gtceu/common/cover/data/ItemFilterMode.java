package com.gregtechceu.gtceu.common.cover.data;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;

public enum ItemFilterMode implements EnumSelectorWidget.SelectableEnum {

    FILTER_INSERT("cover.filter.mode.filter_insert"),
    FILTER_EXTRACT("cover.filter.mode.filter_extract"),
    FILTER_BOTH("cover.filter.mode.filter_both");

    public static final ItemFilterMode[] VALUES = values();
    private static final float OFFSET = 1.0f / VALUES.length;

    public final String localeName;

    ItemFilterMode(String localeName) {
        this.localeName = localeName;
    }

    @Override
    public String getTooltip() {
        return this.localeName;
    }

    @Override
    public IGuiTexture getIcon() {
        return new TextTexture(this.localeName);
    }
}
