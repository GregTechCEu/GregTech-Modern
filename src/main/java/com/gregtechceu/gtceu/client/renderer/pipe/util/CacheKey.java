package com.gregtechceu.gtceu.client.renderer.pipe.util;

import net.minecraft.util.StringRepresentable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@OnlyIn(Dist.CLIENT)
public class CacheKey implements StringRepresentable {

    protected final float thickness;

    private final int hash;

    public CacheKey(float thickness) {
        this.thickness = thickness;
        this.hash = computeHash();
    }

    public static CacheKey of(@Nullable Float thickness) {
        float thick = thickness == null ? 0.5f : thickness;
        return new CacheKey(thick);
    }

    public float getThickness() {
        return thickness;
    }

    protected int computeHash() {
        return Objects.hash(thickness);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (CacheKey) obj;
        return Float.floatToIntBits(this.thickness) == Float.floatToIntBits(that.thickness);
    }

    @Override
    public final int hashCode() {
        return hash;
    }

    @Override
    public @NotNull String getSerializedName() {
        return String.valueOf(Float.floatToIntBits(thickness));
    }
}
