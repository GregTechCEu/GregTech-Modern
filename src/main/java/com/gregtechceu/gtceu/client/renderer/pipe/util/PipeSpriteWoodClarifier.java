package com.gregtechceu.gtceu.client.renderer.pipe.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@FunctionalInterface
public interface PipeSpriteWoodClarifier {

    ResourceLocation getTexture(boolean isWoodVariant);
}
