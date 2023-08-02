package com.gregtechceu.gtceu.common.cover.voiding;

import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IUICover;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandler;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandlers;
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.common.cover.ConveyorCover;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemVoidingCover extends ConveyorCover implements IUICover, IControllable {
    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(ItemVoidingCover.class, CoverBehavior.MANAGED_FIELD_HOLDER);


    @Persisted @Getter
    protected boolean isWorkingEnabled = true;
    @Persisted @Getter
    protected boolean isEnabled = false;

    @Persisted @DescSynced
    protected final FilterHandler<ItemStack, ItemFilter> filterHandler;
    protected final ConditionalSubscriptionHandler subscriptionHandler;

    public ItemVoidingCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide, 0);

        this.subscriptionHandler = new ConditionalSubscriptionHandler(coverHolder, this::update, this::isSubscriptionActive);
        this.filterHandler = FilterHandlers.item(this).onFilterLoaded(f -> configureFilterHandler());
    }

    private boolean isSubscriptionActive() {
        return isWorkingEnabled() && isEnabled();
    }

    @Nullable
    protected IItemTransfer getItemTransfer() {
        return ItemTransferHelper.getItemTransfer(coverHolder.getLevel(), coverHolder.getPos(), attachedSide);
    }

    //////////////////////////////////////
    //*****     Initialization    ******//
    //////////////////////////////////////

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public boolean canAttach() {
        return super.canAttach();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        subscriptionHandler.initialize(coverHolder.getLevel());
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        subscriptionHandler.unsubscribe();
    }


    @Override
    public List<ItemStack> getAdditionalDrops() {
        var list = super.getAdditionalDrops();
        if (!filterHandler.getFilterItem().isEmpty()) {
            list.add(filterHandler.getFilterItem());
        }
        return list;
    }

    //////////////////////////////////////////////
    //***********     COVER LOGIC    ***********//
    //////////////////////////////////////////////

    private void update() {
        if (coverHolder.getOffsetTimer() % 5 != 0)
            return;

        doVoidItems();
        subscriptionHandler.updateSubscription();
    }

    protected void doVoidItems() {
        IItemTransfer itemTransfer = getItemTransfer();
        if (itemTransfer == null) {
            return;
        }
        voidAny(itemTransfer);
    }

    void voidAny(IItemTransfer itemTransfer) {
        ItemFilter filter = filterHandler.getFilter();

        for (int slot = 0; slot < itemTransfer.getSlots(); slot++) {
            ItemStack sourceStack = itemTransfer.extractItem(slot, Integer.MAX_VALUE, true);
            if (sourceStack.isEmpty() || !filter.test(sourceStack)) {
                continue;
            }
            itemTransfer.extractItem(slot, Integer.MAX_VALUE, false);
        }
    }

    public void setWorkingEnabled(boolean workingEnabled) {
        isWorkingEnabled = workingEnabled;
        subscriptionHandler.updateSubscription();
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
        subscriptionHandler.updateSubscription();
    }

    //////////////////////////////////////
    //***********     GUI    ***********//
    //////////////////////////////////////

    @Override
    public Widget createUIWidget() {
        final var group = new WidgetGroup(0, 0, 176, 120);
        group.addWidget(new LabelWidget(10, 5, getUITitle()));

        group.addWidget(new ToggleButtonWidget(10, 20, 20, 20,
                GuiTextures.BUTTON_POWER, this::isEnabled, this::setEnabled));

        //group.addWidget(filterHandler.createFilterSlotUI(36, 21));
        group.addWidget(filterHandler.createFilterSlotUI(148, 91));
        group.addWidget(filterHandler.createFilterConfigUI(10, 50, 126, 60));

        buildAdditionalUI(group);

        return group;
    }

    @NotNull
    protected String getUITitle() {
        return "cover.conveyor.title";
    }

    protected void buildAdditionalUI(WidgetGroup group) {
        // Do nothing in the base implementation. This is intended to be overridden by subclasses.
    }

    protected void configureFilterHandler() {
        // Do nothing in the base implementation. This is intended to be overridden by subclasses.
    }
}
