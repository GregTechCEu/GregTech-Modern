package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.api.item.jetpack.IJetpackLogic;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.utils.JetpackHelper;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class CPacketJetpackConfiguration implements GTNetwork.INetPacket {

    private final EquipmentSlot slot;
    private final Setting setting;
    private final boolean enabled;

    public CPacketJetpackConfiguration(EquipmentSlot slot, Setting setting, boolean enabled) {
        this.slot = slot;
        this.setting = setting;
        this.enabled = enabled;
    }

    public CPacketJetpackConfiguration(FriendlyByteBuf buf) {
        this.slot = buf.readEnum(EquipmentSlot.class);
        this.setting = buf.readEnum(Setting.class);
        this.enabled = buf.readBoolean();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(this.slot);
        buf.writeEnum(this.setting);
        buf.writeBoolean(this.enabled);
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        ServerPlayer sender = context.getSender();
        if (sender != null && this.slot.isArmor()) {
            ItemStack stack = sender.getItemBySlot(this.slot);
            IJetpackLogic jetpack = JetpackHelper.getJetpackLogic(stack);
            if (jetpack != null) {
                switch (this.setting) {
                    case FLIGHT_ENABLED -> jetpack.setFlightEnabled(stack, this.enabled);
                    case HOVER_MODE_ENABLED -> {
                        if (jetpack.supportsHoverMode(stack)) {
                            jetpack.setHoverModeEnabled(stack, this.enabled);
                        }
                    }
                }
            }
        }
    }

    public enum Setting {
        FLIGHT_ENABLED,
        HOVER_MODE_ENABLED
    }
}
