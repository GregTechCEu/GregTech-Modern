package com.gregtechceu.gtceu.core.mixins.client;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractContainerMenu.class)
public interface AbstractContainerMenuAccessor {

    @Accessor
    int getQuickcraftType();

    @Invoker
    boolean invokeMoveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection);

    @Invoker
    void invokeClearContainer(Player playerIn, Container inventoryIn);
}
