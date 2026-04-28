package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Compile-only compatibility name for APIs that still expose the pre-26 GUI
 * drawing facade.
 */
public class GuiGraphics extends GuiGraphicsExtractor {

    public GuiGraphics(Minecraft minecraft, GuiRenderState renderState, int mouseX, int mouseY) {
        super(minecraft, renderState, mouseX, mouseY);
    }

    public void drawString(Font font, String text, int x, int y, int color, boolean shadow) {
        text(font, text, x, y, color, shadow);
    }

    public void renderItem(ItemStack stack, int x, int y) {
        item(stack, x, y);
    }

    public void renderItem(ItemStack stack, int x, int y, int seed) {
        item(stack, x, y, seed);
    }

    public void renderItem(LivingEntity entity, ItemStack stack, int x, int y, int seed) {
        item(entity, stack, x, y, seed);
    }

    public void renderFakeItem(ItemStack stack, int x, int y) {
        fakeItem(stack, x, y);
    }

    public void renderFakeItem(ItemStack stack, int x, int y, int seed) {
        fakeItem(stack, x, y, seed);
    }

    public void renderItemDecorations(Font font, ItemStack stack, int x, int y) {
        itemDecorations(font, stack, x, y);
    }

    public void renderItemDecorations(Font font, ItemStack stack, int x, int y, String text) {
        itemDecorations(font, stack, x, y, text);
    }
}
