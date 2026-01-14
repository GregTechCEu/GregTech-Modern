package com.gregtechceu.gtceu.api.mui.value.sync;

import com.gregtechceu.gtceu.api.mui.base.value.sync.IValueSyncHandler;

import net.minecraft.network.FriendlyByteBuf;

import lombok.Getter;
import lombok.Setter;

public abstract class ValueSyncHandler<T> extends SyncHandler implements IValueSyncHandler<T> {

    public static final int SYNC_VALUE = 0;

    @Getter
    @Setter
    private Runnable changeListener;

    @Override
    public void readOnClient(int id, FriendlyByteBuf buf) {
        if (id == SYNC_VALUE) read(buf);
    }

    @Override
    public void readOnServer(int id, FriendlyByteBuf buf) {
        if (id == SYNC_VALUE) read(buf);
    }

    protected void sync() {
        sync(SYNC_VALUE, this::write);
    }

    @Override
    public void detectAndSendChanges(boolean init) {
        if (updateCacheFromSource(init)) sync();
    }

    /**
     * Called when the cached value of this sync handler updates. Implementations need to call this inside
     * {@link #setValue(Object, boolean, boolean)}.
     */
    protected void onValueChanged() {
        if (this.changeListener != null) {
            this.changeListener.run();
        }
    }
}
