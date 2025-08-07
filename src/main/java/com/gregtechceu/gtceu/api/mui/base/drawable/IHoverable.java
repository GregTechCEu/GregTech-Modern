package com.gregtechceu.gtceu.api.mui.base.drawable;

import com.gregtechceu.gtceu.api.mui.widget.sizer.Area;
import com.gregtechceu.gtceu.client.mui.screen.RichTooltip;

import org.jetbrains.annotations.Nullable;

public interface IHoverable extends IIcon {

    /**
     * Called every frame this hoverable is hovered inside a
     * {@link com.gregtechceu.gtceu.api.mui.drawable.text.RichText}.
     */
    default void onHover() {}

    @Nullable
    default RichTooltip getTooltip() {
        return null;
    }

    void setRenderedAt(int x, int y);

    Area getRenderedArea();
}
