package com.gregtechceu.gtceu.api.gui.factory;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.IUICover;

import com.lowdragmc.lowdraglib.gui.factory.UIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CoverUIFactory extends UIFactory<CoverBehavior> {

    public static final CoverUIFactory INSTANCE = new CoverUIFactory();

    public CoverUIFactory() {
        super(GTCEu.id("cover"));
    }

    @Override
    protected ModularUI createUITemplate(CoverBehavior holder, Player entityPlayer) {
        if (holder instanceof IUICover cover) {
            return cover.createUI(entityPlayer);
        }
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    protected CoverBehavior readHolderFromSyncData(FriendlyByteBuf syncData) {
        Level world = Minecraft.getInstance().level;
        if (world == null) return null;
        var pos = syncData.readBlockPos();
        var side = syncData.readEnum(Direction.class);
        var coverable = GTCapabilityHelper.getCoverable(world, pos, side);
        if (coverable != null) {
            return coverable.getCoverAtSide(side);
        }
        return null;
    }

    @Override
    protected void writeHolderToSyncData(FriendlyByteBuf syncData, CoverBehavior holder) {
        syncData.writeBlockPos(holder.coverHolder.getPos());
        syncData.writeEnum(holder.attachedSide);
    }
}
