package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.LogicalSidedProvider;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.NotNull;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class SCPacketMonitorGroupNBTChange implements CustomPacketPayload {

    public static final ResourceLocation ID = GTCEu.id("monitor_group_nbt_change");
    public static final Type<SCPacketMonitorGroupNBTChange> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SCPacketMonitorGroupNBTChange> CODEC = StreamCodec
            .composite(
                    ItemStack.OPTIONAL_STREAM_CODEC, SCPacketMonitorGroupNBTChange::getStack,
                    ByteBufCodecs.VAR_INT, SCPacketMonitorGroupNBTChange::getMonitorGroupId,
                    BlockPos.STREAM_CODEC, SCPacketMonitorGroupNBTChange::getPos,
                    SCPacketMonitorGroupNBTChange::new);

    @Getter(AccessLevel.PRIVATE)
    private final ItemStack stack;
    @Getter(AccessLevel.PRIVATE)
    private final int monitorGroupId;
    @Getter(AccessLevel.PRIVATE)
    private final BlockPos pos;

    public SCPacketMonitorGroupNBTChange(ItemStack stack, MonitorGroup group, CentralMonitorMachine machine) {
        this(stack, machine.getMonitorGroups().indexOf(group), machine.getPos());
    }

    public SCPacketMonitorGroupNBTChange(ItemStack stack, int monitorGroupId, BlockPos pos) {
        this.stack = stack;
        this.monitorGroupId = monitorGroupId;
        this.pos = pos;
    }

    public void execute(IPayloadContext context) {
        Level level = context.player().level();

        MetaMachine machine = MetaMachine.getMachine(level, this.pos);
        if (machine instanceof CentralMonitorMachine centralMonitor) {
            IItemHandlerModifiable itemHandler = centralMonitor.getMonitorGroups()
                    .get(this.monitorGroupId)
                    .getItemStackHandler();
            if (ItemStack.isSameItem(itemHandler.getStackInSlot(0), this.stack)) {
                itemHandler.setStackInSlot(0, this.stack);
            }
        }
    }

    @Override
    public @NotNull Type<SCPacketMonitorGroupNBTChange> type() {
        return TYPE;
    }
}
