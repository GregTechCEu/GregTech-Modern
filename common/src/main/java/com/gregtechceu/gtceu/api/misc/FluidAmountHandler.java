package com.gregtechceu.gtceu.api.misc;

import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.gregtechceu.gtceu.api.gui.widget.LongInputWidget;
import com.gregtechceu.gtceu.api.gui.widget.NumberInputWidget;
import com.gregtechceu.gtceu.api.syncdata.IEnhancedManaged;
import com.gregtechceu.gtceu.common.cover.data.BucketMode;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.FieldManagedStorage;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.List;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FluidAmountHandler implements IEnhancedManaged {
    protected static final long MILLIBUCKET_SIZE = FluidHelper.getBucket() / 1000;

    private final Runnable renderUpdateHandler;

    @Persisted @DescSynced
    protected long currentMilliBuckets;

    @Getter
    private long maxMilliBuckets;

    @Persisted @DescSynced @Getter
    protected BucketMode bucketMode = BucketMode.MILLI_BUCKET;

    private NumberInputWidget<Long> transferRateWidget;

    public FluidAmountHandler(long initialMilliBuckets, long maxMilliBuckets, Runnable renderUpdateHandler) {
        this.maxMilliBuckets = maxMilliBuckets;
        this.currentMilliBuckets = initialMilliBuckets;

        this.renderUpdateHandler = renderUpdateHandler;
    }

    /////////////////////////////////////
    //***********    API    ***********//
    /////////////////////////////////////


    public void setMaxMilliBuckets(long maxMilliBuckets) {
        this.maxMilliBuckets = maxMilliBuckets;

        setMilliBuckets(getMilliBuckets()); // Automatically reapplies the new limit.
    }

    public void setMilliBuckets(long milliBuckets) {
        this.currentMilliBuckets = Mth.clamp(milliBuckets, 0, maxMilliBuckets);
    }

    public long getMilliBuckets() {
        return currentMilliBuckets;
    }

    /////////////////////////////////////
    //***********    GUI    ***********//
    /////////////////////////////////////

    public Widget createUI(int x, int y, int width, int height) {
        var group = new WidgetGroup(x, y, width, height);

        this.transferRateWidget = new LongInputWidget(0, 0, width - 22, 20,
                this::getCurrentBucketModeAmount, this::setCurrentBucketModeAmount).setMin(0L);
        setBucketMode(this.bucketMode); // initial input widget config happens here
        group.addWidget(transferRateWidget);

        group.addWidget(new EnumSelectorWidget<>(
                width - 20, 0, 20, 20,
                Arrays.stream(BucketMode.values()).filter(m -> m.multiplier <= maxMilliBuckets).toList(),
                bucketMode, this::setBucketMode
        ).setTooltipSupplier(this::getBucketModeTooltip));

        return group;
    }

    private long getCurrentBucketModeAmount() {
        return this.currentMilliBuckets / this.bucketMode.multiplier;
    }

    private void setCurrentBucketModeAmount(long transferRate) {
        this.setMilliBuckets(transferRate * this.bucketMode.multiplier);
    }

    private List<Component> getBucketModeTooltip(BucketMode mode, String langKey) {
        return List.of(
                Component.translatable(langKey).append(Component.translatable("gtceu.gui.content.units.per_tick"))
        );
    }

    public void setBucketMode(BucketMode bucketMode) {
        var oldMultiplier = this.bucketMode.multiplier;
        var newMultiplier = bucketMode.multiplier;

        this.bucketMode = bucketMode;


        if (transferRateWidget == null) return;

        if (oldMultiplier > newMultiplier) {
            transferRateWidget.setValue(getCurrentBucketModeAmount());
        }

        transferRateWidget.setMax(maxMilliBuckets / bucketMode.multiplier);

        if (newMultiplier > oldMultiplier) {
            transferRateWidget.setValue(getCurrentBucketModeAmount());
        }
    }

    //////////////////////////////////////
    //*****     LDLib SyncData    ******//
    //////////////////////////////////////

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(FluidAmountHandler.class);

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Getter
    private final FieldManagedStorage syncStorage = new FieldManagedStorage(this);

    @Override
    public void onChanged() {
        // No implementation necessary
    }

    @Override
    public void scheduleRenderUpdate() {
        renderUpdateHandler.run();
    }
}
