package com.gregtechceu.gtceu.api.mui.factory.inventory;

import com.gregtechceu.gtceu.utils.NetworkUtils;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * A way of finding and setting an item in an inventory, that is owned by a player.
 * This includes the normal player inventory with all its slots including main hand, off-hand and armor slots.
 * It can also be used for bauble inventory if baubles is loaded.
 * An inventory type has an id assigned used for syncing. It is crucial, types are created in the same order on client
 * and server.
 * Currently, the amount of types is limited to 16, but I don't think we will ever fill that.
 * 
 * @see InventoryTypes InventoryTypes for default implementations
 */
@Getter
public abstract class InventoryType<T> {

    private static final Map<String, InventoryType<?>> inventoryTypes = new Object2ObjectOpenHashMap<>();

    private final String id;

    public InventoryType(String id) {
        this.id = id;
        if (isActive()) {
            inventoryTypes.put(id, this);
        }
    }

    public boolean isActive() {
        return true;
    }

    public abstract ItemStack getStackInSlot(Player player, T context, int index);

    public abstract void setStackInSlot(Player player, T context, int index, ItemStack stack);

    /**
     * Returns the first stackable slot index for the given item
     *
     * @param player player of which inventory to visit
     * @param stack  stack which should be checked stackability with
     * @return index of slot with stackable item or -1 if none found
     */
    public int findFirstStackable(Player player, ItemStack stack, boolean ignoreEmpty) {
        int[] holder = { -1 };
        visitAllStackable(player, stack, (type, context, index, stackInSlot) -> {
            if (stackInSlot.isEmpty() && ignoreEmpty) return false;
            holder[0] = index;
            return true;
        });
        return holder[0];
    }

    /**
     * Visits all slots which item is stackable with the given item in the inventory with the given visitor function.
     * Two empty items also count as stackable.
     *
     * @param player  player of which inventory to visit
     * @param stack   stack which should be checked stackability with
     * @param visitor visit function
     * @return if the visitor function returned true on a slot
     */
    public boolean visitAllStackable(Player player, ItemStack stack, InventoryVisitor<T> visitor) {
        visitAll(player, (type, context, index, stackInSlot) -> {
            if ((stackInSlot.isEmpty() && stack.isEmpty()) ||
                    ItemHandlerHelper.canItemStacksStack(stackInSlot, stack)) {
                return visitor.visit(type, context, index, stack);
            }
            return false;
        });
        return false;
    }

    @SuppressWarnings("unchecked")
    public T castContext(Object o) {
        return (T) o;
    }

    public abstract boolean visitAll(Player player, InventoryVisitor<T> visitor);

    public abstract void writeContext(FriendlyByteBuf byteBuf, T context);

    public abstract T readContext(FriendlyByteBuf byteBuf);

    public void write(FriendlyByteBuf buf) {
        NetworkUtils.writeStringSafe(buf, id);
    }

    public static InventoryType<?> read(FriendlyByteBuf buf) {
        return getFromId(NetworkUtils.readStringSafe(buf));
    }

    public static InventoryType<?> getFromId(String id) {
        return inventoryTypes.get(id);
    }

    public static Collection<InventoryType<?>> getAllRegistered() {
        return Collections.unmodifiableCollection(inventoryTypes.values());
    }
}
