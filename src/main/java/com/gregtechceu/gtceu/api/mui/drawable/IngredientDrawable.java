package com.gregtechceu.gtceu.api.mui.drawable;

import com.gregtechceu.gtceu.api.mui.base.IJsonSerializable;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.client.mui.screen.viewport.GuiContext;

import net.minecraft.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Tolerate;

public class IngredientDrawable implements IDrawable, IJsonSerializable<IngredientDrawable> {

    @Getter
    @Setter
    private ItemStack[] items;
    @Getter
    @Setter
    private int cycleTime = 1000;

    public IngredientDrawable(Ingredient ingredient) {
        this(ingredient.getItems());
    }

    public IngredientDrawable(ItemStack... items) {
        setItems(items);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        if (this.items.length == 0) return;
        ItemStack item = this.items[(int) (Util.getMillis() % (this.cycleTime * this.items.length)) / this.cycleTime];
        if (item != null) {
            GuiDraw.drawItem(context.getGraphics(), item, x, y, width, height, context.getCurrentDrawingZ());
        }
    }

    /**
     * Sets how many milliseconds each item shows up
     *
     * @param cycleTime time per item in milliseconds
     * @return this
     */
    @Tolerate
    public IngredientDrawable cycleTime(int cycleTime) {
        this.cycleTime = cycleTime;
        return this;
    }

    @Tolerate
    public void setItems(Ingredient ingredient) {
        setItems(ingredient.getItems());
    }
}
