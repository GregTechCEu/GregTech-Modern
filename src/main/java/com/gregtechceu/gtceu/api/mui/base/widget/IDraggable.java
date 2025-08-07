package com.gregtechceu.gtceu.api.mui.base.widget;

import com.gregtechceu.gtceu.api.mui.base.layout.IViewport;
import com.gregtechceu.gtceu.api.mui.base.layout.IViewportStack;
import com.gregtechceu.gtceu.api.mui.utils.HoveredWidgetList;
import com.gregtechceu.gtceu.api.mui.widget.DraggableWidget;
import com.gregtechceu.gtceu.api.mui.widget.WidgetTree;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Area;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;

import net.minecraft.client.gui.GuiGraphics;

import org.jetbrains.annotations.Nullable;

/**
 * Marks a widget as draggable.
 * The dragging is handled by ModularUI.
 *
 * @see DraggableWidget
 */
public interface IDraggable extends IViewport {

    /**
     * Gets called every frame after everything else is rendered.
     * Is only called when {@link #isMoving()} is true.
     * Translate to the mouse pos and draw with {@link WidgetTree#drawTree(IWidget, ModularGuiContext)}.
     *
     * @param graphics
     * @param partialTicks difference from last from
     */
    void drawMovingState(GuiGraphics graphics, ModularGuiContext context, float partialTicks);

    /**
     * @param button the mouse button that's holding down
     * @return false if the action should be canceled
     */
    boolean onDragStart(int button);

    /**
     * The dragging has ended and getState == IDLE
     *
     * @param successful is false if this returned to its old position
     */
    void onDragEnd(boolean successful);

    void onDrag(int mouseButton, double timeSinceLastClick);

    /**
     * Gets called when the mouse is released
     *
     * @param widget current top most widget below the mouse
     * @return if the location is valid
     */
    default boolean canDropHere(int x, int y, @Nullable IGuiElement widget) {
        return true;
    }

    /**
     * @return the size and pos during move
     */
    @Nullable
    Area getMovingArea();

    boolean isMoving();

    void setMoving(boolean moving);

    void transform(IViewportStack viewportStack);

    @Override
    default void getWidgetsAt(IViewportStack stack, HoveredWidgetList widgets, int x, int y) {}
}
