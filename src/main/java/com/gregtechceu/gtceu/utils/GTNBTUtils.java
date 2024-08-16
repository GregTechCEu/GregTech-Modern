package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.GTCEu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class GTNBTUtils {
    public static CompoundTag writeItemStack(ItemStack itemStack, CompoundTag compoundTag) {
        ResourceLocation resourceLocation = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
        compoundTag.putString("id", resourceLocation.toString());
        compoundTag.putInt("Count", itemStack.getCount());
        if (itemStack.getTag() != null) {
            compoundTag.put("tag", itemStack.getTag().copy());
        }

        return compoundTag;
    }

    public static ItemStack readItemStack(CompoundTag compoundTag) {
        try {
            Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(compoundTag.getString("id")));
            int count = compoundTag.getInt("Count");
            ItemStack stack = new ItemStack(item, count);
            if (compoundTag.contains("tag", Tag.TAG_COMPOUND)) {
                stack.setTag(compoundTag.getCompound("tag"));
                if (stack.getTag() != null) {
                    stack.getItem().verifyTagAfterLoad(stack.getTag());
                }
            }

            if (stack.getItem().canBeDepleted()) {
                stack.setDamageValue(stack.getDamageValue());
            }
            return stack;
        } catch (RuntimeException ex) {
            GTCEu.LOGGER.debug("Tried to load invalid item: {}", compoundTag, ex);
            return ItemStack.EMPTY;
        }
    }
}
