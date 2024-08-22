package com.gregtechceu.gtceu.common.pipelike.net.optical;

import com.gregtechceu.gtceu.api.capability.data.IDataAccess;
import com.gregtechceu.gtceu.api.capability.data.query.DataAccessFormat;
import com.gregtechceu.gtceu.api.capability.data.query.DataQueryObject;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.graphnet.pipenet.BasicWorldPipeNetPath;
import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNet;
import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNetNode;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.IPipeCapabilityObject;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.tile.PipeBlockEntity;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.IPredicateTestObject;
import com.gregtechceu.gtceu.common.pipelike.block.optical.IOpticalTransferController;
import com.gregtechceu.gtceu.common.pipelike.net.SlowActiveWalker;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.Platform;
import lombok.Setter;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Set;

public class DataCapabilityObject implements IPipeCapabilityObject, IDataAccess {

    private final WorldPipeNet net;

    @Setter
    private @Nullable PipeBlockEntity tile;

    private final Set<DataQueryObject> recentQueries = GTUtil.createWeakHashSet();

    public <N extends WorldPipeNet & BasicWorldPipeNetPath.Provider> DataCapabilityObject(@NotNull N net) {
        this.net = net;
    }

    private BasicWorldPipeNetPath.Provider getProvider() {
        return (BasicWorldPipeNetPath.Provider) net;
    }

    @Override
    public boolean accessData(@NotNull DataQueryObject queryObject) {
        if (tile == null) return false;
        // if the add call fails (because the object already exists in the set) then do not recurse
        if (!recentQueries.add(queryObject)) return false;

        for (Iterator<BasicWorldPipeNetPath> it = getPaths(); it.hasNext();) {
            BasicWorldPipeNetPath path = it.next();
            WorldPipeNetNode destination = path.getTargetNode();
            for (var capability : destination.getBlockEntity().getTargetsWithCapabilities(destination).entrySet()) {
                IDataAccess access = capability.getValue()
                        .getCapability(GTCapability.CAPABILITY_DATA_ACCESS,
                                capability.getKey().getOpposite()).resolve().orElse(null);
                if (access != null) {
                    queryObject.setShouldTriggerWalker(false);
                    boolean cancelled = IOpticalTransferController.CONTROL
                            .get(destination.getBlockEntity().getCoverHolder()
                                    .getCoverAtSide(capability.getKey()))
                            .queryHandler(queryObject, access);
                    if (queryObject.shouldTriggerWalker()) {
                        SlowActiveWalker.dispatch(tile.getLevel(), path, 1);
                    }
                    if (cancelled) return true;
                }
            }
        }
        return false;
    }

    @Override
    public @NotNull DataAccessFormat getFormat() {
        return DataAccessFormat.UNIVERSAL;
    }

    private Iterator<BasicWorldPipeNetPath> getPaths() {
        assert tile != null;
        long tick = Platform.getMinecraftServer().getTickCount();
        return getProvider().getPaths(net.getNode(tile.getBlockPos()), IPredicateTestObject.INSTANCE, null, tick);
    }

    @Override
    public Capability<?>[] getCapabilities() {
        return WorldOpticalNet.CAPABILITIES;
    }

    @Override
    public <T> LazyOptional<T> getCapabilityForSide(Capability<T> capability, @Nullable Direction facing) {
        if (capability == GTCapability.CAPABILITY_DATA_ACCESS) {
            return GTCapability.CAPABILITY_DATA_ACCESS.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return null;
    }
}
