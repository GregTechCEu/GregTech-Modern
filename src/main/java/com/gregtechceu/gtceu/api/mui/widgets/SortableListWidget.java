package com.gregtechceu.gtceu.api.mui.widgets;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.animation.Animator;
import com.gregtechceu.gtceu.api.mui.base.widget.IValueWidget;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.utils.ObjectList;
import com.gregtechceu.gtceu.api.mui.widget.DraggableWidget;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Area;
import com.gregtechceu.gtceu.client.mui.screen.viewport.LocatedWidget;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class SortableListWidget<T> extends ListValueWidget<T, SortableListWidget.Item<T>, SortableListWidget<T>> {

    private Consumer<List<T>> onChange;
    private Consumer<Item<T>> onRemove;
    private int timeSinceLastMove = 0;
    private boolean scheduleAnimation = false;
    private final ObjectList<Area> widgetAreaSnapshots = ObjectList.create();
    private final ObjectList<Animator> animators = ObjectList.create();

    public SortableListWidget() {
        super(Item::getWidgetValue);
        heightRel(1f);
        // this is not desired here in favor of animations
        collapseDisabledChild(false);
    }

    @Override
    public void onInit() {
        super.onInit();
        assignIndexes();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        this.timeSinceLastMove++;
    }

    @Override
    public void beforeResize(boolean onOpen) {
        super.beforeResize(onOpen);
        if (this.scheduleAnimation) {
            this.widgetAreaSnapshots.clear();
            this.widgetAreaSnapshots.size(getTypeChildren().size());
            this.animators.size(getTypeChildren().size());
            @UnmodifiableView
            @NotNull
            List<Item<T>> typeChildren = getTypeChildren();
            for (int i = 0; i < typeChildren.size(); i++) {
                Item<T> item = typeChildren.get(i);
                this.widgetAreaSnapshots.set(i, item.getArea().copyOrImmutable());
            }
        }
    }

    @Override
    public void postResize() {
        super.postResize();
        if (this.scheduleAnimation && !this.widgetAreaSnapshots.isEmpty()) {
            @UnmodifiableView
            @NotNull
            List<Item<T>> typeChildren = getTypeChildren();
            for (int i = 0; i < typeChildren.size(); i++) {
                Item<T> item = typeChildren.get(i);
                Animator current = this.animators.get(i);
                if ((current != null && current.isAnimating()) ||
                        item.getArea().shouldAnimate(this.widgetAreaSnapshots.get(i))) {
                    if (current != null) current.stop(true);
                    Animator animator = item.getArea().animator(this.widgetAreaSnapshots.get(i)).duration(150);
                    this.animators.set(i, animator);
                    animator.animate(true);
                }
            }
        }
        this.scheduleAnimation = false;
    }

    @Override
    public int getDefaultWidth() {
        return 80;
    }

    public void moveTo(int from, int to) {
        if (this.timeSinceLastMove < 3) return;
        if (from < 0 || to < 0 || from == to) {
            GTCEu.LOGGER.error("Failed to move element from {} to {}", from, to);
            return;
        }
        Item<?> child = getTypeChildren().remove(from);
        getChildren().add(to, child);
        assignIndexes();
        if (isValid()) {
            assignIndexes();
            this.scheduleAnimation = true;
            scheduleResize();
        }
        if (this.onChange != null) {
            this.onChange.accept(getValues());
        }
        this.timeSinceLastMove = 0;
    }

    @Override
    public boolean remove(int index) {
        Item<T> widget = getTypeChildren().remove(index);
        if (widget != null) {
            widget.dispose();
            assignIndexes();
            this.scheduleAnimation = true;
            onChildRemove(widget);
            if (this.onChange != null) {
                this.onChange.accept(getValues());
            }
            if (this.onRemove != null) {
                this.onRemove.accept(widget);
            }
            return true;
        }
        return false;
    }

    @Override
    public void onChildAdd(Item<T> child) {
        if (isValid()) {
            assignIndexes();
            this.scheduleAnimation = true;
            if (this.onChange != null) this.onChange.accept(getValues());
            scheduleResize();
        }
    }

    private void assignIndexes() {
        List<Item<T>> children = getTypeChildren();
        for (int i = 0; i < children.size(); i++) {
            children.get(i).index = i;
        }
    }

    public SortableListWidget<T> onChange(Consumer<List<T>> onChange) {
        this.onChange = onChange;
        return this;
    }

    public SortableListWidget<T> onRemove(Consumer<Item<T>> onRemove) {
        this.onRemove = onRemove;
        return this;
    }

    public static class Item<T> extends DraggableWidget<Item<T>> implements IValueWidget<T> {

        private final T value;
        private List<IWidget> children;
        private Predicate<IWidget> dropPredicate;
        private SortableListWidget<T> listWidget;
        @Getter
        private int index = -1;
        private final int movingFrom = -1; // no usages? why added?

        public Item(T value) {
            this.value = value;
            resizer().widthRel(1f).height(18);
            background(GTGuiTextures.BUTTON);
        }

        @Override
        public void onInit() {
            super.onInit();
            if (getParent() instanceof SortableListWidget<?> sortableListWidget) {
                this.listWidget = (SortableListWidget<T>) sortableListWidget;
            }
        }

        @NotNull
        @Override
        public List<IWidget> getChildren() {
            return this.children != null ? this.children : Collections.emptyList();
        }

        @Override
        public boolean canDropHere(int x, int y, @Nullable IWidget widget) {
            return this.dropPredicate == null || this.dropPredicate.test(widget);
        }

        @Override
        public void onDrag(int mouseButton, double timeSinceLastClick) {
            super.onDrag(mouseButton, timeSinceLastClick);
            for (LocatedWidget hovering : getPanel().getAllHoveringList(false)) {
                if (hovering.getElement() instanceof SortableListWidget.Item<?> item && item != this &&
                        item.listWidget == this.listWidget) {
                    this.listWidget.moveTo(this.index, item.index);
                    break;
                }
            }
        }

        @Override
        public void onDragEnd(boolean successful) {}

        @Override
        public T getWidgetValue() {
            return this.value;
        }

        public boolean removeSelfFromList() {
            this.listWidget.remove(this.index);
            return true;
        }

        public Item<T> child(IWidget widget) {
            this.children = Collections.singletonList(widget);
            if (isValid()) widget.initialise(this, true);
            return this;
        }

        public Item<T> child(Function<Item<T>, IWidget> widgetCreator) {
            return child(widgetCreator.apply(this));
        }

        public Item<T> dropPredicate(Predicate<IWidget> dropPredicate) {
            this.dropPredicate = dropPredicate;
            return this;
        }

        /*
         * public Item<T> removeable() {
         * this.removeButton = new ButtonWidget<>()
         * .onMousePressed(mouseButton -> this.listWidget.remove(this.index))
         * .background(GuiTextures.CLOSE.asIcon())
         * .width(10).heightRel(1f)
         * .right(0);
         * return this;
         * }
         * 
         * public Item<T> removeable(Consumer<ButtonWidget<? extends ButtonWidget<?>>> buttonBuilder) {
         * removeable();
         * buttonBuilder.accept(this.removeButton);
         * return this;
         * }
         */
    }
}
