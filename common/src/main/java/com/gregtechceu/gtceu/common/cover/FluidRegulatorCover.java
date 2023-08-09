package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.filter.FluidFilter;
import com.gregtechceu.gtceu.api.cover.filter.SimpleFluidFilter;
import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.gregtechceu.gtceu.api.gui.widget.LongInputWidget;
import com.gregtechceu.gtceu.api.gui.widget.NumberInputWidget;
import com.gregtechceu.gtceu.common.cover.data.BucketMode;
import com.gregtechceu.gtceu.common.cover.data.TransferMode;
import com.gregtechceu.gtceu.utils.FluidStackHashStrategy;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import it.unimi.dsi.fastutil.objects.Object2LongOpenCustomHashMap;
import lombok.Getter;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class FluidRegulatorCover extends PumpCover {
    private static final long MAX_STACK_SIZE = 2_048_000_000; // Capacity of quantum tank IX

    @Persisted @DescSynced @Getter
    private TransferMode transferMode = TransferMode.TRANSFER_ANY;

    @Persisted @DescSynced @Getter
    private BucketMode transferBucketMode = BucketMode.MILLI_BUCKET;
    @Persisted @DescSynced @Getter
    protected long globalTransferSize;
    protected int fluidTransferBuffered;


    private NumberInputWidget<Long> transferSizeInput;
    private EnumSelectorWidget<BucketMode> transferBucketModeInput;

    public FluidRegulatorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide, int tier) {
        super(definition, coverHolder, attachedSide, tier);
    }

    //////////////////////////////////////
    //*****     Transfer Logic    ******//
    //////////////////////////////////////


    @Override
    protected long doTransferFluidsInternal(IFluidTransfer source, IFluidTransfer destination, long platformTransferLimit) {
        return switch (transferMode) {
            case TRANSFER_ANY -> transferAny(source, destination, platformTransferLimit);
            case TRANSFER_EXACT -> transferExact(source, destination, platformTransferLimit);
            case KEEP_EXACT -> keepExact(source, destination, platformTransferLimit);
        };
    }

    private long transferExact(IFluidTransfer source, IFluidTransfer destination, long platformTransferLimit) {
        long fluidLeftToTransfer = platformTransferLimit;

        for (int slot = 0; slot < source.getTanks(); slot++) {
            if (fluidLeftToTransfer <= 0L)
                break;

            FluidStack sourceFluid = source.getFluidInTank(slot).copy();
            long supplyAmount = getFilteredFluidAmount(sourceFluid);

            if (fluidLeftToTransfer < supplyAmount)
                break;

            if (sourceFluid.isEmpty() || supplyAmount <= 0L)
                continue;

            sourceFluid.setAmount(supplyAmount);
            FluidStack drained = source.drain(sourceFluid, true);

            if (drained.isEmpty() || drained.getAmount() < supplyAmount)
                continue;

            long insertableAmount = destination.fill(drained.copy(), true);
            if (insertableAmount <= 0)
                continue;

            drained.setAmount(insertableAmount);
            drained = source.drain(drained, false);

            if (!drained.isEmpty()) {
                destination.fill(drained, false);
                fluidLeftToTransfer -= drained.getAmount();
            }
        }

        return platformTransferLimit - fluidLeftToTransfer;
    }

    private long keepExact(IFluidTransfer source, IFluidTransfer destination, long platformTransferLimit) {
        long fluidLeftToTransfer = platformTransferLimit;

        final Map<FluidStack, Long> sourceAmounts = enumerateDistinctFluids(source, TransferDirection.EXTRACT);
        final Map<FluidStack, Long> destinationAmounts = enumerateDistinctFluids(destination, TransferDirection.INSERT);

        for (FluidStack fluidStack : sourceAmounts.keySet()) {
            if (fluidLeftToTransfer <= 0L)
                break;

            long amountToKeep = getFilteredFluidAmount(fluidStack);
            long amountInDest = destinationAmounts.getOrDefault(fluidStack, 0L);
            if (amountInDest >= amountToKeep)
                continue;

            FluidStack fluidToMove = fluidStack.copy();
            fluidToMove.setAmount(Math.min(fluidLeftToTransfer, amountToKeep - amountInDest));
            if (fluidToMove.getAmount() <= 0L)
                continue;

            FluidStack drained = source.drain(fluidToMove, true);
            long fillableAmount = destination.fill(drained, true);
            if (fillableAmount <= 0L)
                continue;

            fluidToMove.setAmount(Math.min(fluidToMove.getAmount(), fillableAmount));

            drained = source.drain(fluidToMove, false);
            long movedAmount = destination.fill(drained, false);

            fluidLeftToTransfer -= movedAmount;
        }

        return platformTransferLimit - fluidLeftToTransfer;
    }

    private enum TransferDirection {
        INSERT,
        EXTRACT
    }

    private Map<FluidStack, Long> enumerateDistinctFluids(IFluidTransfer fluidTransfer, TransferDirection direction) {
        final Map<FluidStack, Long> summedFluids = new Object2LongOpenCustomHashMap<>(FluidStackHashStrategy.comparingAllButAmount());

        for (int tank = 0; tank < fluidTransfer.getTanks(); tank++) {
            if (!canTransfer(fluidTransfer, direction, tank))
                continue;

            FluidStack fluidStack = fluidTransfer.getFluidInTank(tank);
            if (fluidStack.isEmpty())
                continue;

            summedFluids.putIfAbsent(fluidStack, 0L);
            summedFluids.computeIfPresent(fluidStack, (stack, totalAmount) -> {
                return totalAmount + stack.getAmount();
            });
        }

        return summedFluids;
    }

    private static boolean canTransfer(IFluidTransfer fluidTransfer, TransferDirection direction, int tank) {
        return switch (direction) {
            case INSERT -> fluidTransfer.supportsFill(tank);
            case EXTRACT -> fluidTransfer.supportsDrain(tank);
        };
    }

    private void setTransferBucketMode(BucketMode transferBucketMode) {
        var oldMultiplier = this.transferBucketMode.multiplier;
        var newMultiplier = transferBucketMode.multiplier;

        this.transferBucketMode = transferBucketMode;


        if (transferSizeInput == null) return;

        if (oldMultiplier > newMultiplier) {
            transferSizeInput.setValue(getCurrentBucketModeTransferSize());
        }
        this.transferSizeInput.setMax(MAX_STACK_SIZE / this.transferBucketMode.multiplier);
        if (newMultiplier > oldMultiplier) {
            transferSizeInput.setValue(getCurrentBucketModeTransferSize());
        }
    }

    private void setTransferMode(TransferMode transferMode) {
        this.transferMode = transferMode;

        configureTransferSizeInput();

        if (!this.isRemote()) {
            configureFilter();
        }
    }

    @Override
    protected void configureFilter() {
        if (filterHandler.getFilter() instanceof SimpleFluidFilter filter) {
            filter.setMaxStackSize(transferMode == TransferMode.TRANSFER_ANY ? 1L : MAX_STACK_SIZE);
        }

        configureTransferSizeInput();
    }

    private long getFilteredFluidAmount(FluidStack fluidStack) {
        if (!filterHandler.isFilterPresent())
            return globalTransferSize;

        FluidFilter filter = filterHandler.getFilter();
        return (filter.isBlackList() ? globalTransferSize : filter.testFluidAmount(fluidStack)) * MILLIBUCKET_SIZE;
    }

    ///////////////////////////
    //*****     GUI    ******//
    ///////////////////////////

    @Override
    protected @NotNull String getUITitle() {
        return "cover.fluid_regulator.title";
    }

    @Override
    protected void buildAdditionalUI(WidgetGroup group) {
        group.addWidget(new EnumSelectorWidget<>(146, 45, 20, 20, TransferMode.values(), transferMode, this::setTransferMode));

        this.transferSizeInput = new LongInputWidget(35, 45, 84, 20,
                this::getCurrentBucketModeTransferSize, this::setCurrentBucketModeTransferSize
        ).setMin(0L).setMax(Long.MAX_VALUE);
        configureTransferSizeInput();
        group.addWidget(this.transferSizeInput);

        this.transferBucketModeInput = new EnumSelectorWidget<>(121, 45, 20, 20, BucketMode.values(), transferBucketMode, this::setTransferBucketMode);
        group.addWidget(this.transferBucketModeInput);
    }

    private long getCurrentBucketModeTransferSize() {
        return this.globalTransferSize / this.transferBucketMode.multiplier;
    }

    private void setCurrentBucketModeTransferSize(long transferSize) {
        this.globalTransferSize = Mth.clamp(transferSize * this.transferBucketMode.multiplier, 0, MAX_STACK_SIZE);
    }

    private void configureTransferSizeInput() {
        if (this.transferSizeInput == null || transferBucketModeInput == null)
            return;

        this.transferSizeInput.setVisible(shouldShowTransferSize());
        this.transferBucketModeInput.setVisible(shouldShowTransferSize());
    }

    private boolean shouldShowTransferSize() {
        if (this.transferMode == TransferMode.TRANSFER_ANY)
            return false;

        if (!this.filterHandler.isFilterPresent())
            return true;

        return this.filterHandler.getFilter().isBlackList();
    }

    //////////////////////////////////////
    //*****     LDLib SyncData    ******//
    //////////////////////////////////////

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(FluidRegulatorCover.class, PumpCover.MANAGED_FIELD_HOLDER);

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
