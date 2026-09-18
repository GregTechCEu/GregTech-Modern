package brachy.modularui.drawable;

import brachy.modularui.api.drawable.IHoverable;
import brachy.modularui.api.drawable.IIcon;
import brachy.modularui.api.widget.ITooltip;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.widget.sizer.Area;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HoverableIcon that)) return false;
        if (!super.equals(o)) return false;

        return area.equals(that.area) && Objects.equals(tooltip, that.tooltip);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + area.hashCode();
        result = 31 * result + Objects.hashCode(tooltip);
        return result;
    }
}
