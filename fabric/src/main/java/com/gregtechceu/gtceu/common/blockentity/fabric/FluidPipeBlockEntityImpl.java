package com.gregtechceu.gtceu.common.blockentity.fabric;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.fabric.GTCapability;
import com.gregtechceu.gtceu.common.blockentity.FluidPipeBlockEntity;
import com.gregtechceu.gtceu.common.cover.FluidFilterCover;
import com.gregtechceu.gtceu.common.pipelike.fluidpipe.FluidPipeData;
import com.gregtechceu.gtceu.common.pipelike.fluidpipe.FluidPipeNet;
import com.gregtechceu.gtceu.common.pipelike.fluidpipe.PipeNetRoutePath;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.fabric.FluidHelperImpl;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import lombok.Setter;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import oshi.util.tuples.Pair;

import javax.annotation.Nullable;
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
@SuppressWarnings("UnstableApiUsage")
public class FluidPipeBlockEntityImpl extends FluidPipeBlockEntity {

    public FluidPipeBlockEntityImpl(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public static FluidPipeBlockEntity create(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        return new FluidPipeBlockEntityImpl(type, pos, blockState);
    }

    public static void onBlockEntityRegister(BlockEntityType<FluidPipeBlockEntity> type) {
        FluidStorage.SIDED.registerForBlockEntity(FluidPipeBlockEntityImpl::getFluidStorage, type);
        GTCapability.CAPABILITY_COVERABLE.registerForBlockEntity((blockEntity, direction) -> blockEntity.getCoverContainer(), type);
        GTCapability.CAPABILITY_TOOLABLE.registerForBlockEntity((blockEntity, direction) -> blockEntity, type);
    }

    @Nullable
    private static Storage<FluidVariant> getFluidStorage(FluidPipeBlockEntity blockEntity, Direction direction) {
        return ((FluidPipeBlockEntityImpl)blockEntity).getFluidStorage(direction);
    }

    @Nullable
    public Storage<FluidVariant> getFluidStorage(@Nullable Direction side) {
        if (side != null && isBlocked(side)) return null;
        if (isRemote()) { // for rendering? other mods may need it.
            return Storage.empty();
        }
        var net = getFluidPipeNet();
        if (net != null) {
            return new FluidVariantStorage(net, this, side);
        }
        return null;
    }

    class FluidVariantStorage extends SnapshotParticipant<FluidPipeNet.Snapshot> implements Storage<FluidVariant> {

        private final FluidPipeNet net;
        private final FluidPipeBlockEntity pipe;
        private final List<PipeNetRoutePath> paths;
        @org.jetbrains.annotations.Nullable
        private final Direction side;
        @Setter
        private Predicate<FluidStack> filter = fluid -> true;
        Map<BlockPos, Set<Fluid>> simulateChannelUsed = new HashMap<>();
        Object2LongMap<BlockPos> simulateThroughputUsed = new Object2LongOpenHashMap<>();
        boolean isOuterCallbackAdded = false;

        public FluidVariantStorage(FluidPipeNet net, FluidPipeBlockEntity pipe, @Nullable Direction side) {
            this.net = Objects.requireNonNull(net);
            this.pipe = Objects.requireNonNull(pipe);
            this.paths = net.getNetData(pipe.getPipePos());
            this.side = side;
            if (side != null) {
                if (getCoverContainer().getCoverAtSide(side) instanceof FluidFilterCover cover) {
                    filter = cover.getFluidFilter();
                }
            }
        }

        /**
         * check path. how much fluid can be transferred.
         * @return amount
         */
        private long checkPathAvailable(FluidStack stack, PipeNetRoutePath routePath, Map<BlockPos, Set<Fluid>> simulateChannelUsed, Object2LongMap<BlockPos> simulateThroughputUsed) {
            if (stack.isEmpty()) return 0;
            var amount = stack.getAmount();
            for (Pair<BlockPos, FluidPipeData> node : routePath.getPath()) {
                var properties = node.getB().properties();
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
        public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            if (resource.isBlank() || !filter.test(FluidStack.create(resource.getFluid(), maxAmount, resource.getNbt()))) return 0;
            var left = FluidStack.create(resource.getFluid(), maxAmount, resource.getNbt());
            if (!isOuterCallbackAdded) {
                transaction.addOuterCloseCallback(this);
            }
            updateSnapshots(transaction);
            for (PipeNetRoutePath path : paths) {
                if (Objects.equals(pipe.getPipePos(), path.getPipePos()) && (side == path.getFaceToHandler() || side == null)) {
                    //Do not insert into source handler
                    continue;
                }

                var handler = FluidStorage.SIDED.find(getPipeLevel(), path.getHandlerPos(), path.getFaceToHandler().getOpposite());
                if (handler != null) {
                    var coverable = GTCapabilityHelper.getCoverable(net.getLevel(), path.getPipePos(), null);
                    if (coverable != null) {
                        if (coverable.getCoverAtSide(path.getFaceToHandler()) instanceof FluidFilterCover cover && !cover.getFluidFilter().test(FluidStack.create(resource.getFluid(), maxAmount, resource.getNbt()))) {
                            return 0;
                        }
                    }
                    var accepted = checkPathAvailable(left, path, simulateChannelUsed, simulateThroughputUsed);
                    if (accepted <= 0) continue;
                    var copied = left.copy();
                    copied.setAmount(accepted);
                    var filled = handler.insert(FluidHelperImpl.toFluidVariant(copied), copied.getAmount(), transaction);
                    if (filled > 0) { // occupy capacity + channel
                        for (Pair<BlockPos, FluidPipeData> node : path.getPath()) {
                            var pos = node.getA();
                            net.useThroughput(pos, filled);
                            net.useChannel(pos, resource.getFluid());
                        }

                    }
                    left.shrink(filled);
                    if (left.isEmpty()) {
                        break;
                    }
                }
            }
            return maxAmount - left.getAmount();
        }

        @Override
        public boolean supportsExtraction() {
            return false;
        }

        @Override
        public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            return 0;
        }

        @Override
        public Iterator<StorageView<FluidVariant>> iterator() {
            Collection<StorageView<FluidVariant>> list = List.of(new FluidPipeBlockStorageView());
            return list.iterator();
        }

        @Override
        protected FluidPipeNet.Snapshot createSnapshot() {
            return net.createSnapeShot();
        }

        @Override
        protected void readSnapshot(FluidPipeNet.Snapshot snapshot) {
            net.resetData(snapshot);
        }

        private record FluidPipeBlockStorageView() implements StorageView<FluidVariant> {
            @Override
            public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
                return 0;
            }

            @Override
            public boolean isResourceBlank() {
                return true;
            }

            @Override
            public FluidVariant getResource() {
                return FluidVariant.blank();
            }

            @Override
            public long getAmount() {
                return 0;
            }

            @Override
            public long getCapacity() {
                return 0;
            }
        }
    }
}
