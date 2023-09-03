package com.gregtechceu.gtceu.api.pipenet.enderlink;

import com.gregtechceu.gtceu.api.misc.ProxiedTransferWrapper;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface ITransferType<T> {
    Class<T> getTransferClass();

    long transferAll(T input, T output);

    ProxiedTransferWrapper<T> createTransferWrapper();

    default boolean isPermanentlyLocked() {
        return false;
    }
}
