package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.storage.CreativeChestMachine;
import com.gregtechceu.gtceu.common.machine.storage.QuantumChestMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferProxyPartMachine;
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
import java.util.Collections;
import java.util.List;

/**
 * Custom ItemStack provider for any machines that require it
 * Currently: Quantum Chests, Pattern Buffer Proxies
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
        MetaMachine machine = mmbe.getMetaMachine();
        if (machine instanceof QuantumChestMachine qcm) {
            ItemStack stored = qcm.getStored();
            long amount = qcm.getStoredAmount();
            if (qcm instanceof CreativeChestMachine ccm) {
                amount = (long) Math.ceil(1d * ccm.getItemsPerCycle() / ccm.getTicksPerCycle());
            }
            List<ItemStack> list = new ArrayList<>();
            for (int stack : GTMath.split(amount)) {
                list.add(stored.copyWithCount(stack));
            }
            return list.isEmpty() ? Collections.emptyList() : List.of(new ViewGroup<>(list));
        } else if (machine instanceof MEPatternBufferProxyPartMachine proxy) {
            var buffer = proxy.getBuffer();
            if (buffer == null) return Collections.emptyList();
            return ItemStorageProvider.INSTANCE.getGroups(serverPlayer, serverLevel, buffer.holder, b);
        }

        return ItemStorageProvider.INSTANCE.getGroups(serverPlayer, serverLevel, mmbe, b);
    }
}
