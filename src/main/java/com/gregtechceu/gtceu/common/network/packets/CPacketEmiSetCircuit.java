package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.api.machine.feature.IHasCircuitSlot;
import com.gregtechceu.gtceu.api.recipe.ingredient.item.IntCircuitIngredient;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.common.network.GTNetwork;

import com.lowdragmc.lowdraglib.gui.modular.ModularUIContainer;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class CPacketEmiSetCircuit implements GTNetwork.INetPacket {

    private final int containerId;
    private final int configuration;

    public CPacketEmiSetCircuit(int containerId, int configuration) {
        this.containerId = containerId;
        this.configuration = configuration;
    }

    public CPacketEmiSetCircuit(FriendlyByteBuf buffer) {
        containerId = buffer.readVarInt();
        configuration = buffer.readVarInt();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(containerId);
        buffer.writeVarInt(configuration);
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null || configuration < IntCircuitIngredient.CIRCUIT_MIN ||
                configuration > IntCircuitIngredient.CIRCUIT_MAX ||
                player.containerMenu.containerId != containerId ||
                !(player.containerMenu instanceof ModularUIContainer menu) ||
                !(menu.getModularUI().holder instanceof IHasCircuitSlot circuitMachine) ||
                !circuitMachine.isCircuitSlotEnabled()) {
            return;
        }
        circuitMachine.getCircuitInventory().setStackInSlot(0, IntCircuitBehaviour.stack(configuration));
        menu.broadcastChanges();
    }
}
