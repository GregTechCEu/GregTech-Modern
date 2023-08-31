package com.gregtechceu.gtceu.api.cover;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.EnderLinkControllerMachine;
import com.gregtechceu.gtceu.common.pipelike.enderlink.EnderLinkChannel;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface EnderLinkCover {
    void unlinkController(EnderLinkControllerMachine controller);

    int getChannel();

    EnderLinkChannel.TransferType getTransferType();

    @Nullable
    default IFluidTransfer getFluidTransfer() {
        return null;
    }

    @Nullable
    default IItemTransfer getItemTransfer() {
        return null;
    }

    IO getIo();
}
