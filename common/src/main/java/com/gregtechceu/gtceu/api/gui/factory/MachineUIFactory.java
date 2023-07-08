package com.gregtechceu.gtceu.api.gui.factory;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IUIMachine;
import com.lowdragmc.lowdraglib.gui.factory.UIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * @author KilaBash
 * @date 2023/2/17
 * @implNote MachineUIFactory
 */
public class MachineUIFactory extends UIFactory<MetaMachine> {
    public static final MachineUIFactory INSTANCE  = new MachineUIFactory();

    public MachineUIFactory() {
        super(GTCEu.id("machine"));
    }

    @Override
    protected ModularUI createUITemplate(MetaMachine holder, Player entityPlayer) {
        if (holder instanceof IUIMachine machine) {
            return machine.createUI(entityPlayer);
        }
        return null;
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected MetaMachine readHolderFromSyncData(FriendlyByteBuf syncData) {
        Level world = Minecraft.getInstance().level;
        if (world == null) return null;
        if (world.getBlockEntity(syncData.readBlockPos()) instanceof IMachineBlockEntity holder) {
            return holder.getMetaMachine();
        }
        return null;
    }

    @Override
    protected void writeHolderToSyncData(FriendlyByteBuf syncData, MetaMachine holder) {
        syncData.writeBlockPos(holder.getPos());
    }
}
