package com.gregtechceu.gtceu.api.pipenet.longdistance;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;

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
    Direction getFrontFacing();

    /**
     * @return the output facing
     */
    Direction getOutputFacing();

    /**
     * @return the ld pipe type for this endpoint
     */
    LongDistancePipeType getPipeType();

    /**
     * @return pos in world
     */
    BlockPos getPos();

    static ILDEndpoint tryGet(LevelAccessor world, BlockPos pos) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof IMachineBlockEntity iMachineBlock) {
            MetaMachine mte = iMachineBlock.getMetaMachine();
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