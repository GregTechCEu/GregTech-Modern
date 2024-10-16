package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IUICover;
import com.gregtechceu.gtceu.api.cover.filter.FluidFilter;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.gregtechceu.gtceu.api.transfer.fluid.FluidTransferDelegate;

import com.gregtechceu.gtceu.common.cover.data.FilterMode;
import com.gregtechceu.gtceu.common.cover.data.ManualIOMode;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;

import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/3/13
 * @implNote ItemFilterCover
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidFilterCover extends CoverBehavior implements IUICover {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(FluidFilterCover.class,
            CoverBehavior.MANAGED_FIELD_HOLDER);
    protected FluidFilter fluidFilter;
    @Persisted
    @DescSynced
    @Getter
    protected FilterMode filterMode = FilterMode.FILTER_INSERT;
    private FilteredFluidTransferWrapper fluidFilterWrapper;
    @Setter
    @Getter
    protected ManualIOMode allowFlow = ManualIOMode.DISABLED;

    public FluidFilterCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    public void setFilterMode(FilterMode filterMode) {
        this.filterMode = filterMode;
        coverHolder.markDirty();
    }

    @Override
    public boolean canAttach() {
        return FluidTransferHelper.getFluidTransfer(coverHolder.getLevel(), coverHolder.getPos(), attachedSide) != null;
    }

    public FluidFilter getFluidFilter() {
        if (fluidFilter == null) {
            fluidFilter = FluidFilter.loadFilter(attachItem);
        }
        return fluidFilter;
    }

    @Override
    public @Nullable IFluidTransfer getFluidTransferCap(@Nullable IFluidTransfer defaultValue) {
        if (defaultValue == null) {
            return null;
        }

        if (fluidFilterWrapper == null || fluidFilterWrapper.delegate != defaultValue) {
            this.fluidFilterWrapper = new FilteredFluidTransferWrapper(defaultValue);
        }

        return fluidFilterWrapper;
    }

    @Override
    public Widget createUIWidget() {
        final var group = new WidgetGroup(0, 0, 176, 85);
        group.addWidget(new LabelWidget(7, 5, attachItem.getDescriptionId()));
        group.addWidget(new EnumSelectorWidget<>(7, 25, 18, 18,
                FilterMode.VALUES, filterMode, this::setFilterMode));
        group.addWidget(new EnumSelectorWidget<>(7, 45, 18, 18, ManualIOMode.VALUES, allowFlow, this::setAllowFlow));
        group.addWidget(getFluidFilter().openConfigurator(48, 18));
        return group;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    private class FilteredFluidTransferWrapper extends FluidTransferDelegate {

        public FilteredFluidTransferWrapper(IFluidTransfer delegate) {
            super(delegate);
        }

        @Override
        public long fill(int tank, FluidStack resource, boolean simulate, boolean notifyChanges) {
            if (filterMode == FilterMode.FILTER_EXTRACT && allowFlow == ManualIOMode.UNFILTERED)
                return super.fill(tank, resource, simulate, notifyChanges);
            return getFluidFilter().test(resource) ? super.fill(tank, resource, simulate, notifyChanges) : 0;
        }

        @Override
        public FluidStack drain(int tank, FluidStack resource, boolean simulate, boolean notifyChanges) {
            if (filterMode == FilterMode.FILTER_INSERT && allowFlow == ManualIOMode.UNFILTERED)
                return super.drain(tank, resource, simulate, notifyChanges);
            return getFluidFilter().test(resource) ? super.drain(tank, resource, simulate, notifyChanges) : FluidStack.empty();
        }
    }
}
