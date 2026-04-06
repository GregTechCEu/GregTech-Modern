package com.gregtechceu.gtceu.common.item.behavior;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.common.item.datacomponents.BindingData;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;
import com.gregtechceu.gtceu.core.mixins.EntityAccessor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
    public InteractionResultHolder<ItemStack> use(ItemStack stack, Level level,
                                                  Player player, InteractionHand usedHand) {
        if (player.isSecondaryUseActive()) {
            int permissionLevel = ((EntityAccessor) player).gtceu$getPermissionLevel();
            stack.set(GTDataComponents.BINDING_DATA, new BindingData(permissionLevel, player.getUUID()));

            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        return IInteractionItem.super.use(stack, level, player, usedHand);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction face = context.getClickedFace();
        Player player = context.getPlayer();

        ICoverable coverable = GTCapabilityHelper.getCoverable(level, pos, face);
        if (coverable != null && coverable.getCoverAtSide(face) instanceof IDataStickInteractable interactable) {
            if (context.isSecondaryUseActive()) {
                if (!itemStack.has(GTDataComponents.RESEARCH_ITEM)) {
                    return interactable.onDataStickShiftUse(player, itemStack);
                }
            } else {
                return interactable.onDataStickUse(player, itemStack);
            }
        }

        MetaMachine machine = MetaMachine.getMachine(level, pos);
        if (machine != null) {
            if (!MachineOwner.canOpenOwnerMachine(player, machine)) {
                return InteractionResult.FAIL;
            }
            if (machine instanceof IDataStickInteractable interactable) {
                if (context.isSecondaryUseActive()) {
                    if (!itemStack.has(GTDataComponents.RESEARCH_ITEM)) {
                        return interactable.onDataStickShiftUse(player, itemStack);
                    }
                } else {
                    return interactable.onDataStickUse(player, itemStack);
                }
            }
        }
        return InteractionResult.PASS;
    }
}
