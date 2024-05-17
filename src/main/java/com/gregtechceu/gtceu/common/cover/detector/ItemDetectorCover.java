package com.gregtechceu.gtceu.common.cover.detector;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.utils.RedstoneUtil;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

public class ItemDetectorCover extends DetectorCover {

    public ItemDetectorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    @Override
    public boolean canAttach() {
        return getItemTransfer() != null;
    }

    @Override
    protected void update() {
        if (this.coverHolder.getOffsetTimer() % 20 != 0)
            return;

        IItemHandler itemTransfer = getItemTransfer();
        if (itemTransfer == null)
            return;

        int storedItems = 0;
        int itemCapacity = itemTransfer.getSlots() * itemTransfer.getSlotLimit(0);

        if (itemCapacity == 0)
            return;

        for (int i = 0; i < itemTransfer.getSlots(); i++) {
            storedItems += itemTransfer.getStackInSlot(i).getCount();
        }

        setRedstoneSignalOutput(RedstoneUtil.computeRedstoneValue(storedItems, itemCapacity, isInverted()));
    }

    protected IItemHandler getItemTransfer() {
        return coverHolder.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, coverHolder.getPos(), attachedSide);
    }
}
