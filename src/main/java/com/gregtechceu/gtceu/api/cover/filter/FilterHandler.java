package com.gregtechceu.gtceu.api.cover.filter;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.machine.MachineCoverContainer;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.sync_system.ISyncManaged;
import com.gregtechceu.gtceu.api.sync_system.SyncDataHolder;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class FilterHandler<T, F extends Filter<T, F>> implements ISyncManaged {

    @Getter
    private final SyncDataHolder syncDataHolder = new SyncDataHolder(this);

    private final ISyncManaged container;

    @SaveField
    @SyncToClient
    @Getter
    private @NotNull ItemStack filterItem = ItemStack.EMPTY;

    private @Nullable F filter;
    private @Nullable CustomItemStackHandler filterSlot;

    private @NotNull Consumer<F> onFilterLoaded = (filter) -> {};
    private @NotNull Runnable onFilterRemoved = () -> {};
    private @NotNull Consumer<F> onFilterUpdated = (filter) -> {};

    public FilterHandler(ISyncManaged container) {
        this.container = container;
    }

    public abstract F loadFilter(ItemStack filterItem);

    protected abstract F getEmptyFilter();

    //////////////////////////////////
    // ***** PUBLIC API ******//
    //////////////////////////////////

    public abstract boolean canInsertFilterItem(ItemStack itemStack);

    public boolean isFilterPresent() {
        return filter != null || !filterItem.isEmpty();
    }

    public F getFilter() {
        if (this.filter == null) {
            if (this.filterItem.isEmpty()) {
                return getEmptyFilter();
            } else {
                loadFilterFromItem();
            }
        }

        return this.filter;
    }

    public boolean test(T resource) {
        return getFilter().test(resource);
    }

    public FilterHandler<T, F> onFilterLoaded(Consumer<F> onFilterLoaded) {
        this.onFilterLoaded = onFilterLoaded;
        return this;
    }

    public FilterHandler<T, F> onFilterRemoved(Runnable onFilterRemoved) {
        this.onFilterRemoved = onFilterRemoved;
        return this;
    }

    public FilterHandler<T, F> onFilterUpdated(Consumer<F> onFilterUpdated) {
        this.onFilterUpdated = onFilterUpdated;
        return this;
    }

    ///////////////////////////////////////
    // ***** FILTER HANDLING ******//
    ///////////////////////////////////////

    public CustomItemStackHandler getFilterSlot() {
        if (this.filterSlot == null) {
            this.filterSlot = new CustomItemStackHandler(this.filterItem) {

                @Override
                public int getSlotLimit(int slot) {
                    return 1;
                }
            };

            this.filterSlot.setFilter(this::canInsertFilterItem);
        }

        return this.filterSlot;
    }

    public void setFilterItem(ItemStack item) {
        getFilterSlot().setStackInSlot(0, item);
        updateFilter();
    }

    private void updateFilter() {
        var filterContainer = getFilterSlot();

        if (GTCEu.isClientThread()) {
            if (!filterContainer.getStackInSlot(0).isEmpty() && !this.filterItem.isEmpty()) {
                return;
            }
        }

        this.filterItem = filterContainer.getStackInSlot(0);
        syncDataHolder.markClientSyncFieldDirty("filterItem");

        if (this.filter != null) {
            this.filter = null;
            this.onFilterRemoved.run();
        }

        loadFilterFromItem();
    }

    private void loadFilterFromItem() {
        if (!this.filterItem.isEmpty()) {
            this.filter = loadFilter(this.filterItem);
            filter.setOnUpdated(this.onFilterUpdated);
            if (filter instanceof SmartItemFilter smart &&
                    container instanceof CoverBehavior cover &&
                    cover.coverHolder instanceof MachineCoverContainer mcc) {
                var machine = MetaMachine.getMachine(mcc.getLevel(), mcc.getBlockPos());
                if (machine != null) {
                    smart.setModeFromMachine(machine.getDefinition().getName());
                }
            }
            this.onFilterLoaded.accept(this.filter);
        }
    }

    @Override
    public void markAsChanged() {
        container.markAsChanged();
    }

    @Override
    public void scheduleRenderUpdate() {
        container.scheduleRenderUpdate();
    }
}
