package com.gregtechceu.gtceu.common.cover.ender_link;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandler;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandlers;
import com.gregtechceu.gtceu.api.cover.filter.FluidFilter;
import com.gregtechceu.gtceu.api.misc.FluidAmountHandler;
import com.gregtechceu.gtceu.api.pipenet.enderlink.ITransferType;
import com.gregtechceu.gtceu.common.data.GTEnderLinkTransferTypes;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class EnderFluidLinkCover extends EnderLinkCover<IFluidTransfer> {

    @Persisted
    private FluidAmountHandler transferRate;

    @Persisted @DescSynced
    protected final FilterHandler<FluidStack, FluidFilter> filterHandler;

    public EnderFluidLinkCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide, GTValues.HV); // TODO support multiple tiers

        var maxMilliBucketsPerTick = 64 * (long) Math.pow(4, tier - 1); // .5b 2b 8b

        this.transferRate = new FluidAmountHandler(maxMilliBucketsPerTick, maxMilliBucketsPerTick, this::scheduleRenderUpdate);
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
    public ITransferType<IFluidTransfer> getTransferType() {
        return GTEnderLinkTransferTypes.FLUID;
    }

    @Nullable
    @Override
    public IFluidTransfer getTransfer() {
        return getOwnFluidTransfer();
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
        return transferRate.createUI(x, y, width, height);
    }

    @Override
    protected Widget createFilterUI(int x, int y, int width, int height) {
        WidgetGroup group = new WidgetGroup(x, y, width, height);

        group.addWidget(filterHandler.createFilterSlotUI(138, 37));
        group.addWidget(filterHandler.createFilterConfigUI(0, 0, 156, 60));

        return group;
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
