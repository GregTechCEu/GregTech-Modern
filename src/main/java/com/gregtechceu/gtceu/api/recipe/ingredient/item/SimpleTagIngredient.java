package com.gregtechceu.gtceu.api.recipe.ingredient.item;

import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item.ItemTagMapIngredient;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public final class SimpleTagIngredient extends ItemIngredient {

    @Getter
    private final TagKey<Item> tag;

    private ItemStack[] items;

    public SimpleTagIngredient(TagKey<Item> tag, int count) {
        super(count);
        this.tag = tag;
    }

    @Override
    public int hash() {
        return tag.hashCode();
    }

    @Override
    public ItemStack[] getItems() {
        // It's ok to cache items because all ingredients will be recreated when reload
        if (items == null) {
            List<ItemStack> list = new ArrayList<>();

            for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
                list.add(new ItemStack(holder, count));
            }

            if (list.isEmpty()) {
                list.add((new ItemStack(Blocks.BARRIER))
                        .setHoverName(Component.literal("Empty Tag: " + tag.location())));
            }
            items = list.toArray(new ItemStack[0]);
        }
        return items;
    }

    @Override
    public List<AbstractMapIngredient> getMapIngredients() {
        return List.of(new ItemTagMapIngredient(tag));
    }

    @Override
    public boolean test(ItemStack itemStack) {
        return itemStack.is(tag);
    }

    @Override
    public SimpleTagIngredient copy() {
        return new SimpleTagIngredient(tag, count);
    }

    @Override
    public SimpleTagIngredient copyWithCount(int count) {
        return new SimpleTagIngredient(tag, count);
    }

    @Override
    public SimpleTagIngredient copyWithMultiplier(int multiplier) {
        return new SimpleTagIngredient(tag, count * multiplier);
    }

    @Override
    public ChancedItemIngredient copyWithChance(int chance) {
        return new ChancedItemIngredient(copy(), chance);
    }

    @Override
    public Ingredient toVanillaIngredient() {
        return Ingredient.of(tag);
    }
}
