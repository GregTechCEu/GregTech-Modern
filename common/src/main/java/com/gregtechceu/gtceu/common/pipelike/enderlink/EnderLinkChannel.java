package com.gregtechceu.gtceu.common.pipelike.enderlink;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.IEnderLinkCover;
import com.gregtechceu.gtceu.api.misc.ProxiedTransferWrapper;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import lombok.Getter;

import java.util.function.IntSupplier;


/**
 * Represents a single channel in the ender link network.
 * <p>
 * Each channel can transfer either items or fluids, or link a controller to the network.<br>
 * Once a transfer handler for a specific type is requested,
 */
public class EnderLinkChannel {
    @Getter
    private final int id;
    private final IntSupplier currentTickSupplier;

    private final ProxiedTransferWrapper<IItemTransfer> itemTransferWrapper = new ProxiedTransferWrapper.Item();
    private final ProxiedTransferWrapper<IFluidTransfer> fluidTransferWrapper = new ProxiedTransferWrapper.Fluid();

    @Getter
    private TransferType currentTransferType;
    @Getter
    private int typeLockedUntilTick;

    public EnderLinkChannel(IntSupplier currentTickSupplier, int id) {
        this.currentTickSupplier = currentTickSupplier;
        this.id = id;
    }

    public void addItemTransfer(IO io, IEnderLinkCover owner, IItemTransfer transfer) {
        itemTransferWrapper.addTransfer(io, owner, transfer);
    }

    public void removeItemTransfer(IEnderLinkCover owner) {
        itemTransferWrapper.removeTransfer(owner);
    }

    public void addFluidTransfer(IO io, IEnderLinkCover owner, IFluidTransfer transfer) {
        fluidTransferWrapper.addTransfer(io, owner, transfer);
    }

    public void removeFluidTransfer(IEnderLinkCover owner) {
        fluidTransferWrapper.removeTransfer(owner);
    }

    public IFluidTransfer getFluidTransferWrapper(IO io) {
        if (lock(TransferType.FLUID)) {
            return fluidTransferWrapper.get(io);
        }
        return fluidTransferWrapper.none();

    }

    public IItemTransfer getItemTransferWrapper(IO io) {
        if (lock(TransferType.ITEM)) {
            return itemTransferWrapper.get(io);
        }
        return itemTransferWrapper.none();

    }

    private boolean lock(TransferType transferType) {
        var currentTick = currentTickSupplier.getAsInt();

        if (this.currentTransferType != null && this.currentTransferType != transferType && currentTick <= typeLockedUntilTick) {
            return false;
        }

        this.currentTransferType = transferType;
        this.typeLockedUntilTick = (transferType == TransferType.CONTROLLER) ?
                Integer.MAX_VALUE : currentTick + (20 * 2);

        return true;
    }

    public enum TransferType {
        // TODO possibly make this a registry instead of an enum
        CONTROLLER,
        ITEM,
        FLUID,
    }
}
