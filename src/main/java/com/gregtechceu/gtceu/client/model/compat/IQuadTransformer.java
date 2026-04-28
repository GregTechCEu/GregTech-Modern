package com.gregtechceu.gtceu.client.model.compat;

import net.minecraft.client.resources.model.geometry.BakedQuad;

import java.util.List;

@FunctionalInterface
public interface IQuadTransformer {

    void processInPlace(List<BakedQuad> quads);
}
