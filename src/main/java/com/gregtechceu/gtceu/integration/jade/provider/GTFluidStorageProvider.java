package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.storage.CreativeTankMachine;
import com.gregtechceu.gtceu.common.machine.storage.QuantumTankMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.MEInputHatchPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.MEOutputHatchPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferProxyPartMachine;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEFluidList;
import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidStack;

import appeng.api.stacks.AEFluidKey;
import org.jetbrains.annotations.Nullable;
import snownee.jade.addon.universal.FluidStorageProvider;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.FluidView;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ViewGroup;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.util.FluidTextHelper;
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
        return ClientViewGroup.map(groups, GTFluidStorageProvider::readFluid, null);
    }

    @Override
    public @Nullable List<ViewGroup<CompoundTag>> getGroups(Accessor<?> accessor) {
        if (accessor.getTarget() instanceof QuantumTankMachine qtm) {
            FluidStack stored = qtm.getStored();
            if (stored.isEmpty() && qtm instanceof CreativeTankMachine) return Collections.emptyList();
            if (stored.isEmpty() && qtm.isLocked()) stored = qtm.getLockedFluid();
            CompoundTag tag = FluidView.writeDefault(JadeForgeUtils.fromFluidStack(stored.copyWithAmount(1000)),
                    qtm.getMaxAmount());
            tag.putBoolean("special", true);
            return List.of(new ViewGroup<>(List.of(tag)));
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
        } else if (GTCEu.Mods.isAE2Loaded() && accessor.getTarget() instanceof MEInputHatchPartMachine buffer) {
            var tanks = ((ExportOnlyAEFluidList) buffer.tank).getInventory();
            List<CompoundTag> list = new ArrayList<>(tanks.length);
            for (var storage : tanks) {
                var stack = storage.getFluid();
                if (stack.isEmpty()) continue;
                int capacity = storage.getFluidConfig().getAmount();
                capacity = (capacity == 1 ? stack.getAmount() : capacity);
                list.add(FluidView.writeDefault(JadeForgeUtils.fromFluidStack(stack), capacity));
            }
            return list.isEmpty() ? Collections.emptyList() : List.of(new ViewGroup<>(list));
        } else if (GTCEu.Mods.isAE2Loaded() && accessor.getTarget() instanceof MEOutputHatchPartMachine hatch) {
            List<CompoundTag> list = new ArrayList<>();
            var iterator = hatch.storageIterator();
            while (iterator.hasNext()) {
                var entry = iterator.next();
                if (entry.getKey() instanceof AEFluidKey fluidKey) {
                    FluidStack stack = fluidKey.toStack(GTMath.saturatedCast(entry.getLongValue()));
                    list.add(FluidView.writeDefault(JadeForgeUtils.fromFluidStack(stack), entry.getLongValue()));
                }
            }
            return list.isEmpty() ? Collections.emptyList() : List.of(new ViewGroup<>(list));
        } else if (accessor.getTarget() instanceof FluidHatchPartMachine hatch) {
            if (hatch.tank.getTanks() == 1 && hatch.tank.getFluidInTank(0).isEmpty() && hatch.tank.isLocked()) {
                FluidStack stored = hatch.tank.getLockedFluid().getFluid();
                CompoundTag tag = FluidView.writeDefault(JadeForgeUtils.fromFluidStack(stored.copyWithAmount(1000)),
                        hatch.tank.getTankCapacity(0));
                tag.putBoolean("special", true);
                return List.of(new ViewGroup<>(List.of(tag)));
            }
        }

        return FluidStorageProvider.Extension.INSTANCE.getGroups(accessor);
    }

    // FluidView#readDefault can't handle amount = 0
    private static FluidView readFluid(CompoundTag tag) {
        if (!tag.contains("special")) return FluidView.readDefault(tag);

        long capacity = tag.getLong("capacity");
        if (capacity <= 0) return null;

        FluidView fluidView = FluidView.readDefault(tag);
        if (fluidView == null) return null;

        fluidView.current = FluidTextHelper.getUnicodeMillibuckets(0, true);
        fluidView.ratio = (float) (0d / (double) capacity);

        return fluidView;
    }
}
