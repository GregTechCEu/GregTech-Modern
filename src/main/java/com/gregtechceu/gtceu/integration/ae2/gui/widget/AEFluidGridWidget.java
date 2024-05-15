package com.gregtechceu.gtceu.integration.ae2.gui.widget;

import com.gregtechceu.gtceu.integration.ae2.util.ExportOnlyAESlot;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.fluids.FluidStack;

import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.GenericStack;

/**
 * @Author GlodBlock
 * @Description Display fluid list
 * @Date 2023/4/19-0:28
 */
public class AEFluidGridWidget extends AEListGridWidget {

    public AEFluidGridWidget(int x, int y, int slotsY, GenericInternalInventory internalList) {
        super(x, y, slotsY, internalList);
    }

    @Override
    protected void addSlotRows(int amount) {
        for (int i = 0; i < amount; i++) {
            int widgetAmount = this.widgets.size();
            Widget widget = new AEFluidDisplayWidget(0, 0, this, widgetAmount);
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
            for (int j = 0; j < this.list.size(); ++j) {
                GenericStack item2 = this.list.getStack(j);
                if (item != null && (item.what().matches(item2) && item2.amount() == 0)) {
                    this.changeMap.put(ExportOnlyAESlot.copy(item), -item.amount());
                    this.cached.setStack(i, new GenericStack(item.what(), 0));
                    matched = true;
                    break;
                }
            }
            if (!matched && item != null) {
                this.changeMap.put(ExportOnlyAESlot.copy(item), -item.amount());
                this.cached.setStack(i, new GenericStack(item.what(), 0));
            }
        }
        // Change/Add item
        for (int i = 0; i < this.list.size(); ++i) {
            GenericStack item = this.list.getStack(i);
            for (int j = 0; j < this.cached.size(); ++j) {
                GenericStack cachedItem = this.cached.getStack(j);
                if (item != null && (item.what().matches(cachedItem) && cachedItem.amount() == 0)) {
                    this.changeMap.put(ExportOnlyAESlot.copy(item), item.amount());
                    this.cached.insert(j, item.what(), item.amount(), Actionable.MODULATE);
                    break;
                } else {
                    if (cachedItem != null && item != null && cachedItem.amount() != item.amount()) {
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
                if (item.what() instanceof AEFluidKey key) {
                    // TODO fix nbt once AE2 1.20.5 is out
                    FluidStack stack = new FluidStack(key.getFluid(), (int) item.amount()/* , key.getTag() */);
                    FluidStack.OPTIONAL_STREAM_CODEC.encode(buf, stack);
                    buf.writeVarLong(this.changeMap.getLong(item));
                }
            }
        });
    }

    @Override
    protected void readListChange(RegistryFriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        for (int i = 0; i < size; i++) {
            FluidStack fluid = FluidStack.OPTIONAL_STREAM_CODEC.decode(buffer);
            long delta = buffer.readVarLong();
            if (fluid != null) {
                // TODO fix nbt once AE2 1.20.5 is out
                GenericStack stack = new GenericStack(AEFluidKey.of(fluid.getFluid()/* , fluid.getTag() */), delta);
                this.displayList.insert(i, stack.what(), delta, Actionable.MODULATE);
            }
        }
    }
}
