package com.lowdragmc.gtceu.api.item.component;

import com.lowdragmc.gtceu.api.item.ComponentItem;

/**
 * @author KilaBash
 * @date 2023/2/22
 * @implNote IItemComponent
 *
 * Describes generic component attachable to {@link com.lowdragmc.gtceu.api.item.ComponentItem}
 * Multiple components can be attached to one item
 *
 */
public interface IItemComponent {
    default void onAttached(ComponentItem item) {

    }
}
