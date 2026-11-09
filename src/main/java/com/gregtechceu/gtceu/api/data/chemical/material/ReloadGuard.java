package com.gregtechceu.gtceu.api.data.chemical.material;

import com.gregtechceu.gtceu.GTCEu;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ReloadGuard {

    private static final AtomicInteger ACTIVE_RELOADS = new AtomicInteger();

    private ReloadGuard() {}

    public static boolean isReloading() {
        return ACTIVE_RELOADS.get() > 0;
    }

    /**
     * Marks a reload as active.
     *
     * @return the action that ends this reload.
     */
    public static Runnable beginReload() {
        if (GTCEu.isClientThread()) return () -> {};

        ACTIVE_RELOADS.incrementAndGet();
        AtomicBoolean ended = new AtomicBoolean();
        return () -> {
            if (ended.compareAndSet(false, true)) {
                ACTIVE_RELOADS.updateAndGet(count -> Math.max(0, count - 1));
            }
        };
    }

    public static void reset() {
        ACTIVE_RELOADS.set(0);
    }
}
