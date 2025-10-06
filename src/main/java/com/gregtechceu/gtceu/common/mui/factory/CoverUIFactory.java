package com.gregtechceu.gtceu.common.mui.factory;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.IMuiCover;
import com.gregtechceu.gtceu.api.mui.base.IUIHolder;
import com.gregtechceu.gtceu.api.mui.factory.AbstractUIFactory;
import com.gregtechceu.gtceu.api.mui.factory.GuiManager;
import com.gregtechceu.gtceu.api.mui.factory.SidedPosGuiData;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CoverUIFactory extends AbstractUIFactory<SidedPosGuiData> {

    public static final CoverUIFactory INSTANCE = new CoverUIFactory();

    private CoverUIFactory() {
        super(GTCEu.id("cover"));
    }

    public void open(ServerPlayer player, IMuiCover cover) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(cover);
        if (cover.isInvalid()) {
            throw new IllegalArgumentException("Can't open Cover GUI on invalid cover holder!");
        }
        if (player.level() != cover.self().coverHolder.getLevel()) {
            throw new IllegalArgumentException("Cover must be in same dimension as the player!");
        }
        BlockPos pos = cover.self().coverHolder.getPos();
        Direction side = cover.self().attachedSide;
        SidedPosGuiData data = new SidedPosGuiData(player, pos, side);
        GuiManager.open(this, data, player);
    }

    @Override
    public @NotNull IUIHolder<SidedPosGuiData> getGuiHolder(SidedPosGuiData data) {
        BlockEntity be = data.getBlockEntity();
        if (be == null) {
            throw new IllegalStateException("Could not get gui for null BlockEntity!");
        }
        ICoverable coverHolder = be.getCapability(GTCapability.CAPABILITY_COVERABLE, data.getSide())
                .resolve().orElse(null);
        if (coverHolder == null) {
            throw new IllegalStateException("Could not get CoverHolder for found BlockEntity!");
        }
        CoverBehavior cover = coverHolder.getCoverAtSide(data.getSide());
        if (cover == null) {
            throw new IllegalStateException("Could not find cover at side " + data.getSide() +
                    " for found CoverHolder!");
        }
        if (!(cover instanceof IMuiCover uiCover)) {
            throw new IllegalStateException("Cover at side " + data.getSide() + " is not a gui holder!");
        }
        return uiCover;
    }

    @Override
    public boolean canInteractWith(Player player, SidedPosGuiData guiData) {
        return guiData.getSquaredDistance(player) <= 8 * 8;
    }

    @Override
    public void writeGuiData(SidedPosGuiData guiData, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(guiData.getBlockPos());
        buffer.writeByte(guiData.getSide().get3DDataValue());
    }

    @Override
    public @NotNull SidedPosGuiData readGuiData(Player player, FriendlyByteBuf buffer) {
        return new SidedPosGuiData(player, buffer.readBlockPos(), Direction.from3DDataValue(buffer.readByte()));
    }
}
