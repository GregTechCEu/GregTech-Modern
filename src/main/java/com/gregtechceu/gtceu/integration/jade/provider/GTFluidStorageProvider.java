package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.machine.storage.CreativeTankMachine;
import com.gregtechceu.gtceu.common.machine.storage.QuantumTankMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferProxyPartMachine;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;
import snownee.jade.addon.universal.FluidStorageProvider;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.FluidView;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ViewGroup;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.util.JadeForgeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Custom FluidView info provider for any machines that require it
 * Currently: Quantum Tanks, Pattern Buffer Proxies
 * Defaults to Jade's normal FluidView provider
 */
public enum GTFluidStorageProvider implements IServerExtensionProvider<CompoundTag>,
        IClientExtensionProvider<CompoundTag, FluidView> {

    INSTANCE;

    @Override
    public ResourceLocation getUid() {
        return GTCEu.id("custom_fluid_storage");
    }

    @Override
    public List<ClientViewGroup<FluidView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<CompoundTag>> groups) {
        return FluidStorageProvider.Extension.INSTANCE.getClientGroups(accessor, groups);
    }

    @Override
    public @Nullable List<ViewGroup<CompoundTag>> getGroups(Accessor<?> accessor) {
        if (accessor.getTarget() instanceof QuantumTankMachine qtm) {
            FluidStack stored = qtm.getStored();
            if (stored.isEmpty() && qtm instanceof CreativeTankMachine) return Collections.emptyList();

            JadeFluidObject fluidObject = JadeFluidObject.of(stored.getFluid(), qtm.getStoredAmount(),
                    stored.getComponentsPatch());
            CompoundTag tag = FluidView.writeDefault(fluidObject, qtm.getMaxAmount());

            return Collections.singletonList(new ViewGroup<>(Collections.singletonList(tag)));
        } else if (GTCEu.Mods.isAE2Loaded() && accessor.getTarget() instanceof MEPatternBufferPartMachine buffer) {
            var tank = buffer.getShareTank();
            List<CompoundTag> list = new ArrayList<>(tank.getTanks());
            for (var storage : tank.getStorages()) {
                FluidStack stack = storage.getFluid();
                if (stack.isEmpty()) continue;

                int capacity = storage.getCapacity();
                list.add(FluidView.writeDefault(JadeForgeUtils.fromFluidStack(stack), capacity));
            }
            return list.isEmpty() ? Collections.emptyList() : Collections.singletonList(new ViewGroup<>(list));
        } else if (GTCEu.Mods.isAE2Loaded() && accessor.getTarget() instanceof MEPatternBufferProxyPartMachine proxy) {
            var buffer = proxy.getBuffer();
            if (buffer == null) return Collections.emptyList();

            Accessor<?> accessor1 = WailaClientRegistration.instance().blockAccessor().from((BlockAccessor) accessor)
                    .blockEntity(buffer.self())
                    .build();
            return FluidStorageProvider.Extension.INSTANCE.getGroups(accessor1);
        }
        return FluidStorageProvider.Extension.INSTANCE.getGroups(accessor);
    }
}
