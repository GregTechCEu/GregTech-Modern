package com.gregtechceu.gtceu.api.mui.value.sync;

import com.gregtechceu.gtceu.api.mui.base.value.sync.IValueSyncHandler;

import net.minecraft.network.FriendlyByteBuf;

import lombok.Getter;
import lombok.Setter;

public abstract class ValueSyncHandler<T> extends SyncHandler implements IValueSyncHandler<T> {

    @Getter
    @Setter
    private Runnable changeListener;

    @Override
    public void readOnClient(int id, FriendlyByteBuf buf) {
        read(buf);
        onValueChanged();
    }

    @Override
    public void readOnServer(int id, FriendlyByteBuf buf) {
        read(buf);
        onValueChanged();
    }

    @Override
    public void detectAndSendChanges(boolean init) {
        if (updateCacheFromSource(init)) {
            syncToClient(0, this::write);
        }
    }

    protected void onValueChanged() {
        if (this.changeListener != null) {
            this.changeListener.run();
        }
    }
}
