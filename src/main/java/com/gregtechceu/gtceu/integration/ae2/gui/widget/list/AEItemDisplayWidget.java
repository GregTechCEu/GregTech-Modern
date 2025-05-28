package com.gregtechceu.gtceu.integration.ae2.gui.widget.list;

import com.gregtechceu.gtceu.api.gui.GuiTextures;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import org.jetbrains.annotations.NotNull;

import static com.gregtechceu.gtceu.integration.ae2.gui.widget.slot.AEConfigSlotWidget.drawSelectionOverlay;
import static com.lowdragmc.lowdraglib.gui.util.DrawerHelper.drawItemStack;
import static com.lowdragmc.lowdraglib.gui.util.DrawerHelper.drawText;

/**
 * Display a certain {@link appeng.api.stacks.GenericStack} element.
 */
public class AEItemDisplayWidget extends Widget {

    private final AEListGridWidget gridWidget;
    private final int index;

    public AEItemDisplayWidget(int x, int y, AEListGridWidget gridWidget, int index) {
        super(new Position(x, y), new Size(18, 18));
        this.gridWidget = gridWidget;
        this.index = index;
    }

    @Override
    public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        Position position = getPosition();
        GenericStack item = this.gridWidget.getAt(this.index);
        GuiTextures.SLOT.draw(graphics, mouseX, mouseY, position.x, position.y, 18, 18);
        GuiTextures.NUMBER_BACKGROUND.draw(graphics, mouseX, mouseY, position.x + 18, position.y, 140, 18);
        int stackX = position.x + 1;
        int stackY = position.y + 1;
        if (item != null) {
            ItemStack realStack = item.what() instanceof AEItemKey key ? new ItemStack(key.getItem()) : ItemStack.EMPTY;
            drawItemStack(graphics, realStack, stackX, stackY, -1, null);
            String amountStr = String.format("x%,d", item.amount());
            drawText(graphics, amountStr, stackX + 20, stackY + 5, 1, 0xFFFFFFFF);
        }
        if (isMouseOverElement(mouseX, mouseY)) {
            drawSelectionOverlay(graphics, stackX, stackY, 16, 16);
        }
    }

    @Override
    public void drawInForeground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (isMouseOverElement(mouseX, mouseY)) {
            GenericStack item = this.gridWidget.getAt(this.index);
            if (item != null) {
                graphics.renderTooltip(Minecraft.getInstance().font, GenericStack.wrapInItemStack(item), mouseX,
                        mouseY);
            }
        }
    }
}
