package brachy.modularui.widgets.dynamic;

import brachy.modularui.api.widget.IWidget;

import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class DynamicHandler implements IDynamicHandler {

    private Supplier<IWidget> widgetProvider;
    private Consumer<IWidget> onWidgetUpdate;

    private IWidget lastRejectedWidget;

    @ApiStatus.Internal
    @Override
    public void attachDynamicWidgetListener(Consumer<IWidget> onWidgetUpdate) {
        if (this.onWidgetUpdate == null && onWidgetUpdate != null && this.lastRejectedWidget != null) {
            onWidgetUpdate.accept(this.lastRejectedWidget);
            this.lastRejectedWidget = null;
        }
        this.onWidgetUpdate = onWidgetUpdate;
    }

    public void notifyUpdate() {
        if (this.widgetProvider == null) return;
        IWidget widget = this.widgetProvider.get();
        if (this.onWidgetUpdate == null) {
            this.lastRejectedWidget = widget;
        } else {
            this.onWidgetUpdate.accept(widget);
        }
    }

    /**
     * @param widgetProvider the widget creator function
     * @return this
     */
    public DynamicHandler widgetProvider(Supplier<IWidget> widgetProvider) {
        this.widgetProvider = widgetProvider;
        return this;
    }
}

