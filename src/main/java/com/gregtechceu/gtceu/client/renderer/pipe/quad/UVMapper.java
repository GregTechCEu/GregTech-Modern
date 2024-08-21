package com.gregtechceu.gtceu.client.renderer.pipe.quad;

import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.apache.commons.lang3.tuple.Pair;
import org.joml.Vector3f;

@FunctionalInterface
@OnlyIn(Dist.CLIENT)
public interface UVMapper {

    static UVMapper standard(int rot) {
        return (normal, box) -> {
            Vector3f small = box.getLeft();
            Vector3f large = box.getRight();
            return switch (normal.getAxis()) {
                case X -> new BlockFaceUV(new float[] { small.y, large.z, large.y, small.z }, rot);
                case Y -> new BlockFaceUV(new float[] { small.x, large.z, large.x, small.z }, rot);
                case Z -> new BlockFaceUV(new float[] { small.x, large.y, large.x, small.y }, rot);
            };
        };
    }

    BlockFaceUV map(Direction normal, Pair<Vector3f, Vector3f> box);
}
