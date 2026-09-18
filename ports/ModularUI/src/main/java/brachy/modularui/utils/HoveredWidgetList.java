package brachy.modularui.utils;

import brachy.modularui.api.layout.IViewportStack;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.screen.viewport.LocatedWidget;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HoveredWidgetList {

    private final List<LocatedWidget> delegate;

    public HoveredWidgetList(List<LocatedWidget> delegate) {
        this.delegate = delegate;
    }

    public void add(IWidget widget, IViewportStack viewports, Object additionalHoverInfo) {
        this.delegate.addFirst(new LocatedWidget(widget, viewports.peek(), additionalHoverInfo));
    }

    @Nullable
    public IWidget peek() {
        return isEmpty() ? null : this.delegate.getFirst().getElement();
    }

    public boolean isEmpty() {
        return this.delegate.isEmpty();
    }

    public int size() {
        return this.delegate.size();
    }
}
