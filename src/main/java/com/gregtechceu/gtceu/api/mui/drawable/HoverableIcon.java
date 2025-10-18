package com.gregtechceu.gtceu.api.mui.drawable;

import com.gregtechceu.gtceu.api.mui.base.drawable.IHoverable;
import com.gregtechceu.gtceu.api.mui.base.drawable.IIcon;
import com.gregtechceu.gtceu.api.mui.base.widget.ITooltip;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Area;
import com.gregtechceu.gtceu.client.mui.screen.RichTooltip;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HoverableIcon extends DelegateIcon implements IHoverable, ITooltip<HoverableIcon> {

    private final Area area = new Area();
    @Getter
    private @Nullable RichTooltip tooltip;

    public HoverableIcon(IIcon icon) {
        super(icon);
        setRenderedAt(0, 0);
    }

    @Override
    public void setRenderedAt(int x, int y) {
        this.area.set(x, y, getWidth(), getHeight());
    }

    @Override
    public Area getRenderedArea() {
        this.area.setSize(getWidth(), getHeight());
        return this.area;
    }

    @Override
    public @NotNull RichTooltip tooltip() {
        if (this.tooltip == null) this.tooltip = new RichTooltip().parent(area -> area.set(getRenderedArea()));
        return tooltip;
    }

    @Override
    public HoverableIcon tooltip(RichTooltip tooltip) {
        this.tooltip = tooltip;
        return this;
    }
}
