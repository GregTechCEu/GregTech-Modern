package com.gregtechceu.gtceu.integration.ae2.gui.widget;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlot;

import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.utils.Position;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.stacks.GenericStack;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import static com.lowdragmc.lowdraglib.gui.util.DrawerHelper.drawStringSized;

public class AmountSetWidget extends Widget {

    private int index = -1;
    @Getter
    private final TextFieldWidget amountText;
    private final ConfigWidget parentWidget;

    public AmountSetWidget(int x, int y, ConfigWidget widget) {
        super(x, y, 80, 30);
        this.parentWidget = widget;
        this.amountText = new TextFieldWidget(x + 3, y + 12, 65, 13, this::getAmountStr, this::setNewAmount)
                .setNumbersOnly(0, Integer.MAX_VALUE)
                .setMaxStringLength(10);
    }

    @OnlyIn(Dist.CLIENT)
    public void setSlotIndexClient(int slotIndex) {
        this.index = slotIndex;
        writeClientAction(0, buf -> buf.writeVarInt(this.index));
    }

    public void setSlotIndex(int slotIndex) {
        this.index = slotIndex;
    }

    public String getAmountStr() {
        if (this.index < 0) {
            return "0";
        }
        IConfigurableSlot slot = this.parentWidget.getConfig(this.index);
        if (slot.getConfig() != null) {
            return String.valueOf(slot.getConfig().amount());
        }
        return "0";
    }

    public void setNewAmount(String amount) {
        try {
            long newAmount = Long.parseLong(amount);
            if (this.index < 0) {
                return;
            }
            IConfigurableSlot slot = this.parentWidget.getConfig(this.index);
            if (newAmount > 0 && slot.getConfig() != null) {
                slot.setConfig(new GenericStack(slot.getConfig().what(), newAmount));
            }
        } catch (NumberFormatException ignore) {}
    }

    @Override
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        super.handleClientAction(id, buffer);
        if (id == 0) {
            this.index = buffer.readVarInt();
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        Position position = getPosition();
        GuiTextures.BACKGROUND.draw(graphics, mouseX, mouseY, position.x, position.y, 80, 30);
        drawStringSized(graphics, "Amount", position.x + 3, position.y + 3, 0x404040, false, 1f, false);
        GuiTextures.DISPLAY.draw(graphics, mouseX, mouseY, position.x + 3, position.y + 11, 65, 14);
    }
}
