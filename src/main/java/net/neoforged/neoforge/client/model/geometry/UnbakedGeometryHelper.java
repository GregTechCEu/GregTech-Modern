package net.neoforged.neoforge.client.model.geometry;

import com.gregtechceu.gtceu.client.model.compat.IQuadTransformer;
import com.gregtechceu.gtceu.client.model.compat.QuadTransformers;

import net.neoforged.neoforge.client.model.quad.QuadTransforms;

import com.mojang.math.Transformation;

public final class UnbakedGeometryHelper {

    private UnbakedGeometryHelper() {}

    public static IQuadTransformer applyRootTransform(net.minecraft.client.renderer.block.dispatch.ModelState modelState,
                                                      Transformation rootTransform) {
        if (rootTransform.isIdentity()) {
            return QuadTransformers.empty();
        }
        return quads -> quads.replaceAll(quad -> QuadTransforms.applyTransformation(quad, rootTransform));
    }
}
