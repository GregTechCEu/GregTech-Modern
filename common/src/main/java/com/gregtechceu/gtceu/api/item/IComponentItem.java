package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import net.minecraft.world.level.ItemLike;

public interface IComponentItem extends ItemLike {

    void attachComponents(IItemComponent... components);
}
