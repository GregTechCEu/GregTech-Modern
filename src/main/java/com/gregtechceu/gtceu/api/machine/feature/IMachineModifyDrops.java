package com.gregtechceu.gtceu.api.machine.feature;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * @author KilaBash
 * @date 2022/9/23
 * @implNote IBlockEntityModifyDrops
 */
public interface IMachineModifyDrops extends IMachineFeature{

    /**
     * Modify or append drops.
     * @param drops existing drops.
     * @param entity who destroyed it.
     */
    void onDrops(List<ItemStack> drops, Player entity);
}
