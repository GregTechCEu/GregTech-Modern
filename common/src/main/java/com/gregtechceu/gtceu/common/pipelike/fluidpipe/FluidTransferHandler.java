package com.gregtechceu.gtceu.common.pipelike.fluidpipe;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.common.blockentity.FluidPipeBlockEntity;
import com.gregtechceu.gtceu.common.cover.FluidFilterCover;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;

import java.util.*;
import java.util.function.Predicate;

/**
 * @author KilaBash
 * @date 2023/3/11
 * @implNote FluidTransferHandler
 */
public class FluidTransferHandler implements IFluidTransfer {

    private final FluidPipeNet net;
    private final FluidPipeBlockEntity pipe;
    private final List<PipeNetRoutePath> paths;
    @Nullable
    private final Direction side;
    @Setter
    private Predicate<FluidStack> filter = fluid -> true;

    public FluidTransferHandler(FluidPipeNet net, FluidPipeBlockEntity pipe, @Nullable Direction side) {
        this.net = Objects.requireNonNull(net);
        this.pipe = Objects.requireNonNull(pipe);
        this.paths = net.getNetData(pipe.getPipePos());
        this.side = side;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @NotNull
    @Override
    public FluidStack getFluidInTank(int tank) {
        return FluidStack.empty();
    }

    @Override
    public long getTankCapacity(int tank) {
        return tank == 0 ? net.getNodeAt(pipe.getPipePos()).data.properties.getPlatformThroughput() - net.getThroughputUsed(pipe.getPipePos()) : 0;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return filter.test(stack);
    }

    /**
     * check path. how much fluid can be transferred.
     * @return amount
     */
    private long checkPathAvailable(FluidStack stack, PipeNetRoutePath routePath, Map<BlockPos, Set<Fluid>> simulateChannelUsed, Object2LongMap<BlockPos> simulateThroughputUsed) {
        if (stack.isEmpty()) return 0;
        var amount = stack.getAmount();
        for (Pair<BlockPos, FluidPipeData> node : routePath.getPath()) {
            var properties = node.getB().properties;
            var pos = node.getA();
            if (!properties.acceptFluid(stack)) {
                return 0;
            }
            var channels = net.getChannelUsed(pos);
            var simulateChannels = simulateChannelUsed.getOrDefault(pos, Collections.emptySet());

            if (!channels.contains(stack.getFluid()) && !simulateChannels.contains(stack.getFluid())
                    && (channels.size() + simulateChannels.size()) >= properties.getChannels()) return 0;

            var left = properties.getPlatformThroughput() - net.getThroughputUsed(pos) - simulateThroughputUsed.getOrDefault(pos, 0);
            amount = Math.min(amount, left);
            if (amount == 0) return 0;
        }
        return amount;
    }

    @Override
    public long fill(FluidStack resource, boolean simulate) {
        if (resource.isEmpty() || !filter.test(resource)) return 0;
        var left = resource.copy();
        Map<BlockPos, Set<Fluid>> simulateChannelUsed = new HashMap<>();
        Object2LongMap<BlockPos> simulateThroughputUsed = new Object2LongOpenHashMap<>();
        for (PipeNetRoutePath path : paths) {
            if (Objects.equals(pipe.getPipePos(), path.getPipePos()) && (side == path.getFaceToHandler() || side == null)) {
                //Do not insert into source handler
                continue;
            }
            var handler = path.getHandler(pipe.getPipeLevel());
            if (handler != null) {
                var coverable = GTCapabilityHelper.getCoverable(net.getLevel(), path.getPipePos(), null);
                if (coverable != null) {
                    if (coverable.getCoverAtSide(path.getFaceToHandler()) instanceof FluidFilterCover cover && !cover.getFluidFilter().test(resource)) {
                        return 0;
                    }
                }
                var accepted = checkPathAvailable(left, path, simulateChannelUsed, simulateThroughputUsed);
                if (accepted <= 0) continue;
                var copied = left.copy();
                copied.setAmount(accepted);
                var filled = handler.fill(copied, simulate);
                if (filled > 0) { // occupy capacity + channel
                    for (Pair<BlockPos, FluidPipeData> node : path.getPath()) {
                        var pos = node.getA();
                        if (simulate) {
                            simulateThroughputUsed.put(pos, simulateThroughputUsed.getOrDefault(pos, 0) + filled);
                            simulateChannelUsed.computeIfAbsent(pos, p -> new HashSet<>()).add(resource.getFluid());
                        } else {
                            net.useThroughput(pos, filled);
                            net.useChannel(pos, resource.getFluid());
                        }
                    }

                }
                left.shrink(filled);
                if (left.isEmpty()) {
                    break;
                }
            }
        }
        return resource.getAmount() - left.getAmount();
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, boolean simulate) {
        return FluidStack.empty();
    }

    @NotNull
    @Override
    public FluidStack drain(long maxDrain, boolean simulate) {
        return FluidStack.empty();
    }

    @Override
    public boolean supportsFill(int i) {
        return true;
    }

    @Override
    public boolean supportsDrain(int i) {
        return false;
    }
}
