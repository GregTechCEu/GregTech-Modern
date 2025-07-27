package com.gregtechceu.gtceu.integration.ae2.gui.widget.list;

import com.gregtechceu.gtceu.integration.ae2.utils.KeyStorage;

import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

import java.util.ArrayList;
import java.util.List;

/**
 * A display only widget for {@link KeyStorage}
 */
public abstract class AEListGridWidget extends DraggableScrollableWidgetGroup {

    protected final KeyStorage list;
    private final int slotAmountY;
    private int slotRowsAmount;
    protected final static int ROW_CHANGE_ID = 2;
    protected final static int CONTENT_CHANGE_ID = 3;

    protected final Object2LongMap<AEKey> changeMap = new Object2LongOpenHashMap<>();
    protected final KeyStorage cached = new KeyStorage();
    protected final List<GenericStack> displayList = new ArrayList<>();

    public AEListGridWidget(int x, int y, int slotsY, KeyStorage internalList) {
        super(x, y, 18 + 140, slotsY * 18);
        this.list = internalList;
        this.slotAmountY = slotsY;
    }

    public GenericStack getAt(int index) {
        return index >= 0 && index < displayList.size() ? displayList.get(index) : null;
    }

    private void addSlotRows(int amount) {
        for (int i = 0; i < amount; i++) {
            int widgetAmount = this.widgets.size();
            Widget widget = createDisplayWidget(0, i * 18, widgetAmount);
            this.addWidget(widget);
        }
    }

    private void removeSlotRows(int amount) {
        for (int i = 0; i < amount; i++) {
            Widget slotWidget = this.widgets.remove(this.widgets.size() - 1);
            removeWidget(slotWidget);
        }
    }

    private void modifySlotRows(int delta) {
        if (delta > 0) {
            addSlotRows(delta);
        } else {
            removeSlotRows(delta);
        }
    }

    protected void writeListChange(FriendlyByteBuf buffer) {
        this.changeMap.clear();

        // Remove
        var cachedIt = cached.storage.object2LongEntrySet().iterator();
        while (cachedIt.hasNext()) {
            var entry = cachedIt.next();
            var cachedKey = entry.getKey();
            if (!list.storage.containsKey(cachedKey)) {
                this.changeMap.put(cachedKey, -entry.getLongValue());
                cachedIt.remove();
            }
        }

        // Change/Add
        for (var entry : list.storage.object2LongEntrySet()) {
            var key = entry.getKey();
            long value = entry.getLongValue();
            long cacheValue = cached.storage.getOrDefault(key, 0);
            if (cacheValue == 0) {
                // Add
                this.changeMap.put(key, value);
                this.cached.storage.put(key, value);
            } else {
                // Change
                if (cacheValue != value) {
                    this.changeMap.put(key, value - cacheValue);
                    this.cached.storage.put(key, value);
                }
            }
        }

        buffer.writeVarInt(this.changeMap.size());
        for (var entry : this.changeMap.object2LongEntrySet()) {
            entry.getKey().writeToPacket(buffer);
            buffer.writeVarLong(entry.getLongValue());
        }
    }

    protected void readListChange(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        for (int i = 0; i < size; i++) {
            var key = fromPacket(buffer);
            long delta = buffer.readVarLong();

            boolean found = false;
            var li = displayList.listIterator();
            while (li.hasNext()) {
                var stack = li.next();
                if (stack.what().equals(key)) {
                    long newAmount = stack.amount() + delta;
                    if (newAmount > 0) {
                        li.set(new GenericStack(key, newAmount));
                    } else {
                        li.remove();
                    }
                    found = true;
                    break;
                }
            }
            if (!found) {
                displayList.add(new GenericStack(key, delta));
            }
        }
    }

    protected abstract void toPacket(FriendlyByteBuf buffer, AEKey key);

    protected abstract AEKey fromPacket(FriendlyByteBuf buffer);

    protected abstract Widget createDisplayWidget(int x, int y, int index);

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (this.list == null) return;
        int slotRowsRequired = Math.max(this.slotAmountY, list.storage.size());
        if (this.slotRowsAmount != slotRowsRequired) {
            int slotsToAdd = slotRowsRequired - this.slotRowsAmount;
            this.slotRowsAmount = slotRowsRequired;
            this.writeUpdateInfo(ROW_CHANGE_ID, buf -> buf.writeVarInt(slotsToAdd));
            this.modifySlotRows(slotsToAdd);
        }
        this.writeUpdateInfo(CONTENT_CHANGE_ID, this::writeListChange);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == ROW_CHANGE_ID) {
            int slotsToAdd = buffer.readVarInt();
            this.modifySlotRows(slotsToAdd);
        }
        if (id == CONTENT_CHANGE_ID) {
            this.readListChange(buffer);
        }
    }

    @Override
    public void writeInitialData(FriendlyByteBuf buffer) {
        super.writeInitialData(buffer);
        if (this.list == null) return;
        int slotRowsRequired = Math.max(this.slotAmountY, list.storage.size());
        int slotsToAdd = slotRowsRequired - this.slotRowsAmount;
        this.slotRowsAmount = slotRowsRequired;
        this.modifySlotRows(slotsToAdd);
        buffer.writeVarInt(slotsToAdd);
        this.writeListChange(buffer);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void readInitialData(FriendlyByteBuf buffer) {
        super.readInitialData(buffer);
        if (this.list == null) return;
        this.modifySlotRows(buffer.readVarInt());
        this.readListChange(buffer);
    }

    public static class Item extends AEListGridWidget {

        public Item(int x, int y, int slotsY, KeyStorage internalList) {
            super(x, y, slotsY, internalList);
        }

        @Override
        protected void toPacket(FriendlyByteBuf buffer, AEKey key) {
            key.writeToPacket(buffer);
        }

        @Override
        protected AEKey fromPacket(FriendlyByteBuf buffer) {
            return AEItemKey.fromPacket(buffer);
        }

        @Override
        protected Widget createDisplayWidget(int x, int y, int index) {
            return new AEItemDisplayWidget(x, y, this, index);
        }
    }

    public static class Fluid extends AEListGridWidget {

        public Fluid(int x, int y, int slotsY, KeyStorage internalList) {
            super(x, y, slotsY, internalList);
        }

        @Override
        protected void toPacket(FriendlyByteBuf buffer, AEKey key) {
            key.writeToPacket(buffer);
        }

        @Override
        protected AEKey fromPacket(FriendlyByteBuf buffer) {
            return AEFluidKey.fromPacket(buffer);
        }

        @Override
        protected Widget createDisplayWidget(int x, int y, int index) {
            return new AEFluidDisplayWidget(x, y, this, index);
        }
    }
}
