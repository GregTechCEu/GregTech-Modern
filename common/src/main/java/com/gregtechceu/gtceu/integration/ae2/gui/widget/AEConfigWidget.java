package com.gregtechceu.gtceu.integration.ae2.gui.widget;

import appeng.api.stacks.GenericStack;
import com.gregtechceu.gtceu.integration.ae2.util.AEConfigSlot;
import com.gregtechceu.gtceu.integration.ae2.util.AmountSetSlot;
import com.gregtechceu.gtceu.integration.ae2.util.IConfigurableSlot;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;

public abstract class AEConfigWidget extends WidgetGroup {

    protected final IConfigurableSlot[] config;
    protected IConfigurableSlot[] cached;
    protected Int2ObjectMap<IConfigurableSlot> changeMap = new Int2ObjectOpenHashMap<>();
    protected IConfigurableSlot[] displayList;
    protected AmountSetSlot amountSetWidget;
    protected final static int UPDATE_ID = 1000;

    public AEConfigWidget(int x, int y, IConfigurableSlot[] config) {
        super(new Position(x, y), new Size(config.length / 2 * 18, 18 * 4 + 2));
        this.config = config;
        this.init();
        this.amountSetWidget = new AmountSetSlot(80, -40, this);
        this.addWidget(this.amountSetWidget);
        this.addWidget(this.amountSetWidget.getAmountText());
        this.amountSetWidget.setVisible(false);
        this.amountSetWidget.getAmountText().setVisible(false);
    }

    public void enableAmount(int slotIndex) {
        this.amountSetWidget.setSlotIndex(slotIndex);
        this.amountSetWidget.setVisible(true);
        this.amountSetWidget.getAmountText().setVisible(true);
    }

    public void disableAmount() {
        this.amountSetWidget.setSlotIndex(-1);
        this.amountSetWidget.setVisible(false);
        this.amountSetWidget.getAmountText().setVisible(false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.amountSetWidget.isVisible()) {
            if (this.amountSetWidget.getAmountText().mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        for (Widget w : this.widgets) {
            if (w instanceof AEConfigSlot slot) {
                slot.setSelect(false);
            }
        }
        this.disableAmount();
        return super.mouseClicked(mouseX, mouseY, button);
    }

    abstract void init();

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        this.changeMap.clear();
        for (int index = 0; index < this.config.length; index ++) {
            IConfigurableSlot newSlot = this.config[index];
            IConfigurableSlot oldSlot = this.cached[index];
            GenericStack nConfig = newSlot.getConfig();
            GenericStack nStock = newSlot.getStock();
            GenericStack oConfig = oldSlot.getConfig();
            GenericStack oStock = oldSlot.getStock();
            if (!areAEStackCountsEqual(nConfig, oConfig) || !areAEStackCountsEqual(nStock, oStock)) {
                this.changeMap.put(index, newSlot.copy());
                this.cached[index] = this.config[index].copy();
                this.gui.holder.markAsDirty();
            }
        }
        if (!this.changeMap.isEmpty()) {
            this.writeUpdateInfo(UPDATE_ID, buf -> {
                buf.writeVarInt(this.changeMap.size());
                for (int index : this.changeMap.keySet()) {
                    GenericStack sConfig = this.changeMap.get(index).getConfig();
                    GenericStack sStock = this.changeMap.get(index).getStock();
                    buf.writeVarInt(index);
                    if (sConfig != null) {
                        buf.writeBoolean(true);
                        GenericStack.writeBuffer(sConfig, buf);
                    } else {
                        buf.writeBoolean(false);
                    }
                    if (sStock != null) {
                        buf.writeBoolean(true);
                        GenericStack.writeBuffer(sStock, buf);
                    } else {
                        buf.writeBoolean(false);
                    }
                }
            });
        }
    }

    @Override
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == UPDATE_ID) {
            int size = buffer.readVarInt();
            for (int i = 0; i < size; i ++) {
                int index = buffer.readVarInt();
                IConfigurableSlot slot = this.displayList[index];
                if (buffer.readBoolean()) {
                    slot.setConfig(GenericStack.readBuffer(buffer));
                } else {
                    slot.setConfig(null);
                }
                if (buffer.readBoolean()) {
                    slot.setStock(GenericStack.readBuffer(buffer));
                } else {
                    slot.setStock(null);
                }
            }
        }
    }

    public final IConfigurableSlot getConfig(int index) {
        return this.config[index];
    }

    public final IConfigurableSlot getDisplay(int index) {
        return this.displayList[index];
    }

    protected final boolean areAEStackCountsEqual(GenericStack s1, GenericStack s2) {
        if (s2 == s1) {
            return true;
        }
        if (s1 != null && s2 != null) {
            return s1.amount() == s2.amount() && s1.what().matches(s2);
        }
        return false;
    }

}