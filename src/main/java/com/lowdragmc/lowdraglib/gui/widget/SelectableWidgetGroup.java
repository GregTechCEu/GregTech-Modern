package com.lowdragmc.lowdraglib.gui.widget;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;

import net.minecraft.client.gui.GuiGraphics;

import java.util.function.Consumer;

public class SelectableWidgetGroup extends WidgetGroup implements DraggableScrollableWidgetGroup.ISelected {

    private boolean selected;
    private Consumer<SelectableWidgetGroup> onSelected = group -> {};
    private Consumer<SelectableWidgetGroup> onUnSelected = group -> {};
    private IGuiTexture selectedTexture = IGuiTexture.EMPTY;
    private Object prefab;

    public SelectableWidgetGroup(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public SelectableWidgetGroup(Position position) {
        super(position);
    }

    public SelectableWidgetGroup(Position position, Size size) {
        super(position, size);
    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    public SelectableWidgetGroup setOnSelected(Consumer<SelectableWidgetGroup> onSelected) {
        this.onSelected = onSelected;
        return this;
    }

    public SelectableWidgetGroup setOnUnSelected(Consumer<SelectableWidgetGroup> onUnSelected) {
        this.onUnSelected = onUnSelected;
        return this;
    }

    public SelectableWidgetGroup setSelectedTexture(IGuiTexture selectedTexture) {
        this.selectedTexture = selectedTexture;
        return this;
    }

    public SelectableWidgetGroup setSelectedTexture(int color, int border) {
        this.selectedTexture = new ColorRectTexture(color);
        return this;
    }

    @Override
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (selected) {
            selectedTexture.draw(graphics, mouseX, mouseY, getPositionX(), getPositionY(), getSizeWidth(),
                    getSizeHeight());
        }
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
    }

    public boolean allowSelected(double mouseX, double mouseY, int button) {
        return isMouseOverElement(mouseX, mouseY);
    }

    public void onSelected() {
        selected = true;
        onSelected.accept(this);
    }

    public void onUnSelected() {
        selected = false;
        onUnSelected.accept(this);
    }

    @SuppressWarnings("unchecked")
    public <T> T getPrefab() {
        return (T) prefab;
    }

    public void setPrefab(Object prefab) {
        this.prefab = prefab;
    }
}
