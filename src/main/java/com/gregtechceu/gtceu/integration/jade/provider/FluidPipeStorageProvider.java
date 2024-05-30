package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.blockentity.FluidPipeBlockEntity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import org.jetbrains.annotations.Nullable;
import snownee.jade.api.Accessor;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.view.*;

import java.util.ArrayList;
import java.util.List;

public enum FluidPipeStorageProvider implements IServerExtensionProvider<FluidPipeBlockEntity, CompoundTag>,
        IClientExtensionProvider<CompoundTag, FluidView> {

    INSTANCE;

    @Override
    public List<ClientViewGroup<FluidView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<CompoundTag>> groups) {
        return ClientViewGroup.map(groups, FluidView::readDefault, (group, clientGroup) -> {
            if (group.id != null) {
                clientGroup.title = Component.literal(group.id);
            }
            clientGroup.bgColor = 0x55666666;
        });
    }

    @Override
    public @Nullable List<ViewGroup<CompoundTag>> getGroups(ServerPlayer serverPlayer, ServerLevel serverLevel,
                                                            FluidPipeBlockEntity pipe, boolean showDetails) {
        List<ViewGroup<CompoundTag>> tanks = new ArrayList<>();
        for (var tank : pipe.getFluidTanks()) {
            if (tank.getFluidAmount() > 0) {
                tanks.add(new ViewGroup<>(List.of(FluidView.writeDefault(
                        JadeFluidObject.of(tank.getFluid().getFluid(), tank.getFluidAmount()), tank.getCapacity()))));
            }
        }
        return tanks;
    }

    @Override
    public ResourceLocation getUid() {
        return GTCEu.id("fluid_storage");
    }
}
