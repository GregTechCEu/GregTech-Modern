package com.gregtechceu.gtceu.api.item.component;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import net.minecraft.world.item.Item;

/**
 * @author KilaBash
 * @date 2023/2/22
 * @implNote IItemComponent
 *
 * Describes generic component attachable to {@link ComponentItem}
 * Multiple components can be attached to one item
 *
 */
public interface IItemComponent {
    default void onAttached(Item item) {

    }
}
