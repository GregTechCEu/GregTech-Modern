package com.gregtechceu.gtceu.integration.ae2.gui;

import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlot;
import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlotList;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import appeng.api.stacks.GenericStack;
import brachy.modularui.value.sync.SyncHandler;
import org.jetbrains.annotations.Nullable;

public class AEConfigSyncHandler extends SyncHandler<AEConfigSyncHandler> {

    private static final int SYNC_SLOTS = 1;

    private final IConfigurableSlotList slotList;
    private final int slotCount;
    private final GenericStack[] cachedConfig;
    private final GenericStack[] cachedStock;

    @OnlyIn(Dist.CLIENT)
    private @Nullable GenericStack @Nullable [] clientConfig;
    @OnlyIn(Dist.CLIENT)
    private @Nullable GenericStack @Nullable [] clientStock;

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
    public @Nullable GenericStack getClientConfig(int index) {
        return clientConfig != null ? clientConfig[index] : null;
    }

    @OnlyIn(Dist.CLIENT)
    public @Nullable GenericStack getClientStock(int index) {
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
            if (clientStock == null) initClient();
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

    private static @Nullable GenericStack copy(@Nullable GenericStack stack) {
        return stack != null ? new GenericStack(stack.what(), stack.amount()) : null;
    }

    private static void writeStack(FriendlyByteBuf buf, @Nullable GenericStack stack) {
        buf.writeBoolean(stack != null);
        if (stack != null) GenericStack.writeBuffer(stack, buf);
    }

    private static @Nullable GenericStack readStack(FriendlyByteBuf buf) {
        return buf.readBoolean() ? GenericStack.readBuffer(buf) : null;
    }

    private static boolean areEqual(@Nullable GenericStack a, @Nullable GenericStack b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        return a.amount() == b.amount() && a.what().equals(b.what());
    }
}
