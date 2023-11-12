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

    protected final Object2LongMap<GenericStack> changeMap = new Object2LongOpenHashMap<>();
    protected final GenericInternalInventory cached = new GenericStackInv(() -> {}, 16);
    protected final GenericInternalInventory displayList = new GenericStackInv(() -> {}, 16);

    public AEListGridWidget(int x, int y, int slotsY, GenericInternalInventory internalList) {
        super(x, y, 18 + 140, slotsY * 18);
        this.list = internalList;
        this.slotAmountY = slotsY;
    }

    public GenericStack getAt(int index) {
        return displayList.getStack(index);
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
        int amountOfTypes = 0;
        for (int i = 0; i < this.list.size(); ++i) {
            if (this.list.getStack(i) != null) ++amountOfTypes;
        }
        int slotRowsRequired = Math.max(this.slotAmountY, amountOfTypes);
        if (this.slotRowsAmount != slotRowsRequired) {
            int slotsToAdd = slotRowsRequired - this.slotRowsAmount;
            this.slotRowsAmount = slotRowsRequired;
            this.writeUpdateInfo(ROW_CHANGE_ID, buf -> buf.writeVarInt(slotsToAdd));
            this.modifySlotRows(slotsToAdd);
        }
        this.writeListChange();
    }

    protected abstract void writeListChange();

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

    protected abstract void readListChange(FriendlyByteBuf buffer);

    @Override
    public void writeInitialData(FriendlyByteBuf buffer) {
        super.writeInitialData(buffer);
        if (this.list == null) return;
        int amountOfTypes = 0;
        for (int i = 0; i < this.list.size(); ++i) {
            if (this.list.getStack(i) != null) ++amountOfTypes;
        }
        int slotRowsRequired = Math.max(this.slotAmountY, amountOfTypes);
        int slotsToAdd = slotRowsRequired - this.slotRowsAmount;
        this.slotRowsAmount = slotRowsRequired;
        this.modifySlotRows(slotsToAdd);
        buffer.writeVarInt(slotsToAdd);
        this.writeListChange();
    }

    @Override
    public void readInitialData(FriendlyByteBuf buffer) {
        super.readInitialData(buffer);
        if (this.list == null) return;
        this.modifySlotRows(buffer.readVarInt());
    }
}