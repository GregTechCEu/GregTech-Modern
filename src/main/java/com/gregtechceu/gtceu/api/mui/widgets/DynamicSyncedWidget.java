package com.gregtechceu.gtceu.api.mui.widgets;

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
 * via {@link PanelSyncManager#getOrCreateSyncHandler(String, Class, Supplier)}
 * 
 * @param <W>
 */
public class DynamicSyncedWidget<W extends DynamicSyncedWidget<W>> extends Widget<W> {

    private DynamicSyncHandler syncHandler;
    private IWidget child;

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        if (syncHandler instanceof DynamicSyncHandler dynamicSyncHandler) {
            this.syncHandler = dynamicSyncHandler;
            dynamicSyncHandler.onWidgetUpdate(this::updateChild);
            return true;
        }
        return false;
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
        this.child = widget;
        if (isValid()) {
            this.child.initialise(this, true);
            scheduleResize();
        }
    }

    public W syncHandler(DynamicSyncHandler syncHandler) {
        this.syncHandler = syncHandler;
        setSyncHandler(syncHandler);
        syncHandler.onWidgetUpdate(this::updateChild);
        return getThis();
    }
}
