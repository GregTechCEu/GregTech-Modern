package com.gregtechceu.gtceu.common.item.behavior;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.common.item.datacomponents.BindingData;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class DataItemBehavior implements IInteractionItem {

    public static final DataItemBehavior INSTANCE = new DataItemBehavior();

    protected DataItemBehavior() {}

    @Override
    public InteractionResultHolder<ItemStack> use(ItemStack item, Level level, Player player,
                                                  InteractionHand usedHand) {
        if (player.isShiftKeyDown()) {
            ItemStack stack = player.getItemInHand(usedHand);
            int permissionLevel = 0;
            while (player.hasPermissions(permissionLevel)) permissionLevel++;

            stack.set(GTDataComponents.BINDING_DATA, new BindingData(permissionLevel, player.getUUID()));
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }
        return IInteractionItem.super.use(item, level, player, usedHand);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context) {
        ICoverable coverable = GTCapabilityHelper.getCoverable(context.getLevel(), context.getClickedPos(),
                context.getClickedFace());
        if (coverable != null &&
                coverable.getCoverAtSide(context.getClickedFace()) instanceof IDataStickInteractable interactable) {
            if (context.isSecondaryUseActive()) {
                if (!itemStack.has(GTDataComponents.RESEARCH_ITEM)) {
                    return interactable.onDataStickShiftUse(context.getPlayer(), itemStack);
                }
            } else {
                return interactable.onDataStickUse(context.getPlayer(), itemStack);
            }
        }
        if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof MetaMachineBlockEntity blockEntity) {
            var machine = blockEntity.getMetaMachine();
            if (!MachineOwner.canOpenOwnerMachine(context.getPlayer(), machine)) {
                return InteractionResult.FAIL;
            }
            if (machine instanceof IDataStickInteractable interactable) {
                if (context.isSecondaryUseActive()) {
                    if (!itemStack.has(GTDataComponents.RESEARCH_ITEM)) {
                        return interactable.onDataStickShiftUse(context.getPlayer(), itemStack);
                    }
                } else {
                    return interactable.onDataStickUse(context.getPlayer(), itemStack);
                }
            } else {
                return InteractionResult.PASS;
            }
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }
}
