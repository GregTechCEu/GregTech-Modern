package com.gregtechceu.gtceu.api.item.component;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.IComponentItem;

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
    default void onAttached(IComponentItem item) {

    }
}
