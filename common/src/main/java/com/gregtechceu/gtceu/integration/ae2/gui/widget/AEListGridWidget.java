package com.gregtechceu.gtceu.integration.ae2.gui.widget;

import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.config.Actionable;
import appeng.api.stacks.GenericStack;
import appeng.helpers.externalstorage.GenericStackInv;
import com.gregtechceu.gtceu.integration.ae2.util.ExportOnlyAESlot;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

/**
 * @Author GlodBlock
 * @Description A display only widget for {@link GenericInternalInventory}
 * @Date 2023/4/19-0:18
 */
public abstract class AEListGridWidget extends DraggableScrollableWidgetGroup {

    protected final GenericInternalInventory list;
    private final int slotAmountY;
    private int slotRowsAmount = 0;
    protected final static int ROW_CHANGE_ID = 2;
    protected final static int CONTENT_CHANGE_ID = 3;

    private final Object2LongMap<GenericStack> changeMap = new Object2LongOpenHashMap<>();
    protected final GenericInternalInventory cached = new GenericStackInv(() -> {}, 16);
    protected final GenericInternalInventory displayList = new GenericStackInv(() -> {}, 16);

    public AEListGridWidget(int x, int y, int slotsY, GenericInternalInventory internalList) {
        super(x, y, 18 + 140, slotsY * 18);
        this.list = internalList;
        this.slotAmountY = slotsY;
    }

    public GenericStack getAt(int index) {
        return list.getStack(index);
    }

    protected abstract void addSlotRows(int amount);

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

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (this.list == null) return;
        int amountOfTypes = this.list.size();
        int slotRowsRequired = Math.max(this.slotAmountY, amountOfTypes);
        if (this.slotRowsAmount != slotRowsRequired) {
            int slotsToAdd = slotRowsRequired - this.slotRowsAmount;
            this.slotRowsAmount = slotRowsRequired;
            this.writeUpdateInfo(ROW_CHANGE_ID, buf -> buf.writeVarInt(slotsToAdd));
            this.modifySlotRows(slotsToAdd);
        }
        this.writeListChange();
    }

    protected void writeListChange() {
        this.changeMap.clear();
        // Remove item
        for (int i = 0; i < this.cached.size(); ++i) {
            GenericStack item = this.cached.getStack(i);
            boolean matched = false;
            for (int j = 0; j < this.list.size(); ++j) {
                GenericStack item2 = this.list.getStack(j);
                if (item.what().matches(item2) && item2.amount() == 0) {
                    this.changeMap.put(ExportOnlyAESlot.copy(item), -item.amount());
                    this.cached.setStack(i, new GenericStack(item.what(), 0));
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                this.changeMap.put(ExportOnlyAESlot.copy(item), -item.amount());
                this.cached.setStack(i, new GenericStack(item.what(), 0));
            }
        }
        // Change/Add item
        for (int i = 0; i < this.list.size(); ++i) {
            GenericStack item = this.list.getStack(i);
            for (int j = 0; j < this.cached.size(); ++j) {
                GenericStack cachedItem = this.cached.getStack(j);
                if (item.what().matches(cachedItem) && cachedItem.amount() == 0) {
                    this.changeMap.put(ExportOnlyAESlot.copy(item), item.amount());
                    this.cached.insert(j, item.what(), item.amount(), Actionable.MODULATE);
                    break;
                } else {
                    if (cachedItem.amount() != item.amount()) {
                        this.changeMap.put(ExportOnlyAESlot.copy(item), item.amount() - cachedItem.amount());
                        this.cached.insert(j, item.what(), item.amount() - cachedItem.amount(), Actionable.MODULATE);
                        break;
                    }
                }
            }
        }
        this.writeUpdateInfo(CONTENT_CHANGE_ID, buf -> {
            buf.writeVarInt(this.changeMap.size());
            for (GenericStack item : this.changeMap.keySet()) {
                buf.writeItem(GenericStack.wrapInItemStack(item));
                buf.writeVarLong(this.changeMap.getLong(item));
            }
        });
    }
    @Override
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

    protected void readListChange(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        for (int i = 0; i < size; i++) {
            ItemStack item = buffer.readItem();
            item.setCount(1);
            long delta = buffer.readVarLong();
            if (!item.isEmpty()) {
                GenericStack stack = GenericStack.fromItemStack(item);
                this.displayList.insert(i, stack.what(), delta, Actionable.MODULATE);
            }
        }
    }
}