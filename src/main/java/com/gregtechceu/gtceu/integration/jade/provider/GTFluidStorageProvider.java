package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.common.machine.storage.CreativeTankMachine;
import com.gregtechceu.gtceu.common.machine.storage.QuantumTankMachine;

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

import java.util.List;

/**
 * Custom FluidView info provider for any machines that require it
 * Currently: Quantum Tanks
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
        if (mmbe.getMetaMachine() instanceof QuantumTankMachine qtm) {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("special", true);
            FluidStack stored = qtm.getStored();
            tag.putString("fluid", BuiltInRegistries.FLUID.getKey(stored.getFluid()).toString());
            long amount = qtm.getStoredAmount();
            if (qtm instanceof CreativeTankMachine ctm) {
                amount = (long) Math.ceil(1d * ctm.getMBPerCycle() / ctm.getTicksPerCycle());
            }
            tag.putLong("amount", amount);
            tag.putLong("capacity", qtm.getMaxAmount());
            if (stored.hasTag()) tag.put("tag", stored.getTag());
            return List.of(new ViewGroup<>(List.of(tag)));
        }

        return FluidStorageProvider.INSTANCE.getGroups(serverPlayer, serverLevel, mmbe, b);
    }

    // FluidView#readDefault can't handle amount > INT_MAX
    private static FluidView readFluid(CompoundTag tag) {
        if (!tag.contains("special")) return FluidView.readDefault(tag);
        long capacity = tag.getLong("capacity");
        if (capacity <= 0) return null;

        Fluid fluid = BuiltInRegistries.FLUID.get(new ResourceLocation(tag.getString("fluid")));
        CompoundTag nbt = tag.contains("nbt") ? tag.getCompound("nbt") : null;
        long amount = tag.getLong("amount");
        JadeFluidObject fluidObject = JadeFluidObject.of(fluid, 1000, nbt);
        FluidView fluidView = new FluidView(IElementHelper.get().fluid(fluidObject));
        fluidView.fluidName = fluid.getFluidType().getDescription();
        fluidView.current = FluidTextHelper.getUnicodeMillibuckets(amount, true);
        fluidView.max = FluidTextHelper.getUnicodeMillibuckets(capacity, true);
        fluidView.ratio = (float) ((double) amount / capacity);

        return fluidView;
    }
}
