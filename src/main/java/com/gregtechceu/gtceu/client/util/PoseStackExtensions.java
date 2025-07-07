package com.gregtechceu.gtceu.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4fc;

public class PoseStackExtensions {

    public static void mulPoseMatrix(PoseStack self, Matrix4fc matrix) {
        self.last().pose().mul(matrix);
    }
}
