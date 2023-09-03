package com.gregtechceu.gtceu.common.cover.ender_link;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandler;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandlers;
import com.gregtechceu.gtceu.api.cover.filter.FluidFilter;
import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.gregtechceu.gtceu.api.gui.widget.LongInputWidget;
import com.gregtechceu.gtceu.api.gui.widget.NumberInputWidget;
import com.gregtechceu.gtceu.common.cover.data.BucketMode;
import com.gregtechceu.gtceu.common.pipelike.enderlink.EnderLinkChannel;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.List;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class EnderFluidLinkCover extends EnderLinkCover {
    protected static final long MILLIBUCKET_SIZE = FluidHelper.getBucket() / 1000;

    @Persisted @DescSynced @Getter
    protected long currentMilliBucketsPerTick;

    public final long maxMilliBucketsPerTick;

    @Persisted @DescSynced @Getter
    protected BucketMode bucketMode = BucketMode.MILLI_BUCKET;


    @Persisted @DescSynced
    protected final FilterHandler<FluidStack, FluidFilter> filterHandler;
    private NumberInputWidget<Long> transferRateWidget;

    public EnderFluidLinkCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide, GTValues.HV); // TODO support multiple tiers

        this.maxMilliBucketsPerTick = 64 * (long) Math.pow(4, tier - 1); // .5b 2b 8b
        this.filterHandler = FilterHandlers.fluid(this);
    }

    //////////////////////////////////////
    //********     OVERRIDES    ********//
    //////////////////////////////////////

    @Override
    public boolean canAttach() {
        return getOwnFluidTransfer() != null;
    }

    protected @Nullable IFluidTransfer getOwnFluidTransfer() {
        return FluidTransferHelper.getFluidTransfer(coverHolder.getLevel(), coverHolder.getPos(), attachedSide);
    }

    //////////////////////////////////////
    //**********   BEHAVIOR   **********//
    //////////////////////////////////////


    @Override
    public EnderLinkChannel.TransferType getTransferType() {
        return EnderLinkChannel.TransferType.FLUID;
    }

    @Nullable
    @Override
    public IFluidTransfer getFluidTransfer() {
        return getOwnFluidTransfer();
    }

    public void setTransferRate(long milliBucketsPerTick) {
        this.currentMilliBucketsPerTick = Mth.clamp(milliBucketsPerTick, 0, maxMilliBucketsPerTick);
    }

    public void setBucketMode(BucketMode bucketMode) {
        var oldMultiplier = this.bucketMode.multiplier;
        var newMultiplier = bucketMode.multiplier;

        this.bucketMode = bucketMode;


        if (transferRateWidget == null) return;

        if (oldMultiplier > newMultiplier) {
            transferRateWidget.setValue(getCurrentBucketModeTransferRate());
        }

        transferRateWidget.setMax(maxMilliBucketsPerTick / bucketMode.multiplier);

        if (newMultiplier > oldMultiplier) {
            transferRateWidget.setValue(getCurrentBucketModeTransferRate());
        }
    }

    //////////////////////////////////////
    //***********     GUI    ***********//
    //////////////////////////////////////

    @Override
    protected @NotNull String getUITitle() {
        return "cover.ender_link.fluid.label";
    }

    @Override
    protected Widget createTransferRateUI(int x, int y, int width, int height) {
        // TODO extract this to a separate reusable widget, replace this and PumpCover's implementation with it

        var group = new WidgetGroup(x, y, width, height);

        transferRateWidget = new LongInputWidget(0, 0, 134, 20,
                this::getCurrentBucketModeTransferRate, this::setCurrentBucketModeTransferRate).setMin(0L);
        setBucketMode(this.bucketMode); // initial input widget config happens here
        group.addWidget(transferRateWidget);

        group.addWidget(new EnumSelectorWidget<>(
                136, 0, 20, 20,
                Arrays.stream(BucketMode.values()).filter(m -> m.multiplier <= maxMilliBucketsPerTick).toList(),
                bucketMode, this::setBucketMode
        ).setTooltipSupplier(this::getBucketModeTooltip));

        return group;
    }

    @Override
    protected Widget createFilterUI(int x, int y, int width, int height) {
        WidgetGroup group = new WidgetGroup(x, y, width, height);

        group.addWidget(filterHandler.createFilterSlotUI(138, 37));
        group.addWidget(filterHandler.createFilterConfigUI(0, 0, 156, 60));

        return group;
    }

    private List<Component> getBucketModeTooltip(BucketMode mode, String langKey) {
        return List.of(
                Component.translatable(langKey).append(Component.translatable("gtceu.gui.content.units.per_tick"))
        );
    }

    private long getCurrentBucketModeTransferRate() {
        return this.currentMilliBucketsPerTick / this.bucketMode.multiplier;
    }

    private void setCurrentBucketModeTransferRate(long transferRate) {
        this.setTransferRate(transferRate * this.bucketMode.multiplier);
    }

    //////////////////////////////////////
    //*****     LDLib SyncData    ******//
    //////////////////////////////////////

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(EnderFluidLinkCover.class, EnderLinkCover.MANAGED_FIELD_HOLDER);

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
