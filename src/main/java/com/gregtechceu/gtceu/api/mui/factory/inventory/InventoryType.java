package com.gregtechceu.gtceu.api.mui.factory.inventory;

import com.gregtechceu.gtceu.utils.NetworkUtils;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

import lombok.Getter;

/**
 * A way of finding and setting an item in an inventory, that is owned by a player. This includes the normal player
 * inventory with all its
 * slots including main hand, off-hand and armor slots. It can also be used for curios inventory if it is loaded. An
 * inventory type has an
 * id assigned used for syncing. An item stack can be found with a context object and an index. Usually the context is
 * null with type
 * {@link Void}. For curios, it is a string, since slots are not just indexed, but first mapped to an identifier.
 *
 * @param <T> The type of the context. Usually it is {@link Void}, but can be anything needed to find the same item
 *            stack in client and
 *            server.
 * @see InventoryTypes InventoryTypes for default implementations
 */
public abstract class InventoryType<T> {

    @Getter
    private final String id;

    public InventoryType(String id) {
        this.id = id;
        if (isActive()) {
            InventoryTypes.inventoryTypes.put(id, this);
        }
    }

    public boolean isActive() {
        return true;
    }

    /**
     * Obtains a stack from a slot in this inventory type.
     *
     * @param player  player of inventory
     * @param context extra context to find the slot (needed for curios)
     * @param index   index of the slot
     * @return a stack
     */
    public abstract ItemStack getStackInSlot(Player player, T context, int index);

    /**
     * Sets a stack in a slot in this inventory type.
     *
     * @param player  player of inventory
     * @param context extra context to find the slot (needed for curios)
     * @param index   index of the slot
     * @param stack   stack to set
     */
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
     * Two empty items also
     * count as stackable.
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

    /**
     * Casts a context to the right type. Note that this is unchecked.
     *
     * @param o untyped context
     * @return typed context
     */
    @SuppressWarnings("unchecked")
    public T castContext(Object o) {
        return (T) o;
    }

    /**
     * Visits all slots in the inventory with the given visitor function.
     *
     * @param player  player of which inventory to visit
     * @param visitor visit function
     * @return if the visitor function returned true on a slot
     */
    public abstract boolean visitAll(Player player, InventoryVisitor<T> visitor);

    /**
     * Writes a context to a buffer.
     *
     * @param byteBuf byte buffer
     * @param context context
     */
    public abstract void writeContext(FriendlyByteBuf byteBuf, T context);

    /**
     * Reads a context from a buffer.
     *
     * @param byteBuf byte buffer
     * @return context
     */
    public abstract T readContext(FriendlyByteBuf byteBuf);

    public void write(FriendlyByteBuf buf) {
        NetworkUtils.writeStringSafe(buf, id);
    }

    public static InventoryType<?> read(FriendlyByteBuf buf) {
        return getFromId(NetworkUtils.readStringSafe(buf));
    }

    public static InventoryType<?> getFromId(String id) {
        return InventoryTypes.inventoryTypes.get(id);
    }
}
