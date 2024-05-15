package com.gregtechceu.gtceu.client.renderer.block;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.utils.SupplierMemoizer;

import com.lowdragmc.lowdraglib.client.renderer.impl.IModelRenderer;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * @author KilaBash
 * @date 2019/12/7
 * @implNote CTMModelRenderer
 */
public class CTMModelRenderer extends IModelRenderer {

    public static Supplier<Boolean> LOW_PRECISION = SupplierMemoizer.memoize(GTCEu::isSodiumRubidiumEmbeddiumLoaded);

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
