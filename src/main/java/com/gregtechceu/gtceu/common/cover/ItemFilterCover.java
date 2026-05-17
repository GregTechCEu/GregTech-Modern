package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IMuiCover;
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.api.cover.filter.SmartItemFilter;
import com.gregtechceu.gtceu.api.machine.MachineCoverContainer;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.transfer.item.ItemHandlerDelegate;
import com.gregtechceu.gtceu.common.cover.data.FilterMode;
import com.gregtechceu.gtceu.common.cover.data.ManualIOMode;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.factory.SidedPosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.DynamicSyncHandler;
import brachy.modularui.value.sync.EnumSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.DynamicSyncedWidget;
import brachy.modularui.widgets.layout.Flow;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemFilterCover extends CoverBehavior implements IMuiCover {

    protected ItemFilter itemFilter;
    @SaveField
    @SyncToClient
    @Getter
    protected FilterMode filterMode = FilterMode.FILTER_INSERT;
    private FilteredItemHandlerWrapper itemFilterWrapper;
    @SaveField
    @Setter
    @Getter
    protected ManualIOMode allowFlow = ManualIOMode.DISABLED;

    public ItemFilterCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    public ItemFilter getItemFilter() {
        if (itemFilter == null) {
            itemFilter = ItemFilter.loadFilter(attachItem);
            if (itemFilter instanceof SmartItemFilter smart && coverHolder instanceof MachineCoverContainer mcc) {
                var machine = MetaMachine.getMachine(mcc.getLevel(), mcc.getBlockPos());
                if (machine != null) smart.setModeFromMachine(machine.getDefinition().getName());
            }
        }
        return itemFilter;
    }

    public void setFilterMode(FilterMode filterMode) {
        this.filterMode = filterMode;
        syncDataHolder.markClientSyncFieldDirty("filterMode");
    }

    @Override
    public boolean canAttach() {
        return super.canAttach() && coverHolder.getItemHandlerCap(attachedSide, false) != null;
    }

    @Override
    public @Nullable IItemHandlerModifiable getItemHandlerCap(IItemHandlerModifiable defaultValue) {
        if (defaultValue == null) {
            return null;
        }
        if (itemFilterWrapper == null || itemFilterWrapper.delegate != defaultValue) {
            this.itemFilterWrapper = new FilteredItemHandlerWrapper(defaultValue);
        }
        return itemFilterWrapper;
    }

    @Override
    public void onAttached(ItemStack itemStack, @Nullable ServerPlayer player) {
        super.onAttached(itemStack, player);
    }

    @Override
    public void createCoverUIRows(Flow column, SidedPosGuiData data, PanelSyncManager syncManager,
                                  UISettings settings) {
        EnumSyncValue<FilterMode> filterMode = new EnumSyncValue<>(FilterMode.class,
                this::getFilterMode, this::setFilterMode);

        EnumSyncValue<ManualIOMode> ioMode = new EnumSyncValue<>(ManualIOMode.class,
                this::getAllowFlow, this::setAllowFlow);

        syncManager.syncValue("filterMode", filterMode);
        syncManager.syncValue("ioMode", ioMode);

        var panelHandler = syncManager.syncedPanel("filterPanel", true,
                (sm, sh) -> itemFilter.getPanel(data, sm, settings));

        DynamicSyncHandler filterButton = new DynamicSyncHandler()
                .widgetProvider((sm, buf) -> new ButtonWidget<>()
                        .onMousePressed((context, b) -> {
                            panelHandler.openPanel();
                            return true;
                        }));

        column.child(coverUIRow().child(new DynamicSyncedWidget<>().syncHandler(filterButton)));

        column.child(new GTMuiWidgets.EnumRowBuilder<>(FilterMode.class)
                .value(filterMode)
                .overlay(16, GTGuiTextures.FILTER_MODE_OVERLAY)
                .lang(Text.dynamic(() -> Component.translatable(getFilterMode().getTooltip())))
                .build());

        column.child(new GTMuiWidgets.EnumRowBuilder<>(ManualIOMode.class)
                .value(ioMode)
                .overlay(16, GTGuiTextures.MANUAL_IO_OVERLAY_IN)
                .lang(Text.dynamic(() -> Component.translatable(getAllowFlow().name())))
                .build());
    }

    private class FilteredItemHandlerWrapper extends ItemHandlerDelegate {

        public FilteredItemHandlerWrapper(IItemHandlerModifiable delegate) {
            super(delegate);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (filterMode == FilterMode.FILTER_EXTRACT) {
                if (allowFlow == ManualIOMode.DISABLED) {
                    return stack;
                }
                if (allowFlow == ManualIOMode.UNFILTERED) {
                    return super.insertItem(slot, stack, simulate);
                }
            }
            if (!getItemFilter().test(stack)) {
                return stack;
            }
            return super.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (filterMode == FilterMode.FILTER_INSERT) {
                if (allowFlow == ManualIOMode.DISABLED) {
                    return ItemStack.EMPTY;
                }
                if (allowFlow == ManualIOMode.UNFILTERED) {
                    return super.extractItem(slot, amount, simulate);
                }
            }
            ItemStack result = super.extractItem(slot, amount, true);
            if (result.isEmpty() || !getItemFilter().test(result)) {
                return ItemStack.EMPTY;
            }
            return simulate ? result : super.extractItem(slot, amount, false);
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
        itemFilter = ItemFilter.loadFilter(ItemStack.of(tag.getCompound("filter")));
        super.pasteConfig(player, tag);
    }
}
