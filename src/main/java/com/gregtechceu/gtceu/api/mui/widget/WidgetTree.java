package com.gregtechceu.gtceu.api.mui.widget;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.base.MCHelper;
import com.gregtechceu.gtceu.api.mui.base.layout.ILayoutWidget;
import com.gregtechceu.gtceu.api.mui.base.widget.ISynced;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.value.sync.ModularSyncManager;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Streams;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Helper class to perform operations on widget trees such as traversing, drawing, resizing, finding widgets and
 * printing it.
 */
public class WidgetTree {

    /**
     * If this variable is true, the time it takes to resize a sub widget tree is logged each time.
     * In production this should always be false.
     */
    public static boolean logResizeTime = false;

    public static final WidgetInfo INFO_AREA = (root, widget, builder) -> builder
            .append("Area xywh:")
            .append(widget.getArea().x - root.getArea().x)
            .append(", ")
            .append(widget.getArea().y - root.getArea().y)
            .append(" | ")
            .append(widget.getArea().width)
            .append(", ")
            .append(widget.getArea().height);

    public static final WidgetInfo INFO_ENABLED = (root, widget, builder) -> builder
            .append("Enabled: ").append(widget.isEnabled());

    public static final WidgetInfo INFO_FULLY_RESIZED = (root, widget, builder) -> builder
            .append("Fully resized: ")
            .append(widget.resizer()
                    .isFullyCalculated(widget.hasParent() && widget.getParent() instanceof ILayoutWidget));

    public static final WidgetInfo INFO_RESIZED_DETAILED = (root, widget, builder) -> builder
            .append("Self resized: ")
            .append(widget.resizer()
                    .isSelfFullyCalculated(widget.hasParent() && widget.getParent() instanceof ILayoutWidget))
            .append(", Is pos final: ")
            .append(!widget.resizer().canRelayout(widget.hasParent() && widget.getParent() instanceof ILayoutWidget))
            .append(", Children resized: ").append(widget.resizer().areChildrenCalculated())
            .append(", Layout done: ").append(widget.resizer().isLayoutDone());

    public static final WidgetInfo INFO_RESIZED_COLLAPSED = (root, widget, builder) -> {
        if (widget.resizer().isFullyCalculated(widget.hasParent() && widget.getParent() instanceof ILayoutWidget)) {
            INFO_FULLY_RESIZED.addInfo(root, widget, builder);
        } else {
            INFO_RESIZED_DETAILED.addInfo(root, widget, builder);
        }
    };

    public static final WidgetInfo INFO_WIDGET_THEME = ((root, widget, builder) -> builder
            .append(widget.getWidgetTheme(widget.getContext().getTheme()).getKey().getFullName()));

    private WidgetTree() {}

    public static List<IWidget> getAllChildrenByLayer(IWidget parent) {
        return getAllChildrenByLayer(parent, false);
    }

    public static List<IWidget> getAllChildrenByLayer(IWidget parent, boolean includeSelf) {
        List<IWidget> children = new ArrayList<>();
        if (includeSelf) children.add(parent);
        ObjectList<IWidget> parents = new ObjectArrayList<>();
        parents.add(parent);
        while (!parents.isEmpty()) {
            for (IWidget child : parents.remove(0).getChildren()) {
                if (!child.getChildren().isEmpty()) {
                    parents.add(child);
                }
                children.add(child);
            }
        }
        return children;
    }

    public static boolean foreachChildBFS(IWidget parent, Predicate<IWidget> consumer) {
        return foreachChildBFS(parent, consumer, false);
    }

    /**
     * Iterates through the whole sub widget tree by using breath-first-search.
     * <p>
     * This method delivers good performance and can outperform {@link #foreachChild(IWidget, Predicate, boolean)} in
     * certain small widget
     * trees.
     *
     * @param parent      starting point
     * @param consumer    Operation on each child. Return false to terminate the iteration.
     * @param includeSelf true if the consumer should also consume the parent
     * @return true if the iteration was not terminated by the consumer
     */
    public static boolean foreachChildBFS(IWidget parent, Predicate<IWidget> consumer, boolean includeSelf) {
        if (includeSelf && !consumer.test(parent)) return false;
        ObjectList<IWidget> parents = new ObjectArrayList<>();
        parents.add(parent);
        while (!parents.isEmpty()) {
            for (IWidget child : parents.remove(0).getChildren()) {
                if (child.hasChildren()) {
                    parents.add(child);
                }
                if (!consumer.test(child)) return false;
            }
        }
        return true;
    }

    /**
     * @see #foreachChild(IWidget, Predicate, boolean)
     */
    public static boolean foreachChild(IWidget parent, Predicate<IWidget> consumer) {
        return foreachChild(parent, consumer, false);
    }

    /**
     * Iterates through the whole sub widget tree recursively.
     * <p>
     * This method has the best performance in most cases, but can be outperformed on certain small widget trees.
     *
     * @param parent      starting point
     * @param consumer    Operation on each child. Return false to terminate the iteration.
     * @param includeSelf true if the consumer should also consume the parent
     * @return true if the iteration was not terminated by the consumer
     */
    public static boolean foreachChild(IWidget parent, Predicate<IWidget> consumer, boolean includeSelf) {
        if (includeSelf && !consumer.test(parent)) return false;
        if (!parent.hasChildren()) return true;
        for (IWidget widget : parent.getChildren()) {
            if (!consumer.test(widget)) return false;
            if (widget.hasChildren() && !foreachChild(widget, consumer, false)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Iterates through the whole sub widget tree recursively. Unlike
     * {@link #foreachChild(IWidget, Predicate, boolean)}, which can only
     * return a boolean, this method can return any type. Once the consumer returns a non-null value, the iteration is
     * terminated and the
     * value will be returned.
     *
     * @param parent      starting point
     * @param consumer    Operation on each child. Return a non-null value to terminate the iteration and to return the
     *                    value.
     * @param includeSelf true if the consumer should also consume the parent
     * @return the first resulting value of the consumer or null of it always returned null
     */
    public static <T> @Nullable T foreachChildWithResult(IWidget parent, Function<IWidget, T> consumer,
                                                         boolean includeSelf) {
        if (includeSelf) {
            T t = consumer.apply(parent);
            if (t != null) return t;
        }
        if (!parent.hasChildren()) return null;
        for (IWidget widget : parent.getChildren()) {
            T t = consumer.apply(widget);
            if (t != null) return t;
            if (widget.hasChildren()) {
                t = foreachChildWithResult(widget, consumer, false);
                if (t != null) return t;
            }
        }
        return null;
    }

    public static boolean foreachChildReverse(IWidget parent, Predicate<IWidget> consumer, boolean includeSelf) {
        if (parent.getChildren().isEmpty()) {
            return !includeSelf || consumer.test(parent);
        }
        for (IWidget widget : parent.getChildren()) {
            if (!widget.getChildren().isEmpty() && foreachChildReverse(widget, consumer, false)) {
                return false;
            }
            if (!consumer.test(widget)) return false;
        }
        return !includeSelf || consumer.test(parent);
    }

    /**
     * Creates a flat stream of the whole sub widget tree.
     * <p>
     * {@link Stream#forEach(Consumer)} on this has slightly worse performance than
     * {@link #foreachChildBFS(IWidget, Predicate, boolean)} on
     * small widget trees and has similar performance on large widget trees. The performance is significantly better
     * than
     * {@link #iteratorBFS(IWidget)} even though this method uses it.
     *
     * @param parent starting point.
     * @return stream of the sub widget tree
     */
    @SuppressWarnings("UnstableApiUsage")
    public static Stream<IWidget> flatStream(IWidget parent) {
        if (!parent.hasChildren()) return Stream.of(parent);
        return Streams.stream(iteratorBFS(parent));
    }

    @UnmodifiableView
    public static Iterable<IWidget> iterableBFS(IWidget parent) {
        return () -> iteratorBFS(parent);
    }

    /**
     * Creates an unmodifiable iterator of the whole sub widget tree.
     * <p>
     * This method of iterating has the worst performance in every case. It's roughly 4 times worse than
     * {@link #foreachChildBFS(IWidget, Predicate, boolean)}. If not used extensively the performance is still nothing
     * to worry about.
     *
     * @param parent starting point
     * @return an unmodifiable iterator of the sub widget tree
     */
    @UnmodifiableView
    public static Iterator<IWidget> iteratorBFS(IWidget parent) {
        return new AbstractIterator<>() {

            private final ObjectList<IWidget> queue = ObjectList.of();
            private Iterator<IWidget> currentIt;

            @Override
            protected IWidget computeNext() {
                if (currentIt == null) {
                    currentIt = parent.getChildren().iterator();
                    return parent;
                }
                if (currentIt.hasNext()) return handleWidget(currentIt.next());
                while (!queue.isEmpty()) {
                    currentIt = queue.remove(0).getChildren().iterator();
                    if (currentIt.hasNext()) return handleWidget(currentIt.next());
                }
                return endOfData();
            }

            private IWidget handleWidget(IWidget widget) {
                if (widget.hasChildren()) {
                    queue.add(widget);
                }
                return widget;
            }
        };
    }

    /**
     * Finds all widgets in the sub widget tree which match the test.
     *
     * @param parent starting point
     * @param test   test which the target widgets have to pass
     * @return a list of matching widgets
     */
    public static List<IWidget> collectWidgets(IWidget parent, Predicate<IWidget> test) {
        List<IWidget> widgets = new ArrayList<>();
        foreachChild(parent, w -> {
            if (test.test(w)) widgets.add(w);
            return true;
        }, true);
        return widgets;
    }

    public static <T extends IWidget> List<T> collectWidgetsByType(IWidget parent, Class<T> type) {
        return collectWidgetsByType(parent, type, null);
    }

    /**
     * Finds all widgets in the sub widget tree which match the given type and additional test.
     *
     * @param parent starting point
     * @param type   type of the target widgets
     * @param test   test which the target widgets have to pass
     * @param <T>    type of the target widgets
     * @return a list of matching widgets
     */
    @SuppressWarnings("unchecked")
    public static <T extends IWidget> List<T> collectWidgetsByType(IWidget parent, Class<T> type,
                                                                   @Nullable Predicate<T> test) {
        List<T> widgets = new ArrayList<>();
        foreachChild(parent, w -> {
            if (w.isType(type)) {
                T t = (T) w;
                if (test == null || test.test(t)) widgets.add(t);
            }
            return true;
        }, true);
        return widgets;
    }

    /**
     * Finds the first widget in the sub widget tree, for which the test returns true.
     *
     * @param parent starting point
     * @param test   test which the widget has to pass
     * @return the first matching widget
     */
    public static IWidget findFirst(IWidget parent, @NotNull Predicate<IWidget> test) {
        return foreachChildWithResult(parent, w -> {
            if (test.test(w)) {
                return w;
            }
            return null;
        }, true);
    }

    /**
     * Finds the first widget in the sub widget tree with the given type, for which the test returns true.
     *
     * @param parent starting point
     * @param type   type of the target widget
     * @param test   test which the widget has to pass
     * @return the first matching widget
     */
    @SuppressWarnings("unchecked")
    public static <T extends IWidget> T findFirst(IWidget parent, Class<T> type, @Nullable Predicate<T> test) {
        return foreachChildWithResult(parent, w -> {
            if (w.isType(type)) {
                T t = (T) w;
                if (test == null || test.test(t)) {
                    return t;
                }
            }
            return null;
        }, true);
    }

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

    public static void applyPos(IWidget parent) {
        WidgetTree.foreachChildBFS(parent, child -> {
            child.resizer().applyPos(child);
            return true;
        }, true);
    }

    public static IWidget findParent(IWidget parent, Predicate<IWidget> filter) {
        if (parent == null) return null;
        while (!(parent instanceof ModularPanel)) {
            if (filter.test(parent)) {
                return parent;
            }
            parent = parent.getParent();
        }
        return filter.test(parent) ? parent : null;
    }

    @SuppressWarnings("unchecked")
    public static <T extends IWidget> T findParent(IWidget parent, Class<T> type) {
        if (parent == null) return null;
        while (!(parent instanceof ModularPanel)) {
            if (type.isAssignableFrom(parent.getClass())) {
                return (T) parent;
            }
            parent = parent.getParent();
        }
        return type.isAssignableFrom(parent.getClass()) ? (T) parent : null;
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
    public static void resizeInternal(IWidget parent, boolean onOpen) {
        if (!GTCEu.isClientThread()) return;

        long fullTime = Util.getNanos();

        while (!(parent instanceof ModularPanel) && (parent.getParent() instanceof ILayoutWidget ||
                parent.getParent().flex().dependsOnChildren())) {
            parent = parent.getParent();
        }
        long rawTime = Util.getNanos();
        // resize each widget and calculate their relative pos
        if (!InternalWidgetTree.resizeWidget(parent, true, onOpen, false) &&
                !InternalWidgetTree.resizeWidget(parent, false, onOpen, false)) {
            if (MCHelper.getPlayer() != null) {
                MCHelper.getPlayer().sendSystemMessage(Component.literal("MUI: Failed to resize sub tree of widget " +
                        parent + " of screen " + parent.getScreen().toString() + " '. See log for more info."));
            }
            GTCEu.LOGGER.error("Failed to resize widget. Affected widget tree:");
            printTree(parent, INFO_RESIZED_COLLAPSED);
        }
        rawTime = Util.getNanos() - rawTime;
        // now apply the calculated pos
        applyPos(parent);
        WidgetTree.foreachChildBFS(parent, child -> {
            child.postResize();
            return true;
        }, true);

        if (WidgetTree.logResizeTime) {
            fullTime = Util.getNanos() - fullTime;
            GTCEu.LOGGER.info("Resized widget tree in {} ns and {} ns for full resize.",
                    FormattingUtil.formatNumbers(rawTime),
                    FormattingUtil.formatNumbers(fullTime));
        }
    }

    /**
     * Prints the whole sub widget tree to the log as a human-readable tree graph with unicode characters. You may need
     * to enabled unicode
     * characters in your IDE terminal to display them properly.
     *
     * @param parent starting point
     */
    public static void printTree(IWidget parent) {
        printTree(parent, w -> true, null);
    }

    /**
     * Prints the whole sub widget tree to the log as a human-readable tree graph with unicode characters. You may need
     * to enabled unicode
     * characters in your IDE terminal to display them properly.
     *
     * @param parent         starting point
     * @param additionalInfo additional info function which is executed for each widget
     */
    public static void printTree(IWidget parent, WidgetInfo additionalInfo) {
        printTree(parent, w -> true, additionalInfo);
    }

    /**
     * Prints the whole sub widget tree to the log as a human-readable tree graph with unicode characters. You may need
     * to enabled unicode
     * characters in your IDE terminal to display them properly.
     *
     * @param parent starting point
     * @param test   test widgets have to pass to be added to the string builder
     */
    public static void printTree(IWidget parent, Predicate<IWidget> test) {
        printTree(parent, test, null);
    }

    /**
     * Prints the whole sub widget tree to the log as a human-readable tree graph with unicode characters. You may need
     * to enabled unicode
     * characters in your IDE terminal to display them properly.
     *
     * @param parent         starting point
     * @param test           test widgets have to pass to be added to the string builder
     * @param additionalInfo additional info function which is executed for each widget
     */
    public static void printTree(IWidget parent, Predicate<IWidget> test, WidgetInfo additionalInfo) {
        StringBuilder builder = new StringBuilder("Widget tree of ")
                .append(parent).append('\n');
        GTCEu.LOGGER.info(widgetTreeToString(builder, parent, test, additionalInfo));
    }

    public static String widgetTreeToString(IWidget parent) {
        return widgetTreeToString(parent, w -> true, null);
    }

    public static String widgetTreeToString(IWidget parent, WidgetInfo additionalInfo) {
        return widgetTreeToString(parent, w -> true, additionalInfo);
    }

    public static String widgetTreeToString(IWidget parent, Predicate<IWidget> test) {
        return widgetTreeToString(parent, test, null);
    }

    public static String widgetTreeToString(IWidget parent, Predicate<IWidget> test, WidgetInfo additionalInfo) {
        return widgetTreeToString(null, parent, test, additionalInfo).toString();
    }

    /**
     * Writes the sub widget tree into a human-readable tree graph with unicode characters.
     *
     * @param builder        the string builder to add the tree to or null for a new builder
     * @param parent         starting point
     * @param test           test widgets have to pass to be added to the string builder
     * @param additionalInfo additional info function which is executed for each widget
     * @return the string builder which was used to build the graph
     */
    public static StringBuilder widgetTreeToString(StringBuilder builder, IWidget parent, Predicate<IWidget> test,
                                                   WidgetInfo additionalInfo) {
        if (builder == null) builder = new StringBuilder();
        InternalWidgetTree.getTree(parent, parent, test, builder, additionalInfo, "", false);
        return builder;
    }

    /**
     * An interface to add information of a widget to a string builder.
     */
    public interface WidgetInfo {

        void addInfo(IWidget root, IWidget widget, StringBuilder builder);

        default WidgetInfo combine(WidgetInfo other, String joiner) {
            return (root, widget, builder) -> {
                addInfo(root, widget, builder);
                builder.append(joiner);
                other.addInfo(root, widget, builder);
            };
        }

        default WidgetInfo combine(WidgetInfo other) {
            return combine(other, " | ");
        }

        static WidgetInfo of(String joiner, WidgetInfo... infos) {
            return (root, widget, builder) -> {
                for (int i = 0; i < infos.length; i++) {
                    WidgetInfo info = infos[i];
                    info.addInfo(root, widget, builder);
                    if (i < infos.length - 1) {
                        builder.append(joiner);
                    }
                }
            };
        }

        static WidgetInfo of(WidgetInfo... infos) {
            return of(" | ", infos);
        }
    }
}
