package com.gregtechceu.gtceu.common.machine.storage;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.*;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.value.sync.SyncHandlers;
import com.gregtechceu.gtceu.api.mui.widget.ParentWidget;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ItemSlot;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTGuis;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.RequireRerender;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CrateMachine extends MetaMachine implements IMuiMachine, IMachineLife,
                          IDropSaveMachine, IInteractedMachine {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(CrateMachine.class,
            MetaMachine.MANAGED_FIELD_HOLDER);

    public static final BooleanProperty TAPED_PROPERTY = BooleanProperty.create("taped");

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Getter
    private final Material material;
    @Getter
    private final int inventorySize;
    @Getter
    private final int rowLength;
    @Getter
    @RequireRerender
    @Persisted
    @DescSynced
    private boolean isTaped;

    @Persisted
    public final NotifiableItemStackHandler inventory;

    public CrateMachine(IMachineBlockEntity holder, Material material, int inventorySize, int rowLength) {
        super(holder);
        this.material = material;
        this.inventorySize = inventorySize;
        this.rowLength = rowLength;
        this.inventory = new NotifiableItemStackHandler(this, inventorySize, IO.BOTH);
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        syncManager.registerSlotGroup("item_inv", inventorySize);

        int rows = inventorySize / rowLength;
        ParentWidget<?> slots = new ParentWidget<>();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < this.rowLength; j++) {
                int index = i * rowLength + j;
                slots.child(new ItemSlot()
                        .slot(SyncHandlers.itemSlot(inventory, index).slotGroup("item_inv"))
                        .left(18 * j)
                        .top(18 * i));
            }
        }

        return GTGuis.createPanel(this, rowLength * 18 + 14, 18 + 4 * 18 + 5 + 14 + 18 * rows)
                .background(GTGuiTextures.BACKGROUND_STEEL)
                .child(IKey.lang(getBlockState().getBlock().getName()).asWidget().pos(5, 5))
                .child(slots.top(18).left(7).right(7).height(rows * 18))
                .bindPlayerInventory();
    }

    /*
     * @Override
     * public ModularUI createUI(Player entityPlayer) {
     * int xOffset = inventorySize >= 90 ? 162 : 0;
     * int yOverflow = xOffset > 0 ? 18 : 9;
     * int yOffset = inventorySize > 3 * yOverflow ?
     * (inventorySize - 3 * yOverflow - (inventorySize - 3 * yOverflow) % yOverflow) / yOverflow * 18 : 0;
     * var modularUI = new ModularUI(176 + xOffset, 166 + yOffset, this, entityPlayer)
     * .background(GuiTextures.BACKGROUND)
     * .widget(new LabelWidget(5, 5, getBlockState().getBlock().getDescriptionId()))
     * .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7 + xOffset / 2,
     * 82 + yOffset, true));
     * int x = 0;
     * int y = 0;
     * for (int slot = 0; slot < inventorySize; slot++) {
     * modularUI.widget(new SlotWidget(inventory, slot, x * 18 + 7, y * 18 + 17)
     * .setBackgroundTexture(GuiTextures.SLOT));
     * x++;
     * if (x == yOverflow) {
     * x = 0;
     * y++;
     * }
     * }
     * return modularUI;
     * }
     */

    @Override
    public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
                                   BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isCrouching() && !isTaped) {
            if (stack.is(GTItems.DUCT_TAPE.asItem()) || stack.is(GTItems.BASIC_TAPE.asItem())) {
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                isTaped = true;
                setRenderState(getRenderState().setValue(TAPED_PROPERTY, isTaped));
                return InteractionResult.sidedSuccess(world.isClientSide);
            }
        }
        return IInteractedMachine.super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public void onMachinePlaced(@Nullable LivingEntity player, ItemStack stack) {
        IMachineLife.super.onMachinePlaced(player, stack);
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            this.isTaped = tag.contains("taped") && tag.getBoolean("taped");
            if (isTaped) {
                this.inventory.storage.deserializeNBT(tag.getCompound("inventory"));
            }

            tag.remove("taped");
            this.isTaped = false;
            setRenderState(getRenderState().setValue(TAPED_PROPERTY, isTaped));
        }
        stack.setTag(null);
    }

    @Override
    public void saveToItem(CompoundTag tag) {
        if (isTaped) {
            IDropSaveMachine.super.saveToItem(tag);
            tag.putBoolean("taped", isTaped);
            tag.put("inventory", inventory.storage.serializeNBT());
        }
    }

    @Override
    public boolean saveBreak() {
        return isTaped;
    }

    @Override
    public void onMachineRemoved() {
        if (!isTaped) clearInventory(inventory.storage);
    }
}
