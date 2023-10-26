package com.gregtechceu.gtceu.integration.ae2.gui.widget;

import appeng.api.stacks.GenericStack;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.LongInputWidget;
import com.gregtechceu.gtceu.integration.ae2.util.IConfigurableSlot;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.utils.Position;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import static com.lowdragmc.lowdraglib.gui.util.DrawerHelper.drawStringSized;

/**
 * @Author GlodBlock
 * @Description The amount set widget for config slot
 * @Date 2023/4/21-21:20
 */
public class AmountSetSlot extends Widget {

    private int index = -1;
    @Getter
    private final LongInputWidget amountText;
    private final AEConfigWidget parentWidget;

    public AmountSetSlot(int x, int y, AEConfigWidget widget) {
        super(x, y, 80, 30);
        this.parentWidget = widget;
        this.amountText = new LongInputWidget(x + 3, y + 14, 60, 15, this::getAmount, this::setNewAmount) {
            protected void buildUI() {
                int buttonWidth = Mth.clamp(this.getSize().width / 5, 15, 40);
                int textFieldWidth = this.getSize().width - (2 * buttonWidth) - 4;
                this.textField = new TextFieldWidget(buttonWidth + 2, 0, textFieldWidth, 20,
                        () -> toText(getValueSupplier().get()),
                        stringValue -> this.setValue(clamp(fromText(stringValue), getMin(), getMax()))
                );
                this.updateTextFieldRange();
                this.addWidget(this.textField);
            }
        };
    }

    public void setSlotIndex(int slotIndex) {
        this.index = slotIndex;
        writeClientAction(0, buf -> buf.writeVarInt(this.index));
    }

    public long getAmount() {
        if (this.index < 0) {
            return 0;
        }
        IConfigurableSlot slot = this.parentWidget.getConfig(this.index);
        if (slot.getConfig() != null) {
            return slot.getConfig().amount();
        }
        return 0;
    }

    public void setNewAmount(long amount) {
        writeClientAction(1, buf -> buf.writeVarLong(amount));
    }

    @Override
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        super.handleClientAction(id, buffer);
        if (id == 0) {
            this.index = buffer.readVarInt();
        } else if (id == 1) {
            if (this.index < 0) {
                return;
            }
            IConfigurableSlot slot = this.parentWidget.getConfig(this.index);
            long newAmt = buffer.readVarLong();
            if (newAmt > 0 && slot.getConfig() != null) {
                slot.setConfig(new GenericStack(slot.getConfig().what(), newAmt));
            }
        }
    }

    @Override
    public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        Position position = getPosition();
        GuiTextures.BACKGROUND.draw(graphics, mouseX, mouseY, position.x, position.y, 80, 30);
        drawStringSized(graphics, "Amount", position.x + 3, position.y + 3, 0x404040, false, 1f, false);
        GuiTextures.DISPLAY.draw(graphics, mouseX, mouseY, position.x + 3, position.y + 11, 65, 14);
    }

}