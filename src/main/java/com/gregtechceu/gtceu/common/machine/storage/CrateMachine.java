package com.gregtechceu.gtceu.common.machine.storage;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.*;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.sync_system.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.component.ItemContainerContents;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.SyncHandlers;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.slot.ItemSlot;
import lombok.Getter;

public class CrateMachine extends MetaMachine implements IMuiMachine {

    @Getter
    private final Material material;
    @Getter
    private final int inventorySize;
    @Getter
    private final int rowLength;
    @Getter
    @RerenderOnChanged
    @SaveField
    @SyncToClient
    private boolean isTaped;

    @SaveField
    public final NotifiableItemStackHandler inventory;

    public CrateMachine(BlockEntityCreationInfo info, Material material, int inventorySize, int rowLength) {
        super(info);
        this.material = material;
        this.inventorySize = inventorySize;
        this.rowLength = rowLength;
        this.inventory = attachTrait(new NotifiableItemStackHandler(inventorySize, IO.BOTH));
    }

    @Override
    public MachineUIPanelBuilder getPanelBuilder(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return MachineUIPanelBuilder.panelBuilder(this).addTitleBar(false);
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        syncManager.registerSlotGroup("item_inv", inventorySize);

        int rows = inventorySize / rowLength;
        ParentWidget<?> slots = new ParentWidget<>();
        slots.coverChildren();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < this.rowLength; j++) {
                int index = i * rowLength + j;
                slots.child(new ItemSlot()
                        .slot(SyncHandlers.itemSlot(inventory, index).slotGroup("item_inv"))
                        .left(18 * j)
                        .top(18 * i));
            }
        }

        var col = Flow.col()
                .margin(5, 5, 0, 5)
                .coverChildren();
        col.child(
                Text.of(getBlockState().getBlock().getName()).asWidget().leftRel(0).margin(0, 0, 3, 3))
                .child(slots.height(rows * 18));
        mainWidget.child(col);
    }

    @Override
    public InteractionResult onUseWithItem(ExtendedUseOnContext context) {
        var stack = context.getItemInHand();
        var player = context.getPlayer();
        if (stack.is(GTItems.DUCT_TAPE.asItem()) || stack.is(GTItems.BASIC_TAPE.asItem())) {
            if (player != null && player.isCrouching() && !isTaped) {
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                isTaped = true;
                inventory.shouldDropInventoryInWorld(false);
                setRenderState(getRenderState().setValue(GTMachineModelProperties.IS_TAPED, isTaped));
                syncDataHolder.markClientSyncFieldDirty("isTaped");
                return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
            }
        }
        return super.onUseWithItem(context);
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
        if (componentInput.get(GTDataComponents.TAPED) != null &&
                componentInput.get(DataComponents.CONTAINER) != null) {
            var contents = componentInput.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
            contents.copyInto(inventory.storage.getStacks());
            setRenderState(getRenderState().setValue(GTMachineModelProperties.IS_TAPED, false));
        }
    }

    @Override
    public void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (isTaped) {
            components.set(GTDataComponents.TAPED, Unit.INSTANCE);
            components.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(inventory.storage.getStacks()));
        }
    }
}
