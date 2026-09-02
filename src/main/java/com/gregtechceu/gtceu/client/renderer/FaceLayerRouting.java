package com.gregtechceu.gtceu.client.renderer;

import com.gregtechceu.gtceu.client.model.FaceLayer;
import com.gregtechceu.gtceu.core.mixins.GTMixinPlugin;

import net.minecraft.client.renderer.block.model.BakedQuad;

import static com.gregtechceu.gtceu.api.GTValues.MODID_EMBEDDIUM;
import static com.gregtechceu.gtceu.api.GTValues.MODID_SODIUM;
import static com.gregtechceu.gtceu.core.config.GTEarlyConfig.isModLoaded;

public final class FaceLayerRouting {

    private static final int TAG_MASK = 0xFFFF0000;
    private static final int TAG_PREFIX = 0x47540000;

    public static boolean usesCustomChunkPass() {
        return (isModLoaded(MODID_SODIUM) || isModLoaded(MODID_EMBEDDIUM)) &&
                GTMixinPlugin.isOptionEnabled("client.customchunk.");
    }

    public static boolean shouldRoute(BakedQuad quad) {
        return quad.gtceu$getFaceLayer().depthRank() > FaceLayer.BASE.depthRank();
    }

    public static int encodeTag(BakedQuad quad) {
        FaceLayer layer = quad.gtceu$getFaceLayer();
        return layer.depthRank() > FaceLayer.BASE.depthRank() ? TAG_PREFIX | layer.ordinal() : 0;
    }

    public static boolean shouldRoute(int tag) {
        return (tag & TAG_MASK) == TAG_PREFIX;
    }

    private FaceLayerRouting() {}
}
