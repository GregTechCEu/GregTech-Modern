package com.gregtechceu.gtceu.common.pipelike.net.laser;

import com.gregtechceu.gtceu.api.capability.ILaserRelay;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.graphnet.pipenet.BasicWorldPipeNetPath;
import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNet;
import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNetNode;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.IPipeCapabilityObject;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.tile.PipeBlockEntity;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.IPredicateTestObject;
import com.gregtechceu.gtceu.common.pipelike.net.SlowActiveWalker;

import com.lowdragmc.lowdraglib.Platform;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class LaserCapabilityObject implements IPipeCapabilityObject, ILaserRelay {

    private final WorldPipeNet net;
    @Setter
    private @Nullable PipeBlockEntity tile;

    private boolean transmitting;

    public <N extends WorldPipeNet & BasicWorldPipeNetPath.Provider> LaserCapabilityObject(@NotNull N net) {
        this.net = net;
    }

    private BasicWorldPipeNetPath.Provider getProvider() {
        return (BasicWorldPipeNetPath.Provider) net;
    }

    private Iterator<BasicWorldPipeNetPath> getPaths() {
        assert tile != null;
        long tick = Platform.getMinecraftServer().getTickCount();
        return getProvider().getPaths(net.getNode(tile.getBlockPos()), IPredicateTestObject.INSTANCE, null, tick);
    }

    @Override
    public long receiveLaser(long laserVoltage, long laserAmperage) {
        if (tile == null || this.transmitting) return 0;
        this.transmitting = true;

        long available = laserAmperage;
        for (Iterator<BasicWorldPipeNetPath> it = getPaths(); it.hasNext();) {
            BasicWorldPipeNetPath path = it.next();
            WorldPipeNetNode destination = path.getTargetNode();
            for (var capability : destination.getBlockEntity().getTargetsWithCapabilities(destination).entrySet()) {
                ILaserRelay laser = capability.getValue()
                        .getCapability(GTCapability.CAPABILITY_LASER, capability.getKey().getOpposite()).resolve()
                        .orElse(null);
                if (laser != null) {
                    long transmitted = ILaserTransferController.CONTROL
                            .get(destination.getBlockEntity().getCoverHolder()
                                    .getCoverAtSide(capability.getKey()))
                            .insertToHandler(laserVoltage, laserAmperage, laser);
                    if (transmitted > 0) {
                        SlowActiveWalker.dispatch(tile.getLevel(), path, 1, 2, 2);
                        available -= transmitted;
                        if (available <= 0) {
                            this.transmitting = false;
                            return laserAmperage;
                        }
                    }
                }
            }
        }
        this.transmitting = false;

        return laserAmperage - available;
    }

    @Override
    public Capability<?>[] getCapabilities() {
        return WorldLaserNet.CAPABILITIES;
    }

    @Override
    public <T> LazyOptional<T> getCapabilityForSide(Capability<T> capability, @Nullable Direction facing) {
        if (capability == GTCapability.CAPABILITY_LASER) {
            return GTCapability.CAPABILITY_LASER.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return null;
    }
}
