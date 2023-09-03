package com.gregtechceu.gtceu.common.pipelike.enderlink;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.IEnderLinkCover;
import com.gregtechceu.gtceu.api.misc.ProxiedTransferWrapper;
import com.gregtechceu.gtceu.api.pipenet.enderlink.ITransferType;
import com.gregtechceu.gtceu.common.data.GTEnderLinkTransferTypes;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;

import java.util.Map;
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

    private final Map<ITransferType<?>, ProxiedTransferWrapper<?>> transferWrappersByType = new Object2ObjectOpenHashMap<>();

    @Getter
    private ITransferType<?> currentTransferType;
    @Getter
    private int typeLockedUntilTick;

    public EnderLinkChannel(IntSupplier currentTickSupplier, int id) {
        this.currentTickSupplier = currentTickSupplier;
        this.id = id;
    }

    public <T> void addTransfer(ITransferType<T> transferType, IO io, IEnderLinkCover<T> owner, T transfer) {
        getOrCreateTransferWrapper(transferType).addTransfer(io, owner, transfer);
    }

    public <T> void removeTransfer(ITransferType<T> transferType, IEnderLinkCover<T> owner) {
        getOrCreateTransferWrapper(transferType).removeTransfer(owner);
    }

    private <T> ProxiedTransferWrapper<T> getOrCreateTransferWrapper(ITransferType<T> type) {
        // This cast is safe because ITransferType.createTransferWrapper() always returns a wrapper of the correct type.
        // Because of the limitations imposed by java's generics, the wrappers need to be stored untyped, however.
        //noinspection unchecked
        return (ProxiedTransferWrapper<T>) transferWrappersByType.computeIfAbsent(type, ITransferType::createTransferWrapper);
    }

    /**
     * Checks whether transferring is currently allowed for this transfer type.
     * To be used in conjunction with {@link #lock(ITransferType)}
     */
    private boolean canTransfer(ITransferType<?> transferType) {
        var currentTick = currentTickSupplier.getAsInt();

        if (this.currentTransferType == null)
            return true;

        if (this.currentTransferType == transferType)
            return true;

        if (this.currentTransferType.isPermanentlyLocked())
            return false;

        return currentTick > typeLockedUntilTick;
    }


    private void lock(ITransferType<?> transferType) {
        var currentTick = currentTickSupplier.getAsInt();

        this.currentTransferType = transferType;
        this.typeLockedUntilTick = (transferType == GTEnderLinkTransferTypes.CONTROLLER) ?
                Integer.MAX_VALUE : currentTick + (20 * 2);
    }

    public void transferAll() {
        for (ITransferType<?> transferType : transferWrappersByType.keySet()) {
            if (!canTransfer(transferType)) {
                continue;
            }

            // This cast is safe because getTransfer(type, io) always returns the correct transfer for the TransferType.
            // Because of java's generic type erasure, we need to work with objects here, however.
            //noinspection unchecked
            ITransferType<Object> untypedTransferType = (ITransferType<Object>) transferType;
            ProxiedTransferWrapper<Object> transferWrapper = getOrCreateTransferWrapper(untypedTransferType);

            // The transfer directions of the transfers need to be inverted here:
            // The input is extracted from and the output inserted into.
            Object input = transferWrapper.get(IO.OUT);
            Object output = transferWrapper.get(IO.IN);

            var transferred = untypedTransferType.transferAll(input, output);

            if (transferred > 0L) {
                lock(transferType);
                break;
            }
        }
    }
}
