package com.gregtechceu.gtceu.api.mui.value.sync;

import net.minecraft.network.FriendlyByteBuf;

public class CursorSlotSyncHandler extends SyncHandler {

    public void sync() {
        sync(0, buffer -> buffer.writeItem(getSyncManager().getPlayer().containerMenu.getCarried()));
    }

    @Override
    public void readOnClient(int id, FriendlyByteBuf buf) {
        getSyncManager().getPlayer().containerMenu.setCarried(buf.readItem());
    }

    @Override
    public void readOnServer(int id, FriendlyByteBuf buf) {
        getSyncManager().getPlayer().containerMenu.setCarried(buf.readItem());
    }
}
