package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.GTCEu;
import dev.architectury.injectables.annotations.ExpectPlatform;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.Getter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Stream;

public class SizedIngredient extends Ingredient {
    public static final ResourceLocation TYPE = GTCEu.id("sized");

    protected final int amount;
    @Getter
    @Nullable
    protected TagKey<Item> tag;
    protected final Ingredient inner;
    protected ItemStack[] itemStacks = null;

    protected SizedIngredient(Ingredient inner, int amount) {
        super(Stream.empty());
        this.amount = amount;
        this.inner = inner;
    }


    protected SizedIngredient(@NotNull String tag, int amount) {
        this(Ingredient.of(TagKey.create(Registries.ITEM, new ResourceLocation(tag.toLowerCase(Locale.ROOT)))), amount);
        this.tag = TagKey.create(Registries.ITEM, new ResourceLocation(tag.toLowerCase(Locale.ROOT)));
    }

    protected SizedIngredient(@NotNull TagKey<Item> tag, int amount) {
        this(Ingredient.of(tag), amount);
        this.tag = tag;
    }

    protected SizedIngredient(ItemStack itemStack) {
        this((itemStack.hasTag() || itemStack.getDamageValue() > 0) ? NBTIngredient.createNBTIngredient(itemStack) : Ingredient.of(itemStack), itemStack.getCount());
    }

    @ExpectPlatform
    public static SizedIngredient create(ItemStack inner) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static SizedIngredient create(Ingredient inner, int amount) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static SizedIngredient create(Ingredient inner) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static SizedIngredient create(TagKey<Item> tag, int amount) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static SizedIngredient create(String tag, int amount) {
        throw new AssertionError();
    }

    public static SizedIngredient copy(Ingredient ingredient) {
        if (ingredient instanceof SizedIngredient sizedIngredient) {
            var copied = SizedIngredient.create(sizedIngredient.inner, sizedIngredient.amount);
            copied.tag = sizedIngredient.tag;
            return copied;
        }
        return SizedIngredient.create(ingredient);
    }

    public int getAmount() {
        return amount;
    }

    public Ingredient getInner() {
        return inner;
    }

    @ExpectPlatform
    public static SizedIngredient fromJson(JsonObject json) {
        throw new AssertionError();
    }

    @Override
    public @NotNull JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", TYPE.toString());
        json.addProperty("fabric:type", TYPE.toString());
        json.addProperty("count", amount);
        if (tag != null) {
            json.addProperty("tag", tag.location().toString());
        } else {
            json.add("ingredient", inner.toJson());
        }
        return json;
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        return inner.test(stack);
    }

    @Override
    public ItemStack @NotNull [] getItems() {
        if (itemStacks == null)
            itemStacks = Arrays.stream(inner.getItems()).map(i -> {
                ItemStack ic = i.copy();
                ic.setCount(amount);
                return ic;
            }).toArray(ItemStack[]::new);
        return itemStacks;
    }

    @Override
    public @NotNull IntList getStackingIds() {
        return inner.getStackingIds();
    }

    @Override
    public boolean isEmpty() {
        return inner.isEmpty();
    }

    public boolean isTag() {
        return tag != null;
    }
}
