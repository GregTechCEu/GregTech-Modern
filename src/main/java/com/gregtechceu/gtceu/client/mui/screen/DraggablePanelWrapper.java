package com.gregtechceu.gtceu.client.mui.screen;

import com.gregtechceu.gtceu.api.mui.base.layout.IViewportStack;
import com.gregtechceu.gtceu.api.mui.base.widget.IDraggable;
import com.gregtechceu.gtceu.api.mui.widget.WidgetTree;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Area;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;

import net.minecraft.client.gui.GuiGraphics;

import lombok.Getter;

public class DraggablePanelWrapper implements IDraggable {

    private final ModularPanel panel;
    @Getter
    private final Area movingArea;
    private int relativeClickX, relativeClickY;
    @Getter
    private boolean moving;

    public DraggablePanelWrapper(ModularPanel panel) {
        this.panel = panel;
        this.movingArea = panel.getArea().createCopy();
    }

    @Override
    public void drawMovingState(GuiGraphics graphics, ModularGuiContext context, float partialTicks) {
        context.pushMatrix();
        transform(context);
        WidgetTree.drawTree(this.panel, context, true);
        context.popMatrix();
    }

    @Override
    public boolean onDragStart(int button) {
        if (button == 0) {
            ModularGuiContext context = this.panel.getContext();
            this.movingArea.x = context.transformX(0, 0);
            this.movingArea.y = context.transformY(0, 0);
            this.relativeClickX = context.getMouseX() - this.movingArea.x;
            this.relativeClickY = context.getMouseY() - this.movingArea.y;
            return true;
        }
        return false;
    }

    @Override
    public void onDragEnd(boolean successful) {
        if (successful) {
            float y = this.panel.getContext().getMouseY() - this.relativeClickY;
            float x = this.panel.getContext().getMouseX() - this.relativeClickX;
            y = y / (this.panel.getScreen().getScreenArea().height - this.panel.getArea().height);
            x = x / (this.panel.getScreen().getScreenArea().width - this.panel.getArea().width);
            this.panel.flex().resetPosition();
            this.panel.flex().relativeToScreen();
            this.panel.flex().topRelAnchor(y, y)
                    .leftRelAnchor(x, x);
            WidgetTree.resize(this.panel);
        }
    }

    @Override
    public void onDrag(int mouseButton, double timeSinceLastClick) {
        this.movingArea.x = this.panel.getContext().getMouseX() - this.relativeClickX;
        this.movingArea.y = this.panel.getContext().getMouseY() - this.relativeClickY;
    }

    @Override
    public void setMoving(boolean moving) {
        this.moving = moving;
        this.panel.setEnabled(!moving);
    }

    @Override
    public void transform(IViewportStack stack) {
        if (isMoving()) {
            Area area = this.panel.getArea();
            stack.translate(-area.x, -area.y);
            stack.translate(this.movingArea.x, this.movingArea.y);
        }
    }
}
