package com.gregtechceu.gtceu.api.pipenet.longdistance;

import com.gregtechceu.gtceu.common.pipelike.fluidpipe.longdistance.LDFluidPipeType;
import com.gregtechceu.gtceu.common.pipelike.item.longdistance.LDItemPipeType;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * This class defines a long distance pipe type. This class MUST be a singleton class!
 */
public abstract class LongDistancePipeType {

    private static final Object2ObjectOpenHashMap<String, LongDistancePipeType> PIPE_TYPES = new Object2ObjectOpenHashMap<>();

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

    public static LDFluidPipeType fluid() {
        return LDFluidPipeType.INSTANCE;
    }

    public static LDItemPipeType item() {
        return LDItemPipeType.INSTANCE;
    }

    public static LongDistancePipeType getPipeType(String name) {
        return PIPE_TYPES.get(name);
    }

    public boolean isValidPart(ILDNetworkPart networkPart) {
        return networkPart != null && networkPart.getPipeType() == this;
    }

    /**
     * @return The minimum required distance (not pipe count) between two endpoints to work.
     */
    public int getMinLength() {
        return 0;
    }

    public boolean satisfiesMinLength(ILDEndpoint endpoint1, ILDEndpoint endpoint2) {
        BlockPos p = endpoint2.getPos();
        int minLength = getMinLength();
        return endpoint1 != endpoint2 && endpoint1.getPos().distSqr(p) >= minLength * minLength;
    }

    @NotNull
    public LongDistanceNetwork createNetwork(LongDistanceNetwork.WorldData worldData) {
        return new LongDistanceNetwork(this, worldData);
    }

    public final LongDistanceNetwork createNetwork(Level world) {
        return createNetwork(LongDistanceNetwork.WorldData.get(world));
    }

    public final String getName() {
        return name;
    }
}
