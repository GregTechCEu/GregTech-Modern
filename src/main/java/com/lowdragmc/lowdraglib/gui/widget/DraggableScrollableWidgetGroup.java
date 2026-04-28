package com.lowdragmc.lowdraglib.gui.widget;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

public class DraggableScrollableWidgetGroup extends WidgetGroup {

    public enum ScrollWheelDirection {
        VERTICAL,
        HORIZONTAL
    }

    public interface ISelected {

        boolean isSelected();
    }

    private int scrollXOffset;
    private int scrollYOffset;
    private boolean draggable = true;
    private boolean scrollable = true;
    private boolean useScissor = true;
    private ScrollWheelDirection scrollWheelDirection = ScrollWheelDirection.VERTICAL;
    private final Set<BiConsumer<Integer, Integer>> moveCallbacks = new HashSet<>();

    public DraggableScrollableWidgetGroup() {}

    public DraggableScrollableWidgetGroup(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public DraggableScrollableWidgetGroup setXScrollBarHeight(int height) {
        return this;
    }

    public DraggableScrollableWidgetGroup setYScrollBarWidth(int width) {
        return this;
    }

    public DraggableScrollableWidgetGroup setBackground(IGuiTexture texture) {
        super.setBackground(texture);
        return this;
    }

    public DraggableScrollableWidgetGroup setXBarStyle(IGuiTexture background, IGuiTexture foreground) {
        return this;
    }

    public DraggableScrollableWidgetGroup setYBarStyle(IGuiTexture background, IGuiTexture foreground) {
        return this;
    }

    public void computeMax() {}

    public int getWidgetBottomHeight() {
        return widgets.stream().mapToInt(widget -> widget.getSelfPositionY() + widget.getSizeHeight()).max().orElse(0);
    }

    public void setScrollXOffset(int scrollXOffset) {
        this.scrollXOffset = scrollXOffset;
    }

    public void setScrollYOffset(int scrollYOffset) {
        this.scrollYOffset = scrollYOffset;
    }

    public void setSelected(Widget widget) {}

    public int getScrollXOffset() {
        return scrollXOffset;
    }

    public int getScrollYOffset() {
        return scrollYOffset;
    }

    public DraggableScrollableWidgetGroup setDraggable(boolean draggable) {
        this.draggable = draggable;
        return this;
    }

    public boolean isDraggable() {
        return draggable;
    }

    public DraggableScrollableWidgetGroup setScrollable(boolean scrollable) {
        this.scrollable = scrollable;
        return this;
    }

    public boolean isScrollable() {
        return scrollable;
    }

    public ScrollWheelDirection getScrollWheelDirection() {
        return scrollWheelDirection;
    }

    public DraggableScrollableWidgetGroup setScrollWheelDirection(ScrollWheelDirection scrollWheelDirection) {
        this.scrollWheelDirection = scrollWheelDirection;
        return this;
    }

    public boolean isUseScissor() {
        return useScissor;
    }

    public DraggableScrollableWidgetGroup setUseScissor(boolean useScissor) {
        this.useScissor = useScissor;
        return this;
    }

    public Set<BiConsumer<Integer, Integer>> getMoveCallbacks() {
        return moveCallbacks;
    }
}
