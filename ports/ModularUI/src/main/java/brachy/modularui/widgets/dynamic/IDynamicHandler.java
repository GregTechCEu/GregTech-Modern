package brachy.modularui.widgets.dynamic;

import brachy.modularui.api.widget.IWidget;

import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;

public interface IDynamicHandler {

    /**
     * An internal function which is used to link the {@link DynamicWidget}.
     */
    @ApiStatus.Internal
    void attachDynamicWidgetListener(Consumer<IWidget> onWidgetUpdate);
}
