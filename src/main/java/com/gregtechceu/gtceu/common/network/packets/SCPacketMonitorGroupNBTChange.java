package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;
import com.gregtechceu.gtceu.common.network.GTNetwork;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;

public class SCPacketMonitorGroupNBTChange implements GTNetwork.INetPacket {

    private final ItemStack stack;
    private final int monitorGroupId;
    private final BlockPos pos;

    public SCPacketMonitorGroupNBTChange(ItemStack stack, MonitorGroup group, CentralMonitorMachine machine) {
        this.stack = stack;
        this.monitorGroupId = machine.getMonitorGroups().indexOf(group);
        this.pos = machine.getPos();
    }

    public SCPacketMonitorGroupNBTChange(FriendlyByteBuf buf) {
        this.stack = buf.readItem();
        this.monitorGroupId = buf.readVarInt();
        this.pos = buf.readBlockPos();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeItemStack(stack, false);
        buffer.writeVarInt(monitorGroupId);
        buffer.writeBlockPos(pos);
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        Level level = LogicalSidedProvider.CLIENTWORLD.get(context.getDirection().getReceptionSide())
                .or(() -> Optional.ofNullable(context.getSender()).map(ServerPlayer::level))
                .orElse(null);
        if (level == null) return;

        MetaMachine machine = MetaMachine.getMachine(level, pos);
        if (machine instanceof CentralMonitorMachine centralMonitor) {
            centralMonitor.getMonitorGroups().get(monitorGroupId)
                    .getItemStackHandler().setStackInSlot(0, stack);
        }
    }

    private static class ClientCallWrapper {

        private static Level getClientLevel() {
            return Minecraft.getInstance().level;
        }
    }
}
