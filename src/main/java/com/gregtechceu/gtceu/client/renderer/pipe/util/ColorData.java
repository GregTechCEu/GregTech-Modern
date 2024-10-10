package com.gregtechceu.gtceu.client.renderer.pipe.util;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Arrays;

@OnlyIn(Dist.CLIENT)
public record ColorData(int... colorsARGB) {

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ColorData) obj;
        return Arrays.equals(this.colorsARGB, that.colorsARGB);
    }
}
