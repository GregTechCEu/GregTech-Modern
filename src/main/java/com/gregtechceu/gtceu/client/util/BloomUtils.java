package com.gregtechceu.gtceu.client.util;

import com.lowdragmc.shimmer.client.postprocessing.PostProcessing;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class BloomUtils {

    public static void entityBloom(Consumer<MultiBufferSource> sourceConsumer) {
        // Shimmer will call PostProcessing.BLOOM_UNREAL.renderEntityPost in LevelRenderer#renderLevel
        // We probably don't need to call it ourselves
        PostProcessing.BLOOM_UNREAL.postEntity(sourceConsumer);
    }
}
