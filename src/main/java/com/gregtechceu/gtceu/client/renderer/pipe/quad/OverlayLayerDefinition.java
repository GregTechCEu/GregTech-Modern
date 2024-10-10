package com.gregtechceu.gtceu.client.renderer.pipe.quad;

import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

@FunctionalInterface
@OnlyIn(Dist.CLIENT)
public interface OverlayLayerDefinition {

    ImmutablePair<Vector3f, Vector3f> computeBox(@Nullable Direction facing, float x1, float y1, float z1, float x2,
                                                 float y2, float z2);
}
