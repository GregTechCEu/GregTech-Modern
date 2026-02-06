package com.gregtechceu.gtceu.api.mui.widget;

import com.gregtechceu.gtceu.api.mui.base.layout.IViewportStack;
import com.gregtechceu.gtceu.api.mui.base.widget.IDelegatingWidget;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.utils.MutableSingletonList;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Area;
import com.gregtechceu.gtceu.api.mui.widget.sizer.StandardResizer;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DelegatingWidget extends AbstractWidget implements IDelegatingWidget {

    private final MutableSingletonList<IWidget> delegate = new MutableSingletonList<>();

    public DelegatingWidget(IWidget delegate) {
        this.delegate.set(delegate);
        resizer(new StandardResizer(this));
    }

    protected void setDelegate(IWidget delegate) {
        if (!this.delegate.isEmpty()) {
            this.delegate.get().dispose();
            this.delegate.remove();
        }
        if (delegate != null) {
            this.delegate.set(delegate);
            if (isValid()) {
                initialise(getParent(), true);
                delegate.scheduleResize();
            }
            onChangeDelegate(delegate);
        }
    }

    protected void onChangeDelegate(IWidget delegate) {}

    @Override
    public @NotNull List<IWidget> getChildren() {
        return this.delegate;
    }

    @Override
    public void afterInit() {
        super.resizer().setDefaultParent(null); // remove this widget from the resize node tree
        if (hasChildren()) {
            getDelegate().resizer().setDefaultParentIsDelegating(true);
            getDelegate().resizer().relative(getParent()); // add the delegated widget at the place of this widget on
                                                           // the resize node tree
        }
    }

    @Override
    public void postResize() {
        super.postResize();
        if (getDelegate() != null) {
            Area childArea = getDelegate().getArea();
            Area area = super.getArea();
            area.set(childArea);
            area.rx = childArea.rx;
            area.ry = childArea.ry;
            childArea.rx = 0;
            childArea.ry = 0;
        }
    }

    @Override
    public @NotNull StandardResizer resizer() {
        return getDelegate() != null ? getDelegate().resizer() : super.resizer();
    }

    @Override
    public Area getArea() {
        return getDelegate() != null ? getDelegate().getArea() : super.getArea();
    }

    @Override
    public void transform(IViewportStack stack) {
        stack.translate(super.getArea().rx, super.getArea().ry, 0);
    }

    @Override
    public boolean canBeSeen(IViewportStack stack) {
        return false;
    }

    @Override
    public boolean requiresResize() {
        return getDelegate() != null && getDelegate().requiresResize();
    }

    @Override
    public int getDefaultWidth() {
        return getDelegate() != null ? getDelegate().getDefaultWidth() : super.getDefaultWidth();
    }

    @Override
    public int getDefaultHeight() {
        return getDelegate() != null ? getDelegate().getDefaultHeight() : super.getDefaultHeight();
    }

    @Override
    public IWidget getDelegate() {
        return delegate.getOrNull();
    }
}
