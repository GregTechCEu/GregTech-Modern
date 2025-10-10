package com.gregtechceu.gtceu.api.mui.factory;

import com.gregtechceu.gtceu.api.mui.factory.inventory.InventoryType;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import lombok.Getter;

/**
 * GuiData that finds an item in a player bound inventory. This can be the hotbar, main inventory, armor slots, offhand
 * slot or curios slots
 * if curios is loaded.
 *
 * @param <T> Type of the context. Usually {@link Void}. User will usually use <?>.
 */
@Getter
public class PlayerInventoryGuiData<T> extends GuiData {

    public static <T> PlayerInventoryGuiData<T> of(Player player, InventoryType<T> inventoryType, T context,
                                                   int slotIndex) {
        return new PlayerInventoryGuiData<>(player, inventoryType, context, slotIndex);
    }

    /**
     * Inventory type where the item can be found (player or curios for example).
     */
    private final InventoryType<T> inventoryType;
    /**
     * Additional context to find the item. Usually this is null, but for curios it is a string (slot identifier).
     */
    private final T context;
    /**
     * Slot index where the item can be found.
     */
    private final int slotIndex;

    private PlayerInventoryGuiData(Player player, InventoryType<T> inventoryType, T context, int slotIndex) {
        super(player);
        this.inventoryType = inventoryType;
        this.context = context;
        this.slotIndex = slotIndex;
    }

    public ItemStack getUsedItemStack() {
        return getInventoryType().getStackInSlot(getPlayer(), context, this.slotIndex);
    }

    public void setUsedItemStack(ItemStack stack) {
        getInventoryType().setStackInSlot(getPlayer(), context, this.slotIndex, stack);
    }
}
