package com.gregtechceu.gtceu.api.pipenet.longdistance;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public interface ILDEndpoint extends ILDNetworkPart {

    /**
     * @return the current type of this endpoint (input, output or none)
     */
    IOType getIoType();

    /**
     * @param IOType new active type
     */
    void setIoType(IOType IOType);

    /**
     * @return true if this endpoint is considered a network input
     */
    default boolean isInput() {
        return getIoType() == IOType.INPUT;
    }

    /**
     * @return true if this endpoint is considered a network output
     */
    default boolean isOutput() {
        return getIoType() == IOType.OUTPUT;
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
    @NotNull
    Direction getFrontFacing();

    /**
     * @return the output facing
     */
    @NotNull
    Direction getOutputFacing();

    /**
     * @return the ld pipe type for this endpoint
     */
    @Override
    @NotNull
    LongDistancePipeType getPipeType();

    /**
     * @return pos in world
     */
    BlockPos getPos();

    Level getWorld();

    boolean isValid();

    @Nullable
    static ILDEndpoint tryGet(LevelAccessor world, BlockPos pos) {
        BlockEntity be = world.getChunk(pos).getBlockEntity(pos);
        if (be instanceof IMachineBlockEntity machine && machine.getMetaMachine() instanceof ILDEndpoint endpoint) {
            return endpoint;
        }
        return null;
    }

    enum IOType {
        NONE, INPUT, OUTPUT
    }
}