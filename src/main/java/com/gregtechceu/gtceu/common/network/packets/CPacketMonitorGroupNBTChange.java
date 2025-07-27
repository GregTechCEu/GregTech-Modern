package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;
import com.gregtechceu.gtceu.common.network.GTNetwork;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class CPacketMonitorGroupNBTChange implements GTNetwork.INetPacket {

    private final ItemStack stack;
    private final int monitorGroupId;
    private final BlockPos pos;

    public CPacketMonitorGroupNBTChange(ItemStack stack, MonitorGroup group, CentralMonitorMachine machine) {
        this.stack = stack;
        this.monitorGroupId = machine.getMonitorGroups().indexOf(group);
        this.pos = machine.getPos();
    }

    public CPacketMonitorGroupNBTChange(FriendlyByteBuf buf) {
        this.stack = buf.readItem();
        this.monitorGroupId = buf.readInt();
        this.pos = buf.readBlockPos();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeItemStack(stack, false);
        buffer.writeInt(monitorGroupId);
        buffer.writeBlockPos(pos);
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        if (context.getDirection() != NetworkDirection.PLAY_TO_SERVER) return;
        if (context.getSender() == null) return;
        if (context.getSender().serverLevel().getBlockEntity(pos) instanceof IMachineBlockEntity machine) {
            if (machine.getMetaMachine() instanceof CentralMonitorMachine centralMonitor) {
                centralMonitor.getMonitorGroups().get(monitorGroupId).getItemStackHandler().setStackInSlot(0, stack);
            }
        }
    }
}
