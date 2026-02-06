package com.gregtechceu.gtceu.api.mui.widget;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.base.MCHelper;
import com.gregtechceu.gtceu.api.mui.base.widget.ISynced;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.utils.TreeUtil;
import com.gregtechceu.gtceu.api.mui.value.sync.ModularSyncManager;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widget.sizer.ResizeNode;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;

import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Helper class to perform operations on widget trees such as traversing, drawing, resizing, finding widgets and
 * printing it.
 */
public class WidgetTree extends TreeUtil {

    /**
     * If this variable is true, the time it takes to resize a sub widget tree is logged each time.
     * In production this should always be false.
     */
    public static boolean logResizeTime = false;

    public static final WidgetInfo WIDGET_INFO_AREA = (root, widget, builder) -> builder
            .append("Area xywh:")
            .append(widget.getArea().x - root.getArea().x)
            .append(", ")
            .append(widget.getArea().y - root.getArea().y)
            .append(" | ")
            .append(widget.getArea().width)
            .append(", ")
            .append(widget.getArea().height)
            .append(", rx: ").append(widget.getArea().rx)
            .append(", ry: ").append(widget.getArea().ry);

    public static final WidgetInfo WIDGET_INFO_ENABLED = (root, widget, builder) -> builder.append("Enabled: ")
            .append(widget.isEnabled());
    public static final WidgetInfo WIDGET_INFO_WIDGET_THEME = (root, widget, builder) -> builder
            .append("Widget theme: ")
            .append(widget.getWidgetTheme(widget.getPanel().getTheme()).getKey().getFullName());

    private WidgetTree() {}

    /**
     * Finds the first widget in the sub widget tree that matches the given name.
     *
     * @param parent starting point
     * @param name   name of the target widget
     * @return the first widget with matching name or null if none was found
     */
    public static @Nullable IWidget findFirstWithNameNullable(IWidget parent, String name) {
        return foreachChildWithResult(parent, w -> w.isName(name) ? w : null, true);
    }

    /**
     * Finds the first widget in the sub widget tree that matches the given name.
     *
     * @param parent starting point
     * @param name   name of the target widget
     * @return the first widget with matching name
     * @throws NoSuchElementException if no widget with the given name and type was found
     */
    public static @NotNull IWidget findFirstWithName(IWidget parent, String name) {
        IWidget w = findFirstWithNameNullable(parent, name);
        if (w == null) {
            throw new NoSuchElementException("Expected to find widget with name '" + name +
                    "' in sub widget tree of '" + parent + "', but non was found.");
        }
        return w;
    }

    /**
     * Finds the first widget in the sub widget tree that matches the given name and type.
     *
     * @param parent starting point
     * @param name   name of the target widget
     * @param type   type of the target widget
     * @param <T>    type of the target widget
     * @return the first widget with matching name and class or null if none was found
     */
    @SuppressWarnings("unchecked")
    public static <T extends IWidget> @Nullable T findFirstWithNameNullable(IWidget parent, String name,
                                                                            Class<T> type) {
        return foreachChildWithResult(parent, w -> w.isNameAndType(name, type) ? (T) w : null, true);
    }

    /**
     * Finds the first widget in the sub widget tree that matches the given name and type.
     *
     * @param parent starting point
     * @param name   name of the target widget
     * @param type   type of the target widget
     * @param <T>    type of the target widget
     * @return the first widget with matching name and class
     * @throws NoSuchElementException if no widget with the given name and type was found.
     */
    public static <T extends IWidget> @NotNull T findFirstWithName(IWidget parent, String name, Class<T> type) {
        T w = findFirstWithNameNullable(parent, name, type);
        if (w == null) {
            throw new NoSuchElementException(
                    "Expected to find widget with name '" + name + "' and type '" + type.getName() +
                            "' in sub widget tree of '" + parent + "', but non was found.");
        }
        return w;
    }

    /**
     * Finds a child of a parent at a given path. Each part of the path is name of a widget. That means every widget in
     * the path to the
     * target must be named and their names must be included in order.
     *
     * @param parent starting point
     * @param path   path with each widget name in the path to the target
     * @return the widget at the given path or null if none was found.
     * @throws IllegalArgumentException if the path is empty
     */
    public static @Nullable IWidget findChildAtNullable(IWidget parent, String... path) {
        if (path.length == 0) throw new IllegalArgumentException("Path to child must not be empty!");
        return InternalWidgetTree.findChildAt(parent, IWidget.class, path, 0, true);
    }

    /**
     * Finds a child of a parent at a given path. Each part of the path is name of a widget. That means every widget in
     * the path to the
     * target must be named and their names must be included in order.
     *
     * @param parent starting point
     * @param path   path with each widget name in the path to the target
     * @return the widget at the given path
     * @throws IllegalArgumentException if the path is empty
     * @throws NoSuchElementException   if a single widget in the path could not be found
     */
    public static @NotNull IWidget findChildAt(IWidget parent, String... path) {
        if (path.length == 0) throw new IllegalArgumentException("Path to child must not be empty!");
        return InternalWidgetTree.findChildAt(parent, IWidget.class, path, 0, false);
    }

    /**
     * Finds a child of a parent at a given path. Each part of the path is name of a widget. That means every widget in
     * the path to the
     * target must be named and their names must be included in order.
     *
     * @param parent starting point
     * @param type   type of the target
     * @param path   path with each widget name in the path to the target
     * @param <T>    the target widget type
     * @return the widget at the given path or null if none was found.
     * @throws IllegalArgumentException if the path is empty
     * @throws ClassCastException       if a target widget was found, but the expected type doesn't match
     */
    public static <T extends IWidget> @Nullable T findChildAtNullable(IWidget parent, Class<T> type, String... path) {
        if (path.length == 0) throw new IllegalArgumentException("Path to child must not be empty!");
        return InternalWidgetTree.findChildAt(parent, type, path, 0, true);
    }

    /**
     * Finds a child of a parent at a given path. Each part of the path is name of a widget. That means every widget in
     * the path to the
     * target must be named and their names must be included in order.
     *
     * @param parent starting point
     * @param type   type of the target
     * @param path   path with each widget name in the path to the target
     * @param <T>    the target widget type
     * @return the widget at the given path
     * @throws IllegalArgumentException if the path is empty
     * @throws ClassCastException       if a target widget was found, but the expected type doesn't match
     * @throws NoSuchElementException   if a single widget in the path could not be found
     */
    public static <T extends IWidget> @NotNull T findChildAt(IWidget parent, Class<T> type, String... path) {
        if (path.length == 0) throw new IllegalArgumentException("Path to child must not be empty!");
        return InternalWidgetTree.findChildAt(parent, type, path, 0, false);
    }

    public static boolean hasSyncedValues(ModularPanel panel) {
        return !foreachChild(panel, widget -> !(widget instanceof ISynced<?> synced) || !synced.isSynced(), true);
    }

    @ApiStatus.Internal
    public static void collectSyncValues(PanelSyncManager syncManager, ModularPanel panel) {
        collectSyncValues(syncManager, panel, true);
    }

    @ApiStatus.Internal
    public static void collectSyncValues(PanelSyncManager syncManager, ModularPanel panel, boolean includePanel) {
        collectSyncValues(syncManager, panel.getName(), panel, includePanel);
    }

    @ApiStatus.Internal
    public static void collectSyncValues(PanelSyncManager syncManager, String panelName, IWidget panel,
                                         boolean includePanel) {
        MutableInt id = new MutableInt(0);
        String syncKey = ModularSyncManager.AUTO_SYNC_PREFIX + panelName;
        foreachChildBFS(panel, widget -> {
            if (widget instanceof ISynced<?> synced) {
                if (synced.isSynced() && !synced.getSyncHandler().isRegistered()) {
                    syncManager.syncValue(syncKey, id.getAndIncrement(), synced.getSyncHandler());
                }
            }
            return true;
        }, includePanel);
    }

    public static int countUnregisteredSyncHandlers(IWidget parent) {
        MutableInt count = new MutableInt();
        foreachChildBFS(parent, widget -> {
            if (widget instanceof ISynced<?> synced && synced.isSynced() && !synced.getSyncHandler().isRegistered()) {
                count.increment();
            }
            return true;
        });
        return count.intValue();
    }

    public static void drawTree(IWidget parent, ModularGuiContext context) {
        drawTree(parent, context, false, true);
    }

    public static void drawTree(IWidget parent, ModularGuiContext context, boolean ignoreEnabled,
                                boolean shouldDrawBackground) {
        InternalWidgetTree.drawTree(parent, context, ignoreEnabled, shouldDrawBackground);
    }

    public static void drawTreeForeground(IWidget parent, ModularGuiContext context) {
        InternalWidgetTree.drawTreeForeground(parent, context);
    }

    @ApiStatus.Internal
    public static void onUpdate(IWidget parent) {
        foreachChildBFS(parent, widget -> {
            widget.onUpdate();
            return true;
        }, true);
    }

    @Deprecated
    public static void resize(IWidget parent) {
        parent.scheduleResize();
    }

    @ApiStatus.Internal
    public static void resizeInternal(ResizeNode parent, boolean onOpen) {
        if (!GTCEu.isClientThread()) return;

        long time = Util.getNanos();

        while (parent.getParent() != null &&
                (parent.getParent().dependsOnChildren() || parent.getParent().isLayout())) {
            parent = parent.getParent();
        }
        // resize each widget and calculate their relative pos
        try {
            if (!InternalWidgetTree.resize(parent, true, onOpen, false) &&
                    !InternalWidgetTree.resize(parent, false, onOpen, false)) {
                if (MCHelper.getPlayer() != null) {
                    MCHelper.getPlayer().sendSystemMessage(Component.literal("MUI: Failed to resize sub tree of " +
                            parent.getDebugDisplayName() + ". See log for more info."));
                }
                GTCEu.LOGGER.error("Failed to resize widget. Affected resize node tree:");
                print(parent, RESIZE_NODE_INFO_RESIZED_COLLAPSED);
            }
            // now apply the calculated pos
            preApplyPos(parent);
            applyPos(parent);
            postFullResize(parent);
        } catch (Throwable e) {
            GTCEu.LOGGER.fatal("An exception was thrown while resizing widgets. Exception:");
            GTCEu.LOGGER.catching(e);
            GTCEu.LOGGER.fatal("Affected node tree:");
            print(parent, RESIZE_NODE_INFO_RESIZED_COLLAPSED);
        }

        if (WidgetTree.logResizeTime) {
            time = Util.getNanos() - time;
            GTCEu.LOGGER.info("Resized widget tree in {} ns and {} ns for full resize.",
                    FormattingUtil.formatNumbers(time),
                    FormattingUtil.formatNumbers(time));
        }
    }

    public static void preApplyPos(ResizeNode parent) {
        parent.preApplyPos();
        for (ResizeNode resizeNode : parent.getChildren()) {
            preApplyPos(resizeNode);
        }
    }

    public static void applyPos(ResizeNode parent) {
        parent.applyPos();
        for (ResizeNode resizeNode : parent.getChildren()) {
            applyPos(resizeNode);
        }
    }

    public static void postFullResize(ResizeNode parent) {
        parent.postFullResize();
        for (ResizeNode resizeNode : parent.getChildren()) {
            postFullResize(resizeNode);
        }
    }

    public static void verifyTree(ResizeNode parent, Set<ResizeNode> visited) {
        if (visited.contains(parent)) {
            throw new IllegalStateException("Found cycling resize node dependencies!");
        }
        visited.add(parent);
        if (!parent.getChildren().isEmpty()) {
            for (ResizeNode child : parent.getChildren()) {
                verifyTree(child, visited);
            }
        }
    }

    /**
     * An interface to add information of a widget to a string builder.
     */
    public interface WidgetInfo extends NodeInfo<IWidget> {}
}
