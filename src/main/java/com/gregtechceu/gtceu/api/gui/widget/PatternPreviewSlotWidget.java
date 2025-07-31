package com.gregtechceu.gtceu.api.gui.widget;

import com.lowdragmc.lowdraglib.gui.modular.ModularUIGuiContainer;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.utils.ColorUtils;
import com.lowdragmc.lowdraglib.utils.Position;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import com.mojang.blaze3d.systems.RenderSystem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

public class PatternPreviewSlotWidget extends SlotWidget {

    public PatternPreviewSlotWidget(IItemHandlerModifiable itemHandler, int slotIndex, int xPosition, int yPosition,
                                    boolean canTakeItems, boolean canPutItems) {
        super(itemHandler, slotIndex, xPosition, yPosition, canTakeItems, canPutItems);
    }

    /**
     * Override the draw method for regular slot widget since we do custom offsets when drawing the stack
     */
    @Override
    public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackgroundTexture(graphics, mouseX, mouseY);
        Position pos = this.getPosition();
        if (this.slotReference != null) {
            ItemStack itemStack = this.getRealStack(this.slotReference.getItem());
            ModularUIGuiContainer modularUIGui = this.gui == null ? null : this.gui.getModularUIGui();
            if (itemStack.isEmpty() && modularUIGui != null && modularUIGui.getQuickCrafting() &&
                    modularUIGui.getQuickCraftSlots().contains(this.slotReference)) {
                int splitSize = modularUIGui.getQuickCraftSlots().size();
                itemStack = this.gui.getModularUIContainer().getCarried();
                if (!itemStack.isEmpty() && splitSize > 1 &&
                        AbstractContainerMenu.canItemQuickReplace(this.slotReference, itemStack, true)) {
                    itemStack = itemStack.copy();
                    itemStack.grow(AbstractContainerMenu.getQuickCraftPlaceCount(modularUIGui.getQuickCraftSlots(),
                            modularUIGui.dragSplittingLimit, itemStack));
                    int k = Math.min(itemStack.getMaxStackSize(), this.slotReference.getMaxStackSize(itemStack));
                    if (itemStack.getCount() > k) {
                        itemStack.setCount(k);
                    }
                }
            }

            if (!itemStack.isEmpty()) {
                drawItemStack(graphics, itemStack, pos.x + 1, pos.y + 1, -1, (String) null);
            }
        }

        this.drawOverlay(graphics, mouseX, mouseY, partialTicks);
        if (this.drawHoverOverlay && this.isMouseOverElement((double) mouseX, (double) mouseY) &&
                this.getHoverElement((double) mouseX, (double) mouseY) == this) {
            RenderSystem.colorMask(true, true, true, false);
            DrawerHelper.drawSolidRect(graphics, this.getPosition().x + 1, this.getPosition().y + 1, 16, 16,
                    -2130706433);
            RenderSystem.colorMask(true, true, true, true);
        }
    }

    public static void drawItemStack(@Nonnull GuiGraphics graphics, ItemStack itemStack, int x, int y, int color,
                                     @Nullable String altTxt) {
        var a = ColorUtils.alpha(color);
        var r = ColorUtils.red(color);
        var g = ColorUtils.green(color);
        var b = ColorUtils.blue(color);
        RenderSystem.setShaderColor(r, g, b, a);

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);

        Minecraft mc = Minecraft.getInstance();

        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 100);

        graphics.renderItem(itemStack, x, y);
        graphics.pose().translate(0, 0, 100);

        graphics.pose().pushPose();

        // actual offset bit that's important :3
        int xOffset = 0;
        if (itemStack.getCount() / 100_000 != 0) {
            xOffset = 9;
        } else if (itemStack.getCount() / 10_000 != 0) {
            xOffset = 6;
        } else if (itemStack.getCount() / 1000 != 0) {
            xOffset = 3;
        }

        graphics.renderItemDecorations(mc.font, itemStack, x + xOffset, y, altTxt);
        graphics.pose().popPose();

        graphics.pose().popPose();

        // clear depth buffer,it may cause some rendering issues?
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        RenderSystem.depthMask(false);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
    }
}
