package com.gregtechceu.gtceu.api.item.spoilage;

import com.gregtechceu.gtceu.common.item.SpoilableItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SpoilableBehaviourStack extends SpoilableItemStack {

    public final ItemSpoilBehaviour itemSpoilBehaviour;

    public SpoilableBehaviourStack(ItemStack stack, ItemSpoilBehaviour itemSpoilBehaviour) {
        super(stack);
        this.itemSpoilBehaviour = itemSpoilBehaviour;
    }

    @Override
    public long getSpoilTicks() {
        return itemSpoilBehaviour.ticks;
    }

    @Override
    public ItemStack spoilResult(SpoilContext spoilContext, boolean simulate) {
        //spoilResult.getSpoilResult(getStack(), spoilContext, simulate)
        return ItemStack.EMPTY;
    }

    @Override
    protected Component getSpoilResultTooltip() {
        return Component.empty(); //spoilsIntoTooltip.apply(getStack());
    }
}
