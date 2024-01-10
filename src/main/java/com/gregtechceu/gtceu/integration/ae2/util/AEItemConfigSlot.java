package com.gregtechceu.gtceu.integration.ae2.util;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import com.google.common.collect.Lists;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.AEConfigWidget;
import com.lowdragmc.lowdraglib.gui.ingredient.Target;
import com.lowdragmc.lowdraglib.gui.util.TextFormattingUtil;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

import static com.lowdragmc.lowdraglib.gui.util.DrawerHelper.drawItemStack;
import static com.lowdragmc.lowdraglib.gui.util.DrawerHelper.drawStringFixedCorner;

/**
 * @Author GlodBlock
 * @Description A configurable slot for {@link ItemStack}
 * @Date 2023/4/22-0:48
 */
public class AEItemConfigSlot extends AEConfigSlot {

    public AEItemConfigSlot(int x, int y, AEConfigWidget widget, int index) {
        super(new Position(x, y), new Size(18, 18 * 2), widget, index);
    }

    @Override
    public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        Position position = getPosition();
        IConfigurableSlot slot = this.parentWidget.getDisplay(this.index);
        GenericStack config = slot.getConfig();
        GenericStack stock = slot.getStock();
        GuiTextures.SLOT.draw(graphics, mouseX, mouseY, position.x, position.y, 18, 18);
        GuiTextures.SLOT.draw(graphics, mouseX, mouseY, position.x, position.y + 18, 18, 18);
        GuiTextures.CONFIG_ARROW_DARK.draw(graphics, mouseX, mouseY, position.x, position.y, 18, 18);
        if (this.select) {
            GuiTextures.SELECT_BOX.draw(graphics, mouseX, mouseY, position.x, position.y, 18, 18);
        }
        int stackX = position.x + 1;
        int stackY = position.y + 1;
        if (config != null) {
            ItemStack stack = config.what() instanceof AEItemKey key ? new ItemStack(key.getItem(), (int) config.amount()) : ItemStack.EMPTY;
            stack.setCount(1);
            drawItemStack(graphics, stack, stackX, stackY, 0xFFFFFFFF, null);
            String amountStr = TextFormattingUtil.formatLongToCompactString(config.amount(), 4);
            drawStringFixedCorner(graphics, amountStr, stackX + 17, stackY + 17, 16777215, true, 0.5f);
        }
        if (stock != null) {
            ItemStack stack = stock.what() instanceof AEItemKey key ? new ItemStack(key.getItem(), (int) stock.amount()) : ItemStack.EMPTY;
            stack.setCount(1);
            drawItemStack(graphics, stack, stackX, stackY + 18, 0xFFFFFFFF, null);
            String amountStr = TextFormattingUtil.formatLongToCompactString(stock.amount(), 4);
            drawStringFixedCorner(graphics, amountStr, stackX + 17, stackY + 18 + 17, 16777215, true, 0.5f);
        }
        if (mouseOverConfig(mouseX, mouseY)) {
            drawSelectionOverlay(graphics, stackX, stackY, 16, 16);
        } else if (mouseOverStock(mouseX, mouseY)) {
            drawSelectionOverlay(graphics, stackX, stackY + 18, 16, 16);
        }
    }

    @Override
    public void drawInForeground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
        GenericStack item = null;
        IConfigurableSlot slot = this.parentWidget.getDisplay(this.index);
        if (mouseOverConfig(mouseX, mouseY)) {
            item = slot.getConfig();
        } else if (mouseOverStock(mouseX, mouseY)) {
            item = slot.getStock();
        }
        if (item != null) {
            graphics.renderTooltip(Minecraft.getInstance().font, GenericStack.wrapInItemStack(item), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseOverConfig(mouseX, mouseY)) {
            if (button == 1) {
                // Right click to clear
                this.parentWidget.disableAmount();
                writeClientAction(REMOVE_ID, buf -> {});
            } else if (button == 0) {
                // Left click to set/select
                ItemStack item = this.gui.getModularUIContainer().getCarried();

                if (!item.isEmpty()) {
                    writeClientAction(UPDATE_ID, buf -> buf.writeItem(item));
                    this.parentWidget.enableAmount(this.index);
                }
                this.select = true;
            }
            return true;
        } else if (mouseOverStock(mouseX, mouseY)) {
            // Left click to pick up
            if (button == 0) {
                GenericStack stack = this.parentWidget.getDisplay(this.index).getStock();
                if (stack != null) {
                    writeClientAction(PICK_UP_ID, buf -> {});
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        super.handleClientAction(id, buffer);
        IConfigurableSlot slot = this.parentWidget.getConfig(this.index);
        if (id == REMOVE_ID) {
            slot.setConfig(null);
            this.parentWidget.disableAmount();
            writeUpdateInfo(REMOVE_ID, buf -> {});
        }
        if (id == UPDATE_ID) {
            ItemStack item = buffer.readItem();
            slot.setConfig(new GenericStack(AEItemKey.of(item.getItem(), item.getTag()), item.getCount()));
            this.parentWidget.enableAmount(this.index);
            if (!item.isEmpty()) {
                writeUpdateInfo(UPDATE_ID, buf -> buf.writeItem(item));
            }
        }
        if (id == AMOUNT_CHANGE_ID) {
            if (slot.getConfig() != null) {
                long amt = buffer.readVarLong();
                slot.setConfig(new GenericStack(slot.getConfig().what(), amt));
                writeUpdateInfo(AMOUNT_CHANGE_ID, buf -> buf.writeVarLong(amt));
            }
        }
        if (id == PICK_UP_ID) {
            if (slot.getStock() != null && this.gui.getModularUIContainer().getCarried() == ItemStack.EMPTY && slot.getStock().what() instanceof AEItemKey key) {
                ItemStack stack = new ItemStack(key.getItem(), Math.min((int) slot.getStock().amount(), key.getItem().getMaxStackSize()));
                if (key.hasTag()) {
                    stack.setTag(key.getTag().copy());
                }
                this.gui.getModularUIContainer().setCarried(stack);
                GenericStack stack1 = ExportOnlyAESlot.copy(slot.getStock(), Math.max(0, (slot.getStock().amount() - stack.getCount())));
                slot.setStock(stack1.amount() == 0 ? null : stack1);
                writeUpdateInfo(PICK_UP_ID, buf -> {});
            }
        }
    }

    @Override
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        super.readUpdateInfo(id, buffer);
        IConfigurableSlot slot = this.parentWidget.getDisplay(this.index);
        if (id == REMOVE_ID) {
            slot.setConfig(null);
        }
        if (id == UPDATE_ID) {
            ItemStack item = buffer.readItem();
            slot.setConfig(new GenericStack(AEItemKey.of(item.getItem(), item.getTag()), item.getCount()));
        }
        if (id == AMOUNT_CHANGE_ID) {
            if (slot.getConfig() != null) {
                long amt = buffer.readVarLong();
                slot.setConfig(new GenericStack(slot.getConfig().what(), amt));
            }
        }
        if (id == PICK_UP_ID) {
            if (slot.getStock() != null && slot.getStock().what() instanceof AEItemKey key) {
                ItemStack stack = new ItemStack(key.getItem(), Math.min((int) slot.getStock().amount(), key.getItem().getMaxStackSize()));
                if (key.hasTag()) {
                    stack.setTag(key.getTag().copy());
                }
                this.gui.getModularUIContainer().setCarried(stack);
                GenericStack stack1 = ExportOnlyAESlot.copy(slot.getStock(), Math.max(0, (slot.getStock().amount() - stack.getCount())));
                slot.setStock(stack1.amount() == 0 ? null : stack1);
            }
        }
    }

    @Override
    public List<Target> getPhantomTargets(Object ingredient) {
        if (!(ingredient instanceof ItemStack)) {
            return Collections.emptyList();
        }
        Rect2i rectangle = toRectangleBox();
        rectangle.setHeight(rectangle.getHeight() / 2);
        return Lists.newArrayList(new Target() {

            @Nonnull
            @Override
            public Rect2i getArea() {
                return rectangle;
            }

            @Override
            public void accept(@Nonnull Object ingredient) {
                if (ingredient instanceof ItemStack) {
                    writeClientAction(UPDATE_ID, buf -> buf.writeItem((ItemStack) ingredient));
                }
            }
        });
    }

    @Override
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        IConfigurableSlot slot = this.parentWidget.getDisplay(this.index);
        Rect2i rectangle = toRectangleBox();
        rectangle.setHeight(rectangle.getHeight() / 2);
        if (slot.getConfig() == null || wheelDelta == 0 || !rectangle.contains((int) mouseX, (int) mouseY)) {
            return false;
        }
        GenericStack stack = slot.getConfig();
        long amt;
        if (isCtrlDown()) {
            amt = wheelDelta > 0 ? stack.amount() * 2L : stack.amount() / 2L;
        } else {
            amt = wheelDelta > 0 ? stack.amount() + 1L : stack.amount() - 1L;
        }
        if (amt > 0 && amt < Integer.MAX_VALUE + 1L) {
            writeClientAction(AMOUNT_CHANGE_ID, buf -> buf.writeVarLong(amt));
            return true;
        }
        return false;
    }

}