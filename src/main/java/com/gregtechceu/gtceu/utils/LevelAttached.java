/*
 * This file is part of Flywheel (https://github.com/Engine-Room/Flywheel).
 * Copyright (c) 2021-2024 Jozufozu
 *
 * MIT license
 */
package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.lang.ref.Cleaner;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Based on the <a href=
 * "https://github.com/Engine-Room/Flywheel/blob/04933a534326032333c3b39c94f4f364a30d9aaf/common/src/lib/java/dev/engine_room/flywheel/lib/util/LevelAttached.java">class
 * from Flywheel</a> with the same name.
 */
@EventBusSubscriber(modid = GTCEu.MOD_ID)
public final class LevelAttached<T> {

    private static final ConcurrentLinkedDeque<WeakReference<LevelAttached<?>>> ALL = new ConcurrentLinkedDeque<>();
    private static final Cleaner CLEANER = Cleaner.create();

    private final LoadingCache<LevelAccessor, T> cache;

    public LevelAttached(Function<LevelAccessor, T> factory, Consumer<T> finalizer) {
        WeakReference<LevelAttached<?>> thisRef = new WeakReference<>(this);
        ALL.add(thisRef);

        this.cache = CacheBuilder.newBuilder()
                // This prevents Levels from being leaked if invalidateLevel isn't called for them, usually in level
                // wrapper cases
                .weakKeys()
                .<LevelAccessor, T>removalListener(n -> finalizer.accept(n.getValue()))
                .build(new CacheLoader<>() {

                    @Override
                    public T load(LevelAccessor key) {
                        return factory.apply(key);
                    }
                });

        CLEANER.register(this, new CleaningAction(thisRef, cache));
    }

    public LevelAttached(Function<LevelAccessor, T> factory) {
        this(factory, t -> {});
    }

    public static void invalidateLevel(LevelAccessor level) {
        Iterator<WeakReference<LevelAttached<?>>> iterator = ALL.iterator();
        while (iterator.hasNext()) {
            LevelAttached<?> attached = iterator.next().get();
            if (attached == null) {
                iterator.remove();
            } else {
                attached.remove(level);
            }
        }
    }

    public T get(LevelAccessor level) {
        return cache.getUnchecked(level);
    }

    public void remove(LevelAccessor level) {
        cache.invalidate(level);
    }

    public T refresh(LevelAccessor level) {
        remove(level);
        return get(level);
    }

    public void reset() {
        cache.invalidateAll();
    }

    private record CleaningAction(WeakReference<LevelAttached<?>> ref, LoadingCache<?, ?> cache) implements Runnable {

        @Override
        public void run() {
            ALL.remove(ref);
            cache.invalidateAll();
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        invalidateLevel(event.getLevel());
    }
}
