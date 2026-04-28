package com.gregtechceu.gtceu.client.renderer;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class GTRenderTypes {

    private GTRenderTypes() {}

    public static RenderType getLightRing() {
        return RenderTypes.linesTranslucent();
    }

    public static RenderType getMonitor() {
        return RenderTypes.debugQuads();
    }

    public static RenderType guiTexture(Identifier texture) {
        return RenderTypes.text(texture);
    }
}
