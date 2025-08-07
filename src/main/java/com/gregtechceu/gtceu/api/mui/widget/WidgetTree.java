package com.gregtechceu.gtceu.api.mui.widget;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.base.GuiAxis;
import com.gregtechceu.gtceu.api.mui.base.layout.ILayoutWidget;
import com.gregtechceu.gtceu.api.mui.base.layout.IResizeable;
import com.gregtechceu.gtceu.api.mui.base.layout.IViewport;
import com.gregtechceu.gtceu.api.mui.base.widget.IGuiElement;
import com.gregtechceu.gtceu.api.mui.base.widget.ISynced;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.api.mui.value.sync.ModularSyncManager;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Area;
import com.gregtechceu.gtceu.api.mui.widgets.layout.IExpander;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.gui.GuiGraphics;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

/**
 * Helper class to apply actions to each widget in a tree.
 */
public class WidgetTree {

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

    public static boolean foreachChildByLayer2(IWidget parent, Predicate<IWidget> consumer, boolean includeSelf) {
        if (includeSelf && !consumer.test(parent)) return false;
        ObjectList<IWidget> parents = new ObjectArrayList<>();
        parents.add(parent);
        while (!parents.isEmpty()) {
            for (IWidget child : parents.remove(0).getChildren()) {
                if (!consumer.test(child)) return false;

                if (child.hasChildren()) {
                    parents.add(child);
                }
            }
        }
        return true;
    }

    public static boolean foreachChild(IWidget parent, Predicate<IWidget> consumer, boolean includeSelf) {
        if (includeSelf && !consumer.test(parent)) return false;
        if (parent.getChildren().isEmpty()) return true;
        for (IWidget widget : parent.getChildren()) {
            if (!consumer.test(widget)) return false;
            if (!widget.getChildren().isEmpty() && foreachChild(widget, consumer, false)) {
                return false;
            }
        }
        return true;
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

    public static void drawTree(IWidget parent, ModularGuiContext context) {
        drawTree(parent, context, false);
    }

    public static void drawTree(IWidget parent, ModularGuiContext context, boolean ignoreEnabled) {
        if (!parent.isEnabled() && !ignoreEnabled) return;

        GuiGraphics graphics = context.getGraphics();
        float alpha = parent.getPanel().getAlpha();
        IViewport viewport = parent instanceof IViewport ? (IViewport) parent : null;

        // transform stack according to the widget
        context.pushMatrix();
        parent.transform(context);

        boolean canBeSeen = parent.canBeSeen(context);

        // apply transformations to opengl
        graphics.pose().pushPose();
        context.applyTo(graphics.pose());

        if (canBeSeen) {
            // draw widget
            RenderSystem.colorMask(true, true, true, true);
            graphics.setColor(1f, 1f, 1f, alpha);
            RenderSystem.enableBlend();
            WidgetTheme widgetTheme = parent.getWidgetTheme(context.getTheme());
            parent.drawBackground(context, widgetTheme);
            parent.draw(context, widgetTheme);
            parent.drawOverlay(context, widgetTheme);
        }

        if (viewport != null) {
            if (canBeSeen) {
                // draw viewport without children transformation
                graphics.setColor(1f, 1f, 1f, alpha);
                RenderSystem.enableBlend();
                viewport.preDraw(context, false);
                graphics.pose().popPose();
                // apply children transformation of the viewport
                context.pushViewport(viewport, parent.getArea());
                viewport.transformChildren(context);
                // apply to opengl and draw with transformation
                graphics.pose().pushPose();
                context.applyTo(graphics.pose());
                viewport.preDraw(context, true);
            } else {
                // only transform stack
                context.pushViewport(viewport, parent.getArea());
                viewport.transformChildren(context);
            }
        }
        // remove all opengl transformations
        graphics.pose().popPose();

        // render all children if there are any
        List<IWidget> children = parent.getChildren();
        if (!children.isEmpty()) {
            children.forEach(widget -> drawTree(widget, context, false));
        }

        if (viewport != null) {
            if (canBeSeen) {
                // apply opengl transformations again and draw
                graphics.setColor(1f, 1f, 1f, alpha);
                RenderSystem.enableBlend();
                graphics.pose().pushPose();
                context.applyTo(graphics.pose());
                viewport.postDraw(context, true);
                // remove children transformation of this viewport
                context.popViewport(viewport);
                graphics.pose().popPose();
                // apply transformation again to opengl and draw
                graphics.pose().pushPose();
                context.applyTo(graphics.pose());
                viewport.postDraw(context, false);
                graphics.pose().popPose();
            } else {
                // only remove transformation
                context.popViewport(viewport);
            }
        }
        // remove all widget transformations
        context.popMatrix();
    }

    public static void drawTreeForeground(IWidget parent, ModularGuiContext context) {
        IViewport viewport = parent instanceof IViewport viewport1 ? viewport1 : null;
        context.pushMatrix();
        parent.transform(context);

        context.getGraphics().setColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        parent.drawForeground(context);

        List<IWidget> children = parent.getChildren();
        if (!children.isEmpty()) {
            if (viewport != null) {
                context.pushViewport(viewport, parent.getArea());
                viewport.transformChildren(context);
            }
            children.forEach(widget -> drawTreeForeground(widget, context));
            if (viewport != null) context.popViewport(viewport);
        }
        context.popMatrix();
    }

    @ApiStatus.Internal
    public static void onUpdate(IWidget parent) {
        foreachChildBFS(parent, widget -> {
            widget.onUpdate();
            return true;
        }, true);
    }

    public static void resize(IWidget parent) {
        if (!GTCEu.isClientThread()) return;

        while(!(parent instanceof ModularPanel) && (parent.getParent() instanceof ILayoutWidget ||
                parent.getParent().flex().dependsOnChildren())) {
            parent = parent.getParent();
        }
        // resize each widget and calculate their relative pos
        if (!resizeWidget(parent, true) && !resizeWidget(parent, false)) {
            throw new IllegalStateException("Failed to resize widgets");
        }
        // now apply the calculated pos
        applyPos(parent);
        WidgetTree.foreachChildBFS(parent, child -> {
            child.postResize();
            return true;
        }, true);
    }

    private static boolean resizeWidget(IWidget widget, boolean init) {
        boolean alreadyCalculated = false;
        // first try to resize this widget
        IResizeable resizer = widget.resizer();
        if (init) {
            widget.beforeResize();
            resizer.initResizing();
        } else {
            // if this is not the first time check if this widget is already resized
            alreadyCalculated = resizer.isFullyCalculated();
        }
        boolean result = alreadyCalculated || resizer.resize(widget);

        GuiAxis expandAxis = widget instanceof IExpander expander ? expander.getExpandAxis() : null;
        // now resize all children and collect children which could not be fully calculated
        List<IWidget> anotherResize = Collections.emptyList();
        if (widget.hasChildren()) {
            anotherResize = new ArrayList<>();
            for (IWidget child : widget.getChildren()) {
                if (init && expandAxis != null) child.flex().checkExpanded(expandAxis);
                if (!resizeWidget(child, init)) {
                    anotherResize.add(child);
                }
            }
        }

        if (!alreadyCalculated) {
            if (widget instanceof ILayoutWidget layoutWidget) {
                layoutWidget.layoutWidgets();
            }

            // post resize this widget if possible
            if (!result) {
                result = resizer.postResize(widget);
            }

            if (widget instanceof ILayoutWidget layoutWidget) {
                layoutWidget.postLayoutWidgets();
            }
        }

        // now fully resize all children which needs it
        if (!anotherResize.isEmpty()) {
            anotherResize.removeIf(iWidget -> resizeWidget(iWidget, false));
        }

        if (result && !alreadyCalculated) widget.onResized();

        return result && anotherResize.isEmpty();
    }

    public static void applyPos(IWidget parent) {
        WidgetTree.foreachChildBFS(parent, child -> {
            child.resizer().applyPos(child);
            return true;
        }, true);
    }

    public static IGuiElement findParent(IGuiElement parent, Predicate<IGuiElement> filter) {
        if (parent == null) return null;
        while (!(parent instanceof ModularPanel)) {
            if (filter.test(parent)) {
                return parent;
            }
            parent = parent.getParent();
        }
        return filter.test(parent) ? parent : null;
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

    @ApiStatus.Internal
    public static void collectSyncValues(PanelSyncManager syncManager, ModularPanel panel) {
        collectSyncValues(syncManager, panel, true);
    }

    @ApiStatus.Internal
    public static void collectSyncValues(PanelSyncManager syncManager, ModularPanel panel, boolean includePanel) {
        AtomicInteger id = new AtomicInteger(0);
        String syncKey = ModularSyncManager.AUTO_SYNC_PREFIX + panel.getName();
        foreachChildBFS(panel, widget -> {
            if (widget instanceof ISynced<?> synced) {
                if (synced.isSynced() && !syncManager.hasSyncHandler(synced.getSyncHandler())) {
                    syncManager.syncValue(syncKey, id.getAndIncrement(), synced.getSyncHandler());
                }
            }
            return true;
        }, includePanel);
    }

    public static boolean hasSyncedValues(ModularPanel panel) {
        return !foreachChildBFS(panel, widget -> !(widget instanceof ISynced<?> synced) || !synced.isSynced(), true);
    }

    public static void print(IWidget parent, Predicate<IWidget> test) {
        StringBuilder builder = new StringBuilder("Widget tree of ")
                .append(parent)
                .append('\n');
        getTree(parent.getArea(), parent, test, builder, 0);
        GTCEu.LOGGER.info(builder.toString());
    }

    private static void getTree(Area root, IWidget parent, Predicate<IWidget> test, StringBuilder builder, int indent) {
        if (indent >= 2) {
            builder.append(StringUtils.repeat(' ', indent - 2))
                    .append("- ");
        }
        builder.append(parent).append(" {")
                .append(parent.getArea().x - root.x)
                .append(", ")
                .append(parent.getArea().y - root.y)
                .append(" | ")
                .append(parent.getArea().width)
                .append(", ")
                .append(parent.getArea().height)
                .append("}\n");
        if (parent.hasChildren()) {
            for (IWidget child : parent.getChildren()) {
                if (test.test(child)) {
                    getTree(root, child, test, builder, indent + 2);
                }
            }
        }
    }
}
