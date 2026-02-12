package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IMuiCover;
import com.gregtechceu.gtceu.api.cover.filter.FluidFilter;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.factory.SidedPosGuiData;
import com.gregtechceu.gtceu.api.mui.value.sync.DynamicSyncHandler;
import com.gregtechceu.gtceu.api.mui.value.sync.EnumSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widget.ParentWidget;
import com.gregtechceu.gtceu.api.mui.widgets.ButtonWidget;
import com.gregtechceu.gtceu.api.mui.widgets.DynamicSyncedWidget;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.transfer.fluid.FluidHandlerDelegate;
import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.cover.data.FilterMode;
import com.gregtechceu.gtceu.common.cover.data.ManualIOMode;
import com.gregtechceu.gtceu.common.data.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidFilterCover extends CoverBehavior implements IMuiCover {

    protected FluidFilter fluidFilter;
    @SaveField
    @SyncToClient
    @Getter
    protected FilterMode filterMode = FilterMode.FILTER_INSERT;
    private FilteredFluidHandlerWrapper fluidFilterWrapper;
    @SaveField
    @Setter
    @Getter
    protected ManualIOMode allowFlow = ManualIOMode.DISABLED;

    public FluidFilterCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    public void setFilterMode(FilterMode filterMode) {
        this.filterMode = filterMode;
        syncDataHolder.markClientSyncFieldDirty("filterMode");
    }

    @Override
    public boolean canAttach() {
        return super.canAttach() && coverHolder.getFluidHandlerCap(attachedSide, false) != null;
    }

    public FluidFilter getFluidFilter() {
        if (fluidFilter == null) {
            fluidFilter = FluidFilter.loadFilter(attachItem);
        }
        return fluidFilter;
    }

    @Override
    public @Nullable IFluidHandlerModifiable getFluidHandlerCap(@Nullable IFluidHandlerModifiable defaultValue) {
        if (defaultValue == null) {
            return null;
        }

        if (fluidFilterWrapper == null || fluidFilterWrapper.delegate != defaultValue) {
            this.fluidFilterWrapper = new FilteredFluidHandlerWrapper(defaultValue);
        }

        return fluidFilterWrapper;
    }

    @Override
    public ParentWidget<?> createCoverUI(SidedPosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        Flow column = Flow.column()
                .top(7).margin(7, 0)
                .widthRel(1.0f).coverChildrenHeight();

        EnumSyncValue<FilterMode> filterMode = new EnumSyncValue<>(FilterMode.class,
                this::getFilterMode, this::setFilterMode);

        EnumSyncValue<ManualIOMode> ioMode = new EnumSyncValue<>(ManualIOMode.class,
                this::getAllowFlow, this::setAllowFlow);

        syncManager.syncValue("filterMode", filterMode);
        syncManager.syncValue("ioMode", ioMode);

        var panelHandler = syncManager.syncedPanel("filterPanel", true,
                (sm, sh) -> fluidFilter.getPanel(data, sm, settings));

        DynamicSyncHandler filterButton = new DynamicSyncHandler()
                .widgetProvider((sm, buf) -> new ButtonWidget<>()
                        .onMousePressed((x, y, b) -> {
                            panelHandler.openPanel();
                            return true;
                        }));

        column.child(Flow.row()
                .coverChildrenHeight()
                .child(new DynamicSyncedWidget<>().syncHandler(filterButton)));

        column.child(new GTMuiWidgets.EnumRowBuilder<>(FilterMode.class)
                .value(filterMode)
                .overlay(16, GTGuiTextures.FILTER_MODE_OVERLAY)
                .lang(IKey.dynamic(() -> Component.translatable(getFilterMode().getTooltip())))
                .build());

        column.child(new GTMuiWidgets.EnumRowBuilder<>(ManualIOMode.class)
                .value(ioMode)
                .overlay(16, GTGuiTextures.MANUAL_IO_OVERLAY_IN)
                .lang(IKey.dynamic(() -> Component.translatable(getAllowFlow().getTooltip())))
                .build());

        return column;
    }

    private class FilteredFluidHandlerWrapper extends FluidHandlerDelegate {

        public FilteredFluidHandlerWrapper(IFluidHandlerModifiable delegate) {
            super(delegate);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (filterMode == FilterMode.FILTER_EXTRACT) {
                if (allowFlow == ManualIOMode.DISABLED) {
                    return 0;
                }
                if (allowFlow == ManualIOMode.UNFILTERED) {
                    return super.fill(resource, action);
                }
            }
            if (!getFluidFilter().test(resource)) {
                return 0;
            }
            return super.fill(resource, action);
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (filterMode == FilterMode.FILTER_INSERT) {
                if (allowFlow == ManualIOMode.DISABLED) {
                    return FluidStack.EMPTY;
                }
                if (allowFlow == ManualIOMode.UNFILTERED) {
                    return super.drain(resource, action);
                }
            }
            if (!getFluidFilter().test(resource)) {
                return FluidStack.EMPTY;
            }
            return super.drain(resource, action);
        }
    }

    @Override
    public CompoundTag copyConfig(CompoundTag tag) {
        tag.putInt("manualIO", getAllowFlow().ordinal());
        tag.putInt("filterMode", getFilterMode().ordinal());
        tag.put("filter", attachItem.serializeNBT());
        return super.copyConfig(tag);
    }

    @Override
    public void pasteConfig(ServerPlayer player, CompoundTag tag) {
        setAllowFlow(ManualIOMode.values()[tag.getInt("manualIO")]);
        setFilterMode(FilterMode.values()[tag.getInt("filterMode")]);
        fluidFilter = FluidFilter.loadFilter(ItemStack.of(tag.getCompound("filter")));
        super.pasteConfig(player, tag);
    }
}
