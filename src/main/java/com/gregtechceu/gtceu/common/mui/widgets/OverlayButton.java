package com.gregtechceu.gtceu.common.mui.widgets;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.GTGuiScreen;

import net.minecraft.client.gui.GuiGraphics;

import brachy.modularui.api.ITheme;
import brachy.modularui.api.MCHelper;
import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.widget.IPositioned;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.api.widget.Interactable;
import brachy.modularui.drawable.GuiDraw;
import brachy.modularui.overlay.OverlayStack;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.ThemeAPI;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.utils.Color;
import brachy.modularui.utils.Point;
import brachy.modularui.widget.Widget;
import brachy.modularui.widget.sizer.Area;
import brachy.modularui.widget.sizer.StandardResizer;
import brachy.modularui.widgets.ListWidget;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class OverlayButton extends Widget<OverlayButton> implements Interactable {

    private static final IDrawable background = (context, x, y, width, height, widgetTheme) -> {
        GuiGraphics gfx = context.getGraphics();
        GuiDraw.drawRect(gfx, x, y, width, height, Color.GREY.darker(1));
        GuiDraw.drawRect(gfx, x + 1, y + 1, width - 2, height - 2, Color.GREY.main);
    };

    private final String panelName;

    @Accessors(fluent = true)
    @Setter
    private Consumer<ListWidget<IWidget, ?>> menuList;

    @Accessors(fluent = true)
    @Setter
    private Direction direction = Direction.DOWN;

    private MenuScreen menuScreen;

    private boolean open = false;

    public OverlayButton(String panelName) {
        this.panelName = panelName;
        size(16);
    }

    private MenuScreen constructOverlay() {
        final ModularPanel<?> panel = ModularPanel.defaultPanel(this.panelName)
                .invisible()
                .size(getArea().w(), getArea().h());
        ListWidget<IWidget, ?> list = new ListWidget<>();
        if (menuList != null) {
            menuList.accept(list);
        }
        this.direction.position(list);
        list.background(background);
        Point point = unTransformedPos();
        // use panel as anchor
        panel.pos(point.x, point.y).child(list);

        return new MenuScreen(panel, this);
    }

    private @NotNull Point unTransformedPos() {
        int x = getArea().x, y = getArea().y;
        ModularGuiContext context = getContext();
        return new Point(
                x - context.unTransformX(x, y),
                y - context.unTransformY(x, y));
    }

    @Override
    protected WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
        return theme.getWidgetTheme(ThemeAPI.BUTTON);
    }

    @Override
    public @NotNull Result onMousePressed(int button) {
        // different buttons may have the same panel name
        if (!open) {
            OverlayManager.open(this);
            this.open = true;
        } else {
            OverlayManager.close(this);
            this.open = false;
        }
        Interactable.playButtonClickSound();
        return Result.SUCCESS;
    }

    @Override
    public void dispose() {
        OverlayManager.close(this);
        super.dispose();
    }

    /**
     * Sets the menu to open in the "up" direction. This does not set a horizontal position.
     * This is best used with {@link IPositioned#widthRel(float) IPositioned.widthRel(1f)}
     *
     * @return this
     */
    public OverlayButton openUp() {
        return direction(Direction.UP);
    }

    /**
     * Sets the menu to open in the "down" direction. This does not set a horizontal position.
     * This is best used with {@link IPositioned#widthRel(float) IPositioned.widthRel(1f)}
     *
     * @return this
     */
    public OverlayButton openDown() {
        return direction(Direction.DOWN);
    }

    /**
     * Sets the menu to open in the "left and up" direction.
     *
     * @return this
     */
    public OverlayButton openLeftUp() {
        return direction(Direction.LEFT_UP);
    }

    /**
     * Sets the menu to open in the "left and down" direction.
     *
     * @return this
     */
    public OverlayButton openLeftDown() {
        return direction(Direction.LEFT_DOWN);
    }

    /**
     * Sets the menu to open in the "right and up" direction.
     *
     * @return this
     */
    public OverlayButton openRightUp() {
        return direction(Direction.RIGHT_UP);
    }

    /**
     * Sets the menu to open in the "right and down" direction.
     *
     * @return this
     */
    public OverlayButton openRightDown() {
        return direction(Direction.RIGHT_DOWN);
    }

    /**
     * Sets the menu to open in no specified direction. The position of the menu must be set manually, or it is left at
     * 0,0.
     *
     * @return this
     */
    public OverlayButton openCustom() {
        return direction(Direction.UNDEFINED);
    }

    private static class OverlayManager {

        @SuppressWarnings("UnstableApiUsage")
        private static void open(OverlayButton menuHolder) {
            if (menuHolder.open) {
                GTCEu.LOGGER.warn("Overlay Screen already exists for panel {}", menuHolder.getPanel());
                return;
            }
            var overlay = menuHolder.constructOverlay();
            var parent = menuHolder.getScreen();
            if (parent instanceof MenuScreen menuScreen) {
                menuScreen.setChild(overlay);
            } else {
                GTCEu.LOGGER.warn("new overlay?");
            }

            overlay.constructOverlay(MCHelper.getCurrentScreen());
            OverlayStack.open(overlay);
            Area screenArea = parent.getScreenArea();
            overlay.onResize(screenArea.w(), screenArea.h());
            menuHolder.menuScreen = overlay;
        }

        @SuppressWarnings("UnstableApiUsage")
        private static void close(OverlayButton menuHolder) {
            MenuScreen screen = menuHolder.menuScreen;
            if (screen != null) {
                screen.close();
                OverlayStack.close(screen);
                menuHolder.open = false;
            }
        }
    }

    private static class MenuScreen extends GTGuiScreen {

        private @Nullable MenuScreen child = null;
        private final OverlayButton owner;

        public MenuScreen(@NotNull ModularPanel<?> mainPanel, OverlayButton owner) {
            super(mainPanel);
            this.owner = owner;
        }

        @Override
        public void onOpen() {}

        public void setChild(MenuScreen parent) {
            if (child != null) {
                OverlayManager.close(child.owner);
            }
            this.child = parent;
        }

        @Override
        public void close() {
            if (child != null) {
                child.close();
            }
            super.close();
        }
    }

    // copied from AbstractMenuButton.java
    public enum Direction {

        UNDEFINED,
        UP(resizer -> resizer.bottomRel(1f)),
        DOWN(resizer -> resizer.topRel(1f)),
        LEFT_UP(resizer -> resizer.rightRel(1f).bottom(0)),
        LEFT_DOWN(resizer -> resizer.rightRel(1f).top(0)),
        RIGHT_UP(resizer -> resizer.leftRel(1f).bottom(0)),
        RIGHT_DOWN(resizer -> resizer.leftRel(1f).top(0));

        private final @Nullable Consumer<StandardResizer> positioner;

        Direction() {
            this.positioner = null;
        }

        Direction(Consumer<StandardResizer> positioner) {
            this.positioner = Objects.requireNonNull(positioner);
        }

        public void position(IWidget widget) {
            this.position(widget.resizer());
        }

        public void position(StandardResizer resizer) {
            if (this.positioner != null) {
                this.positioner.accept(resizer);
            }
        }
    }
}
