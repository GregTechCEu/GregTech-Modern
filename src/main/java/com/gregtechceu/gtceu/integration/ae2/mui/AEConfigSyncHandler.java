package com.gregtechceu.gtceu.integration.ae2.mui;

import com.gregtechceu.gtceu.api.mui.value.sync.SyncHandler;
import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlot;
import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlotList;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.stacks.GenericStack;

public class AEConfigSyncHandler extends SyncHandler {

    private static final int SYNC_SLOTS = 1;

    private final IConfigurableSlotList slotList;
    private final int slotCount;
    private final GenericStack[] cachedConfig;
    private final GenericStack[] cachedStock;

    @OnlyIn(Dist.CLIENT)
    private GenericStack[] clientConfig;
    @OnlyIn(Dist.CLIENT)
    private GenericStack[] clientStock;

    public AEConfigSyncHandler(IConfigurableSlotList slotList, int slotCount) {
        this.slotList = slotList;
        this.slotCount = slotCount;
        this.cachedConfig = new GenericStack[slotCount];
        this.cachedStock = new GenericStack[slotCount];
    }

    @OnlyIn(Dist.CLIENT)
    public void initClient() {
        this.clientConfig = new GenericStack[slotCount];
        this.clientStock = new GenericStack[slotCount];
    }

    @OnlyIn(Dist.CLIENT)
    public GenericStack getClientConfig(int index) {
        return clientConfig != null ? clientConfig[index] : null;
    }

    @OnlyIn(Dist.CLIENT)
    public GenericStack getClientStock(int index) {
        return clientStock != null ? clientStock[index] : null;
    }

    @Override
    public void detectAndSendChanges(boolean init) {
        int changedCount = 0;
        for (int i = 0; i < slotCount; i++) {
            IConfigurableSlot slot = slotList.getConfigurableSlot(i);
            if (!areEqual(slot.getConfig(), cachedConfig[i]) || !areEqual(slot.getStock(), cachedStock[i])) {
                changedCount++;
            }
        }

        if (!init && changedCount == 0) return;

        syncToClient(SYNC_SLOTS, buf -> {
            for (int i = 0; i < slotCount; i++) {
                IConfigurableSlot slot = slotList.getConfigurableSlot(i);
                GenericStack newConfig = slot.getConfig();
                GenericStack newStock = slot.getStock();
                boolean changed = init || !areEqual(newConfig, cachedConfig[i]) ||
                        !areEqual(newStock, cachedStock[i]);
                buf.writeBoolean(changed);
                if (changed) {
                    writeStack(buf, newConfig);
                    writeStack(buf, newStock);
                    cachedConfig[i] = copy(newConfig);
                    cachedStock[i] = copy(newStock);
                }
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void readOnClient(int id, FriendlyByteBuf buf) {
        if (id == SYNC_SLOTS) {
            if (clientConfig == null) initClient();
            for (int i = 0; i < slotCount; i++) {
                if (buf.readBoolean()) {
                    clientConfig[i] = readStack(buf);
                    clientStock[i] = readStack(buf);
                }
            }
        }
    }

    @Override
    public void readOnServer(int id, FriendlyByteBuf buf) {}

    private static GenericStack copy(GenericStack stack) {
        return stack != null ? new GenericStack(stack.what(), stack.amount()) : null;
    }

    private static void writeStack(FriendlyByteBuf buf, GenericStack stack) {
        buf.writeBoolean(stack != null);
        if (stack != null) GenericStack.writeBuffer(stack, buf);
    }

    private static GenericStack readStack(FriendlyByteBuf buf) {
        return buf.readBoolean() ? GenericStack.readBuffer(buf) : null;
    }

    private static boolean areEqual(GenericStack a, GenericStack b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        return a.amount() == b.amount() && a.what().equals(b.what());
    }
}
