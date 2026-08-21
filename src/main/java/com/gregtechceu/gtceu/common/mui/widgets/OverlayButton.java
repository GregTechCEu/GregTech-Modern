package com.gregtechceu.gtceu.common.mui.widgets;

import brachy.modularui.widget.SingleChildWidget;
import brachy.modularui.widget.sizer.Area;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.GTGuiScreen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

import brachy.modularui.api.ITheme;
import brachy.modularui.api.MCHelper;
import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.widget.IPositioned;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.api.widget.Interactable;
import brachy.modularui.drawable.GuiDraw;
import brachy.modularui.overlay.OverlayStack;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.ModularScreen;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.ThemeAPI;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.utils.Color;
import brachy.modularui.utils.Point;
import brachy.modularui.widget.Widget;
import brachy.modularui.widget.sizer.StandardResizer;
import brachy.modularui.widgets.ListWidget;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class OverlayButton extends Widget<OverlayButton> implements Interactable {

    private static final IDrawable background = (context, x, y, width, height, widgetTheme) -> {
        GuiGraphics gfx = context.getGraphics();
        GuiDraw.drawRect(gfx, x, y, width, height, Color.GREY.darker(1));
        GuiDraw.drawRect(gfx, x + 1, y + 1, width - 2, height - 2, Color.GREY.main);
    };

    private final String panelName;

    @Getter
    private MenuHandler handler;

    @Accessors(fluent = true)
    @Setter
    private Consumer<ListWidget<IWidget, ?>> menuList;

    @Accessors(fluent = true)
    @Setter
    private Direction direction = Direction.DOWN;

    public OverlayButton(String panelName) {
        this.panelName = panelName;
        size(16);
    }

    @Override
    public void afterInit() {
        this.handler = new MenuHandler(getPanel(), (handler) -> new MenuPanel(this.panelName, getMenu()));
    }

    private MenuWidget getMenu() {
        ListWidget<IWidget, ?> list = new ListWidget<>();
        if (this.menuList != null) {
            this.menuList.accept(list);
        }
        this.direction.position(list);
        list.background(background);
        return new MenuWidget(this).child(list);
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
        this.handler.toggleMenu();
        Interactable.playButtonClickSound();
        return Result.SUCCESS;
    }

    @Override
    public void onMouseLeaveArea() {
        super.onMouseLeaveArea();
        GTCEu.LOGGER.warn("mouse left overlay button area");
    }

    @Override
    public void dispose() {
        this.handler.close();
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

    private void setChild(MenuHandler menuHandler) {
        getHandler().setChild(menuHandler);
    }

    private static class MenuWidget extends SingleChildWidget<MenuWidget> {

        @Getter
        private final OverlayButton owner;

        private boolean justHovered = true;

        private MenuWidget(OverlayButton owner) {
            this.owner = owner;
            Point point = owner.unTransformedPos();
            pos(point.x, point.y);
            Area area = owner.getArea();
            size(area.w(), area.h());
        }

        @Override
        public void onUpdate() {
            super.onUpdate();
            int mx = getContext().getMouseX();
            int my = getContext().getMouseY();
            boolean hoveringMenu = getChild().getArea().isInside(mx,my);
            if (hoveringMenu) {
                if (justHovered) {
                    GTCEu.LOGGER.warn("mouse entered menu area");
                    justHovered = false;
                }
            } else if (!justHovered) {
                GTCEu.LOGGER.warn("mouse left menu area");
                // is hovering overlay button?
                // is hovering a child menu?
                justHovered = true;
            }
        }
    }

    private static class MenuPanel extends ModularPanel<MenuPanel> {

        @Getter
        private final MenuWidget menu;

        public MenuPanel(String name, MenuWidget menu) {
            super(name);
            this.menu = menu;
            fullScreenInvisible();
            child(menu);
        }

        @Override
        public void closeIfOpen() {
            if (!isOpen() || (!getContext().getUItype().isScreen && isMainPanel())) return;
            closeSubPanels();
            if (!shouldAnimate()) {
                closeInternal();
                return;
            }
            if (isOpening() || isClosing()) return;

            getAnimator().onFinish(this::closeInternal);
            getAnimator().reset(true);
            getAnimator().animate(true);
        }

        private void closeInternal() {
            if (GTCEu.isClientSide()) {
                ModularScreen ms = getScreen();
                ms.close();
                // noinspection UnstableApiUsage
                OverlayStack.close(ms);
            }
        }
    }

    // discount "panel" handler
    private static class MenuHandler {

        private final ModularPanel<?> owner;
        private final Function<MenuHandler, ModularPanel<?>> panelSupplier;

        @Getter
        private boolean open;
        private ModularPanel<?> panel;

        private MenuHandler child;

        public MenuHandler(ModularPanel<?> parent, Function<MenuHandler, ModularPanel<?>> panelSupplier) {
            this.owner = parent;
            this.panelSupplier = panelSupplier;
        }

        @SuppressWarnings("UnstableApiUsage")
        public void open() {
            if (open) return;
            this.panel = Objects.requireNonNull(this.panelSupplier.apply(this));
            if (GTCEu.isClientSide()) {
                ModularScreen screen = new GTGuiScreen(this.panel);
                Screen mcScreen = MCHelper.getCurrentScreen();
                screen.constructOverlay(mcScreen);
                OverlayStack.open(screen);
                screen.onResize(mcScreen.width, mcScreen.height);
            }
            getMenuPanel().ifPresent(mp -> mp.getMenu().getOwner().setChild(this));
            open = true;
        }

        private Optional<MenuPanel> getMenuPanel() {
            if (this.owner instanceof MenuPanel) {
                return Optional.of((MenuPanel) this.owner);
            }
            return Optional.empty();
        }

        private void closeChild() {
            if (this.child != null) {
                child.close();
            }
        }

        private void setChild(MenuHandler menuHandler) {
            closeChild();
            this.child = menuHandler;
        }

        public void close() {
            if (open) {
                closeChild();
                this.panel.closeIfOpen();
                open = false;
            }
        }

        public boolean toggleMenu() {
            if (isOpen()) {
                close();
                return true;
            } else {
                open();
                return false;
            }
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
