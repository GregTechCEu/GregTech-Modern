package com.gregtechceu.gtceu.client.renderer.pipe.util;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class WoodCacheKey extends CacheKey {

    private final boolean wood;

    public WoodCacheKey(float thickness, boolean wood) {
        super(thickness);
        this.wood = wood;
    }

    public static WoodCacheKey of(@Nullable Float thickness, @Nullable Boolean wood) {
        float thick = thickness == null ? 0.5f : thickness;
        boolean wd = wood != null && wood;
        return new WoodCacheKey(thick, wd);
    }

    public static WoodCacheKey of(@Nullable Float thickness, @Nullable Material material) {
        float thick = thickness == null ? 0.5f : thickness;
        boolean wood = material != null && material.hasProperty(PropertyKey.WOOD);
        return new WoodCacheKey(thick, wood);
    }

    public boolean isWood() {
        return wood;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (WoodCacheKey) obj;
        return Float.floatToIntBits(thickness) == Float.floatToIntBits(thickness) &&
                this.wood == that.wood;
    }

    @Override
    protected int computeHash() {
        return Objects.hash(thickness, wood);
    }

    @Override
    public @NotNull String getSerializedName() {
        return super.getSerializedName() + wood;
    }
}
