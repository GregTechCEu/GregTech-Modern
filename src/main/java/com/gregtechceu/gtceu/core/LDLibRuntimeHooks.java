package com.gregtechceu.gtceu.core;

import com.lowdragmc.lowdraglib2.async.AsyncThreadData;
import com.lowdragmc.lowdraglib2.utils.virtuallevel.DummyWorld;

import org.jetbrains.annotations.Nullable;

/**
 * Thin compile-time bridge to a couple of LDLib2 runtime predicates that gtceu's
 * own mixins/machines need to consult. Centralizes the LDLib2 import so future
 * package drift only requires a single edit.
 *
 * <p>
 * Earlier 26.1 port revisions did string-keyed {@code Class.forName} lookups
 * here against LDLib1 class names that no longer exist; every call returned
 * {@code false} as a result, silently masking real behaviour. LDLib2 is now a
 * hard dependency (see {@code neoforge.mods.toml}), so we link to the actual
 * LDLib2 types directly.
 */
public final class LDLibRuntimeHooks {

    private LDLibRuntimeHooks() {}

    public static boolean isDummyWorld(@Nullable Object object) {
        return object instanceof DummyWorld;
    }

    public static boolean isAsyncThreadService() {
        return AsyncThreadData.isThreadService();
    }
}
