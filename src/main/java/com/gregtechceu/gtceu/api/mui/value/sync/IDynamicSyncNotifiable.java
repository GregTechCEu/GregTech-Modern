package com.gregtechceu.gtceu.api.mui.value.sync;

import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;

import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;

public interface IDynamicSyncNotifiable {

    /**
     * An internal function which is used to link the {@link com.gregtechceu.gtceu.api.mui.widgets.DynamicSyncedWidget}.
     */
    @ApiStatus.Internal
    void attachDynamicWidgetListener(Consumer<IWidget> onWidgetUpdate);
}
