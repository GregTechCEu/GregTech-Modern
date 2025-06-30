package com.gregtechceu.gtceu.client.util;

import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.util.Mth;

import com.google.common.base.Preconditions;
import com.mojang.math.Transformation;
import org.joml.Quaternionf;

/**
 * This code is from
 * <a href=
 * "https://github.com/AppliedEnergistics/Applied-Energistics-2/blob/forge/1.20.1/src/main/java/appeng/hooks/BlockstateDefinitionHook.java">Applied
 * Energistics 2</a>,
 * licensed as LGPL 3.0.
 */
public class VariantRotationHelpers {

    private static final Transformation[] TRANSFORMS = createTransformations();

    private static Transformation[] createTransformations() {
        var result = new Transformation[4 * 4 * 4];

        for (var xRot = 0; xRot < 360; xRot += 90) {
            for (var yRot = 0; yRot < 360; yRot += 90) {
                // Reuse existing transform from Vanilla
                result[indexFromAngles(xRot, yRot, 0)] = BlockModelRotation.by(xRot, yRot).getRotation();

                for (var zRot = 90; zRot < 360; zRot += 90) {
                    var idx = indexFromAngles(xRot, yRot, zRot);

                    // NOTE: Mojang's block model rotation rotates in the opposite direction
                    var quaternion = new Quaternionf().rotateYXZ(
                            -yRot * Mth.DEG_TO_RAD,
                            -xRot * Mth.DEG_TO_RAD,
                            -zRot * Mth.DEG_TO_RAD);

                    result[idx] = new Transformation(null, quaternion, null, null);
                }
            }
        }

        return result;
    }

    private VariantRotationHelpers() {}

    public static Variant rotateVariant(Variant variant, int xRot, int yRot, int zRot) {
        return new Variant(
                variant.getModelLocation(),
                getRotationTransform(xRot, yRot, zRot),
                variant.isUvLocked(),
                variant.getWeight());
    }

    public static Transformation getRotationTransform(int xRot, int yRot, int zRot) {
        return TRANSFORMS[indexFromAngles(xRot, yRot, zRot)];
    }

    private static int indexFromAngles(int xRot, int yRot, int zRot) {
        Preconditions.checkArgument(xRot >= 0 && xRot < 360 && xRot % 90 == 0);
        Preconditions.checkArgument(yRot >= 0 && yRot < 360 && yRot % 90 == 0);
        Preconditions.checkArgument(zRot >= 0 && zRot < 360 && zRot % 90 == 0);
        return xRot / 90 * 16 + yRot / 90 * 4 + zRot / 90;
    }
}
