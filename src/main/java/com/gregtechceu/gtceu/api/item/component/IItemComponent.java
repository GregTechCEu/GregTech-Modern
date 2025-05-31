package com.gregtechceu.gtceu.api.item.component;

import com.gregtechceu.gtceu.api.item.ComponentItem;

import net.minecraft.world.item.Item;

/**
 * Describes generic component attachable to {@link ComponentItem}
 * Multiple components can be attached to one item
 */
public interface IItemComponent {

    default void onAttached(Item item) {}
}
