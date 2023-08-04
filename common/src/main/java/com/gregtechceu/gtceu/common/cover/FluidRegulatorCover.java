package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
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
import org.jetbrains.annotations.NotNull;

public class FluidRegulatorCover extends PumpCover {
    @Persisted @DescSynced @Getter
    private TransferMode transferMode = TransferMode.TRANSFER_ANY;
    @Persisted @DescSynced @Getter
    private BucketMode transferBucketMode = BucketMode.MILLI_BUCKET;

    @Persisted @Getter
    protected long globalTransferLimit;

    private NumberInputWidget<Long> transferSizeInput;

    public FluidRegulatorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide, int tier) {
        super(definition, coverHolder, attachedSide, tier);
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

        this.transferSizeInput = new LongInputWidget(64, 45, 80, 20,
                () -> globalTransferLimit, val -> globalTransferLimit = val
        ).setMax(0L).setMax(Long.MAX_VALUE);
        configureTransferSizeInput();

        group.addWidget(this.transferSizeInput);
    }

    private void setTransferMode(TransferMode transferMode) {
        this.transferMode = transferMode;

        configureTransferSizeInput();

        if (!this.isRemote()) {
            configureFilterHandler();
        }
    }

    @Override
    protected void configureFilterHandler() {
        if (filterHandler.getFilter() instanceof SimpleFluidFilter filter) {
            filter.setMaxStackSize(transferMode == TransferMode.TRANSFER_ANY ? 1L : Long.MAX_VALUE);
        }
    }

    private void configureTransferSizeInput() {
        if (this.transferSizeInput == null)
            return;

        this.transferSizeInput.setVisible(this.transferMode != TransferMode.TRANSFER_ANY);
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
