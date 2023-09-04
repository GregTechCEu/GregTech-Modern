package com.gregtechceu.gtceu.common.pipelike.enderlink;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.StatusWidget;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.item.EnderLinkCardBehavior;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.EnderLinkControllerMachine;
import com.gregtechceu.gtceu.data.lang.LangHandler;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.syncdata.IContentChangeAware;
import com.lowdragmc.lowdraglib.syncdata.IManaged;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.FieldManagedStorage;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class EnderLinkCardReader implements IManaged, IContentChangeAware {

    @Getter @Setter
    private Runnable onContentsChanged = () -> {};

    private final Consumer<Optional<EnderLinkControllerMachine>> controllerChangeCallback;

    @Persisted
    private final ItemStackTransfer linkCardSlot;

    @Nullable
    @DescSynced
    private UUID controllerUUID = null;

    public EnderLinkCardReader(Consumer<Optional<EnderLinkControllerMachine>> onControllerChanged) {
        this.controllerChangeCallback = onControllerChanged;

        linkCardSlot = new ItemStackTransfer(1) {
            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };
        linkCardSlot.setFilter(EnderLinkCardReader::isValidLinkCard);
    }

    private static boolean isValidLinkCard(ItemStack stack) {
        return GTItems.ENDER_LINK_CARD.isIn(stack) && EnderLinkCardBehavior.readData(stack).isPresent();
    }

    //////////////////////////////////////
    //**********   BEHAVIOR   **********//
    //////////////////////////////////////


    public void searchController() {
        updateControllerData();
    }

    public List<ItemStack> getDroppedItems() {
        ItemStack linkCardStack = linkCardSlot.getStackInSlot(0).copy();
        return linkCardStack.isEmpty() ? List.of() : List.of(linkCardStack);
    }

    public WidgetGroup createUI(int x, int y) {
        WidgetGroup widgetGroup = new WidgetGroup(x, y, 36, 18);

        widgetGroup.addWidget(new StatusWidget(0, 0, this::getStatus));

        var cardSlot = new SlotWidget(linkCardSlot, 0, 18, 0, true, true)
                .setChangeListener(this::updateControllerData)
                .setBackgroundTexture(new GuiTextureGroup(GuiTextures.SLOT, GuiTextures.ENDER_LINK_CARD_SLOT_OVERLAY));
        widgetGroup.addWidget(cardSlot);

        return widgetGroup;
    }

    private StatusWidget.Status getStatus() {
        if (this.controllerUUID == null)
            return StatusWidget.StatusType.NONE.toStatus();

        var controller = EnderLinkControllerRegistry.getController(controllerUUID).orElse(null);

        if (controller == null)
            return StatusWidget.StatusType.ERROR.toStatus(List.copyOf(LangHandler.getSingleOrMultiLang("ender_link.status.not_loaded")));

        if (!controller.isFormed())
            return StatusWidget.StatusType.WARNING.toStatus(List.copyOf(LangHandler.getSingleOrMultiLang("ender_link.status.not_formed")));

        return StatusWidget.StatusType.OK.toStatus();
    }

    private void updateControllerData() {
        this.controllerUUID = EnderLinkCardBehavior
                .readData(linkCardSlot.getStackInSlot(0))
                .map(EnderLinkControllerData::uuid)
                .orElse(null);

        this.controllerChangeCallback.accept(EnderLinkControllerRegistry.getController(controllerUUID));
    }


    //////////////////////////////////////
    //*****     LDLib SyncData    ******//
    //////////////////////////////////////

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(EnderLinkCardReader.class);

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Getter
    private final FieldManagedStorage syncStorage = new FieldManagedStorage(this);

    @Override
    public void onChanged() {

    }
}
