package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.storage.CreativeTankMachine;
import com.gregtechceu.gtceu.common.machine.storage.QuantumTankMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferProxyPartMachine;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;
import snownee.jade.addon.universal.FluidStorageProvider;
import snownee.jade.api.Accessor;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.FluidView;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ViewGroup;
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
public enum GTFluidStorageProvider implements IServerExtensionProvider<MetaMachineBlockEntity, CompoundTag>,
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
    public @Nullable List<ViewGroup<CompoundTag>> getGroups(ServerPlayer serverPlayer, ServerLevel serverLevel,
                                                            MetaMachineBlockEntity mmbe, boolean b) {
        MetaMachine machine = mmbe.getMetaMachine();
        if (machine instanceof QuantumTankMachine qtm) {
            FluidStack stored = qtm.getStored();
            if (stored.isEmpty() && qtm instanceof CreativeTankMachine) return Collections.emptyList();
            CompoundTag tag = JadeForgeUtils.fromFluidStack(stored, qtm.getMaxAmount());
            tag.putBoolean("special", true);
            tag.putLong("amount", qtm.getStoredAmount());
            return List.of(new ViewGroup<>(List.of(tag)));
        } else if (GTCEu.Mods.isAE2Loaded() && machine instanceof MEPatternBufferPartMachine buffer) {
            var tank = buffer.getShareTank();
            List<CompoundTag> list = new ArrayList<>(tank.getTanks());
            for (var storage : tank.getStorages()) {
                var stack = storage.getFluid();
                if (stack.isEmpty()) continue;
                int capacity = storage.getCapacity();
                list.add(JadeForgeUtils.fromFluidStack(stack, capacity));
            }
            return list.isEmpty() ? List.of() : List.of(new ViewGroup<>(list));
        } else if (GTCEu.Mods.isAE2Loaded() && machine instanceof MEPatternBufferProxyPartMachine proxy) {
            var buffer = proxy.getBuffer();
            if (buffer == null) return Collections.emptyList();
            return FluidStorageProvider.INSTANCE.getGroups(serverPlayer, serverLevel, buffer.holder, b);
        }

        return FluidStorageProvider.INSTANCE.getGroups(serverPlayer, serverLevel, mmbe, b);
    }

    // FluidView#readDefault can't handle amount > INT_MAX
    private static FluidView readFluid(CompoundTag tag) {
        if (!tag.contains("special")) return FluidView.readDefault(tag);
        long capacity = tag.getLong("capacity");
        if (capacity <= 0) return null;

        Fluid fluid = BuiltInRegistries.FLUID.get(new ResourceLocation(tag.getString("fluid")));
        CompoundTag nbt = tag.contains("tag") ? tag.getCompound("tag") : null;
        long amount = tag.getLong("amount");
        JadeFluidObject fluidObject = JadeFluidObject.of(fluid, 1000, nbt);
        FluidView fluidView = new FluidView(IElementHelper.get().fluid(fluidObject));
        fluidView.fluidName = fluid.getFluidType().getDescription();
        fluidView.current = FluidTextHelper.getUnicodeMillibuckets(amount, true);
        fluidView.max = FluidTextHelper.getUnicodeMillibuckets(capacity, true);
        fluidView.ratio = Math.min(1f, (float) ((double) amount / capacity));

        return fluidView;
    }
}
