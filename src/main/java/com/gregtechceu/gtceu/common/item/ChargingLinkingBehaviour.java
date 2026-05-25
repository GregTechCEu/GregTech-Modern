package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.electric.BatteryBufferMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.PowerSubstationMachine;
import com.gregtechceu.gtceu.common.machine.owner.PlayerOwner;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public class ChargingLinkingBehaviour implements IInteractionItem {

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        MetaMachine machine = MetaMachine.getMachine(context.getLevel(), pos);
        if (machine instanceof PowerSubstationMachine || machine instanceof BatteryBufferMachine) {
            PlayerOwner owner = machine.getPlayerOwner();
            Player player = context.getPlayer();
            if (owner == null || player != null && owner.isPlayerFriendly(player.getUUID())) {
                CompoundTag tag = itemStack.getOrCreateTagElement("LinkedCharger");
                tag.putInt("x", pos.getX());
                tag.putInt("y", pos.getY());
                tag.putInt("z", pos.getZ());
                tag.putString("dim", context.getLevel().dimension().location().toString());
                if (player != null) player.sendSystemMessage(Component.translatable("behaviour.charger_linked"));
            }
        }
        return IInteractionItem.super.onItemUseFirst(itemStack, context);
    }
}
