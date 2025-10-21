package com.gregtechceu.gtceu.api.mui.widget;

import com.gregtechceu.gtceu.api.mui.base.layout.IResizeable;
import com.gregtechceu.gtceu.api.mui.base.layout.IViewportStack;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.theme.WidgetThemeEntry;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Area;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Flex;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.ModularScreen;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EmptyWidget implements IWidget {

    @Getter
    private final Area area = new Area();
    @Getter
    private final Flex flex = new Flex(this);
    private boolean requiresResize = false;
    @Setter
    @Getter
    public boolean enabled = true;
    @Getter
    private IWidget parent;

    @Override
    public ModularScreen getScreen() {
        return null;
    }

    @Override
    public void initialise(@NotNull IWidget parent, boolean late) {
        this.parent = parent;
        getArea().setPanelLayer(getPanel().getArea().getPanelLayer());
        getArea().z(parent.getArea().z() + 1);
    }

    @Override
    public void dispose() {
        this.parent = null;
    }

    @Override
    public boolean isValid() {
        return this.parent != null;
    }

    @Override
    public void drawBackground(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {}

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {}

    @Override
    public void drawOverlay(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {}

    @Override
    public void drawForeground(ModularGuiContext context) {}

    @Override
    public void onUpdate() {}

    @Override
    public @NotNull ModularPanel getPanel() {
        return this.parent.getPanel();
    }

    @Override
    public void scheduleResize() {
        this.requiresResize = true;
    }

    @Override
    public boolean requiresResize() {
        return this.requiresResize;
    }

    @Override
    public void onResized() {
        this.requiresResize = false;
    }

    @Override
    public boolean canBeSeen(IViewportStack stack) {
        return false;
    }

    @Override
    public boolean canHover() {
        return false;
    }

    @Override
    public boolean canHoverThrough() {
        return true;
    }

    @Override
    public void markTooltipDirty() {}

    @Override
    public ModularGuiContext getContext() {
        return this.parent.getContext();
    }

    @Override
    public Flex flex() {
        return this.flex;
    }

    @NotNull
    @Override
    public IResizeable resizer() {
        return this.flex;
    }

    @Override
    public void resizer(IResizeable resizer) {}

    @Nullable
    @Override
    public String getName() {
        return null;
    }
}
