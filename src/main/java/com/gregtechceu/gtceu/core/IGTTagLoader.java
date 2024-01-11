package com.gregtechceu.gtceu.core;

import net.minecraft.core.Registry;
import org.jetbrains.annotations.Nullable;

public interface IGTTagLoader<T> {

    void gtceu$setRegistry(Registry<T> registry);

    @Nullable
    Registry<T> gtceu$getRegistry();

}
