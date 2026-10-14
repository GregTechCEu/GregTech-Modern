package com.gregtechceu.gtceu.integration.modernfix;

import com.gregtechceu.gtceu.client.util.AssetEventListener;
import com.gregtechceu.gtceu.client.util.ModelEventHelper;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;

import lombok.Getter;
import org.embeddedt.modernfix.ModernFixClient;
import org.embeddedt.modernfix.api.entrypoint.ModernFixClientIntegration;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Function;

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
    public BakedModel onBakedModelLoad(ResourceLocation modelLocation, UnbakedModel baseModel,
                                       BakedModel model, ModelState state, ModelBakery bakery,
                                       Function<Material, TextureAtlasSprite> textureGetter) {
        // process all model replacers
        for (var listener : ModelEventHelper.EVENT_LISTENERS) {
            if (!(listener.listener() instanceof AssetEventListener.BakedModelReplacement modelReplacement)) continue;
            model = modelReplacement.modifyBakedModel(modelLocation, model, baseModel, bakery);
        }
        return model;
    }
}
