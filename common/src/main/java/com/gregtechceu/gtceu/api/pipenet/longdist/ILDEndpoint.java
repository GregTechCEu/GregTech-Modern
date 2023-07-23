package com.gregtechceu.gtceu.api.pipenet.longdist;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public interface ILDEndpoint {

    /**
     * @return the current type of this endpoint (input, output or none)
     */
    Type getType();

    /**
     * @param type new active type
     */
    void setType(Type type);

    /**
     * @return true if this endpoint is considered a network input
     */
    default boolean isInput() {
        return getType() == Type.INPUT;
    }

    /**
     * @return true if this endpoint is considered a network output
     */
    default boolean isOutput() {
        return getType() == Type.OUTPUT;
    }

    /**
     * @return the currently linked endpoint or null
     */
    @Nullable
    ILDEndpoint getLink();

    /**
     * removes the linked endpoint if there is any
     */
    void invalidateLink();

    /**
     * @return the front facing, usually the input face
     */
    EnumFacing getFrontFacing();

    /**
     * @return the output facing
     */
    EnumFacing getOutputFacing();

    /**
     * @return the ld pipe type for this endpoint
     */
    LongDistancePipeType getPipeType();

    /**
     * @return pos in world
     */
    BlockPos getPos();

    static ILDEndpoint tryGet(World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof IGregTechTileEntity) {
            MetaTileEntity mte = ((IGregTechTileEntity) te).getMetaTileEntity();
            if (mte instanceof ILDEndpoint) {
                return (ILDEndpoint) mte;
            }
        }
        return null;
    }

    enum Type {
        NONE, INPUT, OUTPUT
    }
}
