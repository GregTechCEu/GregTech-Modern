package com.gregtechceu.gtceu.api.misc;

import com.lowdragmc.lowdraglib.LDLib;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;


/**
 * A wrapper around a value that needs to be local to minecraft's side (server/remote).
 * Similar to {@link java.lang.ThreadLocal}, but not limited to a thread per side.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SideLocal<T> {
    private T serverValue;
    private T remoteValue;

    public SideLocal() {
        this(() -> null);
    }

    public SideLocal(Supplier<T> initializer) {
        serverValue = initializer.get();
        remoteValue = initializer.get();
    }

    public T get() {
        if (LDLib.isRemote())
            return remoteValue;
        else
            return serverValue;
    }

    public void set(T value) {
        if (LDLib.isRemote())
            remoteValue = value;
        else
            serverValue = value;
    }
}
