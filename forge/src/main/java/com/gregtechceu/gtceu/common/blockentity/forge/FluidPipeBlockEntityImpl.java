package com.gregtechceu.gtceu.common.blockentity.forge;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidPipeProperties;
import com.gregtechceu.gtceu.common.blockentity.FluidPipeBlockEntity;
import com.gregtechceu.gtceu.common.cover.FluidFilterCover;
import com.gregtechceu.gtceu.common.pipelike.fluidpipe.FluidPipeData;
import com.gregtechceu.gtceu.common.pipelike.fluidpipe.FluidPipeNet;
import com.gregtechceu.gtceu.common.pipelike.fluidpipe.PipeNetRoutePath;
import com.lowdragmc.lowdraglib.side.fluid.forge.FluidHelperImpl;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import lombok.Setter;
import lombok.val;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Predicate;

/**
 * @author KilaBash
 * @date 2023/3/11
 * @implNote FluidPipeBlockEntityImpl
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidPipeBlockEntityImpl extends FluidPipeBlockEntity {
    public FluidPipeBlockEntityImpl(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public static FluidPipeBlockEntity create(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        return new FluidPipeBlockEntityImpl(type, pos, blockState);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            var handler = getFluidHandler(side);
            if (handler != null) {
                return ForgeCapabilities.FLUID_HANDLER.orEmpty(cap, LazyOptional.of(() -> handler));
            }
        } else if (cap == GTCapability.CAPABILITY_COVERABLE) {
            return GTCapability.CAPABILITY_COVERABLE.orEmpty(cap, LazyOptional.of(this::getCoverContainer));
        } else if (cap == GTCapability.CAPABILITY_TOOLABLE) {
            return GTCapability.CAPABILITY_TOOLABLE.orEmpty(cap, LazyOptional.of(() -> this));
        }
        return super.getCapability(cap, side);
    }

    @Nullable
    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        if (side != null && isBlocked(side)) return null;
        if (isRemote()) { // for rendering? other mods may need it.
            return EmptyFluidHandler.INSTANCE;
        }
        var net = getFluidPipeNet();
        if (net != null) {
            return new FluidPipeHandler(net, this, side);
        }
        return null;
    }

    public static void onBlockEntityRegister(BlockEntityType<FluidPipeBlockEntity> cableBlockEntityBlockEntityType) {
    }

    class FluidPipeHandler implements IFluidHandler {

        private final FluidPipeNet net;
        private final FluidPipeBlockEntity pipe;
        private final List<PipeNetRoutePath> paths;
        @Nullable
        private final Direction side;
        @Setter
        private Predicate<FluidStack> filter = fluid -> true;

        public FluidPipeHandler(FluidPipeNet net, FluidPipeBlockEntity pipe, @Nullable Direction side) {
            this.net = Objects.requireNonNull(net);
            this.pipe = Objects.requireNonNull(pipe);
            this.paths = net.getNetData(pipe.getPipePos());
            this.side = side;
            if (side != null) {
                if (getCoverContainer().getCoverAtSide(side) instanceof FluidFilterCover cover) {
                    filter = f -> cover.getFluidFilter().test(FluidHelperImpl.toFluidStack(f));
                }
            }
        }

        @Override
        public int getTanks() {
            FluidPipeProperties properties = net.getNodeAt(pipe.getPipePos()).data.properties();
            return properties.getChannels();
        }

        @NotNull
        @Override
        public FluidStack getFluidInTank(int tank) {
            Fluid fluid = net.getFluid(pipe.getPipePos(), tank);

            if (fluid == null)
                return FluidStack.EMPTY;

            return new FluidStack(fluid, (int) net.getLastSecondTotalThroughput(pipe.getPipePos(), tank));
        }

        @Override
        public int getTankCapacity(int tank) {
            FluidPipeProperties properties = net.getNodeAt(pipe.getPipePos()).data.properties();

            if (tank < 0 || tank > properties.getChannels())
                return 0;

            long pipeThroughput = properties.getPlatformThroughput();
            return (int) (20 * pipeThroughput);
            // return (int) ((20 * pipeThroughput) - net.getLastSecondTotalThroughput(pipe.getPipePos(), 0));
        }

        @Override
        public @NotNull FluidStack drain(FluidStack fluidStack, FluidAction fluidAction) {
            return FluidStack.EMPTY;
        }

        @Override
        public @NotNull FluidStack drain(int i, FluidAction fluidAction) {
            return FluidStack.EMPTY;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return filter.test(stack);
        }

        /**
         * check path. how much fluid can be transferred.
         * @return amount
         */
        private int checkPathAvailable(FluidStack stack, PipeNetRoutePath routePath, Map<BlockPos, Set<Fluid>> simulateChannelUsed, Object2LongMap<BlockPos> simulateThroughputUsed) {
            if (stack.isEmpty()) return 0;
            var amount = stack.getAmount();
            for (Pair<BlockPos, FluidPipeData> node : routePath.getPath()) {
                var properties = node.getB().properties();
                var pos = node.getA();
                if (!properties.test(FluidHelperImpl.toFluidStack(stack))) {
                    return 0;
                }

                var maxChannel = properties.getChannels() - 1;

                var channel = net.getChannel(pos, stack.getFluid());
                var simulateChannels = simulateChannelUsed.getOrDefault(pos, Collections.emptySet());
                if ((channel == -1 || channel > maxChannel) && !simulateChannels.contains(stack.getFluid())) {
                    channel = net.useChannel(pos, stack.getFluid());
                    if (channel == -1 || channel > maxChannel) {
                        return 0;
                    }
                }

                long platformThroughputPerSecond = 20 * properties.getPlatformThroughput();
                var left = platformThroughputPerSecond - net.getLastSecondTotalThroughput(pos, channel) - simulateThroughputUsed.getOrDefault(pos, 0);
                amount = (int) Math.min(amount, left);
                if (amount <= 0) return 0;
            }
            return amount;
        }

        @Override
        public int fill(FluidStack resource, FluidAction fluidAction) {
            if (resource.isEmpty() || !filter.test(resource)) return 0;
            var left = resource.copy();
            Map<BlockPos, Set<Fluid>> simulateChannelUsed = new HashMap<>();
            Object2LongMap<BlockPos> simulateThroughputUsed = new Object2LongOpenHashMap<>();
            for (PipeNetRoutePath path : paths) {
                if (Objects.equals(pipe.getPipePos(), path.getPipePos()) && (side == path.getFaceToHandler() || side == null)) {
                    //Do not insert into source handler
                    continue;
                }

                val blockEntity = pipe.getPipeLevel().getBlockEntity(path.getHandlerPos());
                if (blockEntity != null) {
                    var handler = blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, path.getFaceToHandler().getOpposite()).resolve().orElse(null);
                    if (handler != null) {
                        var coverable = GTCapabilityHelper.getCoverable(net.getLevel(), path.getPipePos(), null);
                        if (coverable != null) {
                            if (coverable.getCoverAtSide(path.getFaceToHandler()) instanceof FluidFilterCover cover && !cover.getFluidFilter().test(FluidHelperImpl.toFluidStack(resource))) {
                                return 0;
                            }
                        }
                        var accepted = checkPathAvailable(left, path, simulateChannelUsed, simulateThroughputUsed);
                        if (accepted <= 0) continue;
                        var copied = left.copy();
                        copied.setAmount(accepted);
                        var filled = handler.fill(copied, fluidAction);
                        if (filled > 0) { // occupy capacity + channel
                            for (Pair<BlockPos, FluidPipeData> node : path.getPath()) {
                                var pos = node.getA();
                                if (fluidAction.simulate()) {
                                    simulateThroughputUsed.put(pos, simulateThroughputUsed.getOrDefault(pos, 0) + filled);
                                    simulateChannelUsed.computeIfAbsent(pos, p -> new HashSet<>()).add(resource.getFluid());
                                } else {
                                    int channel = net.getChannel(pos, resource.getFluid());
                                    if (channel != -1)
                                        net.useThroughput(pos, channel, filled);
                                }
                            }

                        }
                        left.shrink(filled);
                        if (left.isEmpty()) {
                            break;
                        }
                    }
                }
            }
            return resource.getAmount() - left.getAmount();
        }

    }
}
