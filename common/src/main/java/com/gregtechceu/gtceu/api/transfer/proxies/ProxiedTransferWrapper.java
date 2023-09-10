package com.gregtechceu.gtceu.api.transfer.proxies;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.misc.IOFluidTransferList;
import com.gregtechceu.gtceu.api.misc.IOItemTransferList;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class ProxiedTransferWrapper<T> {
    // It is necessary to store each transfer's owner with it, because it is not guaranteed that a transfer will always
    // remain the same object across invocations.
    private final Map<Object, T> transfersIn = new Object2ObjectOpenHashMap<>();
    private final Map<Object, T> transfersOut = new Object2ObjectOpenHashMap<>();
    private final Map<Object, T> transfersBoth = new Object2ObjectOpenHashMap<>();

    // Avoid rebuilding proxies on every add or remove operation. Instead, the proxies are rebuilt on access.
    private boolean dirty = false;

    private T proxyTransferIn;
    private T proxyTransferOut;
    private T proxyTransferBoth;
    private final T proxyTransferNone;

    public ProxiedTransferWrapper() {
        buildProxies();

        proxyTransferNone = createTransferProxy(IO.NONE, List.of());
    }

    protected abstract T createTransferProxy(IO io, Collection<T> transfers);

    public void addTransfer(IO io, Object owner, T transfer) {
        getTransfersByOwner(io).put(owner, transfer);

        dirty = true;
    }

    public void removeTransfer(Object owner) {
        getTransfersByOwner(IO.IN).remove(owner);
        getTransfersByOwner(IO.OUT).remove(owner);
        getTransfersByOwner(IO.BOTH).remove(owner);

        dirty = true;
    }

    private Map<Object, T> getTransfersByOwner(IO io) {
        return switch (io) {
            case IN -> transfersIn;
            case OUT -> transfersOut;
            case BOTH -> transfersBoth;
            case NONE -> new HashMap<>(); // Must not be immutable so that inserts are still supported in the API
        };
    }

    private Collection<T> getTransfersList(IO io) {
        return getTransfersByOwner(io).values();
    }

    private void buildProxies() {
        this.proxyTransferIn = createTransferProxy(IO.IN,
                Stream.of(transfersIn, transfersBoth).flatMap(transfers -> transfers.values().stream()).toList()
        );
        this.proxyTransferOut = createTransferProxy(IO.OUT,
                Stream.of(transfersOut, transfersBoth).flatMap(transfers -> transfers.values().stream()).toList()
        );
        this.proxyTransferBoth = createTransferProxy(IO.BOTH,
                Stream.of(transfersIn, transfersOut, transfersBoth).flatMap(transfers -> transfers.values().stream()).toList()
        );

        dirty = false;
    }

    public T get(IO io) {
        if (dirty) {
            buildProxies();
        }

        return switch (io) {
            case IN -> proxyTransferIn;
            case OUT -> proxyTransferOut;
            case BOTH -> proxyTransferBoth;
            case NONE -> proxyTransferNone;
        };
    }

    public T in() {
        return get(IO.IN);
    }

    public T out() {
        return get(IO.OUT);
    }

    public T both() {
        return get(IO.BOTH);
    }

    public T none() {
        return get(IO.NONE);
    }

    public static class Fluid extends ProxiedTransferWrapper<IFluidTransfer> {
        @Override
        protected IFluidTransfer createTransferProxy(IO io, Collection<IFluidTransfer> transfers) {
            return new IOFluidTransferList(List.copyOf(transfers), io, f -> true);
        }
    }

    public static class Item extends ProxiedTransferWrapper<IItemTransfer> {
        @Override
        protected IItemTransfer createTransferProxy(IO io, Collection<IItemTransfer> transfers) {
            return new IOItemTransferList(List.copyOf(transfers), io, f -> true);
        }
    }
}
