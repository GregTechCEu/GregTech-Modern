package com.gregtechceu.gtceu.core.util.extensions;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.*;

@SuppressWarnings("unused")
public interface VertexConsumerExt {

    private VertexConsumer self() {
        return (VertexConsumer) this;
    }

    default VertexConsumer gtceu$addVertex(Matrix4f poseMatrix, Vector3fc pos) {
        pos = poseMatrix.transformPosition(pos, ExtensionHelpers.scratch.get());
        return self().addVertex(pos.x(), pos.y(), pos.z());
    }

    default VertexConsumer gtceu$setNormal(Matrix3f normalMatrix, Vector3fc normal) {
        normal = normalMatrix.transform(normal, ExtensionHelpers.scratch.get());
        return self().setNormal(normal.x(), normal.y(), normal.z());
    }
}
