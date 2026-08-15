package com.gregtechceu.gtceu.common.mui.widgets;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.GTGuiScreen;

import net.minecraft.client.gui.GuiGraphics;

import brachy.modularui.api.ITheme;
import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.widget.IPositioned;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.api.widget.Interactable;
import brachy.modularui.drawable.GuiDraw;
import brachy.modularui.overlay.OverlayStack;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.ModularScreen;
import brachy.modularui.theme.ThemeAPI;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.utils.Color;
import brachy.modularui.widget.Widget;
import brachy.modularui.widget.sizer.Area;
import brachy.modularui.widget.sizer.StandardResizer;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.menu.AbstractMenuButton;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class OverlayButton extends Widget<OverlayButton> implements Interactable {

    private static final Map<String, MenuScreen> overlayScreens = new HashMap<>();
    private static final MethodHandle positionerGetter;
    private static final IDrawable background = (context, x, y, width, height, widgetTheme) -> {
        GuiGraphics gfx = context.getGraphics();
        GuiDraw.drawRect(gfx, x, y, width, height, Color.GREY.darker(1));
        GuiDraw.drawRect(gfx, x + 1, y + 1, width - 2, height - 2, Color.GREY.main);
    };

    static {
        MethodHandle handle = null;
        try {
            Field positioner = AbstractMenuButton.Direction.class.getDeclaredField("positioner");
            positioner.setAccessible(true);
            handle = MethodHandles.lookup().unreflectGetter(positioner);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {}
        positionerGetter = handle;
    }

    private static void applyDirection(AbstractMenuButton.Direction direction, IWidget widget) {
        if (positionerGetter == null) return;
        try {
            // noinspection unchecked
            final Consumer<StandardResizer> positioner = (Consumer<StandardResizer>) positionerGetter.invoke(direction);
            positioner.accept(widget.resizer());
        } catch (Throwable ignored) {}
    }

    @SuppressWarnings("UnstableApiUsage")
    private static void open(String panelName, MenuScreen overlay, ModularScreen parent) {
        if (overlayScreens.containsKey(panelName)) {
            GTCEu.LOGGER.warn("Overlay Screen already exists for panel {}", panelName);
            return;
        }
        overlayScreens.put(panelName, overlay);
        overlay.constructOverlay(parent.getScreenWrapper().wrappedScreen());
        OverlayStack.open(overlay);
        Area screenArea = parent.getScreenArea();
        overlay.onResize(screenArea.w(), screenArea.h());
    }

    @SuppressWarnings("UnstableApiUsage")
    private static void close(String panelName) {
        MenuScreen overlay = overlayScreens.remove(panelName);
        if (overlay != null) {
            overlay.close();
            OverlayStack.close(overlay);
        }
    }

    private final String panelName;
    private boolean opened = false;

    @Accessors(fluent = true)
    @Setter
    private Consumer<ListWidget<IWidget, ?>> menuList;

    @Accessors(fluent = true)
    @Setter
    private AbstractMenuButton.Direction direction = AbstractMenuButton.Direction.DOWN;

    public OverlayButton(String panelName) {
        this.panelName = panelName;
        size(16);
        onUpdateListener(b -> {
            if (opened && !(isBelowMouse() || overlayScreens.get(this.panelName).areParentsHovered())) {
                // close(this.panelName);
                // opened = false;
            }
        });
    }

    private MenuScreen constructOverlay() {
        final ModularPanel<?> panel = ModularPanel.defaultPanel(this.panelName)
                .invisible()
                .size(getArea().w(), getArea().h());
        ListWidget<IWidget, ?> list = new ListWidget<>();
        if (menuList != null) {
            menuList.accept(list);
        }
        applyDirection(this.direction, list);
        list.background(background);
        int x = getArea().x;
        int y = getArea().y;
        // get offset
        int x2 = getContext().unTransformX(x, y);
        int y2 = getContext().unTransformY(x, y);
        // apply offset
        int x3 = getArea().x - x2;
        int y3 = getArea().y - y2;
        // use panel as anchor
        panel.pos(x3, y3).child(list);

        return new MenuScreen(panel);
    }

    @Override
    protected WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
        return theme.getWidgetTheme(ThemeAPI.BUTTON);
    }

    @Override
    public @NotNull Result onMousePressed(int button) {
        if (!opened) {
            MenuScreen screen = this.constructOverlay();
            if (getScreen() instanceof MenuScreen menuScreen) {
                // parent child relation
                screen.setParent(menuScreen);
            }
            open(this.panelName, screen, getScreen());
            opened = true;
        } else {
            close(this.panelName);
            opened = false;
        }
        Interactable.playButtonClickSound();
        return Result.SUCCESS;
    }

    @Override
    public void dispose() {
        close(this.panelName);
        super.dispose();
    }

    /**
     * Sets the menu to open in the "up" direction. This does not set a horizontal position.
     * This is best used with {@link IPositioned#widthRel(float) IPositioned.widthRel(1f)}
     *
     * @return this
     */
    public OverlayButton openUp() {
        return direction(AbstractMenuButton.Direction.UP);
    }

    /**
     * Sets the menu to open in the "down" direction. This does not set a horizontal position.
     * This is best used with {@link IPositioned#widthRel(float) IPositioned.widthRel(1f)}
     *
     * @return this
     */
    public OverlayButton openDown() {
        return direction(AbstractMenuButton.Direction.DOWN);
    }

    /**
     * Sets the menu to open in the "left and up" direction.
     *
     * @return this
     */
    public OverlayButton openLeftUp() {
        return direction(AbstractMenuButton.Direction.LEFT_UP);
    }

    /**
     * Sets the menu to open in the "left and down" direction.
     *
     * @return this
     */
    public OverlayButton openLeftDown() {
        return direction(AbstractMenuButton.Direction.LEFT_DOWN);
    }

    /**
     * Sets the menu to open in the "right and up" direction.
     *
     * @return this
     */
    public OverlayButton openRightUp() {
        return direction(AbstractMenuButton.Direction.RIGHT_UP);
    }

    /**
     * Sets the menu to open in the "right and down" direction.
     *
     * @return this
     */
    public OverlayButton openRightDown() {
        return direction(AbstractMenuButton.Direction.RIGHT_DOWN);
    }

    /**
     * Sets the menu to open in no specified direction. The position of the menu must be set manually, or it is left at
     * 0,0.
     *
     * @return this
     */
    public OverlayButton openCustom() {
        return direction(AbstractMenuButton.Direction.UNDEFINED);
    }

    private static final class MenuScreen extends GTGuiScreen {

        public String parent;

        public MenuScreen(@NotNull ModularPanel<?> mainPanel) {
            super(mainPanel);
        }

        public void setParent(MenuScreen parent) {
            this.parent = parent.getMainPanel().getName();
        }

        public boolean areParentsHovered() {
            if (this.getContext().isHovered()) return true;
            if (parent != null && overlayScreens.containsKey(parent)) {
                MenuScreen parent = overlayScreens.get(this.parent);
                return parent != null && parent.areParentsHovered();
            }
            return false;
        }

        @Override
        public void onClose() {
            if (parent != null) {
                OverlayButton.close(parent);
            }
        }
    }
}
