package com.gregtechceu.gtceu.client.renderer;

import com.gregtechceu.gtceu.client.model.FaceLayer;
import com.gregtechceu.gtceu.core.config.GTEarlyConfig;
import com.gregtechceu.gtceu.core.mixins.GTMixinPlugin;

import net.minecraft.client.renderer.block.model.BakedQuad;

import static com.gregtechceu.gtceu.api.GTValues.MODID_EMBEDDIUM;
import static com.gregtechceu.gtceu.core.config.GTEarlyConfig.isModLoaded;

public final class FaceLayerRouting {

    private static final boolean CUSTOM_PASS_ENABLED = !GTMixinPlugin
            .isOptionEnabled(GTEarlyConfig.FACE_LAYER_SAFE_MODE) &&
            isModLoaded(MODID_EMBEDDIUM);

    public static boolean isCustomPassEnabled() {
        return CUSTOM_PASS_ENABLED;
    }

    public static boolean shouldUseCustomPass(BakedQuad quad) {
        return isCustomPassEnabled() && quad.gtceu$getFaceLayer().depthRank() > FaceLayer.BASE.depthRank();
    }

    private FaceLayerRouting() {}
}
