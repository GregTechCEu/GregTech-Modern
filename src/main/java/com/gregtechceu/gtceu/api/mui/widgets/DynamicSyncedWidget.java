package com.gregtechceu.gtceu.api.mui.widgets;

import com.gregtechceu.gtceu.api.mui.base.value.ISyncOrValue;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.value.sync.DynamicSyncHandler;
import com.gregtechceu.gtceu.api.mui.value.sync.SyncHandler;
import com.gregtechceu.gtceu.api.mui.widget.Widget;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * A widget which can update its child based on a function in {@link DynamicSyncHandler}.
 * Such a sync handler must be supplied or else this widget has no effect.
 * The dynamic child can be a widget tree of any size which can also contain {@link SyncHandler}s. These sync handlers
 * MUST be registered
 * via a variant of
 * {@link com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager#getOrCreateSyncHandler(String, Class, Supplier)}
 * 
 * @param <W> type of this widget
 */
public class DynamicSyncedWidget<W extends DynamicSyncedWidget<W>> extends Widget<W> {

    private DynamicSyncHandler syncHandler;
    private IWidget child;

    @Override
    public boolean isValidSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        return syncOrValue.isTypeOrEmpty(DynamicSyncHandler.class);
    }

    @Override
    protected void setSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        super.setSyncOrValue(syncOrValue);
        this.syncHandler = syncOrValue.castNullable(DynamicSyncHandler.class);
        if (this.syncHandler != null) this.syncHandler.attachDynamicWidgetListener(this::updateChild);
    }

    @Override
    public @NotNull List<IWidget> getChildren() {
        if (this.child == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(this.child);
        }
    }

    private void updateChild(IWidget widget) {
        if (this.child != null) {
            this.child.dispose();
        } else if (widget == null) {
            return;
        }
        this.child = widget;
        if (isValid()) {
            if (this.child != null) this.child.initialise(this, true);
            scheduleResize();
        }
    }

    public W syncHandler(DynamicSyncHandler syncHandler) {
        setSyncOrValue(ISyncOrValue.orEmpty(syncHandler));
        return getThis();
    }

    /**
     * Sets an initial child. This can only be done before the widget is initialised.
     *
     * @param child initial child
     * @return this
     */
    public W initialChild(IWidget child) {
        if (isValid()) throw new IllegalStateException("Can only set initial child before the widget is initialised.");
        this.child = child;
        return getThis();
    }
}
