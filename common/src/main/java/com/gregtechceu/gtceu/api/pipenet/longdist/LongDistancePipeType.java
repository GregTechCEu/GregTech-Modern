package com.gregtechceu.gtceu.api.pipenet.longdist;

import gregtech.common.pipelike.fluidpipe.longdistance.LDFluidPipeType;
import gregtech.common.pipelike.itempipe.longdistance.LDItemPipeType;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * This class defines a long distance pipe type. This class MUST be a singleton class!
 */
public abstract class LongDistancePipeType {

    private static final Object2ObjectOpenHashMap<String, LongDistancePipeType> PIPE_TYPES = new Object2ObjectOpenHashMap<>();

    private static LDFluidPipeType FLUID;
    private static LDItemPipeType ITEM;

    private final String name;

    protected LongDistancePipeType(String name) {
        this.name = Objects.requireNonNull(name);
        if (PIPE_TYPES.containsKey(name)) {
            throw new IllegalArgumentException("Pipe Type with name " + name + " already exists!");
        }
        for (LongDistancePipeType pipeType : PIPE_TYPES.values()) {
            if (this.getClass() == pipeType.getClass()) {
                throw new IllegalStateException("Duplicate Pipe Type " + name + " and " + pipeType.name);
            }
        }
        PIPE_TYPES.put(name, this);
    }

    public static void init() {
        FLUID = LDFluidPipeType.INSTANCE;
        ITEM = LDItemPipeType.INSTANCE;
    }

    public static LDFluidPipeType fluid() {
        return FLUID;
    }

    public static LDItemPipeType item() {
        return ITEM;
    }

    public static LongDistancePipeType getPipeType(String name) {
        return PIPE_TYPES.get(name);
    }

    /**
     * Checks if the given block state is a valid ld pipe block for this type
     *
     * @param blockState potential ld pipe block
     * @return if the given block state is a valid ld pipe block for this type
     */
    public abstract boolean isValidBlock(IBlockState blockState);

    /**
     * Checks if the given endpoint is a valid endpoint for this type
     *
     * @param endpoint potential endpoint
     * @return if the given endpoint is a valid endpoint for this type
     */
    public abstract boolean isValidEndpoint(ILDEndpoint endpoint);

    /**
     * @return The minimum required distance (not pipe count between endpoints) between to endpoints to work.
     */
    public int getMinLength() {
        return 0;
    }

    public boolean satisfiesMinLength(ILDEndpoint endpoint1, ILDEndpoint endpoint2) {
        BlockPos p = endpoint2.getPos();
        return endpoint1 != endpoint2 && endpoint1.getPos().getDistance(p.getX(), p.getY(), p.getZ()) >= getMinLength();
    }

    @Nonnull
    public LongDistanceNetwork createNetwork(LongDistanceNetwork.WorldData worldData) {
        return new LongDistanceNetwork(this, worldData);
    }

    public final LongDistanceNetwork createNetwork(World world) {
        return createNetwork(LongDistanceNetwork.WorldData.get(world));
    }

    public final String getName() {
        return name;
    }
}
