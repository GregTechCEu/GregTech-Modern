package com.gregtechceu.gtceu.common.cover.data;

import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public enum ItemFilterMode implements EnumSelectorWidget.SelectableEnum {

    FILTER_INSERT("filter_insert"),
    FILTER_EXTRACT("filter_extract"),
    FILTER_BOTH("filter_both");

    public static final ItemFilterMode[] VALUES = values();

    public final String localeName;

    ItemFilterMode(String localeName) {
        this.localeName = localeName;
    }

    @Override
    public String getTooltip() {
        return "cover.filter.mode." + this.localeName;
    }

    @Override
    public IGuiTexture getIcon() {
        return new ResourceTexture("gtceu:textures/gui/icon/item_filter_mode/" + localeName + ".png");
    }
}
