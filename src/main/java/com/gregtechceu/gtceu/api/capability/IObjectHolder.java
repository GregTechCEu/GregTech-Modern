package com.gregtechceu.gtceu.api.capability;

import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;

public interface IObjectHolder {
    Direction getFrontFacing();
}
