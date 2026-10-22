package com.gregtechceu.gtceu.api.mui.factory;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.mui.GTGuiScreen;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import brachy.modularui.api.IUIHolder;
import brachy.modularui.factory.AbstractUIFactory;
import brachy.modularui.factory.GuiManager;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.ModularScreen;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.gregtechceu.gtceu.GTCEu.MOD_ID;

public class MachineUIFactory extends AbstractUIFactory<PosGuiData> {

    public static final MachineUIFactory INSTANCE = new MachineUIFactory();

    private MachineUIFactory() {
        super(GTCEu.id("machine"));
    }

    public void open(ServerPlayer player, IMuiMachine machine) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(machine);
        if (machine.self().isRemoved()) {
            throw new IllegalArgumentException("Can't open invalid MetaMachine GUI!");
        }
        if (player.level() != machine.self().getLevel()) {
            throw new IllegalArgumentException("MetaMachine must be in same dimension as the player!");
        }
        BlockPos pos = machine.self().getBlockPos();
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
        MetaMachine machine = getMachine(data);
        if (machine.getDefinition().getUI() != null) {
            return machine.getDefinition().getUI();
        }
        return Objects.requireNonNull(castUIHolder(machine), "Found MetaMachine is not a gui holder!");
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

    @Override
    @OnlyIn(Dist.CLIENT)
    public ModularScreen createScreen(PosGuiData data, ModularPanel<?> mainPanel) {
        return new GTGuiScreen(MOD_ID, mainPanel, getThemeId(data));
    }

    public String getThemeId(PosGuiData data) {
        return getMachine(data).getDefinition().getThemeId();
    }
}
