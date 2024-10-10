package com.gregtechceu.gtceu.client.renderer.pipe.util;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class ActivableCacheKey extends CacheKey {

    @Getter
    private final boolean active;

    public ActivableCacheKey(float thickness, boolean active) {
        super(thickness);
        this.active = active;
    }

    public static ActivableCacheKey of(@Nullable Float thickness, @Nullable Boolean active) {
        float thick = thickness == null ? 0.5f : thickness;
        boolean act = active != null && active;
        return new ActivableCacheKey(thick, act);
    }

    // activeness is merely a way to pass information onwards, it does not result in separate mappings.
}
