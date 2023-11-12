package com.gregtechceu.gtceu.api.cover.filter;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.syncdata.IEnhancedManaged;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.FieldManagedStorage;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class FilterHandler<T, F extends Filter<T, F>> implements IEnhancedManaged {

    private final IEnhancedManaged container;

    @Persisted @DescSynced @Getter
    private @NotNull ItemStack filterItem = ItemStack.EMPTY;

    private @Nullable F filter;
    private @Nullable ItemStackTransfer filterSlot;
    private @Nullable WidgetGroup filterGroup;

    private @NotNull Consumer<F> onFilterLoaded = (filter) -> {};
    private @NotNull Consumer<F> onFilterRemoved = (filter) -> {};
    private @NotNull Consumer<F> onFilterUpdated = (filter) -> {};

    public FilterHandler(IEnhancedManaged container) {
        this.container = container;
    }

    protected abstract F loadFilter(ItemStack filterItem);
    protected abstract F getEmptyFilter();
    protected abstract boolean canInsertFilterItem(ItemStack itemStack);


    //////////////////////////////////
    //*****     PUBLIC API    ******//
    //////////////////////////////////

    public Widget createFilterSlotUI(int xPos, int yPos) {
        return new SlotWidget(getFilterSlot(), 0, xPos, yPos)
                .setChangeListener(this::updateFilter)
                .setBackgroundTexture(new GuiTextureGroup(GuiTextures.SLOT, GuiTextures.FILTER_SLOT_OVERLAY));
    }

    public Widget createFilterConfigUI(int xPos, int yPos, int width, int height) {
        this.filterGroup = new WidgetGroup(xPos, yPos, width, height);
        if (!this.filterItem.isEmpty()) {
            this.filterGroup.addWidget(getFilter().openConfigurator(0, 0));
        }

        return this.filterGroup;
    }

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

    public FilterHandler<T, F> onFilterRemoved(Consumer<F> onFilterRemoved) {
        this.onFilterRemoved = onFilterRemoved;
        return this;
    }

    public FilterHandler<T, F> onFilterUpdated(Consumer<F> onFilterUpdated) {
        this.onFilterUpdated = onFilterUpdated;
        return this;
    }

    ///////////////////////////////////////
    //*****     FILTER HANDLING    ******//
    ///////////////////////////////////////

    private ItemStackTransfer getFilterSlot() {
        if (this.filterSlot == null) {
            this.filterSlot = new ItemStackTransfer(this.filterItem);

            this.filterSlot.setFilter(this::canInsertFilterItem);
        }

        return this.filterSlot;
    }

    private void updateFilter() {
        var filterContainer = getFilterSlot();

        if (LDLib.isRemote()) {
            if (!filterContainer.getStackInSlot(0).isEmpty() && !this.filterItem.isEmpty()) {
                return;
            }
        }

        this.filterItem = filterContainer.getStackInSlot(0);

        if (this.filter != null) {
            this.filter = null;
            this.onFilterRemoved.accept(this.filter);
        }

        loadFilterFromItem();
    }

    private void loadFilterFromItem() {
        if (!this.filterItem.isEmpty()) {
            this.filter = loadFilter(this.filterItem);
            filter.setOnUpdated(this.onFilterUpdated);

            this.onFilterLoaded.accept(this.filter);
        }
        updateFilterGroupUI();
    }

    private void updateFilterGroupUI() {
        if (this.filterGroup == null)
            return;

        this.filterGroup.clearAllWidgets();

        if (!this.filterItem.isEmpty() && this.filter != null) {
            this.filterGroup.addWidget(this.filter.openConfigurator(0, 0));
        }
    }


    //////////////////////////////////////
    //*****     LDLib SyncData    ******//
    //////////////////////////////////////

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(FilterHandler.class);

    @Getter
    private final FieldManagedStorage syncStorage = new FieldManagedStorage(this);

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onChanged() {
        this.container.onChanged();
    }

    @Override
    public void scheduleRenderUpdate() {
        this.container.scheduleRenderUpdate();
    }
}
