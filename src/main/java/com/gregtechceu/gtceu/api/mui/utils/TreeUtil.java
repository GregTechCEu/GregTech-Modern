package com.gregtechceu.gtceu.api.mui.utils;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.base.ITreeNode;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.widget.sizer.ResizeNode;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Streams;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class TreeUtil {

    public static boolean allowUnicode = true;

    private static final String U_T = "✓";
    private static final String U_F = "✘";
    private static final String T = "T";
    private static final String F = "F";
    private static final String U_PIPE = "│";
    private static final String U_PIPE_MID = "├";
    private static final String U_PIPE_END = "└";
    private static final String PIPE = "|";
    private static final String PIPE_MID = "+";
    private static final String PIPE_END = "-";

    public static final NodeInfo<ResizeNode> RESIZE_NODE_INFO_FULLY_RESIZED = (root, node, builder) -> builder
            .append("Fully resized: ")
            .append(str(node.isFullyCalculated(node.hasParent() && node.getParent().isLayout())));
    public static final NodeInfo<ResizeNode> RESIZE_NODE_INFO_RESIZED_DETAILED = (root, node, builder) -> {
        builder.append("XYWH: ")
                .append(str(node.isXCalculated()))
                .append(str(node.isYCalculated()))
                .append(str(node.isWidthCalculated()))
                .append(str(node.isHeightCalculated()))
                .append(", Children resized: ").append(str(node.areChildrenCalculated()))
                .append(", Self layout done: ").append(str(node.isLayoutDone()))
                .append(", Parent layout done: ")
                .append(str(!node.canRelayout(node.hasParent() && node.getParent().isLayout())));
        /*
         * if (!self) {
         * builder.append(", Self detail: (");
         * RESIZE_NODE_INFO_SELF_RESIZED_DETAIL.addInfo(root, node, builder);
         * builder.append(")");
         * }
         */
    };
    public static final NodeInfo<ResizeNode> RESIZE_NODE_INFO_RESIZED_COLLAPSED = (root, node, builder) -> {
        if (node.isFullyCalculated(node.hasParent() && node.getParent().isLayout())) {
            RESIZE_NODE_INFO_FULLY_RESIZED.addInfo(root, node, builder);
        } else {
            RESIZE_NODE_INFO_RESIZED_DETAILED.addInfo(root, node, builder);
        }
    };

    private static String str(boolean b) {
        if (TreeUtil.allowUnicode) return b ? U_T : U_F;
        return b ? T : F;
    }

    public static <T extends ITreeNode<T>> boolean foreachChildBFS(T parent, Predicate<T> consumer) {
        return foreachChildBFS(parent, consumer, false);
    }

    /**
     * Iterates through the whole sub widget tree by using breath-first-search.
     * <p>
     * This method delivers good performance and can outperform {@link #foreachChild(T, Predicate, boolean)} in certain
     * small widget
     * trees.
     *
     * @param parent      starting point
     * @param consumer    Operation on each child. Return false to terminate the iteration.
     * @param includeSelf true if the consumer should also consume the parent
     * @return true if the iteration was not terminated by the consumer
     */
    public static <T extends ITreeNode<T>> boolean foreachChildBFS(T parent, Predicate<T> consumer,
                                                                   boolean includeSelf) {
        if (includeSelf && !consumer.test(parent)) return false;
        ObjectList<T> parents = ObjectList.create();
        parents.add(parent);
        while (!parents.isEmpty()) {
            for (T child : parents.removeFirst().getChildren()) {
                if (child.hasChildren()) {
                    parents.addLast(child);
                }
                if (!consumer.test(child)) return false;
            }
        }
        return true;
    }

    /**
     * @see #foreachChild(T, Predicate, boolean)
     */
    public static <T extends ITreeNode<T>> boolean foreachChild(T parent, Predicate<T> consumer) {
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
    public static <T extends ITreeNode<T>> boolean foreachChild(T parent, Predicate<T> consumer, boolean includeSelf) {
        if (includeSelf && !consumer.test(parent)) return false;
        if (!parent.hasChildren()) return true;
        for (T widget : parent.getChildren()) {
            if (!consumer.test(widget)) return false;
            if (widget.hasChildren() && !foreachChild(widget, consumer, false)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Iterates through the whole sub widget tree recursively. Unlike {@link #foreachChild(T, Predicate, boolean)},
     * which can only
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
    public static <T extends ITreeNode<T>, V> @Nullable V foreachChildWithResult(T parent, Function<T, V> consumer,
                                                                                 boolean includeSelf) {
        if (includeSelf) {
            V t = consumer.apply(parent);
            if (t != null) return t;
        }
        if (!parent.hasChildren()) return null;
        for (T widget : parent.getChildren()) {
            V t = consumer.apply(widget);
            if (t != null) return t;
            if (widget.hasChildren()) {
                t = foreachChildWithResult(widget, consumer, false);
                if (t != null) return t;
            }
        }
        return null;
    }

    public static <T extends ITreeNode<T>> boolean foreachChildReverse(T parent, Predicate<T> consumer,
                                                                       boolean includeSelf) {
        if (parent.getChildren().isEmpty()) {
            return !includeSelf || consumer.test(parent);
        }
        for (T widget : parent.getChildren()) {
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
     * {@link #foreachChildBFS(T, Predicate, boolean)} on
     * small widget trees and has similar performance on large widget trees. The performance is significantly better
     * than
     * {@link #iteratorBFS(T)} even though this method uses it.
     *
     * @param parent starting point.
     * @return stream of the sub widget tree
     */
    @SuppressWarnings("UnstableApiUsage")
    public static <T extends ITreeNode<T>> Stream<T> flatStreamBFS(T parent) {
        if (!parent.hasChildren()) return Stream.of(parent);
        return Streams.stream(iteratorBFS(parent));
    }

    public static <T extends ITreeNode<T>> @UnmodifiableView Iterable<T> iterableBFS(T parent) {
        return () -> iteratorBFS(parent);
    }

    /**
     * Creates an unmodifiable iterator of the whole sub widget tree.
     * <p>
     * This method of iterating has the worst performance in every case. It's roughly 4 times worse than
     * {@link #foreachChildBFS(T, Predicate, boolean)}. If not used extensively the performance is still nothing to
     * worry about.
     *
     * @param parent starting point
     * @return an unmodifiable iterator of the sub widget tree
     */
    public static <T extends ITreeNode<T>> @UnmodifiableView Iterator<T> iteratorBFS(T parent) {
        return new AbstractIterator<>() {

            private final ObjectList<T> queue = ObjectList.create();
            private Iterator<T> currentIt;

            @Override
            protected T computeNext() {
                if (currentIt == null) {
                    currentIt = parent.getChildren().iterator();
                    return parent;
                }
                if (currentIt.hasNext()) return handleWidget(currentIt.next());
                while (!queue.isEmpty()) {
                    currentIt = queue.removeFirst().getChildren().iterator();
                    if (currentIt.hasNext()) return handleWidget(currentIt.next());
                }
                return endOfData();
            }

            private T handleWidget(T widget) {
                if (widget.hasChildren()) {
                    queue.add(widget);
                }
                return widget;
            }
        };
    }

    /**
     * Finds all children in the sub widget tree which match the test and puts them in a list.
     *
     * @param parent starting point
     * @param test   test which the target children have to pass
     * @return a list of matching children
     */
    public static <T extends ITreeNode<T>> List<T> flatList(T parent, Predicate<T> test) {
        List<T> widgets = new ArrayList<>();
        foreachChild(parent, w -> {
            if (test.test(w)) widgets.add(w);
            return true;
        }, true);
        return widgets;
    }

    /**
     * Finds all children in the sub widget tree which match the test and puts them in a list.
     *
     * @param parent starting point
     * @param test   test which the target children have to pass
     * @return a list of matching children
     */
    public static <T extends ITreeNode<T>> List<T> flatListBFS(T parent, Predicate<T> test) {
        List<T> widgets = new ArrayList<>();
        foreachChildBFS(parent, w -> {
            if (test.test(w)) widgets.add(w);
            return true;
        }, true);
        return widgets;
    }

    public static <T extends ITreeNode<T>, R extends ITreeNode<T>> List<R> flatListByType(T parent, Class<R> type) {
        return flatListByType(parent, type, null);
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
    public static <T extends ITreeNode<T>, R extends ITreeNode<T>> List<R> flatListByType(T parent, Class<R> type,
                                                                                          @Nullable Predicate<R> test) {
        List<R> widgets = new ArrayList<>();
        foreachChild(parent, w -> {
            if (type.isAssignableFrom(w.getClass())) {
                if (test == null || test.test((R) w)) widgets.add((R) w);
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
    public static <T extends ITreeNode<T>> T findFirst(T parent, @NotNull Predicate<T> test) {
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
    public static <T extends ITreeNode<T>, R extends ITreeNode<T>> R findFirst(T parent, Class<R> type,
                                                                               @Nullable Predicate<R> test) {
        return foreachChildWithResult(parent, t -> {
            if (type.isAssignableFrom(t.getClass())) {
                if (test == null || test.test((R) t)) {
                    return (R) t;
                }
            }
            return null;
        }, true);
    }

    public static <T extends ITreeNode<T>> T findParent(T parent, Predicate<T> filter) {
        if (parent == null) return null;
        while (!(parent instanceof ModularPanel)) {
            if (filter.test(parent)) {
                return parent;
            }
            parent = parent.getParent();
        }
        return filter.test(parent) ? parent : null;
    }

    public static <T extends ITreeNode<T>, R extends ITreeNode<T>> R findParent(T parent, Class<R> type) {
        return findParent(parent, type, null);
    }

    @SuppressWarnings("unchecked")
    public static <T extends ITreeNode<T>, R extends ITreeNode<T>> R findParent(T parent, Class<R> type,
                                                                                @Nullable Predicate<R> test) {
        if (parent == null) return null;
        while (!(parent instanceof ModularPanel)) {
            if (type.isAssignableFrom(parent.getClass()) && (test == null || test.test((R) parent))) {
                return (R) parent;
            }
            parent = parent.getParent();
        }
        return type.isAssignableFrom(parent.getClass()) && (test == null || test.test((R) parent)) ? (R) parent : null;
    }

    /**
     * Prints the whole sub widget tree to the log as a human-readable tree graph with Unicode characters. You may need
     * to enabled Unicode
     * characters in your IDE terminal to display them properly.
     *
     * @param parent starting point
     */
    public static <T extends ITreeNode<T>> void print(T parent) {
        print(parent, w -> true, null);
    }

    /**
     * Prints the whole sub widget tree to the log as a human-readable tree graph with Unicode characters. You may need
     * to enabled Unicode
     * characters in your IDE terminal to display them properly.
     *
     * @param parent         starting point
     * @param additionalInfo additional info function which is executed for each widget
     */
    public static <T extends ITreeNode<T>> void print(T parent, NodeInfo<T> additionalInfo) {
        print(parent, w -> true, additionalInfo);
    }

    /**
     * Prints the whole sub widget tree to the log as a human-readable tree graph with Unicode characters. You may need
     * to enabled Unicode
     * characters in your IDE terminal to display them properly.
     *
     * @param parent starting point
     * @param test   test widgets have to pass to be added to the string builder
     */
    public static <T extends ITreeNode<T>> void print(T parent, Predicate<T> test) {
        print(parent, test, null);
    }

    /**
     * Prints the whole sub widget tree to the log as a human-readable tree graph with Unicode characters. You may need
     * to enabled Unicode
     * characters in your IDE terminal to display them properly.
     *
     * @param parent         starting point
     * @param test           test widgets have to pass to be added to the string builder
     * @param additionalInfo additional info function which is executed for each widget
     */
    public static <T extends ITreeNode<T>> void print(T parent, Predicate<T> test, NodeInfo<T> additionalInfo) {
        StringBuilder builder = new StringBuilder();
        if (parent instanceof IWidget) builder.append("Widget");
        else if (parent instanceof ResizeNode) builder.append("ResizeNode");
        else builder.append(parent.getClass());
        builder.append(" tree of ").append(parent).append('\n');
        GTCEu.LOGGER.info(toString(builder, parent, test, additionalInfo));
    }

    public static <T extends ITreeNode<T>> String toString(T parent) {
        return toString(parent, w -> true, null);
    }

    public static <T extends ITreeNode<T>> String toString(T parent, NodeInfo<T> additionalInfo) {
        return toString(parent, w -> true, additionalInfo);
    }

    public static <T extends ITreeNode<T>> String toString(T parent, Predicate<T> test) {
        return toString(parent, test, null);
    }

    public static <T extends ITreeNode<T>> String toString(T parent, Predicate<T> test, NodeInfo<T> additionalInfo) {
        return toString(null, parent, test, additionalInfo).toString();
    }

    /**
     * Writes the sub widget tree into a human-readable tree graph with Unicode characters.
     *
     * @param builder        the string builder to add the tree to or null for a new builder
     * @param parent         starting point
     * @param test           test widgets have to pass to be added to the string builder
     * @param additionalInfo additional info function which is executed for each widget
     * @return the string builder which was used to build the graph
     */
    public static <T extends ITreeNode<T>> StringBuilder toString(StringBuilder builder, T parent, Predicate<T> test,
                                                                  NodeInfo<T> additionalInfo) {
        if (builder == null) builder = new StringBuilder();
        getTree(parent, parent, test, builder, additionalInfo, "", false, null);
        return builder;
    }

    protected static <T extends ITreeNode<T>> void getTree(T root, T parent, Predicate<T> test, StringBuilder builder,
                                                           NodeInfo<T> additionalInfo, String indent,
                                                           boolean hasNextSibling, Set<T> visited) {
        if (!indent.isEmpty()) {
            builder.append(indent);
            if (TreeUtil.allowUnicode) {
                builder.append(hasNextSibling ? U_PIPE_MID : U_PIPE_END);
            } else {
                builder.append(hasNextSibling ? PIPE_MID : PIPE_END);
            }
            builder.append(' ');
        }
        if (visited == null) visited = new ReferenceOpenHashSet<>();
        if (visited.contains(parent)) {
            builder.append("CYCLING TREE FOUND (").append(parent).append(")\n");
            return;
        }
        visited.add(parent);
        builder.append(parent);
        if (additionalInfo != null) {
            builder.append(" {");
            additionalInfo.addInfo(root, parent, builder);
            builder.append("}");
        }
        builder.append('\n');
        if (parent.hasChildren()) {
            List<T> children = parent.getChildren();
            for (int i = 0; i < children.size(); i++) {
                T child = children.get(i);
                if (test.test(child)) {
                    String nextIndent = indent;
                    if (hasNextSibling) {
                        nextIndent += (TreeUtil.allowUnicode ? U_PIPE : PIPE) + ' ';
                    } else {
                        nextIndent += "  ";
                    }
                    getTree(root, child, test, builder, additionalInfo, nextIndent, i < children.size() - 1, visited);
                }
            }
        }
    }

    public interface NodeInfo<T extends ITreeNode<T>> {

        void addInfo(T root, T widget, StringBuilder builder);

        default NodeInfo<T> combine(NodeInfo<T> other, String joiner) {
            return (root, widget, builder) -> {
                addInfo(root, widget, builder);
                builder.append(joiner);
                other.addInfo(root, widget, builder);
            };
        }

        default NodeInfo<T> combine(NodeInfo<T> other) {
            return combine(other, " | ");
        }

        @SafeVarargs
        static <T extends ITreeNode<T>> NodeInfo<T> of(String joiner, NodeInfo<T>... infos) {
            return (root, widget, builder) -> {
                for (int i = 0; i < infos.length; i++) {
                    NodeInfo<T> info = infos[i];
                    info.addInfo(root, widget, builder);
                    if (i < infos.length - 1) {
                        builder.append(joiner);
                    }
                }
            };
        }

        @SafeVarargs
        static <T extends ITreeNode<T>> NodeInfo<T> of(NodeInfo<T>... infos) {
            return of(" | ", infos);
        }
    }
}
