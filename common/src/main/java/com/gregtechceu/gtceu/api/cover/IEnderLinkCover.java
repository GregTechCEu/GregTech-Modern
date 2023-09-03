package com.gregtechceu.gtceu.api.cover;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.pipenet.enderlink.ITransferType;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.EnderLinkControllerMachine;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface IEnderLinkCover<T> {
    void unlinkController(EnderLinkControllerMachine controller);

    int getChannel();

    ITransferType<T> getTransferType();

    @Nullable T getTransfer();

    IO getIo();
}
