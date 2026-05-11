package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.machine.storage.CreativeChestMachine;
import com.gregtechceu.gtceu.common.machine.storage.QuantumChestMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferProxyPartMachine;
import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;
import snownee.jade.addon.universal.ItemStorageProvider;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ItemView;
import snownee.jade.api.view.ViewGroup;
import snownee.jade.impl.BlockAccessorImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Custom ItemStack provider for any machines that require it
 * Currently: Quantum Chests, Pattern Buffer Proxies
 * Defaults to Jade's normal ItemStack provider
 */
public enum GTItemStorageProvider implements IServerExtensionProvider<ItemStack>,
        IClientExtensionProvider<ItemStack, ItemView> {

    INSTANCE;

    @Override
    public ResourceLocation getUid() {
        return GTCEu.id("custom_item_storage");
    }

    @Override
    public List<ClientViewGroup<ItemView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<ItemStack>> list) {
        return ItemStorageProvider.Extension.INSTANCE.getClientGroups(accessor, list);
    }

    @Override
    public @Nullable List<ViewGroup<ItemStack>> getGroups(Accessor<?> accessor) {
        if (accessor.getTarget() instanceof QuantumChestMachine qcm) {
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
        } else if (accessor.getTarget() instanceof MEPatternBufferProxyPartMachine proxy) {
            var buffer = proxy.getBuffer();
            if (buffer == null) return Collections.emptyList();
            Accessor<?> accessor1 = new BlockAccessorImpl.Builder().from((BlockAccessor) accessor)
                    .blockEntity(buffer.self())
                    .build();
            return ItemStorageProvider.Extension.INSTANCE.getGroups(accessor1);
        }

        return ItemStorageProvider.Extension.INSTANCE.getGroups(accessor);
    }
}
