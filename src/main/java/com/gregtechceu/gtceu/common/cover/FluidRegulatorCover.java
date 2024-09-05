package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.filter.FluidFilter;
import com.gregtechceu.gtceu.api.cover.filter.SimpleFluidFilter;
import com.gregtechceu.gtceu.api.graphnet.IGraphNet;
import com.gregtechceu.gtceu.api.graphnet.edge.SimulatorKey;
import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNetNode;
import com.gregtechceu.gtceu.api.graphnet.pipenet.traverse.SimpleTileRoundRobinData;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.FluidTestObject;
import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.gregtechceu.gtceu.api.gui.widget.LongInputWidget;
import com.gregtechceu.gtceu.api.gui.widget.NumberInputWidget;
import com.gregtechceu.gtceu.common.cover.data.BucketMode;
import com.gregtechceu.gtceu.common.cover.data.TransferMode;
import com.gregtechceu.gtceu.common.pipelike.net.fluid.FluidEQTraverseData;
import com.gregtechceu.gtceu.common.pipelike.net.fluid.FluidRRTraverseData;
import com.gregtechceu.gtceu.common.pipelike.net.fluid.FluidTraverseData;
import com.gregtechceu.gtceu.common.pipelike.net.fluid.IFluidTransferController;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.side.fluid.forge.FluidTransferHelperImpl;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandler;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntToLongFunction;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class FluidRegulatorCover extends PumpCover {

    private static final long MAX_STACK_SIZE = 2_048_000_000; // Capacity of quantum tank IX

    @Persisted
    @DescSynced
    @Getter
    private TransferMode transferMode = TransferMode.TRANSFER_ANY;
    protected boolean noTransferDueToMinimum = false;
    @Persisted
    @DescSynced
    @Getter
    protected long globalTransferSizeMillibuckets;

    private NumberInputWidget<Long> transferSizeInput;
    private EnumSelectorWidget<BucketMode> bucketModeInput;

    public FluidRegulatorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide, int tier) {
        super(definition, coverHolder, attachedSide, tier);
    }

    //////////////////////////////////////
    // ***** Transfer Logic ******//
    //////////////////////////////////////

    @Override
    protected void refreshBuffer(long transferRate) {
        if (this.transferMode == TransferMode.TRANSFER_EXACT && noTransferDueToMinimum) {
            FluidFilter filter = this.getFilterHandler().getFilter();
            if (filter != FluidFilter.EMPTY) {
                this.noTransferDueToMinimum = false;
                this.milliBucketsLeftToTransferLastSecond += transferRate;
                int max = filter.getMaxTransferSize();
                if (this.milliBucketsLeftToTransferLastSecond > max) {
                    this.milliBucketsLeftToTransferLastSecond = max;
                }
                return;
            }
        }
        super.refreshBuffer(transferRate);
    }

    @Override
    protected void performTransferOnUpdate(@NotNull IFluidTransfer sourceHandler, @NotNull IFluidTransfer destHandler) {
        if (transferMode == TransferMode.TRANSFER_ANY) {
            super.performTransferOnUpdate(sourceHandler, destHandler);
            return;
        }
        FluidFilter filter = getFilterHandler().getFilter();
        if (filter == FluidFilter.EMPTY) return;
        if (transferMode == TransferMode.KEEP_EXACT) {
            IntToLongFunction maxflow = s -> Math.min(filter.getTransferLimit(s), getFluidsLeftToTransfer());
            reportFluidsTransfer(performTransfer(sourceHandler, destHandler, true, s -> 0, maxflow, null));
        } else if (transferMode == TransferMode.TRANSFER_EXACT) {
            IntToLongFunction maxflow = s -> {
                int limit = filter.getTransferLimit(s);
                if (getFluidsLeftToTransfer() < limit) {
                    noTransferDueToMinimum = true;
                    return 0;
                } else return limit;
            };
            performTransfer(sourceHandler, destHandler, true, maxflow, maxflow, (a, b) -> reportFluidsTransfer(b));
        }
    }

    @Override
    protected @NotNull FluidTraverseData getTD(IGraphNet net, FluidTestObject testObject, SimulatorKey simulator,
                                               long queryTick, BlockPos sourcePos, Direction inputFacing) {
        if (transferMode == TransferMode.KEEP_EXACT) {
            return new KeepFluidTraverseData(net, testObject, simulator, queryTick, sourcePos, inputFacing);
        }
        return super.getTD(net, testObject, simulator, queryTick, sourcePos, inputFacing);
    }

    @Override
    protected @NotNull FluidEQTraverseData getEQTD(IGraphNet net, FluidTestObject testObject, SimulatorKey simulator,
                                                   long queryTick, BlockPos sourcePos, Direction inputFacing) {
        if (getTransferMode() == TransferMode.KEEP_EXACT) {
            return new KeepFluidEQTraverseData(net, testObject, simulator, queryTick, sourcePos, inputFacing);
        }
        return super.getEQTD(net, testObject, simulator, queryTick, sourcePos, inputFacing);
    }

    @Override
    protected @NotNull FluidRRTraverseData getRRTD(IGraphNet net, FluidTestObject testObject, SimulatorKey simulator,
                                                   long queryTick, BlockPos sourcePos, Direction inputFacing,
                                                   boolean simulate) {
        if (transferMode == TransferMode.KEEP_EXACT) {
            return new KeepFluidRRTraverseData(net, testObject, simulator, queryTick, sourcePos, inputFacing,
                    getRoundRobinCache(simulate));
        }
        return super.getRRTD(net, testObject, simulator, queryTick, sourcePos, inputFacing, simulate);
    }

    @Override
    protected long simpleInsert(@NotNull IFluidTransfer destHandler, FluidTestObject testObject, long count,
                                boolean simulate) {
        if (transferMode == TransferMode.KEEP_EXACT) {
            assert getFilterHandler().isFilterPresent();
            int kept = getFilterHandler().getFilter().getTransferLimit(testObject.recombine());
            count = Math.min(count, kept - computeContained(destHandler, testObject));
        }
        return super.simpleInsert(destHandler, testObject, count, simulate);
    }

    public void setTransferMode(TransferMode transferMode) {
        if (this.transferMode != transferMode) {
            this.transferMode = transferMode;
            this.coverHolder.markDirty();
            this.getFilterHandler().getFilter().setMaxTransferSize(this.transferMode.maxStackSize);
        }
    }

    @Override
    protected void configureFilter() {
        if (filterHandler.getFilter() instanceof SimpleFluidFilter filter) {
            filter.setMaxStackSize(transferMode == TransferMode.TRANSFER_ANY ? 1L : MAX_STACK_SIZE);
        }

        configureTransferSizeInput();
    }

    ///////////////////////////
    // ***** GUI ******//
    ///////////////////////////

    @Override
    protected @NotNull String getUITitle() {
        return "cover.fluid_regulator.title";
    }

    @Override
    protected void buildAdditionalUI(WidgetGroup group) {
        group.addWidget(
                new EnumSelectorWidget<>(146, 45, 20, 20, TransferMode.values(), transferMode, this::setTransferMode));

        this.transferSizeInput = new LongInputWidget(35, 45, 84, 20,
                this::getCurrentBucketModeTransferSize, this::setCurrentBucketModeTransferSize).setMin(0L)
                .setMax(Long.MAX_VALUE);
        configureTransferSizeInput();
        group.addWidget(this.transferSizeInput);

        this.bucketModeInput = new EnumSelectorWidget<>(121, 45, 20, 20, BucketMode.values(),
                bucketMode, this::setBucketMode);
        group.addWidget(this.bucketModeInput);
    }

    private long getCurrentBucketModeTransferSize() {
        return this.getFilterHandler().getFilter().getMaxTransferSize() / this.bucketMode.multiplier;
    }

    private void setCurrentBucketModeTransferSize(long transferSize) {
        this.getFilterHandler().getFilter()
                .setMaxTransferSize((int) Math.min(Math.max(transferSize * this.bucketMode.multiplier, 0),
                        MAX_STACK_SIZE));
    }

    private void configureTransferSizeInput() {
        if (this.transferSizeInput == null || bucketModeInput == null)
            return;

        this.transferSizeInput.setVisible(shouldShowTransferSize());
        this.bucketModeInput.setVisible(shouldShowTransferSize());
    }

    private boolean shouldShowTransferSize() {
        if (this.transferMode == TransferMode.TRANSFER_ANY)
            return false;

        if (!this.filterHandler.isFilterPresent())
            return true;

        return !this.filterHandler.getFilter().supportsAmounts();
    }

    protected long computeContained(@NotNull IFluidTransfer handler, @NotNull FluidTestObject testObject) {
        long found = 0;
        for (int i = 0; i < handler.getTanks(); ++i) {
            FluidStack contained = handler.getFluidInTank(i);
            if (testObject.test(contained)) {
                found += contained.getAmount();
            }
        }
        return found;
    }

    protected class KeepFluidTraverseData extends FluidTraverseData {

        public KeepFluidTraverseData(IGraphNet net, FluidTestObject testObject, SimulatorKey simulator, long queryTick,
                                     BlockPos sourcePos, Direction inputFacing) {
            super(net, testObject, simulator, queryTick, sourcePos, inputFacing);
        }

        @Override
        public long finalizeAtDestination(@NotNull WorldPipeNetNode destination, long flowReachingDestination) {
            long availableFlow = flowReachingDestination;
            for (var capability : destination.getBlockEntity().getTargetsWithCapabilities(destination).entrySet()) {
                if (destination.getEquivalencyData().equals(sourcePos) &&
                        capability.getKey() == inputFacing)
                    continue; // anti insert-to-our-source logic

                IFluidHandler containerCap = capability.getValue()
                        .getCapability(ForgeCapabilities.FLUID_HANDLER,
                                capability.getKey().getOpposite())
                        .resolve().orElse(null);
                if (containerCap != null) {
                    IFluidTransfer container = FluidTransferHelperImpl.toFluidTransfer(containerCap);
                    long contained = computeContained(container, getTestObject());
                    assert getFilterHandler().isFilterPresent();
                    int kept = getFilterHandler().getFilter().getTransferLimit(getTestObject().recombine());
                    if (contained >= kept) continue;
                    availableFlow -= IFluidTransferController.CONTROL.get(destination.getBlockEntity().getCoverHolder()
                            .getCoverAtSide(capability.getKey())).insertToHandler(getTestObject(),
                                    (int) Math.min(kept - contained, availableFlow), container,
                                    getSimulatorKey() == null);
                }
            }
            return flowReachingDestination - availableFlow;
        }
    }

    protected class KeepFluidEQTraverseData extends FluidEQTraverseData {

        public KeepFluidEQTraverseData(IGraphNet net, FluidTestObject testObject, SimulatorKey simulator,
                                       long queryTick,
                                       BlockPos sourcePos, Direction inputFacing) {
            super(net, testObject, simulator, queryTick, sourcePos, inputFacing);
        }

        @Override
        public long finalizeAtDestination(@NotNull WorldPipeNetNode node, long flowReachingNode,
                                          int expectedDestinations) {
            long availableFlow = flowReachingNode;
            long flowPerDestination = flowReachingNode / expectedDestinations;
            if (flowPerDestination == 0) return 0;
            for (var capability : node.getBlockEntity().getTargetsWithCapabilities(node).entrySet()) {
                if (node.getEquivalencyData().equals(sourcePos) &&
                        capability.getKey() == inputFacing)
                    continue; // anti insert-to-our-source logic

                var containerCap = capability.getValue()
                        .getCapability(ForgeCapabilities.FLUID_HANDLER, capability.getKey().getOpposite())
                        .resolve().orElse(null);
                if (containerCap != null) {
                    IFluidTransfer container = FluidTransferHelperImpl.toFluidTransfer(containerCap);
                    long contained = computeContained(container, getTestObject());
                    assert getFilterHandler().isFilterPresent();
                    int kept = getFilterHandler().getFilter().getTransferLimit(getTestObject().recombine());
                    if (contained >= kept) continue;
                    availableFlow -= IFluidTransferController.CONTROL.get(node.getBlockEntity().getCoverHolder()
                            .getCoverAtSide(capability.getKey())).insertToHandler(getTestObject(),
                            (int) Math.min(kept - contained, flowPerDestination), container, !simulating());
                }
            }
            return flowReachingNode - availableFlow;
        }

        @Override
        public long finalizeAtDestination(@NotNull WorldPipeNetNode destination, long flowReachingDestination) {
            long availableFlow = flowReachingDestination;
            for (var capability : destination.getBlockEntity().getTargetsWithCapabilities(destination).entrySet()) {
                if (destination.getEquivalencyData().equals(sourcePos) &&
                        capability.getKey() == inputFacing)
                    continue; // anti insert-to-our-source logic

                IFluidHandler containerCap = capability.getValue()
                        .getCapability(ForgeCapabilities.FLUID_HANDLER,
                                capability.getKey().getOpposite())
                        .resolve().orElse(null);
                if (containerCap != null) {
                    IFluidTransfer container = FluidTransferHelperImpl.toFluidTransfer(containerCap);
                    long contained = computeContained(container, getTestObject());
                    assert getFilterHandler().isFilterPresent();
                    int kept = getFilterHandler().getFilter().getTransferLimit(getTestObject().recombine());
                    if (contained >= kept) continue;
                    availableFlow -= IFluidTransferController.CONTROL.get(destination.getBlockEntity().getCoverHolder()
                            .getCoverAtSide(capability.getKey())).insertToHandler(getTestObject(),
                                    (int) Math.min(kept - contained, availableFlow), container,
                                    getSimulatorKey() == null);
                }
            }
            return flowReachingDestination - availableFlow;
        }
    }

    protected class KeepFluidRRTraverseData extends FluidRRTraverseData {

        public KeepFluidRRTraverseData(IGraphNet net, FluidTestObject testObject, SimulatorKey simulator,
                                       long queryTick, BlockPos sourcePos, Direction inputFacing,
                                       @NotNull Object2ObjectLinkedOpenHashMap<Object, SimpleTileRoundRobinData<IFluidHandler>> cache) {
            super(net, testObject, simulator, queryTick, sourcePos, inputFacing, cache);
        }

        @Override
        public long finalizeAtDestination(@NotNull SimpleTileRoundRobinData<IFluidHandler> data,
                                          @NotNull WorldPipeNetNode destination, long flowReachingDestination) {
            long availableFlow = flowReachingDestination;
            Direction pointerFacing = data.getPointerFacing(getSimulatorKey());
            // anti insert-to-our-source logic
            if (!destination.getEquivalencyData().equals(sourcePos) ||
                    !(pointerFacing == inputFacing)) {
                IFluidHandler containerCap = data.getAtPointer(destination, getSimulatorKey());
                if (containerCap != null) {
                    IFluidTransfer container = FluidTransferHelperImpl.toFluidTransfer(containerCap);
                    long contained = computeContained(container, getTestObject());
                    assert getFilterHandler().isFilterPresent();
                    int kept = getFilterHandler().getFilter().getTransferLimit(getTestObject().recombine());
                    if (contained < kept) {
                        availableFlow -= IFluidTransferController.CONTROL.get(
                                destination.getBlockEntity().getCoverHolder()
                                        .getCoverAtSide(pointerFacing))
                                .insertToHandler(getTestObject(),
                                        (int) Math.min(kept - contained, availableFlow), container,
                                        getSimulatorKey() == null);
                    }
                }
            }
            return flowReachingDestination - availableFlow;
        }
    }

    //////////////////////////////////////
    // ***** LDLib SyncData ******//
    //////////////////////////////////////

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(FluidRegulatorCover.class,
            PumpCover.MANAGED_FIELD_HOLDER);

    @NotNull
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
