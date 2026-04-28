package com.lowdragmc.lowdraglib.gui.texture;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemStackTexture extends com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture implements IGuiTexture {

    public ItemStackTexture() {}

    public ItemStackTexture(ItemStack... items) {
        super(items);
    }

    public ItemStackTexture(Item... items) {
        super(items);
    }

    @Override
    public ItemStackTexture setItems(ItemStack... items) {
        super.setItems(items);
        return this;
    }

    @Override
    public ItemStackTexture setColor(int color) {
        super.setColor(color);
        return this;
    }

    @Override
    public ItemStackTexture rotate(float degree) {
        super.rotate(degree);
        return this;
    }

    @Override
    public ItemStackTexture scale(float scale) {
        super.scale(scale);
        return this;
    }

    @Override
    public ItemStackTexture transform(int xOffset, int yOffset) {
        super.transform(xOffset, yOffset);
        return this;
    }

    @Override
    public ItemStackTexture transform(float xOffset, float yOffset) {
        super.transform(xOffset, yOffset);
        return this;
    }

    @Override
    public ItemStackTexture copy() {
        return new ItemStackTexture(items);
    }
}
