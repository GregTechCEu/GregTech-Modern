package brachy.modularui.drawable;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.utils.serialization.codec.CodecUtil;
import brachy.modularui.utils.serialization.codec.MutableObjectCodec;

import net.minecraft.util.Util;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import com.google.gson.JsonObject;

import com.google.gson.JsonParseException;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@ToString
@Accessors(fluent = true, chain = true)
public class ItemDrawable implements IDrawable {

    public static final MutableObjectCodec<ItemDrawable> CODEC = MutableObjectCodec.drawableBuilder(ItemDrawable::new)
            .add("items", ItemDrawable::items, ItemDrawable::getItemList, CodecUtil.listLike(ItemStack.CODEC)).alias("item")
            .addOpt("cycleTime", ItemDrawable::cycleTime, ItemDrawable::cycleTime, Codec.INT, 1000)
            .build();

    private ItemStack[] items = new ItemStack[0];
    @Getter
    @Setter
    private int cycleTime = 1000;

    private ItemDrawable() {
        this(new ItemStack[0]);
    }

    public ItemDrawable(Ingredient ingredient) {
        this(ingredient.getItems());
    }

    public ItemDrawable(ItemStack... items) {
        items(items);
    }

    public ItemDrawable(ItemStack item) {
        item(item);
    }

    public ItemDrawable(ItemLike item) {
        item(item);
    }

    public ItemDrawable(ItemLike item, int amount) {
        item(item, amount);
    }

    public ItemDrawable(ItemLike item, int amount, DataComponentPatch componentPatch) {
        item(item, amount, componentPatch);
    }

    public static ItemDrawable ofJson(JsonObject json) {
        return CODEC.codec().parse(JsonOps.INSTANCE, json).getOrThrow(JsonParseException::new);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        if (this.items.length == 0) return;
        ItemStack item = this.items.length == 1 ? this.items[0] :
                this.items[(int) (Util.getMillis() % (this.cycleTime * this.items.length)) / this.cycleTime];
        if (item != null) {
            GuiDraw.drawItem(context.getGraphics(), item, x, y, width, height, context.getCurrentDrawingZ());
        }
    }

    @Override
    public int getDefaultHeight() {
        return 16;
    }

    @Override
    public int getDefaultWidth() {
        return 16;
    }

    public ItemStack[] getItems() {
        return this.items;
    }

    public void ingredient(Ingredient ingredient) {
        items(ingredient.getItems());
    }

    public ItemDrawable items(Collection<ItemStack> items) {
        return items(items.toArray(ItemStack[]::new));
    }

    public ItemDrawable items(ItemStack... items) {
        this.items = items;
        return this;
    }

    public ItemDrawable item(ItemStack item) {
        if (this.items.length != 1) {
            this.items = new ItemStack[1];
        }
        this.items[0] = item;
        return this;
    }

    public ItemDrawable item(ItemLike item) {
        return item(item.asItem(), 1, DataComponentPatch.EMPTY);
    }

    public ItemDrawable item(ItemLike item, int amount) {
        return item(item, amount, DataComponentPatch.EMPTY);
    }

    public ItemDrawable item(ItemLike item, int amount, DataComponentPatch componentPatch) {
        ItemStack itemStack = new ItemStack(item, amount);
        itemStack.applyComponents(componentPatch);
        return item(itemStack);
    }

    public ItemDrawable addItem(ItemStack item) {
        this.items = ArrayUtils.add(this.items, item);
        return this;
    }

    public ItemDrawable addItem(ItemLike item) {
        return addItem(item.asItem(), 1, DataComponentPatch.EMPTY);
    }

    public ItemDrawable addItem(ItemLike item, int amount) {
        return addItem(item, amount, DataComponentPatch.EMPTY);
    }

    public ItemDrawable addItem(ItemLike item, int amount, DataComponentPatch componentPatch) {
        ItemStack itemStack = new ItemStack(item, amount);
        itemStack.applyComponents(componentPatch);
        return addItem(itemStack);
    }

    public List<ItemStack> getItemList() {
        return Arrays.asList(this.items);
    }

    @Override
    public String getTypeName() {
        return "item";
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof ItemDrawable that)) return false;
        if (this.cycleTime != that.cycleTime || this.items.length != that.items.length) return false;
        for (int i = 0; i < this.items.length; i++) {
            var i1 = this.items[i];
            var i2 = that.items[i];
            if ((i1 == null || i2 == null) && i1 != i2) return false;
            if (!ItemStack.isSameItemSameComponents(i1, i2)) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(this.items);
        result = 31 * result + this.cycleTime;
        return result;
    }
}
