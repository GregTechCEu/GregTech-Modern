package com.gregtechceu.gtceu.client.renderer.pipe.util;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record ColorData(int... colorsARGB) {}
