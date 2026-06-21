package com.gregtechceu.gtceu.integration.modernfix;

import com.gregtechceu.gtceu.client.util.AssetEventListener;
import com.gregtechceu.gtceu.client.util.ModelEventHelper;

import net.minecraft.client.resources.model.*;

import lombok.Getter;
import org.embeddedt.modernfix.ModernFixClient;
import org.embeddedt.modernfix.api.entrypoint.ModernFixClientIntegration;
import org.jetbrains.annotations.ApiStatus;

public class GTModernFixIntegration implements ModernFixClientIntegration {

    private static GTModernFixIntegration INSTANCE = null;
    @Getter
    private static boolean dynamicResourcesEnabled = false;

    @ApiStatus.Internal
    public GTModernFixIntegration() {
        INSTANCE = this;
    }

    public static void setAsLast() {
        if (INSTANCE != null) {
            ModernFixClient.CLIENT_INTEGRATIONS.remove(INSTANCE);
        } else {
            INSTANCE = new GTModernFixIntegration();
        }
        ModernFixClient.CLIENT_INTEGRATIONS.add(INSTANCE);
    }

    @Override
    public void onDynamicResourcesStatusChange(boolean enabled) {
        dynamicResourcesEnabled = enabled;
    }

    @Override
    public BakedModel onBakedModelLoad(ModelResourceLocation location, UnbakedModel baseModel,
                                       BakedModel originalModel, ModelState state, ModelBakery bakery,
                                       ModelBakery.TextureGetter textureGetter) {
        // process all model replacers
        for (var listener : ModelEventHelper.EVENT_LISTENERS) {
            if (!(listener.listener() instanceof AssetEventListener.BakedModelReplacement modelReplacement)) continue;
            originalModel = modelReplacement.modifyBakedModel(location, originalModel, baseModel, bakery);
        }
        return originalModel;
    }
}
