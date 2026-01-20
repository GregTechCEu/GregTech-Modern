package com.gregtechceu.gtceu.integration.top.provider;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.MachineTraitType;

import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public abstract class MachineTraitInfoProvider<T extends MachineTrait> implements IProbeInfoProvider {

    @Getter
    private final ResourceLocation ID;
    private final MachineTraitType<T> traitType;

    public MachineTraitInfoProvider(ResourceLocation id, MachineTraitType<T> traitType) {
        this.ID = id;
        this.traitType = traitType;
    }

    protected abstract void addProbeInfo(T trait, IProbeInfo probeInfo, Player player, BlockEntity blockEntity,
                                         IProbeHitData data);

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState,
                             IProbeHitData data) {
        if (blockState.hasBlockEntity()) {
            BlockEntity blockEntity = world.getBlockEntity(data.getPos());
            if (blockEntity instanceof MetaMachine machine) {
                var t = machine.getTraitHolder().getTrait(traitType);
                if (t != null) addProbeInfo(t, probeInfo, player, blockEntity, data);
            }
        }
    }
}
