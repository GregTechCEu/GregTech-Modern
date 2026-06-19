package com.gregtechceu.gtceu.api.cover.filter;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.machine.MachineCoverContainer;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.sync_system.SyncDataHolder;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.sync_system.managed.ISyncManaged;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A filter handler represents a slot that can hold filters for a specific type of stack/object.
 * 
 * @param <T> The stack/object type this filter handler holds filters for.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FilterHandler<T> implements ISyncManaged {

    @Getter
    private final SyncDataHolder syncDataHolder = new SyncDataHolder(this);

    private final ISyncManaged container;
    private final Class<T> filterableType;

    @SaveField
    @SyncToClient
    @Getter
    private ItemStack filterItem = ItemStack.EMPTY;

    private @Nullable Filter<T> filter;
    private @Nullable CustomItemStackHandler filterSlot;

    private Consumer<Filter<T>> onFilterLoaded = (filter) -> {};
    private Runnable onFilterRemoved = () -> {};
    private Consumer<Filter<T>> onFilterUpdated = (filter) -> {};

    /**
     *
     * @param container      The machine/pipe/cover/etc this filter handler is attached to.
     * @param filterableType The stack/object type which this filter handler should hold filters for.
     */
    public FilterHandler(ISyncManaged container, Class<T> filterableType) {
        this.container = container;
        this.filterableType = filterableType;
    }

    public Filter<T> loadFilter(ItemStack filterItem) {
        return Filters.loadFilter(filterableType, filterItem);
    }

    //////////////////////////////////
    // ***** PUBLIC API ******//
    //////////////////////////////////

    public boolean canInsertFilterItem(ItemStack itemStack) {
        return Filters.isValidFilter(filterableType, itemStack.getItem());
    }

    public boolean isFilterPresent() {
        return filter != null || !filterItem.isEmpty();
    }

    public Filter<T> getFilter() {
        if (this.filter == null) {
            if (this.filterItem.isEmpty()) {
                return Filters.getEmptyFilter();
            } else {
                loadFilterFromItem();
            }
        }

        return this.filter;
    }

    public boolean test(T resource) {
        return getFilter().test(resource);
    }

    public FilterHandler<T> onFilterLoaded(Consumer<Filter<T>> onFilterLoaded) {
        this.onFilterLoaded = onFilterLoaded;
        return this;
    }

    public FilterHandler<T> onFilterRemoved(Runnable onFilterRemoved) {
        this.onFilterRemoved = onFilterRemoved;
        return this;
    }

    public FilterHandler<T> onFilterUpdated(Consumer<Filter<T>> onFilterUpdated) {
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
    public @Nullable ISyncManaged getParentSyncObject() {
        return container;
    }
}
