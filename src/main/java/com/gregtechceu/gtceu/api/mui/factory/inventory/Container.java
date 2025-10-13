package com.gregtechceu.gtceu.api.mui.factory.inventory;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * A {@link InventoryType} implementation for {@link net.minecraft.world.Container Container}.
 */
public abstract class Container extends InventoryType<Void> {

    public Container(String id) {
        super(id);
    }

    public abstract net.minecraft.world.Container getInventory(Player player);

    @Override
    public ItemStack getStackInSlot(Player player, Void context, int index) {
        return getInventory(player).getItem(index);
    }

    @Override
    public void setStackInSlot(Player player, Void context, int index, ItemStack stack) {
        getInventory(player).setItem(index, stack);
    }

    public int getSlotCount(Player player) {
        return getInventory(player).getContainerSize();
    }

    /**
     * Visits all slots in the inventory with the given visitor function.
     *
     * @param player  player of which inventory to visit
     * @param visitor visit function
     * @return if the visitor function returned true on a slot
     */
    @Override
    public boolean visitAll(Player player, InventoryVisitor<Void> visitor) {
        for (int i = 0, n = getSlotCount(player); i < n; ++i) {
            ItemStack stackInSlot = getStackInSlot(player, null, i);
            if (visitor.visit(this, null, i, stackInSlot)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void writeContext(FriendlyByteBuf byteBuf, Void context) {}

    @Override
    public Void readContext(FriendlyByteBuf byteBuf) {
        return null;
    }
}
