package com.gregtechceu.gtceu.api.item.data;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.function.Consumer;

/** Access to GT's legacy item fields inside vanilla's persisted and synchronized custom-data component. */
public final class ItemStackData {

    private ItemStackData() {}

    /** A detached snapshot. Reading absent data does not change stack equality or create a component. */
    public static CompoundTag read(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    /**
     * Mutate only the supplied tag, not the stack, inside the callback. Commits on normal return only.
     * The committed value is copied again so retaining the callback's tag cannot mutate the component.
     */
    public static void update(ItemStack stack, Consumer<CompoundTag> mutation) {
        CompoundTag tag = read(stack);
        mutation.accept(tag);
        CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
    }

    /** Update a nested item-data compound while retaining all unrelated root and nested fields. */
    public static void updateCompound(ItemStack stack, String key, Consumer<CompoundTag> mutation) {
        update(stack, root -> {
            CompoundTag nested = root.getCompound(key).orElseGet(CompoundTag::new);
            mutation.accept(nested);
            root.put(key, nested);
        });
    }
}
