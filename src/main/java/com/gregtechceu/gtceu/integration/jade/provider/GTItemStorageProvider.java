package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.common.machine.storage.CreativeChestMachine;
import com.gregtechceu.gtceu.common.machine.storage.QuantumChestMachine;
import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;
import snownee.jade.addon.universal.ItemStorageProvider;
import snownee.jade.api.Accessor;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ItemView;
import snownee.jade.api.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom ItemStack provider for any machines that require it
 * Currently: Quantum Chests
 * Defaults to Jade's normal ItemStack provider
 */
public enum GTItemStorageProvider implements IServerExtensionProvider<MetaMachineBlockEntity, ItemStack>,
        IClientExtensionProvider<ItemStack, ItemView> {

    INSTANCE;

    @Override
    public ResourceLocation getUid() {
        return GTCEu.id("custom_item_storage");
    }

    @Override
    public List<ClientViewGroup<ItemView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<ItemStack>> list) {
        return ItemStorageProvider.INSTANCE.getClientGroups(accessor, list);
    }

    @Override
    public @Nullable List<ViewGroup<ItemStack>> getGroups(ServerPlayer serverPlayer, ServerLevel serverLevel,
                                                          MetaMachineBlockEntity mmbe, boolean b) {
        if (mmbe.getMetaMachine() instanceof QuantumChestMachine qcm) {
            ItemStack stored = qcm.getStored();
            long amount = qcm.getStoredAmount();
            if (qcm instanceof CreativeChestMachine ccm) {
                amount = (long) Math.ceil(1d * ccm.getItemsPerCycle() / ccm.getTicksPerCycle());
            }
            List<ItemStack> list = new ArrayList<>();
            for (int stack : GTMath.split(amount)) {
                list.add(stored.copyWithCount(stack));
            }
            return list.isEmpty() ? List.of() : List.of(new ViewGroup<>(list));
        }
        return ItemStorageProvider.INSTANCE.getGroups(serverPlayer, serverLevel, mmbe, b);
    }
}
