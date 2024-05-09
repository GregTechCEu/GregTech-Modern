package com.gregtechceu.gtceu.common.covers.voiding;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.covers.CoverDefinition;
import com.gregtechceu.gtceu.api.covers.filter.FluidFilter;
import com.gregtechceu.gtceu.api.covers.filter.SimpleFluidFilter;
import com.gregtechceu.gtceu.api.guis.widget.EnumSelectorWidget;
import com.gregtechceu.gtceu.api.guis.widget.IntInputWidget;
import com.gregtechceu.gtceu.api.guis.widget.NumberInputWidget;
import com.gregtechceu.gtceu.common.covers.data.BucketMode;
import com.gregtechceu.gtceu.common.covers.data.VoidingMode;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.side.fluid.IFluidHandlerModifiable;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class AdvancedFluidVoidingCover extends FluidVoidingCover {
    @Persisted @DescSynced @Getter
    private VoidingMode voidingMode = VoidingMode.VOID_ANY;

    @Persisted @DescSynced @Getter
    protected int globalTransferSizeMillibuckets = 1;
    @Persisted @DescSynced @Getter
    private BucketMode transferBucketMode = BucketMode.MILLI_BUCKET;

    private NumberInputWidget<Integer> stackSizeInput;
    private EnumSelectorWidget<BucketMode> stackSizeBucketModeInput;

    public AdvancedFluidVoidingCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }


    //////////////////////////////////////////////
    //***********     COVER LOGIC    ***********//
    //////////////////////////////////////////////

    @Override
    protected void doVoidFluids() {
        IFluidHandlerModifiable fluidTransfer = getOwnFluidTransfer();
        if (fluidTransfer == null) {
            return;
        }

        switch (voidingMode) {
            case VOID_ANY -> voidAny(fluidTransfer);
            case VOID_OVERFLOW -> voidOverflow(fluidTransfer);
        }
    }

    private void voidOverflow(IFluidHandlerModifiable fluidTransfer) {
        final Map<FluidStack, Integer> fluidAmounts = enumerateDistinctFluids(fluidTransfer, TransferDirection.EXTRACT);

        for (FluidStack fluidStack : fluidAmounts.keySet()) {
            int presentAmount = fluidAmounts.get(fluidStack);
            int targetAmount = getFilteredFluidAmount(fluidStack) * MILLIBUCKET_SIZE;
            if (targetAmount <= 0L || targetAmount > presentAmount)
                continue;

            var toDrain = fluidStack.copy();
            toDrain.setAmount(presentAmount - targetAmount);

            fluidTransfer.drain(toDrain, IFluidHandler.FluidAction.EXECUTE);
        }
    }

    private int getFilteredFluidAmount(FluidStack fluidStack) {
        if (!filterHandler.isFilterPresent())
            return globalTransferSizeMillibuckets;

        FluidFilter filter = filterHandler.getFilter();
        return filter.isBlackList() ? globalTransferSizeMillibuckets : filter.testFluidAmount(fluidStack);
    }

    public void setVoidingMode(VoidingMode voidingMode) {
        this.voidingMode = voidingMode;

        configureStackSizeInput();

        if (!this.isRemote()) {
            configureFilter();
        }
    }

    private void setTransferBucketMode(BucketMode transferBucketMode) {
        var oldMultiplier = this.transferBucketMode.multiplier;
        var newMultiplier = transferBucketMode.multiplier;

        this.transferBucketMode = transferBucketMode;


        if (stackSizeInput == null) return;
        stackSizeInput.setValue(getCurrentBucketModeTransferSize());
    }


    //////////////////////////////////////
    //***********     GUI    ***********//
    //////////////////////////////////////

    @Override
    protected @NotNull String getUITitle() {
        return "cover.fluid.voiding.advanced.title";
    }

    @Override
    protected void buildAdditionalUI(WidgetGroup group) {
        group.addWidget(new EnumSelectorWidget<>(146, 20, 20, 20, VoidingMode.values(), voidingMode, this::setVoidingMode));

        this.stackSizeInput = new IntInputWidget(35, 20, 84, 20,
                this::getCurrentBucketModeTransferSize, this::setCurrentBucketModeTransferSize
        ).setMin(1).setMax(Integer.MAX_VALUE);
        configureStackSizeInput();
        group.addWidget(this.stackSizeInput);

        this.stackSizeBucketModeInput = new EnumSelectorWidget<>(121, 20, 20, 20, BucketMode.values(), transferBucketMode, this::setTransferBucketMode);
        group.addWidget(this.stackSizeBucketModeInput);
    }

    private int getCurrentBucketModeTransferSize() {
        return this.globalTransferSizeMillibuckets / this.transferBucketMode.multiplier;
    }

    private void setCurrentBucketModeTransferSize(int transferSize) {
        this.globalTransferSizeMillibuckets = Math.max(transferSize * this.transferBucketMode.multiplier, 0);
    }

    @Override
    protected void configureFilter() {
        if (filterHandler.getFilter() instanceof SimpleFluidFilter filter) {
            filter.setMaxStackSize(voidingMode == VoidingMode.VOID_ANY ? 1 : Integer.MAX_VALUE);
        }

        configureStackSizeInput();
    }

    private void configureStackSizeInput() {
        if (this.stackSizeInput == null || stackSizeBucketModeInput == null)
            return;

        this.stackSizeInput.setVisible(shouldShowStackSize());
        this.stackSizeBucketModeInput.setVisible(shouldShowStackSize());
    }

    private boolean shouldShowStackSize() {
        if (this.voidingMode == VoidingMode.VOID_ANY)
            return false;

        if (!this.filterHandler.isFilterPresent())
            return true;

        return this.filterHandler.getFilter().isBlackList();
    }
    
    //////////////////////////////////////
    //*****     LDLib SyncData    ******//
    //////////////////////////////////////
    
    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(AdvancedFluidVoidingCover.class, FluidVoidingCover.MANAGED_FIELD_HOLDER);
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
