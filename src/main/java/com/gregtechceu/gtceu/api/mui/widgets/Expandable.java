package com.gregtechceu.gtceu.api.mui.widgets;

import com.gregtechceu.gtceu.api.mui.animation.Animator;
import com.gregtechceu.gtceu.api.mui.animation.MutableObjectAnimator;
import com.gregtechceu.gtceu.api.mui.base.drawable.IInterpolation;
import com.gregtechceu.gtceu.api.mui.base.layout.IViewport;
import com.gregtechceu.gtceu.api.mui.base.layout.IViewportStack;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.base.widget.Interactable;
import com.gregtechceu.gtceu.api.mui.drawable.Stencil;
import com.gregtechceu.gtceu.api.mui.utils.HoveredWidgetList;
import com.gregtechceu.gtceu.api.mui.utils.Interpolation;
import com.gregtechceu.gtceu.api.mui.utils.Rectangle;
import com.gregtechceu.gtceu.api.mui.widget.EmptyWidget;
import com.gregtechceu.gtceu.api.mui.widget.Widget;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Area;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

public class Expandable extends Widget<Expandable> implements Interactable, IViewport {

    private IWidget normalView = new EmptyWidget();
    private IWidget expandedView = new EmptyWidget();
    private final List<IWidget> children = Arrays.asList(normalView, expandedView);
    private List<IWidget> currentChildren = children;
    private boolean expanded = false;
    private Area areaSnapshot;
    private Animator animator;
    private BiConsumer<Rectangle, Boolean> stencilTransform;
    private int animationDuration = 300;
    private IInterpolation interpolation = Interpolation.SINE_OUT;

    public Expandable() {
        coverChildren();
    }

    @Override
    public void onInit() {
        this.children.set(0, normalView);
        this.children.set(1, expandedView);
        this.normalView.setEnabled(!this.expanded);
        this.expandedView.setEnabled(this.expanded);
    }

    @Override
    public void beforeResize(boolean onOpen) {
        super.beforeResize(onOpen);
        this.currentChildren = Collections.singletonList(this.expanded ? this.expandedView : this.normalView);
    }

    @Override
    public void onResized() {
        super.onResized();
        currentChildren = children;
    }

    @Override
    public void postResize() {
        super.postResize();
        if (this.animator != null) {
            this.animator.stop(true);
            this.animator = null;
        }
        if (this.areaSnapshot != null) {
            if (this.animationDuration <= 0) {
                if (!this.expanded) {
                    this.normalView.setEnabled(true);
                    this.expandedView.setEnabled(false);
                }
            } else {
                this.animator = new MutableObjectAnimator<>(getArea(), this.areaSnapshot, getArea().copyOrImmutable())
                        .duration(
                                this.animationDuration)
                        .curve(this.interpolation).onFinish(() -> {
                            if (!this.expanded) {
                                this.normalView.setEnabled(true);
                                this.expandedView.setEnabled(false);
                            }
                        });
                this.animator.animate();
            }
            this.areaSnapshot = null;
        }
    }

    @Override
    @NotNull
    public List<IWidget> getChildren() {
        return currentChildren;
    }

    @Override
    public @NotNull Result onMousePressed(double mouseX, double mouseY, int button) {
        toggle();
        return Result.SUCCESS;
    }

    public void toggle() {
        expanded(!expanded);
    }

    @Override
    public void preDraw(ModularGuiContext context, boolean transformed) {
        if (!transformed) {
            Rectangle rect = new Rectangle(getArea());
            rect.x = 0;
            rect.y = 0;
            if (this.stencilTransform != null) {
                this.stencilTransform.accept(rect, this.expanded);
            }
            Stencil.apply(rect, context);
        }
    }

    @Override
    public void postDraw(ModularGuiContext context, boolean transformed) {
        if (!transformed) {
            Stencil.remove();
        }
    }

    @Override
    public void getSelfAt(IViewportStack stack, HoveredWidgetList widgets, int x, int y) {
        if (isInside(stack, x, y)) {
            widgets.add(this, stack.peek());
        }
    }

    @Override
    public void getWidgetsAt(IViewportStack stack, HoveredWidgetList widgets, int x, int y) {
        if (hasChildren()) {
            IViewport.getChildrenAt(this, stack, widgets, x, y);
        }
    }

    public Expandable expanded(boolean expanded) {
        if (this.expanded == expanded) return this;
        this.expanded = expanded;
        if (expanded) {
            this.normalView.setEnabled(false);
            this.expandedView.setEnabled(true);
        }
        if (isValid()) {
            this.areaSnapshot = getArea().copyOrImmutable();
            scheduleResize();
        }
        return this;
    }

    public Expandable normalView(IWidget normalView) {
        this.normalView = normalView;
        this.children.set(0, normalView);
        if (isValid()) {
            this.normalView.initialise(this, true);
        }
        return this;
    }

    public Expandable expandedView(IWidget expandedView) {
        this.expandedView = expandedView;
        this.children.set(1, expandedView);
        if (isValid()) {
            this.expandedView.initialise(this, true);
        }
        return this;
    }

    public Expandable stencilTransform(BiConsumer<Rectangle, Boolean> stencilTransform) {
        this.stencilTransform = stencilTransform;
        return this;
    }

    public Expandable animationDuration(int animationDuration) {
        this.animationDuration = animationDuration;
        return this;
    }

    public Expandable interpolation(IInterpolation interpolation) {
        this.interpolation = interpolation;
        return this;
    }
}
