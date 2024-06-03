package com.gregtechceu.gtceu.client.renderer.block;

import com.lowdragmc.lowdraglib.client.renderer.impl.IModelRenderer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * @author KilaBash
 * @date 2019/12/7
 * @implNote CTMModelRenderer
 */
public class CTMModelRenderer extends IModelRenderer {

    public static final AABB SLIGHTLY_OVER_BLOCK = new AABB(-0.001f, -0.001f, -0.001f, 1.001f, 1.001f,
            1.001f);

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
