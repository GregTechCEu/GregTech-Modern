package com.gregtechceu.gtceu.api.graphnet.pipenet.physical;

import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.tile.PipeBlockEntity;
import com.gregtechceu.gtceu.client.renderer.pipe.AbstractPipeModel;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface IPipeStructure extends StringRepresentable {

    /**
     * Used as reference for misc things, e.g. rendering the backing of a cover.
     * 
     * @return render thickness
     */
    float getRenderThickness();

    boolean isPaintable();

    AbstractPipeModel<?> getModel();

    /**
     * Allows for controlling what sides can be connected to based on current connections,
     * such as in the case of optical and laser pipes.
     */
    default boolean canConnectTo(Direction side, byte connectionMask) {
        return true;
    }

    @Contract("_ -> new")
    default VoxelShape getPipeBoxes(@NotNull PipeBlockEntity tileContext) {
        VoxelShape pipeBoxes = Shapes.empty();
        float thickness = getRenderThickness();
        if ((tileContext.getConnectionMask() & 63) < 63) {
            pipeBoxes = Shapes.or(pipeBoxes, getSideBox(null, thickness));
        }
        for (Direction facing : GTUtil.DIRECTIONS) {
            if (tileContext.isConnected(facing))
                pipeBoxes = Shapes.or(pipeBoxes, getSideBox(facing, thickness));
        }
        return pipeBoxes;
    }

    static VoxelShape getSideBox(Direction side, float thickness) {
        float min = (1.0f - thickness) / 2.0f, max = min + thickness;
        float faceMin = 0f, faceMax = 1f;

        if (side == null)
            return Shapes.box(min, min, min, max, max, max);
        return switch (side) {
            case WEST -> Shapes.box(faceMin, min, min, min, max, max);
            case EAST -> Shapes.box(max, min, min, faceMax, max, max);
            case NORTH -> Shapes.box(min, min, faceMin, max, max, min);
            case SOUTH -> Shapes.box(min, min, max, max, max, faceMax);
            case UP -> Shapes.box(min, max, min, max, faceMax, max);
            case DOWN -> Shapes.box(min, faceMin, min, max, min, max);
        };
    }
}
