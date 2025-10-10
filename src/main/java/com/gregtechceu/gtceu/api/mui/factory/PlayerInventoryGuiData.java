package com.gregtechceu.gtceu.api.mui.factory;

import com.gregtechceu.gtceu.api.mui.factory.inventory.InventoryType;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import lombok.Getter;

@Getter
public class PlayerInventoryGuiData<T> extends GuiData {

    public static <T> PlayerInventoryGuiData<T> of(Player player, InventoryType<T> inventoryType, T context,
                                                   int slotIndex) {
        return new PlayerInventoryGuiData<>(player, inventoryType, context, slotIndex);
    }

    private final InventoryType<T> inventoryType;
    private final T context;
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
