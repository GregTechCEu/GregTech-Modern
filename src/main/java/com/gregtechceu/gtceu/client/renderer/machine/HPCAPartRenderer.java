package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.client.renderer.block.TextureOverrideRenderer;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class HPCAPartRenderer extends TextureOverrideRenderer {
    public HPCAPartRenderer(ResourceLocation texture, ResourceLocation damagedTexture) {
        super(texture);
    }
}
