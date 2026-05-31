package com.gregtechceu.gtceu.client.model.quad.transform;

import com.gregtechceu.gtceu.client.model.quad.MutableQuadView;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraftforge.client.model.IQuadTransformer;

@FunctionalInterface
public interface QuadTransform extends IQuadTransformer {

    /**
     * Return false to filter out quads from rendering. When more than one transform is in effect, returning false
     * means unapplied transforms will not receive the quad.
     */
    boolean transform(MutableQuadView quad);

    @Override
    default void processInPlace(BakedQuad quad) {
        MutableQuadView quadView = MutableQuadView.getInstance().fromVanilla(quad.getVertices(), 0);
        this.transform(quadView);
        quadView.toVanilla(quad.getVertices(), 0);
    }
}
