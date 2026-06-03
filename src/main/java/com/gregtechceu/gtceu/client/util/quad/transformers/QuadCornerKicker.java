/*
 * This file is part of CodeChickenLib.
 * Copyright (c) 2018, covers1624, All rights reserved.
 *
 * CodeChickenLib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * CodeChickenLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with CodeChickenLib. If not, see <http://www.gnu.org/licenses/lgpl>.
 */
package com.gregtechceu.gtceu.client.util.quad.transformers;

import com.gregtechceu.gtceu.client.model.quad.MutableQuadView;
import com.gregtechceu.gtceu.client.model.quad.transform.QuadTransform;
import com.gregtechceu.gtceu.client.util.quad.GeometryHelper;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.Nullable;

import static net.minecraft.core.Direction.*;
import static net.minecraft.core.Direction.AxisDirection.*;

/**
 * This transformer is a little complicated.
 * Basically, a Facade / Cover can use this to 'kick' the edges of quads in to fix Z-fighting in the corners.
 * <p>
 * Use it by specifying
 * <ul>
 * <li>the side of the block you are on,</li>
 * <li>the bitmask for where the other Facades / Covers are,</li>
 * <li>the bounding box of the facade (NOT the hole piece),</li>
 * <li>and the thickness of your Facade / Cover (which is used as the kick amount).</li>
 * </ul>
 *
 * @author covers1624
 */
public class QuadCornerKicker implements QuadTransform {

    public static final QuadCornerKicker INSTANCE = new QuadCornerKicker();

    // Simple horizonal lookups.
    public static Direction[][] horizonals = new Direction[][] {
            // Around Y axis, NSWE.
            { NORTH, SOUTH, WEST, EAST },
            { NORTH, SOUTH, WEST, EAST },

            // Around Z axis, DUWE.
            { DOWN, UP, WEST, EAST },
            { DOWN, UP, WEST, EAST },

            // Around X axis, DUNS.
            { DOWN, UP, NORTH, SOUTH },
            { DOWN, UP, NORTH, SOUTH } };

    private Direction mySide;
    private int facadeMask;
    private AABB bounds;
    private double thickness;

    public QuadCornerKicker() {
        super();
    }

    /**
     * Set's the side this Facade / Cover is attached to.
     *
     * @param side The side.
     */
    public void setSide(Direction side) {
        this.mySide = side;
    }

    /**
     * Sets the bitmask of Facades / Covers in the block space.
     * This is as simple as {@code mask = (1 << side)}.
     *
     * @param mask The mask.
     */
    public void setFacadeMask(int mask) {
        this.facadeMask = mask;
    }

    /**
     * Sets the bounding box of the Facade / Cover, this should be the full box, not just a piece of the hole's 'ring'.
     *
     * @param bounds The bounding box.
     */
    public void setBounds(AABB bounds) {
        this.bounds = bounds;
    }

    /**
     * Sets the amount to kick the vertex in by, this is your facades thickness.
     *
     * @param thickness The thickness.
     */
    public void setThickness(double thickness) {
        this.thickness = thickness;
    }

    @Override
    public boolean transform(MutableQuadView quad) {
        Direction side = quad.nominalFace();
        if (side == this.mySide || side == this.mySide.getOpposite()) {
            return true;
        }
        for (Direction hoz : horizonals[this.mySide.get3DDataValue()]) {
            if (side == hoz || side == hoz.getOpposite() || (this.facadeMask & (1 << hoz.ordinal())) == 0) {
                continue;
            }
            Corner corner = Corner.fromSides(this.mySide.getOpposite(), side, hoz);
            for (int i = 0; i < 4; i++) {
                float x = quad.posByIndex(i, 0);
                float y = quad.posByIndex(i, 1);
                float z = quad.posByIndex(i, 2);
                if (Mth.equal(x, corner.pX(this.bounds)) && Mth.equal(y, corner.pY(this.bounds)) &&
                        Mth.equal(z, corner.pZ(this.bounds))) {
                    Vec3i normal = hoz.getNormal();
                    x -= normal.getX() * this.thickness;
                    y -= normal.getY() * this.thickness;
                    z -= normal.getZ() * this.thickness;
                    quad.pos(i, x, y, z);
                }
            }
        }

        return true;
    }

    @Override
    public void processInPlace(BakedQuad quad) {}

    public enum Corner {

        MIN_X_MIN_Y_MIN_Z(NEGATIVE, NEGATIVE, NEGATIVE),
        MIN_X_MIN_Y_MAX_Z(NEGATIVE, NEGATIVE, POSITIVE),
        MIN_X_MAX_Y_MIN_Z(NEGATIVE, POSITIVE, NEGATIVE),
        MIN_X_MAX_Y_MAX_Z(NEGATIVE, POSITIVE, POSITIVE),

        MAX_X_MIN_Y_MIN_Z(POSITIVE, NEGATIVE, NEGATIVE),
        MAX_X_MIN_Y_MAX_Z(POSITIVE, NEGATIVE, POSITIVE),
        MAX_X_MAX_Y_MIN_Z(POSITIVE, POSITIVE, NEGATIVE),
        MAX_X_MAX_Y_MAX_Z(POSITIVE, POSITIVE, POSITIVE);

        private final AxisDirection xAxis;
        private final AxisDirection yAxis;
        private final AxisDirection zAxis;

        public static final Corner[] VALUES = values();
        private static final int[] sideMask = { 0, 2, 0, 1, 0, 4 };

        Corner(AxisDirection xAxis, AxisDirection yAxis, AxisDirection zAxis) {
            this.xAxis = xAxis;
            this.yAxis = yAxis;
            this.zAxis = zAxis;
        }

        /**
         * Used to find what corner is at the 3 sides.
         * This method assumes you pass in the X axis side, Y axis side, and Z axis side,
         * it will NOT complain about an invalid side, you will just get garbage data.
         * This method also does not care what order the 3 axes are in.
         *
         * @param sideA Side one.
         * @param sideB Side two.
         * @param sideC Side three.
         * @return The corner at the 3 sides.
         */
        public static Corner fromSides(@Nullable Direction sideA,
                                       @Nullable Direction sideB,
                                       @Nullable Direction sideC) {
            int aIndex = GeometryHelper.toFaceIndex(sideA);
            int bIndex = GeometryHelper.toFaceIndex(sideB);
            int cIndex = GeometryHelper.toFaceIndex(sideC);

            // <3 Chicken-Bones.
            return Corner.VALUES[sideMask[aIndex] | sideMask[bIndex] | sideMask[cIndex]];
        }

        public float pX(AABB box) {
            return (float) (this.xAxis == NEGATIVE ? box.minX : box.maxX);
        }

        public float pY(AABB box) {
            return (float) (this.yAxis == NEGATIVE ? box.minY : box.maxY);
        }

        public float pZ(AABB box) {
            return (float) (this.zAxis == NEGATIVE ? box.minZ : box.maxZ);
        }
    }
}
