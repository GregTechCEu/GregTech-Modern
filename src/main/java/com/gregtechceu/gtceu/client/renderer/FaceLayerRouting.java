package com.gregtechceu.gtceu.client.renderer;

import com.gregtechceu.gtceu.client.model.FaceLayer;
import com.gregtechceu.gtceu.core.config.GTEarlyConfig;
import com.gregtechceu.gtceu.core.mixins.GTMixinPlugin;

import net.minecraft.client.renderer.block.model.BakedQuad;

import static com.gregtechceu.gtceu.api.GTValues.MODID_EMBEDDIUM;
import static com.gregtechceu.gtceu.api.GTValues.MODID_SODIUM;
import static com.gregtechceu.gtceu.core.config.GTEarlyConfig.isModLoaded;

public final class FaceLayerRouting {

    // Sodium drops BakedQuad extensions, so, we just smuggle it through FRAPI inside of sodium, kinda hacky but
    // whatever.
    private static final int SODIUM_FACE_LAYER_TAG = 0x47540001;
    private static final boolean CUSTOM_PASS_ENABLED = !GTMixinPlugin
            .isOptionEnabled(GTEarlyConfig.FACE_LAYER_SAFE_MODE) &&
            (isModLoaded(MODID_SODIUM) || isModLoaded(MODID_EMBEDDIUM)) &&
            GTMixinPlugin.isOptionEnabled(GTEarlyConfig.CUSTOM_CHUNK_MIXINS);

    public static boolean isCustomPassEnabled() {
        return CUSTOM_PASS_ENABLED;
    }

    public static boolean shouldUseCustomPass(BakedQuad quad) {
        return isCustomPassEnabled() && quad.gtceu$getFaceLayer().depthRank() > FaceLayer.BASE.depthRank();
    }

    public static int getSodiumRoutingTag(BakedQuad quad) {
        return shouldUseCustomPass(quad) ? SODIUM_FACE_LAYER_TAG : 0;
    }

    public static boolean isSodiumFaceLayerTag(int tag) {
        return tag == SODIUM_FACE_LAYER_TAG;
    }

    private FaceLayerRouting() {}
}
