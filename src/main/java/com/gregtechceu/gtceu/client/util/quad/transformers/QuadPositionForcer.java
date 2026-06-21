package com.gregtechceu.gtceu.client.util.quad.transformers;

import com.gregtechceu.gtceu.client.model.quad.MutableQuadView;
import com.gregtechceu.gtceu.client.model.quad.transform.QuadTransform;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

import org.joml.Vector3f;

/**
 * This transformer simply sets the vertices to be on the edges of the provided box.<br>
 * You probably want to Re-Interpolate the UV's, Color, and Lightmap. For that, see {@link QuadReInterpolator}.
 *
 * @see QuadReInterpolator
 * @author screret
 */
public class QuadPositionForcer implements QuadTransform {

    private final AABB bounds;

    private final Vector3f pos = new Vector3f();

    public QuadPositionForcer(AABB bounds) {
        this.bounds = bounds;
    }

    @Override
    public boolean transform(MutableQuadView quad) {
        Direction.Axis axis = quad.nominalFace().getAxis();

        setPosition(quad, this.bounds);

        // Check if the quad would be invisible and cull it.
        float x1 = quad.posByIndex(0, xCoord(axis));
        float x2 = quad.posByIndex(1, xCoord(axis));
        float x3 = quad.posByIndex(2, xCoord(axis));
        float x4 = quad.posByIndex(3, xCoord(axis));

        float y1 = quad.posByIndex(0, yCoord(axis));
        float y2 = quad.posByIndex(1, yCoord(axis));
        float y3 = quad.posByIndex(2, yCoord(axis));
        float y4 = quad.posByIndex(3, yCoord(axis));

        // These comparisons are safe as we are comparing clamped values.
        boolean flag1 = x1 == x2 && x2 == x3 && x3 == x4;
        boolean flag2 = y1 == y2 && y2 == y3 && y3 == y4;
        return !flag1 && !flag2;
    }

    private void setPosition(MutableQuadView quad, AABB bb) {
        float minX = (float) bounds.minX, minY = (float) bounds.minY, minZ = (float) bounds.minZ;
        float maxX = (float) bounds.maxX, maxY = (float) bounds.maxY, maxZ = (float) bounds.maxZ;
        float middleX = (minX + maxX) / 2f, middleY = (minY + maxY) / 2f, middleZ = (minZ + maxZ) / 2f;

        for (int i = 0; i < 4; i++) {
            quad.copyPos(i, pos);
            pos.set(middleX - pos.x() > Mth.EPSILON ? minX : maxX,
                    middleY - pos.y() > Mth.EPSILON ? minY : maxY,
                    middleZ - pos.z() > Mth.EPSILON ? minZ : maxZ);
            quad.pos(i, pos);
        }
    }

    /**
     * Gets the 2d X coordinate for the given axis.
     *
     * @param axis The axis. side >> 1
     * @return The x coordinate.
     */
    private static int xCoord(Direction.Axis axis) {
        if (axis == Direction.Axis.Y) {
            return 0;
        } else {
            return 2;
        }
    }

    /**
     * Gets the 2d Y coordinate for the given axis.
     *
     * @param axis The axis.
     * @return The y coordinate.
     */
    private static int yCoord(Direction.Axis axis) {
        if (axis != Direction.Axis.Y) {
            return 1;
        } else {
            return 2;
        }
    }
}
