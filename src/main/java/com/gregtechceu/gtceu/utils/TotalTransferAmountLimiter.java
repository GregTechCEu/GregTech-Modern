package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.api.transfer.item.ITransferAmountLimiter;

import net.minecraft.world.item.ItemStack;

import lombok.Getter;

public class TotalTransferAmountLimiter implements ITransferAmountLimiter {

    @Getter
    private int totalRemainingTransferAmount;

    public TotalTransferAmountLimiter(int maxTransferAmount) {
        totalRemainingTransferAmount = maxTransferAmount;
    }

    @Override
    public int getRemainingTransferAmount(ItemStack stack) {
        return this.totalRemainingTransferAmount;
    }

    @Override
    public void notifyItemTransferred(ItemStack stack) {
        this.totalRemainingTransferAmount -= stack.getCount();
    }

    @Override
    public boolean canTransferMoreItems() {
        return this.totalRemainingTransferAmount > 0;
    }
}
