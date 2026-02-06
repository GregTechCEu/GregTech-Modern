package com.gregtechceu.gtceu.api.mui.drawable;

import com.gregtechceu.gtceu.api.mui.base.IJsonSerializable;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.api.mui.widget.Widget;
import com.gregtechceu.gtceu.client.mui.screen.viewport.GuiContext;
import com.gregtechceu.gtceu.utils.serialization.json.JsonHelper;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;

public class ItemDrawable implements IDrawable, IJsonSerializable<ItemDrawable> {

    @Getter
    private ItemStack item = ItemStack.EMPTY;

    public ItemDrawable() {}

    public ItemDrawable(@NotNull ItemStack item) {
        setItem(item);
    }

    public ItemDrawable(@NotNull Item item) {
        setItem(item);
    }

    public ItemDrawable(@NotNull Item item, int amount) {
        setItem(item, amount);
    }

    public ItemDrawable(@NotNull Item item, int amount, @Nullable CompoundTag nbt) {
        setItem(item, amount, nbt);
    }

    public ItemDrawable(@NotNull Block item) {
        setItem(item);
    }

    public ItemDrawable(@NotNull Block item, int amount) {
        setItem(new ItemStack(item, amount));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        applyColor(widgetTheme.getColor());
        GuiDraw.drawItem(context.getGraphics(), this.item, x, y, width, height, context.getCurrentDrawingZ());
    }

    @Override
    public int getDefaultWidth() {
        return 16;
    }

    @Override
    public int getDefaultHeight() {
        return 16;
    }

    @Override
    public Widget<?> asWidget() {
        return IDrawable.super.asWidget().size(16);
    }

    public ItemDrawable setItem(@NotNull ItemStack item) {
        this.item = item;
        return this;
    }

    public ItemDrawable setItem(@NotNull Item item) {
        return setItem(item, 1, null);
    }

    public ItemDrawable setItem(@NotNull Item item, int amount) {
        return setItem(item, amount, null);
    }

    public ItemDrawable setItem(@NotNull Item item, int amount, @Nullable CompoundTag nbt) {
        ItemStack itemStack = new ItemStack(item, amount);
        itemStack.setTag(nbt);
        return setItem(itemStack);
    }

    public ItemDrawable setItem(@NotNull Block item) {
        return setItem(item, 1);
    }

    public ItemDrawable setItem(@NotNull Block item, int amount) {
        return setItem(new ItemStack(item, amount));
    }

    public static ItemDrawable ofJson(JsonObject json) {
        String itemName = JsonHelper.getString(json, null, "item");
        if (itemName == null) throw new JsonParseException("Item property not found!");
        if (itemName.isEmpty()) return new ItemDrawable();
        ItemStack stack;
        try {
            ResourceLocation id = new ResourceLocation(itemName);
            stack = new ItemStack(BuiltInRegistries.ITEM.get(id));
        } catch (NoSuchElementException e) {
            throw new JsonParseException(e);
        }
        if (json.has("nbt")) {
            CompoundTag nbt = (CompoundTag) JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE,
                    JsonHelper.getObject(json, new JsonObject(), o -> o, "nbt"));
            stack.setTag(nbt);
        }
        return new ItemDrawable(stack);
    }

    @Override
    public boolean saveToJson(JsonObject json) {
        if (this.item == null || this.item.isEmpty()) {
            json.addProperty("item", "");
            return true;
        }
        json.addProperty("item", this.item.getItemHolder().unwrapKey().get().location().toString());
        if (this.item.hasTag()) {
            json.addProperty("nbt", this.item.getTag().toString());
        }
        return true;
    }
}
