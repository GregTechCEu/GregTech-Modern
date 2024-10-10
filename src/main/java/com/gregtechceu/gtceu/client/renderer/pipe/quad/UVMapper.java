package com.gregtechceu.gtceu.client.renderer.pipe.quad;

import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Vector3f;

@FunctionalInterface
@OnlyIn(Dist.CLIENT)
public interface UVMapper {

    Int2ObjectArrayMap<UVMapper> STANDARD = new Int2ObjectArrayMap<>(4);
    Int2ObjectArrayMap<UVMapper> FLIPPED = new Int2ObjectArrayMap<>(4);

    static UVMapper standard(int rot) {
        return FLIPPED.computeIfAbsent(rot, (r) -> (normal, box) -> {
            Vector3f small = box.getLeft();
            Vector3f large = box.getRight();
            return switch (normal.getAxis()) {
                case X -> new BlockFaceUV(new float[] { small.z, 16 - large.y, large.z, 16 - small.y }, r);
                case Y -> new BlockFaceUV(new float[] { small.x, 16 - large.z, large.x, 16 - small.z }, r);
                case Z -> new BlockFaceUV(new float[] { small.x, 16 - large.y, large.x, 16 - small.y }, r);
            };
        });
    }

    static UVMapper flipped(int rot) {
        return STANDARD.computeIfAbsent(rot, (r) -> (normal, box) -> {
            Vector3f small = box.getLeft();
            Vector3f large = box.getRight();
            return switch (normal.getAxis()) {
                case X -> new BlockFaceUV(new float[] { 16 - large.z, small.y, 16 - small.z, large.y }, r);
                case Y -> new BlockFaceUV(new float[] { 16 - large.x, small.z, 16 - small.x, large.z }, r);
                case Z -> new BlockFaceUV(new float[] { 16 - large.x, small.y, 16 - small.x, large.y }, r);
            };
        });
    }

    BlockFaceUV map(Direction normal, Pair<Vector3f, Vector3f> box);
}
