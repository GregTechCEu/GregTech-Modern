package com.gregtechceu.gtceu.api.cover;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.pipenet.enderlink.ITransferType;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.EnderLinkControllerMachine;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.GlobalPos;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface IEnderLinkCover<T> {
    GlobalPos getGlobalPos();

    void unlinkController(EnderLinkControllerMachine controller);

    int getChannel();

    IO getIo();

    ITransferType<T> getTransferType();

    @Nullable
    T getTransfer();

    void resetTransferRateLimit();
}
