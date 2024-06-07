package com.gregtechceu.gtceu.client.renderer.block;

import com.lowdragmc.lowdraglib.client.renderer.impl.IModelRenderer;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
    @OnlyIn(Dist.CLIENT)
    public boolean reBakeCustomQuads() {
        return true;
    }

    @Override
    public float reBakeCustomQuadsOffset() {
        return 0.000f;
    }
}
