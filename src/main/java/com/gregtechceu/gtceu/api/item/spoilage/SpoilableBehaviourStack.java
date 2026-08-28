package com.gregtechceu.gtceu.api.item.spoilage;

import com.gregtechceu.gtceu.common.item.SpoilableItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class SpoilableBehaviourStack extends SpoilableItemStack {

    public final ItemSpoilageData itemSpoilageData;

    public SpoilableBehaviourStack(ItemStack stack, ItemSpoilageData itemSpoilageData) {
        super(stack);
        this.itemSpoilageData = itemSpoilageData;
    }

    @Override
    public long getSpoilTicks() {
        return itemSpoilageData.ticks;
    }

    @Override
    public ItemStack spoilResult(SpoilContext spoilContext, boolean simulate) {
        ItemStack result = ItemStack.EMPTY;
        for (SpoilAction action: itemSpoilageData.spoilActions) {
            result = action.getSpoilResult(result, getStack(), spoilContext, simulate);
        }
        return result;
    }

    @Override
    protected void appendSpoilResultTooltips(List<Component> tooltips) {
        for (SpoilAction action: itemSpoilageData.spoilActions) {
            action.appendTooltip(tooltips, getStack());
        }
    }
}
