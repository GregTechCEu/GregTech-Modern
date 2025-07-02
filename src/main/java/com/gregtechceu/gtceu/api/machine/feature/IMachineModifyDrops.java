package com.gregtechceu.gtceu.api.machine.feature;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface IMachineModifyDrops extends IMachineFeature {

    /**
     * Modify or append drops.
     *
     * @param drops existing drops.
     */
    void onDrops(List<ItemStack> drops);
}
