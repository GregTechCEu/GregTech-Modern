package com.gregtechceu.gtceu.api.machine.feature;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IDataStickInteractable {

    InteractionResult onDataStickUse(Player player, ItemStack dataStick);

    InteractionResult onDataStickShiftUse(Player player, ItemStack dataStick);
}
