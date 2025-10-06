package com.gregtechceu.gtceu.common.mui.factory;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.mui.base.IUIHolder;
import com.gregtechceu.gtceu.api.mui.factory.AbstractUIFactory;
import com.gregtechceu.gtceu.api.mui.factory.GuiManager;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MachineUIFactory extends AbstractUIFactory<PosGuiData> {

    public static final MachineUIFactory INSTANCE = new MachineUIFactory();

    private MachineUIFactory() {
        super(GTCEu.id("machine"));
    }

    public void open(ServerPlayer player, IMuiMachine machine) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(machine);
        if (machine.self().isInValid()) {
            throw new IllegalArgumentException("Can't open invalid MetaMachine GUI!");
        }
        if (player.level() != machine.self().getLevel()) {
            throw new IllegalArgumentException("MetaMachine must be in same dimension as the player!");
        }
        BlockPos pos = machine.self().getPos();
        PosGuiData data = new PosGuiData(player, pos);
        GuiManager.open(this, data, player);
    }

    public void open(Player player, BlockPos pos) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(pos);
        PosGuiData data = new PosGuiData(player, pos);
        GuiManager.open(this, data, (ServerPlayer) player);
    }

    @Override
    public @NotNull IUIHolder<PosGuiData> getGuiHolder(PosGuiData data) {
        return Objects.requireNonNull(castUIHolder(getMachine(data)), "Found MetaMachine is not a gui holder!");
    }

    @Override
    public boolean canInteractWith(Player player, PosGuiData guiData) {
        return player == guiData.getPlayer() && getMachine(guiData) != null &&
                guiData.getSquaredDistance(player) <= 8 * 8;
    }

    @Override
    public void writeGuiData(PosGuiData guiData, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(guiData.getBlockPos());
    }

    @Override
    public @NotNull PosGuiData readGuiData(Player player, FriendlyByteBuf buffer) {
        return new PosGuiData(player, buffer.readBlockPos());
    }

    public static MetaMachine getMachine(PosGuiData data) {
        return MetaMachine.getMachine(data.getLevel(), data.getBlockPos());
    }
}
