package com.gregtechceu.gtceu.common.cover.detector;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.client.renderer.pipe.cover.CoverRenderer;
import com.gregtechceu.gtceu.client.renderer.pipe.cover.CoverRendererBuilder;
import com.gregtechceu.gtceu.utils.RedstoneUtil;

import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.NotNull;

public class ItemDetectorCover extends DetectorCover {

    public ItemDetectorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    @Override
    public boolean canAttach(@NotNull ICoverable coverable, @NotNull Direction side) {
        return coverable.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent();
    }

    @Override
    protected CoverRenderer buildRenderer() {
        return new CoverRendererBuilder(GTCEu.id("block/cover/overlay_item_detector"), GTCEu.id("block/cover/overlay_item_detector_emissive")).build();
    }

    @Override
    protected void update() {
        if (this.coverHolder.getOffsetTimer() % 20 != 0)
            return;

        IItemTransfer itemTransfer = getItemTransfer();
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

    protected IItemTransfer getItemTransfer() {
        return ItemTransferHelper.getItemTransfer(coverHolder.getLevel(), coverHolder.getPos(), attachedSide);
    }
}
