package com.gregtechceu.gtceu.common.machine.storage;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.*;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.sync_system.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.ItemContainerContents;

import lombok.Getter;

public class CrateMachine extends MetaMachine implements IUIMachine {

    @Getter
    private final Material material;
    @Getter
    private final int inventorySize;
    @Getter
    @RerenderOnChanged
    @SaveField
    @SyncToClient
    private boolean isTaped;

    @SaveField
    public final NotifiableItemStackHandler inventory;

    public CrateMachine(BlockEntityCreationInfo info, Material material, int inventorySize) {
        super(info);
        this.material = material;
        this.inventorySize = inventorySize;
        this.inventory = attachTrait(new NotifiableItemStackHandler(inventorySize, IO.BOTH));
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        int xOffset = inventorySize >= 90 ? 162 : 0;
        int yOverflow = xOffset > 0 ? 18 : 9;
        int yOffset = inventorySize > 3 * yOverflow ?
                (inventorySize - 3 * yOverflow - (inventorySize - 3 * yOverflow) % yOverflow) / yOverflow * 18 : 0;
        var modularUI = new ModularUI(176 + xOffset, 166 + yOffset, this, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(new LabelWidget(5, 5, getBlockState().getBlock().getDescriptionId()))
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7 + xOffset / 2,
                        82 + yOffset, true));
        int x = 0;
        int y = 0;
        for (int slot = 0; slot < inventorySize; slot++) {
            modularUI.widget(new SlotWidget(inventory, slot, x * 18 + 7, y * 18 + 17)
                    .setBackgroundTexture(GuiTextures.SLOT));
            x++;
            if (x == yOverflow) {
                x = 0;
                y++;
            }
        }
        return modularUI;
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
