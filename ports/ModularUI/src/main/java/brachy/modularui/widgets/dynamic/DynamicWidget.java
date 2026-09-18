package brachy.modularui.widgets.dynamic;

import brachy.modularui.api.value.ISyncOrValue;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.utils.MutableSingletonList;
import brachy.modularui.value.sync.DynamicLinkedSyncHandler;
import brachy.modularui.value.sync.DynamicSyncHandler;
import brachy.modularui.value.sync.SyncHandler;
import brachy.modularui.widget.Widget;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

/**
 * A widget which can update its child based on a function in {@link DynamicSyncHandler}.
 * Such a sync handler must be supplied or else this widget has no effect.
 * The dynamic child can be a widget tree of any size which can also contain {@link SyncHandler}s. These sync handlers MUST be registered
 * via a variant of {@link brachy.modularui.value.sync.PanelSyncManager#getOrCreateSyncHandler(String, Class, Supplier)}.
 *
 * @param <W> type of this widget
 */
public class DynamicWidget<W extends DynamicWidget<W>> extends Widget<W> {

    private IDynamicHandler dynamicHandler;
    private final MutableSingletonList<IWidget> child = new MutableSingletonList<>();

    @Override
    public void onInit() {
        if (this.child.isEmpty() && this.dynamicHandler instanceof DynamicHandler dynamicHandler1) {
            dynamicHandler1.notifyUpdate();
        }
    }

    @Override
    public boolean isValidSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        return syncOrValue.isTypeOrEmpty(IDynamicHandler.class);
    }

    @Override
    protected void setSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        super.setSyncOrValue(syncOrValue);
        this.dynamicHandler = syncOrValue.castNullable(IDynamicHandler.class);
        if (this.dynamicHandler != null) {
            this.dynamicHandler.attachDynamicWidgetListener(this::updateChild);
        }
    }

    @Override
    public @NotNull List<IWidget> getChildren() {
        return this.child;
    }

    private void updateChild(IWidget widget) {
        if (!this.child.isEmpty()) {
            this.child.get().dispose();
        }
        if (widget == null) {
            this.child.remove();
            if (isValid()) scheduleResize();
            return;
        }
        this.child.set(widget);
        if (isValid()) {
            widget.initialise(this, true);
            scheduleResize();
        }
    }

    public W syncHandler(DynamicSyncHandler syncHandler) {
        setSyncOrValue(ISyncOrValue.orEmpty(syncHandler));
        return getThis();
    }

    public W syncHandler(DynamicLinkedSyncHandler<?, ?> syncHandler) {
        setSyncOrValue(ISyncOrValue.orEmpty(syncHandler));
        return getThis();
    }

    public W clientOnlyHandler(DynamicHandler dynamicHandler) {
        dynamicHandler.attachDynamicWidgetListener(this::updateChild);
        this.dynamicHandler = dynamicHandler;
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
        if (child != null) this.child.set(child);
        return getThis();
    }
}
