package com.gregtechceu.gtceu.client.mui.screen.viewport;

import com.gregtechceu.gtceu.api.mui.base.ITheme;
import com.gregtechceu.gtceu.api.mui.base.MCHelper;
import com.gregtechceu.gtceu.api.mui.base.widget.*;
import com.gregtechceu.gtceu.client.mui.CursorHandler;
import com.gregtechceu.gtceu.client.mui.screen.*;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

import com.google.common.collect.AbstractIterator;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.Consumer;

/**
 * This class contains all the info from {@link GuiContext} and additional MUI specific info like the current
 * {@link ModularScreen},
 * current hovered widget, current dragged widget, current focused widget and XEI settings.
 * An instance can only be obtained from {@link ModularScreen#getContext()}. One instance is created every time a
 * {@link ModularScreen}
 * is created.
 */
public class ModularGuiContext extends GuiContext {

    /* GUI elements */
    @Getter
    private final ModularScreen screen;
    @Getter
    @Setter
    private @Nullable Screen parent;
    @Getter
    private LocatedWidget focusedWidget = LocatedWidget.EMPTY;
    private List<LocatedWidget> belowMouse = Collections.emptyList();
    /**
     * the hovered widget (widget directly below the mouse)
     */
    private List<LocatedWidget> hovered = Collections.emptyList();
    private LocatedWidget resizeable = null;
    private final HoveredIterable hoveredWidgets;

    private LocatedElement<IDraggable> draggable;
    private int lastButton = -1;
    private long lastClickTime = 0;
    private int lastDragX, lastDragY;

    public List<Consumer<ModularGuiContext>> postRenderCallbacks = new ArrayList<>();

    private UISettings settings;

    private final Iterable<IWidget> hoveredIterable = () -> new AbstractIterator<>() {

        private final List<LocatedWidget> currentHovered = ModularGuiContext.this.hovered;
        private final Iterator<LocatedWidget> it = currentHovered.iterator();

        @Override
        protected @Nullable IWidget computeNext() {
            if (ModularGuiContext.this.hovered != this.currentHovered) {
                throw new ConcurrentModificationException("Tried to use hovered iterable over multiple ticks," +
                        "where hovered list changed. This is not allowed.");
            }
            return this.it.hasNext() ? this.it.next().getElement() : computeNext();
        }
    };

    public ModularGuiContext(ModularScreen screen) {
        this.screen = screen;
        this.hoveredWidgets = new HoveredIterable(this.screen.getPanelManager());
    }

    /**
     * @return true if any widget is being hovered
     */
    public boolean isHovered() {
        return !this.hovered.isEmpty();
    }

    /**
     * @return true if the widget is directly below the mouse
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.7.0")
    @Deprecated
    public boolean isHovered(IGuiElement guiElement) {
        return guiElement.isHovering();
    }

    /**
     * Checks if a widget is hovered for a certain amount of ticks
     *
     * @param guiElement widget
     * @param ticks      time hovered
     * @return true if the widget is hovered for at least a certain number of ticks
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.7.0")
    @Deprecated
    public boolean isHoveredFor(IGuiElement guiElement, int ticks) {
        // convert from frames per second to ticks per second
        return guiElement.isHoveringFor(ticks);
    }

    /**
     * @return the hovered widget (widget directly below the mouse)
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.7.0")
    @Deprecated
    @Nullable
    public IWidget getHovered() {
        return getTopHovered();
    }

    public @Nullable IWidget getTopHovered() {
        return this.hovered.isEmpty() ? null : this.hovered.get(0).getElement();
    }

    @UnmodifiableView
    public Iterable<IWidget> getAllHovered() {
        return this.hoveredIterable;
    }

    /**
     * @return all widgets which are below the mouse ({@link GuiContext#isAbove(IGuiElement)} is true)
     */
    public Iterable<IGuiElement> getAllBelowMouse() {
        return this.hoveredWidgets;
    }

    /**
     * @return true if there is any focused widget
     */
    public boolean isFocused() {
        return this.focusedWidget.getElement() != null;
    }

    /* Element focusing */

    /**
     * @return true if there is any focused widget
     */
    public boolean isFocused(IFocusedWidget widget) {
        return this.focusedWidget.getElement() == widget;
    }

    /**
     * Tries to focus the given widget
     *
     * @param widget widget to focus
     */
    public void focus(IFocusedWidget widget) {
        focus(LocatedWidget.of((IWidget) widget));
    }

    /**
     * Tries to focus the given widget
     *
     * @param widget widget to focus
     */
    public void focus(@NotNull LocatedWidget widget) {
        if (this.focusedWidget.getElement() == widget.getElement()) {
            return;
        }

        if (widget.getElement() != null && !(widget.getElement() instanceof IFocusedWidget)) {
            throw new IllegalArgumentException();
        }

        if (this.focusedWidget.getElement() != null) {
            IFocusedWidget focusedWidget = (IFocusedWidget) this.focusedWidget.getElement();
            focusedWidget.onRemoveFocus(this);
            this.screen.setFocused(false);
        }

        this.focusedWidget = widget;

        if (this.focusedWidget.getElement() != null) {
            IFocusedWidget focusedWidget = (IFocusedWidget) this.focusedWidget.getElement();
            focusedWidget.onFocus(this);
            this.screen.setFocused(true);
        }
    }

    /**
     * Removes focus from any widget
     */
    public void removeFocus() {
        focus(LocatedWidget.EMPTY);
    }

    /**
     * Tries to find the next focusable widget.
     *
     * @param parent focusable context
     * @return true if successful
     */
    public boolean focusNext(IWidget parent) {
        return focus(parent, -1, 1);
    }

    /**
     * Tries to find the previous focusable widget.
     *
     * @param parent focusable context
     * @return true if successful
     */
    public boolean focusPrevious(IWidget parent) {
        return focus(parent, -1, -1);
    }

    public boolean focus(IWidget parent, int index, int factor) {
        return focus(parent, index, factor, false);
    }

    /**
     * Focus next focusable GUI element
     */
    public boolean focus(IWidget widget, int index, int factor, boolean stop) {
        List<IWidget> children = widget.getChildren();

        factor = factor >= 0 ? 1 : -1;
        index += factor;

        for (; index >= 0 && index < children.size(); index += factor) {
            IWidget child = children.get(index);

            if (!child.isEnabled()) {
                continue;
            }

            if (child instanceof IFocusedWidget focusedWidget1) {
                focus(focusedWidget1);

                return true;
            } else {
                int start = factor > 0 ? -1 : child.getChildren().size();

                if (focus(child, start, factor, true)) {
                    return true;
                }
            }
        }

        IWidget grandparent = widget.getParent();
        boolean isRoot = grandparent instanceof ModularPanel;

        if (!stop && (isRoot || grandparent.canBeSeen(this))) {
            List<IWidget> siblings = grandparent.getChildren();
            if (focus(grandparent, siblings.indexOf(widget), factor)) {
                return true;
            }
            if (isRoot) {
                return focus(grandparent, factor > 0 ? -1 : siblings.size() - 1, factor);
            }
        }

        return false;
    }

    /* draggable */

    public boolean hasDraggable() {
        return this.draggable != null;
    }

    public boolean isMouseItemEmpty() {
        Player player = MCHelper.getPlayer();
        return player == null || player.containerMenu.getCarried().isEmpty();
    }

    @ApiStatus.Internal
    public boolean onMousePressed(double mouseX, double mouseY, int button) {
        if ((button == 0 || button == 1) && isMouseItemEmpty() && hasDraggable()) {
            dropDraggable();
            return true;
        }
        return false;
    }

    @ApiStatus.Internal
    public boolean onMouseReleased(double mouseX, double mouseY, int button) {
        if (button == this.lastButton && isMouseItemEmpty() && hasDraggable()) {
            long time = Util.getMillis();
            if (time - this.lastClickTime < 200) return false;
            dropDraggable();
            return true;
        }
        return false;
    }

    @ApiStatus.Internal
    public void dropDraggable() {
        this.draggable.applyMatrix(this);
        this.draggable.getElement()
                .onDragEnd(this.draggable.getElement().canDropHere(getAbsMouseX(), getAbsMouseY(), getTopHovered()));
        // TODO: getTopHovered correct here?
        this.draggable.getElement().setMoving(false);
        this.draggable.unapplyMatrix(this);
        this.draggable = null;
        this.lastButton = -1;
        this.lastClickTime = 0;
    }

    @ApiStatus.Internal
    public boolean onHoveredClick(int button, LocatedWidget hovered) {
        if ((button == 0 || button == 1) && isMouseItemEmpty() && !hasDraggable()) {
            IWidget widget = hovered.getElement();
            LocatedElement<IDraggable> draggable;
            if (widget instanceof IDraggable iDraggable) {
                draggable = new LocatedElement<>(iDraggable, hovered.getTransformationMatrix());
            } else if (widget instanceof ModularPanel panel) {
                if (panel.isDraggable()) {
                    if (!panel.flex().hasFixedSize()) {
                        throw new IllegalStateException(
                                "Panel must have a fixed size. It can't specify left AND right or top AND bottom!");
                    }
                    draggable = new LocatedElement<>(new DraggablePanelWrapper(panel), TransformationMatrix.EMPTY);
                } else {
                    return false;
                }
            } else {
                return false;
            }
            if (draggable.getElement().onDragStart(button)) {
                draggable.getElement().setMoving(true);

                this.draggable = draggable;
                this.lastButton = button;
                this.lastClickTime = Util.getMillis();
                return true;
            }
        }
        return false;
    }

    @ApiStatus.Internal
    public void drawDraggable(GuiGraphics graphics) {
        if (hasDraggable()) {
            this.draggable.applyMatrix(this);
            this.draggable.getElement().drawMovingState(graphics, this, getPartialTicks());
            this.draggable.unapplyMatrix(this);
        }
    }

    private static boolean isStillHovered(List<LocatedWidget> newHovered, LocatedWidget lw) {
        if (newHovered == null) return false;
        for (LocatedWidget hovered : newHovered) {
            if (hovered.getElement() == lw.getElement()) {
                return true;
            }
        }
        return false;
    }

    @ApiStatus.Internal
    public void onFrameUpdate() {
        if (hasDraggable() && (this.lastDragX != getAbsMouseX() || this.lastDragY != getAbsMouseY())) {
            this.lastDragX = getAbsMouseX();
            this.lastDragY = getAbsMouseY();
            this.draggable.applyMatrix(this);
            this.draggable.getElement().onDrag(this.lastButton, this.lastClickTime);
            this.draggable.unapplyMatrix(this);
        }
        List<LocatedWidget> newBelowMouse = this.screen.getPanelManager().getAllHoveredWidgetsList(false);
        if (!newBelowMouse.isEmpty()) {
            List<LocatedWidget> oldBelowMouse = this.belowMouse;
            this.belowMouse = newBelowMouse;
            for (LocatedWidget lw : this.belowMouse) {
                if (lw.getElement().isValid() && !lw.getElement().isBelowMouse()) {
                    lw.getElement().onMouseEnterArea();
                }
            }

            List<LocatedWidget> newHovered = getHoveredWidgets(newBelowMouse);
            List<LocatedWidget> oldHovered = this.hovered;
            this.hovered = newHovered;

            checkHoverEnd(newHovered, oldHovered, IWidget::onMouseEndHover);
            checkHoverEnd(newBelowMouse, oldBelowMouse, IWidget::onMouseLeaveArea);
        } else {
            checkHoverEnd(null, this.hovered, IWidget::onMouseEndHover);
            checkHoverEnd(null, this.belowMouse, IWidget::onMouseLeaveArea);
            this.hovered = Collections.emptyList();
            this.belowMouse = Collections.emptyList();
            this.resizeable = null;
            CursorHandler.resetCursorIcon();
        }
    }

    private List<LocatedWidget> getHoveredWidgets(List<LocatedWidget> belowMouse) {
        Slot slot = null;
        LocatedWidget resizeable = null;
        ResizeDragArea newResizeDragArea = null;
        List<LocatedWidget> newHovered = new ArrayList<>();
        for (LocatedWidget lw : belowMouse) {
            if (!lw.getElement().isValid()) continue;
            if (lw.getElement().canHover()) {
                newHovered.add(lw);
                if (!lw.getElement().isHovering()) {
                    lw.getElement().onMouseStartHover();
                }
                if (slot == null && lw.getElement() instanceof IVanillaSlot vanillaSlot &&
                        vanillaSlot.handleAsVanillaSlot()) {
                    slot = vanillaSlot.getVanillaSlot();
                }
                if (lw.getAdditionalHoverInfo() instanceof ResizeDragArea resizeDragArea) {
                    resizeable = lw;
                    newResizeDragArea = resizeDragArea;
                }
                if (!lw.getElement().canHoverThrough()) break;
            }
        }
        ResizeDragArea oldResizeDragArea = this.resizeable != null ?
                (ResizeDragArea) this.resizeable.getAdditionalHoverInfo() : null;
        if (newResizeDragArea != oldResizeDragArea) {
            CursorHandler.setCursorResizeIcon(newResizeDragArea);
            this.resizeable = resizeable;
        }
        this.screen.getScreenWrapper().setHoveredSlot(slot);
        return newHovered.isEmpty() ? Collections.emptyList() : newHovered;
    }

    private void checkHoverEnd(List<LocatedWidget> newList, List<LocatedWidget> oldList, Consumer<IWidget> onHoverEnd) {
        if (!oldList.isEmpty()) {
            for (LocatedWidget lw : oldList) {
                if (!isStillHovered(newList, lw)) {
                    onHoverEnd.accept(lw.getElement());
                }
            }
        }
    }

    public ITheme getTheme() {
        return this.screen.getCurrentTheme();
    }

    @Override
    public boolean isMuiContext() {
        return true;
    }

    @Override
    public ModularGuiContext getMuiContext() {
        return this;
    }

    public UISettings getUISettings() {
        if (this.settings == null) {
            throw new IllegalStateException("The screen is not yet initialised!");
        }
        return this.settings;
    }

    public XeiSettingsImpl getXeiSettings() {
        if (this.screen.isOverlay()) {
            throw new IllegalStateException("Overlays don't have JEI settings!");
        }
        return (XeiSettingsImpl) getUISettings().getXeiSettings();
    }

    @ApiStatus.Internal
    public void setSettings(UISettings settings) {
        if (this.settings != null) {
            throw new IllegalStateException("Tried to set settings twice");
        }
        this.settings = settings;
        if (this.settings.getTheme() != null) {
            this.screen.useTheme(this.settings.getTheme());
        }
    }

    private static class HoveredIterable implements Iterable<IGuiElement> {

        private final PanelManager panelManager;

        private HoveredIterable(PanelManager panelManager) {
            this.panelManager = panelManager;
        }

        @NotNull
        @Override
        public Iterator<IGuiElement> iterator() {
            return new Iterator<>() {

                private final Iterator<ModularPanel> panelIt = HoveredIterable.this.panelManager.getOpenPanels()
                        .iterator();
                private Iterator<LocatedWidget> widgetIt;

                @Override
                public boolean hasNext() {
                    if (this.widgetIt == null) {
                        if (!this.panelIt.hasNext()) {
                            return false;
                        }
                        this.widgetIt = this.panelIt.next().getHovering().iterator();
                    }
                    return this.widgetIt.hasNext();
                }

                @Override
                public IGuiElement next() {
                    if (this.widgetIt == null || !this.widgetIt.hasNext()) {
                        this.widgetIt = this.panelIt.next().getHovering().iterator();
                    }
                    return this.widgetIt.next().getElement();
                }
            };
        }
    }
}
