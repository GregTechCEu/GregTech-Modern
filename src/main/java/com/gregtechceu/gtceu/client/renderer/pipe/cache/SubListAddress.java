package com.gregtechceu.gtceu.client.renderer.pipe.cache;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public record SubListAddress(int startInclusive, int endExclusive) {

    public <T> @NotNull List<T> getSublist(@NotNull List<T> list) {
        return list.subList(startInclusive, endExclusive);
    }
}
