package com.gregtechceu.gtceu.client.renderer.block;

import com.gregtechceu.gtlib.client.renderer.impl.IModelRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;

/**
 * @author KilaBash
 * @date 2019/12/7
 * @implNote CTMModelRenderer
 */
public class CTMModelRenderer extends IModelRenderer {
    public CTMModelRenderer(ResourceLocation modelLocation) {
        super(modelLocation);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean reBakeCustomQuads() {
        return true;
    }
}
