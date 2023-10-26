package com.gregtechceu.gtceu.integration.ae2.gui.widget;

import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.config.Actionable;
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

    private final Object2LongMap<GenericStack> changeMap = new Object2LongOpenHashMap<>();
    protected final GenericInternalInventory cached = new GenericStackInv(() -> {}, 16);
    protected final GenericInternalInventory displayList = new GenericStackInv(() -> {}, 16);

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

