package brachy.modularui.widget;

import brachy.modularui.api.layout.IViewport;
import brachy.modularui.api.layout.IViewportStack;
import brachy.modularui.api.widget.IDraggable;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.screen.DraggablePanelWrapper;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.utils.HoveredWidgetList;
import brachy.modularui.widget.sizer.Area;

import net.minecraft.client.gui.GuiGraphicsExtractor;

import org.jetbrains.annotations.Nullable;

public class DragHandle extends Widget<DragHandle> implements IDraggable, IViewport {

    private IDraggable parentDraggable;

    @Override
    public void onInit() {
        IWidget parent = getParent();
        while (!(parent instanceof ModularPanel)) {
            if (parent instanceof IDraggable draggable) {
                this.parentDraggable = draggable;
                return;
            }
            parent = parent.getParent();
        }
        if (((ModularPanel) parent).isDraggable()) {
            this.parentDraggable = new DraggablePanelWrapper((ModularPanel) parent);
        }
    }

    @Override
    public void drawMovingState(GuiGraphicsExtractor graphics, ModularGuiContext context, float partialTicks) {
        if (this.parentDraggable != null) {
            this.parentDraggable.drawMovingState(graphics, context, partialTicks);
        }
    }

    @Override
    public boolean onDragStart(int button) {
        return this.parentDraggable != null && this.parentDraggable.onDragStart(button);
    }

    @Override
    public void onDragEnd(boolean successful) {
        if (this.parentDraggable != null) {
            this.parentDraggable.onDragEnd(successful);
        }
    }

    @Override
    public void onDrag(int mouseButton, double timeSinceLastClick) {
        if (this.parentDraggable != null) {
            this.parentDraggable.onDrag(mouseButton, timeSinceLastClick);
        }
    }

    @Override
    public boolean canDropHere(int x, int y, @Nullable IWidget widget) {
        return this.parentDraggable != null && this.parentDraggable.canDropHere(x, y, widget);
    }

    @Override
    public @Nullable Area getMovingArea() {
        Area.SHARED.reset();
        return this.parentDraggable != null ? this.parentDraggable.getMovingArea() : Area.SHARED;
    }

    @Override
    public boolean isMoving() {
        return this.parentDraggable != null && this.parentDraggable.isMoving();
    }

    @Override
    public void setMoving(boolean moving) {
        if (this.parentDraggable != null) {
            this.parentDraggable.setMoving(moving);
        }
    }

    @Override
    public void transform(IViewportStack stack) {
        super.transform(stack);
    }

    @Override
    public void transformChildren(IViewportStack stack) {
        if (this.parentDraggable instanceof IViewport viewport) {
            viewport.transformChildren(stack);
        }
    }

    @Override
    public void getWidgetsAt(IViewportStack stack, HoveredWidgetList widgets, int x, int y) {
        if (this.parentDraggable instanceof IViewport viewport) {
            viewport.getWidgetsAt(stack, widgets, x, y);
        } else {
            IViewport.super.getWidgetsAt(stack, widgets, x, y);
        }
    }

    @Override
    public void getSelfAt(IViewportStack stack, HoveredWidgetList widgets, int x, int y) {
        if (this.parentDraggable instanceof IViewport viewport) {
            viewport.getSelfAt(stack, widgets, x, y);
        } else {
            IViewport.super.getSelfAt(stack, widgets, x, y);
        }
    }
}
