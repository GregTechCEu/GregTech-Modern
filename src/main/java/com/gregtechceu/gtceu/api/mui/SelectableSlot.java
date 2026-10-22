package com.gregtechceu.gtceu.api.mui;

import brachy.modularui.api.value.IBoolValue;
import brachy.modularui.api.value.IIntValue;
import brachy.modularui.drawable.GuiDraw;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.value.BoolValue;
import brachy.modularui.value.IntValue;
import brachy.modularui.widgets.slot.ItemSlot;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Setter
@Accessors(fluent = true)
public class SelectableSlot extends ItemSlot {

    private @Nullable Integer index;
    private IIntValue<?> selectedIndex = new IntValue(-1);
    private IBoolValue<?> selectable = new BoolValue(true);

    private int getIndex() {
        return index == null ? getSlot().getSlotIndex() : index;
    }

    @Override
    public @NotNull Result onMousePressed(int button) {
        if (!selectable.getBoolValue())
            return super.onMousePressed(button);
        selectedIndex.setIntValue(getIndex());
        return Result.SUCCESS;
    }

    @Override
    public boolean onMouseReleased(int button) {
        if (!selectable.getBoolValue())
            return super.onMouseReleased(button);
        return false;
    }

    @Override
    protected void drawOverlay(ModularGuiContext context) {
        if (!selectable.getBoolValue())
            super.drawOverlay(context);
        if (getIndex() == selectedIndex.getIntValue()) {
            GuiDraw.drawBorder(context.getGraphics(), 0, 0, 17, 17, 0xFFFFFF00, 1);
        }
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        if (selectable.getBoolValue() && !isHovering() && getIndex() != selectedIndex.getIntValue()) {
            GuiDraw.drawRect(context.getGraphics(), 1, 1, 15, 15, 0x88444444);
        }
        super.draw(context, widgetTheme);
    }
}
