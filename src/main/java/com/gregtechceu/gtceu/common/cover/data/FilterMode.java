package com.gregtechceu.gtceu.common.cover.data;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public enum FilterMode implements EnumSelectorWidget.SelectableEnum {

    FILTER_INSERT("filter_insert"),
    FILTER_EXTRACT("filter_extract"),
    FILTER_BOTH("filter_both");

    public static final FilterMode[] VALUES = values();

    public final String localeName;

    FilterMode(String localeName) {
        this.localeName = localeName;
    }

    @Override
    public String getTooltip() {
        return "cover.filter.mode." + this.localeName;
    }

    @Override
    public IGuiTexture getIcon() {
        return new ResourceTexture("gtceu:textures/gui/icon/filter_mode/" + localeName + ".png");
    }

    public boolean filters(IO io) {
        return (this == FILTER_INSERT && io.support(IO.IN)) || (this == FILTER_EXTRACT && io.support(IO.OUT)) ||
                (this == FILTER_BOTH);
    }
}
