package com.gregtechceu.gtceu.common.machine.storage;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.*;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
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
import com.gregtechceu.gtceu.syncsystem.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.syncsystem.annotations.SaveField;
import com.gregtechceu.gtceu.syncsystem.annotations.SyncToClient;

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

    public static final BooleanProperty TAPED_PROPERTY = GTMachineModelProperties.IS_TAPED;

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
                .background(GTGuiTextures.BACKGROUND)
                .child(IKey.lang(getBlockState().getBlock().getName()).asWidget().pos(5, 5))
                .child(slots.top(18).left(7).right(7).height(rows * 18))
                .bindPlayerInventory();
    }

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
                setRenderState(getRenderState().setValue(GTMachineModelProperties.IS_TAPED, isTaped));
                syncDataHolder.markClientSyncFieldDirty("isTaped");
                return InteractionResult.sidedSuccess(world.isClientSide);
            }
        }
        return IInteractedMachine.super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public void onMachinePlaced(@Nullable LivingEntity player, ItemStack stack) {
        super.onMachinePlaced(player, stack);
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            if (tag.contains("taped") && tag.getBoolean("taped")) {
                this.inventory.storage.deserializeNBT(tag.getCompound("inventory"));
            }
            setRenderState(getRenderState().setValue(GTMachineModelProperties.IS_TAPED, isTaped));
            syncDataHolder.markClientSyncFieldDirty("isTaped");
        }
    }

    @Override
    public void saveToItem(CompoundTag tag) {
        if (isTaped) tag.put("inventory", inventory.storage.serializeNBT());
    }

    @Override
    public void loadFromItem(CompoundTag tag) {
        inventory.storage.deserializeNBT(tag.getCompound("inventory"));
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
