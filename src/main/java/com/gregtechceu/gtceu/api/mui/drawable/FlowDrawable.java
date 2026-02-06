package com.gregtechceu.gtceu.api.mui.drawable;

import com.gregtechceu.gtceu.api.mui.base.GuiAxis;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.base.drawable.IIcon;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Box;
import com.gregtechceu.gtceu.client.mui.screen.viewport.GuiContext;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;

public class FlowDrawable implements IDrawable {

    public static FlowDrawable row() {
        return new FlowDrawable(GuiAxis.X);
    }

    public static FlowDrawable column() {
        return new FlowDrawable(GuiAxis.Y);
    }

    @Getter
    private final GuiAxis axis;
    @Getter
    private final List<IIcon> icons = new ArrayList<>();
    private Alignment.MainAxis mainAxisAlignment = Alignment.MainAxis.START;
    private Alignment.CrossAxis crossAxisAlignment = Alignment.CrossAxis.CENTER;

    public FlowDrawable(GuiAxis axis) {
        this.axis = axis;
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        if (this.icons.isEmpty()) return;
        if (this.icons.size() == 1) {
            this.icons.get(0).draw(context, x, y, width, height, widgetTheme);
            return;
        }

        GuiAxis otherAxis = this.axis.getOther();
        int size = this.axis.isHorizontal() ? width : height;
        int crossSize = this.axis.isHorizontal() ? height : width;
        int pos = this.axis.isHorizontal() ? x : y;
        int amount = this.icons.size();
        int childrenSize = 0;
        int expandedAmount = 0;
        int space = 0; // child margin

        for (IIcon icon : this.icons) {
            int s = icon.getSize(this.axis);
            if (s <= 0) {
                expandedAmount++;
            } else {
                childrenSize += s;
            }
        }
        Alignment.MainAxis maa = this.mainAxisAlignment;
        if (maa == Alignment.MainAxis.SPACE_BETWEEN || maa == Alignment.MainAxis.SPACE_AROUND) {
            if (expandedAmount > 0) {
                maa = Alignment.MainAxis.START;
            } else {
                space = 0;
            }
        }
        final int spaceCount = Math.max(amount - 1, 0);
        childrenSize += spaceCount * space;

        int lastP = pos;
        if (maa == Alignment.MainAxis.CENTER) {
            lastP += (int) (size / 2f - childrenSize / 2f);
        } else if (maa == Alignment.MainAxis.END) {
            lastP += size - childrenSize;
        }

        int expanderSize = (size - childrenSize) / expandedAmount;
        int is, ics, ip, icp; // s = size, p = pos, c = cross
        for (IIcon icon : this.icons) {
            Box margin = icon.getMargin();
            int s = icon.getSize(this.axis);
            int cs = icon.getSize(otherAxis);
            is = s;
            ics = cs;
            ip = lastP;
            icp = 0;
            if (s <= 0) {
                is = expanderSize;
                if (margin != null) ip += margin.getStart(this.axis);
            }
            if (cs <= 0) {
                ics = crossSize;
                if (margin != null) {
                    ics -= margin.getTotal(otherAxis);
                    icp = margin.getStart(otherAxis);
                }
            } else {
                if (this.crossAxisAlignment == Alignment.CrossAxis.CENTER) {
                    icp = (crossSize - cs) / 2;
                } else if (this.crossAxisAlignment == Alignment.CrossAxis.END) {
                    icp = crossSize - cs;
                }
            }
            if (this.axis.isHorizontal()) {
                icon.draw(context, ip, icp, is, ics, widgetTheme);
            } else {
                icon.draw(context, icp, ip, ics, is, widgetTheme);
            }
            lastP += is;

            if (maa == Alignment.MainAxis.SPACE_BETWEEN) {
                lastP += (size - childrenSize) / spaceCount;
            }
        }
    }

    @Override
    public int getDefaultWidth() {
        return this.axis.isHorizontal() ? getMainAxisDefaultSize() : getCrossAxisDefaultSize();
    }

    @Override
    public int getDefaultHeight() {
        return this.axis.isHorizontal() ? getCrossAxisDefaultSize() : getMainAxisDefaultSize();
    }

    public int getMainAxisDefaultSize() {
        int s = 0;
        for (IIcon icon : this.icons) {
            int is = icon.getSize(this.axis);
            if (is <= 0) is = this.axis.isHorizontal() ? icon.getDefaultWidth() : icon.getDefaultHeight();
            if (is <= 0) is = 10;
            s += is + icon.getMargin().getTotal(this.axis);
        }
        return s;
    }

    public int getCrossAxisDefaultSize() {
        int s = 0;
        GuiAxis axis = this.axis.getOther();
        for (IIcon icon : this.icons) {
            int is = icon.getSize(axis);
            if (is <= 0) is = axis.isHorizontal() ? icon.getDefaultWidth() : icon.getDefaultHeight();
            if (is <= 0) is = 10;
            s += is + icon.getMargin().getTotal(axis);
        }
        return s;
    }

    public FlowDrawable mainAxisAlignment(Alignment.MainAxis maa) {
        this.mainAxisAlignment = maa;
        return this;
    }

    public FlowDrawable crossAxisAlignment(Alignment.CrossAxis caa) {
        this.crossAxisAlignment = caa;
        return this;
    }

    public FlowDrawable icon(IIcon icon) {
        this.icons.add(icon);
        return this;
    }

    public FlowDrawable icons(int amount, IntFunction<IIcon> func) {
        for (int i = 0; i < amount; i++) {
            icon(func.apply(i));
        }
        return this;
    }

    public <T> FlowDrawable icons(Iterable<T> it, Function<T, IIcon> func) {
        for (T t : it) {
            icon(func.apply(t));
        }
        return this;
    }

    public FlowDrawable icons(Collection<IIcon> icons) {
        this.icons.addAll(icons);
        return this;
    }

    public FlowDrawable removeIcon(IIcon icon) {
        this.icons.remove(icon);
        return this;
    }

    public FlowDrawable removeAll() {
        this.icons.clear();
        return this;
    }
}
