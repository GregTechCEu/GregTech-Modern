package com.gregtechceu.gtceu.common.cover.data;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;

public enum DistributionMode implements EnumSelectorWidget.SelectableEnum {
    ROUND_ROBIN_GLOBAL("cover.conveyor.distribution.round_robin_enhanced"),
    ROUND_ROBIN_PRIO("cover.conveyor.distribution.round_robin"),
    INSERT_FIRST("cover.conveyor.distribution.first_insert");

    public static final DistributionMode[] VALUES = values();
    private static final float OFFSET = 1.0f / VALUES.length;

    public final String localeName;

    DistributionMode(String localeName) {
        this.localeName = localeName;
    }

    @Override
    public String getTooltip() {
        return localeName;
    }

    @Override
    public IGuiTexture getIcon() {
        return GuiTextures.DISTRIBUTION_MODE.getSubTexture(0, this.ordinal() * OFFSET, 1, OFFSET);
    }
}
