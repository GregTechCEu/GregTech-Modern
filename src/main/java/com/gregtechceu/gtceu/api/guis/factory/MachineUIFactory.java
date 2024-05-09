package com.gregtechceu.gtceu.api.guis.factory;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machines.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machines.MetaMachine;
import com.gregtechceu.gtceu.api.machines.feature.IUIMachine;
import com.lowdragmc.lowdraglib.gui.factory.UIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

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

    @OnlyIn(Dist.CLIENT)
    @Override
    protected MetaMachine readHolderFromSyncData(RegistryFriendlyByteBuf syncData) {
        Level world = Minecraft.getInstance().level;
        if (world == null) return null;
        if (world.getBlockEntity(syncData.readBlockPos()) instanceof IMachineBlockEntity holder) {
            return holder.getMetaMachine();
        }
        return null;
    }

    @Override
    protected void writeHolderToSyncData(RegistryFriendlyByteBuf syncData, MetaMachine holder) {
        syncData.writeBlockPos(holder.getPos());
    }
}
