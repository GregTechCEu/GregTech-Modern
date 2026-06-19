package com.gregtechceu.gtceu.api.cover.filter;

import com.gregtechceu.gtceu.GTCEu;
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
    @Getter
    private final Class<T> filterableType;

    @SaveField
    @SyncToClient
    @Getter
    private ItemStack filterItem = ItemStack.EMPTY;

    private @Nullable Filter<T> filter;
    @Getter
    private CustomItemStackHandler filterSlot;

    private Consumer<Filter<T>> onFilterLoaded = (filter) -> {};
    private Runnable onFilterRemoved = () -> {};
    private Consumer<Filter<T>> onFilterUpdated = (filter) -> {};

    /**
     * @param container      The machine/pipe/cover/etc this filter handler is attached to.
     * @param filterableType The stack/object type which this filter handler should hold filters for.
     */
    public FilterHandler(ISyncManaged container, Class<T> filterableType) {
        this.container = container;
        this.filterableType = filterableType;

        this.filterSlot = new CustomItemStackHandler(this.filterItem) {

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };

        this.filterSlot.setOnContentsChanged(this::updateFilter);
        this.filterSlot.setFilter(this::canInsertFilterItem);
    }

    //////////////////////////////////
    // ***** PUBLIC API ******//
    //////////////////////////////////

    public boolean canInsertFilterItem(ItemStack itemStack) {
        return Filters.isValidFilter(filterableType, itemStack.getItem());
    }

    public boolean isFilterPresent() {
        return filter != null;
    }

    public Filter<T> getFilter() {
        if (this.filter == null) return Filters.getEmptyFilter();
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

    public void setFilterItem(ItemStack item) {
        filterSlot.setStackInSlot(0, item);
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

        if (!this.filterItem.isEmpty()) {
            this.filter = Filters.loadFilter(filterableType, filterItem);
            filter.onFilterLoaded(this);
            filter.setOnUpdated(this.onFilterUpdated);
            this.onFilterLoaded.accept(this.filter);
        }
    }

    @Override
    public @Nullable ISyncManaged getParentSyncObject() {
        return container;
    }
}
