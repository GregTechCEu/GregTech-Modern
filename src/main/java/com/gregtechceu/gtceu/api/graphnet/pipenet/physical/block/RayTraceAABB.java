package com.gregtechceu.gtceu.api.graphnet.pipenet.physical.block;

import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/**
 * A ray trace that additionally contains the {@link AxisAlignedBB} it hit.
 */
public class RayTraceAABB extends RayTraceResult {

    private final AxisAlignedBB bb;

    public RayTraceAABB(Type typeIn, Vec3d hitVecIn, Direction sideHitIn, BlockPos blockPosIn, AxisAlignedBB bb) {
        super(typeIn, hitVecIn, sideHitIn, blockPosIn);
        this.bb = bb;
    }

    @Contract("null, _ -> null; !null, _ -> new")
    public static RayTraceAABB of(@Nullable RayTraceResult result, AxisAlignedBB bb) {
        if (result == null) return null;
        if (result.typeOfHit == Type.ENTITY)
            throw new IllegalArgumentException("Cannot create a RayTraceAABB for an entity hit!");
        return new RayTraceAABB(result.typeOfHit, result.hitVec, result.sideHit, result.getBlockPos(), bb);
    }

    public AxisAlignedBB getBB() {
        return bb;
    }
}
