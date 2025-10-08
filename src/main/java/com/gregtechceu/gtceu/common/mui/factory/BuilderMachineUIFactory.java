package com.gregtechceu.gtceu.common.mui.factory;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.mui.base.IUIHolder;
import com.gregtechceu.gtceu.api.mui.factory.AbstractUIFactory;
import com.gregtechceu.gtceu.api.mui.factory.GuiManager;
import com.gregtechceu.gtceu.api.mui.factory.IMuiFactory;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BuilderMachineUIFactory extends AbstractUIFactory<PosGuiData> {

    public static final BuilderMachineUIFactory INSTANCE = new BuilderMachineUIFactory();

    private BuilderMachineUIFactory() {
        super(GTCEu.id("machine_factory"));
    }

    public void open(ServerPlayer player, BlockPos pos, IMuiFactory factory) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(factory);
        PosGuiData data = new PosGuiData(player, pos);
        GuiManager.open(this, data, player);
    }

    @Override
    public @NotNull IUIHolder<PosGuiData> getGuiHolder(PosGuiData data) {
        return getMachine(data).getDefinition().getUI();
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
