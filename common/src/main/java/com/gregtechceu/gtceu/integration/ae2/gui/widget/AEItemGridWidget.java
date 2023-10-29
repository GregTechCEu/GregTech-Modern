package com.gregtechceu.gtceu.integration.ae2.gui.widget;

import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.helpers.externalstorage.GenericStackInv;
import com.gregtechceu.gtceu.integration.ae2.util.ExportOnlyAESlot;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;

/**
 * @Author GlodBlock
 * @Description Display item list
 * @Date 2023/4/19-21:33
 */
public class AEItemGridWidget extends AEListGridWidget {

    public AEItemGridWidget(int x, int y, int slotsY, GenericInternalInventory internalList) {
        super(x, y, slotsY, internalList);
    }

    @Override
    protected void addSlotRows(int amount) {
        for (int i = 0; i < amount; i++) {
            int widgetAmount = this.widgets.size();
            Widget widget = new AEItemDisplayWidget(0, 0, this, widgetAmount);
            this.addWidget(widget);
        }
    }

    @Override
    protected void writeListChange() {
        this.changeMap.clear();
        // Remove item
        for (int i = 0; i < this.cached.size(); ++i) {
            GenericStack item = this.cached.getStack(i);
            boolean matched = false;
            if (item == null) continue;
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
            if (item == null) continue;
            boolean matched = false;
            for (int j = 0; j < this.cached.size(); ++j) {
                GenericStack cachedItem = this.cached.getStack(j);
                if (item.what().matches(cachedItem) && cachedItem.amount() != item.amount()) {
                    this.changeMap.put(ExportOnlyAESlot.copy(item), item.amount() - cachedItem.amount());
                    this.cached.insert(j, item.what(), item.amount() - cachedItem.amount(), Actionable.MODULATE);
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                this.changeMap.put(ExportOnlyAESlot.copy(item), item.amount());
                this.cached.insert(i, item.what(), item.amount(), Actionable.MODULATE);
            }
        }
        this.writeUpdateInfo(CONTENT_CHANGE_ID, buf -> {
            buf.writeVarInt(this.changeMap.size());
            for (GenericStack item : this.changeMap.keySet()) {
                if (item.what() instanceof AEItemKey key) {
                    ItemStack stack = new ItemStack(key.getItem(), (int) item.amount());
                    if (key.hasTag()) {
                        stack.setTag(key.getTag().copy());
                    }
                    buf.writeItem(stack);
                    buf.writeVarLong(this.changeMap.getLong(item));
                }
            }
        });
    }

    @Override
    protected void readListChange(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        for (int i = 0; i < size; i++) {
            ItemStack item = buffer.readItem();
            item.setCount(1);
            long delta = buffer.readVarLong();
            if (!item.isEmpty()) {
                GenericStack stack = new GenericStack(AEItemKey.of(item.getItem(), item.getTag()), delta);
                this.displayList.setStack(i, stack);
            }
        }
    }
}

