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
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class FluidRegulatorCover extends PumpCover {
    private static final long MAX_STACK_SIZE = 2_048_000_000; // Capacity of quantum tank IX

    @Persisted @DescSynced @Getter
    private TransferMode transferMode = TransferMode.TRANSFER_ANY;

    @Persisted @DescSynced @Getter
    private BucketMode transferBucketMode = BucketMode.MILLI_BUCKET;
    @Persisted @DescSynced @Getter
    protected long globalTransferSize;

    private NumberInputWidget<Long> transferSizeInput;

    public FluidRegulatorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide, int tier) {
        super(definition, coverHolder, attachedSide, tier);

        filterHandler.onFilterUpdated(this::onFilterUpdated);
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

        group.addWidget(new EnumSelectorWidget<>(121, 45, 20, 20, BucketMode.values(), transferBucketMode, this::setTransferBucketMode));
    }

    private long getCurrentBucketModeTransferSize() {
        return this.globalTransferSize / this.transferBucketMode.multiplier;
    }

    private void setCurrentBucketModeTransferSize(long transferSize) {
        this.globalTransferSize = Mth.clamp(transferSize * this.transferBucketMode.multiplier, 0, MAX_STACK_SIZE);
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

    private void onFilterUpdated(FluidFilter filter) {
        configureFilter();
        configureTransferSizeInput();
    }

    @Override
    protected void configureFilter() {
        if (filterHandler.getFilter() instanceof SimpleFluidFilter filter) {
            filter.setMaxStackSize(transferMode == TransferMode.TRANSFER_ANY ? 1L : MAX_STACK_SIZE);
        }
    }

    private void configureTransferSizeInput() {
        if (this.transferSizeInput == null)
            return;

        this.transferSizeInput.setVisible(shouldShowTransferSize());
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
