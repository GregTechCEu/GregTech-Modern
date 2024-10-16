package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IUICover;
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.gregtechceu.gtceu.api.transfer.item.ItemTransferDelegate;
import com.gregtechceu.gtceu.common.cover.data.FilterMode;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/3/13
 * @implNote ItemFilterCover
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemFilterCover extends CoverBehavior implements IUICover {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(ItemFilterCover.class,
            CoverBehavior.MANAGED_FIELD_HOLDER);

    protected ItemFilter itemFilter;
    @Persisted
    @DescSynced
    @Getter
    protected FilterMode filterMode = FilterMode.FILTER_INSERT;
    private FilteredItemTransferWrapper itemFilterWrapper;
    @Setter
    @Getter
    protected boolean allowFlow = false;

    public ItemFilterCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    public ItemFilter getItemFilter() {
        if (itemFilter == null) {
            itemFilter = ItemFilter.loadFilter(attachItem);
        }
        return itemFilter;
    }

    public void setFilterMode(FilterMode filterMode) {
        this.filterMode = filterMode;
        coverHolder.markDirty();
    }

    @Override
    public boolean canAttach() {
        return ItemTransferHelper.getItemTransfer(coverHolder.getLevel(), coverHolder.getPos(), attachedSide) != null;
    }

    @Override
    public @Nullable IItemTransfer getItemTransferCap(IItemTransfer defaultValue) {
        if (defaultValue == null) {
            return null;
        }
        if (itemFilterWrapper == null || itemFilterWrapper.delegate != defaultValue) {
            this.itemFilterWrapper = new ItemFilterCover.FilteredItemTransferWrapper(defaultValue);
        }
        return itemFilterWrapper;
    }

    @Override
    public Widget createUIWidget() {
        final var group = new WidgetGroup(0, 0, 176, 85);
        group.addWidget(new LabelWidget(7, 5, attachItem.getDescriptionId()));
        group.addWidget(new EnumSelectorWidget<>(7, 25, 18, 18,
                FilterMode.VALUES, filterMode, this::setFilterMode));
        group.addWidget(new ToggleButtonWidget(7, 45, 18, 18, GuiTextures.BUTTON, this::isAllowFlow, this::setAllowFlow));
        group.addWidget(getItemFilter().openConfigurator(48, 18));
        return group;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    private class FilteredItemTransferWrapper extends ItemTransferDelegate {

        public FilteredItemTransferWrapper(IItemTransfer delegate) {
            super(delegate);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate,
                                             boolean notifyChanges) {
            if (filterMode == FilterMode.FILTER_EXTRACT && allowFlow)
                return super.insertItem(slot, stack, simulate, notifyChanges);
            return getItemFilter().test(stack) ? super.insertItem(slot, stack, simulate, notifyChanges) : stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate, boolean notifyChanges) {
            ItemStack result = super.extractItem(slot, amount, true, notifyChanges);
            if (!result.isEmpty() && filterMode == FilterMode.FILTER_INSERT && allowFlow) {
                return super.extractItem(slot, amount, false, notifyChanges);
            }
            return simulate ? result :
                    (!getItemFilter().test(result) ? super.extractItem(slot, amount, false, notifyChanges) : ItemStack.EMPTY);
        }
    }
}
