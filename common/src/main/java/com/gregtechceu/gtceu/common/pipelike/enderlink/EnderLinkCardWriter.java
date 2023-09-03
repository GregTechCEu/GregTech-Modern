package com.gregtechceu.gtceu.common.pipelike.enderlink;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.item.EnderLinkCardBehavior;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.EnderLinkControllerMachine;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.syncdata.IContentChangeAware;
import com.lowdragmc.lowdraglib.syncdata.IManaged;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.FieldManagedStorage;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.stream.Stream;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class EnderLinkCardWriter implements IManaged, IContentChangeAware {
    private final EnderLinkControllerMachine controller;

    @Getter @Setter
    private Runnable onContentsChanged = () -> {};

    @Persisted
    private final ItemStackTransfer linkCardInputSlot;
    @Persisted
    private final ItemStackTransfer linkCardOutputSlot;

    public EnderLinkCardWriter(EnderLinkControllerMachine controller) {
        this.controller = controller;

        linkCardInputSlot = new ItemStackTransfer(1);
        linkCardInputSlot.setFilter(GTItems.ENDER_LINK_CARD::isIn);
        linkCardOutputSlot = new ItemStackTransfer(1);
    }

    //////////////////////////////////////
    //**********   BEHAVIOR   **********//
    //////////////////////////////////////

    public List<ItemStack> getDroppedItems() {
        return Stream.of(
                linkCardInputSlot.getStackInSlot(0),
                linkCardOutputSlot.getStackInSlot(0)
        ).map(ItemStack::copy).filter(item -> !item.isEmpty()).toList();
    }


    public WidgetGroup createUI(int x, int y) {
        WidgetGroup widgetGroup = new WidgetGroup(x, y, 18, 43);

        var inputSlot = new SlotWidget(linkCardInputSlot, 0, 0, 0, true, true)
                .setChangeListener(this::handleInteraction)
                .setBackgroundTexture(new GuiTextureGroup(GuiTextures.SLOT, GuiTextures.ENDER_LINK_CARD_SLOT_OVERLAY));
        var outputSlot = new SlotWidget(linkCardOutputSlot, 0, 0, 25, true, false)
                .setChangeListener(this::handleInteraction);

        widgetGroup.addWidget(inputSlot);
        widgetGroup.addWidget(new ImageWidget(6, 30, 6, 3, GuiTextures.ARROW_HEAD_DOWN));
        widgetGroup.addWidget(outputSlot);

        return widgetGroup;
    }

    private void handleInteraction() {
        var controllerData = new EnderLinkControllerData(controller.getGlobalPosition(), controller.getUuid());

        var previousCount = linkCardInputSlot.getStackInSlot(0).getCount();
        var simExtracted = linkCardInputSlot.extractItem(0, linkCardInputSlot.getStackInSlot(0).getCount(), true);

        if (!simExtracted.isEmpty())
            EnderLinkCardBehavior.writeData(simExtracted, controllerData);

        var simInserted = linkCardOutputSlot.insertItem(0, simExtracted, true);

        var transferredCount = simInserted.isEmpty() ? previousCount : previousCount - simInserted.getCount();
        var extracted = linkCardInputSlot.extractItem(0, transferredCount, false);

        if (!extracted.isEmpty())
            EnderLinkCardBehavior.writeData(extracted, controllerData);

        linkCardOutputSlot.insertItem(0, extracted, false);

        onContentsChanged.run();
    }

    //////////////////////////////////////
    //*****     LDLib SyncData    ******//
    //////////////////////////////////////

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(EnderLinkCardWriter.class);

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Getter
    private final FieldManagedStorage syncStorage = new FieldManagedStorage(this);

    @Override
    public void onChanged() {
        // No implementation necessary
    }
}
