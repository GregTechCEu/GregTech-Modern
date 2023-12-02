package com.gregtechceu.gtceu.client.renderer.block;

import com.google.common.base.Suppliers;
import com.gregtechceu.gtceu.GTCEu;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.client.renderer.impl.IModelRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

/**
 * @author KilaBash
 * @date 2019/12/7
 * @implNote CTMModelRenderer
 */
public class CTMModelRenderer extends IModelRenderer {
    public static Supplier<Boolean> LOW_PRECISION = Suppliers.memoize(GTCEu::isSodiumRubidiumEmbeddiumLoaded);
    public CTMModelRenderer(ResourceLocation modelLocation) {
        super(modelLocation);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean reBakeCustomQuads() {
        return true;
    }

    @Override
    public float reBakeCustomQuadsOffset() {
        return LOW_PRECISION.get() ? 0.008f : 0.002f;
    }
}
